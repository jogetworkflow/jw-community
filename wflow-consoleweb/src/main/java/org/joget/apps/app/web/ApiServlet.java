package org.joget.apps.app.web;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joget.apps.app.model.CustomBuilder;
import org.joget.apps.app.service.CustomBuilderUtil;
import org.joget.commons.util.HostManager;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

public class ApiServlet extends HttpServlet {
    ServletConfig config;

    @Override
    public void init(ServletConfig config) {
        this.config = config;
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }
    
    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // reset profile and set hostname
        HostManager.initHost();
        
        CustomBuilder api = CustomBuilderUtil.getBuilder("api");
        
        if (api != null) {
            Date timestamp = new Date();
            
            Map<String, Object> config = new HashMap<String, Object>();
            
            config.put("request", request);
            config.put("response", response);
            config.put("timestamp", timestamp);
            
            api.getBuilderResult(null, config);
        } else {
            response.sendError(500);
        }
    }
}
