JPopup = {
    dialogboxes : new Array(),
    
    create: function (id, title, width, height) {
        if (JPopup.dialogboxes[id] === undefined || JPopup.dialogboxes[id] === null) {
            if($("#"+id).length == 0){
                var newWidth = UI.getPopUpWidth(width);
                var newHeight = UI.getPopUpHeight(height);
                
                if (!title || title === "") {
                    title = "&nbsp;";
                }
                JPopup.dialogboxes[id] = new Boxy('<iframe id="'+id+'" name="'+id+'" src="'+UI.base+'/images/v3/clear.gif" style="frameborder:0;height:'+newHeight+'px;width:'+newWidth+'px;"></iframe>', {title:title,closeable:true,draggable:true,show:false,fixed: false});
            } else {
                JPopup.dialogboxes[id] = Boxy.get($("#"+id));
            }
        }
    },
    
    show : function (id, url, params, title, width, height, action) {
        if (JPopup.dialogboxes[id] === undefined || JPopup.dialogboxes[id] === null) {
            JPopup.create(id, title, width, height);
        } else {
            width = UI.getPopUpWidth(width);
            height = UI.getPopUpHeight(height);
        }
        
        $("#"+id).remove();
        JPopup.dialogboxes[id].setContent('<iframe id="'+id+'" name="'+id+'" src="'+UI.base+'/images/v3/clear.gif" style="frameborder:0;height:'+height+'px;width:'+width+'px;"></iframe>');
        JPopup.dialogboxes[id].show();
        
        UI.adjustPopUpDialog(JPopup.dialogboxes[id]);
        
        if (action !== undefined && action.toLowerCase() === "get") {
            $.each(params, function (key, data) {
                url += "&" + key + "=" + encodeURIComponent(data);
            });
        }
        
        var form = $('<form method="post" data-ajax="false" style="display:none;" target="'+id+'" action="'+url+'"></form>'); 
        $(document.body).append(form); 
        
        if (!(action !== undefined && action.toLowerCase() === "get")) {
            $.each(params, function (key, data) {
                $(form).append("<input id=\""+key+"\" name=\""+key+"\">");
                $(form).find('#'+key).val(data);
            });
        }
        setTimeout(function() {
            $(form).submit();
            $(form).remove();
        }, 120);
    },
    
    hide : function (id) {
        JPopup.dialogboxes[id].hide();
    }
}