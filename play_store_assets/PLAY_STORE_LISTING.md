# Google Play Store Listing & Data Safety Guide
**Ilova nomi**: Farzandim: Ota-onalar portali  
**Paket nomi (Package Name)**: `uz.farzandim.parent`  
**Kategoriya**: Education (Ta'lim)  

---

## 1. Ilova Tavsiflari (App Metadata)

### O'zbek tili (Asosiy / Primary Language)
- **Ilova nomi (App Name - max 30 belgi)**:  
  `Farzandim: Ota-onalar portali`
- **Qisqa tavsif (Short Description - max 80 belgi)**:  
  `Farzandingizning baholari, dars jadvali va davomatini onlayn kuzatib boring.` (76 belgi)
- **To'liq tavsif (Full Description - max 4000 belgi)**:  
```
"Farzandim" — ota-onalar uchun mo'ljallangan zamonaviy ta'lim portali bo'lib, farzandingizning maktabdagi akademik o'zlashtirishini real vaqt rejimida kuzatish imkonini beradi.

ASOSIY IMKONIYaTLAR:

📘 Elektron Kundalik:
• Har bir dars uchun o'qituvchi qo'ygan baholarni real vaqtda ko'rish;
• Uy vazifalari va dars mavzularidan xabardor bo'lish;
• Ota-ona tomonidan "Ko'rdim ✓" tugmasi orqali baholarni rasmiy tasdiqlash.

📊 O'zlashtirish Dinamikasi va Tahlil:
• Fanlar kesimida o'rtacha ballar va choraklik ko'rsatkichlar;
• O'sish va pasayish tendentsiyalarini interaktiv grafiklar orqali ko'rish;
• Maktab va o'qituvchilarning izohlari bilan tanishish.

🔔 Davomat va Tartib-intizom:
• Farzandingizning darslarga qatnashishi (bor, kechikdi, sababli/sababsiz dars qoldirish);
• Xulq-atvor baholari va o'qituvchi sharhlari.

🔒 Xavfsizlik va Maxfiylik:
• 4 xonali shifrlangan PIN-kod yoki barmoq izi (Biometriya) orqali tezkor va xavfsiz kirish;
• Maktab ma'lumotlar bazalarining alohida ajratilganligi (Database-per-Tenant);
• Hech qanday reklama yo'q, shaxsiy ma'lumotlar uchinchi shaxslarga berilmaydi.

Farzandingiz kelajagi va mustahkam bilimi uchun "Farzandim" ilovasidan hoziroq foydalaning!
```

---

### Rus tili (Русский язык)
- **Название приложения (max 30 символов)**:  
  `Farzandim: Дневник Родителя`
- **Краткое описание (max 80 символов)**:  
  `Следите за оценками, расписанием и посещаемостью ребенка онлайн.` (64 символа)
- **Полное описание (max 4000 символов)**:  
```
"Farzandim" — удобный электронный дневник и образовательный портал для родителей. Следите за успехами и посещаемостью вашего ребенка в школе в реальном времени.

ОСНОВНЫЕ ВОЗМОЖНОСТИ:

📘 Электронный дневник:
• Мгновенный просмотр оценок за каждый урок;
• Темы занятий и домашние задания;
• Подтверждение родителем просмотра оценок («Ознакомлен ✓»).

📊 Аналитика успеваемости:
• Средний балл по каждому предмету и четвертные итоги;
• Наглядные графики динамики обучения;
• Заметки и рекомендации учителей.

🔔 Посещаемость и дисциплина:
• Учет присутствия, опозданий и пропусков уроков;
• Оценки за поведение на уроках.

🔒 Безопасность и конфиденциальность:
• Вход по PIN-коду или биометрии (отпечаток пальца);
• Полная защита персональных данных (TLS 1.3 шифрование);
• Без рекламы и передачи данных третьим лицам.
```

---

## 2. Google Play Data Safety (Xavfsizlik deklaratsiyasi bo'yicha javoblar)

Google Play Console'ning **App content -> Data safety** bo'limida quyidagicha to'ldiring:

| Savol | Javob | Izoh |
|-------|-------|------|
| **Does your app collect or share any of the required user data types?** | **Yes** | Foydalanuvchi hisobi va o'quvchi baholari |
| **Is all of the user data collected by your app encrypted in transit?** | **Yes** | Barcha so'rovlar HTTPS (TLS 1.3) orqali shifrlangan |
| **Do you provide a way for users to request that their data be deleted?** | **Yes** | Maktab ma'muriyati yoki support@farzandim-edu.uz orqali |
| **Is this app designed for children? (Target age)** | **18 and over (Parents only)** | Ilova ota-onalarga mo'ljallangan |
| **Does your app use Advertising ID?** | **No** | Reklama SDK yoki AD ID umuman mavjud emas |

### To'planadigan ma'lumot turlari:
1. **Personal info**:
   - *Name*: App functionality, Account management.
   - *User IDs (Passport / Student ID)*: App functionality, Account authentication.
2. **App info and performance**:
   - *Crash logs*: Analytics / Bug fixing (ixtiyoriy).
3. **Other data**:
   - *Academic Grades & Attendance*: App functionality (asosiy maqsad).
   - Hech bir ma'lumot uchinchi shaxslarga berilmaydi (*Data is not shared*).

---

## 3. Play Store Grafik Aktivlari (Tayyor fayllar)
Quyidagi aktivlar `play_store_assets/` papkasida tayyorlab qo'yilgan:
1. `icon_512x512.png` — 512×512 App Icon (32-bit PNG, no alpha canvas)
2. `feature_graphic_1024x500.png` — 1024×500 Feature Graphic (Google Play banner)
3. `screenshot_1_diary.png` — 1080×1920 Telefon skrinshoti (Kundalik)
4. `screenshot_2_dynamics.png` — 1080×1920 Telefon skrinshoti (Dinamika)
5. `screenshot_3_security.png` — 1080×1920 Telefon skrinshoti (Xavfsizlik)
