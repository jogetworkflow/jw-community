const currentDensity = localStorage.getItem("density");

function toggleThemeDensity(density) {
    $('body').toggleClass("compact-mode", density === "compact");
    
    var iframes = $('iframe');
    if (iframes.length > 0) {
        var iframeBody = iframes.contents().find('body');
        iframeBody.toggleClass("compact-mode", density === "compact");
    }
}

function toggleThemeDensityPopupBody(density){
    var popupBody = $('.popupBody');
    popupBody.toggleClass("compact-mode", density === "compact");
}

function updateSelectedDensityAndSave(density) {
    $("#selectedDensity").remove();
    $('#density-selector').find('[data-value="' + density + '"]').prepend('<i id="selectedDensity" class="zmdi zmdi-check"></i>');
    localStorage.setItem("density", density);
}

if (currentDensity === "compact") {
    updateSelectedDensityAndSave("compact");
} else{
    updateSelectedDensityAndSave("normal");
}

$(document).ready(function () {
    $("#density-selector li").click(function () {
        const selectedDensity = $(this).data("value");
        toggleThemeDensity(selectedDensity);
        updateSelectedDensityAndSave(selectedDensity);
    });
    
    $("iframe").ready(function(){
        toggleThemeDensityPopupBody(currentDensity);
    });
});



