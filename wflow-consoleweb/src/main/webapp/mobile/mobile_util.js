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
        var framewidth = Math.round(this.width - 40);
        var frameHeight = Math.round(this.height - 40);
        if (!newDiv) {
            newDiv = document.createElement("DIV");
            newDiv.setAttribute("id", "jqueryDialogDiv");
            newDiv.setAttribute("data-role", "popup");
            newDiv.setAttribute("data-overlay-theme", "a");
            newDiv.setAttribute("data-theme", "d");
            newDiv.setAttribute("data-tolerance", "15,15");
            newDiv.setAttribute("class", "ui-content");
            
            var closeButton = document.createElement("A");
            closeButton.setAttribute("href", "#");
            closeButton.setAttribute("data-rel", "back");
            closeButton.setAttribute("class", "ui-btn ui-corner-all ui-shadow ui-btn-a ui-icon-delete ui-btn-icon-notext ui-btn-right");
            newDiv.appendChild(closeButton);
            
            if (!newFrame) {
                newFrame = document.createElement("IFRAME");
                newFrame.setAttribute("id", "jqueryDialogFrame");
                newFrame.setAttribute("name", "jqueryDialogFrame");
                newFrame.setAttribute("frameborder", "0");
                newFrame.setAttribute("width", framewidth);
                newFrame.setAttribute("height", frameHeight);
                newFrame.setAttribute("seamless", "seamless");
                
                if (UI.userview_app_id === undefined || UI.userview_app_id === '') {
                    frameHeight = this.height - 20;
                    newFrame.setAttribute("scrolling", "no");
                }
                newDiv.appendChild(newFrame);
                document.body.appendChild(newDiv);
            }
        }

        $(newDiv).on({
            popupbeforeposition: function() {
                $(newDiv).find("iframe")
                        .attr("width", framewidth)
                        .attr("height", frameHeight);
                $(newDiv).find("iframe").show();
            },
            popupafterclose: function() {
                $(newDiv).remove();
            }
        });
        this.popupDialog = $(newDiv).popup();
        var popupDialog = this.popupDialog;
        
        setTimeout(function() { 
            popupDialog.popup("open");
            newFrame.setAttribute("src", newSrc);
        }, 500);
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
              this.popupDialog.popup("close");
              this.popupDialog = null;
              result = true;
          }
          PopupDialogCache.popupDialog = null;
      }
      return result;
  }

}