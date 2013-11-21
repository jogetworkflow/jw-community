ConnectionManager = {
    post : function(url, callback, params){
              var thisWindow = window;
              $.support.cors = true;
              $.ajax({
                 type: 'POST',
                 url: url,
                 data: params,
                 dataType : "text",
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
	        success:     function(data) {
                    callback.success(data);
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
				document.location = baseUrl + "/web/json/workflow/closeDialog";
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
                    document.location = baseUrl + "/web/json/workflow/closeDialog";
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
                    document.location = baseUrl + "/web/json/workflow/closeDialog";
			}
		};
		ConnectionManager.post(url, callback, variableData);
    }
};

UrlUtil = {
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
    }
}

function filter(jsonTable, url, value){
    var newUrl = url + value;
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
