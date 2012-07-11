<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>

<commons:popupHeader />

<style>
    #pocess-body h3 {
        margin-top: 50px;
        color: #666666;
        text-align: center;
        font-family: Arial, sans-serif;
        font-size: 24px;
        font-weight: bold;
    }
    #pocess-body-content {
        font-family: Arial, sans-serif;
        color: #888888;
        font-size: 16px;
        text-align: center;
    }
</style>
<div id="pocess-body">
    <h3>
        <fmt:message key="client.app.run.process.label.start.unauthorized" />
    </h3>
    <div id="pocess-body-content">
        <fmt:message key="client.app.run.process.label.start.unauthorized.explanation" />
    </div>
</div>

<commons:popupFooter />