// data/TipsDatabase.kt
package com.example.glucare.data

import android.content.Context

object TipsDatabase {

    /**
     * Get tips in the current app language
     */
    fun getTips(context: Context): List<Tip> {
        val language = getCurrentLanguage(context)
        return when (language) {
            "fr" -> getFrenchTips()
            "ar" ->getArabicTips()
            else -> getEnglishTips()

        }
    }

    /**
     * Get a random tip in the current language
     */
    fun getRandomTip(context: Context): Tip = getTips(context).random()

    private fun getCurrentLanguage(context: Context): String {
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        return prefs.getString("app_language", "en") ?: "en"
    }

    // ========== ENGLISH TIPS ==========
    private fun getEnglishTips(): List<Tip> = listOf(
        // NUTRITION TIPS
        Tip("🍎", "Apple a Day", "An apple with peanut butter is the perfect diabetes snack: fiber from apple + protein from peanut butter = stable blood sugar!", "Nutrition"),
        Tip("🥑", "Healthy Fats", "Avocado, nuts, and olive oil don't spike blood sugar and keep you full longer. Add them to salads or eat as snacks!", "Nutrition"),
        Tip("🥗", "Plate Method", "Fill half your plate with non-starchy veggies, quarter with lean protein, quarter with complex carbs. Simple and effective!", "Nutrition"),
        Tip("🌿", "Leafy Greens", "Spinach, kale, and lettuce are packed with vitamins and have minimal impact on blood sugar. Eat them freely!", "Nutrition"),
        Tip("🥦", "Cruciferous Veggies", "Broccoli, cauliflower, and cabbage are rich in fiber and antioxidants. Great for diabetes management!", "Nutrition"),
        Tip("🍠", "Sweet Potatoes", "Sweet potatoes have a lower glycemic index than regular potatoes. Eat with skin for extra fiber!", "Nutrition"),
        Tip("🥜", "Nuts for Snacks", "A handful of almonds or walnuts makes a great snack - protein, healthy fats, and minimal carbs!", "Nutrition"),
        Tip("🫘", "Bean Power", "Beans and lentils are high in fiber and protein, slow to digest, and won't spike blood sugar!", "Nutrition"),
        Tip("🐟", "Omega-3s", "Salmon, sardines, and mackerel are rich in omega-3s which reduce inflammation and improve heart health!", "Nutrition"),
        Tip("🍳", "Egg-cellent", "Eggs are protein-rich and have zero carbs. Perfect for breakfast to keep you full until lunch!", "Nutrition"),
        Tip("🥛", "Greek Yogurt", "Greek yogurt has twice the protein of regular yogurt. Choose plain to avoid added sugars!", "Nutrition"),
        Tip("🍓", "Berry Good", "Berries are lower in sugar than other fruits and packed with antioxidants. Blueberries, strawberries, raspberries!", "Nutrition"),
        Tip("🍋", "Citrus Power", "Oranges, lemons, and grapefruit provide vitamin C without spiking blood sugar too much. Eat whole, not juice!", "Nutrition"),
        Tip("🍚", "Brown Rice", "Switch from white rice to brown rice for more fiber and slower glucose release!", "Nutrition"),
        Tip("🌾", "Whole Grains", "Oats, quinoa, and barley are excellent choices. They release energy slowly and keep you full!", "Nutrition"),
        Tip("🧂", "Watch the Salt", "Diabetes increases heart disease risk. Limit sodium to 2300mg per day - skip processed foods!", "Nutrition"),
        Tip("🧈", "Butter Alternatives", "Use olive oil or avocado oil instead of butter. Better for your heart and insulin sensitivity!", "Nutrition"),
        Tip("🥤", "Skip Sugary Drinks", "Soda, sweet tea, and fruit juice spike blood sugar quickly. Water is always best!", "Nutrition"),
        Tip("☕", "Coffee Facts", "Black coffee may lower diabetes risk. But skip the sugar and flavored syrups!", "Nutrition"),
        Tip("🍵", "Green Tea", "Green tea is rich in antioxidants and may improve insulin sensitivity. Drink it unsweetened!", "Nutrition"),

        // EXERCISE TIPS
        Tip("🚶", "After-Meal Walk", "A 15-minute walk after meals can lower blood sugar by up to 30%! Make it a habit.", "Exercise"),
        Tip("🏋️", "Strength Training", "Building muscle helps your body use insulin more effectively. Try weights or resistance bands 2-3x/week.", "Exercise"),
        Tip("🧘", "Yoga Benefits", "Yoga reduces stress hormones that raise blood sugar. Plus it improves flexibility and balance!", "Exercise"),
        Tip("🚴", "Cycle for Health", "Cycling is low-impact and great for cardiovascular health. Start with 15-20 minutes daily.", "Exercise"),
        Tip("🏊", "Swim Safely", "Swimming is easy on joints and a full-body workout. Always check glucose before and after!", "Exercise"),
        Tip("⏰", "Timing Matters", "Morning exercise may lower glucose all day. Find what works best for your schedule!", "Exercise"),
        Tip("🎯", "Start Small", "Even 10 minutes of activity helps. Start small and build up to 150 minutes per week.", "Exercise"),
        Tip("🦵", "Leg Day", "Leg exercises use large muscles and burn more glucose. Squats and lunges are great!", "Exercise"),
        Tip("💪", "Resistance Bands", "Easy to use at home, great for strength training, and gentle on joints. Perfect for beginners!", "Exercise"),
        Tip("👟", "Walk More", "Take the stairs, park farther away, walk while on phone. Every step counts!", "Exercise"),
        Tip("🎵", "Music Motivation", "Create an exercise playlist. Music makes workouts feel easier and more enjoyable!", "Exercise"),
        Tip("👥", "Buddy System", "Exercise with a friend - you're more likely to stick with it and have fun!", "Exercise"),
        Tip("📱", "Track Your Steps", "Use a step counter. Aim to increase by 500 steps each week until you reach 8000-10000 daily.", "Exercise"),
        Tip("🌅", "Morning Stretch", "Start your day with 5 minutes of stretching. Wakes up your body and improves flexibility!", "Exercise"),
        Tip("🪑", "Desk Exercises", "Sitting too long? Do seated leg lifts, shoulder rolls, and stand up every hour.", "Exercise"),

        // INSULIN & MEDICATION TIPS
        Tip("💉", "Rotate Sites", "Rotate insulin injection sites to prevent lipodystrophy (lumps under skin). Abdomen, thighs, upper arms.", "Insulin"),
        Tip("🌡️", "Insulin Storage", "Don't let insulin freeze or get hot. Store in fridge, but let it warm to room temp before injecting.", "Insulin"),
        Tip("⏱️", "Timing Matters", "Take rapid insulin 15-20 minutes before meals for best results. It peaks when your food digests!", "Insulin"),
        Tip("📝", "Track Your Doses", "Logging insulin helps identify patterns. Use our calculator to learn from your history!", "Insulin"),
        Tip("🧴", "Check Expiration", "Always check insulin expiration dates. Expired insulin may not work properly.", "Insulin"),
        Tip("🗑️", "Dispose Safely", "Use a sharps container for needles. Never throw loose needles in the trash!", "Insulin"),
        Tip("✈️", "Travel with Insulin", "Keep insulin in carry-on luggage. Airport cargo holds get too cold!", "Insulin"),
        Tip("📦", "Don't Shake", "Roll insulin vials gently to mix. Shaking can create bubbles and damage the insulin.", "Insulin"),
        Tip("🩹", "Injection Spots", "Avoid injecting into scars, moles, or areas with lumps. Clean skin with alcohol first!", "Insulin"),
        Tip("🧊", "Frio Packs", "Use Frio packs when traveling in hot climates. They keep insulin cool without refrigeration.", "Insulin"),
        Tip("📅", "Track Refills", "Mark your calendar for prescription refills. Don't run out of insulin!", "Insulin"),
        Tip("💊", "Medication Routine", "Take diabetes medications at the same time daily. Use a pill organizer if needed.", "Insulin"),
        Tip("🔄", "Switching Insulins", "Never switch insulin types without doctor approval. They work differently!", "Insulin"),
        Tip("📞", "Emergency Supply", "Always have a 3-day emergency supply of insulin and supplies. Natural disasters happen!", "Insulin"),

        // GLUCOSE MONITORING TIPS
        Tip("📊", "Know Your Numbers", "Target: Before meals 80-130 mg/dL, after meals <180 mg/dL, A1C <7%. Know your goals!", "Monitoring"),
        Tip("📓", "Log Patterns", "Track not just numbers but what you ate, activity, stress. Patterns help you manage better!", "Monitoring"),
        Tip("👆", "Wash Hands", "Always wash hands with soap and water before testing. Food residue can give false highs!", "Monitoring"),
        Tip("🩸", "Alternative Sites", "Some meters allow arm or palm testing. Good for when fingers are sore.", "Monitoring"),
        Tip("📉", "Trends Matter", "One high reading isn't a disaster. Look for patterns over time, not single numbers.", "Monitoring"),
        Tip("⏰", "Consistent Timing", "Test at the same times daily for consistent data. Before meals and bedtime are key.", "Monitoring"),
        Tip("🔄", "Rotate Fingers", "Use different fingers and different spots on each finger. Give sore spots a break!", "Monitoring"),
        Tip("📱", "Use Our App", "Log glucose in our app to see trends and get insights from your data!", "Monitoring"),
        Tip("📈", "Learn Your Patterns", "Notice morning highs? Could be dawn phenomenon. Afternoon lows? Maybe exercise effect.", "Monitoring"),
        Tip("🧪", "Check Ketones", "Test for ketones when glucose is over 250 mg/dL, especially if sick. Prevents DKA!", "Monitoring"),
        Tip("🔋", "Meter Battery", "Keep spare batteries for your glucose meter. You don't want it dying when needed!", "Monitoring"),
        Tip("📅", "Control Solution", "Use control solution monthly to ensure your meter is accurate.", "Monitoring"),

        // LIFESTYLE & WELLNESS TIPS
        Tip("😴", "Sleep Matters", "Poor sleep increases insulin resistance. Aim for 7-8 hours nightly. Consistent bedtime helps!", "Wellness"),
        Tip("🧘", "Stress Less", "Stress hormones raise blood sugar. Try deep breathing, meditation, or a hobby you enjoy.", "Wellness"),
        Tip("💧", "Stay Hydrated", "Water helps kidneys flush out excess sugar. Aim for 8 glasses daily!", "Wellness"),
        Tip("🚭", "Quit Smoking", "Smoking increases diabetes complications risk. Your doctor can help with quitting plans.", "Wellness"),
        Tip("🍷", "Alcohol Wisely", "If you drink, do so with food. Alcohol can cause delayed lows hours later!", "Wellness"),
        Tip("🦷", "Dental Health", "Diabetes increases gum disease risk. Brush twice daily, floss, see dentist regularly.", "Wellness"),
        Tip("👣", "Foot Care", "Check feet daily for cuts, blisters, redness. Diabetes can reduce sensation - you might not feel injuries!", "Wellness"),
        Tip("👀", "Eye Exams", "Get annual dilated eye exams. Diabetes is a leading cause of vision loss, but early treatment helps!", "Wellness"),
        Tip("💅", "Nail Care", "Cut nails straight across and file edges. Avoid cutting cuticles to prevent infections.", "Wellness"),
        Tip("🧦", "Proper Socks", "Wear clean, dry socks without tight elastic. Consider diabetic socks with extra cushioning.", "Wellness"),
        Tip("👞", "Proper Shoes", "Wear well-fitting shoes. Break in new shoes gradually. Check inside for objects before putting on.", "Wellness"),
        Tip("🌞", "Vitamin D", "Get your vitamin D levels checked. Low vitamin D is linked to insulin resistance.", "Wellness"),
        Tip("🤝", "Support System", "Join a diabetes support group. Sharing experiences helps mental health and learning.", "Wellness"),
        Tip("🎉", "Celebrate Wins", "Celebrate small victories - a good reading, a new recipe, 1000 extra steps. You're doing great!", "Wellness"),

        // SAFETY & EMERGENCY TIPS
        Tip("⚡", "15-15 Rule", "Low sugar? Eat 15g carbs (4 oz juice, 3-4 glucose tablets), wait 15 minutes, recheck. Repeat if needed.", "Safety"),
        Tip("🆔", "Medical ID", "Wear a medical ID bracelet. In an emergency, it tells responders you have diabetes.", "Safety"),
        Tip("🍬", "Always Carry Snacks", "Always carry fast-acting carbs like glucose tablets, candy, or juice boxes. Be prepared!", "Safety"),
        Tip("🚗", "Safe Driving", "Check glucose before driving. Never drive if low or if you feel symptoms. Keep snacks in car!", "Safety"),
        Tip("🏥", "Know Emergency Signs", "Severe hypoglycemia: confusion, unable to eat, unconscious. Use glucagon and call 911.", "Safety"),
        Tip("🔥", "DKA Warning", "Very high glucose + nausea/vomiting + fruity breath = DKA. Go to ER immediately!", "Safety"),
        Tip("🤒", "Sick Day Rules", "When sick, check glucose every 4 hours. Stay hydrated. Have a sick-day meal plan ready!", "Safety"),
        Tip("📞", "Emergency Contacts", "Keep doctor's number and emergency contacts in your phone. ICE (In Case of Emergency) is standard.", "Safety"),
        Tip("🧾", "Medical Info Card", "Carry a card with your conditions, medications, allergies, and doctor contacts.", "Safety"),
        Tip("🌡️", "Fever Warning", "Fever can spike blood sugar. Check more often and stay in touch with your doctor.", "Safety"),
        Tip("💊", "Medication Interactions", "Some medications (steroids, decongestants) can raise blood sugar. Ask pharmacist before taking.", "Safety"),
        Tip("✈️", "Travel Prep", "When traveling, pack twice the supplies you need. Keep some in carry-on, some in checked bag.", "Safety"),

        // SPECIAL SITUATIONS TIPS
        Tip("🕌", "Ramadan Fasting", "If fasting during Ramadan, work with your doctor to adjust meds. Check glucose more often.", "Special"),
        Tip("🤰", "Pregnancy", "Pregnancy changes insulin needs. Work closely with your healthcare team for healthy baby!", "Special"),
        Tip("👴", "Aging with Diabetes", "As you age, fall prevention becomes crucial. Keep pathways clear, use nightlights.", "Special"),
        Tip("🧒", "Kids with Diabetes", "Involve the whole family in care. Make it normal, not scary. Celebrate their bravery!", "Special"),
        Tip("🏫", "School Planning", "Work with school on a diabetes care plan. Ensure staff knows what to do in emergencies.", "Special"),
        Tip("💼", "Workplace Rights", "You have rights to breaks for testing and eating at work. Know your workplace accommodations!", "Special"),
        Tip("💑", "Dating with Diabetes", "Be open about your diabetes. Good partners will support you. You deserve understanding!", "Special"),
        Tip("🏕️", "Camping Tips", "Bring extra supplies, keep insulin cool, and tell campmates about your diabetes.", "Special"),
        Tip("🎢", "Holiday Eating", "Holidays = temptation. Use smaller plate, fill with veggies first, enjoy treats in moderation.", "Special"),

        // MOTIVATIONAL TIPS
        Tip("🌟", "You've Got This", "Managing diabetes is hard, but you're doing it! Every day you show up and try is a win.", "Motivation"),
        Tip("💪", "Small Steps", "You don't have to be perfect. Small consistent steps lead to big changes over time.", "Motivation"),
        Tip("🎯", "Set Goals", "Set small achievable goals. Walk 10 minutes daily. Add one veggie to dinner. Build from there.", "Motivation"),
        Tip("📈", "Progress Not Perfection", "Don't let one bad reading ruin your day. Look at the trend, not the single point.", "Motivation"),
        Tip("🤗", "Be Kind to Yourself", "Diabetes is hard. You're doing your best. Treat yourself with the kindness you deserve.", "Motivation"),
        Tip("🌈", "You're Not Alone", "Over 400 million people worldwide have diabetes. You're part of a huge community!", "Motivation"),
        Tip("🏆", "Celebrate", "Celebrate your wins - good A1C, new exercise routine, cooking a healthy meal. You earned it!", "Motivation"),
        Tip("🌱", "Keep Growing", "Every day you learn something new about your body and diabetes. That knowledge is power!", "Motivation")
    )

    // ========== FRENCH TIPS ==========
    // TODO: Add French translations here
    private fun getFrenchTips(): List<Tip> = listOf(
        // NUTRITION TIPS - FRENCH
        Tip("🍎", "Une pomme par jour", "Une pomme avec du beurre de cacahuète est la collation parfaite pour le diabète : les fibres de la pomme + les protéines du beurre de cacahuète = glycémie stable !", "Nutrition"),
        Tip("🥑", "Bonnes graisses", "L'avocat, les noix et l'huile d'olive ne font pas grimper la glycémie et vous rassasient plus longtemps. Ajoutez-les aux salades ou mangez-les en collation !", "Nutrition"),
        Tip("🥗", "Méthode de l'assiette", "Remplissez la moitié de votre assiette de légumes non féculents, un quart de protéines maigres, un quart de glucides complexes. Simple et efficace !", "Nutrition"),
        Tip("🌿", "Légumes verts", "Les épinards, le chou frisé et la laitue sont riches en vitamines et ont un impact minime sur la glycémie. Mangez-en librement !", "Nutrition"),
        Tip("🥦", "Légumes crucifères", "Le brocoli, le chou-fleur et le chou sont riches en fibres et en antioxydants. Excellents pour la gestion du diabète !", "Nutrition"),
        Tip("🍠", "Patates douces", "Les patates douces ont un index glycémique plus bas que les pommes de terre ordinaires. Mangez-les avec la peau pour plus de fibres !", "Nutrition"),
        Tip("🥜", "Noix pour collation", "Une poignée d'amandes ou de noix fait une excellente collation - protéines, bonnes graisses, et glucides minimes !", "Nutrition"),
        Tip("🫘", "Puissance des haricots", "Les haricots et les lentilles sont riches en fibres et en protéines, digèrent lentement, et ne font pas grimper la glycémie !", "Nutrition"),
        Tip("🐟", "Oméga-3", "Le saumon, les sardines et le maquereau sont riches en oméga-3 qui réduisent l'inflammation et améliorent la santé cardiaque !", "Nutrition"),
        Tip("🍳", "Œuf-cellent", "Les œufs sont riches en protéines et n'ont pas de glucides. Parfaits pour le petit-déjeuner pour tenir jusqu'au déjeuner !", "Nutrition"),
        Tip("🥛", "Yaourt grec", "Le yaourt grec a deux fois plus de protéines que le yaourt ordinaire. Choisissez nature pour éviter les sucres ajoutés !", "Nutrition"),
        Tip("🍓", "Les baies, c'est bon", "Les baies contiennent moins de sucre que les autres fruits et sont riches en antioxydants. Myrtilles, fraises, framboises !", "Nutrition"),
        Tip("🍋", "Puissance des agrumes", "Les oranges, les citrons et les pamplemousses fournissent de la vitamine C sans trop augmenter la glycémie. Mangez le fruit entier, pas le jus !", "Nutrition"),
        Tip("🍚", "Riz brun", "Remplacez le riz blanc par du riz brun pour plus de fibres et une libération plus lente du glucose !", "Nutrition"),
        Tip("🌾", "Céréales complètes", "L'avoine, le quinoa et l'orge sont d'excellents choix. Ils libèrent l'énergie lentement et vous rassasient !", "Nutrition"),
        Tip("🧂", "Surveillez le sel", "Le diabète augmente le risque de maladies cardiaques. Limitez le sodium à 2300 mg par jour - évitez les aliments transformés !", "Nutrition"),
        Tip("🧈", "Alternatives au beurre", "Utilisez de l'huile d'olive ou de l'huile d'avocat au lieu du beurre. Meilleur pour le cœur et la sensibilité à l'insuline !", "Nutrition"),
        Tip("🥤", "Évitez les boissons sucrées", "Les sodas, les thés sucrés et les jus de fruits font grimper la glycémie rapidement. L'eau est toujours la meilleure option !", "Nutrition"),
        Tip("☕", "Faits sur le café", "Le café noir peut réduire le risque de diabète. Mais évitez le sucre et les sirops aromatisés !", "Nutrition"),
        Tip("🍵", "Thé vert", "Le thé vert est riche en antioxydants et peut améliorer la sensibilité à l'insuline. Buvez-le sans sucre !", "Nutrition"),
        // EXERCISE TIPS - FRENCH
        Tip("🚶", "Promenade après le repas", "Une promenade de 15 minutes après les repas peut réduire la glycémie jusqu'à 30 % ! Prenez-en l'habitude.", "Exercice"),
        Tip("🏋️", "Musculation", "Développer ses muscles aide votre corps à utiliser l'insuline plus efficacement. Essayez des poids ou des élastiques 2-3 fois par semaine.", "Exercice"),
        Tip("🧘", "Bienfaits du yoga", "Le yoga réduit les hormones de stress qui augmentent la glycémie. Il améliore aussi la flexibilité et l'équilibre !", "Exercice"),
        Tip("🚴", "Vélo pour la santé", "Le vélo est doux pour les articulations et excellent pour la santé cardiovasculaire. Commencez par 15-20 minutes par jour.", "Exercice"),
        Tip("🏊", "Nagez en sécurité", "La natation est douce pour les articulations et fait travailler tout le corps. Vérifiez toujours votre glycémie avant et après !", "Exercice"),
        Tip("⏰", "Le timing compte", "L'exercice matinal peut réduire la glycémie toute la journée. Trouvez ce qui fonctionne le mieux pour votre emploi du temps !", "Exercice"),
        Tip("🎯", "Commencez petit", "Même 10 minutes d'activité aident. Commencez petit et augmentez jusqu'à 150 minutes par semaine.", "Exercice"),
        Tip("🦵", "Journée des jambes", "Les exercices pour les jambes utilisent de grands muscles et brûlent plus de glucose. Les squats et les fentes sont excellents !", "Exercice"),
        Tip("💪", "Élastiques de résistance", "Faciles à utiliser à la maison, excellents pour la musculation et doux pour les articulations. Parfaits pour les débutants !", "Exercice"),
        Tip("👟", "Marchez plus", "Prenez les escaliers, garez-vous plus loin, marchez en parlant au téléphone. Chaque pas compte !", "Exercice"),
        Tip("🎵", "Motivation musicale", "Créez une playlist d'exercice. La musique rend les entraînements plus faciles et plus agréables !", "Exercice"),
        Tip("👥", "Système du binôme", "Faites de l'exercice avec un ami - vous êtes plus susceptible de continuer et de vous amuser !", "Exercice"),
        Tip("📱", "Suivez vos pas", "Utilisez un compteur de pas. Visez à augmenter de 500 pas chaque semaine jusqu'à atteindre 8000-10000 par jour.", "Exercice"),
        Tip("🌅", "Étirements matinaux", "Commencez votre journée par 5 minutes d'étirements. Réveille votre corps et améliore la flexibilité !", "Exercice"),
        Tip("🪑", "Exercices au bureau", "Vous restez assis trop longtemps ? Faites des levées de jambes assises, des rotations d'épaules et levez-vous toutes les heures.", "Exercice"),
        // INSULIN & MEDICATION TIPS - FRENCH
        Tip("💉", "Rotation des sites", "Faites pivoter les sites d'injection d'insuline pour éviter la lipodystrophie (bosses sous la peau). Abdomen, cuisses, bras.", "Insuline"),
        Tip("🌡️", "Stockage de l'insuline", "Ne laissez pas l'insuline geler ou chauffer. Conservez-la au réfrigérateur, mais laissez-la revenir à température ambiante avant l'injection.", "Insuline"),
        Tip("⏱️", "Le timing compte", "Prenez l'insuline rapide 15 à 20 minutes avant les repas pour de meilleurs résultats. Elle atteint son pic quand votre nourriture digère !", "Insuline"),
        Tip("📝", "Suivez vos doses", "Enregistrer l'insuline aide à identifier les tendances. Utilisez notre calculateur pour apprendre de votre historique !", "Insuline"),
        Tip("🧴", "Vérifiez l'expiration", "Vérifiez toujours les dates d'expiration de l'insuline. L'insuline périmée peut ne pas fonctionner correctement.", "Insuline"),
        Tip("🗑️", "Élimination sécurisée", "Utilisez un conteneur pour objets pointus pour les aiguilles. Ne jetez jamais les aiguilles seules à la poubelle !", "Insuline"),
        Tip("✈️", "Voyager avec l'insuline", "Gardez l'insuline dans vos bagages à main. Les soutes des avions sont trop froides !", "Insuline"),
        Tip("📦", "Ne secouez pas", "Roulez doucement les flacons d'insuline pour mélanger. Secouer peut créer des bulles et endommager l'insuline.", "Insuline"),
        Tip("🩹", "Points d'injection", "Évitez d'injecter dans les cicatrices, les grains de beauté ou les zones avec des bosses. Nettoyez la peau avec de l'alcool d'abord !", "Insuline"),
        Tip("🧊", "Packs Frio", "Utilisez des packs Frio lorsque vous voyagez dans des climats chauds. Ils gardent l'insuline au frais sans réfrigération.", "Insuline"),
        Tip("📅", "Suivez les renouvellements", "Marquez votre calendrier pour les renouvellements d'ordonnance. Ne manquez pas d'insuline !", "Insuline"),
        Tip("💊", "Routine médicamenteuse", "Prenez les médicaments pour le diabète à la même heure chaque jour. Utilisez un organiseur de pilules si nécessaire.", "Insuline"),
        Tip("🔄", "Changer d'insuline", "Ne changez jamais de type d'insuline sans l'approbation du médecin. Ils fonctionnent différemment !", "Insuline"),
        Tip("📞", "Réserve d'urgence", "Ayez toujours une réserve d'urgence de 3 jours d'insuline et de fournitures. Les catastrophes naturelles arrivent !", "Insuline"),
        // GLUCOSE MONITORING TIPS - FRENCH
        Tip("📊", "Connaissez vos chiffres", "Objectif : Avant les repas 80-130 mg/dL, après les repas <180 mg/dL, HbA1c <7 %. Connaissez vos objectifs !", "Surveillance"),
        Tip("📓", "Enregistrez les tendances", "Suivez non seulement les chiffres mais aussi ce que vous avez mangé, l'activité, le stress. Les tendances vous aident à mieux gérer !", "Surveillance"),
        Tip("👆", "Lavez-vous les mains", "Lavez-vous toujours les mains avec du savon et de l'eau avant de tester. Les résidus alimentaires peuvent donner des faux élevés !", "Surveillance"),
        Tip("🩸", "Sites alternatifs", "Certains lecteurs permettent de tester sur le bras ou la paume. Bon quand les doigts sont douloureux.", "Surveillance"),
        Tip("📉", "Les tendances comptent", "Une lecture élevée n'est pas une catastrophe. Cherchez des tendances sur la durée, pas des chiffres isolés.", "Surveillance"),
        Tip("⏰", "Horaires réguliers", "Testez aux mêmes heures chaque jour pour des données cohérentes. Avant les repas et au coucher sont essentiels.", "Surveillance"),
        Tip("🔄", "Faites pivoter les doigts", "Utilisez différents doigts et différents endroits sur chaque doigt. Donnez une pause aux points douloureux !", "Surveillance"),
        Tip("📱", "Utilisez notre application", "Enregistrez votre glycémie dans notre application pour voir les tendances et obtenir des informations de vos données !", "Surveillance"),
        Tip("📈", "Apprenez vos tendances", "Vous remarquez des pics matinaux ? Peut-être le phénomène de l'aube. Des baisses l'après-midi ? Peut-être l'effet de l'exercice.", "Surveillance"),
        Tip("🧪", "Vérifiez les cétones", "Testez les cétones lorsque la glycémie dépasse 250 mg/dL, surtout en cas de maladie. Prévient l'acidocétose diabétique !", "Surveillance"),
        Tip("🔋", "Batterie du lecteur", "Gardez des piles de rechange pour votre lecteur de glycémie. Vous ne voulez pas qu'il tombe en panne quand vous en avez besoin !", "Surveillance"),
        Tip("📅", "Solution de contrôle", "Utilisez une solution de contrôle chaque mois pour vous assurer que votre lecteur est précis.", "Surveillance"),
        // LIFESTYLE & WELLNESS TIPS - FRENCH
        Tip("😴", "Le sommeil compte", "Un mauvais sommeil augmente la résistance à l'insuline. Visez 7-8 heures par nuit. Un coucher régulier aide !", "Bien-être"),
        Tip("🧘", "Moins de stress", "Les hormones de stress augmentent la glycémie. Essayez la respiration profonde, la méditation ou un loisir qui vous plaît.", "Bien-être"),
        Tip("💧", "Restez hydraté", "L'eau aide les reins à éliminer l'excès de sucre. Visez 8 verres par jour !", "Bien-être"),
        Tip("🚭", "Arrêtez de fumer", "Le tabagisme augmente le risque de complications du diabète. Votre médecin peut vous aider à arrêter.", "Bien-être"),
        Tip("🍷", "Alcool avec modération", "Si vous buvez, faites-le avec de la nourriture. L'alcool peut causer des hypoglycémies tardives des heures après !", "Bien-être"),
        Tip("🦷", "Santé dentaire", "Le diabète augmente le risque de maladie des gencives. Brossez-vous deux fois par jour, utilisez du fil dentaire, consultez régulièrement le dentiste.", "Bien-être"),
        Tip("👣", "Soin des pieds", "Vérifiez vos pieds quotidiennement pour les coupures, ampoules, rougeurs. Le diabète peut réduire la sensation - vous pourriez ne pas sentir les blessures !", "Bien-être"),
        Tip("👀", "Examens des yeux", "Faites un examen annuel des yeux avec dilatation. Le diabète est une cause majeure de perte de vision, mais un traitement précoce aide !", "Bien-être"),
        Tip("💅", "Soin des ongles", "Coupez les ongles droits et limez les bords. Évitez de couper les cuticules pour prévenir les infections.", "Bien-être"),
        Tip("🧦", "Chaussettes adaptées", "Portez des chaussettes propres et sèches sans élastique serré. Envisagez des chaussettes diabétiques avec rembourrage supplémentaire.", "Bien-être"),
        Tip("👞", "Chaussures adaptées", "Portez des chaussures bien ajustées. Faites le rodage des nouvelles chaussures progressivement. Vérifiez l'intérieur pour des objets avant de les enfiler.", "Bien-être"),
        Tip("🌞", "Vitamine D", "Faites vérifier vos niveaux de vitamine D. Un faible taux de vitamine D est lié à la résistance à l'insuline.", "Bien-être"),
        Tip("🤝", "Système de soutien", "Rejoignez un groupe de soutien pour le diabète. Partager des expériences aide la santé mentale et l'apprentissage.", "Bien-être"),
        Tip("🎉", "Célébrez les victoires", "Célébrez les petites victoires - une bonne lecture, une nouvelle recette, 1000 pas supplémentaires. Vous faites du bon travail !", "Bien-être"),
        // SAFETY & EMERGENCY TIPS - FRENCH
        Tip("⚡", "Règle des 15-15", "Glycémie basse ? Mangez 15g de glucides (120ml de jus, 3-4 comprimés de glucose), attendez 15 minutes, revérifiez. Répétez si nécessaire.", "Sécurité"),
        Tip("🆔", "Identification médicale", "Portez un bracelet d'identification médicale. En cas d'urgence, il indique aux secours que vous avez du diabète.", "Sécurité"),
        Tip("🍬", "Emportez toujours des collations", "Emportez toujours des glucides à action rapide comme des comprimés de glucose, des bonbons ou des boîtes de jus. Soyez prêt !", "Sécurité"),
        Tip("🚗", "Conduite sécuritaire", "Vérifiez votre glycémie avant de conduire. Ne conduisez jamais si elle est basse ou si vous ressentez des symptômes. Gardez des collations dans la voiture !", "Sécurité"),
        Tip("🏥", "Connaissez les signes d'urgence", "Hypoglycémie sévère : confusion, incapacité de manger, inconscience. Utilisez le glucagon et appelez le 15.", "Sécurité"),
        Tip("🔥", "Avertissement ACD", "Glycémie très élevée + nausées/vomissements + haleine fruitée = Acidocétose Diabétique. Allez immédiatement aux urgences !", "Sécurité"),
        Tip("🤒", "Règles pour les jours de maladie", "En cas de maladie, vérifiez la glycémie toutes les 4 heures. Restez hydraté. Ayez un plan de repas pour les jours de maladie prêt !", "Sécurité"),
        Tip("📞", "Contacts d'urgence", "Gardez le numéro de votre médecin et vos contacts d'urgence dans votre téléphone. ICE (In Case of Emergency) est standard.", "Sécurité"),
        Tip("🧾", "Carte d'information médicale", "Portez une carte avec vos conditions, médicaments, allergies et contacts médicaux.", "Sécurité"),
        Tip("🌡️", "Avertissement de fièvre", "La fièvre peut faire grimper la glycémie. Vérifiez plus souvent et restez en contact avec votre médecin.", "Sécurité"),
        Tip("💊", "Interactions médicamenteuses", "Certains médicaments (stéroïdes, décongestionnants) peuvent augmenter la glycémie. Demandez conseil au pharmacien avant de les prendre.", "Sécurité"),
        Tip("✈️", "Préparation au voyage", "En voyage, emportez deux fois plus de fournitures que nécessaire. Gardez-en dans vos bagages à main et dans vos bagages en soute.", "Sécurité"),
        // SPECIAL SITUATIONS TIPS - FRENCH
        Tip("🕌", "Jeûne du Ramadan", "Si vous jeûnez pendant le Ramadan, travaillez avec votre médecin pour ajuster les médicaments. Vérifiez la glycémie plus souvent.", "Spécial"),
        Tip("🤰", "Grossesse", "La grossesse modifie les besoins en insuline. Travaillez en étroite collaboration avec votre équipe soignante pour un bébé en bonne santé !", "Spécial"),
        Tip("👴", "Vieillir avec le diabète", "En vieillissant, la prévention des chutes devient cruciale. Gardez les passages dégagés, utilisez des veilleuses.", "Spécial"),
        Tip("🧒", "Enfants avec le diabète", "Impliquez toute la famille dans les soins. Faites-en quelque chose de normal, pas effrayant. Célébrez leur courage !", "Spécial"),
        Tip("🏫", "Planification scolaire", "Travaillez avec l'école sur un plan de soins pour le diabète. Assurez-vous que le personnel sait quoi faire en cas d'urgence.", "Spécial"),
        Tip("💼", "Droits au travail", "Vous avez droit à des pauses pour tester et manger au travail. Connaissez vos aménagements sur le lieu de travail !", "Spécial"),
        Tip("💑", "Rencontres avec le diabète", "Soyez ouvert sur votre diabète. Les bons partenaires vous soutiendront. Vous méritez d'être compris !", "Spécial"),
        Tip("🏕️", "Conseils de camping", "Apportez des fournitures supplémentaires, gardez l'insuline au frais, et informez vos compagnons de camping de votre diabète.", "Spécial"),
        Tip("🎢", "Repas de fêtes", "Les fêtes = tentation. Utilisez une assiette plus petite, remplissez de légumes d'abord, profitez des gourmandises avec modération.", "Spécial"),
        // MOTIVATIONAL TIPS - FRENCH
        Tip("🌟", "Tu peux le faire", "Gérer le diabète est difficile, mais vous le faites ! Chaque jour où vous vous présentez et essayez est une victoire.", "Motivation"),
        Tip("💪", "Petits pas", "Vous n'avez pas besoin d'être parfait. De petits pas constants mènent à de grands changements avec le temps.", "Motivation"),
        Tip("🎯", "Fixez-vous des objectifs", "Fixez-vous de petits objectifs réalisables. Marchez 10 minutes par jour. Ajoutez un légume au dîner. Construisez à partir de là.", "Motivation"),
        Tip("📈", "Progrès pas perfection", "Ne laissez pas une mauvaise lecture ruiner votre journée. Regardez la tendance, pas le point unique.", "Motivation"),
        Tip("🤗", "Soyez gentil avec vous-même", "Le diabète est difficile. Vous faites de votre mieux. Traitez-vous avec la gentillesse que vous méritez.", "Motivation"),
        Tip("🌈", "Vous n'êtes pas seul", "Plus de 400 millions de personnes dans le monde ont le diabète. Vous faites partie d'une immense communauté !", "Motivation"),
        Tip("🏆", "Célébrez", "Célébrez vos victoires - bon A1C, nouvelle routine d'exercice, préparation d'un repas sain. Vous l'avez mérité !", "Motivation"),
        Tip("🌱", "Continuez à grandir", "Chaque jour, vous apprenez quelque chose de nouveau sur votre corps et le diabète. Cette connaissance est un pouvoir !", "Motivation"),
    )
    private fun getArabicTips(): List<Tip> = listOf(
        // NUTRITION TIPS - ARABIC
        Tip("🍎", "تفاحة في اليوم", "تفاحة مع زبدة الفول السوداني هي الوجبة الخفيفة المثالية لمرضى السكري: الألياف من التفاح + البروتين من زبدة الفول السوداني = سكر دم مستقر!", "Nutrition"),
        Tip("🥑", "الدهون الصحية", "الأفوكادو والمكسرات وزيت الزيتون لا ترفع سكر الدم وتبقيك ممتلئًا لفترة أطول. أضفها إلى السلطات أو تناولها كوجبات خفيفة!", "Nutrition"),
        Tip("🥗", "طريقة الطبق", "املأ نصف طبقك بالخضروات غير النشوية، وربع بالبروتين الخالي من الدهون، وربع بالكربوهيدرات المعقدة. بسيط وفعال!", "Nutrition"),
        Tip("🌿", "الخضروات الورقية", "السبانخ والكرنب والخس مليئة بالفيتامينات ولها تأثير ضئيل على سكر الدم. تناولها بحرية!", "Nutrition"),
        Tip("🥦", "الخضروات الصليبية", "البروكلي والقرنبيط والكرنب غنية بالألياف ومضادات الأكسدة. رائعة للتحكم في مرض السكري!", "Nutrition"),
        Tip("🍠", "البطاطا الحلوة", "البطاطا الحلوة لديها مؤشر جلايسيمي أقل من البطاطس العادية. تناولها مع القشرة لمزيد من الألياف!", "Nutrition"),
        Tip("🥜", "المكسرات للوجبات الخفيفة", "حفنة من اللوز أو الجوز تعتبر وجبة خفيفة رائعة - بروتين ودهون صحية وكربوهيدرات قليلة!", "Nutrition"),
        Tip("🫘", "قوة الفول", "الفول والعدس غنيان بالألياف والبروتين، بطيئا الهضم، ولا يرفعان سكر الدم!", "Nutrition"),
        Tip("🐟", "أوميغا-3", "السلمون والسردين والماكريل غنية بأوميغا-3 التي تقلل الالتهاب وتحسن صحة القلب!", "Nutrition"),
        Tip("🍳", "البيض ممتاز", "البيض غني بالبروتين ولا يحتوي على كربوهيدرات. مثالي للإفطار ليبقيك ممتلئًا حتى الغداء!", "Nutrition"),
        Tip("🥛", "الزبادي اليوناني", "الزبادي اليوناني يحتوي على ضعف بروتين الزبادي العادي. اختر السادة لتجنب السكريات المضافة!", "Nutrition"),
        Tip("🍓", "التوت جيد", "التوت أقل سكرًا من الفواكه الأخرى ومليء بمضادات الأكسدة. التوت الأزرق والفراولة والتوت الأحمر!", "Nutrition"),
        Tip("🍋", "قوة الحمضيات", "البرتقال والليمون والجريب فروت توفر فيتامين سي دون رفع سكر الدم كثيرًا. تناول الفاكهة كاملة، وليس العصير!", "Nutrition"),
        Tip("🍚", "الأرز البني", "استبدل الأرز الأبيض بالأرز البني لمزيد من الألياف وإطلاق أبطأ للجلوكوز!", "Nutrition"),
        Tip("🌾", "الحبوب الكاملة", "الشوفان والكينوا والشعير خيارات ممتازة. تطلق الطاقة ببطء وتبقيك ممتلئًا!", "Nutrition"),
        Tip("🧂", "راقب الملح", "مرض السكري يزيد من خطر أمراض القلب. حد الصوديوم إلى 2300 مجم يوميًا - تجنب الأطعمة المصنعة!", "Nutrition"),
        Tip("🧈", "بدائل الزبدة", "استخدم زيت الزيتون أو زيت الأفوكادو بدلاً من الزبدة. أفضل لقلبك وحساسية الأنسولين!", "Nutrition"),
        Tip("🥤", "تجنب المشروبات السكرية", "الصودا والشاي المحلى وعصائر الفاكهة ترفع سكر الدم بسرعة. الماء دائمًا هو الأفضل!", "Nutrition"),
        Tip("☕", "حقائق عن القهوة", "القهوة السوداء قد تقلل من خطر السكري. لكن تجنب السكر والشراب المنكه!", "Nutrition"),
        Tip("🍵", "الشاي الأخضر", "الشاي الأخضر غني بمضادات الأكسدة وقد يحسن حساسية الأنسولين. اشربه بدون سكر!", "Nutrition"),
        // EXERCISE TIPS - ARABIC
        Tip("🚶", "مشى بعد الوجبة", "المشي لمدة 15 دقيقة بعد الوجبات يمكن أن يخفض سكر الدم بنسبة تصل إلى 30٪! اجعله عادة.", "تمارين"),
        Tip("🏋️", "تدريبات القوة", "بناء العضلات يساعد جسمك على استخدام الأنسولين بشكل أكثر فعالية. جرب الأوزان أو الأربطة المطاطية 2-3 مرات في الأسبوع.", "تمارين"),
        Tip("🧘", "فوائد اليوجا", "اليوجا تقلل هرمونات التوتر التي ترفع سكر الدم. كما أنها تحسن المرونة والتوازن!", "تمارين"),
        Tip("🚴", "الدراجة للصحة", "ركوب الدراجة منخفض التأثير ورائع لصحة القلب والأوعية الدموية. ابدأ بـ 15-20 دقيقة يوميًا.", "تمارين"),
        Tip("🏊", "السباحة بأمان", "السباحة سهلة على المفاصل وتمرين كامل للجسم. تحقق دائمًا من سكر الدم قبل وبعد!", "تمارين"),
        Tip("⏰", "التوقيت مهم", "التمرين الصباحي قد يخفض سكر الدم طوال اليوم. اكتشف ما يناسب جدولك!", "تمارين"),
        Tip("🎯", "ابدأ صغيرًا", "حتى 10 دقائق من النشاط تساعد. ابدأ صغيرًا وزد حتى 150 دقيقة في الأسبوع.", "تمارين"),
        Tip("🦵", "يوم الساقين", "تمارين الساقين تستخدم عضلات كبيرة وتحترق المزيد من الجلوكوز. القرفصاء والاندفاع رائعة!", "تمارين"),
        Tip("💪", "الأربطة المطاطية", "سهلة الاستخدام في المنزل، رائعة لتدريبات القوة، ولطيفة على المفاصل. مثالية للمبتدئين!", "تمارين"),
        Tip("👟", "امش أكثر", "استخدم السلالم، ابتعد في الوقوف، تمشى أثناء التحدث على الهاتف. كل خطوة تهم!", "تمارين"),
        Tip("🎵", "تحفيز الموسيقى", "أنشئ قائمة تشغيل للتمرين. الموسيقى تجعل التمارين أسهل وأكثر متعة!", "تمارين"),
        Tip("👥", "نظام الرفيق", "تمرن مع صديق - من المرجح أن تستمر وتستمتع!", "تمارين"),
        Tip("📱", "تتبع خطواتك", "استخدم عداد الخطوات. استهدف زيادة 500 خطوة كل أسبوع حتى تصل إلى 8000-10000 يوميًا.", "تمارين"),
        Tip("🌅", "تمدد الصباح", "ابدأ يومك بـ 5 دقائق من التمدد. ينشط جسمك ويحسن المرونة!", "تمارين"),
        Tip("🪑", "تمارين المكتب", "تجلس طويلاً؟ قم برفع الساقين أثناء الجلوس، ودوران الكتفين، وانهض كل ساعة.", "تمارين"),
        // INSULIN & MEDICATION TIPS - ARABIC
        Tip("💉", "تدوير أماكن الحقن", "قم بتدوير أماكن حقن الأنسولين لمنع تضخم الأنسجة (كتل تحت الجلد). البطن، الفخذين، أعلى الذراعين.", "الأنسولين"),
        Tip("🌡️", "تخزين الأنسولين", "لا تدع الأنسولين يتجمد أو يسخن. خزنه في الثلاجة، لكن دعه يصل إلى درجة حرارة الغرفة قبل الحقن.", "الأنسولين"),
        Tip("⏱️", "التوقيت مهم", "تناول الأنسولين سريع المفعول قبل 15-20 دقيقة من الوجبات للحصول على أفضل النتائج. يصل ذروته عندما يهضم طعامك!", "الأنسولين"),
        Tip("📝", "تتبع جرعاتك", "تسجيل الأنسولين يساعد في تحديد الأنماط. استخدم حاسبتنا للتعلم من تاريخك!", "الأنسولين"),
        Tip("🧴", "تحقق من تاريخ الصلاحية", "تحقق دائمًا من تواريخ انتهاء صلاحية الأنسولين. الأنسولين منتهي الصلاحية قد لا يعمل بشكل صحيح.", "الأنسولين"),
        Tip("🗑️", "التخلص بأمان", "استخدم حاوية للأدوات الحادة للإبر. لا ترمي الإبر السائبة في سلة المهملات أبدًا!", "الأنسولين"),
        Tip("✈️", "السفر مع الأنسولين", "احتفظ بالأنسولين في أمتعة اليد. عنابر الطائرات باردة جدًا!", "الأنسولين"),
        Tip("📦", "لا ترج", "لف قوارير الأنسولين بلطف للخلط. الرج يمكن أن يخلق فقاعات ويتلف الأنسولين.", "الأنسولين"),
        Tip("🩹", "أماكن الحقن", "تجنب الحقن في الندوب أو الشامات أو المناطق التي بها كتل. نظف الجلد بالكحول أولاً!", "الأنسولين"),
        Tip("🧊", "أكياس فريو", "استخدم أكياس فريو عند السفر في المناخات الحارة. تحافظ على الأنسولين باردًا بدون تبريد.", "الأنسولين"),
        Tip("📅", "تتبع إعادة التعبئة", "حدد في تقويمك مواعيد إعادة تعبئة الوصفات الطبية. لا تنفد من الأنسولين!", "الأنسولين"),
        Tip("💊", "روتين الدواء", "تناول أدوية السكري في نفس الوقت يوميًا. استخدم منظم حبوب إذا لزم الأمر.", "الأنسولين"),
        Tip("🔄", "تبديل الأنسولين", "لا تغير أنواع الأنسولين أبدًا دون موافقة الطبيب. إنها تعمل بشكل مختلف!", "الأنسولين"),
        Tip("📞", "إمداد الطوارئ", "احتفظ دائمًا بإمداد طوارئ لمدة 3 أيام من الأنسولين والمستلزمات. الكوارث الطبيعية تحدث!", "الأنسولين"),
        // GLUCOSE MONITORING TIPS - ARABIC
        Tip("📊", "اعرف أرقامك", "الهدف: قبل الوجبات 80-130 ملجم/ديسيلتر، بعد الوجبات <180 ملجم/ديسيلتر، الهيموجلوبين السكري <7%. اعرف أهدافك!", "مراقبة"),
        Tip("📓", "تسجيل الأنماط", "تتبع ليس فقط الأرقام ولكن أيضًا ما أكلته ونشاطك وتوترك. الأنماط تساعدك على التحكم بشكل أفضل!", "مراقبة"),
        Tip("👆", "اغسل يديك", "اغسل يديك دائمًا بالصابون والماء قبل الاختبار. بقايا الطعام يمكن أن تعطي قراءات مرتفعة خاطئة!", "مراقبة"),
        Tip("🩸", "مواقع بديلة", "بعض الأجهزة تسمح باختبار الذراع أو راحة اليد. جيد عندما تكون الأصابع مؤلمة.", "مراقبة"),
        Tip("📉", "الأنماط مهمة", "قراءة مرتفعة واحدة ليست كارثة. ابحث عن الأنماط بمرور الوقت، وليس الأرقام الفردية.", "مراقبة"),
        Tip("⏰", "التوقيت المتسق", "اختبر في نفس الأوقات يوميًا للحصول على بيانات متسقة. قبل الوجبات ووقت النوم هما المفتاح.", "مراقبة"),
        Tip("🔄", "دور الأصابع", "استخدم أصابع مختلفة ونقاط مختلفة في كل إصبع. امنح النقاط المؤلمة راحة!", "مراقبة"),
        Tip("📱", "استخدم تطبيقنا", "سجل الجلوكوز في تطبيقنا لرؤية الاتجاهات والحصول على رؤى من بياناتك!", "مراقبة"),
        Tip("📈", "تعلم أنماطك", "تلاحظ ارتفاعًا صباحيًا؟ قد يكون ظاهرة الفجر. انخفاض بعد الظهر؟ ربما تأثير التمرين.", "مراقبة"),
        Tip("🧪", "تحقق من الكيتونات", "اختبر الكيتونات عندما يكون الجلوكوز أعلى من 250 ملجم/ديسيلتر، خاصة عند المرض. يمنع الحماض الكيتوني السكري!", "مراقبة"),
        Tip("🔋", "بطارية الجهاز", "احتفظ ببطاريات احتياطية لجهاز قياس الجلوكوز. لا تريده أن ينفد عندما تحتاجه!", "مراقبة"),
        Tip("📅", "محلول التحكم", "استخدم محلول التحكم شهريًا للتأكد من دقة جهاز القياس.", "مراقبة"),
        // LIFESTYLE & WELLNESS TIPS - ARABIC
        Tip("😴", "النوم مهم", "قلة النوم تزيد من مقاومة الأنسولين. استهدف 7-8 ساعات ليلاً. وقت النوم المنتظم يساعد!", "العافية"),
        Tip("🧘", "قلل التوتر", "هرمونات التوتر ترفع سكر الدم. جرب التنفس العميق أو التأمل أو هواية تستمتع بها.", "العافية"),
        Tip("💧", "ابق رطبًا", "الماء يساعد الكلى على طرد السكر الزائد. استهدف 8 أكواب يوميًا!", "العافية"),
        Tip("🚭", "أقلع عن التدخين", "التدخين يزيد من خطر مضاعفات السكري. طبيبك يمكنه مساعدتك في خطة الإقلاع.", "العافية"),
        Tip("🍷", "الكحول بحكمة", "إذا كنت تشرب، فافعل ذلك مع الطعام. الكحول يمكن أن يسبب انخفاضًا متأخرًا بعد ساعات!", "العافية"),
        Tip("🦷", "صحة الأسنان", "السكري يزيد من خطر أمراض اللثة. اغسل أسنانك مرتين يوميًا، استخدم الخيط، راجع طبيب الأسنان بانتظام.", "العافية"),
        Tip("👣", "العناية بالقدمين", "افحص قدميك يوميًا للجروح والبثور والاحمرار. السكري يمكن أن يقلل الإحساس - قد لا تشعر بالإصابات!", "العافية"),
        Tip("👀", "فحوصات العين", "احصل على فحص سنوي للعين مع توسيع الحدقة. السكري هو سبب رئيسي لفقدان البصر، لكن العلاج المبكر يساعد!", "العافية"),
        Tip("💅", "العناية بالأظافر", "اقطع الأظافر بشكل مستقيم وبرد الحواف. تجنب قطع الجلد المحيط لمنع العدوى.", "العافية"),
        Tip("🧦", "جوارب مناسبة", "ارتدِ جوارب نظيفة وجافة بدون مطاط ضيق. فكر في جوارب للسكري مع تبطين إضافي.", "العافية"),
        Tip("👞", "أحذية مناسبة", "ارتدِ أحذية مناسبة المقاس. قم بتليين الأحذية الجديدة تدريجيًا. تحقق من الداخل بحثًا عن أشياء قبل الارتداء.", "العافية"),
        Tip("🌞", "فيتامين د", "افحص مستويات فيتامين د. انخفاض فيتامين د مرتبط بمقاومة الأنسولين.", "العافية"),
        Tip("🤝", "نظام الدعم", "انضم إلى مجموعة دعم للسكري. مشاركة التجارب تساعد الصحة النفسية والتعلم.", "العافية"),
        Tip("🎉", "احتفل بالانتصارات", "احتفل بالانتصارات الصغيرة - قراءة جيدة، وصفة جديدة، 1000 خطوة إضافية. أنت تبلي بلاءً حسنًا!", "العافية"),
        // SAFETY & EMERGENCY TIPS - ARABIC
        Tip("⚡", "قاعدة 15-15", "سكر منخفض؟ تناول 15 جرام كربوهيدرات (120 مل عصير، 3-4 أقراص جلوكوز)، انتظر 15 دقيقة، أعد الفحص. كرر إذا لزم الأمر.", "سلامة"),
        Tip("🆔", "بطاقة تعريف طبية", "ارتدِ سوار تعريف طبي. في حالة الطوارئ، يخبر المستجيبين أنك مصاب بالسكري.", "سلامة"),
        Tip("🍬", "احمل دائمًا وجبات خفيفة", "احمل دائمًا كربوهيدرات سريعة المفعول مثل أقراص الجلوكوز أو الحلوى أو علب العصير. كن مستعدًا!", "سلامة"),
        Tip("🚗", "قيادة آمنة", "تحقق من سكر الدم قبل القيادة. لا تقود أبدًا إذا كان منخفضًا أو شعرت بأعراض. احتفظ بوجبات خفيفة في السيارة!", "سلامة"),
        Tip("🏥", "اعرف علامات الطوارئ", "نقص السكر الشديد: ارتباك، عدم القدرة على الأكل، فقدان الوعي. استخدم الجلوكاجون واتصل بالإسعاف.", "سلامة"),
        Tip("🔥", "تحذير الحماض الكيتوني", "سكر مرتفع جدًا + غثيان/قيء + رائحة الفم فاكهية = حماض كيتوني سكري. اذهب إلى الطوارئ فورًا!", "سلامة"),
        Tip("🤒", "قواعد يوم المرض", "عند المرض، تحقق من سكر الدم كل 4 ساعات. حافظ على رطوبتك. اجعل خطة وجبات ليوم المرض جاهزة!", "سلامة"),
        Tip("📞", "جهات اتصال الطوارئ", "احتفظ برقم طبيبك وجهات اتصال الطوارئ في هاتفك. ICE (في حالة الطوارئ) هو المعيار.", "سلامة"),
        Tip("🧾", "بطاقة المعلومات الطبية", "احمل بطاقة تحتوي على حالتك الطبية وأدويتك وحساسياتك واتصالات طبيبك.", "سلامة"),
        Tip("🌡️", "تحذير الحمى", "الحمى يمكن أن ترفع سكر الدم. تحقق أكثر وابق على اتصال بطبيبك.", "سلامة"),
        Tip("💊", "تفاعلات الأدوية", "بعض الأدوية (الستيرويدات، مزيلات الاحتقان) يمكن أن ترفع سكر الدم. اسأل الصيدلي قبل تناولها.", "سلامة"),
        Tip("✈️", "الاستعداد للسفر", "عند السفر، احزم ضعف المستلزمات التي تحتاجها. احتفظ ببعضها في حقيبة اليد وبعضها في الأمتعة المسجلة.", "سلامة"),
        // SPECIAL SITUATIONS TIPS - ARABIC
        Tip("🕌", "صيام رمضان", "إذا كنت تصوم خلال رمضان، اعمل مع طبيبك لتعديل الأدوية. تحقق من سكر الدم بشكل متكرر.", "خاص"),
        Tip("🤰", "الحمل", "الحمل يغير احتياجات الأنسولين. اعمل بشكل وثيق مع فريق الرعاية الصحية الخاص بك لطفل سليم!", "خاص"),
        Tip("👴", "الشيخوخة مع السكري", "مع تقدم العمر، تصبح الوقاية من السقوط حاسمة. حافظ على الممرات خالية، واستخدم أضواء الليل.", "خاص"),
        Tip("🧒", "الأطفال المصابون بالسكري", "أشرك الأسرة بأكملها في الرعاية. اجعل الأمر طبيعيًا وليس مخيفًا. احتفل بشجاعتهم!", "خاص"),
        Tip("🏫", "التخطيط للمدرسة", "اعمل مع المدرسة على خطة رعاية للسكري. تأكد من أن الموظفين يعرفون ماذا يفعلون في حالات الطوارئ.", "خاص"),
        Tip("💼", "حقوق مكان العمل", "لديك حقوق في فترات راحة للاختبار وتناول الطعام في العمل. اعرف تسهيلات مكان العمل الخاصة بك!", "خاص"),
        Tip("💑", "المواعدة مع السكري", "كن منفتحًا بشأن مرض السكري. الشركاء الجيدون سيدعمونك. أنت تستحق التفهم!", "خاص"),
        Tip("🏕️", "نصائح التخييم", "احضر مستلزمات إضافية، حافظ على برودة الأنسولين، وأخبر رفاق التخييم عن مرض السكري الخاص بك.", "خاص"),
        Tip("🎢", "تناول الطعام في العطلات", "العطلات = إغراء. استخدم طبقًا أصغر، املأ بالخضروات أولاً، استمتع بالحلويات باعتدال.", "خاص"),
        // MOTIVATIONAL TIPS - ARABIC
        Tip("🌟", "أنت قادر على ذلك", "إدارة السكري صعبة، لكنك تفعلها! كل يوم تظهر فيه وتحاول هو فوز.", "تحفيز"),
        Tip("💪", "خطوات صغيرة", "ليس عليك أن تكون مثاليًا. الخطوات الصغيرة المتسقة تؤدي إلى تغييرات كبيرة بمرور الوقت.", "تحفيز"),
        Tip("🎯", "حدد أهدافًا", "حدد أهدافًا صغيرة قابلة للتحقيق. امشِ 10 دقائق يوميًا. أضف خضارًا واحدًا للعشاء. انطلق من هناك.", "تحفيز"),
        Tip("📈", "تقدم وليس كمال", "لا تدع قراءة سيئة واحدة تدمر يومك. انظر إلى الاتجاه، وليس النقطة الواحدة.", "تحفيز"),
        Tip("🤗", "كن لطيفًا مع نفسك", "السكري صعب. أنت تبذل قصارى جهدك. عامل نفسك بلطف تستحقه.", "تحفيز"),
        Tip("🌈", "لست وحدك", "أكثر من 400 مليون شخص حول العالم مصابون بالسكري. أنت جزء من مجتمع ضخم!", "تحفيز"),
        Tip("🏆", "احتفل", "احتفل بانتصاراتك - مستوى A1C جيد، روتين تمرين جديد، طهي وجبة صحية. لقد استحققت ذلك!", "تحفيز"),
        Tip("🌱", "استمر في النمو", "كل يوم تتعلم شيئًا جديدًا عن جسمك وعن السكري. هذه المعرفة قوة!", "تحفيز"),

    )

    data class Tip(
        val icon: String,
        val title: String,
        val content: String,
        val category: String
    )

}