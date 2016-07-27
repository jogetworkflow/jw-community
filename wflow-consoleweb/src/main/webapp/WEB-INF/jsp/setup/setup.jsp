<%@page import="org.joget.apps.app.service.AppUtil"%>
<%@page import="org.joget.commons.util.*"%>
<%@include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%
    boolean test = true;
    if (!test) {
        try {
            SetupManager setupManager = (SetupManager) AppUtil.getApplicationContext().getBean("setupManager");
            response.sendRedirect(request.getContextPath());
            return;
        } catch (Exception e) {
            // ignore
        }
    }
    if (HostManager.isVirtualHostEnabled()) {
        response.sendError(response.SC_FORBIDDEN);
        return;
    }
%>    
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
    <head>
        <title><%= ResourceBundleUtil.getMessage("console.header.top.title")%></title>
        <link rel="shortcut icon" href="${pageContext.request.contextPath}/images/favicon.ico"/>
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/js/font-awesome/css/font-awesome.min.css">
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/v5.css">
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/console_custom.css">
        <style>
            #main {
                width: 99%;
                margin-left: 10px;
                top: -8px;                
            }
            #main-header {
                display: block;
            }
            #setupNotice {
                border-bottom: solid 1px #555;
                padding: 10px 0px;
                margin-bottom: 10px;
            }
            #jdbcSetup {
                display: none;
            }
            #setupProgress {
                margin: 20px 0px;
            }
            #setupResult {
                background: #ddd;
                padding: 10px;
                margin: 20px 0px;
                border: dashed 1px #555;
                border-radius: 5px;
            }
            #setupResult.setupSuccess {
                background: #ccffcc;
            }
            #setupResult.setupError {
                background: #ffcccc;
            }
            #setupResult ul {
                margin: 10px 15px;
            }
            #setupDetailsLink {
                display: block;
                text-decoration: underline;
                cursor: pointer;
                margin: 5px 0px;
            }
            #setupErrorDetails {
                margin: 5px 0px;
            }
        </style>
    </head>
    <body>
        <div id="main-header">
            <a id="home-link" href="${pageContext.request.contextPath}/">
                <span id="logo"></span>
                <span id="logo-title"><%= ResourceBundleUtil.getMessage("console.header.top.title")%> <%= ResourceBundleUtil.getMessage("setup.datasource.label.title")%></span>
            </a>
        </div>
        <div id="container">
            <div id="content-container">        
                <div id="main">
                    <div id="main-body">
                        <form id="setupForm" method="post" action="${pageContext.request.contextPath}/web/console/setting/datasource/submit">
                            <h3><%= ResourceBundleUtil.getMessage("setup.datasource.label.dbSetup")%></h3>
                            <div id="setupNotice">
                                <%= ResourceBundleUtil.getMessage("setup.datasource.label.notice")%>
                            </div>
                            <div class="form-row">
                                <label for="dbType"><%= ResourceBundleUtil.getMessage("setup.datasource.label.dbType")%></label>
                                <span class="form-input">
                                    <select id="dbType" name="dbType">
                                        <option value="mysql">MySQL</option>
                                        <option value="oracle">Oracle</option>
                                        <option value="sqlserver">MS SQL Server</option>
                                        <option value="custom"><%= ResourceBundleUtil.getMessage("setup.datasource.label.custom")%></option>
                                    </select>
                                </span>
                            </div>
                            <div id="dbSetup" class="main-row-content">
                                <div class="form-row">
                                    <label for="dbHost"><%= ResourceBundleUtil.getMessage("setup.datasource.label.dbHost")%></label>
                                    <span class="form-input">
                                        <input type="text" size="40" id="dbHost" name="dbHost" value="localhost"/>
                                    </span>
                                </div>
                                <div class="form-row">
                                    <label for="dbPort"><%= ResourceBundleUtil.getMessage("setup.datasource.label.dbPort")%></label>
                                    <span class="form-input">
                                        <input type="text" size="40" id="dbPort" name="dbPort" value="3306"/>
                                    </span>
                                </div>
                                <div class="form-row">
                                    <label for="dbName"><%= ResourceBundleUtil.getMessage("setup.datasource.label.dbName")%></label>
                                    <span class="form-input">
                                        <input type="text" size="40" id="dbName" name="dbName" value="jwdb"/>
                                    </span>
                                </div>
                                <div class="form-row">
                                    <label for="dbUser"><%= ResourceBundleUtil.getMessage("setup.datasource.label.dbUser")%></label>
                                    <span class="form-input">
                                        <input type="text" size="40" id="dbUser" name="dbUser" value="root"/>
                                    </span>
                                </div>
                                <div class="form-row">
                                    <label for="dbPassword"><%= ResourceBundleUtil.getMessage("setup.datasource.label.dbPassword")%></label>
                                    <span class="form-input">
                                        <input type="password" size="40" id="dbPassword" name="dbPassword" value="" autocomplete="off"/>
                                    </span>
                                </div>
                            </div>
                            <div id="jdbcSetup" class="main-row-content">
                                <div class="form-row">
                                    <label for="jdbcDriver"><%= ResourceBundleUtil.getMessage("setup.datasource.label.jdbcDriver")%></label>
                                    <span class="form-input">
                                        <input type="text" size="40" id="jdbcDriver" name="jdbcDriver" value="com.mysql.jdbc.Driver"/>
                                    </span>
                                </div>
                                <div class="form-row">
                                    <label for="jdbcUrl"><%= ResourceBundleUtil.getMessage("setup.datasource.label.jdbcUrl")%></label>
                                    <span class="form-input">
                                        <input type="text" size="40" id="jdbcUrl" name="jdbcUrl" value="jdbc:mysql://localhost:3306/jwdb?characterEncoding=UTF-8"/>
                                        <input type="hidden" size="40" id="jdbcFullUrl" name="jdbcFullUrl" value=""/>
                                    </span>
                                </div>
                                <div class="form-row">
                                    <label for="jdbcUser"><%= ResourceBundleUtil.getMessage("setup.datasource.label.jdbcUser")%></label>
                                    <span class="form-input">
                                        <input type="text" size="40" id="jdbcUser" name="jdbcUser" value="root"/>
                                    </span>
                                </div>
                                <div class="form-row">
                                    <label for="jdbcPassword"><%= ResourceBundleUtil.getMessage("setup.datasource.label.jdbcPassword")%></label>
                                    <span class="form-input">
                                        <input type="password" size="40" id="jdbcPassword" name="jdbcPassword" value="" autocomplete="off"/>
                                    </span>
                                </div>
                            </div>
                            <div id="advancedSetup" class="main-row-content">
                                <div class="form-row">
                                    <label for="sampleApps"><%= ResourceBundleUtil.getMessage("setup.datasource.label.sampleApps")%></label>                                     
                                    <span class="form-input">
                                        <input type="checkbox" id="sampleApps" name="sampleApps" value="true" checked />
                                    </span>
                                </div>
                                <div class="form-row">
                                    <label for="sampleUsers"><%= ResourceBundleUtil.getMessage("setup.datasource.label.sampleUsers")%></label>                                     
                                    <span class="form-input">
                                        <input type="checkbox" id="sampleUsers" name="sampleUsers" value="true" checked />
                                    </span>
                                </div>
                            </div>                                        
                            <div class="main-row-content" id="setupStatus" style="display: none">
                                <div id="setupProgress"></div>
                            </div>
                            <div class="form-buttons">
                                <input class="form-button" id="setupButton" type="button" value="<%= ResourceBundleUtil.getMessage("setup.datasource.label.save")%>" />
                            </div>
                        </form>
                    </div>
                </div>

            </div>
        </div>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/jquery-1.9.1.min.js"></script>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/json/util.js"></script>
        <script type="text/javascript">
            var Setup = {
                currentDbType: "",
                setupStatusCallback: {
                    success: function(data) {
                        $("#setupProgress").empty();
                        var obj = JSON.parse(data);
                        var success = (obj.result === "true");
                        var label = success ? "<%= ResourceBundleUtil.getMessage("setup.datasource.label.success")%>" : '<%= ResourceBundleUtil.getMessage("setup.datasource.label.errorWithDetails")%>';
                        var labelClass = success ? "setupSuccess" : "setupError";
                        var setupResult = $("<div id='setupResult' class='" + labelClass + "'>" + label + "</div>");
//                        $("#setupStatus").append(setupResult);
                        $("#setupNotice").after(setupResult);
                        if (!success) {
                            var setupDetails = "<div id='setupErrorDetails' style='display:none'>" + obj.message + "</div>";
                            var setupDetailsLink = $("<a id='setupDetailsLink'><%= ResourceBundleUtil.getMessage("setup.datasource.label.details")%></a>");
                            $("#setupResult").append(setupDetailsLink);
                            $("#setupResult").append(setupDetails);
                            $("#setupDetailsLink").on("click", function() {
                                $("#setupErrorDetails").show();
                            });
                        }
                        $("#setupButton").show();
                        $("#setupButton").removeAttr("disabled");
                        if (success) {
                            $("#setupButton").val("<%= ResourceBundleUtil.getMessage("setup.datasource.label.done")%>");
                            $("#setupButton").off("click");
                            $("#setupButton").on("click", function() {
                                location.href = "${pageContext.request.contextPath}";
                            });
                        } else {
                            $("#setupForm input, #setupForm select").removeAttr("disabled");
                        }                            
                    },
                    error: function(data) {
                        $("#setupProgress").empty();
                        var label = '<%= ResourceBundleUtil.getMessage("setup.datasource.label.error")%>: ';
                        label += data.statusText;
                        var labelClass = "setupError";
                        var setupResult = $("<div id='setupResult' class='" + labelClass + "'>" + label + "</div>");
//                        $("#setupStatus").append(setupResult);
                        $("#setupNotice").after(setupResult);
                        $("#setupButton").show();
                        $("#setupForm input, #setupForm select").removeAttr("disabled");
                    }
                },    
                setupStatus: function() {
                    $("#setupResult").remove();
                    $("#setupButton").hide();
                    $("#setupForm input, #setupForm select").attr("disabled", "disabled")
                    $("#setupProgress").append($("<b><%= ResourceBundleUtil.getMessage("setup.datasource.label.inProgress")%></b> <img src='${pageContext.request.contextPath}/images/v3/loading.gif'/>"));
                    $("#setupStatus").show();
                    var dbType = $("#dbType").val();
                    var dbName = $("#dbName").val();
                    var jdbcDriver = $("#jdbcDriver").val();
                    var jdbcUrl = $("#jdbcUrl").val();
                    var jdbcFullUrl = (dbType !== "custom") ? $("#jdbcFullUrl").val(): "";
                    var jdbcUser = $("#jdbcUser").val();
                    var jdbcPassword = $("#jdbcPassword").val();
                    var sampleApps = $("#sampleApps:checked").length > 0;
                    var sampleUsers = $("#sampleUsers:checked").length > 0;
                    var setupParams = "dbType=" + encodeURIComponent(dbType) + "&dbName=" + encodeURIComponent(dbName) + "&jdbcDriver=" + encodeURIComponent(jdbcDriver) + "&jdbcUrl=" + encodeURIComponent(jdbcUrl) + "&jdbcFullUrl=" + encodeURIComponent(jdbcFullUrl) + "&jdbcUser=" + encodeURIComponent(jdbcUser) + "&jdbcPassword=" + encodeURIComponent(jdbcPassword);
                    if (sampleApps) {
                        setupParams += "&sampleApps=true";
                    }
                    if (sampleUsers) {
                        setupParams += "&sampleUsers=true";
                    }
                    var setupUrl = "${pageContext.request.contextPath}/setup/init";
                    ConnectionManager.post(setupUrl, Setup.setupStatusCallback, setupParams);
                },
                selectType: function() {
                    var dbType = $("#dbType").val();
                    var dbName = $("#dbName").val();
                    var dbHost = $("#dbHost").val();
                    var dbPort = $("#dbPort").val();
                    var dbUser = $("#dbUser").val();
                    var dbPassword = $("#dbPassword").val();
                    if (dbType === "oracle") {
                        $("#jdbcSetup").hide();
                        $("#dbSetup").show();
                        $("#jdbcDriver").val("oracle.jdbc.driver.OracleDriver");
                        if (Setup.currentDbType !== dbType) {
                            $("#dbPort").val("1521");
                            dbPort = 1521;
                        }
                        $("#jdbcUrl").val("jdbc:oracle:thin:@" + dbHost + ":" + dbPort + ":" + dbName);
                        $("#jdbcFullUrl").val("jdbc:oracle:thin:@" + dbHost + ":" + dbPort + ":" + dbName);
                    } else if (dbType === "sqlserver") {
                        $("#jdbcSetup").hide();
                        $("#dbSetup").show();
                        $("#jdbcDriver").val("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                        if (Setup.currentDbType !== dbType) {
                            $("#dbPort").val("1433");
                            dbPort = 1433;
                        }
                        $("#jdbcUrl").val("jdbc:sqlserver://" + dbHost + ":" + dbPort + ";SelectMethod=cursor");
                        $("#jdbcFullUrl").val("jdbc:sqlserver://" + dbHost + ":" + dbPort + ";SelectMethod=cursor;DatabaseName=" + dbName);
                    } else if (dbType === "mysql") {
                        $("#jdbcSetup").hide();
                        $("#dbSetup").show();
                        $("#jdbcDriver").val("com.mysql.jdbc.Driver");
                        if (Setup.currentDbType !== dbType) {
                            $("#dbPort").val("3306");
                            dbPort = 3306;
                        }
                        $("#jdbcUrl").val("jdbc:mysql://" + dbHost + ":" + dbPort + "/?characterEncoding=UTF-8");
                        $("#jdbcFullUrl").val("jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName + "?characterEncoding=UTF-8");
                    } else {
                        $("#jdbcUrl").val($("#jdbcFullUrl").val());
                        $("#jdbcSetup").show();
                        $("#dbSetup").hide();
                    }
                    Setup.currentDbType = dbType;
                    $("#jdbcUser").val(dbUser);
                    $("#jdbcPassword").val(dbPassword);
                }
            }
            $("#dbType, #dbSetup input").on("change", Setup.selectType)
            $("#setupButton").on("click", Setup.setupStatus);
            $(function() {
                Setup.selectType();
            });
        </script>
    </body>
</html>
