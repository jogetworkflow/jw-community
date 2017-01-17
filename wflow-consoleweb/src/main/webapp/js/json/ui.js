UI = {
   base: '',
   userview_app_id: '',
   userview_id: '',
   locale: '',
   escapeHTML: function(c) {
      if (c == null || c == undefined) {
          return '';
      } else {
          var span = $('<span></span>').text(c);
          return span.html();
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
            if ($(this).attr("href") === undefined || $(this).attr("href") === null) {
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
       var minHeight = 200;
       var maxHeight = windowHeight - 100;
           
       if (isNaN(height) && height.indexOf("%") !== -1) {
           var tempHeight = parseFloat(height.replace("%", ""));
           height = windowHeight * tempHeight / 100;
       }
       
       if (height > maxHeight) {
           height = maxHeight;
       }
       
       if (height < minHeight) {
            height = minHeight;
       }
       return height;
   }, 
   getPopUpWidth: function(width) {
       if (width === undefined || width === "") {
           width = "90%";
       }
       var windowWidth = $(window).width();
       var minWidth = 200;
       var maxWidth = windowWidth - 100;
           
       if (isNaN(width) && width.indexOf("%") !== -1) {
           var tempWidth = parseFloat(width.replace("%", ""));
           width = windowWidth * tempWidth / 100;
       }
       
       if (width > maxWidth) {
           width = maxWidth;
       }
       
       if (width < minWidth) {
           width = minWidth;
       }
       return width;
   },
   adjustPopUpDialog: function(dialogbox) {
       // center dialogbox
       dialogbox.center('x');
       dialogbox.center('y');
   }
}

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
                  newFrame.setAttribute("height", this.height-40);
              }
              if (/iPhone|iPod|iPad/.test(navigator.userAgent)) {
                  newFrame.onload = function() {
                      $(document).scrollTop(0);
                      $('#jqueryDialogDiv').height($('#jqueryDialogFrame').height());
                  };
              }
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
                setTimeout(function() { 
                    newFrame.setAttribute("src", newSrc); 
                    newFrame.contentWindow.focus();
                }, 100);
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
            $(this).parents('.ui-dialog').find('.ui-dialog-titlebar-close').blur();
      }
      var closePopupDialog = function() {
          $('body').removeClass("stop-scrolling");
          var newFrame = document.getElementById("jqueryDialogFrame");
          if (newFrame != null) {
              newFrame.setAttribute("src", "");
          }
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
          position: 'center',
          draggable: false,
          autoOpen: true,
          resizable: false,
          overlay: {
              opacity: 0.5,
              background: "black"
          },
          open: openDialog,
          close: closePopupDialog,
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
                link += escape(this.value);
                if (this.suffix) {
                    link += this.suffix;
                }
            }
            else {
                var hasQueryString = link.indexOf("?") >= 0;
                if (!hasQueryString) {
                    link += "?";
                }
                link += this.param + "=" + escape(this.value);
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
                $form = $("<form method='POST' action='" + link + "'></form>");
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
            $(celDiv).click (
                function () {
                    var row = $(celDiv).parents("tr:first");
                    var checkbox = row.find('input[type="checkbox"], input[type="radio"]');
                    var checkboxSelected = (checkbox.length > 0) && $(celDiv).find('input[type="checkbox"], input[type="radio"]').length > 0;
                    if (thisObject.link && !checkboxSelected) {
                        thisObject.link.value = id.replace(/__dot__/g, '.');
                        thisObject.link.init();
                        return false;
                    } else {
                        if (!checkboxSelected) {
                            var cb = $(checkbox[0]);
                            if (thisObject.checkboxSelectSingle) {
                                cb.click();
                            } else {
                                cb.attr('checked', !cb.attr('checked'));
                                toggleCheckbox(cb.attr("id"));
                            }
                        }
                        return true;
                    }
                });
        }

        // define columns
        var gridColumns = this.columns;
        {
            var idx;
            for (idx=0; idx<gridColumns.length; idx++) {
                var col = gridColumns[idx];
                if (col.key) {
                    col.name = col.key;
                }
                if (col.label) {
                    col.display = col.label;
                }
                if (!col.width) {
                    //alert(thisObject.width + "," + thisObject.width.charAt(thisObject.width.length-1));
                    if (typeof thisObject.width == "string" && thisObject.width.charAt(thisObject.width.length-1) == "%") {
                        col.width = 150;
                    }
                    else if (typeof thisObject.width == "string" && thisObject.width.substring(thisObject.width.length-2) == "px") {
                        col.width = 150;
                    }
                    else {
                        col.width = Math.round(thisObject.width/gridColumns.length) - 20;
                    }
                }
                col.process = handleRowSelection;
                gridColumns[idx] = col;
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
                var newRow = new Object();
                newRow.id = encodeURIComponent(row[key].replace(/\./g, '__dot__'));
                newRow.cell = cell;
                newRows.push(newRow);
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
            showTableToggleBtn: true,
            width: thisObject.width,
            height: thisObject.height,
            resizable: false,
            singleSelect: !thisObject.checkbox || thisObject.checkboxSelectSingle,
            preProcess: dataPreProcess
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

        this.tabView = $("#" + this.div).tabs();
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
    },
    
    hide: function() {
        if(window['guiders'] != undefined){
            guiders.hideAll();
        }
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
        var button = $('<span id="main-action-help"></span>');
        
        // insert button
        if ($("#main-action-help").length == 0) {
            if (!div) {
                div = document.body;
            }
            $(div).prepend(button);
        }
        
        // display icon and set event handler
        $("#main-action-help").css("display", "block");
        $("#main-action-help").click(function() {
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
                        HelpGuide.displayGuide(def);
                    }
                }, 500);
            }
        }
    },
    
    displayGuide: function(def) {
        if(window['guiders'] != undefined){
            var guider = guiders.createGuider(def);
            if (def.show) {
                guider.show();
            }
        }
    }
    
}