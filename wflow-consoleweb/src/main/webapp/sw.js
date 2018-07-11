var version = "0.0.1";
var cacheName = "jw-sw-demo";
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

    try {
        var json = event.data.json(); // {"title":"Title", "text":"Text", "url":"/jw/web/userview/appcenter/v/_/home"}
        text = json.text;
        title = json.title;
        url = json.url;
    } catch(e) {
        text = (event && event.data) ? event.data.text() : "Push notification received."
        title = 'Push Notification';
        url = "/jw";
    }
    var options = {
        body: text,
        icon: '/jw/images/v3/logo.png',
        badge: '/jw/images/v3/logo.png',
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
