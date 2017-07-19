(function($){
    $.fn.extend({
        fileUploadField : function(o){
            var target = this;
            if($(target)){
                
                var previewTemplate = $(target).find("li.template").clone();
                $(previewTemplate).removeClass("template");
                $(previewTemplate).removeAttr("style");
                $(previewTemplate).find("input").removeAttr("disabled");
                $(target).find("li.template").remove();
                
                var options = {
                    url : o.url,
                    paramName : o.paramName,
                    previewsContainer : "#"+$(target).attr("id") + " ul.form-fileupload-value",
                    previewTemplate : '<li>'+$(previewTemplate).html()+'</li>',
                    fallback: function() {
                        $(target).find(".dz-message.needsclick").remove();
                        $(target).find("input[type=file]").show();
                        $(target).removeClass("dropzone");
                    },
                    dictInvalidFileType : o.fileTypeMsg,
                    dictFileTooBig : o.maxSizeMsg
                };
                
                if (o.fileType !== "") {
                    options.acceptedFiles = o.fileType.replace(/;/g, ',');
                }
                if (o.maxSize !== "") {
                    try {
                        options.maxFilesize = parseInt(o.maxSize) / 1024;
                    } catch (err) {}
                } else {
                    options.maxFilesize = 100000;
                } 
                if (o.height !== "") {
                    options.thumbnailHeight = o.height + "px";
                }
                if (o.width !== "") {
                    options.thumbnailWidth = o.width + "px";
                }
                
                var myDropzone = new Dropzone("#"+$(target).attr("id"), options);
                myDropzone.on("success", function(file, resp) {
                    if (o.multiple !== "true") {
                        $(target).find("li").each(function() {
                            if (!$(this).is($(file.previewElement))) {
                                $(this).remove();
                            }
                        });
                    }
                    $(file.previewElement).find(".progress").remove();
                    $(file.previewElement).find(".remove").show();
                    $(file.previewElement).find("input").val(resp.path);
                    $(file.previewElement).find("img").attr("src", $(file.previewElement).find("img").attr("src") + encodeURIComponent(resp.path));
                });
                myDropzone.on("error", function(file, error) {
                    $(file.previewElement).find(".progress").remove();
                    $(file.previewElement).find(".remove").show();
                    $(file.previewElement).find("input").remove();
                    $(file.previewElement).find(".name").css("color" , "red");
                });
                
                $(target).on("click", ".remove", function(){
                    $(this).closest("li").remove();
                });
                $(target).on('keyup', function(e){
                    var keyCode = e.keyCode || e.which;
                    if (keyCode === 13) { 
                        $(target).trigger("click");
                        return false;
                    }
                });
                
                if ($(target).hasClass("dropzone") && o.padding !== "") {
                    $(target).attr("style", "padding:" +o.padding+ " !important;");
                }
            }
            return target;
        }
    });
    
})(jQuery);