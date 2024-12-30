UI = {
   rtl: false, 
   base: '',
   userview_app_id: '',
   userview_id: '',
   locale: '',
   theme: '',
   escapeHTML: function(c) {
      if (c == null || c == undefined) {
          return '';
      } else {
          var span = $('<span></span>').text(c);
          return span.html();
      }
   },
   stripHtmlTags: function(c) {
        if (c == null || c == undefined) {
            return '';
        } else {
            let div= document.createElement("div");
            div.innerHTML= c;
            return (div.textContent || div.innerText || "");
        } 
   },
   userviewThemeParams: function () {
      var params = ''; 
      if (UI.userview_app_id != undefined && UI.userview_app_id != '') {
          params += "&__a_=" + UI.userview_app_id;
          params += "&__u_=" + UI.userview_id;
      }
      return params;
   },
   initThemeParams: function () {
        $("form").each(function(){
            $(this).attr("action", UI.addThemeParamsToUrl($(this).attr("action")));
        });
        $("a").each(function(){
            if (!($(this).attr("href") === undefined || $(this).attr("href") === null)) {
                $(this).attr("href", UI.addThemeParamsToUrl($(this).attr("href")));
            }
        });
   },
   addThemeParamsToUrl: function (url) {
        if (url === undefined || url === null) {
            url = "";
        }
        if (url.indexOf("__a_") !== -1 && url.indexOf("__u_") !== -1) {
            return url;
        }
        if (url.indexOf("?") < 0) {
            url += "?";
        } else {
            url += "&";
        }
        return url += "__a_=" + UI.userview_app_id + "&__u_=" + UI.userview_id;
   },
   getPopUpHeight: function(height) {
       if (height === undefined || height === "") {
           height = "90%";
       }
       var windowHeight = $(window).height();
       var windowWidth = $(window).width();
       var maxHeight = windowHeight - 100;
           
       if (isNaN(height) && height.indexOf("%") !== -1) {
           var tempHeight = parseFloat(height.replace("%", ""));
           height = windowHeight * tempHeight / 100;
       }
       
       if (height > maxHeight || windowWidth < 668) {
           height = maxHeight;
       }
       
       return height;
   }, 
   getPopUpWidth: function(width) {
       if (width === undefined || width === "") {
           width = "90%";
       }
       var windowWidth = $(window).width();
       var minWidth = 668;
       var maxWidth = windowWidth - 50;
           
       if (isNaN(width) && width.indexOf("%") !== -1) {
           var tempWidth = parseFloat(width.replace("%", ""));
           width = windowWidth * tempWidth / 100;
       }
       
       if (width > maxWidth) {
           width = maxWidth;
       } else if (width < minWidth && minWidth < maxWidth) {
           width = minWidth;
       } else if (windowWidth < 668) {
           width = maxWidth;
       }
       
       return width;
   },
   adjustPopUpDialog: function(dialogbox) {
       // center dialogbox
       dialogbox.center('x');
       dialogbox.center('y');
   },
   isMobileUserAgent: function() {
        var mobileUserAgent = false;
        (function(a){if(/android|avantgo|bada\/|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(ad|hone|od)|iris|kindle|lge |maemo|midp|mmp|netfront|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\/|plucker|pocket|psp|symbian|treo|up\.(browser|link)|vodafone|wap|windows (ce|phone)|xda|xiino/i.test(a)||/1207|6310|6590|3gso|4thp|50[1-6]i|770s|802s|a wa|abac|ac(er|oo|s\-)|ai(ko|rn)|al(av|ca|co)|amoi|an(ex|ny|yw)|aptu|ar(ch|go)|as(te|us)|attw|au(di|\-m|r |s )|avan|be(ck|ll|nq)|bi(lb|rd)|bl(ac|az)|br(e|v)w|bumb|bw\-(n|u)|c55\/|capi|ccwa|cdm\-|cell|chtm|cldc|cmd\-|co(mp|nd)|craw|da(it|ll|ng)|dbte|dc\-s|devi|dica|dmob|do(c|p)o|ds(12|\-d)|el(49|ai)|em(l2|ul)|er(ic|k0)|esl8|ez([4-7]0|os|wa|ze)|fetc|fly(\-|_)|g1 u|g560|gene|gf\-5|g\-mo|go(\.w|od)|gr(ad|un)|haie|hcit|hd\-(m|p|t)|hei\-|hi(pt|ta)|hp( i|ip)|hs\-c|ht(c(\-| |_|a|g|p|s|t)|tp)|hu(aw|tc)|i\-(20|go|ma)|i230|iac( |\-|\/)|ibro|idea|ig01|ikom|im1k|inno|ipaq|iris|ja(t|v)a|jbro|jemu|jigs|kddi|keji|kgt( |\/)|klon|kpt |kwc\-|kyo(c|k)|le(no|xi)|lg( g|\/(k|l|u)|50|54|e\-|e\/|\-[a-w])|libw|lynx|m1\-w|m3ga|m50\/|ma(te|ui|xo)|mc(01|21|ca)|m\-cr|me(di|rc|ri)|mi(o8|oa|ts)|mmef|mo(01|02|bi|de|do|t(\-| |o|v)|zz)|mt(50|p1|v )|mwbp|mywa|n10[0-2]|n20[2-3]|n30(0|2)|n50(0|2|5)|n7(0(0|1)|10)|ne((c|m)\-|on|tf|wf|wg|wt)|nok(6|i)|nzph|o2im|op(ti|wv)|oran|owg1|p800|pan(a|d|t)|pdxg|pg(13|\-([1-8]|c))|phil|pire|pl(ay|uc)|pn\-2|po(ck|rt|se)|prox|psio|pt\-g|qa\-a|qc(07|12|21|32|60|\-[2-7]|i\-)|qtek|r380|r600|raks|rim9|ro(ve|zo)|s55\/|sa(ge|ma|mm|ms|ny|va)|sc(01|h\-|oo|p\-)|sdk\/|se(c(\-|0|1)|47|mc|nd|ri)|sgh\-|shar|sie(\-|m)|sk\-0|sl(45|id)|sm(al|ar|b3|it|t5)|so(ft|ny)|sp(01|h\-|v\-|v )|sy(01|mb)|t2(18|50)|t6(00|10|18)|ta(gt|lk)|tcl\-|tdg\-|tel(i|m)|tim\-|t\-mo|to(pl|sh)|ts(70|m\-|m3|m5)|tx\-9|up(\.b|g1|si)|utst|v400|v750|veri|vi(rg|te)|vk(40|5[0-3]|\-v)|vm40|voda|vulc|vx(52|53|60|61|70|80|81|83|85|98)|w3c(\-| )|webc|whit|wi(g |nc|nw)|wmlb|wonu|x700|xda(\-|2|g)|yas\-|your|zeto|zte\-/i.test(a.substr(0,4)))mobileUserAgent=true;})(navigator.userAgent||navigator.vendor||window.opera);
        return mobileUserAgent;
    },
    blockUI : function() {
        $.blockUI({ css: { 
            border: 'none', 
            padding: '15px', 
            backgroundColor: 'transparent', 
            '-webkit-border-radius': '10px', 
            '-moz-border-radius': '10px', 
            opacity: 0.8, 
            color: '#fff' 
        }, message : '<i class="fas fa-spin fa-spinner fa-3x"></i>' }); 
    },
    unblockUI : function() {
        $.unblockUI();
    },
    maxIframe : function(id) {
        if (id !== "" && $("iframe#" + id).length > 0 && !(/iPhone|iPod|iPad|Safari/.test(navigator.userAgent))) {
            var iframe = $("iframe#" + id);
            $(iframe).trigger("iframe-ui-maxsize");
            if ($(iframe)[0].hasAttribute("frameBorder")) {
                $(iframe).data("frameBorder", $(iframe).attr("frameBorder"));
            }
            $(iframe).attr("frameBorder", 0);
            $(iframe).data("style", $(iframe).attr("style"));
            $(iframe).addClass("maxsize");
            $(iframe).attr("style", "position:fixed; top:0; left:0; width: 100vw;height: 100vh; margin:0; padding:0; z-index: 9999999;");
        }
    },
    restoreIframe : function(id) {
        if (id !== "" && $("iframe#" + id).length > 0 && !(/iPhone|iPod|iPad|Safari/.test(navigator.userAgent))) {
            var iframe = $("iframe#" + id);
            var style = $(iframe).data("style");
            if (style === null || style === undefined) {
                style = "";
            }
            $(iframe).attr("style", style);
            $(iframe).removeAttr("frameBorder");
            if ($(iframe).data("frameBorder") !== undefined) {
                $(iframe).attr("frameBorder", $(iframe).data("frameBorder"));
            }
            $(iframe).removeClass("maxsize");
            $(iframe).trigger("iframe-ui-restore");
        }
    },
    /*
     * Use to replace the window.setInterval with added window visibility change into consideration.
     * Clear the interval when it is hidden and start it again when it is visible.
     */
    visibilityChangeSetInterval : function(name, callback, milliseconds) {
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
            if (UI.visibilityChangeIntervals === undefined) {
                UI.visibilityChangeIntervals = {};
            }
            document.addEventListener(visibilityChange, function(event){
                if (!document[hidden]) {
                    callback();
                    UI.visibilityChangeIntervals[name] = setInterval(callback, milliseconds);
                } else if (UI.visibilityChangeIntervals[name] !== undefined && UI.visibilityChangeIntervals[name] !== null) {
                    clearInterval(UI.visibilityChangeIntervals[name]);
                    delete UI.visibilityChangeIntervals[name];
                }
            }, false);
        }
        UI.visibilityChangeIntervals[name] = setInterval(callback, milliseconds);
    },
    /*
     * Retreive i18n messages for javascript usage
     */
    loadMsg : function(keys, callback) {
        if (callback !== undefined && (typeof callback === "function")) {
            if (UI.messages === undefined) {
                UI.messages = {};
            }
            if (UI.messagesCalls === undefined) {
                UI.messagesCalls = {};
            }
            var missingKeys = [];
            if (keys !== undefined && keys !== null && keys.length > 0) {
                for (var i = 0; i < keys.length; i++) {
                    if (UI.messages[keys[i]] === undefined) {
                        missingKeys.push(keys[i]);
                    }
                }
            }
            if (missingKeys.length > 0) {
                //check if there is existing calls for the missing keys
                var callKey = missingKeys.join();
                if (UI.messagesCalls[callKey] === undefined) {
                    UI.messagesCalls[callKey] = [callback];
                    
                    ConnectionManager.post(UI.base + '/web/userview/'+UI.userview_app_id+'/appI18nMessages', {
                        success : function(data) {
                            UI.messages = $.extend(UI.messages, eval('['+data+']')[0]);
                            
                            var callbacks = UI.messagesCalls[callKey];
                            delete UI.messagesCalls[callKey];
                            
                            for (var c in callbacks) {
                                callbacks[c](UI.messages);
                            }
                        }
                    }, {
                       'keys' : missingKeys
                    });
                } else {
                    UI.messagesCalls[callKey].push(callback);
                }
            } else {
                callback(UI.messages);
            }
        }
    }
};

/*
 * Modal popup dialog box showing a URL in an IFRAME
 */
PopupDialog = function(src, title, windowName) {
    this.src = src;
    this.title = title;
    if (windowName) {
        this.windowName = windowName;
    }
}

PopupDialogCache = {
    popupDialog: null
}

PopupDialog.closeDialog = function() {
    var cacheObj = PopupDialogCache.popupDialog;
    if (cacheObj != null) {
        cacheObj.close();
    }
    else if (cacheObj == null && opener) {
        try {
            cacheObj = opener.PopupDialogCache.popupDialog;
            cacheObj.close();
        }
        catch(e) {
            window.close();
        }
    }
    else if (cacheObj == null && parent && parent.PopupDialogCache && parent.PopupDialogCache.popupDialog) {
        cacheObj = parent.PopupDialogCache.popupDialog;
        cacheObj.close();
    }
}

PopupDialog.prototype = {

  width: 860,
  height: 520,
  title: ' ',
  src: null,
  windowName: null,
  popupDialog: null,
  popupWindow: null,

  init: function() {
      this.show();
  },

  show: function() {
      // hide help
      HelpGuide.hide();
      
      var newSrc = this.src;
      if (newSrc.indexOf("?") < 0) {
          newSrc += "?";
      }
      newSrc += "&_=" + new Date().valueOf().toString();
      newSrc += UI.userviewThemeParams();
              
      PopupDialogCache.popupDialog = this;

      if (this.windowName) {
          var opts = (this.width && this.height) ? "width=" + this.width + ",height=" + this.height + ",resizable=1,scrollbars=1,top=50,left=50" : null;
          if (opts) {
            this.popupWindow = window.open(this.src, this.windowName, opts);
          } else {
            this.popupWindow = window.open(this.src, this.windowName);  
          }
          return;
      }
      
      try {
        if (parent && parent.UI !== undefined && window.frameElement !== null && window.frameElement.id !== "quickOverlayFrame") {
            $("html").css("background", "#fff");
            parent.UI.maxIframe(window.frameElement.id);
        }
      } catch (err) {}
      
      var temWidth = $(window).width();
      var temHeight = $(window).height();
      if (temWidth >= 768) {
          this.width = temWidth * 0.8;
          this.height = temHeight * 0.9;
      } else {
          this.width = temWidth - 20;
          this.height = temHeight - 20;
      }
      
      var thisObject = this;
      var newDiv = document.getElementById("jqueryDialogDiv");
      var newFrame = document.getElementById("jqueryDialogFrame");
      if (!newDiv) {
          newDiv = document.createElement("DIV");
          newDiv.setAttribute("id", "jqueryDialogDiv");
          if (!newFrame) {
              newFrame = document.createElement("IFRAME");
              newFrame.setAttribute("id", "jqueryDialogFrame");
              newFrame.setAttribute("name", "jqueryDialogFrame");
              newFrame.setAttribute("frameborder", "0");
              newFrame.setAttribute("width", "100%");
              if (UI.userview_app_id === undefined || UI.userview_app_id === '') {
                  newFrame.setAttribute("height", this.height-20);
                  newFrame.setAttribute("scrolling", "no");
              } else {
                  newFrame.setAttribute("height", this.height-10);
              }
              newFrame.onload = function() {
                    try {
                        var url = newFrame.contentWindow.location.href;
                        if (url.indexOf("/web/userview/") !== -1 || url.indexOf("&__a_=") !== -1) {
                            newFrame.setAttribute("scrolling", "yes");
                            newFrame.setAttribute("height", thisObject.height-10);
                        }
                    } catch (err) {}
                    
                    if (/iPhone|iPod|iPad/.test(navigator.userAgent)) {
                        $(document).scrollTop(0);
                        $('#jqueryDialogDiv').height($('#jqueryDialogFrame').height());
                    }
              };
              
              newDiv.appendChild(newFrame);
              document.body.appendChild(newDiv);
          }
            if (/iPhone|iPod|iPad/.test(navigator.userAgent)) {
                $('#jqueryDialogDiv').css({
                    overflow: 'visible'
                });
            }
      }

      var openDialog = function() {
            var newFrame = document.getElementById("jqueryDialogFrame");
            if (newFrame != null) {
                newFrame.setAttribute("src", newSrc); 
                setTimeout(function() { 
                    newFrame.contentWindow.focus();
                }, 100);
                $(newFrame).addClass("iframeloading");
            }
            
            var temWidth = $(window).width();
            var temHeight = $(window).height();
            if (temWidth >= 768) {
                this.width = temWidth * 0.8;
                this.height = temHeight * 0.9;
            } else {
                this.width = temWidth - 20;
                this.height = temHeight - 20;
            }
            if (UI.userview_app_id === undefined || UI.userview_app_id === '') {
                newFrame.setAttribute("height", this.height-20);
            } else {
                newFrame.setAttribute("height", this.height-10);
            }
            
            if (/iPhone|iPod|iPad/.test(navigator.userAgent)) {
                $(".ui-dialog.ui-widget").css("position", "absolute");
                $(".ui-dialog.ui-widget").css("top", "5%");
                $('.ui-dialog .ui-dialog-titlebar-close').css({
                    'z-index' : '99999',
                    'height' : '30px',
                    'width' : '30px',
                    'background-color' : 'red'
                });
            } else {
                $(".ui-dialog.ui-widget").css("position", "fixed");
                $(".ui-dialog.ui-widget").css("top", "5%");
                $('body').addClass("stop-scrolling");
            }
            
            $('.ui-widget-overlay').off('click');
            $('.ui-widget-overlay').on('click',function(){
                PopupDialogCache.popupDialog.close();
            });
            
            $(this).parents('.ui-dialog').find('.ui-dialog-titlebar-close').blur();
      }
      var closePopupDialog = function() {
          $('body').removeClass("stop-scrolling");
          var newFrame = document.getElementById("jqueryDialogFrame");
          if (newFrame != null) {
              newFrame.setAttribute("src", "");
          }
          
          try {
            if (parent && parent.UI !== undefined && window.frameElement !== null && window.frameElement.id !== "quickOverlayFrame") {
                  parent.UI.restoreIframe(window.frameElement.id);
                  $("html").css("background", "transparent");
            }
          } catch (err) {}
      }
      
      this.popupDialog = $(newDiv).dialog({
          modal: true,
          //title: this.title,
          //show: 'drop',
          //hide: 'scale',
          //minWidth: this.width,
          //minHeight: this.height,
          width: this.width,
          height: this.height,
          position: { my: 'center' },
          draggable: false,
          autoOpen: true,
          resizable: false,
          overlay: {
              opacity: 0.5,
              background: "black"
          },
          open: openDialog,
          close: closePopupDialog,
          closeText: '',
          zIndex: 15001
      });
  },

  close: function() {
      $('body').removeClass("stop-scrolling");
      var result = false;
      if (this.popupWindow) {
          this.popupWindow.close();
          this.popupWindow = null;
          result = true;
      }
      else {
          if (this.popupDialog != null) {
              this.popupDialog.dialog("close");
              this.popupDialog = null;
              result = true;
          }
          PopupDialogCache.popupDialog = null;
      }
      return result;
  }

}

/*
 * Link object to represent a clickable link that either redirects to a new URL or pops up a dialog
 */
Link = function(href, param, queryString, popupDialog) {
    this.href = href;
    this.param = param;
    this.queryString = queryString;
    this.popupDialog = popupDialog;
}

Link.prototype = {

    href: null,
    param: null,
    value: null,
    suffix: null,
    queryString: false,
    post: false,
    popupDialog: null,

    init: function() {
        var link = this.href;
        if (this.param) {
            if (!this.queryString) {
                var endsWith = link.match("/$") == "/";
                if (!endsWith) {
                    link += "/";
                }
                link += this.value;
                if (this.suffix) {
                    link += this.suffix;
                }
            }
            else {
                var hasQueryString = link.indexOf("?") >= 0;
                if (!hasQueryString) {
                    link += "?";
                }
                link += this.param + "=" + this.value;
            }
        }

        if (this.popupDialog) {
            this.popupDialog.src = link;
            this.popupDialog.show.apply(this.popupDialog);
        }
        else if (this.post) {
            if (link.indexOf("?") < 0) {
                link += "?";
            }
            link += "&" + ConnectionManager.tokenName + "=" + ConnectionManager.tokenValue;
            var $form = $("#ui_link_form:first");
            if ($form.length > 0){
                $form.attr("method", "POST");
                $form.attr("action", link);
            } else {
                $form = $("<form method='POST' class='blockui' action='" + link + "'></form>");
                $(document).append($form);
            }
            $form.submit();
        }
        else {
            window.location = link;
        }

    }

}

/*
 * JSON Datatable
 */
JsonTable = function(divToUpdate, url) {
    // set config
    this.divToUpdate = divToUpdate;
    this.url = url;
};

JsonTable.initialized = false;

JsonTable.prototype = {

    divToUpdate: '',
    url: '',
    jsonData: 'data',
    useRp: true,
    rowsPerPage: 10,
    width: '600',
    height: '298',
    sort: null,
    desc: false,
    checkbox: false,
    checkboxSelectSingle: false,
    checkboxSelection: false,
    fields: [],
    columns: [],
    history: null,
    link: null,
    dataTable: null,
    dataSource: null,
    paginator: null,
    initialState: '',
    flexiGrid: null,
    key: 'id',
    buttons: null, // array of buttons eg [{ name:'Label', cssClass:'cssClass', callback: callbackFunction}]
    customPreProcessor: null,

    init: function() {
        var thisObject = this;

        var handleRowSelection = function (celDiv, id) {
            if ($(celDiv).find('input[type="checkbox"], input[type="radio"]').length > 0) {
                $(celDiv).addClass("selectionTd");
            }
        };

        // define columns
        var gridColumns = this.columns;
        {
            var idx;
            var total = 0;
            var selectionWidth = 0;
            for (idx = 0; idx < gridColumns.length; idx++) {
                var col = gridColumns[idx];
                if (col.key) {
                    col.name = col.key;
                }
                if (col.label) {
                    col.display = col.label;
                }
                if (!col.width) {
                    if (typeof thisObject.width == "string" && thisObject.width.charAt(thisObject.width.length - 1) == "%") {
                        col.width = 150;
                    } else if (typeof thisObject.width == "string" && thisObject.width.substring(thisObject.width.length - 2) == "px") {
                        col.width = 150;
                    } else {
                        col.width = Math.round(thisObject.width / gridColumns.length) - 20;
                    }
                } else {
                    if (typeof col.width == "string") {
                        if (col.width.charAt(col.width.length - 1) == "%") {
                            try {
                                col.width = ($("body").width() - 50) / 100 * parseInt(col.width.substring(0, col.width.length - 1));
                            } catch (err) {
                                col.width = 200;
                            }
                        } else if (col.width.substring(col.width.length - 2) == "px") {
                            col.width = parseInt(col.width.substring(0, col.width.length - 2));
                        } else {
                            col.width = parseInt(col.width);
                        }
                    }
                }
                if (col.name === "checkbox" || col.name === "radio") {
                    selectionWidth += col.width;
                } else {
                    total += col.width;
                }
                col.process = handleRowSelection;
                gridColumns[idx] = col;
            }
            var tableWidth = $("#" + thisObject.divToUpdate).parent().width() - 40 - selectionWidth;
            if (total < tableWidth) {
                for (idx = 0; idx < gridColumns.length; idx++) {
                    if (gridColumns[idx].name === "checkbox" || gridColumns[idx].name === "radio") {
                        continue;
                    }
                    gridColumns[idx].width = gridColumns[idx].width / total * tableWidth;
                }
            }
        }

        var dataPreProcess = function(jsonObject) {
            //custom pre-processor
            if(thisObject.customPreProcessor)
                jsonObject = thisObject.customPreProcessor(jsonObject);

            var key = (thisObject.link) ? thisObject.link.param : thisObject.key;
            var data = jsonObject.data;
            var i, j;
            var newRows = new Array();
            if (!data) {
                data = new Array();
            }
            else if (data && !data.length) {
                data = [ data ];
            }
            for (i=0; i<data.length; i++) {
                var cell = new Array();
                var row = data[i];
                if (row[key]) {
                    var newRow = new Object();
                    newRow.id = encodeURIComponent(row[key].replace(/\./g, '__dot__'));
                    
                    for (j=0; j<this.colModel.length; j++) {
                        var prop = this.colModel[j].name;
                        if(thisObject.checkbox && prop == 'checkbox'){
                            var check = '';
                            if($("#"+thisObject.divToUpdate+"_selectedIds").html().indexOf(row[key]) != -1){
                                check = 'checked="true"';
                            }
                            row[prop]='<input type="checkbox" class="' + thisObject.divToUpdate + '-checkbox-list" id="' + thisObject.divToUpdate + '_checkbox_' + i + '" ' + check + 'onclick="toggleCheckbox(\'' + thisObject.divToUpdate + '_checkbox_' + i + '\')">';
                        }else if(thisObject.checkbox && thisObject.checkboxSelectSingle && prop == 'radio'){
                            row[prop]='<input type="radio" id="' + thisObject.divToUpdate +'_radio_' + i + '" name="' + thisObject.divToUpdate + '_radio" onclick="' + thisObject.divToUpdate + '_toggleRadioButton(\'' + thisObject.divToUpdate + '_radio_' + i + '\')">';
                        }else {
                            var relaxed = this.colModel[j].relaxed;
                            if (!relaxed) {
                                row[prop]= UI.escapeHTML(row[prop]);
                            }
                        }
                        cell.push(row[prop]);
                    }
                    
                    newRow.cell = cell;
                    newRows.push(newRow);
                }
            }
            var returnObject = new Object();
            returnObject.total = jsonObject.total;
            returnObject.rows = newRows;
            returnObject.page = this.newp;



            return returnObject;
        };

        var handleButtonClick = function(command, grid) {
            var i;
            for (i=0; i<thisObject.buttons.length; i++) {
                var name = thisObject.buttons[i].name;
                if (name == command) {
                    var callback = thisObject.buttons[i].callback;
                    var selectedRows = thisObject.getSelectedRows();
                    var functionCall = callback + "(selectedRows)";
                    //alert(command + ": " + thisObject.getSelectedRows() + "; " + functionCall);
                    var result = eval(functionCall);
                    //alert(result);
                    return result;
                }
            }
        }

        var gridButtons = null;
//        if (this.checkbox) {
//            gridButtons = new Array();
//            gridButtons.push({name: 'Select', bclass: 'add', onpress : test});
//        }
        if (this.buttons) {
            gridButtons = new Array();
            var i;
            for (i=0; i<this.buttons.length; i++) {
                var label = this.buttons[i].name;
                var callback = this.buttons[i].callback;
                var cssClass = this.buttons[i].cssClass;
                if (!cssClass) {
                    cssClass = "add";
                }
                gridButtons.push({name: label, bclass: cssClass,
                    onpress: function(command, grid) {
                        handleButtonClick(command, grid);
                    }
                });
            }
        }

        var gridSearchItems = null;

        var newUrl = thisObject.url;
        if (newUrl.indexOf("?") < 0) {
            newUrl += "?";
        }
        newUrl += "&_=" + new Date().valueOf().toString();
        $("#" + thisObject.divToUpdate).addClass("jsontable");
        $("#" + thisObject.divToUpdate).data("jsontable", this);

        // create flexigrid
        this.flexiGrid = $("#" + thisObject.divToUpdate).flexigrid({
            url: newUrl,
            dataType: 'json',
            method: 'GET',
            colModel : gridColumns,
            buttons : gridButtons,
            searchItems: gridSearchItems,
//                {name: 'Add', bclass: 'add', onpress : test},
//                {name: 'Delete', bclass: 'delete', onpress : test},
//                {separator: true}
//            ],
//            searchitems : [
//                {display: 'Name', name : 'name'},
            sortname: thisObject.sort,
            sortorder: thisObject.desc ? "desc" : "asc",
            usepager: true,
            //title: '',
            useRp: thisObject.useRp,
            rp: thisObject.rowsPerPage,
            rpOptions: [10, 15, 20, 25, 50, 100],
            showTableToggleBtn: true,
            width: thisObject.width,
            height: thisObject.height,
            resizable: false,
            singleSelect: !thisObject.checkbox || thisObject.checkboxSelectSingle,
            preProcess: dataPreProcess
        });
        
        $("#" + thisObject.divToUpdate).on("click", "*", function(){
            if ($(this).closest(".jsontable tbody tr").length > 0) {
                var row = $(this).closest("tr");
                var id = $(row).attr("id").substring(3);
                var checkboxSelected = $(this).hasClass("selectionTd") || $(this).closest(".selectionTd").length > 0 || $(this).find(".selectionTd").length > 0;
                if (thisObject.link && !checkboxSelected) {
                    thisObject.link.value = id.replace(/__dot__/g, '.');
                    thisObject.link.init();
                    return false;
                } else if (!checkboxSelected) {
                    var checkbox = row.find('input[type="checkbox"], input[type="radio"]');
                    if (checkbox.length > 0) {
                        var cb = $(checkbox[0]);
                        if (thisObject.checkboxSelectSingle) {
                            cb.click();
                        } else {
                            cb.prop("checked", !cb.prop("checked"));
                            toggleCheckbox(cb.attr("id"));
                        }
                    }
                    return false;
                }
            }
            return true;
        });
    },

    refresh: function() {
        var thisObject = this;
        var newUrl = thisObject.url;
        if (newUrl.indexOf("?") < 0) {
            newUrl += "?";
        }
        newUrl += "&_=" + new Date().valueOf().toString();

        this.flexiGrid.flexReload({
            url: newUrl
        });
    },

    load: function(url) {
        if (url) {
            url += "&_=" + new Date().valueOf().toString();
            this.flexiGrid[0].p.newp = 1;
            this.flexiGrid.flexReload({
                url: url
            });
        }
    },

    closeDialog: function() {
        if (this.link && this.link.popupDialog) {
            this.link.popupDialog.close();
        }
    },

    getSelectedRows: function() {
        var selected = new Array();
        if (this.checkbox && this.checkboxSelectSingle) {
            var rows = $('.trSelected', this.flexiGrid);
            var i;
            for (i=0; i<rows.length; i++) {
                var id = rows[i].id.substring(3).replace(/__dot__/g, '.'); // strip prefix "row"
                selected.push(id);
            }
        }else if($("#"+this.divToUpdate+"_selectedIds").html() != null){
            var ids = $("#"+this.divToUpdate+"_selectedIds").html().substring(1, $("#"+this.divToUpdate+"_selectedIds").html().length);
            selected = ids.split(",");
        }
        return selected;
    },

    clearSelectedRows: function() {
        $("#"+this.divToUpdate+"_selectedIds").html("");
    }
}

/**
 * TreeView loading from a JSON source
 */
JsonTree = function(divToUpdate, nodeUrl) {
    // set config
    this.divToUpdate = divToUpdate;
    this.nodeUrl = nodeUrl;
};

JsonTree.callbacks = [];

JsonTree.prototype = {

    divToUpdate: '',
    nodeUrl: '',
    baseUrl: '',
    title: '',
    jsonData: 'data',
    nodeKey: 'id',
    nodeLabel: 'label',
    nodeDescription: 'description',
    nodeCount: 'count',
    xss: false,

    treeView: {},
    link: null,

    init: function() {
        var thisObject = this;

        var rootUrl = thisObject.nodeUrl;
        if (thisObject.xss) {
            if (rootUrl.indexOf("?") < 0) {
                rootUrl += "?";
            }
            rootUrl += "&callback=JsonTree.callbacks['" + thisObject.divToUpdate + "']";
            JsonTree.callbacks[thisObject.divToUpdate] = function(o) {
                var root = $(thisObject.treeView).dynatree("getRoot");
                if (o) {
                    root.appendData(o);
                }
            }
        }

        thisObject.treeView = $("#" + thisObject.divToUpdate).dynatree({
            title: thisObject.title,
            rootVisible: (thisObject.title != null && thisObject.title != ''),
            onSelect: function(dtnode) {
                if( dtnode.data[thisObject.nodeKey] ) {
                    if (thisObject.link) {
                        thisObject.link.value = dtnode.data[thisObject.nodeKey];
                        thisObject.link.init();
                    }
                    dtnode.unselect();
                    dtnode.tree.tnSelected = null;
                }
            },
            initAjax: {
                url: rootUrl
                ,dataType: 'json'
                ,xss: thisObject.xss
                //,data: {rnd: new Date().valueOf().toString()}
            },
            onLazyRead: function(dtnode){
                //window.setTimeout(fakeAjaxResponse(), 1500);
                var nodeUrl = dtnode.data.url;
                if (nodeUrl) {
                    var tmpUrl = thisObject.baseUrl;
                    if (tmpUrl.match("/$") != "/" && nodeUrl.charAt(0) != "/") {
                        tmpUrl += "/";
                    }
                    nodeUrl = tmpUrl + nodeUrl;
                    if (thisObject.xss) {
                        if (nodeUrl.indexOf("?") < 0) {
                            nodeUrl += "?";
                        }
                        nodeUrl += "&callback=JsonTree.callbacks['" + thisObject.divToUpdate + "']";
                        JsonTree.callbacks[thisObject.divToUpdate] = function(o) {
                            dtnode.appendData(o);
                        }
                    }
                    dtnode.appendAjax({
                        url: nodeUrl
                        ,dataType: 'json'
                        ,xss: thisObject.xss
                    });
                }
            }
        });

    },

    refresh: function() {
        var root = $(this.treeView).dynatree("getRoot");
        var el = root.div.parentNode;
        el.removeChild(root.div);
        root.aChilds = null;
        this.init();
    },

    closeDialog: function() {
        if (this.link && this.link.popupDialog) {
            this.link.popupDialog.close();
        }
    }

};

TabView = function(div, orientation){
    this.div = div;
    this.orientation = orientation;
}

TabView.prototype = {
    div: '',
    orientation: 'top',
    tabView: '',
    selectFunction: null, // callback function onSelect


    init : function(){
        //var thisObject = this;

        this.tabView = $("#" + this.div).tabs({
            activate: function( event, ui ) {
                $(ui.newPanel).find(".jsontable").each(function(){
                    var jsontable = $(this).data("jsontable");
                    jsontable.refresh();
                });
            }
        });
    },

    getTab : function(index){
        //return this.tabView.getTab(index);

    },

    select: function(index) {
        this.tabView.tabs("option", "active", $(index).index());
    },

    disable: function(index) {
        this.tabView.tabs("disable", index);
    }

}

Calendar = {
    show: function(id) {
        $("#" + id).datepicker({ dateFormat: 'yy-mm-dd' });
    }
}

Menu = {
    show: function(id) {
        jquerycssmenu.buildmenu(id, arrowimages);
    }
}


BubbleDialog = {
    show: function(element, content, top, left, callback) {
        this.updatePosition(element, content, top, left);

        $(content).css("display", "block");
        $(content).show("slide", callback);

        var thisObject = this;
        $(window).resize(function(){
            thisObject.updatePosition(element, content, top, left);
        })
    },

    updatePosition : function(element, content, top, left) {
        //get the position of the placeholder element
        var pos = $(element).offset();
        var width = $(element).width();
        var cTop = (pos.top) + "px";
        if(top != null){
            cTop = (pos.top + parseInt(top)) + "px";
        }

        cLeft = (pos.left + width) + "px"
        if(left != null){
            cLeft = (pos.left + width + parseInt(left)) + "px"
        }

        //show the menu directly over the placeholder
        $(content).css( { "left": cLeft, "top": cTop } );
    }
}

HelpGuide = {
    
    prefix: "help.",
    url: "/web/help/guide?locale=" + UI.locale,
    attachTo: null,
    key: null,
    definition: null,
    
    clear: function(){
        HelpGuide.hide();
        if(window['guiders'] != undefined){
            guiders._guiders = {};
        }
        $("#main-action-help").remove();
    },
    
    enable: function() {
        $.cookie("helpGuide", "true", { expires: 3650, path:HelpGuide.base });
    },
    
    disable: function() {
        HelpGuide.hide();
        $.cookie("helpGuide", "false", { expires: 3650, path:HelpGuide.base });
    },
    
    isEnabled: function() {
        var status = $.cookie("helpGuide");
        return (status != "false");
    },

    toggle: function() {
        if (HelpGuide.isEnabled()) {
            HelpGuide.disable();
        } else {
            HelpGuide.enable();
        }
    },
    
    show: function() {
        // determine key
        var helpKey = (HelpGuide.key == null) ? HelpGuide.determineKey() : HelpGuide.key;
        
        // display key in footer for debugging
//        if ($("#helpKey").length == 0) {
//            $(document.body).append($("<div id='helpKey'></div>"));
//        }
//        $("#helpKey").text(helpKey);

        // ajax request to get help definition
        $.ajax({
            type: "GET",
            data: {
                "key": helpKey
            },
            dataType : "text",
            url: HelpGuide.base + HelpGuide.url,
            success: function(response) {
                var helpDef = response;
                HelpGuide.startGuide(helpDef);
            }
        });
        
        $(window).on("resize scroll", function() {
            HelpGuide.reposition();
        });
    },
    
    hide: function() {
        if(window['guiders'] != undefined){
            guiders.hideAll();
        }
        $('body .guider_hloverlay').hide();
    },
    
    determineKey: function() {
        // parse URL path
        var key = '';
        var regex = HelpGuide.base + "\\/(.*)";
        var match = location.pathname.match(regex);
        if (match && match.length > 0) {
            key = match[1];
        }
        if (key != '') {
            key = key.replace(/\/\//g, "/");
            key = key.replace(/\//g, ".");
            key = HelpGuide.prefix + key;
        }
        return key;
    },

    insertButton: function(div) {
        // create button
        var button = $('<a id="main-action-help"><i class="fa fas fa-info-circle"></i></a>');
        
        // insert button
        if ($("#main-action-help").length == 0) {
            if (!div) {
                div = document.body;
            }
            $(div).append(button);
        }
        
        // display icon and set event handler
        $("#main-action-help").show();
        $("#main-action-help").click(function(e) {
            e.preventDefault();
            HelpGuide.hide();
            HelpGuide.enable();
            HelpGuide.show();
        });

    },
    
    startGuide: function(helpJson) {
        // eval definition
        var helpDefObj;
        if (helpJson != "") {
            try {
                helpDefObj = eval(helpJson);
            } catch (e) {
                //alert(e);
            }
        }
        if (helpDefObj == null) {
            // use default
            helpDefObj = HelpGuide.definition;
        }        

        // display guides
        if (helpDefObj && helpDefObj.length > 0) {
            // show button
            HelpGuide.insertButton(HelpGuide.attachTo);
            
            // check activated status
            var active = HelpGuide.isEnabled();
            if (active) {
                setTimeout(function() {
                    // loop thru guides
                    for (i=0; i<helpDefObj.length; i++) {
                        var def = helpDefObj[i];
                        HelpGuide.displayGuide(def, i, helpDefObj.length);
                    }
                }, 500);
            }
        }
    },
    
    displayGuide: function(def, i, total) {
        if(window['guiders'] != undefined){
            var guider = guiders._guiders[def.id]; 
            if (!guider) {
                def.onShow = HelpGuide.guiderOnShow;
                
                def.current = i;
                def.steps = total;
                
                //auto adjust position
                if (def.position !== undefined && def.position !== 0) {
                    var atOffset = $(def.attachTo).offset();
                    var atWidth = $(def.attachTo).outerWidth();
                    var atHeight = $(def.attachTo).outerHeight();
                    var scWidth = $(window).width();
                    var scHeight = $(window).height();
                    var wlimit = atOffset.left + atWidth + 400;
                    var hlimit = atOffset.top + atHeight + 150;
                    if (def.position >= 2 & def.position <= 4) { //right
                        if (wlimit > scWidth) {
                            if (atOffset.left - 400 < 0) {
                                def.position = 0;
                            } else {
                                if (def.position === 2) {
                                    def.position = 10;
                                } else if (def.position === 3) {
                                    def.position = 9;
                                } else if (def.position === 4) {
                                    def.position = 8;
                                }
                            }
                        }
                    } else if (def.position >= 8 & def.position <= 10) { //left
                        if (atOffset.left - 400 < 0) {
                            if (wlimit > scWidth) {
                                def.position = 0;
                            } else {
                                if (def.position === 10) {
                                    def.position = 2;
                                } else if (def.position === 9) {
                                    def.position = 3;
                                } else if (def.position === 8) {
                                    def.position = 4;
                                }
                            }
                        }
                    } else if (def.position >= 5 & def.position <= 7) { //bottom
                        if (hlimit > scHeight) {
                            if (atOffset.top - 150 < 0) {
                                def.position = 0;
                            } else {
                                if (def.position === 5) {
                                    def.position = 1;
                                } else if (def.position === 6) {
                                    def.position = 12;
                                } else if (def.position === 7) {
                                    def.position = 11;
                                }
                            }
                        }
                    } else { //top
                        if (atOffset.top - 150 < 0) {
                            if (hlimit > scHeight) {
                                def.position = 0;
                            } else {
                                if (def.position === 1) {
                                    def.position = 5;
                                } else if (def.position === 12) {
                                    def.position = 6;
                                } else if (def.position === 11) {
                                    def.position = 7;
                                }
                            }
                        }
                    }
                }
                
                guider = guiders.createGuider(def);
                if (def.show) {
                    guider.show();
                }
            } else if (def.show) {
                guiders.show(def.id);
            }
        }
    },
    
    guiderOnShow : function(guider) {
        if (guider.init === undefined) {
            //add steps to guider
            $(guider.elem).find('.guider_buttons').append('<spn class="steps">'+(guider.current+1)+'/'+guider.steps+'</span>')
            guider.init = true;
        }
        
        if (guider.script !== undefined) {
            try {
                eval(guider.script);
            } catch (err) {}
        }
        
        if (guider.highlight === undefined) {
            guider.highlight = guider.attachTo;
        }
        
        //if highlight
        if (guider.highlight !== false) {
            if ($('body .guider_hloverlay').length === 0) {
                $('body').append('<div class="guider_hloverlay top"></div><div class="guider_hloverlay right"></div><div class="guider_hloverlay bottom"></div><div class="guider_hloverlay left"></div>');
            }

            var offset = $(guider.highlight).offset();
            var width = $(guider.highlight).outerWidth();
            var height = $(guider.highlight).outerHeight();
            var pad = 5;
            $('.guider_hloverlay.top').css({top:'0px', left: (offset.left-pad) + 'px', width: (width+pad+pad) + 'px', height: (offset.top - pad) + 'px'});
            $('.guider_hloverlay.right').css({top:'0px', left: (offset.left+width+pad) + 'px', bottom: '0px', right: '0px'});
            $('.guider_hloverlay.bottom').css({top:(offset.top+height+pad) + 'px', left: (offset.left-pad) + 'px', width: (width+pad+pad) + 'px', bottom: '0px'});
            $('.guider_hloverlay.left').css({top:'0px', left: '0px', bottom: '0px', width: (offset.left-pad) + 'px'});

            $('body .guider_hloverlay').show();
        } else {
            $('body .guider_hloverlay').hide();
        }
    },
    
    reposition: function() {
        var g;
        for(g in guiders._guiders) {
            guiders._attach(guiders._guiders[g]);
        }        
    }

};

/* adding content placeholder to iframe while it is loading*/
if (window.self !== window.parent && window.frameElement) {
    window.addEventListener('load', function(event) {
        $(window.frameElement).removeClass("iframeloading");
    });
    window.addEventListener('unload', function(event) {
        $(window.frameElement).addClass("iframeloading");
    });
}