// Firebase Cloud Messaging Service Worker
importScripts('https://www.gstatic.com/firebasejs/10.7.1/firebase-app-compat.js');
importScripts('https://www.gstatic.com/firebasejs/10.7.1/firebase-messaging-compat.js');

// Firebase 설정 - 본인의 Firebase 프로젝트 정보로 변경하세요
firebase.initializeApp({
  apiKey: "AIzaSyB4zfupHNoGbDuCfDGbCz__aiFFEvQNhkM",
  authDomain: "today-wardrobe.firebaseapp.com",
  projectId: "today-wardrobe",
  storageBucket: "today-wardrobe.firebasestorage.app",
  messagingSenderId: "243188962870",
  appId: "1:243188962870:web:7930cc6d88872038b5ea97",

});

const messaging = firebase.messaging();

// 백그라운드 메시지 처리
messaging.onBackgroundMessage((payload) => {
    console.log('백그라운드 메시지 수신:', payload);
    
    const notificationTitle = payload.notification.title;
    const notificationOptions = {
        body: payload.notification.body,
        icon: payload.notification.icon || '/firebase-logo.png'
    };

    self.registration.showNotification(notificationTitle, notificationOptions);
});
