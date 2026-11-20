package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.DhikrDao
import com.example.data.dao.DhikrLogDao
import com.example.data.dao.DhikrPlaylistDao
import com.example.data.model.DhikrItem
import com.example.data.model.DhikrLog
import com.example.data.model.DhikrPlaylist
import com.example.data.model.DhikrPlaylistSerializer
import com.example.data.model.DhikrPlaylistStep
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [DhikrItem::class, DhikrLog::class, DhikrPlaylist::class],
    version = 2,
    exportSchema = false
)
abstract class TasbihDatabase : RoomDatabase() {
    abstract fun dhikrDao(): DhikrDao
    abstract fun dhikrLogDao(): DhikrLogDao
    abstract fun dhikrPlaylistDao(): DhikrPlaylistDao

    companion object {
        @Volatile
        private var INSTANCE: TasbihDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `dhikr_playlists` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `subtitle` TEXT NOT NULL,
                        `stepsJson` TEXT NOT NULL,
                        `isBuiltIn` INTEGER NOT NULL,
                        `orderIndex` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope): TasbihDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TasbihDatabase::class.java,
                    "tasbih_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialAdhkar(database.dhikrDao())
                        populateInitialPlaylists(database.dhikrPlaylistDao())
                    }
                }
            }
        }

        suspend fun populateInitialPlaylists(dao: DhikrPlaylistDao) {
            val builtIns = listOf(
                DhikrPlaylist(
                    id = 1L,
                    name = "After-Salah Tasbih Set",
                    subtitle = "Prophetic 100 Praises Chain • Sunnah",
                    isBuiltIn = true,
                    orderIndex = 1,
                    stepsJson = DhikrPlaylistSerializer.stepsToJson(
                        listOf(
                            DhikrPlaylistStep(
                                dhikrId = 1L,
                                arabicText = "سُبْحَانَ اللَّهِ",
                                transliteration = "SubhanAllah",
                                englishTranslation = "Glory be to Allah",
                                targetCount = 33
                            ),
                            DhikrPlaylistStep(
                                dhikrId = 2L,
                                arabicText = "الْحَمْدُ لِلَّهِ",
                                transliteration = "Alhamdulillah",
                                englishTranslation = "All praise is due to Allah",
                                targetCount = 33
                            ),
                            DhikrPlaylistStep(
                                dhikrId = 3L,
                                arabicText = "اللَّهُ أَكْبَرُ",
                                transliteration = "Allahu Akbar",
                                englishTranslation = "Allah is the Greatest",
                                targetCount = 33
                            ),
                            DhikrPlaylistStep(
                                dhikrId = 5L,
                                arabicText = "لَا إِلٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ",
                                transliteration = "La ilaha illallah wahdahu la shareeka lah, lahul mulku wa lahul hamdu wa Huwa 'ala kulli shay'in qadeer",
                                englishTranslation = "There is no deity except Allah alone without partner, to Him belongs sovereignty and praise",
                                targetCount = 1
                            )
                        )
                    )
                ),
                DhikrPlaylist(
                    id = 2L,
                    name = "Morning Noor Trio",
                    subtitle = "Praise, Forgiveness & Salawat • 100 Beads",
                    isBuiltIn = true,
                    orderIndex = 2,
                    stepsJson = DhikrPlaylistSerializer.stepsToJson(
                        listOf(
                            DhikrPlaylistStep(
                                dhikrId = 7L,
                                arabicText = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ",
                                transliteration = "SubhanAllahi wa bihamdihi",
                                englishTranslation = "Glory be to Allah and His praise",
                                targetCount = 33
                            ),
                            DhikrPlaylistStep(
                                dhikrId = 4L,
                                arabicText = "أَسْتَغْفِرُ اللَّهَ وَأَتُوبُ إِلَيْهِ",
                                transliteration = "Astaghfirullah wa atubu ilayh",
                                englishTranslation = "I seek Allah's forgiveness and repent to Him",
                                targetCount = 33
                            ),
                            DhikrPlaylistStep(
                                dhikrId = 6L,
                                arabicText = "اللَّهُمَّ صَلِّ عَلَىٰ مُحَمَّدٍ وَعَلَىٰ آلِ مُحَمَّدٍ",
                                transliteration = "Allahumma salli 'ala Muhammad wa 'ala ali Muhammad",
                                englishTranslation = "O Allah, send blessings upon Muhammad and his family",
                                targetCount = 34
                            )
                        )
                    )
                ),
                DhikrPlaylist(
                    id = 3L,
                    name = "Calm & Reliance",
                    subtitle = "Evening Tranquility & Tawakkul • 99 Beads",
                    isBuiltIn = true,
                    orderIndex = 3,
                    stepsJson = DhikrPlaylistSerializer.stepsToJson(
                        listOf(
                            DhikrPlaylistStep(
                                dhikrId = 8L,
                                arabicText = "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ",
                                transliteration = "La hawla wa la quwwata illa billah",
                                englishTranslation = "There is no power and no strength except with Allah",
                                targetCount = 33
                            ),
                            DhikrPlaylistStep(
                                dhikrId = 10L,
                                arabicText = "حَسْبُنَا اللَّهُ وَنِعْمَ الْوَكِيلُ",
                                transliteration = "HasbunAllahu wa ni'mal wakeel",
                                englishTranslation = "Allah is sufficient for us, and He is the best Disposer of affairs",
                                targetCount = 33
                            ),
                            DhikrPlaylistStep(
                                dhikrId = 9L,
                                arabicText = "لَا إِلٰهَ إِلَّا أَنْتَ سُبْحَانَكَ إِنِّي كُنْتُ مِنَ الظَّالِمِينَ",
                                transliteration = "La ilaha illa Anta subhanaka inni kuntu minaz-zalimeen",
                                englishTranslation = "There is no deity except You; exalted are You",
                                targetCount = 33
                            )
                        )
                    )
                )
            )
            dao.insertAll(builtIns)
        }

        suspend fun populateInitialAdhkar(dao: DhikrDao) {
            val defaultAdhkar = listOf(
                DhikrItem(
                    id = 1L,
                    arabicText = "سُبْحَانَ اللَّهِ",
                    transliteration = "SubhanAllah",
                    englishTranslation = "Glory be to Allah",
                    category = "After Salah",
                    defaultTarget = 33,
                    virtues = "A palm tree will be planted for him in Paradise (Jami` at-Tirmidhi 3464).",
                    reference = "Tasbih of Fatimah Az-Zahra",
                    orderIndex = 1
                ),
                DhikrItem(
                    id = 2L,
                    arabicText = "الْحَمْدُ لِلَّهِ",
                    transliteration = "Alhamdulillah",
                    englishTranslation = "All praise is due to Allah",
                    category = "After Salah",
                    defaultTarget = 33,
                    virtues = "Fills the scale of good deeds on the Day of Judgment (Sahih Muslim 223).",
                    reference = "Tasbih of Fatimah Az-Zahra",
                    orderIndex = 2
                ),
                DhikrItem(
                    id = 3L,
                    arabicText = "اللَّهُ أَكْبَرُ",
                    transliteration = "Allahu Akbar",
                    englishTranslation = "Allah is the Greatest",
                    category = "After Salah",
                    defaultTarget = 34,
                    virtues = "Beloved words to Allah. Recited after prayers to complete 100 recitations.",
                    reference = "Tasbih of Fatimah Az-Zahra",
                    orderIndex = 3
                ),
                DhikrItem(
                    id = 4L,
                    arabicText = "أَسْتَغْفِرُ اللَّهَ وَأَتُوبُ إِلَيْهِ",
                    transliteration = "Astaghfirullah wa atubu ilayh",
                    englishTranslation = "I seek Allah's forgiveness and repent to Him",
                    category = "Forgiveness",
                    defaultTarget = 100,
                    virtues = "The Prophet ﷺ said: 'By Allah, I seek Allah's forgiveness more than 70 times a day.' (Bukhari 6307).",
                    reference = "Daily Sunnah",
                    orderIndex = 4
                ),
                DhikrItem(
                    id = 5L,
                    arabicText = "لَا إِلٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ",
                    transliteration = "La ilaha illallah wahdahu la shareeka lah",
                    englishTranslation = "There is no deity worthy of worship except Allah alone, without partner",
                    category = "Daily",
                    defaultTarget = 100,
                    virtues = "Whoever says this 100 times will have the reward of freeing 10 slaves, 100 good deeds written, and 100 sins erased (Bukhari 3293).",
                    reference = "Morning & Evening",
                    orderIndex = 5
                ),
                DhikrItem(
                    id = 6L,
                    arabicText = "اللَّهُمَّ صَلِّ عَلَىٰ مُحَمَّدٍ وَعَلَىٰ آلِ مُحَمَّدٍ",
                    transliteration = "Allahumma salli 'ala Muhammad wa 'ala ali Muhammad",
                    englishTranslation = "O Allah, send blessings upon Muhammad and upon the family of Muhammad",
                    category = "Daily",
                    defaultTarget = 100,
                    virtues = "Whoever sends blessings upon me once, Allah sends blessings upon him ten times (Sahih Muslim 408).",
                    reference = "Salawat upon the Prophet ﷺ",
                    orderIndex = 6
                ),
                DhikrItem(
                    id = 7L,
                    arabicText = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ",
                    transliteration = "SubhanAllahi wa bihamdihi",
                    englishTranslation = "Glory be to Allah and His praise",
                    category = "Daily",
                    defaultTarget = 100,
                    virtues = "Whoever says this 100 times in the morning and evening, his sins will be forgiven even if like the foam of the sea (Bukhari 6405).",
                    reference = "Sahih al-Bukhari",
                    orderIndex = 7
                ),
                DhikrItem(
                    id = 71L,
                    arabicText = "سُبْحَانَ اللَّهِ الْعَظِيمِ",
                    transliteration = "SubhanAllahil 'Azeem",
                    englishTranslation = "Glory be to Allah the Magnificent",
                    category = "Daily",
                    defaultTarget = 100,
                    virtues = "Beloved to the Most Merciful, light on the tongue, heavy on the divine scales of deeds (Sahih al-Bukhari 6682).",
                    reference = "Sahih al-Bukhari",
                    orderIndex = 8
                ),
                DhikrItem(
                    id = 8L,
                    arabicText = "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ",
                    transliteration = "La hawla wa la quwwata illa billah",
                    englishTranslation = "There is no power and no strength except with Allah",
                    category = "Daily",
                    defaultTarget = 100,
                    virtues = "A treasure from the treasures of Paradise (Sahih al-Bukhari 6409).",
                    reference = "Hawqala",
                    orderIndex = 8
                ),
                DhikrItem(
                    id = 9L,
                    arabicText = "لَا إِلٰهَ إِلَّا أَنْتَ سُبْحَانَكَ إِنِّي كُنْتُ مِنَ الظَّالِمِينَ",
                    transliteration = "La ilaha illa Anta subhanaka inni kuntu minaz-zalimeen",
                    englishTranslation = "There is no deity except You; exalted are You. Indeed, I have been of the wrongdoers",
                    category = "Quranic",
                    defaultTarget = 40,
                    virtues = "Supplication of Prophet Yunus (AS) in the belly of the whale. No Muslim supplication with this is rejected (Tirmidhi 3505).",
                    reference = "Surah Al-Anbiya 21:87",
                    orderIndex = 9
                ),
                DhikrItem(
                    id = 10L,
                    arabicText = "حَسْبُنَا اللَّهُ وَنِعْمَ الْوَكِيلُ",
                    transliteration = "HasbunAllahu wa ni'mal wakeel",
                    englishTranslation = "Allah is sufficient for us, and He is the best Disposer of affairs",
                    category = "Quranic",
                    defaultTarget = 100,
                    virtues = "Said by Ibrahim (AS) when thrown into fire, and by Muhammad ﷺ when the army gathered (Bukhari 4563).",
                    reference = "Surah Ali 'Imran 3:173",
                    orderIndex = 10
                )
            )
            dao.insertAll(defaultAdhkar)
        }
    }
}
