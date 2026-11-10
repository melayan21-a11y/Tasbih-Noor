package com.example.data.model

data class AthkarItem(
    val id: Int,
    val arabicText: String,
    val transliteration: String,
    val englishTranslation: String,
    val targetCount: Int,
    val virtue: String,
    val reference: String
)

enum class AthkarType(val titleArabic: String, val titleEnglish: String) {
    MORNING("أَذْكَارُ الصَّبَاحِ", "Morning Adhkar"),
    EVENING("أَذْكَارُ الْمَسَاءِ", "Evening Adhkar")
}

object AthkarRepository {

    val morningAthkar = listOf(
        AthkarItem(
            id = 101,
            arabicText = "اللَّهُ لَا إِلٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ ۗ مَن ذَا الَّذِي يَشْفَعُ عِندَهُ إِلَّا بِإِذْنِهِ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلَّا بِمَا شَاءَ ۚ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ ۖ وَلَا يَئُودُهُ حِفْظُهُمَا ۚ وَهُوَ الْعَلِيُّ الْعَظِيمُ",
            transliteration = "Allahu la ilaha illa Huwal-Hayyul-Qayyum, la ta'khudhuhu sinatuw-wa la nawm, lahu ma fis-samawati wa ma fil-ard...",
            englishTranslation = "Allah! There is no deity except Him, the Ever-Living, the Sustainer of all existence. Neither drowsiness overtakes Him nor sleep. To Him belongs whatever is in the heavens and whatever is on the earth...",
            targetCount = 1,
            virtue = "Whoever recites this when he rises in the morning will be protected from jinn and evil until the evening.",
            reference = "Ayatul Kursi (Surah Al-Baqarah 2:255) • Al-Hakim (1/562)"
        ),
        AthkarItem(
            id = 102,
            arabicText = "قُلْ هُوَ اللَّهُ أَحَدٌ ۝ اللَّهُ الصَّمَدُ ۝ لَمْ يَلِدْ وَلَمْ يُولَدْ ۝ وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ",
            transliteration = "Qul Huwallahu Ahad, Allahus-Samad, Lam yalid wa lam yoolad, Wa lam yakul-lahu kufuwan ahad.",
            englishTranslation = "Say: He is Allah, the One. Allah, the Eternal Refuge. He neither begets nor is born, nor is there to Him any equivalent.",
            targetCount = 3,
            virtue = "Reciting Al-Ikhlas and the Mu'awwidhat (Falaq & Nas) three times in morning and evening will suffice you against everything.",
            reference = "Surah Al-Ikhlas • Sunan Abi Dawud (5082)"
        ),
        AthkarItem(
            id = 103,
            arabicText = "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ ۝ مِن شَرِّ مَا خَلَقَ ۝ وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ ۝ وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ ۝ وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ",
            transliteration = "Qul a'oodhu bi Rabbil-falaq, Min sharri ma khalaq, Wa min sharri ghasiqin idha waqab, Wa min sharrin-naffathati fil-'uqad, Wa min sharri hasidin idha hasad.",
            englishTranslation = "Say: I seek refuge in the Lord of daybreak, from the evil of that which He created, and from the evil of darkness when it settles, and from the evil of the blowers in knots, and from the evil of an envier when he envies.",
            targetCount = 3,
            virtue = "Shield against jealousy, dark harm, and secret malice.",
            reference = "Surah Al-Falaq • Jami` at-Tirmidhi (3575)"
        ),
        AthkarItem(
            id = 104,
            arabicText = "قُلْ أَعُوذُ بِرَبِّ النَّاسِ ۝ مَلِكِ النَّاسِ ۝ إِلَٰهِ النَّاسِ ۝ مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ ۝ الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ ۝ مِنَ الْجِنَّةِ وَالنَّاسِ",
            transliteration = "Qul a'oodhu bi Rabbin-nas, Malikin-nas, Ilahin-nas, Min sharril-waswasil-khannas, Alladhi yuwaswisu fee sudoorin-nas, Minal-jinnati wan-nas.",
            englishTranslation = "Say: I seek refuge in the Lord of mankind, the Sovereign of mankind, the God of mankind, from the evil of the retreating whisperer who whispers into the breasts of mankind, from among the jinn and mankind.",
            targetCount = 3,
            virtue = "Ultimate protection against inner whispers and anxiety.",
            reference = "Surah An-Nas • Sunan an-Nasa'i (5428)"
        ),
        AthkarItem(
            id = 105,
            arabicText = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ ، وَالْحَمْدُ لِلَّهِ ، لَا إِلٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ ، رَبِّ أَسْأَلُكَ خَيْرَ مَا فِي هٰذَا الْيَوْمِ وَخَيْرَ مَا بَعْدَهُ ، وَأَعُوذُ بِكَ مِنْ شَرِّ مَا فِي هٰذَا الْيَوْمِ وَشَرِّ مَا بَعْدَهُ",
            transliteration = "Asbahna wa asbahal mulku lillahi walhamdu lillah, la ilaha illallahu wahdahu la shareeka lah, lahul mulku wa lahul hamdu wa Huwa 'ala kulli shay'in qadeer. Rabbi as'aluka khayra ma fee hadhal yawmi wa khayra ma ba'dahu...",
            englishTranslation = "We have reached the morning and at this very time unto Allah belongs all sovereignty, and all praise is for Allah. None has the right to be worshipped except Allah alone, without partner...",
            targetCount = 1,
            virtue = "Affirmation of Allah's morning kingdom and seeking protection from laziness and hardships of the day.",
            reference = "Sahih Muslim (2723)"
        ),
        AthkarItem(
            id = 106,
            arabicText = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلٰهَ إِلَّا أَنْتَ ، خَلَقْتَنِي وَأَنَا عَبْدُكَ ، وَأَنَا عَلَىٰ عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ",
            transliteration = "Allahumma Anta Rabbi la ilaha illa Anta, khalaqtani wa ana 'abduka, wa ana 'ala 'ahdika wa wa'dika mastata'tu, a'oodhu bika min sharri ma sana'tu, aboo'u laka bini'matika 'alayya, wa aboo'u bidhanbi faghfir li fa'innahu la yaghfirudh-dhunooba illa Anta.",
            englishTranslation = "O Allah, You are my Lord, none has the right to be worshipped except You. You created me and I am Your servant, and I abide by Your covenant and promise as best I can. I seek refuge with You from the evil of what I have done. I acknowledge Your favor upon me, and I acknowledge my sin, so forgive me, for none forgives sins except You.",
            targetCount = 1,
            virtue = "Sayyidul Istighfar (The Master Supplication for Forgiveness). Whoever says this with conviction in the morning and dies before evening will be of the people of Paradise.",
            reference = "Sahih al-Bukhari (6306)"
        ),
        AthkarItem(
            id = 107,
            arabicText = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
            transliteration = "Bismillahilladhi la yadurru ma'asmihi shay'un fil-ardi wa la fis-sama'i wa Huwas-Sami'ul-'Aleem.",
            englishTranslation = "In the Name of Allah, with Whose Name nothing can cause harm in the earth or in the heavens, and He is the All-Hearing, the All-Knowing.",
            targetCount = 3,
            virtue = "Whoever recites this three times in the morning will not be afflicted by any sudden calamity until evening.",
            reference = "Sunan Abi Dawud (5088) • Jami` at-Tirmidhi (3388)"
        ),
        AthkarItem(
            id = 108,
            arabicText = "رَضِيتُ بِاللَّهِ رَبًّا ، وَبِالْإِسْلَامِ دِينًا ، وَبِمُحَمَّدٍ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ نَبِيًّا",
            transliteration = "Raditu billahi Rabba, wa bil-Islami deena, wa bi Muhammadin sallallahu 'alayhi wa sallama Nabiyya.",
            englishTranslation = "I am pleased with Allah as my Lord, with Islam as my religion, and with Muhammad (peace and blessings be upon him) as my Prophet.",
            targetCount = 3,
            virtue = "Allah has promised that whoever says this three times every morning and evening, He will surely make him pleased on the Day of Resurrection.",
            reference = "Jami` at-Tirmidhi (3389) • Musnad Ahmad (18967)"
        ),
        AthkarItem(
            id = 109,
            arabicText = "يَا حَيُّ يَا قَيُّومُ بِرَحْمَتِكَ أَسْتَغِيثُ ، أَصْلِحْ لِي شَأْنِي كُلَّهُ ، وَلَا تَكِلْنِي إِلَىٰ نَفْسِي طَرْفَةَ عَيْنٍ",
            transliteration = "Ya Hayyu Ya Qayyumu birahmatika astagheeth, aslih li sha'ni kullahu wa la takilni ila nafsi tarfata 'ayn.",
            englishTranslation = "O Ever-Living, O Sustainer, in Your mercy I seek relief. Rectify for me all of my affairs, and do not leave me to myself even for the blink of an eye.",
            targetCount = 1,
            virtue = "Recommended by the Prophet ﷺ to his daughter Fatimah (RA) for complete divine guardianship.",
            reference = "Sunan an-Nasa'i • Al-Hakim (1/545)"
        ),
        AthkarItem(
            id = 110,
            arabicText = "حَسْبِيَ اللَّهُ لَا إِلٰهَ إِلَّا هُوَ ۖ عَلَيْهِ تَوَكَّلْتُ ۖ وَهُوَ رَبُّ الْعَرْشِ الْعَظِيمِ",
            transliteration = "Hasbiyallahu la ilaha illa Huwa, 'alayhi tawakkaltu wa Huwa Rabbul-'Arshil-'Azeem.",
            englishTranslation = "Allah is sufficient for me; there is no deity except Him. Upon Him I have relied, and He is the Lord of the Great Throne.",
            targetCount = 7,
            virtue = "Whoever says this seven times in the morning and seven times in the evening, Allah will suffice him of whatever concerns him of the world and the hereafter.",
            reference = "Sunan Abi Dawud (5081)"
        ),
        AthkarItem(
            id = 111,
            arabicText = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ ، عَدَدَ خَلْقِهِ ، وَرِضَا نَفْسِهِ ، وَزِنَةَ عَرْشِهِ ، وَمِدَادَ كَلِمَاتِهِ",
            transliteration = "SubhanAllahi wa bihamdihi, 'adada khalqihi, wa rida nafsihi, wa zeenata 'arshihi, wa midada kalimatih.",
            englishTranslation = "Glory be to Allah and all praise is for Him, by the number of His creation, by His pleasure, by the weight of His Throne, and by the ink of His words.",
            targetCount = 3,
            virtue = "The Prophet ﷺ taught Juwayriyah (RA) that reciting these words three times weighs more than hours of continuous worship.",
            reference = "Sahih Muslim (2726)"
        ),
        AthkarItem(
            id = 112,
            arabicText = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ",
            transliteration = "SubhanAllahi wa bihamdihi",
            englishTranslation = "Glory be to Allah and His praise",
            targetCount = 100,
            virtue = "Whoever recites this 100 times in the morning and evening, his sins will be forgiven even if they were like the foam of the sea, and none will come on Judgment Day with deeds better than his except someone who said the same or more.",
            reference = "Sahih Muslim (2691) • Sahih al-Bukhari (6405)"
        )
    )

    val eveningAthkar = listOf(
        AthkarItem(
            id = 201,
            arabicText = "اللَّهُ لَا إِلٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ ۗ مَن ذَا الَّذِي يَشْفَعُ عِندَهُ إِلَّا بِإِذْنِهِ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلَّا بِمَا شَاءَ ۚ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ ۖ وَلَا يَئُودُهُ حِفْظُهُمَا ۚ وَهُوَ الْعَلِيُّ الْعَظِيمُ",
            transliteration = "Allahu la ilaha illa Huwal-Hayyul-Qayyum, la ta'khudhuhu sinatuw-wa la nawm...",
            englishTranslation = "Allah! There is no deity except Him, the Ever-Living, the Sustainer of all existence...",
            targetCount = 1,
            virtue = "Whoever recites this in the evening will be protected from all harm until the morning.",
            reference = "Ayatul Kursi (Surah Al-Baqarah 2:255) • Al-Hakim (1/562)"
        ),
        AthkarItem(
            id = 202,
            arabicText = "قُلْ هُوَ اللَّهُ أَحَدٌ ۝ اللَّهُ الصَّمَدُ ۝ لَمْ يَلِدْ وَلَمْ يُولَدْ ۝ وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ",
            transliteration = "Qul Huwallahu Ahad, Allahus-Samad, Lam yalid wa lam yoolad, Wa lam yakul-lahu kufuwan ahad.",
            englishTranslation = "Say: He is Allah, the One. Allah, the Eternal Refuge...",
            targetCount = 3,
            virtue = "Recited three times in the evening to protect from all distress and harm.",
            reference = "Surah Al-Ikhlas • Sunan Abi Dawud (5082)"
        ),
        AthkarItem(
            id = 203,
            arabicText = "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ ۝ مِن شَرِّ مَا خَلَقَ ۝ وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ ۝ وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ ۝ وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ",
            transliteration = "Qul a'oodhu bi Rabbil-falaq, Min sharri ma khalaq...",
            englishTranslation = "Say: I seek refuge in the Lord of daybreak, from the evil of that which He created...",
            targetCount = 3,
            virtue = "Shield against night-time evils, envy, and witchcraft.",
            reference = "Surah Al-Falaq • Jami` at-Tirmidhi (3575)"
        ),
        AthkarItem(
            id = 204,
            arabicText = "قُلْ أَعُوذُ بِرَبِّ النَّاسِ ۝ مَلِكِ النَّاسِ ۝ إِلَٰهِ النَّاسِ ۝ مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ ۝ الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ ۝ مِنَ الْجِنَّةِ وَالنَّاسِ",
            transliteration = "Qul a'oodhu bi Rabbin-nas, Malikin-nas, Ilahin-nas...",
            englishTranslation = "Say: I seek refuge in the Lord of mankind, the Sovereign of mankind...",
            targetCount = 3,
            virtue = "Night shield against whispering doubts and negative thoughts.",
            reference = "Surah An-Nas • Sunan an-Nasa'i (5428)"
        ),
        AthkarItem(
            id = 205,
            arabicText = "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ ، وَالْحَمْدُ لِلَّهِ ، لَا إِلٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ ، رَبِّ أَسْأَلُكَ خَيْرَ مَا فِي هٰذِهِ اللَّيْلَةِ وَخَيْرَ مَا بَعْدَهَا ، وَأَعُوذُ بِكَ مِنْ شَرِّ مَا فِي هٰذِهِ اللَّيْلَةِ وَشَرِّ مَا بَعْدَهَا",
            transliteration = "Amsayna wa amsal mulku lillahi walhamdu lillah, la ilaha illallahu wahdahu la shareeka lah, lahul mulku wa lahul hamdu wa Huwa 'ala kulli shay'in qadeer. Rabbi as'aluka khayra ma fee hadhihil laylati wa khayra ma ba'daha...",
            englishTranslation = "We have reached the evening and at this very time unto Allah belongs all sovereignty, and all praise is for Allah. None has the right to be worshipped except Allah alone, without partner...",
            targetCount = 1,
            virtue = "Evening supplication seeking divine goodness for the coming night.",
            reference = "Sahih Muslim (2723)"
        ),
        AthkarItem(
            id = 206,
            arabicText = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلٰهَ إِلَّا أَنْتَ ، خَلَقْتَنِي وَأَنَا عَبْدُكَ ، وَأَنَا عَلَىٰ عهدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ",
            transliteration = "Allahumma Anta Rabbi la ilaha illa Anta, khalaqtani wa ana 'abduka, wa ana 'ala 'ahdika wa wa'dika mastata'tu...",
            englishTranslation = "O Allah, You are my Lord, none has the right to be worshipped except You. You created me and I am Your servant...",
            targetCount = 1,
            virtue = "Sayyidul Istighfar. Whoever recites it in the evening with conviction and dies during the night will enter Paradise.",
            reference = "Sahih al-Bukhari (6306)"
        ),
        AthkarItem(
            id = 207,
            arabicText = "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ",
            transliteration = "A'oodhu bi kalimatillahit-tammati min sharri ma khalaq.",
            englishTranslation = "I seek refuge in the perfect words of Allah from the evil of that which He has created.",
            targetCount = 3,
            virtue = "Whoever recites this three times in the evening will not be harmed by any poisonous creature or sting during the night.",
            reference = "Sahih Muslim (2709) • Jami` at-Tirmidhi (3604)"
        ),
        AthkarItem(
            id = 208,
            arabicText = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
            transliteration = "Bismillahilladhi la yadurru ma'asmihi shay'un fil-ardi wa la fis-sama'i wa Huwas-Sami'ul-'Aleem.",
            englishTranslation = "In the Name of Allah, with Whose Name nothing can cause harm in the earth or in the heavens, and He is the All-Hearing, the All-Knowing.",
            targetCount = 3,
            virtue = "Protection from any sudden harm throughout the entire night.",
            reference = "Sunan Abi Dawud (5088) • Jami` at-Tirmidhi (3388)"
        ),
        AthkarItem(
            id = 209,
            arabicText = "رَضِيتُ بِاللَّهِ رَبًّا ، وَبِالْإِسْلَامِ دِينًا ، وَبِمُحَمَّدٍ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ نَبِيًّا",
            transliteration = "Raditu billahi Rabba, wa bil-Islami deena, wa bi Muhammadin sallallahu 'alayhi wa sallama Nabiyya.",
            englishTranslation = "I am pleased with Allah as my Lord, with Islam as my religion, and with Muhammad ﷺ as my Prophet.",
            targetCount = 3,
            virtue = "Guarantees Allah's good pleasure on the Day of Resurrection.",
            reference = "Jami` at-Tirmidhi (3389)"
        ),
        AthkarItem(
            id = 210,
            arabicText = "حَسْبِيَ اللَّهُ لَا إِلٰهَ إِلَّا هُوَ ۖ عَلَيْهِ تَوَكَّلْتُ ۖ وَهُوَ رَبُّ الْعَرْشِ الْعَظِيمِ",
            transliteration = "Hasbiyallahu la ilaha illa Huwa, 'alayhi tawakkaltu wa Huwa Rabbul-'Arshil-'Azeem.",
            englishTranslation = "Allah is sufficient for me; there is no deity except Him. Upon Him I have relied, and He is the Lord of the Great Throne.",
            targetCount = 7,
            virtue = "Allah suffices the servant for all his worldly and eternal worries.",
            reference = "Sunan Abi Dawud (5081)"
        ),
        AthkarItem(
            id = 211,
            arabicText = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ",
            transliteration = "SubhanAllahi wa bihamdihi",
            englishTranslation = "Glory be to Allah and His praise",
            targetCount = 100,
            virtue = "Removes all sins, beloved to ar-Rahman, heavy on the scales.",
            reference = "Sahih Muslim (2691)"
        )
    )
}
