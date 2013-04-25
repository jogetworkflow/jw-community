<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<jsp:include page="header.jsp" flush="true" />

<div id="nav">
    <div id="nav-title">
        <a href="#">Home</a> &gt; <a href="#">2. Design Apps</a>
    </div>
    <div id="nav-body">
        <ul id="nav-list">
            <li><b>Service Request App</b> <a class="version" href="#"> ver1</a>
                <ul class="nav-sublist">
                    <li><input type="checkbox" checked disabled /><a id="nav-app-processes" class="nav-link" href="#">i. Processes</a></li>
                    <li><input type="checkbox" checked disabled /><a id="nav-app-forms" class="nav-link" href="#">ii. Forms</a></li>
                    <li><input type="checkbox" disabled /><a id="nav-app-lists" class="nav-link" href="#">iii. Lists</a></li>
                    <li><input type="checkbox" disabled /><a id="nav-app-lists" class="nav-link" href="#">iv. Userview</a></li>
                    <li><input type="checkbox" disabled /><a id="nav-app-props" class="nav-link" href="#">v. Properties & Export</a></li>
                </ul>
            </li>
        </ul>
    </div>
</div>

<div id="main">
    <div id="main-title"></div>
    <div id="main-action">
        <ul id="main-action-buttons">
            <li><button onclick="Designer.launch()">Launch Workflow Designer</button></li>
            <li><button>Upload Saved XPDL</button></li>
        </ul>
    </div>
    <div id="main-body">

        <ul id="main-body-list">
            <li>
                <div class="list-thumbnail"><a href="#"><img width="150" src="thumb.jpg" border="0" /></a></div>
                <div class="list-details">
                    <div class="list-name"><a href="#">Registration Process</a>
                    </div>
                    <div class="list-description">
                        <ul id="main-body-sublist">
                            <li>7 activities</li>
                            <li>2 tools</li>
                            <li>3 participants</li>
                        </ul>
                    </div>
                </div>
            </li>
            <li>
                <div class="list-thumbnail"><a href="#"><img width="150" src="thumb.jpg" border="0" /></a></div>
                <div class="list-details">
                    <div class="list-name"><a href="#">Another Process</a>
                    </div>
                    <div class="list-description">
                        <ul id="main-body-sublist">
                            <li>7 activities</li>
                            <li>2 tools</li>
                            <li>3 participants</li>
                        </ul>
                    </div>
                </div>
            </li>
        </ul>

    </div>
    <div id="footer">
        Footer
    </div>
</div>

<script>
    Designer = {
        launch: function() {
            var base = 'http://${pageContext.request.serverName}:${pageContext.request.serverPort}';
            var path = base + '${pageContext.request.contextPath}';
            document.location = base + '/jwdesigner/designer/webstart.jsp?path=' + encodeURIComponent(path) + '&deploy=deploy&locale=en';
        }
    }
    Template.init("#menu-apps", "#nav-app-processes");
</script>

<jsp:include page="footer.jsp" flush="true" />
