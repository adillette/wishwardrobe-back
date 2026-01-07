self.addEventListener('install', (event) => {
  console.log('[Service Worker] Installed');
  self.skipWaiting(); // 즉시 활성화
});

self.addEventListener('activate', (event) => {
  console.log('[Service Worker] Activated');
  event.waitUntil(clients.claim()); // 모든 클라이언트를 즉시 제어
});

self.addEventListener('push', (event) => {
  console.log('[Service Worker] Push received:', event);
  console.log('[Service Worker] Has data:', !!event.data);
  
  try {
    const data = event.data ? event.data.json() : {};
    console.log('[Service Worker] Parsed data:', data);
    
    event.waitUntil(
      self.registration.showNotification(data.title ?? '알림', {
        body: data.message ?? '',
        icon: data.icon ?? '/icon.png',
        data: { clickAction: data.clickAction },
        tag: 'webpush-notification',
        requireInteraction: false
      }).then(() => {
        console.log('[Service Worker] Notification displayed successfully');
      }).catch(err => {
        console.error('[Service Worker] Failed to show notification:', err);
      })
    );
  } catch (error) {
    console.error('[Service Worker] Error handling push:', error);
  }
});

self.addEventListener('notificationclick', (event) => {
  console.log('[Service Worker] Notification clicked');
  event.notification.close();
  const url = event.notification.data?.clickAction;
  if(url) event.waitUntil(clients.openWindow(url));
});