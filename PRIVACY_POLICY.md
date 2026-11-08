# Maxfiylik Siyosati / Privacy Policy / Политика конфиденциальности
**"Farzandim" Mobil Ilovasi (Ota-onalar Portali)**
*Oxirgi yangilanish: 2026-yil 26-oktabr*

---

## 1. Umumiy Qoidalar (General Terms)
Ushbu Maxfiylik siyosati "Farzandim" mobil ilovasi ("Ilova", "Tizim") foydalanuvchilarining ("Foydalanuvchi", "Ota-ona", "Qonuniy vasiy") shaxsiy va ta'limga oid ma'lumotlarini to'plash, qayta ishlash, saqlash va himoya qilish tartibini belgilaydi. 

Ilovadan foydalanish orqali siz mazkur Maxfiylik siyosatida belgilangan shartlarga rozilik bildirasiz.

---

## 2. To'planadigan Ma'lumotlar (Data Collected)
"Farzandim" ilovasi ota-onalarga farzandlarining maktabdagi ta'lim jarayonini kuzatish imkonini beruvchi ta'lim platformasidir. Tizim faqat ta'lim jarayonini tashkil etish va nazorat qilish uchun zarur bo'lgan quyidagi ma'lumotlarni qayta ishlaydi:

1. **Hisob ma'lumotlari (Authentication)**:
   - Ota-onaning pasport seriyasi va raqami yoki ID-karta ma'lumotlari (maktab ma'muriyati tomonidan taqdim etilgan hisobga xavfsiz kirish uchun);
   - Parol va foydalanuvchi tomonidan o'rnatilgan 4 xonali PIN-kod (PBKDF2 kriptografik xeshlangan holda faqat qurilma xotirasida saqlanadi);
   - Maktab kodi (subdomen).
2. **O'quvchi ma'lumotlari (Student Educational Data)**:
   - O'quvchining ismi, familiyasi, sinfi;
   - Fanlar, dars jadvali va dars mavzulari;
   - Akademik baholar, o'zlashtirish ko'rsatkichlari, choraklik natijalar;
   - Davomat (darsga kelgan/kelmagan/kechikkan) va xulq-atvor ko'rsatkichlari;
   - O'qituvchilarning izohlari va fikrlari.
3. **Moliyaviy va Qo'shimcha Xizmatlar (Optional Services)**:
   - Maktab oshxonasi menyusi va ota-ona balansi (agar maktab ushbu moduldan foydalansa);
   - To'garaklar va kutubxona kitoblari ro'yxati.
4. **Qurilma va Xavfsizlik**:
   - Biometrik autentifikatsiya (Barmoq izi / FaceID): biometrik ma'lumotlar faqat qurilmaning o'zida Android BiometricPrompt orqali qayta ishlanadi, hech qachon serverga yuborilmaydi.
   - Ilova reklama identifikatorlari (Advertising ID) va foydalanuvchini kuzatuvchi reklama trekerlaridan MUTLAQO FOYDALANMAYDI.

---

## 3. Ma'lumotlardan Foydalanish Maqsadi (Purpose of Data Use)
Yig'ilgan ma'lumotlar quyidagi maqsadlarda foydalaniladi:
- Ota-onaga o'z farzandining darslari, baholari va davomatini onlayn rejimda ko'rish imkonini yaratish;
- Maktab ma'muriyati va o'qituvchilar bilan samarali qayta aloqa o'rnatish;
- Haftalik ta'lim dinamikasini shakllantirish;
- Tizim xavfsizligini ta'minlash va ruxsatsiz kirishlarning oldini olish.

---

## 4. Ma'lumotlarni Himoyalash va Saqlash (Data Security & Retention)
- **Shifrlash (Encryption in Transit)**: Barcha tarmoq so'rovlari HTTPS (TLS 1.3) protokoli orqali amalga oshiriladi. Ochiq matnli trafigi (`usesCleartextTraffic`) bloklangan.
- **Multitenancy Baza Himoyasi (Data Isolation)**: Har bir maktabning ma'lumotlari alohida PostgreSQL ma'lumotlar bazasida (Database-per-Tenant) ajratilgan holda saqlanadi.
- **Zaxira nusxalash xavfsizligi**: Tizim ma'lumotlari shaxsiy qurilmaning ochiq bulutli zaxirasiga tushib qolmasligi uchun `android:allowBackup="false"` o'rnatilgan.
- **Uchinchi shaxslarga bermaslik**: Foydalanuvchilarning shaxsiy ma'lumotlari hech qachon uchinchi shaxslarga, reklama agentliklariga yoki marketologlarga sotilmaydi yoki topshirilmaydi.

---

## 5. Bolalar Maxfiyligi (Children's Privacy)
Ilova faqat voyaga yetmagan o'quvchilarning qonuniy vakillari (ota-onalar va vasiylar) tomonidan ta'lim natijalarini kuzatish maqsadida ishlatiladi. Ilova o'quvchilardan mustaqil ravishda shaxsiy ma'lumot yig'maydi. Barcha o'quvchi ma'lumotlari maktab va ta'lim muassasasi ma'muriyati tomonidan qonuniy asosda tizimga kiritiladi.

---

## 6. Ma'lumotlarni O'chirish Huquqi (Account & Data Deletion)
Foydalanuvchi istalgan vaqtda o'z hisobini va shaxsiy ma'lumotlarini o'chirishni talab qilish huquqiga ega:
- Ota-ona farzandining maktab ma'muriyatiga yozma yoki og'zaki murojaat qilish orqali tizimdagi hisobini faolsizlantirishi mumkin;
- Yoki bizning qo'llab-quvvatlash xizmatimizga murojaat qilish orqali: **support@farzandim-edu.uz**.
Murojaat 3 ish kuni ichida ko'rib chiqiladi va foydalanuvchining shaxsiy hisobi tizimdan o'chiriladi.

---

## 7. Biz Bilan Bog'lanish (Contact Information)
Maxfiylik siyosati yuzasidan savollar yoki takliflar bo'yicha biz bilan bog'lanishingiz mumkin:
- **Email**: support@farzandim-edu.uz
- **Veb-sayt**: https://farzandim-edu.uz
- **Tashkilot**: "Farzandim" Ta'lim Axborot Tizimi
