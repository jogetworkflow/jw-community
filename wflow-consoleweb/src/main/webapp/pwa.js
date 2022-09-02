PwaUtil = {

    applicationServerPublicKey: "BE54-RlSdVqGwlh_skZ4qQqP1tY7uNZrQbv3IJ_Rd2uRHsId8XjvH2CXav_5PkhrM1XvBLXJyi7tx6io5E3fegg",

    contextPath: "/jw",

    userviewKey: "_",

    homePageLink: "",

    serviceWorkerPath: "/jw/sw.js",

    subscriptionApiPath: "/jw/web/console/profile/subscription",

    pushEnabled: true,

    currentUsername: '',

    onlineNotificationMessage: '',

    offlineNotificationMessage: '',

    loginPromptMessage: '',

    syncingMessage: '',

    syncFailedMessage: '',

    syncSuccessMessage: '',

    isEmbedded: false,

    isOnline: null,
    greenColor: '#3c763d',
    redColor: '#a94442',
    darkColor : '#2d2d2d',

    init: function () {
        Offline.options = {
            checkOnLoad: false,
            checks: {
                xhr: {
                    url: PwaUtil.contextPath + '/images/favicon_uv.ico?m=testconnection&t=' + new Date().getTime(),
                }
            },
            interceptRequests: false,
            requests: false,
            reconnect: false
        };

        if (!navigator.serviceWorker) {
            return;
        }

        $(document).ready(function(){

            /*
             window.addEventListener('online', PwaUtil.handleConnection);
             window.addEventListener('offline', PwaUtil.handleConnection);
             
             //some browsers doesn't fire 'online' and 'offline' events reliably
             setInterval(PwaUtil.handleConnection, 5000);
             
             PwaUtil.handleConnection();
             */

            $("form").submit(function (e) {
                PwaUtil.submitForm(this);
            });

            $('form input[type=submit]').click(function () {
                $('input[type=submit]', $(this).parents('form')).removeAttr('clicked');
                $(this).attr('clicked', 'true');
            });

            navigator.serviceWorker.addEventListener('message', function(event) {
                if(event.data.type !== undefined && event.data.type === 'login'){
                    PwaUtil.showToast(PwaUtil.loginPromptMessage, PwaUtil.darkColor, 'white');
                }

                if(event.data.type !== undefined && event.data.type === 'syncing'){
                    PwaUtil.showToast(PwaUtil.syncingMessage, PwaUtil.darkColor, 'white');
                }

                if(event.data.type !== undefined && event.data.type === 'syncFailed'){
                    PwaUtil.showToast(PwaUtil.syncFailedMessage, PwaUtil.redColor, 'white');
                    PwaUtil.showOfflineIndicator();
                }

                if(event.data.type !== undefined && event.data.type === 'syncSuccess'){
                    PwaUtil.showToast(PwaUtil.syncSuccessMessage, PwaUtil.greenColor, 'white');
                    PwaUtil.hideOfflineIndicator();
                }
            });

            Offline.options = {
                checkOnLoad: false,
                checks: {
                    xhr: {
                        url: PwaUtil.contextPath + '/images/favicon_uv.ico?m=testconnection&t=' + new Date().getTime(),
                    }
                },
                interceptRequests: true,
                requests: false,
                reconnect: {
                    initialDelay: 5,
                    delay: 5
                }
            };
            Offline.check();
            Offline.on('confirmed-down', function () {
                if(PwaUtil.isOnline === true){
                    PwaUtil.showToast(PwaUtil.offlineNotificationMessage, PwaUtil.redColor, 'white');
                }
                PwaUtil.showOfflineIndicator();
                PwaUtil.isOnline = false;
            });

            Offline.on('confirmed-up', function () {
                if(PwaUtil.isOnline === false){
                    PwaUtil.showToast(PwaUtil.onlineNotificationMessage, PwaUtil.greenColor, 'white');

                    navigator.serviceWorker.controller && navigator.serviceWorker.controller.postMessage({
                        sync: true
                    });
                }
                PwaUtil.hideOfflineIndicator();
                PwaUtil.isOnline = true;
            });
        });
    },

    submitForm: function (form) {
        var formData = $(form).serializeObject();

        var $submitButton = $(form).find('input[type=submit][clicked=true]');
        formData[$submitButton.attr('name')] = $submitButton.val();
        $(form).find('input[type=submit]').removeAttr("clicked");

        $(form).find('input[type=file]').each(function (i, elm) {
            var $elm = $(elm);

            var id = elm.id;
            if ($elm.closest('.dropzone').length > 0 && $elm.closest('.dropzone').is(":visible")) {
                var dropzone = $elm.closest('.dropzone').get(0).dropzone;

                if (dropzone !== null && dropzone.files !== undefined && dropzone.files.length > 0) {
                    if (dropzone.files.length === 1) {
                        formData[id] = dropzone.files[0];
                    } else {
                        formData[id] = dropzone.files;
                    }
                }
            }
        });

        var msg = {
            formData: formData,
            formPageTitle: $('title').text(),
            formUserviewAppId: UI.userview_app_id,
            formUsername: PwaUtil.currentUsername
        }
        navigator.serviceWorker && navigator.serviceWorker.controller && navigator.serviceWorker.controller.postMessage(msg);
    },

    showToast: function (message, bgColor, textColor) {
        !PwaUtil.isEmbedded && $.toast({
            text: message,
            position: 'bottom-left',
            bgColor: bgColor,
            textColor: textColor,
            //loader: false
        })
    },

    register: function () {
        if (navigator.serviceWorker) {
            function indexesOf(string, substring){
                var a=[], i=-1;
                while((i=string.indexOf(substring,i+1)) >= 0) a.push(i);
                return a;
            }
            var forwardSlashindexes = indexesOf(PwaUtil.serviceWorkerPath, '/');
            var substringIndex = forwardSlashindexes[forwardSlashindexes.length - 2];

            var swScope = PwaUtil.serviceWorkerPath.substring(0, substringIndex);

            if (PwaUtil.isEmbedded) {
                swScope = swScope.replace('/web/userview', '/web/embed/userview');
            }
            
            PwaUtil.registerBaseServiceWorker();

            console.log('registering service worker, scope: ' + swScope);

            return navigator.serviceWorker.register(PwaUtil.serviceWorkerPath, { scope: swScope })
                    .then(function (registration) {
                        var serviceWorker;
                        if (registration.installing) {
                            serviceWorker = registration.installing;
                            // console.log('Service worker installing');
                        } else if (registration.waiting) {
                            serviceWorker = registration.waiting;
                            // console.log('Service worker installed & waiting');
                        } else if (registration.active) {
                            serviceWorker = registration.active;
                            // console.log('Service worker active');
                        }

                        var afterActivated = function(){                            
                            if (PwaUtil.pushEnabled) {
                                PwaUtil.subscribe(registration);
                            }

                            if (registration.sync) {
                                registration.sync.register('sendFormData')
                                        .then(function () {
                                            console.log('sync event registered');
                                        }).catch(function () {
                                    // system was unable to register for a sync,
                                    // this could be an OS-level restriction
                                    console.log('sync registration failed');
                                });
                            }

                            console.log('Service worker successfully registered and activated.');

                            navigator.serviceWorker.controller && navigator.serviceWorker.controller.postMessage({
                                userviewKey: PwaUtil.userviewKey,
                                homePageLink: PwaUtil.homePageLink
                            });

                            PwaUtil.updateServiceWorkerList();
                        }

                        if (serviceWorker && serviceWorker.state === "installing") {
                            serviceWorker.onstatechange = function(e) {
                                if(e.target.state === 'activated'){
                                    afterActivated();
                                }
                            }
                        }

                        if (serviceWorker && (serviceWorker.state === "installed" || serviceWorker.state === "activated")) {
                            afterActivated();
                        }
                    }, function (err) {
                        console.error('Unsuccessful registration with ', PwaUtil.serviceWorkerPath, err);
                    });
        }
    },

    registerBaseServiceWorker: function () {
        if (navigator.serviceWorker) {
            console.log('registering base service worker, scope: ' + PwaUtil.contextPath + '/');

            return navigator.serviceWorker.register(PwaUtil.contextPath + '/basesw.js', { scope: PwaUtil.contextPath + '/' })
                    .then(function (registration) {
                        var serviceWorker;
                        if (registration.installing) {
                            serviceWorker = registration.installing;
                        } else if (registration.waiting) {
                            serviceWorker = registration.waiting;
                        } else if (registration.active) {
                            serviceWorker = registration.active;
                        }

                        var afterActivated = function(){
                            console.log('Base service worker successfully registered and activated.');
                        }

                        if (serviceWorker && serviceWorker.state === "installing") {
                            serviceWorker.onstatechange = function(e) {
                                if(e.target.state === 'activated'){
                                    afterActivated();
                                }
                            }
                        }

                        if (serviceWorker && (serviceWorker.state === "installed" || serviceWorker.state === "activated")) {
                            afterActivated();
                        }
                    }, function (err) {
                        console.error('Unsuccessful registration with ', PwaUtil.contextPath + '/basesw.js', err);
                    });
        }
    },

    subscribe: function (registration) {
        var newSubscription = false;
        registration.pushManager.getSubscription()
                .then(function (subscription) {
                    if (!subscription) {
                        const subscribeOptions = {
                            userVisibleOnly: true,
                            applicationServerKey: PwaUtil.urlBase64ToUint8Array(PwaUtil.applicationServerPublicKey)
                        };
                        var result = registration.pushManager.subscribe(subscribeOptions);
                        newSubscription = true;
                        return result;
                    }
                    console.log('Existing PushSubscription: ', JSON.stringify(subscription));
                    return subscription;
                })
                .then(function (pushSubscription) {
                    var uuid = localStorage.getItem('deviceUUID');
                    if (!uuid) {
                        uuid = PwaUtil.uuid();
                        localStorage.setItem('deviceUUID', uuid);
                    }
                    var localUuid = localStorage.getItem('deviceUUID_'+UI.userview_app_id+'_'+UI.userview_id);
                    if (newSubscription || uuid !== localUuid) {
                        console.log('New PushSubscription : ' + JSON.stringify(pushSubscription));
                        PwaUtil.storeSubscription(pushSubscription, uuid);
                        localStorage.setItem('deviceUUID_'+UI.userview_app_id+'_'+UI.userview_id, uuid);
                    }
                    return pushSubscription;
                })
                .catch(function (err) {
                    console.warn('PushSubscription failed', err);
                    return false;
                });
    },

    urlBase64ToUint8Array: function (base64String) {
        const padding = '='.repeat((4 - base64String.length % 4) % 4);
        const base64 = (base64String + padding)
                .replace(/\-/g, '+')
                .replace(/_/g, '/');

        const rawData = window.atob(base64);
        const outputArray = new Uint8Array(rawData.length);

        for (let i = 0; i < rawData.length; ++i) {
            outputArray[i] = rawData.charCodeAt(i);
        }
        return outputArray;
    },

    storeSubscription: function (pushSubscription, uuid) {
        console.log('Storing PushSubscription: ', JSON.stringify(pushSubscription));
        let formData = new FormData();
        formData.append('subscription', JSON.stringify(pushSubscription));
        formData.append('deviceId', uuid);
        formData.append('appId', UI.userview_app_id);
        formData.append('userviewId', UI.userview_id);
        return fetch(PwaUtil.subscriptionApiPath + "?" + ConnectionManager.tokenName + "=" + ConnectionManager.tokenValue, {
            method: 'POST',
            credentials: "same-origin",
            body: formData
        });
    },

    hideOfflineIndicator: function () {
        $('#offlineIndicator').hide();
    },

    showOfflineIndicator: function () {
        if(PwaUtil.isIframe()){
            return;
        }

        var $offlineIndicator = $('#offlineIndicator');
        if($offlineIndicator.length === 0){
            var html = '<div id="offlineIndicator" style="display: none; position: fixed; bottom: 60px; right: 10px; background: #009688; color: white; padding: 10px; box-shadow: #666 1px 1px 2px 1px; border-radius: 50%; z-index: 100000050; cursor: pointer;width: 20px; text-align: center; box-sizing: content-box;">'
                    + '<a href="' + PwaUtil.contextPath + '/web/userview/' + UI.userview_app_id + '/' + UI.userview_id + '/_/pwaoffline">'
                    + '<i style="xwidth: 15px; xheight: 15px; color: #fff; font-size: 17px;" class="fas fa-wifi"></i>'
                    + '<i style="xwidth: 15px; xheight: 15px; color: rgba(255, 0, 0, 0.6); font-size: 30px; position: absolute; top: 5px; left: 6px; " class="fas fa-ban"></i>'
                    + '</a>'
                    + '</div>';
            $('body').append(html);
            $offlineIndicator = $('#offlineIndicator');
        }

        $offlineIndicator.show();
    },

    isIframe: function () { // not to display offline for dashboard iframe
        if (PwaUtil.isEmbedded && window.self !== window.top) {
            return true;
        }
        return false;
    },
    
    isReachable: function(url) {
        /**
         * Note: fetch() still "succeeds" for 404s on subdirectories,
         * which is ok when only testing for domain reachability.
         *
         * Example:
         *   https://google.com/noexist does not throw
         *   https://noexist.com/noexist does throw
         */
        return fetch(url, {method: 'HEAD', mode: 'no-cors'})
                .then(function (resp) {
                    return resp && (resp.ok || resp.type === 'opaque');
                })
                .catch(function (err) {
                    console.warn('isReachable failed ' + url, err);
                    return false;
                });
    },

    handleConnection: function () {
        /*
         //this snippet relies purely on "online" "offline" events to be triggered, then uses the ping method to determine online/offline 
         PwaUtil.isReachable(PwaUtil.contextPath + '/images/v3/clear.gif').then(function (online) {
         if (online) {
         console.log("handleConnection", PwaUtil.isOnline, true);
         if(PwaUtil.isOnline === false){
         PwaUtil.showToast(PwaUtil.onlineNotificationMessage, PwaUtil.greenColor, 'white');
         
         navigator.serviceWorker.controller && navigator.serviceWorker.controller.postMessage({
         sync: true
         });
         }
         PwaUtil.hideOfflineIndicator();
         PwaUtil.isOnline = true;
         
         } else {
         console.log("handleConnection", PwaUtil.isOnline, false);
         if(PwaUtil.isOnline === true){
         PwaUtil.showToast(PwaUtil.offlineNotificationMessage, PwaUtil.redColor, 'white');
         }
         PwaUtil.showOfflineIndicator();
         PwaUtil.isOnline = false;
         }
         });
         */
        /*
         //this snippet relies purely on navigator.onLine to determine online/offline
         if (navigator.onLine) {
         console.log("handleConnection", PwaUtil.isOnline, true);
         if(PwaUtil.isOnline === false){
         PwaUtil.showToast(PwaUtil.onlineNotificationMessage, PwaUtil.greenColor, 'white');
         
         navigator.serviceWorker.controller && navigator.serviceWorker.controller.postMessage({
         sync: true
         });
         }
         PwaUtil.hideOfflineIndicator();
         PwaUtil.isOnline = true;
         } else {
         console.log("handleConnection", PwaUtil.isOnline, false);
         if(PwaUtil.isOnline === true){
         PwaUtil.showToast(PwaUtil.offlineNotificationMessage, PwaUtil.redColor, 'white');
         }
         PwaUtil.showOfflineIndicator();
         PwaUtil.isOnline = false;
         }
         */
    },

    updateServiceWorkerList : function() {
        navigator.serviceWorker.getRegistrations().then(function(registrations) {
            var list = [];
            for (let i = 1; i < registrations.length; i++) {
                list.push(registrations[i].scope);
            }
            navigator.serviceWorker.controller.postMessage({
                "serviceWorkerList": list
            });
        });
    },
    
    uuid : function(){
        return 'xxxxxxxx-xxxx-4xxx-xxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {  //xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx
            var r = Math.random()*16|0, v = c == 'x' ? r : (r&0x3|0x8);
            return v.toString(16);
        }).toUpperCase();
    }
}

$.fn.serializeObject = function() {
    var o = {};
    var a = this.serializeArray();
    $.each(a, function() {
        if (o[this.name]) {
            if (!o[this.name].push) {
                o[this.name] = [o[this.name]];
            }
            o[this.name].push(this.value || '');
        } else {
            o[this.name] = this.value || '';
        }
    });
    return o;
};

$(function(){
    $(document).trigger("PwaUtil.ready");
});