var version = "7.0.0";
var cacheName = "jw-cache";
var cache = cacheName + "-" + version;
var urlsToCache = [];

self.addEventListener('install', function(event) {
    console.log('base SW install event');
    self.skipWaiting();
    event.waitUntil(
        caches.delete(cache)
            .then(function(){
                caches.open(cache)
                    .then(function(cacheObj) {
                        //scope should already end with a '/'
                        urlsToCache.push(self.registration.scope + 'home/style.css');
                        urlsToCache.push(self.registration.scope + 'home/logo.png');
                        urlsToCache.push(self.registration.scope + 'images/v3/joget.ico');
                        urlsToCache.push(self.registration.scope + 'web/offline');

                        //cache one by one to prevent duplicate url causing DOMexception
                        return urlsToCache.map(function(url) {
                            return caches.match(url).then(function(checkCache){
                                if(checkCache === undefined){
                                    cacheObj.addAll([url]).then(function() {
                                        //console.log("base SW " + url + " cached");
                                    }).catch(function(err) {
                                        //ignore
                                    });
                                }else{
                                    //console.log(url + ' already exists in cache');
                                }
                            })
                        })
                    })
            })
                
    );
});

self.addEventListener('fetch', function(event) {
    // ignore web console and presence URLs
    if (event.request.url.indexOf('/web/console/') > 0 || event.request.url.indexOf('/web/json/console/') > 0 || event.request.url.indexOf('/web/presence') > 0) {
        return;
    }
    
    var fetchRequest = event.request.clone();

    event.respondWith(
        fetch(fetchRequest)
        .then(
            function(response) {
                //redirect links eg. /jw/home, userview root url, are of response.type 'opaqueredirect', and possibly response.status != 200
                //it is generally not a good idea to cache redirection because the content (target of redirection) might be changed
                //https://medium.com/@boopathi/service-workers-gotchas-44bec65eab3f
                //but without this the top-right home button (/jw/home) will never be cached and will always show offline page when being accessed offline
                //if (!response || response.status !== 200 || response.type !== 'basic' || event.request.method !== 'GET') {
                if (!response || event.request.method !== 'GET') {
                    return response;
                } else {
                    var responseToCache = response.clone();
                    caches.open(cache)
                        .then(function(cache) {
                            cache.put(event.request, responseToCache);
                        });
                }

                return response;
            }
        ).catch(function() {
            return new Promise(function(resolve, reject) {
                caches.match(event.request).then(function(response){
                    if(response === undefined){
                        caches.match(self.registration.scope + 'web/offline').then(function(offlineResponse){
                            resolve(offlineResponse);
                        });
                    }else{
                        resolve(response);
                    }
                })
            });  
        })
    );
});

self.addEventListener('activate', function(event) {
    console.log('base SW activate event');
    self.clients.claim();
});