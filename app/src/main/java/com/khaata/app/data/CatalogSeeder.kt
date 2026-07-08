package com.khaata.app.data

object CatalogSeeder {

    suspend fun seedIfEmpty(db: AppDatabase) {
        if (db.itemDao().count() > 0) return
        db.itemDao().insertAll(CATALOG)
    }

    // 50 common kirana items. Prices are typical MRP-ish demo values.
    val CATALOG = listOf(
        Item(1, "आशीर्वाद आटा", "ಆಶೀರ್ವಾದ್ ಹಿಟ್ಟು", "Aashirvaad Atta", 55.0, "kg", 25.0),
        Item(2, "पिल्सबरी आटा", "ಪಿಲ್ಸ್‌ಬರಿ ಹಿಟ್ಟು", "Pillsbury Atta", 52.0, "kg", 20.0),
        Item(3, "मैदा", "ಮೈದಾ", "Maida", 40.0, "kg", 15.0),
        Item(4, "चीनी", "ಸಕ್ಕರೆ", "Cheeni", 40.0, "kg", 30.0),
        Item(5, "नमक", "ಉಪ್ಪು", "Salt", 22.0, "kg", 25.0),
        Item(6, "तूर दाल", "ತೊಗರಿ ಬೇಳೆ", "Toor Dal", 140.0, "kg", 12.0),
        Item(7, "मूंग दाल", "ಹೆಸರು ಬೇಳೆ", "Moong Dal", 120.0, "kg", 10.0),
        Item(8, "चना दाल", "ಕಡಲೆ ಬೇಳೆ", "Chana Dal", 90.0, "kg", 10.0),
        Item(9, "सोना मसूरी चावल", "ಸೋನಾ ಮಸೂರಿ ಅಕ್ಕಿ", "Sona Masoori Rice", 65.0, "kg", 40.0),
        Item(10, "बासमती चावल", "ಬಾಸಮತಿ ಅಕ್ಕಿ", "Basmati Rice", 120.0, "kg", 15.0),
        Item(11, "पोहा", "ಅವಲಕ್ಕಿ", "Poha", 60.0, "kg", 8.0),
        Item(12, "रवा सूजी", "ರವೆ", "Rava Sooji", 45.0, "kg", 10.0),
        Item(13, "सफोला तेल", "ಸಫೋಲಾ ಎಣ್ಣೆ", "Saffola Oil", 180.0, "litre", 12.0),
        Item(14, "सूरजमुखी तेल", "ಸೂರ್ಯಕಾಂತಿ ಎಣ್ಣೆ", "Sunflower Oil", 140.0, "litre", 18.0),
        Item(15, "सरसों तेल", "ಸಾಸಿವೆ ಎಣ್ಣೆ", "Mustard Oil", 160.0, "litre", 8.0),
        Item(16, "देसी घी", "ತುಪ್ಪ", "Ghee", 550.0, "litre", 5.0),
        Item(17, "पारले-जी", "ಪಾರ್ಲೆ-ಜಿ", "Parle-G", 10.0, "pkt", 60.0),
        Item(18, "मैगी", "ಮ್ಯಾಗಿ", "Maggi Noodles", 15.0, "pkt", 48.0),
        Item(19, "डेयरी मिल्क", "ಡೈರಿ ಮಿಲ್ಕ್", "Dairy Milk", 45.0, "piece", 30.0),
        Item(20, "हॉर्लिक्स", "ಹಾರ್ಲಿಕ್ಸ್", "Horlicks", 260.0, "piece", 6.0),
        Item(21, "बॉर्नविटा", "ಬೋರ್ನ್‌ವಿಟಾ", "Bournvita", 250.0, "piece", 5.0),
        Item(22, "सर्फ एक्सेल", "ಸರ್ಫ್ ಎಕ್ಸೆಲ್", "Surf Excel", 130.0, "kg", 14.0),
        Item(23, "विम बार", "ವಿಮ್ ಬಾರ್", "Vim Bar", 10.0, "piece", 40.0),
        Item(24, "कोलगेट", "ಕೋಲ್ಗೇಟ್", "Colgate", 55.0, "piece", 22.0),
        Item(25, "लाइफबॉय", "ಲೈಫ್‌ಬಾಯ್", "Lifebuoy", 30.0, "piece", 35.0),
        Item(26, "डेटॉल साबुन", "ಡೆಟಾಲ್ ಸೋಪು", "Dettol Soap", 40.0, "piece", 28.0),
        Item(27, "हाजमोला", "ಹಜ್ಮೊಲಾ", "Hajmola", 50.0, "piece", 15.0),
        Item(28, "ब्रेड", "ಬ್ರೆಡ್", "Bread", 40.0, "pkt", 12.0),
        Item(29, "अंडे", "ಮೊಟ್ಟೆ", "Eggs", 84.0, "dozen", 10.0),
        Item(30, "दूध", "ಹಾಲು", "Milk", 27.0, "litre", 24.0),
        Item(31, "दही", "ಮೊಸರು", "Curd", 30.0, "pkt", 15.0),
        Item(32, "पनीर", "ಪನೀರ್", "Paneer", 90.0, "pkt", 8.0),
        Item(33, "टाटा चाय", "ಟಾಟಾ ಚಹಾ", "Tata Tea", 140.0, "pkt", 12.0),
        Item(34, "रेड लेबल चाय", "ರೆಡ್ ಲೇಬಲ್ ಚಹಾ", "Red Label Tea", 150.0, "pkt", 10.0),
        Item(35, "नेस्कैफे कॉफी", "ನೆಸ್ಕೆಫೆ ಕಾಫಿ", "Nescafe Coffee", 190.0, "piece", 8.0),
        Item(36, "बिस्लेरी पानी", "ಬಿಸ್ಲೇರಿ ನೀರು", "Bisleri Water", 20.0, "litre", 24.0),
        Item(37, "कुरकुरे", "ಕುರ್ಕುರೆ", "Kurkure", 20.0, "pkt", 30.0),
        Item(38, "लेज़ चिप्स", "ಲೇಸ್ ಚಿಪ್ಸ್", "Lays Chips", 20.0, "pkt", 30.0),
        Item(39, "हल्दी पाउडर", "ಅರಿಶಿನ ಪುಡಿ", "Haldi Powder", 30.0, "pkt", 20.0),
        Item(40, "मिर्ची पाउडर", "ಮೆಣಸಿನ ಪುಡಿ", "Mirchi Powder", 40.0, "pkt", 18.0),
        Item(41, "धनिया पाउडर", "ಕೊತ್ತಂಬರಿ ಪುಡಿ", "Dhaniya Powder", 35.0, "pkt", 15.0),
        Item(42, "गरम मसाला", "ಗರಂ ಮਸಾಲಾ", "Garam Masala", 60.0, "pkt", 12.0),
        Item(43, "जीरा", "ಜೀರಿಗೆ", "Jeera", 45.0, "pkt", 14.0),
        Item(44, "प्याज", "ಈರುಳ್ಳಿ", "Onion", 35.0, "kg", 30.0),
        Item(45, "आलू", "ಆಲೂಗಡ್ಡೆ", "Potato", 30.0, "kg", 35.0),
        Item(46, "टमाटर", "ಟೊಮೇಟೊ", "Tomato", 40.0, "kg", 20.0),
        Item(47, "अदरक", "ಶುಂಠಿ", "Ginger", 120.0, "kg", 5.0),
        Item(48, "लहसुन", "ಬೆಳ್ಳುಳ್ಳಿ", "Garlic", 160.0, "kg", 5.0),
        Item(49, "अगरबत्ती", "ಅಗರಬತ್ತಿ", "Agarbatti", 25.0, "pkt", 20.0),
        Item(50, "माचिस", "ಬೆಂಕಿಪೊಟ್ಟಣ", "Matchbox", 2.0, "piece", 100.0)
    )
}
