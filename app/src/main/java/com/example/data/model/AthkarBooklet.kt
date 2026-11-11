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

    val morningAthkar: List<AthkarItem> = listOf(
        // 1. Ayat al-Kursi
        AthkarItem(
            id = 101,
            arabicText = "اللَّهُ لَا إِلٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ ۗ مَن ذَا الَّذِي يَشْفَعُ عِندَهُ إِلَّا بِإِذْنِهِ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلَّا بِمَا شَاءَ ۚ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ ۖ وَلَا يَئُودُهُ حِفْظُهُمَا ۚ وَهُوَ الْعَلِيُّ الْعَظِيمُ",
            transliteration = "Allahu la ilaha illa Huwal-Hayyul-Qayyum, la ta'khudhuhu sinatuw-wa la nawm, lahu ma fis-samawati wa ma fil-ard, man dhal-ladhee yashfa'u 'indahu illa bi'idhnih, ya'lamu ma bayna aydeehim wa ma khalfahum, wa la yuheetoona bishay'im-min 'ilmihee illa bima sha'a, wasi'a Kursiyyuhus-samawati wal-arda, wa la ya'ooduhu hifzuhuma, wa Huwal-'Aliyyul-'Azeem.",
            englishTranslation = "Allah! There is no deity except Him, the Ever-Living, the Sustainer of all existence. Neither drowsiness overtakes Him nor sleep. To Him belongs whatever is in the heavens and whatever is on the earth. Who is it that can intercede with Him except by His permission? He knows what is before them and what will be after them, and they encompass not a thing of His knowledge except for what He wills. His Kursi extends over the heavens and the earth, and their preservation tires Him not. And He is the Most High, the Most Great.",
            targetCount = 1,
            virtue = "Whoever recites this when he rises in the morning will be shielded from jinn and all harm until the evening.",
            reference = "Ayatul Kursi (Surah Al-Baqarah 2:255) • Al-Hakim (1/562)"
        ),

        // 2. Surah Al-Ikhlas
        AthkarItem(
            id = 102,
            arabicText = "قُلْ هُوَ اللَّهُ أَحَدٌ ۝ اللَّهُ الصَّمَدُ ۝ لَمْ يَلِدْ وَلَمْ يُولَدْ ۝ وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ",
            transliteration = "Qul Huwallahu Ahad, Allahus-Samad, Lam yalid wa lam yoolad, Wa lam yakul-lahu kufuwan ahad.",
            englishTranslation = "Say: He is Allah, [who is] One, Allah, the Eternal Refuge. He neither begets nor is born, nor is there to Him any equivalent.",
            targetCount = 3,
            virtue = "Reciting Surah Al-Ikhlas and the Mu'awwidhat (Falaq & Nas) three times in the morning and evening will suffice you against everything.",
            reference = "Surah Al-Ikhlas (112) • Sunan Abi Dawud (5082) • Jami` at-Tirmidhi (3575)"
        ),

        // 3. Surah Al-Falaq
        AthkarItem(
            id = 103,
            arabicText = "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ ۝ مِن شَرِّ مَا خَلَقَ ۝ وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ ۝ وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ ۝ وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ",
            transliteration = "Qul a'oodhu bi Rabbil-falaq, Min sharri ma khalaq, Wa min sharri ghasiqin idha waqab, Wa min sharrin-naffathati fil-'uqad, Wa min sharri hasidin idha hasad.",
            englishTranslation = "Say: I seek refuge in the Lord of daybreak, from the evil of that which He created, and from the evil of darkness when it settles, and from the evil of the blowers in knots, and from the evil of an envier when he envies.",
            targetCount = 3,
            virtue = "Shield against jealousy, dark malice, and unseen harms.",
            reference = "Surah Al-Falaq (113) • Sunan Abi Dawud (5082) • Jami` at-Tirmidhi (3575)"
        ),

        // 4. Surah An-Nas
        AthkarItem(
            id = 104,
            arabicText = "قُلْ أَعُوذُ بِرَبِّ النَّاسِ ۝ مَلِكِ النَّاسِ ۝ إِلَٰهِ النَّاسِ ۝ مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ ۝ الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ ۝ مِنَ الْجِنَّةِ وَالنَّاسِ",
            transliteration = "Qul a'oodhu bi Rabbin-nas, Malikin-nas, Ilahin-nas, Min sharril-waswasil-khannas, Alladhee yuwaswisu fee sudoorin-nas, Minal-jinnati wan-nas.",
            englishTranslation = "Say: I seek refuge in the Lord of mankind, the Sovereign of mankind, the God of mankind, from the evil of the retreating whisperer who whispers into the breasts of mankind, from among the jinn and mankind.",
            targetCount = 3,
            virtue = "Sanctuary against doubts, anxiety, and demonic whisperings.",
            reference = "Surah An-Nas (114) • Sunan an-Nasa'i (5428)"
        ),

        // 5. Asbahna wa asbahal-mulku lillah
        AthkarItem(
            id = 105,
            arabicText = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ ، وَالْحَمْدُ لِلَّهِ ، لَا إِلٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ ، رَبِّ أَسْأَلُكَ خَيْرَ مَا فِي هٰذَا الْيَوْمِ وَخَيْرَ مَا بَعْدَهُ ، وَأَعُوذُ بِكَ مِنْ شَرِّ مَا فِي هٰذَا الْيَوْمِ وَشَرِّ مَا بَعْدَهُ ، رَبِّ أَعُوذُ بِكَ مِنَ الْكَسَلِ وَسُوءِ الْكِبَرِ ، رَبِّ أَعُوذُ بِكَ مِنْ عَذَابٍ فِي النَّارِ وَعَذَابٍ فِي الْقَبْرِ",
            transliteration = "Asbahna wa asbahal-mulku lillahi wal-hamdu lillah, la ilaha illallahu wahdahu la shareeka lah, lahul-mulku wa lahul-hamdu wa Huwa 'ala kulli shay'in qadeer. Rabbi as'aluka khayra ma fee hadhal-yawmi wa khayra ma ba'dahu, wa a'oodhu bika min sharri ma fee hadhal-yawmi wa sharri ma ba'dahu, Rabbi a'oodhu bika minal-kasali wa soo'il-kibar, Rabbi a'oodhu bika min 'adhabin fin-nari wa 'adhabin fil-qabr.",
            englishTranslation = "We have entered the morning and at this very time unto Allah belongs all sovereignty, and all praise is for Allah. None has the right to be worshipped except Allah alone, without partner. To Him belongs the dominion and all praise, and He has power over all things. My Lord, I ask You for the good of this day and the good of what comes after it, and I seek refuge in You from the evil of this day and the evil of what comes after it. My Lord, I seek refuge in You from laziness and the hardships of old age. My Lord, I seek refuge in You from punishment in the Hellfire and punishment in the grave.",
            targetCount = 1,
            virtue = "Complete morning entrustment of dominion to Allah and seeking refuge from laziness and the grave.",
            reference = "Sahih Muslim (2723)"
        ),

        // 6. Allahumma bika asbahna
        AthkarItem(
            id = 106,
            arabicText = "اللَّهُمَّ بِكَ أَصْبَحْنَا ، وَبِكَ أَمْسَيْنَا ، وَبِكَ نَحْيَا ، وَبِكَ نَمُوتُ ، وَإِلَيْكَ النُّشُورُ",
            transliteration = "Allahumma bika asbahna, wa bika amsayna, wa bika nahya, wa bika namootu, wa ilaykan-nushoor.",
            englishTranslation = "O Allah, by You we have reached the morning, and by You we reach the evening, and by You we live, and by You we die, and unto You is our resurrection.",
            targetCount = 1,
            virtue = "Daily surrender of existence, life, death, and resurrection to Allah alone.",
            reference = "Jami` at-Tirmidhi (3391) • Sunan Abi Dawud (5068)"
        ),

        // 7. Sayyid al-Istighfar
        AthkarItem(
            id = 107,
            arabicText = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلٰهَ إِلَّا أَنْتَ ، خَلَقْتَنِي وَأَنَا عَبْدُكَ ، وَأَنَا عَلَىٰ عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ",
            transliteration = "Allahumma Anta Rabbi la ilaha illa Anta, khalaqtani wa ana 'abduka, wa ana 'ala 'ahdika wa wa'dika mastata'tu, a'oodhu bika min sharri ma sana'tu, aboo'u laka bini'matika 'alayya, wa aboo'u bidhanbi faghfir lee fa'innahu la yaghfirudh-dhunooba illa Anta.",
            englishTranslation = "O Allah, You are my Lord, none has the right to be worshipped except You. You created me and I am Your servant, and I abide by Your covenant and promise as best I can. I seek refuge with You from the evil of what I have done. I acknowledge Your favor upon me, and I acknowledge my sin, so forgive me, for none forgives sins except You.",
            targetCount = 1,
            virtue = "Sayyid al-Istighfar (The Master Supplication for Forgiveness). Whoever says this with conviction in the morning and dies before evening will be among the people of Paradise.",
            reference = "Sahih al-Bukhari (6306)"
        ),

        // 8. Allahumma inni asbahtu ush-hiduka
        AthkarItem(
            id = 108,
            arabicText = "اللَّهُمَّ إِنِّي أَصْبَحْتُ أُشْهِدُكَ ، وَأُشْهِدُ حَمَلَةَ عَرْشِكَ ، وَمَلَائِكَتَكَ ، وَجَمِيعَ خَلْقِكَ ، أَنَّكَ أَنْتَ اللَّهُ لَا إِلٰهَ إِلَّا أَنْتَ وَحْدَكَ لَا شَرِيكَ لَكَ ، وَأَنَّ مُحَمَّدًا عَبْدُكَ وَرَسُولُكَ",
            transliteration = "Allahumma innee asbahtu ush-hiduka wa ush-hidu hamalata 'arshika, wa mala'ikataka wa jamee'a khalqika, annaka Antallahu la ilaha illa Anta wahdaka la shareeka lak, wa anna Muhammadan 'abduka wa Rasooluk.",
            englishTranslation = "O Allah, verily I have reached the morning calling You to witness, and calling the bearers of Your Throne, Your angels, and the entirety of Your creation to witness, that You are Allah, there is no deity worthy of worship except You alone without partner, and that Muhammad is Your servant and Your Messenger.",
            targetCount = 4,
            virtue = "Whoever recites this 4 times in the morning, Allah emancipates him completely from the Fire.",
            reference = "Sunan Abi Dawud (5069)"
        ),

        // 9. Allahumma ma asbaha bee min ni'matin
        AthkarItem(
            id = 109,
            arabicText = "اللَّهُمَّ مَا أَصْبَحَ بِي مِنْ نِعْمَةٍ أَوْ بِأَحَدٍ مِنْ خَلْقِكَ فَمِنْكَ وَحْدَكَ لَا شَرِيكَ لَكَ ، فَلَكَ الْحَمْدُ وَلَكَ الشُّكْرُ",
            transliteration = "Allahumma ma asbaha bee min ni'matin aw bi'ahadin min khalqika faminka wahdaka la shareeka lak, falakal-hamdu wa lakash-shukr.",
            englishTranslation = "O Allah, whatever blessing I or any of Your creation have risen upon this morning, it is from You alone, without partner. To You belongs all praise and to You belongs all gratitude.",
            targetCount = 1,
            virtue = "Whoever says this in the morning has fulfilled the gratitude owed for that entire day.",
            reference = "Sunan Abi Dawud (5073) • Sunan an-Nasa'i"
        ),

        // 10. Allahumma 'afini fee badani
        AthkarItem(
            id = 110,
            arabicText = "اللَّهُمَّ عَافِنِي فِي بَدَنِي ، اللَّهُمَّ عَافِنِي فِي سَمْعِي ، اللَّهُمَّ عَافِنِي فِي بَصَرِي ، لَا إِلٰهَ إِلَّا أَنْتَ . اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْكُفْرِ وَالْفَقْرِ ، وَأَعُوذُ بِكَ مِنْ عَذَابِ الْقَبْرِ ، لَا إِلٰهَ إِلَّا أَنْتَ",
            transliteration = "Allahumma 'afinee fee badanee, Allahumma 'afinee fee sam'ee, Allahumma 'afinee fee basaree, la ilaha illa Anta. Allahumma innee a'oodhu bika minal-kufri wal-faqr, wa a'oodhu bika min 'adhabil-qabr, la ilaha illa Anta.",
            englishTranslation = "O Allah, grant my body health. O Allah, grant my hearing health. O Allah, grant my sight health. None has the right to be worshipped except You. O Allah, I seek refuge in You from disbelief and poverty, and I seek refuge in You from the torment of the grave. None has the right to be worshipped except You.",
            targetCount = 3,
            virtue = "Sunnah supplication recited 3 times morning and evening for physical health, sensory protection, and refuge from poverty and the grave.",
            reference = "Sunan Abi Dawud (5090) • Musnad Ahmad"
        ),

        // 11. Hasbiyallahu la ilaha illa Huwa
        AthkarItem(
            id = 111,
            arabicText = "حَسْبِيَ اللَّهُ لَا إِلٰهَ إِلَّا هُوَ ۖ عَلَيْهِ تَوَكَّلْتُ ۖ وَهُوَ رَبُّ الْعَرْشِ الْعَظِيمِ",
            transliteration = "Hasbiyallahu la ilaha illa Huwa, 'alayhi tawakkaltu wa Huwa Rabbul-'Arshil-'Azeem.",
            englishTranslation = "Allah is sufficient for me; there is no deity except Him. Upon Him I have relied, and He is the Lord of the Great Throne.",
            targetCount = 7,
            virtue = "Whoever recites this 7 times in the morning and 7 times in the evening, Allah will suffice him in all matters that distress him of this world and the Hereafter.",
            reference = "Sunan Abi Dawud (5081)"
        ),

        // 12. Allahumma inni as'alukal-'afwa wal-'afiyah
        AthkarItem(
            id = 112,
            arabicText = "اللَّهُمَّ إِنِّي أَسْأَلُكَ الْعَفْوَ وَالْعَافِيَةَ فِي الدُّنْيَا وَالْآخِرَةِ ، اللَّهُمَّ إِنِّي أَسْأَلُكَ الْعَفْوَ وَالْعَافِيَةَ فِي دِينِي وَدُنْيَايَ وَأَهْلِي وَمَالِي ، اللَّهُمَّ اسْتُرْ عَوْرَاتِي ، وَآمِنْ رَوْعَاتِي ، اللَّهُمَّ احْفَظْنِي مِنْ بَيْنِ يَدَيَّ ، وَمِنْ خَلْفِي ، وَعَنْ يَمِينِي ، وَعَنْ شِمَالِي ، وَمِنْ فَوْقِي ، وَأَعُوذُ بِعَظَمَتِكَ أَنْ أُغْتَالَ مِنْ تَحْتِي",
            transliteration = "Allahumma innee as'alukal-'afwa wal-'afiyata fid-dunya wal-akhirah. Allahumma innee as'alukal-'afwa wal-'afiyata fee deenee wa dunyaya wa ahlee wa malee. Allahummastur 'awratee, wa amin raw'atee. Allahummahfaznee min bayni yadayya, wa min khalfee, wa 'an yameenee, wa 'an shimalee, wa min fawqee, wa a'oodhu bi'azamatika an ughtala min tahtee.",
            englishTranslation = "O Allah, I ask You for pardon and well-being in this world and the Hereafter. O Allah, I ask You for pardon and well-being in my religious commitment, my worldly affairs, my family, and my wealth. O Allah, conceal my flaws and secure me from my anxieties. O Allah, protect me from in front of me, from behind me, from my right, from my left, and from above me, and I seek refuge in Your Greatness from being swallowed up from beneath me.",
            targetCount = 1,
            virtue = "The Prophet ﷺ never omitted this comprehensive 360-degree shield in the morning and evening.",
            reference = "Sunan Abi Dawud (5074) • Sunan Ibn Majah (3871)"
        ),

        // 13. Allahumma 'alimal-ghaybi wash-shahadah
        AthkarItem(
            id = 113,
            arabicText = "اللَّهُمَّ عَالِمَ الْغَيْبِ وَالشَّهَادَةِ ، فَاطِرَ السَّمَاوَاتِ وَالْأَرْضِ ، رَبَّ كُلِّ شَيْءٍ وَمَلِيكَهُ ، أَشْهَدُ أَنْ لَا إِلٰهَ إِلَّا أَنْتَ ، أَعُوذُ بِكَ مِنْ شَرِّ نَفْسِي ، وَمِنْ شَرِّ الشَّيْطَانِ وَشِرْكِهِ ، وَأَنْ أَقْتَرِفَ عَلَىٰ نَفْسِي سُوءًا أَوْ أَجُرَّهُ إِلَىٰ مُسْلِمٍ",
            transliteration = "Allahumma 'Alimal-ghaybi wash-shahadati Fatiras-samawati wal-ardi, Rabba kulli shay'in wa Maleekahu, ash-hadu an la ilaha illa Anta, a'oodhu bika min sharri nafsee, wa min sharrish-shaytani wa shirkihi, wa an aqtarifa 'ala nafsee soo'an aw ajurrahu ila Muslim.",
            englishTranslation = "O Allah, Knower of the unseen and the seen, Creator of the heavens and the earth, Lord and Sovereign of all things, I testify that none has the right to be worshipped except You. I seek refuge in You from the evil of my own soul, and from the evil of Satan and his polytheism, and from committing evil against myself or bringing it upon any Muslim.",
            targetCount = 1,
            virtue = "Taught by the Prophet ﷺ to Abu Bakr As-Siddiq (RA) to recite every morning and evening.",
            reference = "Jami` at-Tirmidhi (3392) • Sunan Abi Dawud (5067)"
        ),

        // 14. Bismillahil-ladhee la yadurru ma'asmih
        AthkarItem(
            id = 114,
            arabicText = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
            transliteration = "Bismillahil-ladhee la yadurru ma'as-mihee shay'un fil-ardi wa la fis-sama'i wa Huwas-Samee'ul-'Aleem.",
            englishTranslation = "In the Name of Allah, with Whose Name nothing can cause harm on earth or in the heavens, and He is the All-Hearing, the All-Knowing.",
            targetCount = 3,
            virtue = "Whoever recites this 3 times in the morning will not be afflicted by any unexpected harm until evening.",
            reference = "Sunan Abi Dawud (5088) • Jami` at-Tirmidhi (3388)"
        ),

        // 15. Raditu billahi Rabba
        AthkarItem(
            id = 115,
            arabicText = "رَضِيتُ بِاللَّهِ رَبًّا ، وَبِالْإِسْلَامِ دِينًا ، وَبِمُحَمَّدٍ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ نَبِيًّا",
            transliteration = "Raditu billahi Rabba, wa bil-Islami deena, wa bi-Muhammadin sallallahu 'alayhi wa sallama Nabiyya.",
            englishTranslation = "I am pleased with Allah as my Lord, with Islam as my religion, and with Muhammad (peace and blessings of Allah be upon him) as my Prophet.",
            targetCount = 3,
            virtue = "Allah has pledged that whoever repeats this 3 times every morning and evening, He will surely make him pleased on the Day of Resurrection.",
            reference = "Musnad Ahmad (18967) • Jami` at-Tirmidhi (3389)"
        ),

        // 16. Ya Hayyu Ya Qayyum
        AthkarItem(
            id = 116,
            arabicText = "يَا حَيُّ يَا قَيُّومُ بِرَحْمَتِكَ أَسْتَغِيثُ ، أَصْلِحْ لِي شَأْنِي كُلَّهُ ، وَلَا تَكِلْنِي إِلَىٰ نَفْسِي طَرْفَةَ عَيْنٍ",
            transliteration = "Ya Hayyu Ya Qayyumu birahmatika astagheeth, aslih lee sha'nee kullahu wa la takilnee ila nafsee tarfata 'ayn.",
            englishTranslation = "O Ever-Living, O Sustainer, in Your mercy I seek relief. Rectify for me all of my affairs, and do not leave me to myself even for the blink of an eye.",
            targetCount = 1,
            virtue = "The Prophet ﷺ enjoined this upon his beloved daughter Fatimah (RA) to never omit in the morning and evening.",
            reference = "Sunan an-Nasa'i • Al-Hakim (1/545)"
        ),

        // 17. Asbahna 'ala fitratil-Islam
        AthkarItem(
            id = 117,
            arabicText = "أَصْبَحْنَا عَلَىٰ فِطْرَةِ الْإِسْلَامِ ، وَعَلَىٰ كَلِمَةِ الْإِخْلَاصِ ، وَعَلَىٰ دِينِ نَبِيِّنَا مُحَمَّدٍ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ ، وَعَلَىٰ مِلَّةِ أَبِينَا إِبْرَاهِيمَ حَنِيفًا مُسْلِمًا وَمَا كَانَ مِنَ الْمُشْرِكِينَ",
            transliteration = "Asbahna 'ala fitratil-Islam, wa 'ala kalimatil-ikhlas, wa 'ala deeni Nabiyyina Muhammadin sallallahu 'alayhi wa sallam, wa 'ala millati abeena Ibraheema haneefan Musliman wa ma kana minal-mushrikeen.",
            englishTranslation = "We have reached the morning upon the natural disposition of Islam, upon the word of sincere faith, upon the religion of our Prophet Muhammad (peace and blessings be upon him), and upon the creed of our father Abraham, who was upright and devoted to Allah, and was not of the polytheists.",
            targetCount = 1,
            virtue = "Daily renewal of pure monotheism and loyalty to the Abrahamic and Prophetic legacy.",
            reference = "Musnad Ahmad (15367) • Sunan ad-Darimi (2688)"
        ),

        // 18. SubhanAllahi wa bihamdihi (100x)
        AthkarItem(
            id = 118,
            arabicText = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ",
            transliteration = "SubhanAllahi wa bihamdih.",
            englishTranslation = "Glory be to Allah and His praise.",
            targetCount = 100,
            virtue = "Whoever recites this 100 times in the morning and evening, his sins will be forgiven even if they were like the foam of the sea, and none will bring better deeds on the Day of Resurrection.",
            reference = "Sahih Muslim (2691) • Sahih al-Bukhari (6405)"
        ),

        // 19. La ilaha illallahu wahdahu la shareeka lah (100x)
        AthkarItem(
            id = 119,
            arabicText = "لَا إِلٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ ، وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ",
            transliteration = "La ilaha illallahu wahdahu la shareeka lah, lahul-mulku wa lahul-hamdu wa Huwa 'ala kulli shay'in qadeer.",
            englishTranslation = "None has the right to be worshipped except Allah alone, without partner. To Him belongs the dominion, to Him belongs all praise, and He has power over all things.",
            targetCount = 100,
            virtue = "Equivalent to freeing ten slaves, recorded 100 good deeds, erased 100 bad deeds, and serves as an impenetrable shield against Satan for the entire day.",
            reference = "Sahih al-Bukhari (3293) • Sahih Muslim (2691)"
        ),

        // 20. SubhanAllahi wa bihamdihi: 'Adada khalqihi (3x)
        AthkarItem(
            id = 120,
            arabicText = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ ، عَدَدَ خَلْقِهِ ، وَرِضَا نَفْسِهِ ، وَزِنَةَ عَرْشِهِ ، وَمِدَادَ كَلِمَاتِهِ",
            transliteration = "SubhanAllahi wa bihamdihi, 'adada khalqihi, wa rida nafsihi, wa zeenata 'arshihi, wa midada kalimatih.",
            englishTranslation = "Glory be to Allah and all praise is for Him, by the number of His creation, by His good pleasure, by the weight of His Throne, and by the ink of His words.",
            targetCount = 3,
            virtue = "The Prophet ﷺ taught Juwayriyah (RA) that reciting these words 3 times outweighs hours of continuous prayer and remembrance.",
            reference = "Sahih Muslim (2726)"
        ),

        // 21. Allahumma inni as'aluka 'ilman nafi'a
        AthkarItem(
            id = 121,
            arabicText = "اللَّهُمَّ إِنِّي أَسْأَلُكَ عِلْمًا نَافِعًا ، وَرِزْقًا طَيِّبًا ، وَعَمَلًا مُتَقَبَّلًا",
            transliteration = "Allahumma innee as'aluka 'ilman nafi'a, wa rizqan tayyiba, wa 'amalan mutaqabbala.",
            englishTranslation = "O Allah, I ask You for beneficial knowledge, wholesome sustenance, and accepted deeds.",
            targetCount = 1,
            virtue = "Prophetic morning prayer made after Fajr seeking wisdom, pure halal livelihood, and divine acceptance.",
            reference = "Sunan Ibn Majah (925) • Musnad Ahmad"
        ),

        // 22. Astaghfirullaha wa atoobu ilayh (100x)
        AthkarItem(
            id = 122,
            arabicText = "أَسْتَغْفِرُ اللَّهَ وَأَتُوبُ إِلَيْهِ",
            transliteration = "Astaghfirullaha wa atoobu ilayh.",
            englishTranslation = "I seek the forgiveness of Allah and repent to Him.",
            targetCount = 100,
            virtue = "The Prophet ﷺ said: 'By Allah, I seek Allah's forgiveness and repent to Him more than seventy (and in narration, 100) times a day.'",
            reference = "Sahih al-Bukhari (6307) • Sahih Muslim (2702)"
        )
    )

    val eveningAthkar: List<AthkarItem> = listOf(
        // 1. Ayat al-Kursi
        AthkarItem(
            id = 201,
            arabicText = "اللَّهُ لَا إِلٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ ۗ مَن ذَا الَّذِي يَشْفَعُ عِندَهُ إِلَّا بِإِذْنِهِ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلَّا بِمَا شَاءَ ۚ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ ۖ وَلَا يَئُودُهُ حِفْظُهُمَا ۚ وَهُوَ الْعَلِيُّ الْعَظِيمُ",
            transliteration = "Allahu la ilaha illa Huwal-Hayyul-Qayyum, la ta'khudhuhu sinatuw-wa la nawm, lahu ma fis-samawati wa ma fil-ard, man dhal-ladhee yashfa'u 'indahu illa bi'idhnih, ya'lamu ma bayna aydeehim wa ma khalfahum, wa la yuheetoona bishay'im-min 'ilmihee illa bima sha'a, wasi'a Kursiyyuhus-samawati wal-arda, wa la ya'ooduhu hifzuhuma, wa Huwal-'Aliyyul-'Azeem.",
            englishTranslation = "Allah! There is no deity except Him, the Ever-Living, the Sustainer of all existence. Neither drowsiness overtakes Him nor sleep. To Him belongs whatever is in the heavens and whatever is on the earth. Who is it that can intercede with Him except by His permission? He knows what is before them and what will be after them, and they encompass not a thing of His knowledge except for what He wills. His Kursi extends over the heavens and the earth, and their preservation tires Him not. And He is the Most High, the Most Great.",
            targetCount = 1,
            virtue = "Whoever recites this when entering the evening will be protected from all evil spirits and harm until morning.",
            reference = "Ayatul Kursi (Surah Al-Baqarah 2:255) • Al-Hakim (1/562)"
        ),

        // 2. Surah Al-Ikhlas
        AthkarItem(
            id = 202,
            arabicText = "قُلْ هُوَ اللَّهُ أَحَدٌ ۝ اللَّهُ الصَّمَدُ ۝ لَمْ يَلِدْ وَلَمْ يُولَدْ ۝ وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ",
            transliteration = "Qul Huwallahu Ahad, Allahus-Samad, Lam yalid wa lam yoolad, Wa lam yakul-lahu kufuwan ahad.",
            englishTranslation = "Say: He is Allah, [who is] One, Allah, the Eternal Refuge. He neither begets nor is born, nor is there to Him any equivalent.",
            targetCount = 3,
            virtue = "Recited 3 times in the evening along with Falaq and Nas to suffice against any distress through the night.",
            reference = "Surah Al-Ikhlas (112) • Sunan Abi Dawud (5082) • Jami` at-Tirmidhi (3575)"
        ),

        // 3. Surah Al-Falaq
        AthkarItem(
            id = 203,
            arabicText = "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ ۝ مِن شَرِّ مَا خَلَقَ ۝ وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ ۝ وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ ۝ وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ",
            transliteration = "Qul a'oodhu bi Rabbil-falaq, Min sharri ma khalaq, Wa min sharri ghasiqin idha waqab, Wa min sharrin-naffathati fil-'uqad, Wa min sharri hasidin idha hasad.",
            englishTranslation = "Say: I seek refuge in the Lord of daybreak, from the evil of that which He created, and from the evil of darkness when it settles, and from the evil of the blowers in knots, and from the evil of an envier when he envies.",
            targetCount = 3,
            virtue = "Protection during the night against unseen evil, envy, and black magic.",
            reference = "Surah Al-Falaq (113) • Sunan Abi Dawud (5082) • Jami` at-Tirmidhi (3575)"
        ),

        // 4. Surah An-Nas
        AthkarItem(
            id = 204,
            arabicText = "قُلْ أَعُوذُ بِرَبِّ النَّاسِ ۝ مَلِكِ النَّاسِ ۝ إِلَٰهِ النَّاسِ ۝ مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ ۝ الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ ۝ مِنَ الْجِنَّةِ وَالنَّاسِ",
            transliteration = "Qul a'oodhu bi Rabbin-nas, Malikin-nas, Ilahin-nas, Min sharril-waswasil-khannas, Alladhee yuwaswisu fee sudoorin-nas, Minal-jinnati wan-nas.",
            englishTranslation = "Say: I seek refuge in the Lord of mankind, the Sovereign of mankind, the God of mankind, from the evil of the retreating whisperer who whispers into the breasts of mankind, from among the jinn and mankind.",
            targetCount = 3,
            virtue = "Shield against bedtime anxiety, bad dreams, and whispers of the self and devils.",
            reference = "Surah An-Nas (114) • Sunan an-Nasa'i (5428)"
        ),

        // 5. Amsayna wa amsal-mulku lillah
        AthkarItem(
            id = 205,
            arabicText = "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ ، وَالْحَمْدُ لِلَّهِ ، لَا إِلٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ ، رَبِّ أَسْأَلُكَ خَيْرَ مَا فِي هٰذِهِ اللَّيْلَةِ وَخَيْرَ مَا بَعْدَهَا ، وَأَعُوذُ بِكَ مِنْ شَرِّ مَا فِي هٰذِهِ اللَّيْلَةِ وَشَرِّ مَا بَعْدَهَا ، رَبِّ أَعُوذُ بِكَ مِنَ الْكَسَلِ وَسُوءِ الْكِبَرِ ، رَبِّ أَعُوذُ بِكَ مِنْ عَذَابٍ فِي النَّارِ وَعَذَابٍ فِي الْقَبْرِ",
            transliteration = "Amsayna wa amsal-mulku lillahi wal-hamdu lillah, la ilaha illallahu wahdahu la shareeka lah, lahul-mulku wa lahul-hamdu wa Huwa 'ala kulli shay'in qadeer. Rabbi as'aluka khayra ma fee hadhihil-laylati wa khayra ma ba'daha, wa a'oodhu bika min sharri ma fee hadhihil-laylati wa sharri ma ba'daha, Rabbi a'oodhu bika minal-kasali wa soo'il-kibar, Rabbi a'oodhu bika min 'adhabin fin-nari wa 'adhabin fil-qabr.",
            englishTranslation = "We have reached the evening and at this very time unto Allah belongs all sovereignty, and all praise is for Allah. None has the right to be worshipped except Allah alone, without partner. To Him belongs the dominion and all praise, and He has power over all things. My Lord, I ask You for the good of this night and the good of what comes after it, and I seek refuge in You from the evil of this night and the evil of what comes after it. My Lord, I seek refuge in You from laziness and the hardships of old age. My Lord, I seek refuge in You from punishment in the Hellfire and punishment in the grave.",
            targetCount = 1,
            virtue = "Evening entrustment of dominion to Allah and seeking divine refuge through the oncoming darkness.",
            reference = "Sahih Muslim (2723)"
        ),

        // 6. Allahumma bika amsayna
        AthkarItem(
            id = 206,
            arabicText = "اللَّهُمَّ بِكَ أَمْسَيْنَا ، وَبِكَ أَصْبَحْنَا ، وَبِكَ نَحْيَا ، وَبِكَ نَمُوتُ ، وَإِلَيْكَ الْمَصِيرُ",
            transliteration = "Allahumma bika amsayna, wa bika asbahna, wa bika nahya, wa bika namootu, wa ilaykal-maseer.",
            englishTranslation = "O Allah, by You we have reached the evening, and by You we reach the morning, and by You we live, and by You we die, and unto You is our final destination.",
            targetCount = 1,
            virtue = "Entrusting our evening, our life, our sleep, and our final return to Allah.",
            reference = "Jami` at-Tirmidhi (3391) • Sunan Abi Dawud (5068)"
        ),

        // 7. Sayyid al-Istighfar
        AthkarItem(
            id = 207,
            arabicText = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلٰهَ إِلَّا أَنْتَ ، خَلَقْتَنِي وَأَنَا عَبْدُكَ ، وَأَنَا عَلَىٰ عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ",
            transliteration = "Allahumma Anta Rabbi la ilaha illa Anta, khalaqtani wa ana 'abduka, wa ana 'ala 'ahdika wa wa'dika mastata'tu, a'oodhu bika min sharri ma sana'tu, aboo'u laka bini'matika 'alayya, wa aboo'u bidhanbi faghfir lee fa'innahu la yaghfirudh-dhunooba illa Anta.",
            englishTranslation = "O Allah, You are my Lord, none has the right to be worshipped except You. You created me and I am Your servant, and I abide by Your covenant and promise as best I can. I seek refuge with You from the evil of what I have done. I acknowledge Your favor upon me, and I acknowledge my sin, so forgive me, for none forgives sins except You.",
            targetCount = 1,
            virtue = "Sayyid al-Istighfar. Whoever says this in the evening with firm faith and dies during the night will enter Paradise.",
            reference = "Sahih al-Bukhari (6306)"
        ),

        // 8. Allahumma inni amsaytu ush-hiduka
        AthkarItem(
            id = 208,
            arabicText = "اللَّهُمَّ إِنِّي أَمْسَيْتُ أُشْهِدُكَ ، وَأُشْهِدُ حَمَلَةَ عَرْشِكَ ، وَمَلَائِكَتَكَ ، وَجَمِيعَ خَلْقِكَ ، أَنَّكَ أَنْتَ اللَّهُ لَا إِلٰهَ إِلَّا أَنْتَ وَحْدَكَ لَا شَرِيكَ لَكَ ، وَأَنَّ مُحَمَّدًا عَبْدُكَ وَرَسُولُكَ",
            transliteration = "Allahumma innee amsaytu ush-hiduka wa ush-hidu hamalata 'arshika, wa mala'ikataka wa jamee'a khalqika, annaka Antallahu la ilaha illa Anta wahdaka la shareeka lak, wa anna Muhammadan 'abduka wa Rasooluk.",
            englishTranslation = "O Allah, verily I have reached the evening calling You to witness, and calling the bearers of Your Throne, Your angels, and the entirety of Your creation to witness, that You are Allah, there is no deity worthy of worship except You alone without partner, and that Muhammad is Your servant and Your Messenger.",
            targetCount = 4,
            virtue = "Whoever recites this 4 times in the evening, Allah frees him completely from the Fire.",
            reference = "Sunan Abi Dawud (5069)"
        ),

        // 9. Allahumma ma amsa bee min ni'matin
        AthkarItem(
            id = 209,
            arabicText = "اللَّهُمَّ مَا أَمْسَى بِي مِنْ نِعْمَةٍ أَوْ بِأَحَدٍ مِنْ خَلْقِكَ فَمِنْكَ وَحْدَكَ لَا شَرِيكَ لَكَ ، فَلَكَ الْحَمْدُ وَلَكَ الشُّكْرُ",
            transliteration = "Allahumma ma amsa bee min ni'matin aw bi'ahadin min khalqika faminka wahdaka la shareeka lak, falakal-hamdu wa lakash-shukr.",
            englishTranslation = "O Allah, whatever blessing I or any of Your creation have reached this evening, it is from You alone, without partner. To You belongs all praise and to You belongs all gratitude.",
            targetCount = 1,
            virtue = "Whoever says this in the evening has fulfilled the gratitude owed for that night.",
            reference = "Sunan Abi Dawud (5073)"
        ),

        // 10. Allahumma 'afini fee badani
        AthkarItem(
            id = 210,
            arabicText = "اللَّهُمَّ عَافِنِي فِي بَدَنِي ، اللَّهُمَّ عَافِنِي فِي سَمْعِي ، اللَّهُمَّ عَافِنِي فِي بَصَرِي ، لَا إِلٰهَ إِلَّا أَنْتَ . اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْكُفْرِ وَالْفَقْرِ ، وَأَعُوذُ بِكَ مِنْ عَذَابِ الْقَبْرِ ، لَا إِلٰهَ إِلَّا أَنْتَ",
            transliteration = "Allahumma 'afinee fee badanee, Allahumma 'afinee fee sam'ee, Allahumma 'afinee fee basaree, la ilaha illa Anta. Allahumma innee a'oodhu bika minal-kufri wal-faqr, wa a'oodhu bika min 'adhabil-qabr, la ilaha illa Anta.",
            englishTranslation = "O Allah, grant my body health. O Allah, grant my hearing health. O Allah, grant my sight health. None has the right to be worshipped except You. O Allah, I seek refuge in You from disbelief and poverty, and I seek refuge in You from the torment of the grave. None has the right to be worshipped except You.",
            targetCount = 3,
            virtue = "Sunnah supplication recited 3 times morning and evening for physical health, sensory protection, and refuge from poverty and the grave.",
            reference = "Sunan Abi Dawud (5090) • Musnad Ahmad"
        ),

        // 11. Hasbiyallahu la ilaha illa Huwa
        AthkarItem(
            id = 211,
            arabicText = "حَسْبِيَ اللَّهُ لَا إِلٰهَ إِلَّا هُوَ ۖ عَلَيْهِ تَوَكَّلْتُ ۖ وَهُوَ رَبُّ الْعَرْشِ الْعَظِيمِ",
            transliteration = "Hasbiyallahu la ilaha illa Huwa, 'alayhi tawakkaltu wa Huwa Rabbul-'Arshil-'Azeem.",
            englishTranslation = "Allah is sufficient for me; there is no deity except Him. Upon Him I have relied, and He is the Lord of the Great Throne.",
            targetCount = 7,
            virtue = "Whoever recites this 7 times in the evening, Allah will suffice him for all that concerns him in this life and the next.",
            reference = "Sunan Abi Dawud (5081)"
        ),

        // 12. Allahumma inni as'alukal-'afwa wal-'afiyah
        AthkarItem(
            id = 212,
            arabicText = "اللَّهُمَّ إِنِّي أَسْأَلُكَ الْعَفْوَ وَالْعَافِيَةَ فِي الدُّنْيَا وَالْآخِرَةِ ، اللَّهُمَّ إِنِّي أَسْأَلُكَ الْعَفْوَ وَالْعَافِيَةَ فِي دِينِي وَدُنْيَايَ وَأَهْلِي وَمَالِي ، اللَّهُمَّ اسْتُرْ عَوْرَاتِي ، وَآمِنْ رَوْعَاتِي ، اللَّهُمَّ احْفَظْنِي مِنْ بَيْنِ يَدَيَّ ، وَمِنْ خَلْفِي ، وَعَنْ يَمِينِي ، وَعَنْ شِمَالِي ، وَمِنْ فَوْقِي ، وَأَعُوذُ بِعَظَمَتِكَ أَنْ أُغْتَالَ مِنْ تَحْتِي",
            transliteration = "Allahumma innee as'alukal-'afwa wal-'afiyata fid-dunya wal-akhirah. Allahumma innee as'alukal-'afwa wal-'afiyata fee deenee wa dunyaya wa ahlee wa malee. Allahummastur 'awratee, wa amin raw'atee. Allahummahfaznee min bayni yadayya, wa min khalfee, wa 'an yameenee, wa 'an shimalee, wa min fawqee, wa a'oodhu bi'azamatika an ughtala min tahtee.",
            englishTranslation = "O Allah, I ask You for pardon and well-being in this world and the Hereafter. O Allah, I ask You for pardon and well-being in my religious commitment, my worldly affairs, my family, and my wealth. O Allah, conceal my flaws and secure me from my anxieties. O Allah, protect me from in front of me, from behind me, from my right, from my left, and from above me, and I seek refuge in Your Greatness from being swallowed up from beneath me.",
            targetCount = 1,
            virtue = "The Prophet ﷺ never omitted this comprehensive 360-degree shield in the morning and evening.",
            reference = "Sunan Abi Dawud (5074) • Sunan Ibn Majah (3871)"
        ),

        // 13. Allahumma 'alimal-ghaybi wash-shahadah
        AthkarItem(
            id = 213,
            arabicText = "اللَّهُمَّ عَالِمَ الْغَيْبِ وَالشَّهَادَةِ ، فَاطِرَ السَّمَاوَاتِ وَالْأَرْضِ ، رَبَّ كُلِّ شَيْءٍ وَمَلِيكَهُ ، أَشْهَدُ أَنْ لَا إِلٰهَ إِلَّا أَنْتَ ، أَعُوذُ بِكَ مِنْ شَرِّ نَفْسِي ، وَمِنْ شَرِّ الشَّيْطَانِ وَشِرْكِهِ ، وَأَنْ أَقْتَرِفَ عَلَىٰ نَفْسِي سُوءًا أَوْ أَجُرَّهُ إِلَىٰ مُسْلِمٍ",
            transliteration = "Allahumma 'Alimal-ghaybi wash-shahadati Fatiras-samawati wal-ardi, Rabba kulli shay'in wa Maleekahu, ash-hadu an la ilaha illa Anta, a'oodhu bika min sharri nafsee, wa min sharrish-shaytani wa shirkihi, wa an aqtarifa 'ala nafsee soo'an aw ajurrahu ila Muslim.",
            englishTranslation = "O Allah, Knower of the unseen and the seen, Creator of the heavens and the earth, Lord and Sovereign of all things, I testify that none has the right to be worshipped except You. I seek refuge in You from the evil of my own soul, and from the evil of Satan and his polytheism, and from committing evil against myself or bringing it upon any Muslim.",
            targetCount = 1,
            virtue = "Supplication taught by the Prophet ﷺ to Abu Bakr (RA) to protect heart, mind, and deeds day and night.",
            reference = "Jami` at-Tirmidhi (3392) • Sunan Abi Dawud (5067)"
        ),

        // 14. Bismillahil-ladhee la yadurru ma'asmih
        AthkarItem(
            id = 214,
            arabicText = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
            transliteration = "Bismillahil-ladhee la yadurru ma'as-mihee shay'un fil-ardi wa la fis-sama'i wa Huwas-Samee'ul-'Aleem.",
            englishTranslation = "In the Name of Allah, with Whose Name nothing can cause harm on earth or in the heavens, and He is the All-Hearing, the All-Knowing.",
            targetCount = 3,
            virtue = "Whoever recites this 3 times in the evening will not be harmed by any sudden calamity throughout the entire night.",
            reference = "Sunan Abi Dawud (5088) • Jami` at-Tirmidhi (3388)"
        ),

        // 15. Raditu billahi Rabba
        AthkarItem(
            id = 215,
            arabicText = "رَضِيتُ بِاللَّهِ رَبًّا ، وَبِالْإِسْلَامِ دِينًا ، وَبِمُحَمَّدٍ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ نَبِيًّا",
            transliteration = "Raditu billahi Rabba, wa bil-Islami deena, wa bi-Muhammadin sallallahu 'alayhi wa sallama Nabiyya.",
            englishTranslation = "I am pleased with Allah as my Lord, with Islam as my religion, and with Muhammad (peace and blessings of Allah be upon him) as my Prophet.",
            targetCount = 3,
            virtue = "Guarantees Allah's pleasure for the servant on the Day of Resurrection.",
            reference = "Musnad Ahmad (18967) • Jami` at-Tirmidhi (3389)"
        ),

        // 16. Ya Hayyu Ya Qayyum
        AthkarItem(
            id = 216,
            arabicText = "يَا حَيُّ يَا قَيُّومُ بِرَحْمَتِكَ أَسْتَغِيثُ ، أَصْلِحْ لِي شَأْنِي كُلَّهُ ، وَلَا تَكِلْنِي إِلَىٰ نَفْسِي طَرْفَةَ عَيْنٍ",
            transliteration = "Ya Hayyu Ya Qayyumu birahmatika astagheeth, aslih lee sha'nee kullahu wa la takilnee ila nafsee tarfata 'ayn.",
            englishTranslation = "O Ever-Living, O Sustainer, in Your mercy I seek relief. Rectify for me all of my affairs, and do not leave me to myself even for the blink of an eye.",
            targetCount = 1,
            virtue = "Seeking divine mercy, rectification of all affairs, and preservation from self-reliance.",
            reference = "Sunan an-Nasa'i • Al-Hakim (1/545)"
        ),

        // 17. Amsayna 'ala fitratil-Islam
        AthkarItem(
            id = 217,
            arabicText = "أَمْسَيْنَا عَلَىٰ فِطْرَةِ الْإِسْلَامِ ، وَعَلَىٰ كَلِمَةِ الْإِخْلَاصِ ، وَعَلَىٰ دِينِ نَبِيِّنَا مُحَمَّدٍ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ ، وَعَلَىٰ مِلَّةِ أَبِينَا إِبْرَاهِيمَ حَنِيفًا مُسْلِمًا وَمَا كَانَ مِنَ الْمُشْرِكِينَ",
            transliteration = "Amsayna 'ala fitratil-Islam, wa 'ala kalimatil-ikhlas, wa 'ala deeni Nabiyyina Muhammadin sallallahu 'alayhi wa sallam, wa 'ala millati abeena Ibraheema haneefan Musliman wa ma kana minal-mushrikeen.",
            englishTranslation = "We have reached the evening upon the natural disposition of Islam, upon the word of sincere faith, upon the religion of our Prophet Muhammad (peace and blessings be upon him), and upon the creed of our father Abraham, who was upright and devoted to Allah, and was not of the polytheists.",
            targetCount = 1,
            virtue = "Evening affirmation of pure monotheism and unbroken adherence to the Prophetic and Abrahamic path.",
            reference = "Musnad Ahmad (15367) • Sunan ad-Darimi (2688)"
        ),

        // 18. SubhanAllahi wa bihamdihi (100x)
        AthkarItem(
            id = 218,
            arabicText = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ",
            transliteration = "SubhanAllahi wa bihamdih.",
            englishTranslation = "Glory be to Allah and His praise.",
            targetCount = 100,
            virtue = "Whoever recites this 100 times in the evening, all his sins are forgiven even if they equal the foam of the ocean.",
            reference = "Sahih Muslim (2691) • Sahih al-Bukhari (6405)"
        ),

        // 19. La ilaha illallahu wahdahu la shareeka lah (100x)
        AthkarItem(
            id = 219,
            arabicText = "لَا إِلٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ ، وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ",
            transliteration = "La ilaha illallahu wahdahu la shareeka lah, lahul-mulku wa lahul-hamdu wa Huwa 'ala kulli shay'in qadeer.",
            englishTranslation = "None has the right to be worshipped except Allah alone, without partner. To Him belongs the dominion, to Him belongs all praise, and He has power over all things.",
            targetCount = 100,
            virtue = "Sanctuary against Satan, equal to freeing 10 captives, giving 100 rewards and wiping 100 sins.",
            reference = "Sahih al-Bukhari (3293) • Sahih Muslim (2691)"
        ),

        // 20. A'oodhu bi kalimat-illahit-tammati min sharri ma khalaq (3x) [Replaces #20 in the evening]
        AthkarItem(
            id = 220,
            arabicText = "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ",
            transliteration = "A'oodhu bi kalimatil-lahit-tammati min sharri ma khalaq.",
            englishTranslation = "I seek refuge in the perfect words of Allah from the evil of that which He has created.",
            targetCount = 3,
            virtue = "Whoever recites this 3 times in the evening, no poisonous creature, sting, or night-time harm will touch him that night.",
            reference = "Sahih Muslim (2709) • Jami` at-Tirmidhi (3604)"
        ),

        // 21. Allahumma inni as'aluka 'ilman nafi'a
        AthkarItem(
            id = 221,
            arabicText = "اللَّهُمَّ إِنِّي أَسْأَلُكَ عِلْمًا نَافِعًا ، وَرِزْقًا طَيِّبًا ، وَعَمَلًا مُتَقَبَّلًا",
            transliteration = "Allahumma innee as'aluka 'ilman nafi'a, wa rizqan tayyiba, wa 'amalan mutaqabbala.",
            englishTranslation = "O Allah, I ask You for beneficial knowledge, wholesome sustenance, and accepted deeds.",
            targetCount = 1,
            virtue = "Supplication seeking beneficial knowledge, lawful livelihood, and deeds that are accepted by Allah.",
            reference = "Sunan Ibn Majah (925) • Musnad Ahmad"
        ),

        // 22. Astaghfirullaha wa atoobu ilayh (100x)
        AthkarItem(
            id = 222,
            arabicText = "أَسْتَغْفِرُ اللَّهَ وَأَتُوبُ إِلَيْهِ",
            transliteration = "Astaghfirullaha wa atoobu ilayh.",
            englishTranslation = "I seek the forgiveness of Allah and repent to Him.",
            targetCount = 100,
            virtue = "Prophetic istighfar repeated 100 times to purify the soul and seek sincere repentance before the night.",
            reference = "Sahih al-Bukhari (6307) • Sahih Muslim (2702)"
        )
    )
}
