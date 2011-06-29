ConnectionManager = {
    post : function(url, callback, params){
              var thisWindow = window;
              $.ajax({
                 type: 'POST',
                 url: url,
                 data: params,
                 dataType: null,
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
            dataFilter:  function(data, type){
                //unwrap jsonp callback function
                if(/^jsonp[\d]+\(/.test(data)){
                    data = data.replace(/^jsonp[\d]+\(/, '');
                    data = data.replace(/\)$/, '');
                    var obj = eval('(' + data + ')');
                    callback.success(obj);
                    return obj;
                }else
                    callback.success(data);
            },
	        success:     function(data) {
                callback.success(data);
            }
	    });
    },

    get : function(url, callback, params, xss){
        if(!xss){
              $.ajax({
                 type: 'GET',
                 url: url,
                 data: params,
                 dataType: null,
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
                    var url = baseUrl + "/web/json/directory/user/sso?username=" + username + "&password=" + password;
                    ConnectionManager.ajaxJsonp(url, callback, null);
                }else
                    callback.success();
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
		ConnectionManager.ajaxJsonp(url, callback, null);
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
		ConnectionManager.ajaxJsonp(url, callback, null);
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
		ConnectionManager.ajaxJsonp(url, callback, variableData);
    }
};

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
