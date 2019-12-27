(function() {
    var exports = {};
    "use strict";
    Object.defineProperty(exports, "__esModule", {
        value: true
    });
    var _createClass = function() {
        function e(e, t) {
            for (var i = 0; i < t.length; i++) {
                var n = t[i];
                n.enumerable = n.enumerable || false;
                n.configurable = true;
                if ("value" in n) n.writable = true;
                Object.defineProperty(e, n.key, n)
            }
        }
        return function(t, i, n) {
            if (i) e(t.prototype, i);
            if (n) e(t, n);
            return t
        }
    }();

    function _classCallCheck(e, t) {
        if (!(e instanceof t)) {
            throw new TypeError("Cannot call a class as a function")
        }
    }
    var Switcher = exports.Switcher = function() {
        function e(t) {
            var i = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : {};
            _classCallCheck(this, e);
            this.quill = t;
            this.options = i;
            [].slice.call(this.quill.container.parentElement
                .querySelectorAll(".ql-toolbar"))
                .forEach((function(toolbarEl){
                    const buttonContainer = document.createElement("span");
                    buttonContainer.setAttribute("class", "ql-formats");
                    const button = document.createElement("button");
                    if (i.mode !== "inline") {
                        button.setAttribute("class", "toggle-on");
                        button.innerHTML = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 576 512"><path class="ql-fill" fill="currentColor" d="M384 64H192C86 64 0 150 0 256s86 192 192 192h192c106 0 192-86 192-192S490 64 384 64zm0 320c-70.8 0-128-57.3-128-128 0-70.8 57.3-128 128-128 70.8 0 128 57.3 128 128 0 70.8-57.3 128-128 128z"></path></svg>';
                    } else {
                        button.setAttribute("class", "toggle-off");
                        button.innerHTML = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 576 512"><path class="ql-fill" fill="currentColor" d="M384 64H192C85.961 64 0 149.961 0 256s85.961 192 192 192h192c106.039 0 192-85.961 192-192S490.039 64 384 64zM64 256c0-70.741 57.249-128 128-128 70.741 0 128 57.249 128 128 0 70.741-57.249 128-128 128-70.741 0-128-57.249-128-128zm320 128h-48.905c65.217-72.858 65.236-183.12 0-256H384c70.741 0 128 57.249 128 128 0 70.74-57.249 128-128 128z"></path></svg>';
                    }
                    button.onclick = function(e) {
                        e.preventDefault();
                        if ($(this).hasClass("toggle-on")) {
                            $(i.target).trigger(i.offEvent);
                        } else {
                            $(i.target).trigger(i.onEvent);
                        }
                    };
                    buttonContainer.appendChild(button);
                    toolbarEl.appendChild(buttonContainer);
                }));
        }
        _createClass(e, []);
        return e
    }();
    window.Quill.register('modules/switcher', exports.Switcher);
})();