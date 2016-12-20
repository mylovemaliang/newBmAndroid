(function(window) {
    var flObj = {};

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


    function getQueryString(name, str) {
        var r = str.match(new RegExp("[^s]" + name + "=([^&]*)(&|$)", "i"));
        if (r == null) r = str.match(/\/i(\d*)\.htm/i);
        if (r != null) return unescape(r[1]);
        return null;
    }

    function throttle(func, wait, options) {
        var context, args, result;
        var timeout = null;
        var previous = 0;
        if (!options) options = {};
        var later = function() {
            previous = options.leading === false ? 0 : new Date().getTime();
            timeout = null;
            result = func.apply(context, args);
            if (!timeout) context = args = null;
        };
        return function() {
            var now = new Date().getTime();
            if (!previous && options.leading === false) previous = now;
            var remaining = wait - (now - previous);
            context = this;
            args = arguments;
            if (remaining <= 0 || remaining > wait) {
                if (timeout) {
                    clearTimeout(timeout);
                    timeout = null;
                }
                previous = now;
                result = func.apply(context, args);
                if (!timeout) context = args = null;
            } else if (!timeout && options.trailing !== false) {
                timeout = setTimeout(later, remaining);
            }
            return result;
        };
    };

    function renderHtml(data) {
        var div = document.createElement("div");
        if (data.eventRate) {
            div.innerHTML = "<span style='color:#FF3300;'><span>返" + data.eventRate + "%</span><span>约" + data.eventPrice + "元</span></span>"
        } else {
            div.innerHTML = "<span style='color:#FF3300;'>该商品暂无返利</span>"
        }
        return div;
    }

    function _renderfl(bridge) {
        var result = [];
        var items = document.querySelectorAll(".item-list");
        for (var i = 0; i < items.length; i++) {
            if (!items[i].getAttribute("fqbItemId")) {
                var aele = items[i].querySelectorAll(".item-info a[href]");
                if (aele.length) {
                    var href = aele[0].href || "";
                    var itemId = getQueryString("id", href) || "no";
                    items[i].setAttribute("fqbItemId", itemId);
                    if (itemId != "no") {
                        result.push(itemId);
                        flObj[itemId] = {
                            "ele": items[i]
                        }
                    }
                }
            }
        }

        if (result.length) {
            bridge.callHandler('getFanliForCart', {
                "goodIds": result
            }, function(response) {})
        }

    }
    var renderfl = throttle(_renderfl, 2000);
    setupWebViewJavascriptBridge(function(bridge) {
        bridge.init(function(message, responseCallback) {
                             console.log('JS got a message', message);
                             var data = {
                                 'Javascript Responds': 'Wee!'
                             };
                             console.log('JS responding with', data);
                             responseCallback(data);
                         });


        bridge.registerHandler('afterCartFanliLoaded', function(data, responseCallback) {
            var data = eval("("+data+")");
            if (data) {
                for (var i in data) {
                    if (flObj.hasOwnProperty(i) && flObj[i]) {
                        flObj[i].ele.querySelector(".item-info").appendChild(renderHtml(data[i]));
                    }
                }
            }
        });
        renderfl(bridge);
        window.onscroll = function() {
            renderfl(bridge);
        }
    })
})(window)