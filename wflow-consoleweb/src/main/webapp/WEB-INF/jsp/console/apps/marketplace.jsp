<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

    <c:set var="appName" value="${param.name}" />
    <c:set var="appId" value="${param.appId}" />
    <%@ page import="org.joget.workflow.util.WorkflowUtil" %>

        <% String theme=WorkflowUtil.getSystemSetupValue("systemTheme"); pageContext.setAttribute("theme", theme); %>

            <c:if test="${not empty theme and theme ne 'classic'}">
                <link rel="stylesheet" type="text/css"
                    href="${pageContext.request.contextPath}/css/builderTheme.css?build=<fmt:message key="
                    build.number" />" />
            </c:if>

            <script src="https://code.jquery.com/jquery-3.6.4.min.js"></script>
            <script src="https://cdnjs.cloudflare.com/ajax/libs/jszip/3.1.5/jszip.min.js"></script>


            <commons:popupHeader bodyCssClass=" builder-popup" builderTheme="${theme}" />

            <div id="main-body-header" class="marketplaceAppHeader">
                <span class="header-title"><ui:msgEscJS key="cbuilder.seamless.marketplace.joget.marketplace"/></span>
                <div id="selectContainer"></div>
                <input id="searchPlugin" type="text" placeholder="<ui:msgEscJS key="cbuilder.seamless.marketplace.plugin.search"/>">
            </div>
            <div class="content">
                <div class="page-header">
                    <h1 id="page-title" style="font-size:30px"></h1>
                    <section id="page-description"></section>
                </div>
                <div id="plugin-container"></div>
            </div>

            <style>
                *,
                *:after,
                *:before {
                    box-sizing: border-box;
                }
                #plugin-container>article>div.card-footer>div.card-meta.card-meta--views>i {
                    margin-right: 5px;
                    margin-top: 2px;
                }
                #main-body-header .header-title {
                    font-size: 21px;
                }
                #selectContainer #pluginCategory {
                    height: 35px;
                    background-color: var(--theme-primary-color-3, #fff);
                    border: 1px solid var(--theme-border-color-1);
                    border-radius: 0;
                }
                #selectContainer {
                    font-size: 15px;
                    display: inline-block;
                    float: right;
                    margin-right: 60px;
                }
                #searchPlugin {
                    width: 400px;
                    border-radius: 0px;
                    height: 35px;
                    background-color: var(--theme-primary-color-3, #fff);
                    float: right;
                    margin-right: 10px;
                }
                body .page-header {
                    padding: 1px 50px 10px;
                }
                body .page-header h1 {
                    text-transform: none;
                    font-weight: 500;
                    margin-bottom: 15px;
                    color: var(--theme-label-color-1);
                }
                body {
                    overflow: hidden;
                }
                body div.content {
                    overflow: auto;
                    height: 100%;
                    background-color: var(--theme-primary-color-3);
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
                .marketplaceAppHeader {
                    background: var(--theme-primary-color-1,#394249) !important;
                    height: unset;
                }
                .card-image img {
                    max-width: 100%;
                    display: block;
                    width: 55%;
                    border-radius: 10px;
                }
                #plugin-container {
                    width: 100%;
                    display: flex;
                    align-items: center;
                    padding: 15px 50px;
                    flex-wrap: wrap;
                }
                #plugin-container>article>div.card-header>a:nth-child(1) {
                    padding: 0;
                }
                #plugin-container .card {
                    background-color: var(--theme-primary-color-1, #fff);
                    border-radius: 15px;
                    overflow: hidden;
                    padding: 1.25rem;
                    position: relative;
                    transition: 0.15s ease-in;
                    height: 370px;
                    margin-right: 20px;
                    width: calc(25% - 20px);
                    margin-bottom: 20px;
                    border: 1px solid var(--theme-border-color-1, #e6e6e6);
                }
                #plugin-container .card:hover,
                #plugin-container .card:focus-within {
                    box-shadow: 0 0 0 2px #16C79A, 0 10px 60px 0 rgba(0, 0, 0, 0.1);
                    transform: translatey(-5px);
                }
                #plugin-container .card .card-image {
                    border-radius: 10px;
                    overflow: hidden;
                    margin: 0;
                }
                #plugin-container .card .card-header {
                    margin-top: 1.5rem;
                }
                #plugin-container .card .card-header a {
                    font-weight: 600;
                    font-size: 17px;
                    line-height: 1.25;
                    padding-right: 1rem;
                    text-decoration: none;
                    color: var(--theme-label-color-2, #4B5675);
                    will-change: transform;
                }
                #plugin-container .card .card-header .desc {
                    padding: 0px;
                    height: 70px;
                    min-height: 70px;
                    overflow: hidden;
                    color: var(--theme-label-color-2, #6c757d);
                }
                #plugin-container .card .card-header .icon-button {
                    border: 0;
                    background-color: #fff;
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    flex-shrink: 0;
                    transition: 0.25s ease;
                    z-index: 1;
                    cursor: pointer;
                    color: #565656;
                }
                #plugin-container .card .card-header .icon-button svg {
                    width: 1em;
                    height: 1em;
                }
                #plugin-container .card .card-header .icon-button:hover,
                #plugin-container .card .card-header .icon-button:focus {
                    background-color: var(--theme-active-color-2, #0069d9);
                    color: #FFF;
                }
                #plugin-container .card .card-header .icon-button {
                    border: 1px solid var(--theme-border-color-1, #e6e6e6);
                    background-color: var(--theme-primary-color-1, #fff);
                    color: var(--theme-label-color-1, #333);
                }
                #plugin-container .card .card-footer {
                    margin-top: 1.25rem;
                    border-top: 1px solid var(--theme-border-color-1, #e6e6e6);
                    padding-top: 0.7rem;
                    display: flex;
                    align-items: center;
                    flex-wrap: wrap;
                }
                #plugin-container .card .card-footer .card-meta {
                    display: flex;
                    align-items: center;
                    color: var(--theme-label-color-2, #6c757d);
                }
                #plugin-container .card .card-footer .card-meta:first-child:after,
                #plugin-container .card .card-footer .card-meta:nth-child(2):after {
                    display: block;
                    content: "";
                    width: 4px;
                    height: 4px;
                    border-radius: 50%;
                    background-color: currentcolor;
                    margin-left: 0.75rem;
                    margin-right: 0.75rem;
                }
                #plugin-container .card .card-footer .card-meta svg {
                    flex-shrink: 0;
                    width: 1em;
                    height: 1em;
                    margin-right: 0.25em;
                }
                #plugin-container .card .card-header .installing {
                    margin-left: 5px;
                }
                #plugin-container.ajaxloading{
                    min-height: 85%;
                    background-repeat: repeat-y;
                    background-size: 100%;
                    background-image: url(data:image/svg+xml;base64,PHN2ZwogIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyIgCiAgcm9sZT0iaW1nIgogIHdpZHRoPSI4MjAiCiAgaGVpZ2h0PSI0NTAiCiAgYXJpYS1sYWJlbGxlZGJ5PSJsb2FkaW5nLWFyaWEiCiAgdmlld0JveD0iMCAwIDgyMCA0NTAiCiAgcHJlc2VydmVBc3BlY3RSYXRpbz0ibm9uZSIKPgogIDx0aXRsZSBpZD0ibG9hZGluZy1hcmlhIj5Mb2FkaW5nLi4uPC90aXRsZT4KICA8cmVjdAogICAgeD0iMCIKICAgIHk9IjAiCiAgICB3aWR0aD0iMTAwJSIKICAgIGhlaWdodD0iMTAwJSIKICAgIGNsaXAtcGF0aD0idXJsKCNjbGlwLXBhdGgpIgogICAgc3R5bGU9J2ZpbGw6IHVybCgiI2ZpbGwiKTsnCiAgPjwvcmVjdD4KICA8ZGVmcz4KICAgIDxjbGlwUGF0aCBpZD0iY2xpcC1wYXRoIj4KICAgICAgICA8cmVjdCB4PSIxMCIgeT0iMTAiIHJ4PSI1IiByeT0iNSIgd2lkdGg9IjI2MCIgaGVpZ2h0PSIxNDAiPjwvcmVjdD48cmVjdCB4PSIyODAiIHk9IjEwIiByeD0iNSIgcnk9IjUiIHdpZHRoPSIyNjAiIGhlaWdodD0iMjgwIj48L3JlY3Q+PHJlY3QgeD0iNTUwIiB5PSIxMCIgcng9IjUiIHJ5PSI1IiB3aWR0aD0iMjYwIiBoZWlnaHQ9IjE0MCI+PC9yZWN0PjxyZWN0IHg9IjEwIiB5PSIxNjAiIHJ4PSI1IiByeT0iNSIgd2lkdGg9IjI2MCIgaGVpZ2h0PSIyODAiPjwvcmVjdD48cmVjdCB4PSIyODAiIHk9IjMwMCIgcng9IjUiIHJ5PSI1IiB3aWR0aD0iMjYwIiBoZWlnaHQ9IjE0MCI+PC9yZWN0PjxyZWN0IHg9IjU1MCIgeT0iMTYwIiByeD0iNSIgcnk9IjUiIHdpZHRoPSIyNjAiIGhlaWdodD0iMjgwIj48L3JlY3Q+CiAgICA8L2NsaXBQYXRoPgogICAgPGxpbmVhckdyYWRpZW50IGlkPSJmaWxsIj4KICAgICAgPHN0b3AKICAgICAgICBvZmZzZXQ9IjAuNTk5OTY0IgogICAgICAgIHN0b3AtY29sb3I9IiNmM2YzZjMiCiAgICAgICAgc3RvcC1vcGFjaXR5PSIxIgogICAgICA+CiAgICAgICAgPGFuaW1hdGUKICAgICAgICAgIGF0dHJpYnV0ZU5hbWU9Im9mZnNldCIKICAgICAgICAgIHZhbHVlcz0iLTI7IC0yOyAxIgogICAgICAgICAga2V5VGltZXM9IjA7IDAuMjU7IDEiCiAgICAgICAgICBkdXI9IjFzIgogICAgICAgICAgcmVwZWF0Q291bnQ9ImluZGVmaW5pdGUiCiAgICAgICAgPjwvYW5pbWF0ZT4KICAgICAgPC9zdG9wPgogICAgICA8c3RvcAogICAgICAgIG9mZnNldD0iMS41OTk5NiIKICAgICAgICBzdG9wLWNvbG9yPSIjZWNlYmViIgogICAgICAgIHN0b3Atb3BhY2l0eT0iMSIKICAgICAgPgogICAgICAgIDxhbmltYXRlCiAgICAgICAgICBhdHRyaWJ1dGVOYW1lPSJvZmZzZXQiCiAgICAgICAgICB2YWx1ZXM9Ii0xOyAtMTsgMiIKICAgICAgICAgIGtleVRpbWVzPSIwOyAwLjI1OyAxIgogICAgICAgICAgZHVyPSIxcyIKICAgICAgICAgIHJlcGVhdENvdW50PSJpbmRlZmluaXRlIgogICAgICAgID48L2FuaW1hdGU+CiAgICAgIDwvc3RvcD4KICAgICAgPHN0b3AKICAgICAgICBvZmZzZXQ9IjIuNTk5OTYiCiAgICAgICAgc3RvcC1jb2xvcj0iI2YzZjNmMyIKICAgICAgICBzdG9wLW9wYWNpdHk9IjEiCiAgICAgID4KICAgICAgICA8YW5pbWF0ZQogICAgICAgICAgYXR0cmlidXRlTmFtZT0ib2Zmc2V0IgogICAgICAgICAgdmFsdWVzPSIwOyAwOyAzIgogICAgICAgICAga2V5VGltZXM9IjA7IDAuMjU7IDEiCiAgICAgICAgICBkdXI9IjFzIgogICAgICAgICAgcmVwZWF0Q291bnQ9ImluZGVmaW5pdGUiCiAgICAgICAgPjwvYW5pbWF0ZT4KICAgICAgPC9zdG9wPgogICAgPC9saW5lYXJHcmFkaWVudD4KICA8L2RlZnM+Cjwvc3ZnPg==);
                }
                body .boxy-wrapper .title-bar .close {
                    top: 20px;
                    right: 20px;
                }
                section#page-description {
                    color: var(--theme-label-color-1);
                }
            </style>
            <script>
                var buildeType = '${type}';
                var pluginType = '${pluginType}';
                $(document).keypress(function (event) {
                    if (event.which === 13) {
                        searchPlugin();
                    }
                });

                //install plugin in seamless marketplace
                function installPlugin(id) {
                    var installUrl = "${pageContext.request.contextPath}/web/json/apps/install";
                    if (confirm('<ui:msgEscJS key="appCenter.label.confirmInstallation"/>')) {
                        var installCallback = {
                            success: function (data) {
                                $("#installplugin_" + id).html('<ui:msgEscJS key="appCenter.label.installApp"/>');
                                $("#installplugin_" + id).removeAttr("disabled");
                                var selectedValue = $('#pluginCategory').val();
                                parent.CustomBuilder.Builder.reloadPaletteOrProperties(selectedValue);
                                var app = JSON.parse(data);
                                var appId = app.appId;
                                if (appId && appId !== "") {
                                    alert('<ui:msgEscJS key="appCenter.label.appInstalled"/>');
                                } if (app.pluginName) {
                                    alert('<ui:msgEscJS key="appCenter.label.appInstalled"/>');
                                } else {
                                    alert('<ui:msgEscJS key="appCenter.label.appNotInstalled"/>');
                                }
                            },
                            error: function (data) {
                                $("#installplugin_" + id).html('<ui:msgEscJS key="appCenter.label.installApp"/>');
                                $("#installplugin_" + id).removeAttr("disabled");
                                alert('<ui:msgEscJS key="appCenter.label.appNotInstalled"/>');
                            }
                        };
                        $("#installplugin_" + id).html('<i class="icon-spinner icon-spin fas fa-spinner fa-spin"></i><span class="installing"><ui:msgEscJS key="appCenter.label.installingApp"/></span>');
                        $("#installplugin_" + id).attr("disabled", "disabled");

                        // invoke installation
                        var installParams = "url=" + encodeURIComponent("http://localhost:8080/jw/web/json/plugin/org.joget.marketplace.ProtectedAppUpload/service?action=download&id=" + id);
                        ConnectionManager.post(installUrl, installCallback, installParams);
                    }
                }

                //show plugins based on selected category
                function selectedCategory() {
                    $('#plugin-container').addClass('ajaxloading');
                    var searhText = $('#searchPlugin').val();
                    var selectedValue = $('#pluginCategory').val();
                    $('#plugin-container').empty();
                    if (selectedValue === 'all') {
                        getPluginCategories(selectedValue, false);
                    } else {
                        setTitle(selectedValue);
                        getPluginList(searhText, selectedValue);
                    }
                }

                //search plugin based on the search value
                var searchPlugin = function () {
                    $('#plugin-container').addClass('ajaxloading');
                    var searhText = $('#searchPlugin').val();
                    var selectedValue = $('#pluginCategory').val();
                    $('#plugin-container').empty();
                    getPluginList(searhText, selectedValue);

                }

                // get builder label
                var getBuilder = function (type) {
                    switch (type) {
                        case 'form':
                            return "Form";
                        case 'datalist':
                            return "Datalist";
                        case 'userview':
                            return "Userview";
                        case 'process':
                            return "Process";
                        case 'app':
                        default:
                            return "All";
                    }
                };

                //Set header title and description
                var setTitle = function (title) {
                    $('body > div.content > div.page-header > #page-title').text(title);
                    $('body > div.content > div.page-header > #page-description').text('<ui:msgEscJS key="cbuilder.seamless.marketplace.plugin.related"/> ' + title);
                }

                //get list of plugin categories
                var getPluginCategories = function (category, updateSelectbox) {
                    var searhText = $('#searchPlugin').val();
                    var pluginCategoriesUrl = "http://localhost:8080/jw/web/json/marketplace/plugin/list?type=category&d-7098245-fn_publish=true";
                    if (getBuilder(buildeType) !== 'All') {
                        pluginCategoriesUrl += "&d-7098245-fn_joget_component=" + getBuilder(buildeType);
                    }
                    $.ajax({
                        url: pluginCategoriesUrl,
                        method: 'GET',
                        dataType: 'json',
                        success: function (data) {
                            const select = $('<select id="pluginCategory" onChange="selectedCategory()">'); // Create a select element
                            const option = $('<option>').text("All").val("all");
                            select.append(option);
                            // Loop through the options and add category options
                            $.each(data.data, function (index, optionValue) {
                                const option = $('<option>').text(optionValue.category_name).val(optionValue.category_name);
                                if (category === "all" || (category === "undefined")) {
                                    getPluginList(searhText, optionValue.category_name);
                                } else if (optionValue.category_name === category) {
                                    getPluginList(searhText, optionValue.category_name);
                                    option.prop('selected', true);
                                }
                                select.append(option);
                            });
                            if (updateSelectbox) {
                                $('#selectContainer').append(select);
                            }
                            if (category !== "all" && category !== "undefined") {
                                setTitle(category + ' <ui:msgEscJS key="cbuilder.seamless.marketplace.plugins"/>');
                            } else {
                                setTitle(getBuilder(buildeType) + ' <ui:msgEscJS key="cbuilder.seamless.marketplace.plugins"/>');
                            }
                        },
                        error: function (data) {
                            alert('Failed Getting plugin categories');
                        }
                    });
                }

                //render list of plugins in card based on category and search value
                var getPluginList = function (searhText, category) {
                    var verifyUrl = "http://localhost:8080/jw/web/json/marketplace/plugin/list";
                    if (searhText !== '' || (category !== '' && category !== undefined)) {
                        var params = "?";
                        if (searhText !== '') {
                            verifyUrl += params + "d-8160208-fn_AppName=" + encodeURIComponent(searhText);
                            params = "&"; // Change the parameter delimiter to "&" for subsequent parameters
                        }
                        if (category !== '' && category !== undefined && category !== 'all') {
                            verifyUrl += params + "d-8160208-fn_SubCategory=" + encodeURIComponent(category);
                        }
                    }

                    $.ajax({
                        url: verifyUrl,
                        method: 'GET',
                        dataType: 'json',
                        success: function (data) {
                            $('#plugin-container').removeClass('ajaxloading');
                            var cardArray = data.data;
                            $.each(cardArray, function (index, card) {
                                var urlforImage = 'http://localhost:8080/jw/web/json/marketplace/plugin/' + card.id + '/download/' + encodeURIComponent(card.Picture);
                                fetch(urlforImage, {
                                    method: 'GET'
                                })
                                    .then(res => res.blob())
                                    .then(blob => {
                                        var reader = new FileReader();
                                        reader.onloadend = function () {
                                            var base64Image = reader.result;
                                            const cardElement = $(
                                                        `<article class="card">
                                    <figure class="card-image">
                                        <img src="` + base64Image + `" alt="An orange painted blue, cut in half laying on a blue background" />
                                    </figure>
                                    <div class="card-header">
                                        <a href="#">` + card.AppName + `</a>
                                        <p class="desc">` + card.DescBrief + `</p>
                                        <button id="installplugin_` + card.id + `" onclick="installPlugin('` + card.id + `')" class="icon-button">
                                            <span> Install</span>
                                        </button>
                                    </div>
                                    <div class="card-footer">
                                        <div class="card-meta card-meta--views">
                                            <i class="far fa-arrow-alt-circle-down"></i>` + card.Count + `
                                        </div>
                                        <div class="card-meta card-meta--views">
                                            <i class="far fa-star"></i> 5.0
                                        </div>
                                        <div class="card-meta card-meta--views">
                                            <i class="far fa-file"></i> ` + card.AppSize + `KB
                                        </div>
                                    </div>
                                </article>`
                                                        );
                                            cardElement.appendTo("#plugin-container");
                                        };
                                        reader.readAsDataURL(blob);
                                    })
                            });
                        },
                        error: function (data) {
                            $('#plugin-container').removeClass('ajaxloading');
                            alert('Failed loading plugins');
                        }
                    });
                };
                $(document).ready(function () {
                    $('#plugin-container').addClass('ajaxloading');
                    getPluginCategories(pluginType, true);
                });
            </script>
            <commons:popupFooter />