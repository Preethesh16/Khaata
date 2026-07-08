package com.khaata.app.data

/**
 * Pre-seeds the 50-item kirana catalog on first launch.
 * Prices are typical Bangalore kirana MRPs (July 2026-ish, demo data).
 */
object CatalogSeeder {

    suspend fun seedIfEmpty(db: AppDatabase) {
        if (db.itemDao().count() > 0) return
        db.itemDao().insertAll(CATALOG)
    }

    val CATALOG = listOf(
        Item(1, "आटा (आशीर्वाद)", "ಗೋಧಿ ಹಿಟ್ಟು", "Aashirvaad Atta", 55.0, "kg", 40.0),
        Item(2, "आटा (पिल्सबरी)", "ಪಿಲ್ಸ್‌ಬರಿ ಹಿಟ್ಟು", "Pillsbury Atta", 52.0, "kg", 25.0),
        Item(3, "चीनी", "ಸಕ್ಕರೆ", "Cheeni", 44.0, "kg", 50.0),
        Item(4, "नमक", "ಉಪ್ಪು", "Salt", 24.0, "kg", 30.0),
        Item(5, "तूर दाल", "ತೊಗರಿ ಬೇಳೆ", "Toor Dal", 160.0, "kg", 20.0),
        Item(6, "मूंग दाल", "ಹೆಸರು ಬೇಳೆ", "Moong Dal", 130.0, "kg", 18.0),
        Item(7, "चना दाल", "ಕಡಲೆ ಬೇಳೆ", "Chana Dal", 95.0, "kg", 22.0),
        Item(8, "चावल (सोना मसूरी)", "ಸೋನಾ ಮಸೂರಿ ಅಕ್ಕಿ", "Sona Masoori Rice", 65.0, "kg", 60.0),
        Item(9, "बासमती चावल", "ಬಾಸ್ಮತಿ ಅಕ್ಕಿ", "Basmati Rice", 120.0, "kg", 15.0),
        Item(10, "मैदा", "ಮೈದಾ", "Maida", 45.0, "kg", 20.0),
        Item(11, "सूजी", "ರವೆ", "Sooji Rava", 48.0, "kg", 15.0),
        Item(12, "पोहा", "ಅವಲಕ್ಕಿ", "Poha", 60.0, "kg", 12.0),
        Item(13, "सफोला तेल", "ಸಫೋಲಾ ಎಣ್ಣೆ", "Saffola Oil", 180.0, "litre", 15.0),
        Item(14, "सूरजमुखी तेल", "ಸೂರ್ಯಕಾಂತಿ ಎಣ್ಣೆ", "Sunflower Oil", 140.0, "litre", 20.0),
        Item(15, "सरसों तेल", "ಸಾಸಿವೆ ಎಣ್ಣೆ", "Mustard Oil", 170.0, "litre", 10.0),
        Item(16, "घी", "ತುಪ್ಪ", "Ghee", 600.0, "litre", 8.0),
        Item(17, "पारले-जी", "ಪಾರ್ಲೆ-ಜಿ", "Parle-G", 10.0, "pkt", 100.0),
        Item(18, "मैगी", "ಮ್ಯಾಗಿ", "Maggi Noodles", 15.0, "pkt", 80.0),
        Item(19, "डेयरी मिल्क", "ಡೈರಿ ಮಿಲ್ಕ್", "Dairy Milk", 45.0, "piece", 40.0),
        Item(20, "किटकैट", "ಕಿಟ್‌ಕ್ಯಾಟ್", "KitKat", 30.0, "piece", 35.0),
        Item(21, "हॉर्लिक्स", "ಹಾರ್ಲಿಕ್ಸ್", "Horlicks", 260.0, "piece", 10.0),
        Item(22, "बॉर्नविटा", "ಬೋರ್ನ್‌ವಿಟಾ", "Bournvita", 240.0, "piece", 8.0),
        Item(23, "सर्फ एक्सेल", "ಸರ್ಫ್ ಎಕ್ಸೆಲ್", "Surf Excel", 120.0, "kg", 15.0),
        Item(24, "विम बार", "ವಿಮ್ ಬಾರ್", "Vim Bar", 10.0, "piece", 50.0),
        Item(25, "रिन साबुन", "ರಿನ್ ಸೋಪು", "Rin Soap", 32.0, "piece", 30.0),
        Item(26, "कोलगेट", "ಕೋಲ್ಗೇಟ್", "Colgate", 55.0, "piece", 25.0),
        Item(27, "लाइफबॉय", "ಲೈಫ್‌ಬಾಯ್", "Lifebuoy", 35.0, "piece", 40.0),
        Item(28, "डेटॉल", "ಡೆಟಾಲ್", "Dettol", 40.0, "piece", 30.0),
        Item(29, "हाजमोला", "ಹಾಜ್ಮೊಲಾ", "Hajmola", 50.0, "piece", 20.0),
        Item(30, "ब्रेड", "ಬ್ರೆಡ್", "Bread", 40.0, "pkt", 12.0),
        Item(31, "अंडे", "ಮೊಟ್ಟೆ", "Eggs", 84.0, "dozen", 15.0),
        Item(32, "दूध (नंदिनी)", "ನಂದಿನಿ ಹಾಲು", "Nandini Milk", 24.0, "litre", 30.0),
        Item(33, "दही", "ಮೊಸರು", "Curd", 30.0, "pkt", 20.0),
        Item(34, "पनीर", "ಪನೀರ್", "Paneer", 90.0, "pkt", 10.0),
        Item(35, "टाटा चाय", "ಟಾಟಾ ಚಹಾ", "Tata Tea", 145.0, "pkt", 18.0),
        Item(36, "रेड लेबल चाय", "ರೆಡ್ ಲೇಬಲ್ ಚಹಾ", "Red Label Tea", 130.0, "pkt", 15.0),
        Item(37, "नेस्कैफे", "ನೆಸ್ಕೆಫೆ", "Nescafe Coffee", 190.0, "piece", 10.0),
        Item(38, "बेसन", "ಕಡಲೆ ಹಿಟ್ಟು", "Besan", 80.0, "kg", 12.0),
        Item(39, "हल्दी पाउडर", "ಅರಿಶಿನ ಪುಡಿ", "Haldi Powder", 30.0, "pkt", 25.0),
        Item(40, "मिर्ची पाउडर", "ಮೆಣಸಿನ ಪುಡಿ", "Chilli Powder", 45.0, "pkt", 25.0),
        Item(41, "धनिया पाउडर", "ಕೊತ್ತಂಬರಿ ಪುಡಿ", "Dhania Powder", 35.0, "pkt", 20.0),
        Item(42, "जीरा", "ಜೀರಿಗೆ", "Jeera", 55.0, "pkt", 18.0),
        Item(43, "गरम मसाला", "ಗರಂ ಮಸಾಲಾ", "Garam Masala", 60.0, "pkt", 15.0),
        Item(44, "प्याज़", "ಈರುಳ್ಳಿ", "Onion", 35.0, "kg", 40.0),
        Item(45, "आलू", "ಆಲೂಗಡ್ಡೆ", "Potato", 30.0, "kg", 45.0),
        Item(46, "टमाटर", "ಟೊಮೆಟೊ", "Tomato", 40.0, "kg", 30.0),
        Item(47, "कुरकुरे", "ಕುರ್ಕುರೆ", "Kurkure", 20.0, "pkt", 60.0),
        Item(48, "लेज़", "ಲೇಸ್", "Lays Chips", 20.0, "pkt", 60.0),
        Item(49, "गुड़", "ಬೆಲ್ಲ", "Jaggery", 60.0, "kg", 15.0),
        Item(50, "अगरबत्ती", "ಅಗರಬತ್ತಿ", "Agarbatti", 25.0, "pkt", 35.0)
    )
}
