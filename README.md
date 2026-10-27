# JagaLink — Bot pengawal link scam untuk Mak

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
> Ini perkara biasa. Google sekat **semua** app dari luar Play Store yang minta kebenaran *Kebolehcapaian* dan *Baca notifikasi* — JagaLink perlukan kedua-duanya untuk mengesan scam. Dialog tu tiada butang "pasang juga", jadi:
> 1. Buka **Play Store** → tekan **gambar profil** → **Play Protect** → ⚙️ (atas kanan) → matikan **"Scan apps with Play Protect"**.
> 2. Pasang APK sekali lagi.
> 3. **Hidupkan semula** "Scan apps with Play Protect" terus selepas pasang.
>
> Atau guna **Cara B** (ADB) — sekatan ni tak kena pada pemasangan melalui kabel.

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

## 3 lapisan perlindungan
| Lapisan | Bila bot keluar |
|---|---|
| Baca notifikasi | Mesej masuk ada link scam — sebelum Mak buka chat pun |
| Baca skrin (Kebolehcapaian) | Mak buka chat/emel yang ada link scam, atau **tekan** mesej tu |
| Pengawal link (browser utama) | Mak tekan link → JagaLink semak dulu. Selamat = terus buka Chrome. Bahaya = bot halang |

## ⚙️ Setup dalam app (sekali sahaja)
Pilih bahasa di atas (**Melayu / English / 中文**), kemudian:
Buka JagaLink, tekan **Hidupkan** untuk setiap baris sampai semua ✅:
1. Pop-up bot atas skrin
2. Baca mesej masuk
3. Baca skrin → Kebolehcapaian → JagaLink → On
4. Pengawal link → pilih **JagaLink** sebagai aplikasi pelayar (browser)
5. Jangan dimatikan di latar belakang
6. Notifikasi amaran

Kemudian isi **nama panggilan** (cth Mak, Ayah, Atuk) dan **nombor telefon anda** (untuk butang "📞 Tanya anak dulu") → Simpan. Tekan **Tunjuk bot amaran** untuk tengok bot keluar.

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
