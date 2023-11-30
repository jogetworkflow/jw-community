<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<jsp:useBean id="PropertyUtil" class="org.joget.plugin.property.service.PropertyUtil" scope="page"/>
<%@ page import="org.joget.workflow.util.WorkflowUtil"%>

<%
    String theme = WorkflowUtil.getSystemSetupValue("systemTheme");
    pageContext.setAttribute("theme", theme);
%>

<c:if test="${not empty theme and theme ne 'classic'}">
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/builderTheme.css?build=<fmt:message key="build.number"/>" />
</c:if>

<commons:popupHeader builderTheme="${theme}"/>
<c:if test="${!empty propertyEditable}">
    <jsp:include page="/WEB-INF/jsp/console/plugin/library.jsp" />
</c:if>

<div id="main-body-header">
    <c:choose>
        <c:when test="${!empty title}">
            <c:out value=" ${title}" escapeXml="true" />
        </c:when>
        <c:otherwise>
            <fmt:message key="console.plugin.label.pluginConfiguration"/> <ui:stripTag html=" ${param.title}"/>
        </c:otherwise>    
    </c:choose>
</div>

<div id="main-body-content" style="text-align: left;">
<c:if test="${!empty errors}">
    <span class="form-errors" style="display:block">
        <c:forEach items="${errors}" var="error">
            <fmt:message key="${error}"/>
        </c:forEach>
    </span>
</c:if>
<c:choose>
    <c:when test="${empty plugin}">
        <span class="form-errors" style="display:block"><fmt:message key="console.plugin.label.pluginNoProperties"/></span>
    </c:when>
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
                            <input type="submit" value="<ui:msgEscHTML key="general.method.label.submit"/>"/>
                        </div>
                    </c:otherwise>
                </c:choose>
           </fieldset>
        </form>
    </c:when>
    <c:otherwise>
        <div id="propertyEditor" class="pluginConfig menu-wizard-container">

        </div>
        <form id="propertiesForm" action="${actionUrl}" class="form blockui" method="POST" style="display:none">
            <input id="pluginProperties" name="pluginProperties" type="hidden" value=""/>
        </form>
        <script>
            function savePlugin(container, properties){
                $("#pluginProperties").val(JSON.encode(properties));
                $("#propertiesForm").submit();
            }

            function savePluginFailed(container, returnedErrors){
                var errorMsg = '<ui:msgEscJS key="console.plugin.label.youHaveFollowingErrors"/>:\n';
                for(key in returnedErrors){
                    if (returnedErrors[key].fieldName === undefined || returnedErrors[key].fieldName === "") {
                        errorMsg += returnedErrors[key].message + '\n';
                    } else {
                        errorMsg += returnedErrors[key].fieldName + ' : ' + returnedErrors[key].message + '\n';
                    }
                }
                alert(errorMsg);
            }
            
            function cancel(container){
                document.location = "${cancelUrl}";
            }

            $(document).ready(function(){
                var options = {
                    contextPath: '${pageContext.request.contextPath}',
                    <c:if test="${!empty appDef}">
                        appPath: '/${appDef.appId}/${appDef.version}',
                    </c:if>
                    <c:choose>
                        <c:when test="${!empty propertiesDefinition}">
                            propertiesDefinition : ${propertiesDefinition},
                        </c:when>
                        <c:when test="${!empty propertyEditable.propertyOptions}">
                            propertiesDefinition : ${PropertyUtil.injectHelpLink(plugin.helpLink, propertyEditable.propertyOptions)},
                        </c:when>
                    </c:choose>
                    <c:choose>
                        <c:when test="${!empty properties && fn:substring(properties, 0, 1) eq '{'}">
                            propertyValues : ${properties},
                        </c:when>
                    </c:choose>
                    <c:if test="${!empty defaultProperties && fn:substring(defaultProperties, 0, 1) eq '{'}">
                        defaultPropertyValues : ${defaultProperties},
                    </c:if>
                    <c:if test="${!empty skipValidation && skipValidation}">
                        skipValidation : ${skipValidation},
                    </c:if>
                    <c:choose>
                        <c:when test="${!empty cancelUrl}">
                            cancelCallback: cancel,
                            showCancelButton: true,        
                        </c:when>
                        <c:otherwise>
                            showCancelButton: false,
                        </c:otherwise>    
                    </c:choose>        
                    saveCallback: savePlugin,
                    saveButtonLabel: '<c:choose><c:when test="${!empty submitLabel}"><ui:msgEscJS key="${submitLabel}"/></c:when><c:otherwise><ui:msgEscJS key="general.method.label.submit"/></c:otherwise></c:choose>',
                    cancelButtonLabel: '<c:choose><c:when test="${!empty cancelLabel}"><ui:msgEscJS key="${cancelLabel}"/></c:when><c:otherwise><ui:msgEscJS key="general.method.label.cancel"/></c:otherwise></c:choose>',
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