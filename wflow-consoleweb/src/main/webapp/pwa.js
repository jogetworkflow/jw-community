PwaUtil = {

    applicationServerPublicKey: "BE54-RlSdVqGwlh_skZ4qQqP1tY7uNZrQbv3IJ_Rd2uRHsId8XjvH2CXav_5PkhrM1XvBLXJyi7tx6io5E3fegg", // private key: Fe0bTj0H_UYcg7qnFzx0qBl-H90RuptO6r_vOUZJWvI

    serviceWorkerPath: "/jw/sw.js",

    subscriptionApiPath: "/jw/web/console/profile/subscription",
    
    pushEnabled: true,

    register: function () {
        if (navigator.serviceWorker) {
            return navigator.serviceWorker.register(PwaUtil.serviceWorkerPath)
                    .then(function (registration) {
                        console.log('Service worker successfully registered.');
                        if (PwaUtil.pushEnabled) {
                            PwaUtil.subscribe(registration);
                        }
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

}

//$(function () {
//    PwaUtil.register();
//});
