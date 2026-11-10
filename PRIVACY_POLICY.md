# Maxfiylik siyosati / Privacy Policy / Политика конфиденциальности

**"Farzandim: Ota-onalar portali"** (`uz.farzandim.parent`)

Rasmiy va to'liq matn (uz / ru / en) veb-saytda e'lon qilingan va ilovaning "Sozlamalar → Maxfiylik siyosati" bo'limidan ochiladi:

**https://farzandim-edu.uz/privacy**

- Ruscha: https://farzandim-edu.uz/privacy?lang=ru
- Inglizcha: https://farzandim-edu.uz/privacy?lang=en

Google Play Console → *App content → Privacy policy* maydoniga `https://farzandim-edu.uz/privacy` kiritiladi.

## Manba fayllar

Matn `school-frontend/src/app/privacy/content.ts` faylida (uch tilda) saqlanadi, sahifa `school-frontend/src/app/privacy/page.tsx`. Siyosatni o'zgartirganda faqat shu faylni tahrirlang va "updated" sanasini yangilang — bu yerdagi nusxa takrorlanmaydi, shunda eskirib qolmaydi.

## Ilova kodiga bog'liq qoidalar (siyosatni o'zgartirishdan oldin tekshiring)

Siyosat quyidagi holatlarga asoslangan. Agar kodda bular o'zgarsa, siyosatni ham yangilang:

- Manifest ruxsatlari: faqat `INTERNET`, `ACCESS_NETWORK_STATE`.
- Reklama, analytics, crash-reporting SDK yo'q.
- `allowBackup="false"`, `usesCleartextTraffic="false"`.
- PIN faqat qurilmada, PBKDF2 xesh sifatida; chiqishda Room bazasi `clearAllTables()` bilan tozalanadi.
- Backend AI haftalik hisobot uchun Google Gemini API'ga o'quvchi ismi, sinfi, baholari, izohlari, kitoblari va davomatini yuboradi (`backend/internal/services/gemini.go`).
