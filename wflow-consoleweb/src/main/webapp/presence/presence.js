/** start polyfill for EventSource to support IE https://github.com/remy/polyfills/blob/master/EventSource.js **/
;(function (global) {

if ("EventSource" in global) return;

var reTrim = /^(\s|\u00A0)+|(\s|\u00A0)+$/g;

var EventSource = function (url) {
  var eventsource = this,  
      interval = 500, // polling interval  
      lastEventId = null,
      cache = '';

  if (!url || typeof url != 'string') {
    throw new SyntaxError('Not enough arguments');
  }

  this.URL = url;
  this.readyState = this.CONNECTING;
  this._pollTimer = null;
  this._xhr = null;
  
  function pollAgain(interval) {
    eventsource._pollTimer = setTimeout(function () {
      poll.call(eventsource);
    }, interval);
  }
  
  function poll() {
    try { // force hiding of the error message... insane?
      if (eventsource.readyState == eventsource.CLOSED) return;

      // NOTE: IE7 and upwards support
      var xhr = new XMLHttpRequest();
      xhr.open('GET', eventsource.URL, true);
      xhr.setRequestHeader('Accept', 'text/event-stream');
      xhr.setRequestHeader('Cache-Control', 'no-cache');
      // we must make use of this on the server side if we're working with Android - because they don't trigger 
      // readychange until the server connection is closed
      xhr.setRequestHeader('X-Requested-With', 'XMLHttpRequest');

      if (lastEventId != null) xhr.setRequestHeader('Last-Event-ID', lastEventId);
      cache = '';
    
      xhr.timeout = 50000;
      xhr.onreadystatechange = function () {  
        if (this.readyState == 3 || (this.readyState == 4 && this.status == 200)) {
          // on success
          if (eventsource.readyState == eventsource.CONNECTING) {
            eventsource.readyState = eventsource.OPEN;
            eventsource.dispatchEvent('open', { type: 'open' });
          }

          var responseText = '';
          try {
            responseText = this.responseText || '';
          } catch (e) {}
        
          // process this.responseText
          var parts = responseText.substr(cache.length).split("\n"),
              eventType = 'message',
              data = [],
              i = 0,
              line = '';
            
          cache = responseText;
        
          // TODO handle 'event' (for buffer name), retry
          for (; i < parts.length; i++) {
            line = parts[i].replace(reTrim, '');
            if (line.indexOf('event') == 0) {
              eventType = line.replace(/event:?\s*/, '');
            } else if (line.indexOf('retry') == 0) {                           
              retry = parseInt(line.replace(/retry:?\s*/, ''));
              if(!isNaN(retry)) { interval = retry; }
            } else if (line.indexOf('data') == 0) {
              data.push(line.replace(/data:?\s*/, ''));
            } else if (line.indexOf('id:') == 0) {
              lastEventId = line.replace(/id:?\s*/, '');
            } else if (line.indexOf('id') == 0) { // this resets the id
              lastEventId = null;
            } else if (line == '') {
              if (data.length) {
                var event = new MessageEvent(data.join('\n'), eventsource.url, lastEventId);
                eventsource.dispatchEvent(eventType, event);
                data = [];
                eventType = 'message';
              }
            }
          }

          if (this.readyState == 4) pollAgain(interval);
          // don't need to poll again, because we're long-loading
        } else if (eventsource.readyState !== eventsource.CLOSED) {
          if (this.readyState == 4) { // and some other status
            // dispatch error
            eventsource.readyState = eventsource.CONNECTING;
            eventsource.dispatchEvent('error', { type: 'error' });
            pollAgain(interval);
          } else if (this.readyState == 0) { // likely aborted
            pollAgain(interval);
          } else {
          }
        }
      };
    
      xhr.send();
    
      setTimeout(function () {
        if (true || xhr.readyState == 3) xhr.abort();
      }, xhr.timeout);
      
      eventsource._xhr = xhr;
    
    } catch (e) { // in an attempt to silence the errors
      eventsource.dispatchEvent('error', { type: 'error', data: e.message }); // ???
    } 
  };
  
  poll(); // init now
};

EventSource.prototype = {
  close: function () {
    // closes the connection - disabling the polling
    this.readyState = this.CLOSED;
    clearInterval(this._pollTimer);
    this._xhr.abort();
  },
  CONNECTING: 0,
  OPEN: 1,
  CLOSED: 2,
  dispatchEvent: function (type, event) {
    var handlers = this['_' + type + 'Handlers'];
    if (handlers) {
      for (var i = 0; i < handlers.length; i++) {
        handlers[i].call(this, event);
      }
    }

    if (this['on' + type]) {
      this['on' + type].call(this, event);
    }
  },
  addEventListener: function (type, handler) {
    if (!this['_' + type + 'Handlers']) {
      this['_' + type + 'Handlers'] = [];
    }
    
    this['_' + type + 'Handlers'].push(handler);
  },
  removeEventListener: function (type, handler) {
    var handlers = this['_' + type + 'Handlers'];
    if (!handlers) {
      return;
    }
    for (var i = handlers.length - 1; i >= 0; --i) {
      if (handlers[i] === handler) {
        handlers.splice(i, 1);
        break;
      }
    }
  },
  onerror: null,
  onmessage: null,
  onopen: null,
  readyState: 0,
  URL: ''
};

var MessageEvent = function (data, origin, lastEventId) {
  this.data = data;
  this.origin = origin;
  this.lastEventId = lastEventId || '';
};

MessageEvent.prototype = {
  data: null,
  type: 'message',
  lastEventId: '',
  origin: ''
};

if ('module' in global) module.exports = EventSource;
global.EventSource = EventSource;
 
})(this);
/** end polyfill for EventSource to support IE **/

PresenceUtil = {
    url: UI.base + "/web/presence",
    source : null,
    createEventSource : function() {
        // Check that browser supports EventSource 
        if (!!window.EventSource) {
            // Subscribe to url to listen
            PresenceUtil.source = new EventSource(PresenceUtil.url);

            // Define what to do when server sent new event
            PresenceUtil.source.addEventListener(window.location.pathname, function(e) {
                $("#presence").html(e.data);
                $('img[data-lazysrc]').each(function () {
                    $(this).attr('src', $(this).attr('data-lazysrc'));
                });
                $("#presence li").each(function(i, li){
                    $(li).css("right", (i * 5) + "px");
                });
            }, false);

            PresenceUtil.source.addEventListener("error", function(e) {
                if (e.target.readyState === 2) {
                    if (window['CustomBuilder'] !== undefined) {
                        CustomBuilder.sessionTimeout();
                    }
                }
            }, false);
        } else {
            $("#presence").html("Your browser does not support EventSource");
        }
        $(function() {
            if ($("ul#presence").length === 0) {
                var $presence = $("<ul id='presence'></ul>");
                $(document.body).append($presence);
            }
            setTimeout(function() {
                PresenceUtil.message("join");
            }, 500);
        });
    },
    init: function() {
        PresenceUtil.createEventSource();
        if (!(typeof document.addEventListener === "undefined")) {
            
            var hidden, visibilityChange;
            if (typeof document.hidden !== "undefined") { // Opera 12.10 and Firefox 18 and later support 
                hidden = "hidden";
                visibilityChange = "visibilitychange";
            } else if (typeof document.msHidden !== "undefined") {
                hidden = "msHidden";
                visibilityChange = "msvisibilitychange";
            } else if (typeof document.webkitHidden !== "undefined") {
                hidden = "webkitHidden";
                visibilityChange = "webkitvisibilitychange";
            }
            
            document.addEventListener(visibilityChange, function(){
                if (document[hidden]) {
                    if (PresenceUtil.source !== null) {
                        PresenceUtil.source.close();
                        PresenceUtil.source = null;
                    }
                } else {
                    //refresh ConnectionManager token
                    $.ajax({
                        type: 'POST',
                        url: UI.base + "/csrf",
                        headers: {
                            "FETCH-CSRF-TOKEN-PARAM":"true",
                            "FETCH-CSRF-TOKEN":"true"
                        },
                        success: function (response) {
                            var temp = response.split(":");
                            ConnectionManager.tokenValue = temp[1];
                            JPopup.tokenValue = temp[1];
                            
                            PresenceUtil.createEventSource();
                        },
                        error: function(request, status, error) {
                            console.warn("fail to refresh csrf token");
                            document.location.href = document.location;
                        }
                    });
                }
            }, false);
        }
        $(window).on("beforeunload", function() {
            PresenceUtil.message("leave");
        });        
    },
    message: function(action, retried) {
        $.ajax({
            type: 'POST',
            url: PresenceUtil.url,
            data: {action: action},
            beforeSend: function (request) {
               if (ConnectionManager.tokenName !== undefined) { 
                   request.setRequestHeader(ConnectionManager.tokenName, ConnectionManager.tokenValue);
               }
            },
            error: function(request, status, error) {
                console.warn("Presence error", error);
                
                if (status === 403 && (retried === undefined || retried === false)) {
                    //refresh ConnectionManager token and retry
                    $.ajax({
                        type: 'POST',
                        url: UI.base + "/csrf",
                        headers: {
                            "FETCH-CSRF-TOKEN-PARAM":"true",
                            "FETCH-CSRF-TOKEN":"true"
                        },
                        success: function (response) {
                            var temp = response.split(":");
                            ConnectionManager.tokenValue = temp[1];
                            JPopup.tokenValue = temp[1];

                            PresenceUtil.message(action, true);
                        },
                        error: function(request, status, error) {
                            console.warn("fail to refresh csrf token");
                            document.location.href = document.location;
                        }
                    });
                }
            }
        });
    }
};
AdminBar.origHideQuickOverlay = AdminBar.hideQuickOverlay;
AdminBar.hideQuickOverlay = function() {
    $("#quickOverlayFrame").attr("src", "about:blank");
    setTimeout(function() {
        AdminBar.origHideQuickOverlay();
    }, 100);
};

PresenceUtil.init();

