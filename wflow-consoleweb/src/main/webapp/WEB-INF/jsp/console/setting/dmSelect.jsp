<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:popupHeader />
<div id="main-body-header">
    <fmt:message key="console.setting.directory.label.list.choose"/>
</div>
<div id="main-body-content">
    <ui:jsontable url="${pageContext.request.contextPath}/web/console/setting/directoryManagerImpl/list"
                  var="JsonDataTable"
                  divToUpdate="pluginList"
                  jsonData="data"
                  rowsPerPage="15"
                  width="100%"
                  sort="name"
                  desc="false"
                  href="${pageContext.request.contextPath}/web/console/setting/directoryManagerImpl/config"
                  hrefParam="directoryManagerImpl"
                  hrefQuery="true"
                  hrefDialog="false"
                  searchItems="name|Name"
                  fields="['id','name','description','version']"
                  column1="{key: 'name', label: 'console.plugin.label.name', sortable: false, width: 180}"
                  column2="{key: 'description', label: 'console.plugin.label.description', sortable: false, width: 300}"
                  column3="{key: 'version', label: 'console.plugin.label.version', sortable: false, width: 140}"
    />
</div>
<script>
    const observer = new MutationObserver(function() {
        const defaultDmRow = $('table#pluginList tr#rowdefault');
        if (defaultDmRow.length > 0) {
            defaultDmRow.on('click', function (e) {
                if (confirm("<fmt:message key="console.setting.directory.label.changeToDefaultPluginConfirm"/>")) {
                    const callback = {
                        success: () => {
                            if (parent !== self) {
                                parent.location.reload();
                            } else {
                                location.reload();
                            }
                        },
                        error: () => {
                            alert('<fmt:message key="console.setting.directory.label.changeToDefaultError"/>');
                        },
                    };
                    ConnectionManager.post('${pageContext.request.contextPath}/web/console/setting/directoryManagerImpl/config/submit', callback, {id: 'default'});
                }
                e.stopPropagation();
            });
            observer.disconnect();
        }
    });
    observer.observe(document.querySelector('#main-body-content'), {attributes: false, childList: true, characterData: false, subtree:true});

    $(document).ready(function(){
        $('#JsonDataTable_pluginList-search').hide();
    });
</script>
<commons:popupFooter />
