package com.example.glucare.activities

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.ImageView
import android.widget.TextView
import com.example.glucare.R

class TermsOfServiceActivity : BaseActivity() {

    private lateinit var tvTermsContent: TextView
    private lateinit var btnBack: ImageView
    private lateinit var tvTitle: TextView

    private val currentLanguage: String
        get() = getSharedPreferences("settings", MODE_PRIVATE)
            .getString("app_language", "en") ?: "en"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms_of_service)

        tvTermsContent = findViewById(R.id.tvTermsContent)
        btnBack = findViewById(R.id.btnBack)
        tvTitle = findViewById(R.id.tvTitle)

        tvTitle.text = when (currentLanguage) {
            "fr" -> "Conditions d'Utilisation"
            "ar" -> "شروط الخدمة"
            else -> "Terms of Service"
        }

        btnBack.setOnClickListener { finish() }

        tvTermsContent.movementMethod = ScrollingMovementMethod()
        tvTermsContent.text = getTermsText()
    }

    private fun getTermsText(): String {
        return when (currentLanguage) {
            "fr" -> """
                CONDITIONS D'UTILISATION DE GLUCARE
                ===================================
                
                1. ACCEPTATION DES CONDITIONS
                -------------------------------
                En utilisant l'application Glucare, vous acceptez d'être lié par ces conditions.
                
                2. AVERTISSEMENT MÉDICAL
                -------------------------------
                Glucare n'est PAS un dispositif médical et ne fournit PAS de conseils médicaux.
                
                • Toutes les informations sont fournies à titre éducatif uniquement.
                • Le calculateur d'insuline fournit des estimations basées sur des formules standards.
                • Consultez toujours votre professionnel de santé avant de modifier votre traitement.
                • En cas d'urgence médicale, appelez immédiatement les secours.
                
                3. RESPONSABILITÉS DE L'UTILISATEUR
                -------------------------------
                Vous acceptez de:
                • Fournir des informations précises
                • Consulter un médecin pour les décisions médicales
                • Ne pas partager votre compte
                
                4. CONFIDENTIALITÉ DES DONNÉES
                -------------------------------
                Vos données de santé sont privées et sécurisées.
                Nous ne partageons pas vos informations personnelles sans consentement.
                
                5. LIMITATION DE RESPONSABILITÉ
                -------------------------------
                Glucare ne peut être tenu responsable des dommages résultant de l'utilisation de l'application.
                
                6. CONSENTEMENT
                -------------------------------
                En utilisant Glucare, vous consentez à ces conditions.
                
                © 2026 Glucare. Tous droits réservés.
            """.trimIndent()

            "ar" -> """
                شروط استخدام جلوكير
                ===================
                
                1. قبول الشروط
                --------------
                باستخدامك تطبيق جلوكير، فإنك توافق على الالتزام بهذه الشروط.
                
                2. إخلاء المسؤولية الطبية
                ------------------------
                جلوكير ليس جهازاً طبياً ولا يقدم نصائح طبية.
                
                • جميع المعلومات مقدمة للأغراض التعليمية فقط.
                • حاسبة الأنسولين توفر تقديرات بناءً على صيغ قياسية.
                • استشر مقدم الرعاية الصحية دائماً قبل تغيير علاجك.
                • في حالة الطوارئ الطبية، اتصل بالإسعاف فوراً.
                
                3. مسؤوليات المستخدم
                -------------------
                أنت توافق على:
                • تقديم معلومات دقيقة
                • استشارة الطبيب للقرارات الطبية
                • عدم مشاركة حسابك
                
                4. خصوصية البيانات
                ----------------
                بياناتك الصحية خاصة وآمنة.
                نحن لا نشارك معلوماتك الشخصية بدون موافقتك.
                
                5. الحد من المسؤولية
                -------------------
                لا يمكن تحميل جلوكير المسؤولية عن الأضرار الناتجة عن استخدام التطبيق.
                
                6. الموافقة
                ----------
                باستخدامك جلوكير، فإنك توافق على هذه الشروط.
                
                © 2026 جلوكير. جميع الحقوق محفوظة.
            """.trimIndent()

            else -> """
                GLUCARE TERMS OF SERVICE
                ========================
                
                1. ACCEPTANCE OF TERMS
                -----------------------
                By using Glucare, you agree to be bound by these terms.
                
                2. MEDICAL DISCLAIMER
                ---------------------
                Glucare is NOT a medical device and does NOT provide medical advice.
                
                • All information is for educational purposes only.
                • The insulin calculator provides estimates based on standard formulas.
                • Always consult your healthcare provider before changing your treatment.
                • In case of medical emergency, call emergency services immediately.
                
                3. USER RESPONSIBILITIES
                ------------------------
                You agree to:
                • Provide accurate information
                • Consult a doctor for medical decisions
                • Not share your account
                
                4. DATA PRIVACY
                ---------------
                Your health data is private and secure.
                We do not share your personal information without consent.
                
                5. LIMITATION OF LIABILITY
                --------------------------
                Glucare shall not be liable for damages resulting from app use.
                
                6. CONSENT
                ----------
                By using Glucare, you consent to these terms.
                
                © 2026 Glucare. All rights reserved.
            """.trimIndent()
        }
    }
}