<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<c:set var="appDef" scope="request" value="${appDefinition}"/>
<c:set var="builderLabel" scope="request" value="${builder.label}"/>
<c:set var="builderObjectLabel" scope="request" value="${builder.objectLabel}"/>
<c:set var="builderI18N" scope="request" value=""/>
<c:set var="builderJS" scope="request" value="${builder.getBuilderJS(pageContext.request.contextPath, builderNumber)}"/>
<c:set var="builderCSS" scope="request" value="${builder.getBuilderCSS(pageContext.request.contextPath, builderNumber)}"/>
<c:set var="builderCode" scope="request" value="${builder.objectName}"/>
<c:set var="builderColor" scope="request" value="${builder.color}"/>
<c:set var="builderIcon" scope="request" value="${builder.icon}"/>
<c:set var="builderDef" scope="request" value="${builderDefinition}"/>
<c:set var="builderDefJson" scope="request" value="${json}"/>
<c:set var="builderCanvas" scope="request" value="${builderHTML}"/>
<c:set var="builderConfig" scope="request" value="${builder.builderConfig}"/>
<c:set var="builderProps" scope="request" value="${builder.propertyOptions}"/>
<c:set var="saveUrl" scope="request" value="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/cbuilder/${builder.objectName}/save/${builderDefinition.id}"/>
<c:set var="previewUrl" scope="request" value="${pageContext.request.contextPath}/web/console/app/${appId}/${appVersion}/cbuilder/${builder.objectName}/preview/${builderDefinition.id}"/>

<jsp:include page="../cbuilder/base.jsp" flush="true" />