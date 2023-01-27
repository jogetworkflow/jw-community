/*
Script: JSONError.js

Dedicated JSON Error object:


Version:
        1.0 - Too much simple to be unstable ;)

Compatibility:
        FireFox - Version 1, 1.5, 2 and 3 (FireFox uses secure code evaluation)
        Internet Explorer - Version 5, 5.5, 6 and 7
        Opera - 8 and 9 (probably 7 too)
        Safari - Version 2 (probably 1 too)
        Konqueror - Version 3 or greater

Author:
        Andrea Giammarchi, <http://www.3site.eu>

License:
        >Copyright (C) 2007 Andrea Giammarchi - www.3site.eu
        >
        >Permission is hereby granted, free of charge,
        >to any person obtaining a copy of this software and associated
        >documentation files (the "Software"),
        >to deal in the Software without restriction,
        >including without limitation the rights to use, copy, modify, merge,
        >publish, distribute, sublicense, and/or sell copies of the Software,
        >and to permit persons to whom the Software is furnished to do so,
        >subject to the following conditions:
        >
        >The above copyright notice and this permission notice shall be included
        >in all copies or substantial portions of the Software.
        >
        >THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
        >INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
        >FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
        >IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
        >DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
        >ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
        >OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

/*
Object: JSONError
        Extends object Error

Example:
        >try{
        >       throw new JSONError("bad data");
        >}
        >catch(e) {
        >       alert(e);
        >       // JSONError: bad data
        >}
*/
function JSONError(message){

        /* Section: Properties - Public */

        /*
        Property: message
                String - Error message or empty string
        */
        this.message = message || "";

        /*
        Property: name
                String - object name: JSONError
        */
        this.name = "JSONError";
};
JSONError.prototype = new Error;
