<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>v3</title>
        <script type="text/javascript" src="jquery-1.5.2.min.js"></script>
        <script>
            var Template = {
                debug: function() {
                    $("div").prepend(function(index, html) {
                        $(this).attr("style", "border: dotted 1px #dedede");
                        return "<span class='debug'>" + $(this).attr("id") + "</span>";
                    });
                },
                init: function(menu, nav) {
                    $("#menu-items a").click(function() {
                        $("#menu-items li").removeClass("menu-selected");
                        $(this).parent().addClass("menu-selected");
                    });
                    $("#nav-list a").click(function() {
                        $("#nav-list li").removeClass("nav-selected");
                        $(this).parent().addClass("nav-selected");
                    });
                    if (menu) {
                        $(menu).parent().addClass("menu-selected");
                    }
                    if (nav) {
                        $(nav).parent().addClass("nav-selected");
                    }
                }
            }
        </script>
        <link rel="stylesheet" href="style.css" />
    </head>
    <body>

        <div id="header">
            <div id="topbar">
                <div id="logo">Joget Workflow</div>
                <div id="account">Profile | Logout</div>
            </div>
            <div id="menu">
                <ul id="menu-items">
                    <li>
                        <a id="menu-home" href="#" class="menu-link">Home
                        <div id="menu-home-desc" class="menu-description">Run Apps</div></a>
                    </li>
                    <li>
                        <a id="menu-apps" href="#" class="menu-link">Manage Apps
                        <div id="menu-apps-desc" class="menu-description">Design & Monitor Workflows, Forms, etc</div></a>
                    </li>
                    <li>
                        <a id="menu-users" href="#" class="menu-link">Manage Users
                        <div id="menu-users-desc" class="menu-description">Manage organization and users</div></a>
                    </li>
                    <li>
                        <a id="menu-settings" href="#" class="menu-link">System Settings
                        <div id="menu-settings-desc" class="menu-description">Manage plugins and settings</div></a>
                    </li>
                </ul>
            </div>
        </div>

        <div id="nav">
            <div id="nav-title">
                <a href="#">Home</a> &gt; <a href="#">Manage Apps</a> &gt; Design App
            </div>
            <div id="nav-body">
                <ul id="nav-list">
                    <li>App Name
                        <ul class="nav-sublist">
                            <li><a id="nav-app-processes" href="#">Processes</a></li>
                            <li><a id="nav-app-forms" href="#">Forms</a></li>
                            <li><a id="nav-app-lists" href="#">Lists</a></li>
                            <li><a id="nav-app-props" href="#">App Properties</a></li>
                        </ul>
                    </li>
                </ul>
            </div>
        </div>

        <div id="main">
            <div id="main-title"></div>
            <div id="main-action">
                <ul id="main-action-buttons">
                    <li><button>Launch Workflow Designer</button></li>
                    <li><button>Upload Saved XPDL</button></li>
                </ul>
            </div>
            <div id="main-body">

                Main Body Here

            </div>
            <div id="footer">
                Footer
            </div>
        </div>

        <script>
            Template.init("#menu-apps", "#nav-app-processes");
        </script>

        <div id="guide">
            <div id="guide-title">
                Guide
            </div>
            <div id="guide-body">
                <ul id="guide-list">
                    <li><input type="checkbox" checked disabled> Setup Users
                        <ul class="guide-sublist">
                            <li><input type="checkbox" checked disabled> Define Org Chart</li>
                            <li><input type="checkbox" checked disabled> Define Groups</li>
                            <li><input type="checkbox" checked disabled> Define Users</li>
                        </ul>
                    </li>
                    <li><input type="checkbox" checked disabled> Design App
                        <ul class="guide-sublist">
                            <li><input type="checkbox" checked disabled> Design Processes
                                <ul class="guide-sub-sublist">
                                    <li><input type="checkbox" checked disabled> Map Activities</li>
                                    <li><input type="checkbox" checked disabled> Map Tools</li>
                                    <li><input type="checkbox" checked disabled> Map Participants</li>
                                </ul>
                            </li>
                            <li><input type="checkbox" checked disabled> Design Forms</li>
                            <li><input type="checkbox" checked disabled> Design Userviews</li>
                        </ul>
                    </li>
                    <li><input type="checkbox" checked disabled> Run App</li>
                </ul>
            </div>
        </div>

    </body>
</html>
