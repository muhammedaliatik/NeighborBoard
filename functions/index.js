const functions = require('firebase-functions/v1');
const admin = require('firebase-admin');
admin.initializeApp();

// Java kodundaki veritabanı yoluna tam uyumlu tetikleyici:
exports.otomatikDuyuruBildirimi = functions.database.ref('/announcements/{aptCode}/{announcementId}')
    .onCreate((snapshot, context) => {


        const duyuru = snapshot.val();

        // URL'den (yoldan) apartman kodunu çekiyoruz
        const aptCode = context.params.aptCode;

        if (!duyuru || !aptCode) {
            console.log("Duyuru verisi veya apartman kodu eksik, bildirim atlanıyor.");
            return null;
        }

        // Android'de (MainActivity'de) abone olurken kullandığımız topic ismini oluşturuyoruz
        const temizAptCode = aptCode.toString().replace(/\s+/g, '').toLowerCase();
        const topicName = "apartman_" + temizAptCode;

       // Bildirim içeriği: Gönderen kişinin adını başlık, duyuru başlığını içerik yapıyoruz
               const payload = {
                   notification: {
                       title: duyuru.senderName + " yeni bir duyuru paylaştı",
                       body: duyuru.title
                   },
                   // BUNU KESİNLİKLE EKLEMEN LAZIM:
                   android: {
                       priority: "high",
                       notification: {
                           channelId: "duyuru_kanali", // DİKKAT: Android Studio'da oluşturduğun ID neyse tam olarak o yazmalı!
                           sound: "default"
                       }
                   },
                   topic: topicName
               };

        // Bildirimi o apartman grubuna fırlat
        return admin.messaging().send(payload)
            .then((response) => {
                console.log(topicName + " apartmanına bildirim başarıyla gitti:", response);
                return null;
            })
            .catch((error) => {
                console.error("Bildirim gönderilirken hata oluştu:", error);
                return null;
            });
    });