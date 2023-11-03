ConnectionManager = {
    post : function(url, callback, params){
              var thisWindow = window;
              $.support.cors = true;
              $.ajax({
                 type: 'POST',
                 url: url,
                 data: params,
                 dataType : "text",
                 beforeSend: function (request) {
                    if (ConnectionManager.tokenName !== undefined) { 
                        request.setRequestHeader(ConnectionManager.tokenName, ConnectionManager.tokenValue);
                    }
                 },
                 xhrFields: {
                     withCredentials: true
                 },
                 success: function(data) {
                     callback.success.call(thisWindow, data);
                 },
                 error: function(data) {
                     try {
                         // do nothing for now
                         if (callback.error) {
                             callback.error.call(thisWindow, data);
                         }
                     }
                     catch (e) {}
                 }
               });

    },

    ajaxJsonp : function(url, callback, params){
        $.ajax({
	        url:         url,
	        dataType:    'jsonp',
	        data:        params,
	        processData: false,
                timeout: 5000,
	        success:     function(data) {
                    callback.success(data);
                },
                error: function(data) {
                     try {
                         // do nothing for now
                         if (callback.error) {
                             callback.error(data);
                         }
                     }
                     catch (e) {}
                }
	    });
    },

    get : function(url, callback, params, xss){
        if(!xss){
              $.support.cors = true;
              $.ajax({
                 type: 'GET',
                 url: url,
                 data: params,
                 dataType : "text",
                 xhrFields: {
                     withCredentials: true
                 },
                 success: function(data) {
                     callback.success(data)
                 },
                 error: function(data) {
                     try {
                         // do nothing for now
                         if (callback.error) {
                             callback.error(data);
                         }
                     }
                     catch (e) {}
                 }
               });
        }else{
            $.getScript(url, callback);
        }
    }
};

AssignmentManager = {
    getCurrentUsername : function(baseUrl, callback){
        var url = baseUrl + "/web/json/workflow/currentUsername";
        ConnectionManager.ajaxJsonp(url, callback, null);
    },

    login : function(baseUrl, username, password, callback){
        //check current username
        var gcuCallback = {
			success : function(o){
				if(o.username != username){
                    var url = baseUrl + "/web/json/directory/user/sso?username=" + encodeURIComponent(username) + "&password=" + encodeURIComponent(password);
                    ConnectionManager.ajaxJsonp(url, callback, null);
                }else
                    callback.success(o);
			}
		};

        this.getCurrentUsername(baseUrl, gcuCallback);
    },

    loginWithHash : function(baseUrl, username, hash, callback){
        //check current username
        var gcuCallback = {
            success : function(o){
		if(o.username != username){
                    var url = baseUrl + "/web/json/directory/user/sso?username=" + username + "&hash=" + hash;
                    ConnectionManager.ajaxJsonp(url, callback, null);
                }else {
                    callback.success(o);
		}
            }
	};

        this.getCurrentUsername(baseUrl, gcuCallback);
    },

    logout : function(baseUrl){
        var url = baseUrl + "/j_spring_security_logout";
        ConnectionManager.ajaxJsonp(url, null, null);
    },

    withdrawAssignment : function(baseUrl, activityId){
        var url = baseUrl + "/web/json/workflow/assignment/withdraw/" + activityId;
        var callback = {
			success : function(){
				AssignmentManager.refreashOrCloseDialog();
			}
		};
		ConnectionManager.post(url, callback, null);
    },

    completeAssignment : function(baseUrl, activityId, redirect){
        var url = baseUrl + "/web/json/workflow/assignment/complete/" + activityId;

		var callback = {
			success : function(o){
                if(redirect){
                    var param = (redirect.indexOf('?') == -1) ? "?processId=" : "&processId=";
                    var path = redirect + param + o.processId;
                    document.location = path;
                }else
                    AssignmentManager.refreashOrCloseDialog();
			}
		};
		ConnectionManager.post(url, callback, null);
    },

    completeAssignmentWithVariable : function(baseUrl, activityId, variableData, redirect){
        var url = baseUrl + "/web/json/workflow/assignment/completeWithVariable/" + activityId;

		var callback = {
			success : function(o){
                if(redirect){
                    var param = (redirect.indexOf('?') == -1) ? "?processId=" : "&processId=";
                    var path = redirect + param + o.processId;
                    document.location = path;
                }else
                    AssignmentManager.refreashOrCloseDialog();
			}
		};
		ConnectionManager.post(url, callback, variableData);
    },
    
    refreashOrCloseDialog : function() {
        if(parent && parent.refreshAll)
            parent.refreshAll();
        if(parent && parent.closeDialog)
            parent.closeDialog();
    }
};

UrlUtil = {
    //Similar to $.serialize but including empty value selectbox, checkbox & radio button
    serializeForm : function(form) {
        var params = $(form).serialize();
        
        //check for selectbox, checboxes & radio
        $(form).find('select, input[type="checkbox"], input[type="radio"]').each(function(){
            if ($(this).is("[name]") && !$(this).prop('disabled')) {
                var name = $(this).attr("name");
                if (params.indexOf(name) === -1) {
                    //the field is not exist in params, add an empty value for it
                    if (params !== "") {
                        params += "&";
                    }
                    params += name + "=";
                }
            }
        });
        return params;
    },
    
    updateUrlParam : function(url, param, paramValue) {
        var qs1 = "";
        var qs2 = param + "=" + paramValue;
        if (url.indexOf("?") !== -1) {
            qs1 = url.substring(url.indexOf("?") + 1);
            url = url.substring(0, url.indexOf("?"));
        }
        return url + "?" + UrlUtil.mergeRequestQueryString(qs1, qs2);
    },
    
    encodeUrlParam : function(url){
        var urlResult = url;
        try{
            var urlPart = urlResult.split("\\?");

            urlResult = urlPart[0];

            if (urlPart.length > 1) {
                urlResult += "?" + UrlUtil.constructUrlQueryString(getUrlParams(urlPart[1]));
            }
        }catch(err){}

        return urlResult;
    },
    
    mergeRequestQueryString : function(queryString1, queryString2){
        if (queryString1 == null || queryString2 == null) {
            return queryString1;
        }
        var params = UrlUtil.getUrlParams(queryString1);
        params = $.extend(params, UrlUtil.getUrlParams(queryString2));
        return UrlUtil.constructUrlQueryString(params);
    },
    
    getUrlParams : function(url){
        var result = new Object();
        if(url != ""){
            try {
                var queryString = url;
                if (url.indexOf("?") != -1) {
                    queryString = url.substring(url.indexOf("?") + 1);
                }
                if (queryString != "") {
                    var params = queryString.split("&");
                    for(a in params) {
                        if(params[a] != ""){
                            var param = params[a].split("=");
                            var key = decodeURIComponent(param[0]);
                            var value = decodeURIComponent(param[1]);
                            var values = result[key];
                            if(values == undefined){
                                values = new Array();
                            }
                            values.push(value);
                            
                            result[key] = values;
                        }
                    }
                }
            }catch(err){}
        }
        return result;
    },
    
    constructUrlQueryString : function(params){
        var queryString = "";
        try {
            for (key in params) {
                var values = params[key];
                for (value in values) {
                    queryString += encodeURIComponent(key) + "=" + encodeURIComponent(values[value]) + "&";
                }
            }
            if (queryString != "") {
                queryString = queryString.substring(0, queryString.length-1);
            }
        }catch(err){}
        return queryString;
    },
    
    /*
     * To fix the url parameter and form fields having same key,
     * Spring framework 5 will return an array for both value in URL and Field.
     */
    solveUrlParamFormFieldConflict: function(form) {
        if (!form) {
            return;
        }
        var url = $(form).attr("action");
        if (url !== null && url !== undefined && url !== "" && url.indexOf("?") !== -1) {
            var changed = false;
            var params = UrlUtil.getUrlParams(url);
            for (var p in params) {
                var field = FormUtil.getField(p, form);
                if (field.length > 0 && !field.prop('disabled') && field.attr("id") === p) {
                    delete params[p];
                    changed = true;
                }
            }
            if (changed) {
                url = url.substring(0, url.indexOf("?")) + "?" + UrlUtil.constructUrlQueryString(params);
                $(form).attr("action", url);
            }
        }
    }
};

function filter(jsonTable, url, value){
    var newUrl = url + encodeURIComponent(value);
    jsonTable.load(jsonTable.url + newUrl);
}

function getUrlParam(paramName){
    paramName = paramName.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
    var regexS = "[\\?&]"+paramName+"=([^&#]*)";
    var regex = new RegExp( regexS );
    var results = regex.exec( window.location.href );
    if( results == null )
        return "";
    else
        return results[1];
}

var loadingScript = {};
function loadScript(url, callback){
    var name = url.substring(url.lastIndexOf("/") + 1).replace(".", "_");
    var callbackHandler = function() {
        try {
            while(loadingScript[name].length > 0) {
                var c = loadingScript[name].shift();
                c();
            }
        } catch (err) {
            console.log(err);
            var tag = document.getElementById("js-" + name);
            tag.remove();
        }
    };
    if (loadingScript[name] !== undefined && loadingScript[name].length > 0) {
        loadingScript[name].push(callback);
    } else {
        if (document.querySelectorAll('#js-' + name).length === 0) {
            loadingScript[name] = [];
            loadingScript[name].push(callback);
        
            var head = document.getElementsByTagName('head')[0];
            var script = document.createElement('script');
            script.type = 'text/javascript';
            script.src = url;
            script.id = "js-" + name;
            script.onreadystatechange = callbackHandler;
            script.onload = callbackHandler;
            head.appendChild(script);
        } else {
            loadingScript[name].push(callback);
            callbackHandler();
        }
    }
}
var popupActionDialog = null;
function dlPopupAction(element, message) {
    var url = $(element).attr("href");
    var showPopup = true;
    if (message != "") {
        showPopup = confirm(message);
    }
    if (showPopup) {
        if (popupActionDialog == null) {
            popupActionDialog = new PopupDialog(url);
        } else {
            popupActionDialog.src = url;
        }
        popupActionDialog.init();
    }
    return false;
}
function dlPostAction(element, message) {
    var url = $(element).attr("href");
    var showPopup = true;
    if (message != "") {
        showPopup = confirm(message);
    }
    if (showPopup) {
        var  orgAction = $(element).closest("form").attr("action");
        $(element).closest("form").removeAttr("target");
        $(element).closest("form").find("input[type=checkbox]").removeAttr("checked");
        $(element).closest("form").attr("action", $(element).attr("href"));
        $(element).closest("form").submit();

        //reset the action
        $(element).closest("form").attr("action", orgAction);
    }
    return false;
}

/* compatibility implementation of $.browser */
jQuery.uaMatch = function( ua ) {
    ua = ua.toLowerCase();

    var match = /(chrome)[ \/]([\w.]+)/.exec( ua ) ||
        /(webkit)[ \/]([\w.]+)/.exec( ua ) ||
        /(opera)(?:.*version|)[ \/]([\w.]+)/.exec( ua ) ||
        /(msie)[\s?]([\w.]+)/.exec( ua ) ||       
        /(trident)(?:.*? rv:([\w.]+)|)/.exec( ua ) ||
        ua.indexOf("compatible") < 0 && /(mozilla)(?:.*? rv:([\w.]+)|)/.exec( ua ) ||
        [];

    return {
        browser: match[ 1 ] || "",
        version: match[ 2 ] || "0"
    };
};

matched = jQuery.uaMatch( navigator.userAgent );
//IE 11+ fix (Trident) 
matched.browser = matched.browser == 'trident' ? 'msie' : matched.browser;
browser = {};

if ( matched.browser ) {
    browser[ matched.browser ] = true;
    browser.version = matched.version;
}

// Chrome is Webkit, but Webkit is also Safari.
if ( browser.chrome ) {
    browser.webkit = true;
} else if ( browser.webkit ) {
    browser.safari = true;
}

jQuery.browser = browser;