<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<style>
    #assignment-body h3 {
        margin-top: 50px;
        color: #666666;
        text-align: center;
        font-family: Arial, sans-serif;
        font-size: 24px;
        font-weight: bold;
    }
    #assignment-body-content {
        font-family: Arial, sans-serif;
        color: #888888;
        font-size: 16px;
        text-align: center;
    }
</style>
<div id="assignment-body">
    <h3>
        <fmt:message key="client.app.run.process.label.assignment.unavailable" />
    </h3>
    <div id="assignment-body-content">
        <fmt:message key="client.app.run.process.label.assignment.unavailable.explanation" />
    </div>
</div>
