const prefersDarkScheme = window.matchMedia("(prefers-color-scheme: dark)");
const currentTheme = localStorage.getItem("theme");

function setHtmlThemeAttribute(theme) {
    const html = document.documentElement;
    if (theme === "auto") {
        html.setAttribute("data-bs-theme", prefersDarkScheme.matches ? "dark" : "light");
    } else {
        html.setAttribute("data-bs-theme", theme);
    }
}

function toggleTheme(theme) {
    $('body').toggleClass("dark-mode", theme === "dark");
    
    var iframes = $('iframe');
    if (iframes.length > 0) {
        var iframeBody = iframes.contents().find('body');
        iframeBody.toggleClass("dark-mode", theme === "dark");
    }

    // Apply data-bs-theme to html tag
    setHtmlThemeAttribute(theme);
}

function updateSelectedIconAndSave(theme) {
    $("#selectedIcon").remove();
    $('#theme-selector').find('[data-value="' + theme + '"]').prepend('<i id="selectedIcon" class="zmdi zmdi-check"></i>');
    localStorage.setItem("theme", theme);
}

if (currentTheme === "dark") {
    updateSelectedIconAndSave("dark");
} else if (currentTheme === "auto") {
    updateSelectedIconAndSave("auto");
} else{
    updateSelectedIconAndSave("light");
}

$(document).ready(function () {
    $("#theme-selector li").click(function () {
        const selectedTheme = $(this).data("value");
        if (selectedTheme === "auto") {
            const resolvedTheme = prefersDarkScheme.matches ? "dark" : "light";
            toggleTheme(resolvedTheme);
            updateSelectedIconAndSave("auto");
        } else {
            toggleTheme(selectedTheme);
            updateSelectedIconAndSave(selectedTheme);
        }
    });
    
    if (currentTheme === "auto") {
        toggleTheme(prefersDarkScheme.matches ? "dark" : "light");
    } else {
        toggleTheme(currentTheme);
    }
});