PwaUtil = {

    applicationServerPublicKey: "BE54-RlSdVqGwlh_skZ4qQqP1tY7uNZrQbv3IJ_Rd2uRHsId8XjvH2CXav_5PkhrM1XvBLXJyi7tx6io5E3fegg", // private key: Fe0bTj0H_UYcg7qnFzx0qBl-H90RuptO6r_vOUZJWvI

    serviceWorkerPath: "/jw/sw.js",

    registerAndSubscribe: function () {
        return navigator.serviceWorker.register(PwaUtil.serviceWorkerPath)
                .then(function (registration) {
                    console.log('Service worker successfully registered.');
                    const subscribeOptions = {
                        userVisibleOnly: true,
                        applicationServerKey: PwaUtil.urlBase64ToUint8Array(PwaUtil.applicationServerPublicKey)
                    };

                    return registration.pushManager.subscribe(subscribeOptions);
                })
                .then(function (pushSubscription) {
                    console.log('Received PushSubscription: ', JSON.stringify(pushSubscription));
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

}

$(function () {
    PwaUtil.registerAndSubscribe();
});