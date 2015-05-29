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
                        $("#menu-items li").removeClass("menu-next-selected");
                        $(this).parent().addClass("menu-selected");
                        $(this).parent().next().addClass("menu-next-selected");
                    });
                    $("#nav-list a.nav-link").click(function() {
                        $("#nav-list li").removeClass("nav-selected");
                        $(this).parent().addClass("nav-selected");
                    });
                    if (menu) {
                        $(menu).parent().addClass("menu-selected");
                        $(menu).parent().next().addClass("menu-next-selected");
                    }
                    if (nav) {
                        $(nav).parent().addClass("nav-selected");
                    }
                    $(".menu-link").mouseover(function() {
                        $(".menu-panel").css("display", "none");
                        var panel = "#" + $(this).attr("id") + "-panel";
                        $(panel).css("display", "block");
                        $(panel).css("left", $(this).offset().left - 20);
                    });
                    $(".menu-panel").bind("mouseleave", function() {
                        $(".menu-panel").css("display", "none");
                    });
                }
            }
        </script>
        <link rel="stylesheet" href="style.css" />
    </head>
    <body>

        <div id="header">
            <div id="topbar">
                <div id="logo">Joget Workflow</div>
                <div id="account"><a href="#">System Settings</a> | <a href="#">Profile</a> | <a href="#">Logout</a></div>
            </div>
            <div id="menu">
                <ul id="menu-items">
                    <li>
                        <a id="menu-home" href="#" class="menu-link">Home
                            <div id="menu-home-desc" class="menu-description">Welcome Page</div></a>
                    </li>
                    <li>
                        <a id="menu-users" href="orgchart.jsp" class="menu-link">1. Setup Users
                            <div id="menu-users-desc" class="menu-description">Setup organization and users</div></a>
                    </li>
                    <li>
                        <a id="menu-apps" href="apps.jsp" class="menu-link">2. Design Apps
                            <div id="menu-apps-desc" class="menu-description">Design Workflows, Forms, etc</div></a>
                    </li>
                    <li>
                        <a id="menu-run" href="#" class="menu-link">3. Run Apps
                            <div id="menu-run-desc" class="menu-description">Run Apps</div></a>
                    </li>
                    <li>
                        <a id="menu-monitor" href="#" class="menu-link">4. Monitor Apps
                            <div id="menu-monitor-desc" class="menu-description">Monitoring & Reports</div></a>
                    </li>
                </ul>
                <div id="menu-users-panel" class="menu-panel">
                    <div class="menu-body">
                        <ul class="menu-list">
                            <li>
                                <a href="orgchart.jsp" class="menu-list-title">i. Setup Organization Chart
                                <div class="menu-list-description">Manage organization chart</div></a>
                            </li>
                            <li>
                                <a href="groups.jsp" class="menu-list-title">ii. Setup Groups
                                <div class="menu-list-description">Manage groups</div></a>
                            </li>
                            <li>
                                <a href="users.jsp" class="menu-list-title">iii. Setup Users
                                <div class="menu-list-description">Manage users</div></a>
                            </li>
                        </ul>
                    </div>
                    <div class="menu-action">
                        <ul class="menu-action-buttons">
                            <li><button>Create New Group</button></li>
                            <li><button>Create New User</button></li>
                        </ul>
                    </div>
                </div>
                <div id="menu-apps-panel" class="menu-panel">
                    <div class="menu-body">
                        <ul class="menu-list">
                            <li>
                                <img src="thumb.jpg" width="30" />
                                <a href="apps.jsp" class="menu-list-title">Process Management
                                <div class="menu-list-description">version 2</div></a>
                            </li>
                            <li>
                                <img src="thumb.jpg" width="30" />
                                <a href="apps.jsp" class="menu-list-title">Service Request
                                <div class="menu-list-description">version 1</div></a>
                            </li>
                        </ul>
                    </div>
                    <div class="menu-action">
                        <ul class="menu-action-buttons">
                            <li><button>Create New App</button></li>
                            <li><button>Import App</button></li>
                        </ul>
                    </div>
                </div>
                <div id="menu-run-panel" class="menu-panel">
                    <div class="menu-body">
                        <ul class="menu-list">
                            <li>
                                <img src="thumb.jpg" width="30" />
                                <a href="apps.jsp" class="menu-list-title">Process Management
                                <div class="menu-list-description">version 2</div></a>
                            </li>
                            <li>
                                <img src="thumb.jpg" width="30" />
                                <a href="apps.jsp" class="menu-list-title">Service Request
                                <div class="menu-list-description">version 1</div></a>
                            </li>
                        </ul>
                    </div>
                    <div class="menu-action">
                        <ul class="menu-action-buttons">
                        </ul>
                    </div>
                </div>
                <div id="menu-monitor-panel" class="menu-panel">
                    <div class="menu-body">
                        <ul class="menu-list">
                            <li>
                                <img src="thumb.jpg" width="30" />
                                <a href="apps.jsp" class="menu-list-title">Running Processes
                                <div class="menu-list-description">Manage running processes</div></a>
                            </li>
                            <li>
                                <img src="thumb.jpg" width="30" />
                                <a href="apps.jsp" class="menu-list-title">Completed Processes
                                <div class="menu-list-description">View completed or aborted processes</div></a>
                            </li>
                            <li>
                                <img src="thumb.jpg" width="30" />
                                <a href="apps.jsp" class="menu-list-title">Manage Form Data
                                <div class="menu-list-description">Manage form data</div></a>
                            </li>
                            <li>
                                <img src="thumb.jpg" width="30" />
                                <a href="apps.jsp" class="menu-list-title">Service Level Report
                                <div class="menu-list-description">View service level report</div></a>
                            </li>
                            <li>
                                <img src="thumb.jpg" width="30" />
                                <a href="apps.jsp" class="menu-list-title">Audit Trail
                                <div class="menu-list-description">View audit trail</div></a>
                            </li>
                        </ul>
                    </div>
                    <div class="menu-action">
                        <ul class="menu-action-buttons">
                        </ul>
                    </div>
                </div>
            </div>
        </div>

        <div id="container">