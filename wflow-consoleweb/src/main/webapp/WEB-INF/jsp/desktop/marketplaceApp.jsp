<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<c:set var="appUrl" value="${param.url}"/>
<c:set var="appName" value="${param.name}"/>
<c:set var="appId" value="${param.appId}"/>

<commons:popupHeader />

    <div id="main-body-header">
    </div>

    <div id="main-body-actions">
        <button id="installApp" style="display:none"><fmt:message key="appCenter.label.installApp"/></button>
    </div>
    
    <div id="main-body-content">
        <iframe id="marketplaceAppFrame" src='<c:out value="${appUrl}"/>' width="99%" height="98%"></iframe>
    </div>    

    <style>
        #main-action-help {
            display: none;
        }
    </style>
    <script>
        var verifyApp = function(downloadUrl) {
            if (typeof downloadUrl === "undefined" || downloadUrl === null || downloadUrl === "") {
                $("#installApp").hide();                
                return;
            }
            var verifyUrl = "${pageContext.request.contextPath}/web/json/apps/verify?url=" + encodeURIComponent(downloadUrl);
            $.ajax({
                type: 'HEAD',
                url: verifyUrl,
                success: function(data) {
                    $("#installApp").show();
                    HelpGuide.key = "help.web.desktop.marketplace";
                    HelpGuide.show();
                },
                error: function(data) {
                    $("#installApp").hide();                
                }
            });            
            $("#installApp").off("click");
            $("#installApp").on("click", function() {
                var installUrl = "${pageContext.request.contextPath}/web/json/apps/install";
                if (confirm("<fmt:message key="appCenter.label.confirmInstallation"/>")) {
                    var installCallback = {
                        success: function(data) {
                            $("#installApp").html('<fmt:message key="appCenter.label.installApp"/>');
                            $("#installApp").removeAttr("disabled");
                            var app = JSON.parse(data);
                            var appId = app.appId;
                            if (appId && appId !== "") {
                                alert("<fmt:message key="appCenter.label.appInstalled"/>");
                                PopupDialog.closeDialog();                                
                                parent.loadPublishedApps();
                            } else {
                                alert("<fmt:message key="appCenter.label.appNotInstalled"/>");
                            }
                        },
                        error: function(data) {
                            $("#installApp").html('<fmt:message key="appCenter.label.installApp"/>');
                            $("#installApp").removeAttr("disabled");
                            alert("<fmt:message key="appCenter.label.appNotInstalled"/>");
                        }
                    };
                    // show loading icon
                    HelpGuide.hide();
                    $("#installApp").html('<i class="icon-spinner icon-spin"></i> <fmt:message key="appCenter.label.installingApp"/>');
                    $("#installApp").attr("disabled", "disabled");
        
                    // invoke installation
                    var installParams = "url=" + encodeURIComponent(downloadUrl);
                    ConnectionManager.post(installUrl, installCallback, installParams);
                }
            });
        };
        window.addEventListener('message', function(event) {
            var marketplaceUrl = "<fmt:message key="appCenter.link.marketplace.url"/>";
            var marketplaceTrustedUrls = "<fmt:message key="appCenter.link.marketplace.trusted"/>";
            if (marketplaceUrl.indexOf(event.origin) === 0 || marketplaceTrustedUrls.indexOf(event.origin) >= 0) {
                var downloadUrl = event.data;
                verifyApp(downloadUrl);
            } else {
                verifyApp("");
            }
        });
    </script>
    
<commons:popupFooter />
