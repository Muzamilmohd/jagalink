# JagaLink — Bot pengawal link scam untuk Mak

**🇲🇾 Bahasa Melayu** · [🇬🇧 English](#-english)

Bot kecil yang keluar atas skrin bila ada link mencurigakan dalam WhatsApp, Telegram, SMS, Facebook, Instagram, TikTok, Gmail/Outlook — atau bila Mak tekan link tersebut.

## Apa yang dikesan
- **Fail APK** (kad jemputan kahwin palsu, "resit", "saman") — sama ada link `.apk` atau fail yang dihantar dalam chat
- **Huruf pelik / huruf asing** yang menyamar (cth `аpple.com` guna huruf Cyrillic, kod `xn--`)
- **Laman tiruan jenama**: Maybank, CIMB, Public Bank, Bank Islam, BSN, LHDN, KWSP, JPJ, PDRM, Pos Laju, J&T, Shopee, Lazada, TNB, Touch 'n Go dll — termasuk ejaan tipu (`rnaybank`, `shoppe`, `m4ybank`)
- **Link dipendekkan** (bit.ly, tinyurl, s.id…)
- **Hujung alamat murah** yang selalu dipakai scam (.xyz .top .online .icu .click …)
- **Link nombor IP**, simbol `@` tersorok, tiada https, alamat berselirat
- **Ayat scam** dalam mesej/emel (menang hadiah, akaun disekat, sahkan segera, bungkusan tertahan, OTP…) — BM, English & 中文
- **Laman lucah** — disekat terus, tiada pilihan "buka juga"

Semua semakan dibuat **dalam telefon**. Tiada mesej atau link dihantar ke mana-mana. Internet hanya digunakan untuk semak versi baru di GitHub.

**Bahasa:** Melayu, English, 中文 (pilih dalam app). **Sokong** WhatsApp kedua (Samsung Dual Messenger).

## 📥 Cara pasang

> **Muat turun:** [Releases → JagaLink-vX.X.apk](https://github.com/Muzamilmohd/jagalink/releases/latest)

### Cara A — Muat turun APK terus dalam phone
1. Buka [halaman Releases](https://github.com/Muzamilmohd/jagalink/releases/latest) dalam browser phone → tekan fail **`JagaLink-vX.X.apk`**.
2. Bila ditanya, benarkan browser **"Pasang app tidak diketahui"** (Install unknown apps).
3. Tekan **Pasang**.

> ⚠️ **Keluar "App blocked to protect your device" (Google Play Protect)?**
> Ini perkara biasa. Google sekat **semua** app dari luar Play Store yang minta kebenaran *Kebolehcapaian* (baca skrin) — JagaLink perlukan kebenaran tu untuk mengesan scam. Dialog tu tiada butang "pasang juga", jadi:
> 1. Buka **Play Store** → tekan **gambar profil** → **Play Protect** → ⚙️ (atas kanan) → matikan **"Scan apps with Play Protect"**.
> 2. Pasang APK sekali lagi.
> 3. **Hidupkan semula** "Scan apps with Play Protect" terus selepas pasang.
>
> Atau guna **Cara B** (ADB) — sekatan ni tak kena pada pemasangan melalui kabel.

> 🔒 **Suis "Baca skrin" / "Pop-up bot" kelabu, tak boleh tekan?**
> Android 13 ke atas sekat kebenaran sensitif untuk app yang dipasang dari browser ("Tetapan terhad").
> 1. Tekan suis yang kelabu tu **sekali** → keluar dialog "Tetapan terhad" → OK. *(Wajib — pilihan seterusnya hanya muncul lepas ni.)*
> 2. **Tetapan → Apl → JagaLink** → tekan **⋮** (atas kanan) → **Benarkan tetapan terhad** → sahkan dengan PIN.
> 3. Kembali dan hidupkan semula suis tadi.
>
> **Xiaomi / Redmi / POCO:** juga buka **Tetapan → Apl → Urus apl → JagaLink → Kebenaran lain** → benarkan **"Paparkan tetingkap timbul"** dan **"…semasa berjalan di latar belakang"**.
> Masih kelabu? Pasang guna **Cara B** (ADB) — app yang dipasang melalui kabel biasanya tak kena sekatan ni.

### Cara B — Pasang melalui komputer (ADB)
1. Di phone: **Tetapan → Perihal telefon → Maklumat perisian → tekan "Nombor binaan" 7 kali** → masuk **Pilihan pembangun** → hidupkan **Penyahpepijatan USB**.
2. Sambung phone ke komputer guna kabel USB → benarkan **"Allow USB debugging"**.
3. Pasang [Android platform-tools](https://developer.android.com/tools/releases/platform-tools) (ada `adb`), kemudian:
   ```
   adb install -r JagaLink-vX.X.apk
   ```

### Cara C — Bina sendiri dari kod (Android Studio)
1. Install **Android Studio** (percuma) → buka folder projek ni → tunggu Gradle sync siap.
2. Sambung phone (macam Cara B langkah 1–2) → pilih phone di atas → tekan ▶ **Run**.

> Versi yang dibina sendiri (debug) ditandatangan dengan kunci lain — ia **tak boleh** terima kemas kini dari Releases. Uninstall dulu sebelum tukar ke APK dari Releases.

## 🔄 Kemas kini
JagaLink semak GitHub **sekali sehari** dan setiap kali app dibuka. Bila ada versi baru:
1. Keluar notifikasi **"Versi baru JagaLink …"** → tekan.
2. Tekan **Kemas kini** → (kali pertama) benarkan JagaLink **pasang app** → tekan **Kemas kini** lagi.
3. Android tanya pengesahan → tekan **Pasang**. Tetapan dan kebenaran kekal.

Boleh juga semak sendiri: buka JagaLink → **4. Kemas kini → Semak kemas kini**. Suis *Semak sendiri setiap hari* boleh dimatikan.

## 2 lapisan perlindungan
| Lapisan | Bila bot keluar |
|---|---|
| Baca skrin (Kebolehcapaian) | Mak buka chat/emel yang ada link scam, atau **tekan** mesej tu |
| Pengawal link (browser utama) | Mak tekan link → JagaLink semak dulu. Selamat = terus buka Chrome. Bahaya = bot halang |

JagaLink **tidak** baca mesej masuk atau notifikasi — ia hanya semak teks yang nampak di skrin dan link yang Mak tekan.

## ⚙️ Setup dalam app (sekali sahaja)
Pilih bahasa di atas (**Melayu / English / 中文**), kemudian:
Buka JagaLink, tekan **Hidupkan** untuk setiap baris sampai semua ✅:
1. Pop-up bot atas skrin
2. Baca skrin → Kebolehcapaian → JagaLink → On
3. Pengawal link → pilih **JagaLink** sebagai aplikasi pelayar (browser)
4. Jangan dimatikan di latar belakang
5. Notifikasi amaran

Kemudian isi **nama panggilan** (cth Mak, Ayah, Atuk) dan **nombor telefon anda** (untuk butang "💬 Hantar link ke anak" — buka WhatsApp anda dengan link scam tu untuk anda semak) → Simpan. Tekan **Tunjuk bot amaran** untuk tengok bot keluar.

**WhatsApp kedua (Samsung Dual Messenger):** WhatsApp kedua guna salinan JagaLink yang berasingan. Kali pertama bot keluar dari WhatsApp kedua, tekan **⚙️ Isi nama & nombor anak untuk WhatsApp kedua** pada kad amaran dan simpan sekali lagi. (Dalam profil tu bot berbunyi "beep" kerana tiada enjin suara.)

### Tips ikut jenama phone
- **Xiaomi / Redmi / POCO**: Tetapan → Apl → JagaLink → *Autostart* ON, *Penjimat bateri* = Tiada sekatan. Juga benarkan "Tunjuk tetingkap pop-up semasa berjalan di latar belakang".
- **Oppo / Realme / Vivo**: Tetapan → Bateri → JagaLink → benarkan aktiviti latar belakang + Auto-launch.
- **Samsung**: Tetapan → Penjagaan peranti → Bateri → Had penggunaan latar belakang → pastikan JagaLink **tiada** dalam senarai "Apl tidur".
- Kalau keluar **"Tetapan terhad"**: Info Aplikasi JagaLink → ⋮ → *Benarkan tetapan terhad*.
- **Telegram**: Tetapan → Tetapan Sembang → matikan *Pelayar dalam apl* supaya link lalu JagaLink.
- **Facebook / Instagram / TikTok** buka link dalam pelayar sendiri — pengawal link tak nampak, tapi bot masih mengesan link yang nampak atas skrin.

## Tambah jenama / laman selamat
Semua senarai ada dalam `app/src/main/java/com/jagalink/LinkAnalyzer.kt`:
- `BRANDS` – jenama yang selalu ditiru + laman rasmi
- `TRUSTED` – laman yang dikira selamat
- `SCAM_WORDS` – ayat scam
- `SUSPICIOUS_TLDS`, `SHORTENERS`
- `ADULT_SITES`, `ADULT_WORDS` – laman lucah yang disekat

## Had
- Ini pengesan berasaskan peraturan — ia tangkap kebanyakan taktik biasa tetapi bukan 100%. Teruskan ingatkan Mak: **jangan pasang APK, jangan bagi OTP/TAC**.
- Kalau terkena scam: hubungi **NSRC 997** secepat mungkin, dan bank.
- Pasang dengan pengetahuan Mak — terangkan ini "pengawal link" supaya dia tak tutup kebenarannya.

---

## 🇬🇧 English

[🇲🇾 Bahasa Melayu](#jagalink--bot-pengawal-link-scam-untuk-mak) · **🇬🇧 English**

**JagaLink** is a small robot that pops up over other apps to warn an elderly parent when a suspicious link or APK file appears in WhatsApp, Telegram, SMS, Facebook, Instagram, TikTok or email — or when they tap one. It detects fake bank/government/delivery sites (Maybank, CIMB, LHDN, Pos Laju, Shopee…), look-alike letters, shortened links, cheap scam domains, scam phrases (Malay, English, Chinese) and APK files, and **blocks adult sites**.

All checks run **on the phone**. No messages or links are sent anywhere — the internet is only used to check GitHub for new versions. JagaLink does **not** read incoming messages or notifications — it only checks text shown on screen and links that are tapped. App language: Malay, English or 中文 (choose in the app).

### 📥 How to install

> **Download:** [Releases → JagaLink-vX.X.apk](https://github.com/Muzamilmohd/jagalink/releases/latest)

#### Option A — Download the APK on the phone
1. Open the [Releases page](https://github.com/Muzamilmohd/jagalink/releases/latest) in the phone's browser → tap **`JagaLink-vX.X.apk`**.
2. When asked, allow the browser to **"Install unknown apps"**.
3. Tap **Install**.

> ⚠️ **"App blocked to protect your device" (Google Play Protect)?**
> This is expected. Google automatically blocks apps from outside the Play Store that ask for *Accessibility* (read screen) — JagaLink needs it to detect scams. The dialog has no "install anyway" button, so:
> 1. Open **Play Store** → tap your **profile picture** → **Play Protect** → ⚙️ (top right) → turn off **"Scan apps with Play Protect"**.
> 2. Install the APK again.
> 3. **Turn "Scan apps with Play Protect" back on** right after installing.
>
> Or use **Option B** (ADB) — this block does not apply to installs over a cable.

> 🔒 **"Read screen" / "Bot pop-up" switch is greyed out?**
> Android 13+ restricts sensitive permissions for apps installed from a browser ("Restricted settings").
> 1. Tap the greyed-out switch **once** → a "Restricted setting" dialog appears → OK. *(Required — the next option only appears after this.)*
> 2. **Settings → Apps → JagaLink** → tap **⋮** (top right) → **Allow restricted settings** → confirm with your PIN.
> 3. Go back and turn the switch on.
>
> **Xiaomi / Redmi / POCO:** also open **Settings → Apps → Manage apps → JagaLink → Other permissions** → allow **"Display pop-up windows"** and **"…while running in the background"**.
> Still greyed out? Install with **Option B** (ADB) — apps installed over a cable usually aren't restricted.

#### Option B — Install from a computer (ADB)
1. On the phone: **Settings → About phone → Software information → tap "Build number" 7 times** → open **Developer options** → turn on **USB debugging**.
2. Connect the phone with a USB cable → allow **"Allow USB debugging"**.
3. Install [Android platform-tools](https://developer.android.com/tools/releases/platform-tools) (includes `adb`), then:
   ```
   adb install -r JagaLink-vX.X.apk
   ```

#### Option C — Build from source (Android Studio)
1. Install **Android Studio** (free) → open this project folder → wait for Gradle sync.
2. Connect the phone (Option B steps 1–2) → select it at the top → press ▶ **Run**.

> Self-built (debug) versions are signed with a different key and **cannot** receive updates from Releases. Uninstall first before switching to the Releases APK.

### ⚙️ First-time setup in the app
Choose the language at the top (**Melayu / English / 中文**), then tap **Turn on** for each row until all are ✅:
1. Bot pop-up on screen
2. Read screen → Accessibility → JagaLink → On
3. Link guard → choose **JagaLink** as the browser app
4. Keep running in the background
5. Warning notifications

Then fill in **what the bot calls the user** (e.g. Mum, Dad, Grandma) and **your phone number** (for the "💬 Send link to my child" button — opens WhatsApp to you with the scam link so you can check it) → **Save**. Tap **Show warning bot (example)** to see the bot.

**Second WhatsApp (Samsung Dual Messenger):** it uses a separate copy of JagaLink. The first time the bot appears from the second WhatsApp, tap **⚙️ Set name & child's number for the second WhatsApp** on the warning card and save again. (In that profile the bot beeps instead of speaking — there is no voice engine there.)

#### Tips by phone brand
- **Xiaomi / Redmi / POCO**: Settings → Apps → JagaLink → *Autostart* ON, *Battery saver* = No restrictions.
- **Oppo / Realme / Vivo**: Settings → Battery → JagaLink → allow background activity + Auto-launch.
- **Samsung**: Settings → Device care → Battery → Background usage limits → make sure JagaLink is **not** in "Sleeping apps".
- **Telegram**: Settings → Chat Settings → turn off *In-App Browser* so links go through JagaLink.
- **Facebook / Instagram / TikTok** open links in their own browser — the link guard can't see those, but the bot still detects links shown on screen.

### 🔄 Updates
JagaLink checks GitHub **once a day** and whenever the app is opened. When a new version is out:
1. A **"New JagaLink version …"** notification appears → tap it.
2. Tap **Update** → (first time only) allow JagaLink to **install apps** → tap **Update** again.
3. Android asks for confirmation → tap **Install**. Settings and permissions are kept.

You can also check manually: open JagaLink → **4. Updates → Check for updates**.

### Limits
- This is a rule-based detector — it catches most common tricks but not 100%. Keep reminding your parent: **never install APKs, never share OTP/TAC codes**.
- If scammed: call **NSRC 997** (Malaysia) as soon as possible, and the bank.
- Install it with your parent's knowledge — explain it's a "link guard" so they don't turn its permissions off.
