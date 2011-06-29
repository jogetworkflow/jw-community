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

                <h3>Page Not Found (Code 404)</h3>

                Sorry, the page requested is not found.
                <br><br>
                If you have been brought to this page unintentionally, please report the previous URL for troubleshooting purposes.
                <p>&nbsp;</p>
                <p>&nbsp;</p>
                <p>
                    <a href="${pageContext.request.contextPath}/">Click here to go back to the main page</a>
                </p>

            </div>
        </div>

        <div id="footer">
            www.joget.org
        </div>

    </body>
</html>