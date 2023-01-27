<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<c:set var="appName" value="${param.name}"/>
<c:set var="appId" value="${param.appId}"/>

<commons:popupHeader />

    <div id="main-body-header" class="marketplaceAppHeader">
    </div>

    <div id="main-body-actions">
        <button id="installApp" style="display:none"><fmt:message key="appCenter.label.installApp"/></button>
    </div>
    
    <iframe id="marketplaceAppFrame" src='<c:out value="${appUrl}"/>' data-support="App;Plugin"></iframe>

    <style>
        body {
            overflow: hidden;
        }
        #marketplaceAppFrame {
            position: absolute;
            top: 34px;
            left: 0;
            width: 100%;
            height: calc(100% - 34px);
            border: 0;
            margin-top: 7px
        }
    </style>
    <script>
        var verifyApp = function(downloadUrl) {
            if (typeof downloadUrl === "undefined" || downloadUrl === null || downloadUrl === "") {
                $("#installApp").hide();
                $("#main-action-help").hide(); 
                return;
            }
            var verifyUrl = "${pageContext.request.contextPath}/web/json/apps/verify?url=" + encodeURIComponent(downloadUrl);
            $.ajax({
                type: 'HEAD',
                url: verifyUrl,
                success: function(data) {
                    $("#installApp").show();
                    $("#main-action-help").show(); 
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
                if (confirm('<ui:msgEscJS key="appCenter.label.confirmInstallation"/>')) {
                    var installCallback = {
                        success: function(data) {
                            $("#installApp").html('<ui:msgEscJS key="appCenter.label.installApp"/>');
                            $("#installApp").removeAttr("disabled");
                            var app = JSON.parse(data);
                            var appId = app.appId;
                            if (appId && appId !== "") {
                                alert('<ui:msgEscJS key="appCenter.label.appInstalled"/>');
                                PopupDialog.closeDialog();                                
                                parent.AppCenter.loadPublishedApps();
                                parent.AdminBar.hideQuickOverlay();
                            } if (app.pluginName) {
                                alert('<ui:msgEscJS key="appCenter.label.appInstalled"/>');
                            } else {
                                alert('<ui:msgEscJS key="appCenter.label.appNotInstalled"/>');
                            }
                        },
                        error: function(data) {
                            $("#installApp").html('<ui:msgEscJS key="appCenter.label.installApp"/>');
                            $("#installApp").removeAttr("disabled");
                            alert('<ui:msgEscJS key="appCenter.label.appNotInstalled"/>');
                        }
                    };
                    // show loading icon
                    HelpGuide.hide();
                    $("#installApp").html('<i class="icon-spinner icon-spin fas fa-spinner fa-spin"></i> <ui:msgEscJS key="appCenter.label.installingApp"/>');
                    $("#installApp").attr("disabled", "disabled");
        
                    // invoke installation
                    var installParams = "url=" + encodeURIComponent(downloadUrl);
                    ConnectionManager.post(installUrl, installCallback, installParams);
                }
            });
        };
        window.addEventListener('message', function(event) {
            var marketplaceUrl = '<ui:msgEscJS key="appCenter.link.marketplace.url"/>';
            var marketplaceTrustedUrls = '<ui:msgEscJS key="appCenter.link.marketplace.trusted"/>';
            if (marketplaceUrl.indexOf(event.origin) === 0 || marketplaceTrustedUrls.indexOf(event.origin) >= 0) {
                var downloadUrl = event.data;
                verifyApp(downloadUrl);
            } else {
                verifyApp("");
            }
        });
        $(document).ready(function(){
            $("#marketplaceAppFrame").on('load', function(){
                var iframeEl = document.getElementById('marketplaceAppFrame');
                iframeEl.contentWindow.postMessage("Plugin", '*');
            });
        });
    </script>
    
<commons:popupFooter />
