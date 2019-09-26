PwaUtil = {

    applicationServerPublicKey: "BE54-RlSdVqGwlh_skZ4qQqP1tY7uNZrQbv3IJ_Rd2uRHsId8XjvH2CXav_5PkhrM1XvBLXJyi7tx6io5E3fegg",

    contextPath: "/jw",
    
    userviewKey: "_",

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
        //check if in iframe
        if(!PwaUtil.isEmbedded){
            try {
                PwaUtil.isEmbedded = window.self !== window.top;
            } catch (e) {
                PwaUtil.isEmbedded = true;
            }
        }
        
        if (!navigator.serviceWorker || PwaUtil.isEmbedded) {
            return;
        }
        
        $(document).ready(function(){

            window.addEventListener('online', PwaUtil.handleConnection);
            window.addEventListener('offline', PwaUtil.handleConnection);

            //some browsers doesn't fire 'online' and 'offline' events reliably
            setInterval(PwaUtil.handleConnection, 5000);

            PwaUtil.handleConnection();
            //PwaUtil.showOfflineIndicator();

            $("form").submit(function(e){
                var formData = $(this).serializeObject();
                
                var $submitButton = $('input[type=submit][clicked=true]');
                formData[$submitButton.attr('name')] = $submitButton.val();
                $('input[type=submit]').removeAttr("clicked");

                $('form input[type=file]').each(function(i, elm){
                    var $elm = $(elm);

                    var id = elm.id;
                    var dropzone = $elm.closest('.dropzone').get(0).dropzone;

                    if(dropzone !== null && dropzone.files.length > 0){
                        if(dropzone.files.length === 1){
                            formData[id] = dropzone.files[0];
                        }else{
                            formData[id] = dropzone.files;
                        }
                    }
                })

                var msg = {
                    formData: formData,
                    formPageTitle: $('title').text(),
                    formUserviewAppId: UI.userview_app_id,
                    formUsername: PwaUtil.currentUsername
                }
                navigator.serviceWorker.controller && navigator.serviceWorker.controller.postMessage(msg);
            });
            
            $('form input[type=submit]').click(function() {
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
                }
            });
        });
    },
    
    showToast: function(message, bgColor, textColor) {
        !PwaUtil.isEmbedded && $.toast({
            text: message,
            position: 'bottom-left',
            bgColor: bgColor,
            textColor: textColor,
            //loader: false
        })
    },

    register: function () {
        if (navigator.serviceWorker && !PwaUtil.isEmbedded) {
            function indexesOf(string, substring){
                var a=[], i=-1;
                while((i=string.indexOf(substring,i+1)) >= 0) a.push(i);
                return a;
            }
            var forwardSlashindexes = indexesOf(PwaUtil.serviceWorkerPath, '/');
            var substringIndex = forwardSlashindexes[forwardSlashindexes.length - 2];
            
            var swScope = PwaUtil.serviceWorkerPath.substring(0, substringIndex);
            
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
                            registration.sync.register('sendFormData')
                                .then(function () {
                                    console.log('sync event registered');
                                }).catch(function () {
                                    // system was unable to register for a sync,
                                    // this could be an OS-level restriction
                                    console.log('sync registration failed');
                                });
                            
                            console.log('Service worker successfully registered and activated.');
                            
                            navigator.serviceWorker.controller && navigator.serviceWorker.controller.postMessage({
                                userviewKey: PwaUtil.userviewKey
                            });
                            
                            if (PwaUtil.pushEnabled) {
                                PwaUtil.subscribe(registration);
                            }
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
        
        PwaUtil.init();
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
                    if (newSubscription) {
                        console.log('New PushSubscription : ' + JSON.stringify(pushSubscription));
                        PwaUtil.storeSubscription(pushSubscription);
                    }
                    return pushSubscription;
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

    storeSubscription: function (pushSubscription) {
        console.log('Storing PushSubscription: ', JSON.stringify(pushSubscription));
        let formData = new FormData();
        formData.append('subscription', JSON.stringify(pushSubscription));
        return fetch(PwaUtil.subscriptionApiPath + "?" + ConnectionManager.tokenName + "=" + ConnectionManager.tokenValue, {
            method: 'POST',
            credentials: "same-origin",
            body: formData,
        });
    },

    hideOfflineIndicator: function () {
        $('#offlineIndicator').hide();
    },

    showOfflineIndicator: function () {
        if(PwaUtil.isEmbedded){
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
    
    handleConnection: function () {
        
        if (navigator.onLine) {
            if(PwaUtil.isOnline === false){
                PwaUtil.showToast(PwaUtil.onlineNotificationMessage, PwaUtil.greenColor, 'white');
                
                navigator.serviceWorker.controller && navigator.serviceWorker.controller.postMessage({
                    sync: true
                });
            }
            PwaUtil.hideOfflineIndicator();
            PwaUtil.isOnline = true;
        } else {
            if(PwaUtil.isOnline === true){
                PwaUtil.showToast(PwaUtil.offlineNotificationMessage, PwaUtil.redColor, 'white');
            }
            PwaUtil.showOfflineIndicator();
            PwaUtil.isOnline = false;
        }
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