/*!
* jQuery Plugin to use Local Storage or Session Storage without worrying
* about HTML5 support. It uses Cookies for backward compatibility.
*
* @author Alberto Varela Sánchez (http://www.berriart.com)
* @version 1.0 (17th January 2013)
*
* Released under the MIT License (http://opensource.org/licenses/MIT)
*
* Copyright (c) 2013 Alberto Varela Sánchez (alberto@berriart.com)
*
* Permission is hereby granted, free of charge, to any person obtaining a
* copy of this software and associated documentation files (the "Software"),
* to deal in the Software without restriction, including without limitation
* the rights to use, copy, modify, merge, publish, distribute, sublicense,
* and/or sell copies of the Software, and to permit persons to whom the
* Software is furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in
* all copies or substantial portions of the Software.

* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
* THE SOFTWARE.
*/

;(function(window, $ ) {
    "use strict";

    var types = ['localStorage','sessionStorage'],
        support = [];

    $.each(types, function( i, type ) {
        try {
            support[type] = type in window && window[type] !== null;
        } catch (e) {
            support[type] = false;
        }

        $[type] = {
            settings : {
                cookiePrefix : 'html5fallback:' + type + ':',
                cookieOptions : {
                    path : '/',
                    domain : document.domain,
                    expires : ('localStorage' === type) ? { expires: 365 } : undefined
                }
            },
            
            getItem : function( key ) {
                var response;
                if(support[type]) {
                    response = window[type].getItem(key);
                }
                else {
                    response = $.cookie(this.settings.cookiePrefix + key);
                }
                
                return response;
            },
            
            setItem : function( key, value ) {
                if(support[type]) {
                    return window[type].setItem(key, value);
                }
                else {
                    return $.cookie(this.settings.cookiePrefix + key, value, this.settings.cookieOptions);
                }
            },

            removeItem : function( key ) {
                if(support[type]) {
                    return window[type].removeItem(key);
                }
                else {
                    var options = $.extend(this.settings.cookieOptions, {
                        expires: -1
                    });
                    return $.cookie(this.settings.cookiePrefix + key, null, options);
                }
            },

            clear : function() {
                if(support[type]) {
                    return window[type].clear();
                }
                else {
                    var reg = new RegExp('^' + this.settings.cookiePrefix, ''),
                        options = $.extend(this.settings.cookieOptions, {
                            expires: -1
                        });

                    if(document.cookie && document.cookie !== ''){
                        $.each(document.cookie.split(';'), function( i, cookie ){
                            if(reg.test(cookie = $.trim(cookie))) {
                                 $.cookie( cookie.substr(0,cookie.indexOf('=')), null, options);
                            }
                        });
                    }
                }
            }
        };
    });
})(window, jQuery);