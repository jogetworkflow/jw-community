<%@page contentType="image/jpeg" import="java.io.OutputStream,java.awt.image.BufferedImage,org.joget.designer.Viewer"%><%
    request.setCharacterEncoding("UTF-8");
    String xpdl = request.getParameter("xpdl");
    String packageId = request.getParameter("packageId");
    String processDefId = request.getParameter("processId");
    String[] runningActivityIds = request.getParameterValues("activityId");

    if (xpdl == null || xpdl.trim().length() == 0 || packageId == null || packageId.trim().length() == 0 || processDefId == null || processDefId.trim().length() == 0) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Process not available: parameters xpdl, packageId and processId required");
            return;
    }

    response.setContentType("image/jpeg");
    OutputStream output = response.getOutputStream();
    new Viewer().outputProcessImage(xpdl, packageId, processDefId, runningActivityIds, output);
    output.flush();
%>