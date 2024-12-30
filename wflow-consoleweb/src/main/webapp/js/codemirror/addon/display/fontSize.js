(function(mod) {
    if (typeof exports == "object" && typeof module == "object") // CommonJS
      mod(require("../../lib/codemirror"));
    else if (typeof define == "function" && define.amd) // AMD
      define(["../../lib/codemirror"], mod);
    else // Plain browser env
      mod(CodeMirror);
})(function(CodeMirror) {
    "use strict";

    // Function to modify font size
    function modifyFontSize(cm, increase) {
        var wrapper = cm.getWrapperElement();
        var currentSize = parseFloat(window.getComputedStyle(wrapper, null).getPropertyValue('font-size'));
        var newSize = increase ? currentSize + 2 : currentSize - 2;
        wrapper.style.fontSize = newSize + 'px';
        cm.refresh();
    }

    // Expose the function to increase font size
    CodeMirror.defineExtension("increaseFontSize", function() {
        modifyFontSize(this, true);
    });

    // Expose the function to decrease font size
    CodeMirror.defineExtension("decreaseFontSize", function() {
        modifyFontSize(this, false);
    });
});
