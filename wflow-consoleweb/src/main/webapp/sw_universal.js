var version = "7.0.0";
var cacheName = "jw-cache";
var cache = cacheName + "-" + version;
var urlsToCache = [
    './',
    './wro/common.css',
    './wro/universal.preload.min.css',
    './wro/universal.min.css',
    './js/font-awesome4/css/font-awesome.min.css',
    './wro/common.js',
    './wro/universal.preload.min.js',
    './wro/universal.min.js',
];


self.addEventListener('install', function (event) {
    event.waitUntil(
            caches.open(cache)
            .then(function (cache) {
                return cache.addAll(urlsToCache);
            })
            );
});

self.addEventListener('fetch', function (event) {
    var fetchRequest = event.request.clone();

    event.respondWith(
            fetch(fetchRequest)
            .then(
                    function (response) {
                        if (!response || response.status !== 200 || response.type !== 'basic' || event.request.method !== 'GET') {
                            return response;
                        } else {
                            var responseToCache = response.clone();
                            caches.open(cache)
                                    .then(function (cache) {
                                        cache.put(event.request, responseToCache);
                                    });
                        }

                        return response;
                    }
            ).catch(function () {
        return caches.match(event.request);
    })
            );
});

self.addEventListener('activate', function (event) {
    event.waitUntil(
            caches.keys()
            .then(
                    function (keyList) {
                        Promise.all(
                                keyList.map(function (key) {
                                    if (cacheName.indexOf(key) === -1) {
                                        return caches.delete(key);
                                    }
                                })
                                );
                    })
            );
});

self.addEventListener('push', function (event) {
    console.log('Service worker push received.');

    var text;
    var title;
    var url;
    var icon;
    var badge;

    try {
        var json = event.data.json(); // {"title":"Title", "text":"Text", "url":"/jw/web/userview/appcenter/v/_/home"}
        text = json.text;
        title = json.title;
        url = json.url;
        icon = json.icon;
        badge = json.badge;
    } catch (e) {
        text = (event && event.data) ? event.data.text() : "Push notification received."
    }
    if (!text || typeof text === "undefined") {
        text = 'Push notification received.';
    }
    if (typeof title === "undefined") {
        title = 'Push Notification';
    }
    if (typeof url === "undefined") {
        url = '/jw/';
    }
    if (typeof icon === "undefined") {
        icon = '/jw/images/v3/logo.png';
    }
    if (typeof badge === "undefined") {
        badge = '/jw/images/v3/logo.png';
    }
    var options = {
        body: text,
        icon: icon,
        badge: badge,
        data: {
            url: url
        }
    };

    event.waitUntil(self.registration.showNotification(title, options));
});

self.addEventListener('notificationclick', function (event) {
    console.log('Service worker notification click received.');

    event.notification.close();

    const newUrl = event.notification.data.url || '';
    const urlToOpen = new URL(newUrl, self.location.origin).href;

    const promiseChain = clients.matchAll({
        type: 'window',
        includeUncontrolled: true
    })
            .then((windowClients) => {
                let matchingClient = null;

                for (let i = 0; i < windowClients.length; i++) {
                    const windowClient = windowClients[i];
                    if (windowClient.url === urlToOpen) {
                        matchingClient = windowClient;
                        break;
                    }
                }

                if (matchingClient) {
                    return matchingClient.focus();
                } else {
                    return clients.openWindow(urlToOpen);
                }
            });

    event.waitUntil(promiseChain);

});
