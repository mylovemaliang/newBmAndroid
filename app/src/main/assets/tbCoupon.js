(function(window) {
    function setupWebViewJavascriptBridge(callback) {
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
    setupWebViewJavascriptBridge(function(bridge) {
       if (!bridge._messageHandler) {
              bridge.init(function(message, responseCallback) {
                   console.log('JS got a message', message);
                   var data = {
                       'Javascript Responds': 'Wee!'
                   };
                   console.log('JS responding with', data);
                   responseCallback(data);
             });
        }

        var interId=setInterval(function(){
        if(document.querySelector(".coupons-price")){
                var couponNum=document.querySelector(".coupons-price").innerText.slice(1);
                bridge.callHandler('afterCouponGet',{"couponNum": couponNum}, function(response) {});
                clearInterval(interId);
            }
        },1000)
    })
})(window)