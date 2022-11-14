package org.kecak.webapi.json.controller;

import org.joget.apps.app.service.AppService;
import org.joget.commons.util.FileStore;
import org.joget.commons.util.LogUtil;
import org.joget.directory.model.service.DirectoryManager;
import org.joget.plugin.base.PluginManager;
import org.joget.report.service.ReportManager;
import org.joget.workflow.model.service.WorkflowManager;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.joget.workflow.util.WorkflowUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.kecak.apps.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class PluginAdministrationJsonController {
    @Autowired
    private WorkflowUserManager workflowUserManager;
    @Autowired
    @Qualifier("main")
    private DirectoryManager directoryManager;
    @Autowired
    private WorkflowManager workflowManager;
    @Autowired
    private AppService appService;
    @Autowired
    private ReportManager reportManager;
    @Autowired
    private PluginManager pluginManager;

    /**
     * Upload plugin using API
     *
     * @param request
     * @param response
     * @param callback
     */
    @RequestMapping(value = "/json/plugin/upload", method = {RequestMethod.POST, RequestMethod.PUT})
    public void postPluginUpload(final HttpServletRequest request, final HttpServletResponse response, @RequestParam(value = "callback", required = false) String callback) throws JSONException, IOException {
        LogUtil.info(getClass().getName(), "Executing JSON Rest API [" + request.getRequestURI() + "] in method [" + request.getMethod() + "] as [" + WorkflowUtil.getCurrentUsername() + "]");

        try {
            MultipartFile pluginFile = FileStore.getFile("pluginFile");
            if (pluginFile == null) {
                throw new ApiException(HttpServletResponse.SC_BAD_REQUEST, "Multipart field [pluginFile] is not supplied");
            }

            pluginManager.upload(pluginFile.getOriginalFilename(), pluginFile.getInputStream());

            JSONObject responseBody = new JSONObject();
            responseBody.put("message", "success");
            response.getWriter().write(responseBody.toString());

        } catch (ApiException e){
            LogUtil.error(getClass().getName(), e, e.getMessage());
            response.sendError(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            LogUtil.error(getClass().getName(), e, e.getMessage());
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }
}
