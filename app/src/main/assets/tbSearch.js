function connectWebViewJavascriptBridge(callback) {
            if (window.WebViewJavascriptBridge) {
                callback(WebViewJavascriptBridge);
            } else {
                document.addEventListener(
                    'WebViewJavascriptBridgeReady'
                    , function() {
                        callback(WebViewJavascriptBridge)
                    },
                    false
                );
            }
}
connectWebViewJavascriptBridge(function(bridge) {
     bridge.init(function(message, responseCallback) {
                     console.log('JS got a message', message);
                     var data = {
                         'Javascript Responds': 'Wee!'
                     };
                     console.log('JS responding with', data);
                     responseCallback(data);
                 });
     bridge.callHandler('rememberUserInfo',{"message":"maliang"},function(response){});
});





