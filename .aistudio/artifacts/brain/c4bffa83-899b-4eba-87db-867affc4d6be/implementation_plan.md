# Tasbih Noor — Architecture & Implementation Plan

## 1. Project Overview & Vision
**Tasbih Noor** is a tranquil, spiritually immersive Islamic prayer bead (Misbaha / Subha) application designed with genuine physics-inspired beads on a string, tactile haptic feedback, acoustic chimes, and persistent analytics.

The app's design philosophy is centered around **tranquility, spiritual devotion, and zero distractions**—prioritizing the default **Night Noor** emerald-and-gold aesthetic, seamless linear bead dragging, authentic Prophetic sunnah recitations, and 100% offline privacy.

---

## 2. Completed Implementations & Capabilities

### A. Linear Bead Physics & Tactile Recitation
- **Linear Horizontal Dragging**: Beads glide naturally across the string with configurable string tension (`Smooth`, `Standard`, `Firm`) and sensitivity.
- **Single-Bead Pull Lock**: Prevents accidental multi-bead skipping when pulling quickly.
- **Acoustic & Tactile Resonance**: Custom acoustic wood, ceramic, crystal, pearl, and oud beads sound effects paired with variable-intensity haptic feedback (`Light`, `Medium`, `Subtle`, `None`).
- **Rich Bead Materials**: Olive Wood, White Pearl, Black Onyx, Amber Crystal, and textured Tiger's Eye Agate with chatoyancy striations.

### B. Chained After-Salah Tasbih Set (33-33-33-1)
- Authentic Prophetic post-prayer Tasbih (Sahih Muslim 597):
  1. **33 SubhanAllah** (سُبْحَانَ اللَّهِ)
  2. **33 Alhamdulillah** (الْحَمْدُ لِلَّهِ)
  3. **33 Allahu Akbar** (اللَّهُ أَكْبَرُ)
  4. **1 Tahlil Seal** (لَا إِلٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ...)
- Auto-advances across stages upon reaching each 33 count without breaking concentration.
- Accurately splits and logs each dhikr into the local Room database under its respective prayer category for faithful analytics.
- Visual 4-stage stepper HUD (`✓ 1`, `✓ 2`, `▶ 3`, `4`) and total progress (`Set: 47/100`).
- Dedicated launch cards on both the main screen and in the Adhkar Library.

### C. Visual Spiritual Remembrance Widget (Home Screen Athkar & Duas)
- **`TasbihAppWidgetProvider`**: Dedicated visual home screen widget (`3x2` / `horizontal|vertical`) designed exclusively for peaceful spiritual remembrance and daily reminders.
- **Enlarged High-Legibility Typography**: Arabic calligraphy font increased to **20sp** bold with expanded multi-line support (`maxLines="3"`), English translation & transliteration increased to **13sp** with soft emerald contrast (`#D6EFE0`), and prominent category tags (`11sp` bold gold).
- **Pure Visual Focus (Removed Bead Button)**: Removed the tiny `+1 Bead` button and counter clutter, dedicating 100% of the widget canvas to the sacred words and translation.
- **"↻ Next Dua" Instant Shuffle**: Direct interactive button (and tap-on-card) allowing users to shuffle through 20+ authentic Qur'anic Duas, Daily Athkar, and Prophetic reminders with subtle haptic response.
- **One-Tap App Access**: Seamless "Open Noor ➔" shortcut to transition directly into the prayer beads screen.

### D. Dedicated Real-Time Qibla Compass (اتجاه القبلة الشريفة)
- **Dedicated Full-Bleed Experience**: Streamlined the third navigation tab into a pure, uncluttered Qibla Compass screen (`QiblaScreen.kt`), eliminating the prayer times timetable and calculation selectors while preserving the compass.
- **Great-Circle Mathematical Calculation**: 100% offline spherical trigonometry calculating the exact bearing from any coordinate on Earth to the Holy Kaaba in Makkah (21.4225° N, 39.8262° E).
- **Real-Time 3-Axis Sensor Compass**: Live device azimuth with sensor lifecycle management (`DisposableEffect`), cardinal direction labels, degree ticks, and smooth animated dial rotation.
- **Celestial Alignment Aura & Haptic Pulse**: When the device is pointed directly towards the Kaaba (within ±4° of bearing), the dial and status badge glow with radiant emerald & gold ("🕋 Facing the Kaaba (Qibla) ✓") and fire a gentle haptic vibration pulse.
- **Directional Guidance HUD**: Displays dynamic prompts ("Turn X° to the Right ➔" or "Turn X° to the Left ⬅") when rotating to find the Qibla.
- **Telemetry Grid & Global City Selector**: Live telemetry displaying Qibla Bearing (° from True North), Current Device Heading, Great-Circle Distance to Makkah in km, and Holy Sanctuary coordinates, paired with an instant search dialog for 50+ global cities.
- **Sensor Calibration Guidance**: Contextual guidance card explaining figure-8 sensor calibration and real-time hardware sensor status.

### E. Local Data Backup & Export (JSON / CSV)
- **Zero-Permission File Export & Share**: Uses Android Sharesheet and Storage Access Framework.
- **Full JSON Backup**: Backs up all custom dhikrs, categories, targets, recitation history, and timestamps.
- **Spreadsheet CSV Export**: Exports full chronological recitation logs (`ID, Date, Dhikr Title, Count, Timestamp, Formatted Time`) easily imported into Google Sheets or Microsoft Excel.
- **One-Tap JSON Restore**: Allows picking a `.json` backup file from device storage or cloud drives with complete restoration verification and feedback.

### F. Authentic Adhkar Booklets (Morning & Evening — 22 Duas Each)
- Full structured recitation reader for **Morning Adhkar** (22 Duas) and **Evening Adhkar** (22 Duas) matching the authentic *Hisn al-Muslim* sequence from the user's reference (*Azkar* framework).
- Includes the 3 protective Surahs (3x each), Ayatul Kursi (1x), Sayyid al-Istighfar (1x), morning/evening affirmations, 360° protection supplications, the 4-phrase glorification (*'Adada khalqihi*, 3x) in morning replaced by *A'oodhu bi kalimat-illahit-tammati min sharri ma khalaq* (3x) in evening, and 100x tahlil/tasbih/istighfar targets.
- Includes complete Arabic text with full tashkeel, clear English transliteration, comprehensive translations, authentic Hadith citations, individual counters, and progress tracking.

### G. Lifetime Devotion Ranks & Analytics
- 10 perpetual spiritual ranks (from *Seeker of Noor* to *Radiant Devotee* and *Master of Tasbih*).
- Dynamic horizon progression bar showing progress toward the next milestone tier.
- Monthly consistency heatmap and uncluttered *Most Recited Prayers* ranking badges.

### H. Persistence, Theming & Screen Space Optimization
- **`TasbihPreferences`**: Persistent settings for sound effects, haptic intensity, bead material, tension, set mode, city, and calculation conventions backed by `SharedPreferences`.
- **Radiant Day Noor Theme (نور النهار)**: Created a warm, illuminated daylight visual experience matching the elegance of Night Noor, featuring high-detail Islamic geometric arabesque artwork (`bg_islamic_day.jpg`), luminous warm ivory parchment (`#FAF7F0`), burnished gold accents (`#C4972B`), and royal imperial emerald calligraphy (`#0B3326`).
- **Main Screen Space Optimization**: Replaced bloated stacked full-width banners with a single compact, balanced quick-action row (`[ 🌅 Daily Adhkar ➔ ]` and `[ 🕌 After-Salah (33×3) ➔ ]`), reclaiming ~55dp of vertical space so prayer beads glide on a spacious canvas.
- **Full-Page Unified Adhkar Library Scrolling**: Converted the Adhkar Library from a cramped half-page split into a full-page unified `LazyColumn`. Featured booklets, After-Salah card, search, and category chips scroll naturally, giving supplications 100% full-screen vertical space.
- **Custom Adaptive Launcher Icon**: High-definition transparent foreground vector on an emerald gradient base compliant with Google Play standards.
