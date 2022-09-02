var version = "7.0.0";
var cacheName = "jw-cache";
var contextPath = '%s';
var appUserviewId = '%s';
var userviewKey = '_';
var homePageLink = '';
var cache = appUserviewId + "-" + version;
var urlsToCache = [
    contextPath + '/css/v7.css',
    contextPath + '/css/console_custom.css',
    contextPath + '/css/datalistBuilderView.css',
    contextPath + '/js/fontawesome5/css/all.min.css',
    contextPath + '/js/fontawesome5/fonts/fontawesome-webfont.ttf',
    contextPath + '/js/fontawesome5/fonts/fontawesome-webfont.woff2',
    contextPath + '/js/fontawesome5/webfonts/fa-brands-400.ttf',
    contextPath + '/js/fontawesome5/webfonts/fa-brands-400.woff2',
    contextPath + '/js/fontawesome5/webfonts/fa-regular-400.ttf',
    contextPath + '/js/fontawesome5/webfonts/fa-regular-400.woff2',
    contextPath + '/js/fontawesome5/webfonts/fa-solid-900.ttf',
    contextPath + '/js/fontawesome5/webfonts/fa-solid-900.woff2',
    contextPath + '/home/logo.png',
    contextPath + '/js/footable/footable.core.min.css',
    contextPath + '/js/footable/footable.min.js',
    contextPath + '/js/footable/responsiveTable.js',
    contextPath + '/js/footable/fonts/footable.ttf',
    contextPath + '/js/footable/fonts/footable.woff',
    %s
];
var template = '%s';

var ROLE_ANONYMOUS = 'roleAnonymous';

var indexedDBVersion = 1;

var isSyncing = false;

//from pwa.js
var formUsername = null;
var formUserviewAppId = null;
var formPageTitle = null;
var formData = null;

var formDb = null;

var FORM_DB_NAME         = 'joget' + '_' + appUserviewId;
var CACHE_DB_NAME         = 'joget-shared-cache';
var FORM_DB_STORE_NAME   = 'offline_post';
var CACHE_DB_STORE_NAME   = 'cache_data';
var STATUS_PENDING       = 0;
var STATUS_SUCCESS       = 1;
var STATUS_FAILED        = 2;
var STATUS_FORM_ERROR    = 3;

var path = null;
function getPath() { // for offline redirection & cache key to use back non embed url
    if (path === null) {
        path = self.registration.scope;
        if (path.indexOf('/web/embed/userview') !== -1) {
            path = path.replace('/web/embed/userview', '/web/userview');
        }
    }
    return path;
}

function cacheUserview(){
    //const cacheApi = fetchRequest.url.substring(0, fetchRequest.url.lastIndexOf("/")) + "/cacheUrls";
    const cacheApi = getPath() + '/' + userviewKey + "/cacheUrls";
    console.log("Retrieve urls to cache by API (" + cacheApi + ")");
    fetch(cacheApi, {  
        credentials: 'include'  
    })
    .then(function(response) {
        if (response.status !== 200) {
            console.log("Not able to retrieve cache URLs");
            return;
        }
        response.json().then(function(data) {
            data = data.app;
            
            caches.open(cache)
            .then(function (cache) {
                var promises = [];

                data.push(getPath() + '/_/pwaoffline');
                data.push(getPath() + '/_/offline');
                data.push(homePageLink);
               
                promises.push(
                    //cache one by one to prevent duplicate url causing DOMexception
                    data.map(function(url) {
                        return caches.match(url).then(function(checkCache){
                            //always re-cache pwaoffline and offline in case homePageLink changes (eg. after login)
                            var addToCache = false;

                            if(checkCache === undefined || url.indexOf('/pwaoffline') > -1 || url.indexOf('/offline') > -1){
                                addToCache = true;
                            }

                            if(addToCache){
                                cache.addAll([url]).then(function() {
                                    //console.log(url + " cached");
                                }).catch(function(err) {
                                    //ignore
                                });
                            }
                        })
                    })
                )
                
                return Promise.all(promises).then(function() {
                    console.log("URLs retrieved from API cached");
                });
            })
            .catch(function(error){
                console.log('error caching', error, error.message);
            });
        });
    })
    .catch(function(err) {
        console.log("Not able to retrieve cache URLs", err);
    });
}

self.addEventListener('install', function (event) {
    console.log('SW install event');
    self.skipWaiting();
    event.waitUntil(
        caches.delete(cache)
            .then(function(){
                caches.open(cache)
                    .then(function (cache) {
                        var promises = [];

                        urlsToCache.push(getPath() + '/_/pwaoffline');
                        urlsToCache.push(getPath() + '/_/offline');
                        promises.push(
                            //cache one by one to prevent duplicate url causing DOMexception
                            urlsToCache.map(function(url) {
                                return caches.match(url).then(function(checkCache){
                                    if(checkCache === undefined){
                                        cache.addAll([url]).then(function() {
                                            //console.log(url + " cached");
                                        }).catch(function(err) {
                                            //ignore
                                        });
                                    }else{
                                        //console.log(url + ' already exists in cache');
                                    }
                                })
                            })
                        );

                        return Promise.all(promises);
                    })
            })

                
    );
});

self.addEventListener('fetch', function (event) {
    //https://stackoverflow.com/questions/48463483/what-causes-a-failed-to-execute-fetch-on-serviceworkerglobalscope-only-if
    if (event.request.cache === 'only-if-cached' && event.request.mode !== 'same-origin') {
        return;
    }

    var fetchRequest = event.request.clone();

    event.respondWith(
        fetch(fetchRequest)
        .then(function (response) {
            //redirect links eg. /jw/home, userview root url, are of response.type 'opaqueredirect', and possibly response.status != 200
            //it is generally not a good idea to cache redirection because the content (target of redirection) might be changed
            //https://medium.com/@boopathi/service-workers-gotchas-44bec65eab3f
            //but without this the top-right home button (/jw/home) will never be cached and will always show offline page when being accessed offline
            //if (!response || response.status !== 200 || response.type !== 'basic' || event.request.method !== 'GET') {
            if (!response || event.request.method !== 'GET') {
                return response;

            } else {
                if(fetchRequest.url.indexOf('/web/json/workflow/currentUsername') === -1 
                        && fetchRequest.url.indexOf('/images/v3/cj.gif') === -1
                        && fetchRequest.url.indexOf('/images/favicon_uv.ico?m=testconnection') === -1){
                    var responseToCache = response.clone();
                    caches.open(cache)
                        .then(function (cache) {
                            cache.put(event.request, responseToCache);
                        });
                }
            }
            
            return response;
        })
        .catch(function () {
            if(event.request.method === 'POST' && formData !== null){
                console.log('form POST failed, saving to indexedDB');
                
                savePostRequest(event.request.clone().url, formUserviewAppId, formPageTitle, formData, formUsername);

                //redirect instead
                var response = Response.redirect(getPath() + '/_/pwaoffline', 302);
                return response;

            }else{
                if(fetchRequest.url.indexOf('/images/favicon_uv.ico?m=testconnection') === -1){
                    return new Promise(function(resolve, reject) {
                        caches.match(fetchRequest.url, {ignoreVary: true}).then(async function(response){
                            if(response === undefined){
                                var offlineResponse = Response.redirect(self.registration.scope + '/_/offline', 302);
                                resolve(offlineResponse);
                            }else{
                                if (template && template !== "") {
                                    var isAjaxTheme = event.request.headers.get('__ajax_theme_loading');
                                    if (isAjaxTheme === undefined || isAjaxTheme === null) {
                                        var responseText = await response.clone().text();
                                        var menuStartIndex = responseText.indexOf("ajaxtheme_loading_menus");
                                        if (menuStartIndex !== -1) {
                                            var titleStartIndex = responseText.indexOf("ajaxtheme_loading_title");
                                            var contentStartIndex = responseText.indexOf("ajaxtheme_loading_content");
                                            var title = responseText.substring(titleStartIndex + 34, menuStartIndex - 25);
                                            var menus = responseText.substring(menuStartIndex + 25, contentStartIndex - 27);
                                            var content = responseText.substring(contentStartIndex + 27, responseText.length - 40);

                                            responseText = template.replace('{{TEMPLATE_TITLE}}', title);
                                            responseText = responseText.replace('{{TEMPLATE_CONTENT}}', content);
                                            responseText = responseText.replace('{{TEMPLATE_MENUS}}', menus);

                                            response = new Response(responseText, response);
                                        }
                                    }
                                }
                                resolve(response);
                            }
                        })
                    }); 
                }
            }
        })
    );
});

self.addEventListener('activate', function (event) {
    console.log('SW activate event');
    self.clients.claim();
});

self.addEventListener('push', function (event) {
    console.log('Service worker push received.');

    var text;
    var title;
    var url;
    var icon;
    var badge;

    try {
        var json = event.data.json(); // {"title":"Title", "text":"Text", "url":contextPath + "/web/userview/appcenter/v/_/home"}
        text = json.text;
        title = json.title;
        url = json.url;
        icon = json.icon;
        badge = json.badge;
    } catch (e) {
        text = (event && event.data) ? event.data.text() : "Push notification received.";
    }
    if (!text || typeof text === "undefined") {
        text = 'Push notification received.';
    }
    if (typeof title === "undefined") {
        title = 'Push Notification';
    }
    if (typeof url === "undefined") {
        url = contextPath + '/';
    }
    if (typeof icon === "undefined") {
        icon = contextPath + '/images/v3/logo.png';
    }
    if (typeof badge === "undefined") {
        badge = contextPath + '/images/v3/logo.png';
    }
    
    event.waitUntil(new Promise(function(resolve, reject) {
        connectCacheDB(function(store){
            var request = store.get("serviceWorkerList");
            request.onsuccess = function(){
                var serviceWorkerList = this.result.serviceWorkerList;

                var options = {
                    body: text,
                    icon: icon,
                    badge: badge,
                    data: {
                        url: url
                    }
                };

                var show = false;
                if (serviceWorkerList.length <= 1) {
                    show = true;
                } else if (url.indexOf('/web/userview/') !== -1) {
                    if (url.indexOf(appUserviewId.replace('-', '/')) !== -1) {
                        show = true;
                    }
                }

                if (!show) {
                    var found = false;
                    for (let i = 0; i < serviceWorkerList.length; i++) {
                        const sw = serviceWorkerList[i];
                        if (url.indexOf(sw) !== -1) {
                            found = true;
                            break;
                        }
                    }
                    if (!found && serviceWorkerList[0].indexOf(appUserviewId.replace('-', '/')) !== -1) {
                        show = true; //can't found the service worker for current url, use the first 1 to show
                    }
                }

                if (show) {
                    var notification = self.registration.showNotification(title, options);
                    
                    notification.then(function(result) {
                        resolve(result);
                    }, function(err) {
                        reject(err);
                    });
                } else {
                    reject("");
                }
            };
        }, 'readonly');    
    }));
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

function openDatabase() {
    var indexedDBOpenRequest = indexedDB.open(FORM_DB_NAME, indexedDBVersion);

    indexedDBOpenRequest.onerror = function (error) {
        console.error('IndexedDB error:', error);
    }

    indexedDBOpenRequest.onupgradeneeded = function () {
        this.result.createObjectStore(FORM_DB_STORE_NAME, {
            autoIncrement: true,
            keyPath: 'id'
        });
    }

    indexedDBOpenRequest.onsuccess = function () {
        formDb = this.result;
    }
}

function getObjectStore(storeName, mode) {
    return formDb.transaction(storeName, mode).objectStore(storeName);
}

function replaceUrlParam(url, paramName, paramValue){
    if (paramValue === null) {
        paramValue = '';
    }
    var pattern = new RegExp('\\b(' + paramName + '=).*?(&|#|$)');
    if (url.search(pattern) >= 0) {
        return url.replace(pattern, '$1' + paramValue + '$2');
    }
    url = url.replace(/[?#]$/, '');
    return url + (url.indexOf('?') > 0 ? '&' : '?') + paramName + '=' + paramValue;
}

function postMessageToClients(msgObj){
    self.clients.matchAll().then(function(clients) {
        clients.forEach(function(client) {
            client.postMessage(msgObj)
         })
    });
}

function getCurrentUsername(){
    return new Promise(function(resolve, reject) {
        fetch(contextPath + '/web/json/workflow/currentUsername', {
            method: 'GET'
        }).then(function (response) {
            response.json()
                .then(function(json){
                    resolve(json);
                })
                .catch(function(error){
                    reject(error);
                })
        }).catch(function (error) {
            console.error('get current username failed:', error);
            reject(error);
        })
    });
}

function getCsrfToken(){
    return new Promise(function(resolve, reject) {
        fetch(contextPath + '/web/json/directory/user/csrfToken', {
            method: 'GET'
        }).then(function (response) {
            response.json()
                .then(function(json){
                    resolve(json);
                })
                .catch(function(error){
                    reject(error);
                })
        }).catch(function (error) {
            console.error('get CSRF token failed:', error);
            reject(error);
        })
    });
}

function savePostRequest(url, userviewAppId, title, payload, username) {
    var request = getObjectStore(FORM_DB_STORE_NAME, 'readwrite').add({
        url: url,
        userviewAppId: userviewAppId,
        username: username,
        title: title,
        payload: payload,
        timestamp: new Date().getTime(),
        status: STATUS_PENDING,
        method: 'POST'
    });
    
    request.onsuccess = function (event) {
        console.log('a new record has been added to indexedb');
        formData = null;
    }

    request.onerror = function (error) {
        console.error(error)
    }
}

function sendFormDataToServer(savedRequest){
    console.log('sendFormDataToServer', savedRequest);
    
    return new Promise(function(resolve, reject) {
        if(savedRequest.status === STATUS_FORM_ERROR){
            reject();
            return;
        }

        getCsrfToken()
            .then(function(json){
                var requestUrl = savedRequest.url;
                var payload = savedRequest.payload;

                var method = savedRequest.method;

                var formDataObj = new FormData();

                var keysToIgnore = [];
                for(var key in payload){
                    if((Array.isArray(payload[key]) && payload[key][0] instanceof File)
                            || payload[key] instanceof File){
                        //check if {key}_path exists
                        if(payload[key + '_path'] !== undefined){
                            keysToIgnore.push(key + '_path');
                        }
                    }else{
                    }
                }


                for(var key in payload){
                    if(keysToIgnore.indexOf(key) > -1){
                        continue;
                    }

                    if(key === 'OWASP_CSRFTOKEN'){
                        //ignore
                    }else{
                        //check if File array
                        //if(Array.isArray(payload[key]) && payload[key][0] instanceof File){
                        if(Array.isArray(payload[key])){
                            for(var i in payload[key]){
                                formDataObj.append(key, payload[key][i]);
                            }
                        }else{
                            formDataObj.append(key, payload[key]);
                        }
                    }
                }
                formDataObj.append('OWASP_CSRFTOKEN', json.tokenValue);

                fetch(requestUrl, {
                    headers: {
                        //'Content-Type': 'application/x-www-form-urlencoded'
                    },
                    method: method,
                    body: formDataObj,
                }).then(function (response) {
                    if (response.status < 400) {
                        //check if form validation error {name="_FORM_ERRORS"}
                        response.text()
                            .then(function(text){
                                if(text.indexOf('name="_FORM_ERRORS"') > -1){
                                    var textIndex = text.indexOf('name="_FORM_ERRORS"');
                                    var excerpt = text.substring(textIndex - 120, textIndex);
                                    console.log('form validation error', excerpt);

                                    savedRequest.status = STATUS_FORM_ERROR;
                                    getObjectStore(FORM_DB_STORE_NAME, 'readwrite').put(savedRequest);
                                    
                                    reject();
                                    
                                }else{
                                    getObjectStore(FORM_DB_STORE_NAME, 'readwrite').delete(savedRequest.id);
                                    
                                    resolve();
                                }
                            })
                            .catch(function(error){
                                reject(error);
                            });
                    }else{
                        savedRequest.status = STATUS_FAILED;
                        getObjectStore(FORM_DB_STORE_NAME, 'readwrite').put(savedRequest);
                        
                        reject();
                    }
                }).catch(function (error) {
                    console.error('Send to Server failed:', error);

                    savedRequest.status = STATUS_FAILED;
                    getObjectStore(FORM_DB_STORE_NAME, 'readwrite').put(savedRequest);

                    reject(error);
                })
            })
    });
    
        
}

function processStoredFormData() {
    if(isSyncing){
        //console.log('processStoredFormData is syncing, ignoring...');
        return;
    }
    
    isSyncing = true;

    //delay to make sure already online
    setTimeout(function(){
        getCurrentUsername()
            .then(function(json){
                var currentUsername = json.username;

                var otherUsernameFound = false;

                var savedRequests = [];
                var req = getObjectStore(FORM_DB_STORE_NAME).openCursor();

                req.onsuccess = async function(event) {
                    var cursor = event.target.result;
                    
                    if (cursor) {
                        savedRequests.push(cursor.value);
                        cursor.continue();

                    } else {

                        if(savedRequests.length > 0){
                            //show syncing notification
                            postMessageToClients({
                                type: 'syncing'
                            });
                        }

                        //see if any records not matching the current username
                        for(let savedRequest of savedRequests) {
                            if(savedRequest.username !== currentUsername){
                                otherUsernameFound = true;

                                if(currentUsername !== ROLE_ANONYMOUS){
                                    console.log('username is different, deleting...', savedRequest, savedRequest.username);
                                    getObjectStore(FORM_DB_STORE_NAME, 'readwrite').delete(savedRequest.id);
                                }
                            }
                        }

                        if(otherUsernameFound){
                            if(currentUsername === ROLE_ANONYMOUS){
                                //show login prompt
                                postMessageToClients({
                                    type: 'login'
                                })
                            }

                            isSyncing = false;

                        }else{
                            //proceed to send the data
                            var promises = [];
                            
                            for(let savedRequest of savedRequests) {
                                /*
                                if(savedRequest.status !== STATUS_FORM_ERROR){
                                    promises.push(sendFormDataToServer(savedRequest));
                                }
                                */
                                promises.push(sendFormDataToServer(savedRequest));
                            }
                            
                            Promise.allSettled(promises).then(function(results){
                                var hasFailedRequest = false;
                                
                                results.forEach(function(result){
                                    if(result.status === 'rejected'){
                                        hasFailedRequest = true;
                                    }
                                });
                                
                                if(hasFailedRequest){
                                    //show syncFailed notification
                                    postMessageToClients({
                                        type: 'syncFailed'
                                    })
                                }else{
                                    if(promises.length > 0){
                                        //show syncSuccess notification
                                        postMessageToClients({
                                            type: 'syncSuccess'
                                        })
                                    }
                                }
                                
                                isSyncing = false;
                            });

                            //console.log('setting isSyncing to false');
                            //isSyncing = false;
                        }
                    }
                }
            })
            .catch(function(error){
                isSyncing = false;
                console.log('error getting username', error);
            });
    }, 10000);
        
}

function connectCacheDB(f, mode) {
    var request = indexedDB.open(CACHE_DB_NAME, 1);
    request.onerror = function (error) {
        console.error('IndexedDB error:', error);
    };
    request.onsuccess = function(){
        var db = request.result;
        var store = db.transaction(CACHE_DB_STORE_NAME, mode).objectStore(CACHE_DB_STORE_NAME);
        f(store);
    };
    request.onupgradeneeded = function(e){
        var db = e.currentTarget.result;
        
        if(!db.objectStoreNames.contains(CACHE_DB_STORE_NAME)) {
            db.createObjectStore(CACHE_DB_STORE_NAME, {keyPath: "name"});  
        }
        connectCacheDB(f, mode);
    };
}

self.addEventListener('message', function(event) {
    if (event.data.hasOwnProperty('sync')) {
        console.log('sync received');
        processStoredFormData();
    }
    
    if (event.data.hasOwnProperty('userviewKey')) {
        console.log('userviewKey received');
        userviewKey = event.data.userviewKey;
        homePageLink = event.data.homePageLink;
        cacheUserview();
    }
    
    if (event.data.hasOwnProperty('formData')) {
        console.log('formData received');
        formPageTitle = event.data.formPageTitle;
        formData = event.data.formData;
        formUserviewAppId = event.data.formUserviewAppId;
        formUsername = event.data.formUsername;
    }
    
    if (event.data.hasOwnProperty('serviceWorkerList')) {
        console.log("serviceWorkerList received");
        connectCacheDB(function(store){
            store.put({
                name: "serviceWorkerList",
                serviceWorkerList: event.data.serviceWorkerList
            });
        }, 'readwrite');
    }
});


self.addEventListener('sync', function(event) {
    console.log('syncing...');
    //event.tag name checked here must be the same as the one used while registering sync
    if (event.tag === 'sendFormData') {
        event.waitUntil(
            processStoredFormData()
        )
    }
})

openDatabase();
