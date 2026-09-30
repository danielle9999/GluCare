package com.example.glucare.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.glucare.GlucareApplication
import com.example.glucare.R
import java.text.SimpleDateFormat
import java.util.*

class ChatbotActivity : BaseActivity() {

    private lateinit var rvChat: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: Button
    private lateinit var typingIndicator: TextView
    private lateinit var btnBack: ImageView
    private lateinit var app: GlucareApplication

    private val messages = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatAdapter

    // Track conversation context
    private var lastTopic: String = ""
    private var awaitingGlucoseValue: Boolean = false

    // Get current language
    private val currentLanguage: String
        get() = getSharedPreferences("settings", MODE_PRIVATE)
            .getString("app_language", "en") ?: "en"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatbot)

        app = application as GlucareApplication

        rvChat = findViewById(R.id.rvChat)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)
        typingIndicator = findViewById(R.id.typingIndicator)
        btnBack = findViewById(R.id.btnBack)

        btnBack.setOnClickListener { finish() }

        adapter = ChatAdapter(messages)
        rvChat.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        rvChat.adapter = adapter

        btnSend.setOnClickListener { sendMessage() }
        etMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else false
        }

        setupChips()
        showWelcomeMessage()
    }

    private fun setupChips() {
        val lowText = when (currentLanguage) {
            "fr" -> "Que faire en cas d'hypoglycémie?"
            "ar" -> "ماذا تفعل عند انخفاض السكر؟"
            else -> "What to do for low blood sugar?"
        }
        val highText = when (currentLanguage) {
            "fr" -> "Que faire en cas d'hyperglycémie?"
            "ar" -> "ماذا تفعل عند ارتفاع السكر؟"
            else -> "What to do for high blood sugar?"
        }
        val insulinText = when (currentLanguage) {
            "fr" -> "Comment fonctionne l'insuline?"
            "ar" -> "كيف يعمل الأنسولين؟"
            else -> "How does insulin work?"
        }
        val dietText = when (currentLanguage) {
            "fr" -> "Quels aliments dois-je manger?"
            "ar" -> "ما هي الأطعمة التي يجب أن أتناولها؟"
            else -> "What foods should I eat?"
        }
        val exerciseText = when (currentLanguage) {
            "fr" -> "L'exercice est-il sûr pour les diabétiques?"
            "ar" -> "هل التمارين الرياضية آمنة لمرضى السكري؟"
            else -> "Is exercise safe for diabetics?"
        }
        val symptomsText = when (currentLanguage) {
            "fr" -> "Quels sont les symptômes du diabète?"
            "ar" -> "ما هي أعراض مرض السكري؟"
            else -> "What are the symptoms of diabetes?"
        }

        findViewById<TextView>(R.id.chipHypo).setOnClickListener { askQuestion(lowText) }
        findViewById<TextView>(R.id.chipHyper).setOnClickListener { askQuestion(highText) }
        findViewById<TextView>(R.id.chipInsulin).setOnClickListener { askQuestion(insulinText) }
        findViewById<TextView>(R.id.chipDiet).setOnClickListener { askQuestion(dietText) }
        findViewById<TextView>(R.id.chipExercise).setOnClickListener { askQuestion(exerciseText) }
        findViewById<TextView>(R.id.chipSymptoms).setOnClickListener { askQuestion(symptomsText) }
    }

    private fun showWelcomeMessage() {
        val userName = app.sessionManager.getUserName() ?:
        when (currentLanguage) {
            "fr" -> "Utilisateur"
            "ar" -> "مستخدم"
            else -> "User"
        }
        val latestGlucose = app.databaseHelper.getLatestGlucoseReading(app.sessionManager.getUserId())

        val glucoseInfo = if (latestGlucose != null) {
            val status = when {
                latestGlucose.value < 70 -> when (currentLanguage) {
                    "fr" -> "⚠️ Bas"
                    "ar" -> "⚠️ منخفض"
                    else -> "⚠️ Low"
                }
                latestGlucose.value <= 140 -> when (currentLanguage) {
                    "fr" -> "✅ Normal"
                    "ar" -> "✅ طبيعي"
                    else -> "✅ Normal"
                }
                latestGlucose.value <= 180 -> when (currentLanguage) {
                    "fr" -> "⚠️ Élevé"
                    "ar" -> "⚠️ مرتفع"
                    else -> "⚠️ Elevated"
                }
                else -> when (currentLanguage) {
                    "fr" -> "🔴 Très élevé"
                    "ar" -> "🔴 مرتفع جداً"
                    else -> "🔴 High"
                }
            }
            val lastText = when (currentLanguage) {
                "fr" -> "Votre dernière glycémie"
                "ar" -> "آخر قراءة للسكر لديك"
                else -> "Your last glucose"
            }
            "\n\n📊 $lastText: ${latestGlucose.value} mg/dL ($status)"
        } else { "" }

        val welcomeMessage = when (currentLanguage) {
            "fr" -> """
👋 Bonjour $userName! Je suis votre assistant Glucare pour le diabète.$glucoseInfo

Je peux vous aider avec:
• 🔴 Urgences d'hypoglycémie/hyperglycémie
• 💉 Questions sur l'insuline et dosage
• 🥗 Conseils nutritionnels
• 🏃 Conseils de sécurité pour l'exercice
• 🔍 Symptômes du diabète
• 💊 Rappels de médicaments
• 📊 Analyse de vos tendances glycémiques

Tapez votre question ou appuyez sur un sujet ci-dessous!
            """.trimIndent()
            "ar" -> """
👋 مرحباً $userName! أنا مساعد Glucare لمرض السكري.$glucoseInfo

يمكنني مساعدتك في:
• 🔴 حالات الطوارئ لارتفاع/انخفاض السكر
• 💉 أسئلة الأنسولين والجرعات
• 🥗 نصائح التغذية والنظام الغذائي
• 🏃 نصائح سلامة التمارين
• 🔍 أعراض مرض السكري
• 💊 تذكيرات الأدوية
• 📊 قراءة اتجاهات السكر لديك

اكتب سؤالك أو اضغط على موضوع أدناه!
            """.trimIndent()
            else -> """
👋 Hello $userName! I'm your Glucare Diabetes Assistant.$glucoseInfo

I can help you with:
• 🔴 Low/High blood sugar emergencies
• 💉 Insulin questions & dosing
• 🥗 Diet & nutrition advice
• 🏃 Exercise safety tips
• 🔍 Diabetes symptoms
• 💊 Medication reminders
• 📊 Reading your glucose trends

Just type your question or tap a topic below!
            """.trimIndent()
        }
        addMessage(welcomeMessage, false)
    }

    private fun askQuestion(question: String) {
        etMessage.setText(question)
        sendMessage()
    }

    private fun sendMessage() {
        val text = etMessage.text.toString().trim()
        if (text.isEmpty()) return
        etMessage.text.clear()
        addMessage(text, true)
        typingIndicator.visibility = View.VISIBLE
        rvChat.postDelayed({
            typingIndicator.visibility = View.GONE
            addMessage(getSmartResponse(text), false)
        }, 500)
    }

    private fun getSmartResponse(input: String): String {
        val lower = input.lowercase()
        val lang = currentLanguage

        if (awaitingGlucoseValue) {
            awaitingGlucoseValue = false
            val glucose = input.toIntOrNull()
            if (glucose != null) return analyzeGlucoseValue(glucose)
            return when (lang) {
                "fr" -> "Veuillez entrer un nombre de glycémie valide (ex: 120)."
                "ar" -> "الرجاء إدخال رقم سكر صالح (مثال: 120)."
                else -> "Please enter a valid glucose number (e.g., 120)."
            }
        }

        // Multi-language keyword detection
        when {
            lower.contains("low") || lower.contains("hypo") || lower.contains("bas") || lower.contains("faible") || lower.contains("منخفض") -> lastTopic = "hypo"
            lower.contains("high") || lower.contains("hyper") || lower.contains("élevé") || lower.contains("مرتفع") -> lastTopic = "hyper"
            lower.contains("insulin") || lower.contains("insuline") || lower.contains("الأنسولين") -> lastTopic = "insulin"
            lower.contains("food") || lower.contains("diet") || lower.contains("eat") || lower.contains("nourriture") || lower.contains("manger") || lower.contains("أكل") -> lastTopic = "diet"
            lower.contains("exercise") || lower.contains("sport") || lower.contains("exercice") || lower.contains("تمارين") -> lastTopic = "exercise"
            lower.contains("symptom") || lower.contains("symptôme") || lower.contains("أعراض") -> lastTopic = "symptoms"
            lower.contains("a1c") || lower.contains("hba1c") -> lastTopic = "a1c"
            lower.contains("medication") || lower.contains("pill") || lower.contains("médicament") || lower.contains("دواء") -> lastTopic = "medication"
            lower.contains("foot") || lower.contains("pied") || lower.contains("قدم") -> lastTopic = "foot"
            lower.contains("stress") || lower.contains("stresse") || lower.contains("توتر") -> lastTopic = "stress"
            lower.contains("ramadan") || lower.contains("fast") || lower.contains("jeûne") || lower.contains("صوم") -> lastTopic = "ramadan"
        }

        val checkPhrases = listOf("check my glucose", "analyze my glucose", "vérifier ma glycémie", "تحليل السكر")
        if (checkPhrases.any { lower.contains(it) }) {
            awaitingGlucoseValue = true
            return when (lang) {
                "fr" -> "Veuillez entrer votre lecture de glycémie actuelle (mg/dL):"
                "ar" -> "الرجاء إدخال قراءة السكر الحالية لديك (ملجم/دل):"
                else -> "Please enter your current glucose reading (mg/dL):"
            }
        }

        val latestGlucose = app.databaseHelper.getLatestGlucoseReading(app.sessionManager.getUserId())

        return when {
            lower.contains("low") || lower.contains("hypo") || lower.contains("70") ||
                    lower.contains("bas") || lower.contains("faible") || lower.contains("منخفض") -> {
                when (lang) {
                    "fr" -> """
⚠️ TAUX DE SUCRE BAS (Hypoglycémie) - En dessous de 70 mg/dL

🚨 ACTION IMMÉDIATE (Règle de 15):

1️⃣ Mangez/Buvez 15g de GLUCIDES RAPIDES:
   • 1/2 tasse (120ml) de jus de fruit ou soda
   • 3-4 comprimés de glucose
   • 1 cuillère à soupe de miel ou sucre
   • 5-6 bonbons durs

2️⃣ ATTENDEZ 15 minutes

3️⃣ REVÉRIFIEZ votre glycémie

4️⃣ Si toujours bas → RÉPÉTEZ l'étape 1

✅ Une fois normal (>70), mangez une petite collation

🚑 Si inconscient → APPELEZ LES SECOURS

${if (latestGlucose != null && latestGlucose.value < 70) "\n⚠️ VOTRE DERNIÈRE LECTURE ÉTAIT ${latestGlucose.value} mg/dL - SUIVEZ LES ÉTAPES!" else ""}
                    """.trimIndent()
                    "ar" -> """
⚠️ انخفاض سكر الدم - أقل من 70 ملجم/دل

🚨 إجراء فوري (قاعدة 15):

1️⃣ تناول/اشرب 15 جرام كربوهيدرات سريعة:
   • نصف كوب عصير فواكه أو صودا
   • 3-4 أقراص جلوكوز
   • ملعقة كبيرة عسل أو سكر

2️⃣ انتظر 15 دقيقة

3️⃣ أعد فحص السكر

4️⃣ إذا لا يزال منخفضاً → كرر الخطوة 1

✅ عندما يعود طبيعياً، تناول وجبة خفيفة

🚑 إذا فقد الوعي → اتصل بالإسعاف

${if (latestGlucose != null && latestGlucose.value < 70) "\n⚠️ كانت آخر قراءة لك ${latestGlucose.value} - اتبع الخطوات!" else ""}
                    """.trimIndent()
                    else -> """
⚠️ LOW BLOOD SUGAR (Hypoglycemia) - Below 70 mg/dL

🚨 IMMEDIATE ACTION (Rule of 15):

1️⃣ Eat/Drink 15g FAST-ACTING CARBS:
   • 1/2 cup (120ml) fruit juice or regular soda
   • 3-4 glucose tablets
   • 1 tablespoon honey or sugar

2️⃣ WAIT 15 minutes

3️⃣ RE-CHECK glucose

4️⃣ If still below 70 → REPEAT step 1

✅ Once normal, eat a small snack

🚑 If unconscious → CALL EMERGENCY

${if (latestGlucose != null && latestGlucose.value < 70) "\n⚠️ YOUR LAST READING WAS ${latestGlucose.value} mg/dL - FOLLOW THE STEPS!" else ""}
                    """.trimIndent()
                }
            }

            lower.contains("severe") && (lower.contains("low") || lower.contains("bas")) ||
                    lower.contains("unconscious") || lower.contains("inconscient") || lower.contains("فاقد") -> {
                when (lang) {
                    "fr" -> """
🚨 URGENCE MÉDICALE - HYPOGLYCÉMIE SÉVÈRE

Si la personne est inconsciente ou a une convulsion:
• NE PAS donner à manger ou à boire
• Administrer du Glucagon si disponible
• APPELER LES SECOURS IMMÉDIATEMENT
• Placer en position latérale de sécurité

⚠️ C'est une urgence vitale - n'attendez pas!
                    """.trimIndent()
                    "ar" -> """
🚨 حالة طبية طارئة - انخفاض حاد في السكر

إذا كان الشخص فاقداً للوعي أو يعاني من نوبة:
• لا تعطيه طعام أو شراب
• أعط حقنة الجلوكاجون إذا كانت متوفرة
• اتصل بالإسعاف فوراً
• ضعه في وضعية الاستشفاء

⚠️ هذه حالة تهدد الحياة - لا تنتظر!
                    """.trimIndent()
                    else -> """
🚨 MEDICAL EMERGENCY - SEVERE HYPOGLYCEMIA

If person is unconscious or having seizure:
• DO NOT give food or drink
• Administer Glucagon if available
• CALL EMERGENCY IMMEDIATELY
• Place in recovery position

⚠️ This is life-threatening - do not wait!
                    """.trimIndent()
                }
            }

            lower.contains("high") || lower.contains("hyper") || lower.contains("180") ||
                    lower.contains("élevé") || lower.contains("مرتفع") -> {
                when (lang) {
                    "fr" -> """
⚠️ TAUX DE SUCRE ÉLEVÉ (Hyperglycémie) - Au-dessus de 180 mg/dL

✅ MESURES IMMÉDIATES:
1️⃣ Buvez 500-600ml d'eau
2️⃣ Vérifiez les cétones si >250 mg/dL
3️⃣ Marche légère 15-20 minutes (si <300)
4️⃣ Prenez l'insuline de correction
5️⃣ Revérifiez dans 2 heures

⚠️ CONSULTEZ UN MÉDECIN SI:
• Cétones présentes
• Nausées ou vomissements
• Haleine fruitée

📌 Causes fréquentes: Insuline oubliée, repas riche en glucides, stress
                    """.trimIndent()
                    "ar" -> """
⚠️ ارتفاع سكر الدم - أعلى من 180 ملجم/دل

✅ إجراءات فورية:
1️⃣ اشرب 500-600 مل ماء
2️⃣ افحص الكيتونات إذا كان >250
3️⃣ مشي خفيف 15-20 دقيقة (إذا كان <300)
4️⃣ خذ الأنسولين التصحيحي
5️⃣ أعد الفحص بعد ساعتين

⚠️ استشر الطبيب إذا:
• وجود كيتونات
• غثيان أو قيء
• رائحة فم فاكهية

📌 الأسباب الشائعة: جرعة أنسولين منسية، وجبة عالية الكربوهيدرات، إجهاد
                    """.trimIndent()
                    else -> """
⚠️ HIGH BLOOD SUGAR (Hyperglycemia) - Above 180 mg/dL

✅ IMMEDIATE STEPS:
1️⃣ Drink 16-20 oz (500-600ml) of water
2️⃣ Check for ketones if >250 mg/dL
3️⃣ Light walk 15-20 minutes (if <300)
4️⃣ Take correction insulin
5️⃣ Recheck in 2 hours

⚠️ SEEK MEDICAL HELP IF:
• Ketones present
• Nausea or vomiting
• Fruity breath odor

📌 Common causes: Missed insulin, high-carb meal, stress
                    """.trimIndent()
                }
            }

            lower.contains("ketone") || lower.contains("dka") || lower.contains("acidosis") ||
                    lower.contains("cétone") || lower.contains("acidocétose") || lower.contains("كيتون") -> {
                when (lang) {
                    "fr" -> """
🚨 ACIDOCÉTOSE DIABÉTIQUE (DKA) - URGENCE MÉDICALE

⚠️ SIGNES:
• Glycémie très élevée (>250 mg/dL)
• Nausées, vomissements
• Haleine fruitée
• Respiration rapide
• Confusion

🚑 AGISSEZ:
1️⃣ APPELEZ LES SECOURS
2️⃣ Ne sautez PAS l'insuline
3️⃣ Buvez de l'eau
4️⃣ Ne faites PAS d'exercice
                    """.trimIndent()
                    "ar" -> """
🚨 الحماض الكيتوني السكري - حالة طبية طارئة

⚠️ العلامات:
• سكر مرتفع جداً (>250)
• غثيان، قيء
• رائحة فم فاكهية
• تنفس سريع
• ارتباك

🚑 تصرف:
1️⃣ اتصل بالإسعاف
2️⃣ لا تتخطى الأنسولين
3️⃣ اشرب الماء
4️⃣ لا تمارس الرياضة
                    """.trimIndent()
                    else -> """
🚨 DIABETIC KETOACIDOSIS (DKA) - MEDICAL EMERGENCY

⚠️ WARNING SIGNS:
• Very high glucose (>250 mg/dL)
• Nausea, vomiting
• Fruity breath
• Rapid breathing
• Confusion

🚑 ACT NOW:
1️⃣ CALL EMERGENCY
2️⃣ Do NOT skip insulin
3️⃣ Drink water
4️⃣ Do NOT exercise
                    """.trimIndent()
                }
            }

            // INSULIN DOSE CALCULATION
            lower.contains("insulin") && (lower.contains("dose") || lower.contains("calculat") ||
                    lower.contains("dose") || lower.contains("calcul") || lower.contains("جرعة")) -> {
                when (lang) {
                    "fr" -> """
💉 CALCUL DE LA DOSE D'INSULINE

Formule: Dose totale = Dose Glucides + Dose Correction

1️⃣ DOSE GLUCIDES = Glucides (g) ÷ Ratio
   Exemple: 60g ÷ 10 = 6 unités

2️⃣ DOSE CORRECTION = (Glycémie actuelle - Cible) ÷ Facteur
   Exemple: (220 - 120) ÷ 50 = 2 unités

📊 TOTAL = 6 + 2 = 8 unités

🎯 Utilisez le calculateur d'insuline dans l'application!

⚠️ Vérifiez toujours avec votre médecin.
                    """.trimIndent()
                    "ar" -> """
💉 حساب جرعة الأنسولين

المعادلة: الجرعة الكلية = جرعة الكربوهيدرات + جرعة التصحيح

1️⃣ جرعة الكربوهيدرات = الكربوهيدرات (جم) ÷ النسبة
   مثال: 60 جم ÷ 10 = 6 وحدات

2️⃣ جرعة التصحيح = (السكر الحالي - الهدف) ÷ عامل التصحيح
   مثال: (220 - 120) ÷ 50 = 2 وحدة

📊 المجموع = 6 + 2 = 8 وحدات

🎯 استخدم حاسبة الأنسولين في التطبيق!

⚠️ تحقق دائماً مع طبيبك.
                    """.trimIndent()
                    else -> """
💉 INSULIN DOSE CALCULATION

Formula: Total Dose = Carb Dose + Correction Dose

1️⃣ CARB DOSE = Carbs (g) ÷ Ratio
   Example: 60g ÷ 10 = 6 units

2️⃣ CORRECTION DOSE = (Current - Target) ÷ Factor
   Example: (220 - 120) ÷ 50 = 2 units

📊 TOTAL = 6 + 2 = 8 units

🎯 Use the Insulin Calculator in the app!

⚠️ Always verify with your doctor.
                    """.trimIndent()
                }
            }

            // INSULIN TYPES
            lower.contains("insulin") && (lower.contains("type") || lower.contains("kind") ||
                    lower.contains("type") || lower.contains("نوع")) -> {
                when (lang) {
                    "fr" -> """
💉 TYPES D'INSULINE

⚡ RAPIDE (Humalog, NovoLog)
• Début: 15 min • Pic: 1-2h • Durée: 3-4h

📌 COURTE (Regular)
• Début: 30 min • Pic: 2-3h • Durée: 4-6h

🔄 INTERMÉDIAIRE (NPH)
• Début: 2h • Pic: 4-8h • Durée: 12-18h

🌙 LONGUE (Lantus, Levemir)
• Début: 2h • Couverture: 24h+

💡 La plupart utilisent rapide + longue.
                    """.trimIndent()
                    "ar" -> """
💉 أنواع الأنسولين

⚡ سريع المفعول (Humalog, NovoLog)
• يبدأ: 15 دقيقة • الذروة: 1-2 ساعة • المدة: 3-4 ساعات

📌 قصير المفعول (Regular)
• يبدأ: 30 دقيقة • الذروة: 2-3 ساعات • المدة: 4-6 ساعات

🔄 متوسط المفعول (NPH)
• يبدأ: ساعتين • الذروة: 4-8 ساعات • المدة: 12-18 ساعة

🌙 طويل المفعول (Lantus, Levemir)
• يبدأ: ساعتين • التغطية: 24 ساعة+

💡 معظم الناس يستخدمون سريع + طويل المفعول.
                    """.trimIndent()
                    else -> """
💉 TYPES OF INSULIN

⚡ RAPID-ACTING (Humalog, NovoLog)
• Starts: 15 min • Peaks: 1-2h • Duration: 3-4h

📌 SHORT-ACTING (Regular)
• Starts: 30 min • Peaks: 2-3h • Duration: 4-6h

🔄 INTERMEDIATE (NPH)
• Starts: 2h • Peaks: 4-8h • Duration: 12-18h

🌙 LONG-ACTING (Lantus, Levemir)
• Starts: 2h • Coverage: 24h+

💡 Most people use rapid + long-acting.
                    """.trimIndent()
                }
            }

            // DIET
            lower.contains("food") || lower.contains("diet") || lower.contains("eat") || lower.contains("meal") ||
                    lower.contains("nourriture") || lower.contains("manger") || lower.contains("أكل") -> {
                when (lang) {
                    "fr" -> """
🥗 GUIDE ALIMENTAIRE POUR DIABÉTIQUES

✅ BONS CHOIX:

🥬 LÉGUMES (illimités):
• Légumes verts, brocoli, concombre

🥩 PROTÉINES:
• Poulet, dinde, poisson, œufs, tofu

🌾 CÉRÉALES COMPLÈTES:
• Avoine, quinoa, riz brun

🥑 BONNES GRAISSES:
• Avocat, huile d'olive, noix

🍓 FRUITS (1-2 portions):
• Baies, pomme, poire, orange

❌ À ÉVITER:
• Boissons sucrées
• Pain blanc, riz blanc
• Pâtisseries, bonbons

📏 MÉTHODE DE L'ASSIETTE:
½ légumes, ¼ protéines, ¼ céréales
                    """.trimIndent()
                    "ar" -> """
🥗 دليل الأكل الصحي لمرضى السكري

✅ الخيارات الجيدة:

🥬 الخضروات (بدون حدود):
• الخضروات الورقية، بروكلي، خيار

🥩 البروتين:
• دجاج، ديك رومي، سمك، بيض، توفو

🌾 الحبوب الكاملة:
• شوفان، كينوا، أرز بني

🥑 الدهون الصحية:
• أفوكادو، زيت زيتون، مكسرات

🍓 الفواكه (1-2 حصة):
• توت، تفاح، كمثرى، برتقال

❌ تجنب:
• المشروبات السكرية
• الخبز الأبيض، الأرز الأبيض
• الحلويات

📏 طريقة الطبق:
½ خضروات، ¼ بروتين، ¼ حبوب كاملة
                    """.trimIndent()
                    else -> """
🥗 DIABETIC-FRIENDLY FOOD GUIDE

✅ GOOD CHOICES:

🥬 VEGETABLES (unlimited):
• Leafy greens, broccoli, cucumber

🥩 PROTEIN:
• Chicken, turkey, fish, eggs, tofu

🌾 WHOLE GRAINS:
• Oats, quinoa, brown rice

🥑 HEALTHY FATS:
• Avocado, olive oil, nuts

🍓 FRUITS (1-2 servings):
• Berries, apple, pear, orange

❌ LIMIT OR AVOID:
• Sugary drinks
• White bread, white rice
• Sweets, pastries

📏 PLATE METHOD:
½ veggies, ¼ protein, ¼ whole grains
                    """.trimIndent()
                }
            }

            // EXERCISE
            lower.contains("exercise") || lower.contains("workout") || lower.contains("walk") ||
                    lower.contains("exercice") || lower.contains("تمارين") -> {
                when (lang) {
                    "fr" -> """
🏃 GUIDE DE SÉCURITÉ POUR L'EXERCICE

✅ AVANT L'EXERCICE:
• Vérifiez glycémie (idéal: 100-180)
• Si <100: mangez 15g glucides
• Si >250 avec cétones: NE PAS faire d'exercice

✅ PENDANT:
• Gardez des glucides rapides
• Restez hydraté

✅ APRÈS:
• Revérifiez glycémie
• Attention aux hypoglycémies tardives

💡 RECOMMANDÉ:
• 150 minutes/semaine d'activité modérée
• Marche, natation, vélo

⚠️ Si étourdi → STOP et vérifiez glycémie
                    """.trimIndent()
                    "ar" -> """
🏃 دليل سلامة التمارين الرياضية

✅ قبل التمرين:
• افحص السكر (مثالي: 100-180)
• إذا كان <100: تناول 15 جرام كربوهيدرات
• إذا كان >250 مع كيتونات: لا تمارس الرياضة

✅ أثناء التمرين:
• احتفظ بكربوهيدرات سريعة
• حافظ على ترطيب جسمك

✅ بعد التمرين:
• أعد فحص السكر
• انتبه للانخفاض المتأخر

💡 الموصى به:
• 150 دقيقة/أسبوع نشاط معتدل
• مشي، سباحة، دراجة

⚠️ إذا شعرت بدوخة → توقف وافحص السكر
                    """.trimIndent()
                    else -> """
🏃 EXERCISE SAFETY GUIDE

✅ BEFORE EXERCISE:
• Check glucose (ideal: 100-180)
• If <100: eat 15g carbs
• If >250 with ketones: DO NOT exercise

✅ DURING:
• Keep fast-acting carbs nearby
• Stay hydrated

✅ AFTER:
• Recheck glucose
• Watch for delayed lows

💡 RECOMMENDED:
• 150 minutes/week moderate activity
• Walking, swimming, cycling

⚠️ If dizzy → STOP and check glucose
                    """.trimIndent()
                }
            }

            // SYMPTOMS
            lower.contains("symptom") || lower.contains("sign") || lower.contains("feel") ||
                    lower.contains("symptôme") || lower.contains("أعراض") -> {
                when (lang) {
                    "fr" -> """
🔍 SYMPTÔMES COURANTS DU DIABÈTE

Type 1 (apparition soudaine):
• Soif extrême
• Miction fréquente
• Perte de poids inexpliquée
• Fatigue extrême
• Vision floue

Type 2 (apparition progressive):
• Soif/faim accrues
• Miction fréquente
• Cicatrisation lente
• Fourmillements mains/pieds
• Infections récurrentes

⚠️ Consultez un médecin si vous avez ces symptômes!
                    """.trimIndent()
                    "ar" -> """
🔍 أعراض مرض السكري الشائعة

النوع الأول (ظهور مفاجئ):
• عطش شديد
• كثرة التبول
• فقدان وزن غير مبرر
• تعب شديد
• زغللة العين

النوع الثاني (ظهور تدريجي):
• عطش/جوع متزايد
• كثرة التبول
• بطء التئام الجروح
• وخز في اليدين/القدمين
• التهابات متكررة

⚠️ استشر طبيبك إذا كانت لديك هذه الأعراض!
                    """.trimIndent()
                    else -> """
🔍 COMMON DIABETES SYMPTOMS

Type 1 (sudden onset):
• Extreme thirst
• Frequent urination
• Unexplained weight loss
• Extreme fatigue
• Blurry vision

Type 2 (gradual onset):
• Increased thirst/hunger
• Frequent urination
• Slow-healing wounds
• Tingling hands/feet
• Recurring infections

⚠️ See a doctor if you have these symptoms!
                    """.trimIndent()
                }
            }

            // A1C
            lower.contains("a1c") || lower.contains("hba1c") -> {
                when (lang) {
                    "fr" -> """
🧪 HbA1c (Hémoglobine glyquée)

📊 Moyenne de glycémie sur 2-3 mois

🎯 OBJECTIFS:
• <7.0% → Bon contrôle
• 7.0-8.0% → Acceptable
• >8.0% → Trop élevé

📈 A1c vers Glycémie moyenne:
• 6% → 126 mg/dL
• 7% → 154 mg/dL
• 8% → 183 mg/dL
• 9% → 212 mg/dL

📅 Testez tous les 3 mois
💡 Baisser A1c de 1% réduit les risques de 40%!
                    """.trimIndent()
                    "ar" -> """
🧪 تحليل HbA1c (الهيموغلوبين السكري)

📊 متوسط السكر خلال 2-3 أشهر الماضية

🎯 الأهداف:
• <7.0% → تحكم جيد
• 7.0-8.0% → مقبول
• >8.0% → مرتفع جداً

📈 A1c إلى متوسط السكر:
• 6% → 126 ملجم/دل
• 7% → 154 ملجم/دل
• 8% → 183 ملجم/دل
• 9% → 212 ملجم/دل

📅 اختبر كل 3 أشهر
💡 خفض A1c بنسبة 1% يقلل المضاعفات بنسبة 40%!
                    """.trimIndent()
                    else -> """
🧪 HbA1c (Glycated Hemoglobin)

📊 Average blood glucose over 2-3 months

🎯 TARGETS:
• <7.0% → Good control
• 7.0-8.0% → Acceptable
• >8.0% → Too high

📈 A1c to Estimated Average Glucose:
• 6% → 126 mg/dL
• 7% → 154 mg/dL
• 8% → 183 mg/dL
• 9% → 212 mg/dL

📅 Test every 3 months
💡 Lowering A1c by 1% reduces risk by 40%!
                    """.trimIndent()
                }
            }

            // FOOT CARE
            lower.contains("foot") || lower.contains("pied") || lower.contains("قدم") -> {
                when (lang) {
                    "fr" -> """
🦶 SOINS DES PIEDS DIABÉTIQUES - CRITIQUE!

✅ ROUTINE QUOTIDIENNE:
• Inspectez les pieds CHAQUE JOUR
• Vérifiez entre les orteils
• Lavez à l'eau tiède
• Séchez soigneusement
• Hydratez (pas entre les orteils)

✅ CHAUSSURES:
• Portez des chaussures adaptées
• Ne marchez jamais pieds nus
• Vérifiez l'intérieur des chaussures

🚨 CONSULTEZ IMMÉDIATEMENT SI:
• Coupure, ampoule
• Rougeur, gonflement
• Plaie qui ne guérit pas
                    """.trimIndent()
                    "ar" -> """
🦶 العناية بالقدمين لمرضى السكري - مهم جداً!

✅ الروتين اليومي:
• افحص قدميك كل يوم
• تحقق بين الأصابع
• اغسل بماء دافئ
• جفف جيداً
• رطب (ليس بين الأصابع)

✅ الأحذية:
• ارتدِ أحذية مناسبة
• لا تمش حافي القدمين أبداً
• تحقق داخل الأحذية قبل ارتدائها

🚨 استشر الطبيب فوراً إذا:
• جرح أو بثرة
• احمرار أو تورم
• جرح لا يلتئم
                    """.trimIndent()
                    else -> """
🦶 DIABETIC FOOT CARE - CRITICAL!

✅ DAILY ROUTINE:
• Inspect feet EVERY DAY
• Check between toes
• Wash with warm water
• Dry thoroughly
• Moisturize (not between toes)

✅ FOOTWEAR:
• Wear well-fitting shoes
• Never walk barefoot
• Check inside shoes before wearing

🚨 SEEK HELP IMMEDIATELY IF:
• Cut, blister
• Redness, swelling
• Wound not healing
                    """.trimIndent()
                }
            }

            // STRESS
            lower.contains("stress") || lower.contains("stresse") || lower.contains("توتر") -> {
                when (lang) {
                    "fr" -> """
🧠 STRESS ET GLYCÉMIE

📈 Le stress augmente la glycémie

✅ GESTION DU STRESS:
• Respiration profonde (5 minutes)
• Méditation
• Exercice régulier
• Sommeil suffisant (7-9h)
• Parlez à quelqu'un

💡 Respiration 4-7-8:
• Inspirez 4 secondes
• Retenez 7 secondes
• Expirez 8 secondes
• Répétez 4 fois
                    """.trimIndent()
                    "ar" -> """
🧠 التوتر وسكر الدم

📈 التوتر يرفع سكر الدم

✅ إدارة التوتر:
• تنفس عميق (5 دقائق)
• تأمل
• تمارين منتظمة
• نوم كاف (7-9 ساعات)
• تحدث مع شخص ما

💡 تنفس 4-7-8:
• شهيق 4 ثوان
• احبس 7 ثوان
• زفير 8 ثوان
• كرر 4 مرات
                    """.trimIndent()
                    else -> """
🧠 STRESS AND BLOOD SUGAR

📈 Stress raises blood sugar

✅ STRESS MANAGEMENT:
• Deep breathing (5 minutes)
• Meditation
• Regular exercise
• Adequate sleep (7-9h)
• Talk to someone

💡 4-7-8 breathing:
• Inhale 4 seconds
• Hold 7 seconds
• Exhale 8 seconds
• Repeat 4 times
                    """.trimIndent()
                }
            }

            // RAMADAN
            lower.contains("ramadan") || lower.contains("fast") || lower.contains("jeûne") || lower.contains("صوم") -> {
                when (lang) {
                    "fr" -> """
🌙 DIABÈTE ET JEÛNE DU RAMADAN

⚠️ Consultez votre médecin AVANT de jeûner

✅ CONSEILS DE SÉCURITÉ:
• Vérifiez glycémie plus souvent
• Rompez le jeûne si <70 ou >300
• Restez hydraté entre Iftar et Suhoor

🍽️ À L'IFTAR:
• Commencez par eau et dattes (1-2)
• Évitez les grandes quantités de glucides

🍽️ AU SUHOOR:
• Mangez des glucides complexes
• Incluez protéines et bonnes graisses

⚠️ GROUPES À RISQUE (NE devraient PAS jeûner):
• Hypoglycémies fréquentes sévères
• Diabète de type 1 mal contrôlé
• Grossesse
                    """.trimIndent()
                    "ar" -> """
🌙 السكري وصيام رمضان

⚠️ استشر طبيبك قبل الصيام

✅ نصائح السلامة:
• افحص السكر بشكل متكرر
• أفطر فوراً إذا كان السكر <70 أو >300
• حافظ على ترطيب جسمك بين الإفطار والسحور

🍽️ عند الإفطار:
• ابدأ بالماء والتمر (1-2)
• تجنب كميات كبيرة من الكربوهيدرات

🍽️ عند السحور:
• تناول الكربوهيدرات المعقدة
• شامل البروتين والدهون الصحية

⚠️ الفئات عالية الخطورة (لا ينبغي لهم الصوم):
• نوبات انخفاض السكر المتكررة الشديدة
• النوع الأول غير المسيطر عليه
• الحمل
                    """.trimIndent()
                    else -> """
🌙 DIABETES AND RAMADAN FASTING

⚠️ Consult your doctor BEFORE fasting

✅ SAFETY TIPS:
• Check glucose more frequently
• Break fast immediately if <70 or >300
• Stay hydrated between Iftar and Suhoor

🍽️ AT IFTAR:
• Start with water and dates (1-2)
• Avoid large amounts of fast carbs

🍽️ AT SUHOOR:
• Eat complex carbs
• Include protein and healthy fats

⚠️ HIGH-RISK GROUPS (should NOT fast):
• Frequent severe hypos
• Uncontrolled Type 1
• Pregnancy
                    """.trimIndent()
                }
            }

            // MEDICATION REMINDER
            lower.contains("medication") || lower.contains("pill") || lower.contains("reminder") ||
                    lower.contains("médicament") || lower.contains("دواء") -> {
                when (lang) {
                    "fr" -> """
💊 CONSEILS POUR LES RAPPELS DE MÉDICAMENTS

✅ MEILLEURES PRATIQUES:
• Prenez à la même heure chaque jour
• Utilisez un pilulier
• Réglez des alarmes
• Liez à une routine quotidienne

📅 RECHARGES:
• Marquez les dates sur le calendrier
• Rappel 1 semaine avant
• Ayez toujours 3 jours d'avance

⚠️ Ne sautez JAMAIS les médicaments
⚠️ Ne doublez pas la dose

🔔 Utilisez le calendrier de l'application!
                    """.trimIndent()
                    "ar" -> """
💊 نصائح لتذكيرات الأدوية

✅ أفضل الممارسات:
• تناول في نفس الوقت يومياً
• استخدم منظم الحبوب
• اضبط منبهات
• اربط بروتين يومي

📅 إعادة التعبئة:
• حدد التواريخ في التقويم
• تذكير قبل أسبوع
• احتفظ دائماً بمخزون 3 أيام

⚠️ لا تتخطى الأدوية أبداً
⚠️ لا تضاعف الجرعة

🔔 استخدم التقويم في التطبيق!
                    """.trimIndent()
                    else -> """
💊 MEDICATION REMINDER TIPS

✅ BEST PRACTICES:
• Take at same time daily
• Use pill organizer
• Set phone alarms
• Link to daily routine

📅 REFILLS:
• Mark dates on calendar
• Reminder 1 week before
• Always have 3-day supply

⚠️ NEVER skip medications
⚠️ Don't double dose

🔔 Use Calendar feature in the app!
                    """.trimIndent()
                }
            }

            // GOODBYE
            lower.contains("bye") || lower.contains("thank") || lower.contains("merci") || lower.contains("شكرا") -> {
                when (lang) {
                    "fr" -> """
De rien! 😊

N'oubliez pas:
• Vérifiez votre glycémie régulièrement
• Prenez vos médicaments à l'heure
• Restez actif et hydraté
• Enregistrez vos données dans l'application

Revenez quand vous avez des questions! 💙
                    """.trimIndent()
                    "ar" -> """
على الرحب والسعة! 😊

تذكر:
• افحص السكر بانتظام
• تناول الأدوية في موعدها
• حافظ على نشاطك ورطوبة جسمك
• سجل بياناتك في التطبيق

عد في أي وقت لديك أسئلة! 💙
                    """.trimIndent()
                    else -> """
You're welcome! 😊

Remember:
• Check glucose regularly
• Take medications on time
• Stay active and hydrated
• Log your data in the app

Come back anytime if you have questions! 💙
                    """.trimIndent()
                }
            }

            // DEFAULT
            else -> {
                when (lang) {
                    "fr" -> """
Je peux vous aider avec:

🆘 URGENCES:
• Hypoglycémie (taux bas)
• Hyperglycémie (taux élevé)
• Symptômes ACD

💊 GESTION:
• Types et dosage d'insuline
• Médicaments et rappels
• Explication HbA1c

🥗 MODE DE VIE:
• Alimentation
• Exercice
• Gestion du stress

🦶 COMPLICATIONS:
• Soins des pieds
• Symptômes

🕌 SPÉCIAL:
• Jeûne du Ramadan

✨ Essayez:
• "Que faire pour un taux bas?"
• "Comment calculer l'insuline?"
• "Quels aliments manger?"
• "L'exercice est-il sûr?"
• "Analyse ma glycémie"
                    """.trimIndent()
                    "ar" -> """
يمكنني مساعدتك في:

🆘 حالات الطوارئ:
• انخفاض السكر
• ارتفاع السكر
• أعراض الحماض الكيتوني

💊 الإدارة:
• أنواع الأنسولين والجرعات
• الأدوية والتذكيرات
• شرح تحليل A1c

🥗 نمط الحياة:
• التغذية
• التمارين
• إدارة التوتر

🦶 المضاعفات:
• العناية بالقدمين
• الأعراض

🕌 خاص:
• صيام رمضان

✨ جرب أن تسأل:
• "ماذا أفعل عند انخفاض السكر؟"
• "كيف أحسب الأنسولين؟"
• "ما هي الأطعمة التي أتناولها؟"
• "هل التمارين آمنة؟"
• "حلل السكر لدي"
                    """.trimIndent()
                    else -> """
I can help with:

🆘 EMERGENCIES:
• Low blood sugar (hypoglycemia)
• High blood sugar (hyperglycemia)
• DKA symptoms

💊 MANAGEMENT:
• Insulin types & dosing
• Medications & reminders
• A1c explanation

🥗 LIFESTYLE:
• Diet & meal planning
• Exercise safety
• Stress management

🦶 COMPLICATIONS:
• Foot care
• Symptoms

🕌 SPECIAL:
• Ramadan fasting

✨ Try asking:
• "What do I do for low sugar?"
• "How to calculate insulin?"
• "What foods can I eat?"
• "Is exercise safe?"
• "Check my glucose"
                    """.trimIndent()
                }
            }
        }
    }

    private fun analyzeGlucoseValue(glucose: Int): String {
        val lang = currentLanguage
        val message = when {
            glucose < 54 -> when (lang) {
                "fr" -> """
🔴 TAUX DE SUCRE CRITIQUEMENT BAS: $glucose mg/dL

🚨 URGENCE MÉDICALE!
• Si conscient: Mangez 15g glucides rapides
• Si inconscient: Appelez les secours
• Utilisez du Glucagon

N'ATTENDEZ PAS!
                """.trimIndent()
                "ar" -> """
🔴 انخفاض حاد وخطير في السكر: $glucose ملجم/دل

🚨 حالة طبية طارئة!
• إذا كان واعياً: تناول 15 جرام كربوهيدرات سريعة
• إذا كان فاقداً للوعي: اتصل بالإسعاف
• استخدم الجلوكاجون

لا تنتظر!
                """.trimIndent()
                else -> """
🔴 CRITICAL LOW BLOOD SUGAR: $glucose mg/dL

🚨 MEDICAL EMERGENCY!
• If conscious: Eat 15g fast carbs
• If unconscious: Call emergency
• Use Glucagon

DO NOT WAIT!
                """.trimIndent()
            }
            glucose < 70 -> when (lang) {
                "fr" -> """
⚠️ TAUX DE SUCRE BAS: $glucose mg/dL

Règle de 15:
1. Mangez 15g glucides rapides
2. Attendez 15 minutes
3. Revérifiez
4. Si encore bas, répétez
                """.trimIndent()
                "ar" -> """
⚠️ انخفاض السكر: $glucose ملجم/دل

قاعدة 15:
1. تناول 15 جرام كربوهيدرات سريعة
2. انتظر 15 دقيقة
3. أعد الفحص
4. إذا لا يزال منخفضاً، كرر
                """.trimIndent()
                else -> """
⚠️ LOW BLOOD SUGAR: $glucose mg/dL

Rule of 15:
1. Eat 15g fast carbs
2. Wait 15 minutes
3. Recheck
4. If still low, repeat
                """.trimIndent()
            }
            glucose in 70..140 -> when (lang) {
                "fr" -> "✅ GLYCÉMIE NORMALE: $glucose mg/dL\n\nExcellent contrôle! Continuez ainsi."
                "ar" -> "✅ سكر طبيعي: $glucose ملجم/دل\n\nتحكم ممتاز! استمر على هذا المنوال."
                else -> "✅ NORMAL BLOOD SUGAR: $glucose mg/dL\n\nGreat control! Keep it up."
            }
            glucose in 141..180 -> when (lang) {
                "fr" -> "⚠️ GLYCÉMIE ÉLEVÉE: $glucose mg/dL\n\nLégèrement au-dessus. Buvez de l'eau et marchez 15-20 min."
                "ar" -> "⚠️ سكر مرتفع: $glucose ملجم/دل\n\nمرتفع قليلاً. اشرب ماء وامش 15-20 دقيقة."
                else -> "⚠️ ELEVATED BLOOD SUGAR: $glucose mg/dL\n\nSlightly above. Drink water and walk 15-20 min."
            }
            glucose in 181..250 -> when (lang) {
                "fr" -> "⚠️ GLYCÉMIE HAUTE: $glucose mg/dL\n\nBuvez de l'eau, vérifiez les cétones si >250, prenez insuline de correction."
                "ar" -> "⚠️ سكر مرتفع: $glucose ملجم/دل\n\nاشرب ماء، افحص الكيتونات إذا كان >250، خذ الأنسولين التصحيحي."
                else -> "⚠️ HIGH BLOOD SUGAR: $glucose mg/dL\n\nDrink water, check ketones if >250, take correction insulin."
            }
            else -> when (lang) {
                "fr" -> """
🔴 GLYCÉMIE TRÈS HAUTE: $glucose mg/dL

🚨 ACTION URGENTE:
1. Vérifiez les cétones
2. Buvez de l'eau
3. Prenez insuline de correction
4. Consultez un médecin si cétones présentes
                """.trimIndent()
                "ar" -> """
🔴 سكر مرتفع جداً: $glucose ملجم/دل

🚨 إجراء عاجل:
1. افحص الكيتونات
2. اشرب ماء
3. خذ الأنسولين التصحيحي
4. استشر الطبيب إذا كانت الكيتونات موجودة
                """.trimIndent()
                else -> """
🔴 VERY HIGH BLOOD SUGAR: $glucose mg/dL

🚨 URGENT ACTION:
1. Check for ketones
2. Drink water
3. Take correction insulin
4. See doctor if ketones present
                """.trimIndent()
            }
        }

        val latestReading = app.databaseHelper.getLatestGlucoseReading(app.sessionManager.getUserId())
        return if (latestReading != null && latestReading.value != glucose) {
            val trendUp = when (lang) { "fr" -> "↑ En hausse"; "ar" -> "↑ في ارتفاع"; else -> "↑ Rising" }
            val trendDown = when (lang) { "fr" -> "↓ En baisse"; "ar" -> "↓ في انخفاض"; else -> "↓ Falling" }
            val trendText = if (glucose > latestReading.value) trendUp else trendDown
            val previousText = when (lang) { "fr" -> "Lecture précédente"; "ar" -> "القراءة السابقة"; else -> "Previous reading" }
            "$message\n\n📊 $previousText: ${latestReading.value} mg/dL. $trendText"
        } else {
            message
        }
    }

    private fun addMessage(text: String, isUser: Boolean) {
        messages.add(ChatMessage(text, isUser))
        adapter.notifyItemInserted(messages.size - 1)
        rvChat.scrollToPosition(messages.size - 1)
    }

    data class ChatMessage(val text: String, val isUser: Boolean)

    inner class ChatAdapter(private val messages: List<ChatMessage>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun getItemViewType(position: Int) = if (messages[position].isUser) 0 else 1
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return if (viewType == 0) {
                UserViewHolder(inflater.inflate(R.layout.item_chat_user, parent, false))
            } else {
                BotViewHolder(inflater.inflate(R.layout.item_chat_bot, parent, false))
            }
        }
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val msg = messages[position]
            when (holder) {
                is UserViewHolder -> holder.bind(msg.text)
                is BotViewHolder -> holder.bind(msg.text)
            }
        }
        override fun getItemCount() = messages.size
    }

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        fun bind(text: String) { tvMessage.text = text }
    }

    class BotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        fun bind(text: String) { tvMessage.text = text }
    }
}