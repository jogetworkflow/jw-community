(function($){
    
    var methods = {
        /**
         * Init the listing with super app feature
         */
        init: function() {
            return this.each(function(){
                var target = this;
                
                if($(target) && !$(target).hasClass("initialized")){
                    var s = $(target).data("super_app_settings");
                    var container = $(target).closest(".dataList").parent().parent();
                    $(container).attr("data-ajax-component", "");
                    $(container).attr("ajax-content-placeholder", "dashboard");

                    var mode = $(target).data("super_app_mode");
                    if (mode === "edit") {
                        methods.initEditMode(target, container, s);
                    } else {
                        $(container).find(".manageApps").off("click");
                        $(container).find(".manageApps").on("click", function(){
                            var formData = new FormData();
                            formData.append(s.param, "edit");
            
                            AjaxComponent.call($(this), window.location.href, "POST", formData);
                        });
                    }

                    $(target).addClass("initialized");
                }
            });
        },
        
        /*
         * Prepare the page for manage app 
         */
        initEditMode: function(target, container, s) {
            var optionsDiv = $(target).find(".cards");

            $(target).prepend('<h3 class="title">'+s.msg.selectedApps+'</h3><div class="selectedApps cards row"></div><h3 class="title">'+s.msg.availableApps+'</h3>');
            var selectionDiv = $(target).find(".selectedApps");
            
            methods.populateSelectedApps(target, optionsDiv, selectionDiv, s);
            
            //init add, delete & sort action
            methods.initEvent(target, optionsDiv, selectionDiv, s);

            $(container).find(".saveChanges").off("click");
            $(container).find(".saveChanges").on("click", function(){
                methods.submitSelection(this, target, selectionDiv, s);
            });
            
            $(container).find(".cancelChanges").off("click");
            $(container).find(".cancelChanges").on("click", function(){
                AjaxComponent.call($(this), window.location.href, "GET", null);
            });
        },
        
        /*
         * populate the selected app and non-removable app
         */
        populateSelectedApps: function(target, optionsDiv, selectionDiv, s) {
            for(var i in s.selected) {
                var app = $(optionsDiv).find('.card-icon > .ph_selector[value="'+s.selected[i]+'"]').parent();
                methods.selectApp(app, optionsDiv, selectionDiv, s);
            }
            
            //non removable app
            for(var i in s.nonRemovable) {
                var app = $(selectionDiv).find('.card-icon > .ph_selector[value="'+s.nonRemovable[i]+'"]').parent();
                if (app) {
                    $(app).addClass("nonRemovable");
                    $(app).find(".removeApp").remove();
                }
            }
        },
        
        /*
         * Init event for select app & sorting
         */
        initEvent: function(target, optionsDiv, selectionDiv, s) {
            //select app event
            $(optionsDiv).find('.card-icon').each(function(){
                $(this).append('<div class="super-app-action"><i class="selectApp fas fa-plus-square" title="'+UI.escapeHTML(s.msg.selectApp)+'"></i></div>');
            });
            
            $(optionsDiv).find(".selectApp").off("click");
            $(optionsDiv).find(".selectApp").on("click", function(){
                var app = $(this).closest(".card-icon");
                methods.selectApp(app, optionsDiv, selectionDiv, s);
            });
            
            //sorting
            $(selectionDiv).sortable({
                axis: 'xy',
                handle: '.super-app-action'
            });
        },
        
        /*
         * Move app from available apps to selected apps
         */
        selectApp: function(app, optionsDiv, selectionDiv, s) {
            var clone = $(app).clone();
            //add remove icon & event
            $(clone).find(".super-app-action").remove();
            $(clone).append('<div class="super-app-action"><i class="removeApp fas fa-minus-square" title="'+UI.escapeHTML(s.msg.removeApp)+'"></i></div>');
            
            $(clone).find(".removeApp").on("click", function(){
                methods.removeApp(clone, optionsDiv, selectionDiv, s);
            });
            
            $(selectionDiv).append(clone);
            
            $(app).addClass("selectedApp").hide();
        },
        
        /*
         * Move app from selected apps to available apps
         */
        removeApp: function(app, optionsDiv, selectionDiv, s) {
            var id = $(app).find(" > .ph_selector").attr("value");
            
            var optionApp = $(optionsDiv).find('.card-icon > .ph_selector[value="'+id+'"]').parent();
            $(optionApp).removeClass("selectedApp").show();
            
            $(app).remove();
        },
        
        /*
         * Submit the selected app for saving
         */
        submitSelection: function(elment, target, selectionDiv, s) {
            var selectedIds = [];

            $(selectionDiv).find(".card-icon").each(function(){
                selectedIds.push($(this).find(" > .ph_selector").attr("value"));
            });

            var formData = new FormData();
            formData.append("selectedApps", selectedIds.join(';'));

            AjaxComponent.call($(elment), window.location.href, "POST", formData);
        }
    };
    
    $.fn.extend({
        superAppListing : function(method){
            if ( methods[method] ) {
                return methods[method].apply( this, Array.prototype.slice.call( arguments, 1 ));
            } else if ( typeof method === 'object' || ! method ) {
                return methods.init.apply( this, arguments );
            } else {
                $.error( 'Method ' +  method + ' does not exist on jQuery.formgrid' );
            }
        }
    });
})(jQuery);

$(document).ready(function(){
    $("[data-super_app]:not(.initialized)").superAppListing();
});