#Html5 Storage jQuery Plugin 

A light (1,2K) jQuery Plugin to use **Local Storage** or **Session Storage** without worrying about HTML5 support. It uses Cookies for backward compatibility.

If you are using jQuery in your projects and you want to use Local Storage or Session Storage, this is your library. If user's browser doesn't support them it will use cookies instead.

## Installation

Include the script **after** the jQuery library (unless you are packaging scripts somehow else):

    <script src="/path/to/jquery.js"></script>
    <script src="/path/to/jquery.html5storage.min.js"></script>

**Do not include the script directly from GitHub (http://raw.github.com/...).** The file is being served as text/plain and as such being blocked in Internet Explorer on Windows 7 for instance (because of the wrong MIME type). GitHub is not a CDN.

## Usage

###Create or update Key-Value pair:

Local Storage

    $.localStorage.setItem('key_name', 'Key Value');

Session Storage

    $.sessionStorage.setItem('key_name', 'Key Value');

###Get value by key:

Local Storage

    $.localStorage.getItem('key_name');

Session Storage

    $.sessionStorage.getItem('key_name');

###Remove Key-Value pair:

Local Storage

    $.localStorage.removeItem('key_name');

Session Storage

    $.sessionStorage.removeItem('key_name');

###Remove all Key-Value pairs:

Local Storage

    $.localStorage.clear();

Session Storage

    $.sessionStorage.clear();

## Configuration

There is no configuration need for using this plugin. But if you want, you can change the default cookie parameters for users that haven't a browser with HTML5 Storage support.

For doing it you must set the parameters before calling any method:

###Local Storage

    $.localStorage.settings = {
        cookiePrefix : 'html5fallback:localStorage:', // Prefix for the Local Storage substitution cookies
        cookieOptions : {
            path : '/', // Path for the cookie
            domain : document.domain, // Domain for the cookie
            expires: 365 // Days left for cookie expiring
        }
	};

*The shown values are defaults.*

###Session Storage

    $.sessionStorage.settings = {
        cookiePrefix : 'html5fallback:sessionStorage:', / Prefix for the Session Storage substitution cookies
        cookieOptions : {
            path : '/', // Path for the cookie
            domain : document.domain, // Domain for the cookie
            expires: undefined // Days left for cookie expiring (by default expires with the session)
        }
	};

*The shown values are defaults.*


## Development

- Source hosted at [GitHub](https://github.com/artberri/jquery-html5storage)
- Report issues, questions, feature requests on [GitHub Issues](https://github.com/artberri/jquery-html5storage/issues)

Pull requests are very welcome! Make sure your patches are well tested. Please create a topic branch for every separate change you make.

## Licensing

Released under the MIT License [http://opensource.org/licenses/MIT](http://opensource.org/licenses/MIT)

Copyright © 2013 Alberto Varela ([http://www.berriart.com](http://www.berriart.com))

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.




