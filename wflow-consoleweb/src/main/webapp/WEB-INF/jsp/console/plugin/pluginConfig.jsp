<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<%@ page import="org.joget.workflow.util.WorkflowUtil"%>

<%
    String rightToLeft = WorkflowUtil.getSystemSetupValue("rightToLeft");
    pageContext.setAttribute("rightToLeft", rightToLeft);
%>
<commons:popupHeader />

<c:if test="${!empty propertyEditable}">
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/JSONError.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/JSON.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/tiny_mce/jquery.tinymce.js"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/web/console/i18n/peditor"></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery/jquery.propertyeditor.js"></script>
    <link href="${pageContext.request.contextPath}/css/jquery.propertyeditor.css" rel="stylesheet" type="text/css" />
    <c:if test="${rightToLeft == 'true'}">
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/jquery.propertyeditor_rtl.css">
    </c:if>
</c:if>

<div id="main-body-header">
    <fmt:message key="console.plugin.label.pluginConfiguration"/>
</div>

<div id="main-body-content" style="text-align: left;">

<c:choose>
    <c:when test="${empty propertyEditable}">
        <dl>
            <dt><fmt:message key="console.plugin.label.name"/></dt>
            <dd>${plugin.name}&nbsp;</dd>
            <dt><fmt:message key="console.plugin.label.description"/></dt>
            <dd>${plugin.description}&nbsp;</dd>
            <dt><fmt:message key="console.plugin.label.version"/></dt>
            <dd>${plugin.version}&nbsp;</dd>
        </dl>
        <form action="${actionUrl}" class="form" method="POST">
            <fieldset>
                <legend><fmt:message key="console.plugin.label.pluginProperties"/></legend>
                <c:catch var="exception">
                    <c:forEach var="prop" items="${plugin.pluginProperties}" varStatus="rowCounter">
                        <div class="form-row plugin-config">
                            <label for="field1">${prop.label}</label>
                            <span class="form-input">
                                <c:choose>
                                    <c:when test="${prop.type == 'textfield'}">
                                        <c:set var="value" value=""/>
                                        <c:if test="${empty propertyMap[prop.name] && empty defaultPropertyMap[prop.name]}">
                                            <c:set var="value" value="${prop.value}"/>
                                        </c:if>
                                        <c:if test="${!empty propertyMap[prop.name]}">
                                            <c:set var="value" value="${propertyMap[prop.name]}"/>
                                        </c:if>
                                        <input class="full-width" type="text" name="${prop.name}" value="<c:out value="${value}" escapeXml="true" />"/>
                                    </c:when>
                                    <c:when test="${prop.type == 'password'}">
                                        <c:set var="value" value=""/>
                                        <c:if test="${empty propertyMap[prop.name] && empty defaultPropertyMap[prop.name]}">
                                            <c:set var="value" value="${prop.value}"/>
                                        </c:if>
                                        <c:if test="${!empty propertyMap[prop.name]}">
                                            <c:set var="value" value="${propertyMap[prop.name]}"/>
                                        </c:if>
                                        <input class="full-width" type="password" name="${prop.name}" value="<c:out value="${value}" escapeXml="true" />"/>
                                    </c:when>
                                    <c:when test="${prop.type == 'textarea'}">
                                        <c:set var="value" value=""/>
                                        <c:if test="${empty propertyMap[prop.name] && empty defaultPropertyMap[prop.name]}">
                                            <c:set var="value" value="${prop.value}"/>
                                        </c:if>
                                        <c:if test="${!empty propertyMap[prop.name]}">
                                            <c:set var="value" value="${propertyMap[prop.name]}"/>
                                        </c:if>
                                        <textarea class="full-width" name="${prop.name}" cols="60" rows="15">${value}</textarea>
                                    </c:when>
                                    <c:when test="${prop.type == 'checkbox'}">
                                        <c:set var="value" value=""/>
                                        <c:if test="${empty propertyMap[prop.name] && empty defaultPropertyMap[prop.name]}">
                                            <c:set var="value" value="${prop.value}"/>
                                        </c:if>
                                        <c:if test="${!empty propertyMap[prop.name]}">
                                            <c:set var="value" value="${propertyMap[prop.name]}"/>
                                        </c:if>
                                        <c:forEach var="option" items="${prop.options}" varStatus="rowCounter">
                                            <c:set var="checked" value=""/>
                                            <c:forEach var="val" items="${fn:split(value, ',')}">
                                                <c:if test="${val == option}">
                                                    <c:set var="checked" value="checked"/>
                                                </c:if>
                                            </c:forEach>
                                            <input type="checkbox" ${checked} name="${prop.name}" value="${option}"/>&nbsp;${option}<br>
                                        </c:forEach>
                                    </c:when>
                                    <c:when test="${prop.type == 'radio'}">
                                        <c:set var="value" value=""/>
                                        <c:if test="${empty propertyMap[prop.name] && empty defaultPropertyMap[prop.name]}">
                                            <c:set var="value" value="${prop.value}"/>
                                        </c:if>
                                        <c:if test="${!empty propertyMap[prop.name]}">
                                            <c:set var="value" value="${propertyMap[prop.name]}"/>
                                        </c:if>
                                        <c:forEach var="option" items="${prop.options}" varStatus="rowCounter">
                                            <c:set var="checked"><c:if test="${value == option}"> checked</c:if></c:set>
                                            <input type="radio" ${checked} name="${prop.name}" value="${option}"/>&nbsp;${option}<br>
                                        </c:forEach>
                                    </c:when>
                                    <c:when test="${prop.type == 'selectbox'}">
                                        <c:set var="value" value=""/>
                                        <c:if test="${empty propertyMap[prop.name] && empty defaultPropertyMap[prop.name]}">
                                            <c:set var="value" value="${prop.value}"/>
                                        </c:if>
                                        <c:if test="${!empty propertyMap[prop.name]}">
                                            <c:set var="value" value="${propertyMap[prop.name]}"/>
                                        </c:if>
                                        <select class="full-width" name="${prop.name}">
                                            <c:forEach var="option" items="${prop.options}" varStatus="rowCounter">
                                                <c:set var="selected"><c:if test="${value == option}"> selected</c:if></c:set>
                                                <option value="${option}" ${selected}>${option}</option>
                                            </c:forEach>
                                        </select>
                                    </c:when>
                                </c:choose>
                                <c:if test="${!empty defaultPropertyMap[prop.name]}">
                                    <i><fmt:message key="console.plugin.label.default"/>&nbsp;
                                        <c:choose>
                                            <c:when test="${prop.type == 'password'}">
                                                <fmt:message key="console.plugin.label.secretContent"/>
                                            </c:when>
                                            <c:otherwise>
                                                ${defaultPropertyMap[prop.name]}
                                            </c:otherwise>
                                        </c:choose>
                                    </i>
                                </c:if>
                            </span>
                        </div>
                    </c:forEach>
                </c:catch>
                <c:choose>
                    <c:when test="${exception != null}">
                        <fmt:message key="console.plugin.label.pluginNoProperties"/>
                    </c:when>
                    <c:otherwise>
                        <div class="form-buttons">
                            <input type="submit" value="<fmt:message key="general.method.label.submit"/>"/>
                        </div>
                    </c:otherwise>
                </c:choose>
           </fieldset>
        </form>
    </c:when>
    <c:otherwise>
        <div id="propertyEditor" class="pluginConfig menu-wizard-container">

        </div>
        <form id="propertiesForm" action="${actionUrl}" class="form" method="POST" style="display:none">
            <input id="pluginProperties" name="pluginProperties" type="hidden" value=""/>
        </form>
        <script>
            function savePlugin(container, properties){
                $("#pluginProperties").val(JSON.encode(properties));
                $("#propertiesForm").submit();
            }

            function savePluginFailed(container, returnedErrors){
                var errorMsg = '<fmt:message key="console.plugin.label.youHaveFollowingErrors"/>:\n';
                for(key in returnedErrors){
                    errorMsg += returnedErrors[key].fieldName + ' : ' + returnedErrors[key].message + '\n';
                }
                alert(errorMsg);
            }

            $(document).ready(function(){
                var options = {
                    contextPath: '${pageContext.request.contextPath}',
                    tinyMceScript: '${pageContext.request.contextPath}/js/tiny_mce/tiny_mce.js',
                    <c:if test="${!empty propertyEditable.propertyOptions}">
                        propertiesDefinition : ${propertyEditable.propertyOptions},
                    </c:if>
                    <c:choose>
                        <c:when test="${!empty properties && fn:substring(properties, 0, 1) eq '{'}">
                            propertyValues : ${properties},
                        </c:when>
                        <c:when test="${!empty propertyEditable.defaultPropertyValues}">
                            propertyValues : ${propertyEditable.defaultPropertyValues},
                        </c:when>
                    </c:choose>
                    <c:if test="${!empty defaultProperties && fn:substring(defaultProperties, 0, 1) eq '{'}">
                        defaultPropertyValues : ${defaultProperties},
                    </c:if>
                    <c:if test="${skipValidation}">
                        skipValidation : ${skipValidation},
                    </c:if>
                    showCancelButton: false,
                    saveCallback: savePlugin,
                    saveButtonLabel: '<fmt:message key="general.method.label.submit"/>',
                    cancelButtonLabel: '<fmt:message key="general.method.label.cancel"/>',
                    closeAfterSaved: false,
                    validationFailedCallback: savePluginFailed
                }
                $('.menu-wizard-container').propertyEditor(options);
            });
        </script>
    </c:otherwise>
</c:choose>

</div>

<commons:popupFooter />