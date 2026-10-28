# Google Play — JagaPMA

Semua teks untuk Play Console + langkah demi langkah. Salin dan tampal terus.

## 1. Maklumat asas (Create app)
| Medan | Isi |
|---|---|
| App name | `JagaPMA - Jaga Phone Mak Ayah` |
| Default language | Malay (Malaysia) – ms-MY |
| App or game | App |
| Free or paid | **Free** *(tak boleh tukar ke berbayar lepas ni)* |
| Category | Tools |
| Contact email | *(emel developer anda)* |
| Privacy policy | https://github.com/Muzamilmohd/jagalink/blob/main/PRIVACY.md |

## 2. Store listing

### 🇲🇾 Malay (ms-MY) — default

**Short description** (maks 80 aksara)
```
Bot amaran link scam, fail APK & laman lucah untuk Mak Ayah. Tanpa internet.
```

**Full description** (maks 4000 aksara)
```
JagaPMA (Jaga Phone Mak Ayah) ialah bot kecil yang jaga Mak dan Ayah daripada link scam.

Bila Mak tekan link dalam WhatsApp, Telegram, SMS, Facebook atau emel, JagaPMA semak dulu. Link selamat terus dibuka dalam browser biasa. Link bahaya? Bot keluar dengan amaran yang jelas, bersuara, dalam bahasa yang mudah.

🛡️ APA YANG DIKESAN
• Laman tiruan bank, agensi kerajaan, syarikat penghantaran dan kedai online — termasuk ejaan tipu yang menyamar nama sebenar
• Link yang memuat turun fail APK (taktik "kad jemputan kahwin", "resit", "saman")
• Huruf asing yang menyamar jadi huruf biasa
• Link dipendekkan (bit.ly, tinyurl…) dan alamat murah yang selalu dipakai scammer (.xyz, .top, .click…)
• Laman lucah — disekat terus

👵 MUDAH UNTUK WARGA EMAS
• Kad amaran besar dengan bot yang comel
• Bot bercakap dan phone bergetar
• Nama panggilan sendiri: "Mak, hati-hati!"
• Butang "Hantar link ke anak" — buka WhatsApp anak dengan link tu supaya anak boleh semak
• Bahasa Melayu, English dan 中文

🔒 PRIVASI
• Tiada kebenaran internet — semua semakan dibuat dalam phone
• Tidak baca mesej atau notifikasi
• Tiada iklan, tiada pengumpulan data

⚙️ CARA GUNA
1. Buka JagaPMA dan pilih bahasa
2. Jadikan JagaPMA sebagai "browser utama" — supaya setiap link yang ditekan disemak dulu
3. Isi nama panggilan dan nombor telefon anak
4. Siap! Link selamat tetap dibuka dalam Chrome seperti biasa

Ingat: JagaPMA membantu, tetapi tiada pengesan yang 100% sempurna. Jangan sesekali berikan OTP/TAC kepada sesiapa. Jika terkena scam, hubungi NSRC 997 segera.
```

### 🇬🇧 English (en-US) — add as translation

**Short description**
```
Scam-link, APK & adult-site warning bot for elderly parents. Works offline.
```

**Full description**
```
JagaPMA ("Jaga Phone Mak Ayah" — guard Mum & Dad's phone) is a friendly little bot that protects elderly parents from scam links.

When your parent taps a link in WhatsApp, Telegram, SMS, Facebook or email, JagaPMA checks it first. Safe links open in their normal browser. Dangerous ones? The bot pops up with a clear, spoken warning in simple language.

🛡️ WHAT IT DETECTS
• Fake bank, government, delivery and online-shop sites — including trick spellings that imitate the real name
• Links that download APK files (the "wedding invitation", "receipt", "summons" trick)
• Foreign look-alike letters
• Shortened links (bit.ly, tinyurl…) and cheap domains scammers love (.xyz, .top, .click…)
• Adult sites — blocked outright

👵 EASY FOR SENIORS
• Big warning card with a cute bot
• Spoken warning and vibration
• Your own name for them: "Mum, be careful!"
• "Send link to my child" button — opens WhatsApp to you with the link so you can check it
• Malay, English and Chinese

🔒 PRIVACY
• No internet permission — every check happens on the phone
• Does not read messages or notifications
• No ads, no data collection

⚙️ HOW TO USE
1. Open JagaPMA and choose a language
2. Set JagaPMA as the "default browser" so every tapped link is checked first
3. Enter what to call your parent and your phone number
4. Done! Safe links still open in Chrome as usual

Note: JagaPMA helps, but no detector is perfect. Never share OTP/TAC codes with anyone. If scammed in Malaysia, call NSRC 997 immediately.
```

## 3. Grafik (Store listing → Graphics)
| Item | Saiz | Fail |
|---|---|---|
| App icon | 512 × 512 PNG | `play/graphics/icon-512.png` |
| Feature graphic | 1024 × 500 PNG/JPG | `play/graphics/feature-1024x500.png` |
| Phone screenshots | 2–8 keping, sisi panjang ≤ 2× sisi pendek | `play/graphics/screenshot-*.png` |

> Jangan letak logo bank sebenar dalam grafik/screenshot — Google boleh anggap menyamar jenama.

## 4. App content (Policy → App content)
| Bahagian | Jawapan |
|---|---|
| Privacy policy | URL di atas |
| Ads | **No**, my app does not contain ads |
| App access | **All functionality is available without special access** (tiada login) |
| Content rating | Isi soal selidik IARC: kategori *Utility/Productivity*, jawab **No** untuk semua (keganasan, seks, dadah, perjudian, pengguna berinteraksi, kongsi lokasi). App *menyekat* laman lucah, bukan memaparkannya |
| Target audience | **18 and over** sahaja (bukan untuk kanak-kanak) |
| News app | No |
| COVID-19 | No |
| Data safety | Lihat bawah |
| Government app | No |
| Financial features | None |
| Health | No |

### Data safety
- **Does your app collect or share any of the required user data types?** → **No**
- Is all data encrypted in transit? → *(tak berkaitan — tiada data dihantar)*
- Do you provide a way for users to request data deletion? → *(tak berkaitan — tiada data dikumpul)*

> Kenapa "No": versi Play **tiada kebenaran internet**. Nama & nombor anak hanya disimpan dalam phone (data yang kekal dalam peranti tidak dikira "collected"). "Hantar link ke anak" hanya membuka WhatsApp/SMS — pengguna sendiri yang tekan Hantar.

### Permissions yang ditanya Google
Versi Play hanya ada: `POST_NOTIFICATIONS`, `VIBRATE`. Tiada kebenaran sensitif — tiada borang deklarasi diperlukan.

## 5. Ujian tertutup (wajib untuk akaun peribadi baru)
1. **Testing → Closed testing → Create track** (cth "Penguji").
2. Tambah **sekurang-kurangnya 12 penguji** (alamat Gmail) dalam senarai e-mel.
3. Upload `JagaPMA-v<ver>-play.aab` → **Save → Review release → Start rollout**.
4. Hantar link "Join on Android / web" kepada penguji. Mereka mesti **join dan kekal** selama **14 hari berturut-turut**.
5. Lepas 14 hari: **Dashboard → Apply for production** → jawab soalan tentang ujian → tunggu kelulusan.

## 6. Production
**Production → Create new release** → upload AAB (versionCode baru) → negara: Malaysia (+ lain) → **Start rollout to Production**. Semakan Google biasanya 1–7 hari.

## 7. App signing
Bila upload AAB pertama, pilih **"Let Google manage and protect your app signing key"** (cadangan). Fail `jagalink-release.jks` jadi *upload key* — simpan baik-baik.
> Kesan: APK dari Play dan APK dari GitHub ditandatangan dengan kunci berbeza. Pengguna versi GitHub perlu **uninstall** dulu sebelum pasang dari Play.
