# UnipiAudioStories & UnipiPliShopping

![Android](https://img.shields.io/badge/Android-3DDC84?logo=android&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-FFCA28?logo=firebase&logoColor=black)

Δύο εφαρμογές Android που αναπτύχθηκαν με χρήση **Android Studio** και **Firebase**. 

## Πίνακας Περιεχομένων

- [UnipiAudioStories](#unipiaudiostories)
  - [Λειτουργικότητα](#λειτουργικότητα-unipiaudiostories)
  - [Τεχνολογίες](#τεχνολογίες-unipiaudiostories)
- [UnipiPliShopping](#unipiplishopping)
  - [Λειτουργικότητα](#λειτουργικότητα-unipiplishopping)
  - [Τεχνολογίες](#τεχνολογίες-unipiplishopping)
- [Εγκατάσταση](#εγκατάσταση)
- [Άδεια Χρήσης](#άδεια-χρήσης)

---

## UnipiAudioStories

### Λειτουργικότητα UnipiAudioStories

- Προβολή λίστας ηχητικών ιστοριών με εξώφυλλα
- Αναπαραγωγή ήχου για κάθε ιστορία
- Προβολή κειμένου ιστορίας
- Επιλογή γλώσσας εφαρμογής (drop-down μενού)
- Λίστα αγαπημένων ιστοριών

### Τεχνολογίες UnipiAudioStories

- Android Studio (Target SDK: 35, Min SDK: 29)
- Firebase Realtime Database
- MediaPlayer API για αναπαραγωγή ήχου
- RecyclerView για δυναμική λίστα

---

## UnipiPliShopping

### Λειτουργικότητα UnipiPliShopping

- Σύνδεση χρήστη μέσω **Google Sign-In (OAuth2)** ή email/κωδικού
- Εγγραφή νέου χρήστη
- Προβολή διαθέσιμων προϊόντων με φίλτρα
- Διαχείριση καλαθιού αγορών (προσθήκη/αφαίρεση/επεξεργασία ποσότητας)
- Geolocation ειδοποιήσεων για κοντινά καταστήματα (100μ ακτίνα)
- Ρυθμίσεις:
  - Dark/Light theme
  - Επιλογή γλώσσας
  - Ενεργοποίηση geofencing

### Τεχνολογίες UnipiPliShopping

- Firebase Authentication (Google Sign-In)
- Firebase Firestore
- Firebase Cloud Messaging
- Android Location Services
- WorkManager για background εργασίες

---

## Εγκατάσταση

1. **Κλωνοποίηση repository**:
   ```bash
   git clone [repository-url]
   ```
   
2. **Ανοίξτε το έργο σε Android Studio**:
   - Χρησιμοποιήστε Android Studio Arctic Fox ή νεότερη
   - Συγχρονίστε το έργο με Gradle

3. **Εκτέλεση σε emulator ή συσκευή**:
   - Min SDK: Android 10 (API 29)
   - Target SDK: Android 15 (API 35)

---

## Άδεια Χρήσης

Αυτό το έργο διανέμεται υπό την άδεια [MIT](LICENSE).
``` 

> **Σημείωση:**  
> - Προσαρμόστε τα URLs και τις διαδρομές των εικόνων ανάλογα με τη δομή του αποθετηρίου σας
> - Προσθέστε πραγματικά screenshots στον φάκελο `/screenshots`
> - Συμπληρώστε το αρχείο LICENSE αν χρησιμοποιείται συγκεκριμένη άδεια
