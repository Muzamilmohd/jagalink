# JagaLink — Bot pengawal link scam untuk Mak

Bot kecil yang keluar atas skrin bila ada link mencurigakan dalam WhatsApp, Telegram, SMS, Facebook, Instagram, TikTok, Gmail/Outlook — atau bila Mak tekan link tersebut.

## Apa yang dikesan
- **Fail APK** (kad jemputan kahwin palsu, "resit", "saman") — sama ada link `.apk` atau fail yang dihantar dalam chat
- **Huruf pelik / huruf asing** yang menyamar (cth `аpple.com` guna huruf Cyrillic, kod `xn--`)
- **Laman tiruan jenama**: Maybank, CIMB, Public Bank, Bank Islam, BSN, LHDN, KWSP, JPJ, PDRM, Pos Laju, J&T, Shopee, Lazada, TNB, Touch 'n Go dll — termasuk ejaan tipu (`rnaybank`, `shoppe`, `m4ybank`)
- **Link dipendekkan** (bit.ly, tinyurl, s.id…)
- **Hujung alamat murah** yang selalu dipakai scam (.xyz .top .online .icu .click …)
- **Link nombor IP**, simbol `@` tersorok, tiada https, alamat berselirat
- **Ayat scam** dalam mesej/emel (menang hadiah, akaun disekat, sahkan segera, bungkusan tertahan, OTP…)

Semua semakan dibuat **dalam telefon** (offline). Tiada mesej dihantar ke mana-mana.

## 3 lapisan perlindungan
| Lapisan | Bila bot keluar |
|---|---|
| Baca notifikasi | Mesej masuk ada link scam — sebelum Mak buka chat pun |
| Baca skrin (Kebolehcapaian) | Mak buka chat/emel yang ada link scam, atau **tekan** mesej tu |
| Pengawal link (browser utama) | Mak tekan link → JagaLink semak dulu. Selamat = terus buka Chrome. Bahaya = bot halang |

## Cara pasang (guna laptop)
1. Install **Android Studio** (percuma) di laptop.
2. Buka folder `JagaLink` ni dalam Android Studio → tunggu Gradle sync siap (kali pertama agak lama).
3. Di phone Mak: **Tetapan → Perihal telefon → tekan "Nombor binaan" 7 kali** → masuk **Pilihan pembangun** → hidupkan **Penyahpepijatan USB**.
4. Sambung phone Mak ke laptop guna kabel USB → benarkan "Allow USB debugging".
5. Dalam Android Studio pilih phone Mak di atas → tekan butang ▶ **Run**.

## Setup dalam app (sekali sahaja)
Buka JagaLink, tekan **Hidupkan** untuk setiap baris sampai semua ✅:
1. Pop-up bot atas skrin
2. Baca mesej masuk
3. Baca skrin → Kebolehcapaian → JagaLink → On
4. Pengawal link → pilih **JagaLink** sebagai aplikasi pelayar (browser)
5. Jangan dimatikan di latar belakang
6. Notifikasi amaran

Kemudian isi **nama panggilan** dan **nombor telefon anda** (untuk butang "📞 Tanya anak dulu") → Simpan. Tekan **Tunjuk bot amaran** untuk tengok bot keluar.

### Tips ikut jenama phone
- **Xiaomi / Redmi / POCO**: Tetapan → Apl → JagaLink → *Autostart* ON, *Penjimat bateri* = Tiada sekatan. Juga benarkan "Tunjuk tetingkap pop-up semasa berjalan di latar belakang".
- **Oppo / Realme / Vivo**: Tetapan → Bateri → JagaLink → benarkan aktiviti latar belakang + Auto-launch.
- **Samsung**: Tetapan → Penjagaan peranti → Bateri → Had penggunaan latar belakang → pastikan JagaLink **tiada** dalam senarai "Apl tidur".
- Kalau keluar **"Tetapan terhad"**: Info Aplikasi JagaLink → ⋮ → *Benarkan tetapan terhad*.
- **Telegram**: Tetapan → Tetapan Sembang → matikan *Pelayar dalam apl* supaya link lalu JagaLink.

## Tambah jenama / laman selamat
Semua senarai ada dalam `app/src/main/java/com/jagalink/LinkAnalyzer.kt`:
- `BRANDS` – jenama yang selalu ditiru + laman rasmi
- `TRUSTED` – laman yang dikira selamat
- `SCAM_WORDS` – ayat scam
- `SUSPICIOUS_TLDS`, `SHORTENERS`

## Had
- Ini pengesan berasaskan peraturan — ia tangkap kebanyakan taktik biasa tetapi bukan 100%. Teruskan ingatkan Mak: **jangan pasang APK, jangan bagi OTP/TAC**.
- Kalau terkena scam: hubungi **NSRC 997** secepat mungkin, dan bank.
- Pasang dengan pengetahuan Mak — terangkan ini "pengawal link" supaya dia tak tutup kebenarannya.
