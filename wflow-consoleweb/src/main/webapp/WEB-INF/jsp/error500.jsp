<%@ page isErrorPage="true" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
    <head>
        <meta http-equiv="content-type" content="text/html; charset=utf-8">
        <title></title>
        <link rel="stylesheet" type="text/css" href="<%= request.getContextPath()%>/css/v3.css">
        <style>
            #main {
                width: 90%;
                margin: auto;
                float: none;
            }
        </style>
    </head>
    <body>

        <div id="header">
            <div id="topbar">

                <div id="logo">Joget Workflow</div>
                <div id="account"><a href="#">System Settings</a> | <a href="#">Profile</a> | <a href="#">Logout</a></div>
            </div>
            <div id="menu">
                <ul id="menu-items">
                    <li id="menu-home" class="first-inactive"><a href="/jw/web/console/home"><span class="menu-bg"><span class="title">Home</span><span class="subtitle">Welcome Page</span></span></a></li>
                    <li id="menu-last" class="last-inactive"><a href="#"><span class="menu-bg"><span class="title">Error</span><span class="subtitle">Error Page</span></span></a></li>
                </ul>
            </div>

        </div>

        <div id="main">
            <div id="main-body">

                <h3>Internal server error (Code 500)</h3>

                <div style="width:700px">
                    Oops, sorry but an unintended problem has occurred.
                    <br><br>
                    Please click on the link below to display the full error message.
                    If you would like to help report this incident, please copy the full error message and send it to your administrator.
                    <p>&nbsp;</p>
                    <p>&nbsp;</p>
                </div>

                <div>
                    <script type="text/javascript">
                        function showStackTrace() {
                            document.getElementById("stack_trace").style.display = "block";
                        }
                    </script>
                    <a href="#" onclick="showStackTrace()">Show Error Message</a>
                    <p>&nbsp;</p>
                    <p>&nbsp;</p>
                </div>

                <div id="stack_trace" style="display:none; overflow:auto; font-size:10px">
                    <hr />
                    <pre><% exception.printStackTrace(new java.io.PrintWriter(out));%></pre>
                </div>

            </div>
        </div>

        <div id="footer">
            www.joget.org
        </div>

    </body>
</html>

