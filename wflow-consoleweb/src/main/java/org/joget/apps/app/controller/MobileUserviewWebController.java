package org.joget.apps.app.controller;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class MobileUserviewWebController {
    
    @Autowired
    WorkflowUserManager workflowUserManager;
    
    /**
     * URL for Joget Workflow Mobile app backward compatibility
     */
    @RequestMapping("/mapp/xxx/xxx")
    public void mobileCheck(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws IOException {
        String origin = request.getHeader("Origin");
        if (origin != null) {
            origin = origin.replace("\n", "").replace("\r", "");
        }
        response.setHeader("Access-Control-Allow-Origin", origin);
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }    
    
    /**
     * URL for Joget Workflow Mobile app backward compatibility
     */
    @RequestMapping({"/mobile", "/mobile/", "/mobile/apps"})
    public String mobileRunApps(HttpServletRequest request, HttpServletResponse response, ModelMap model) {
        //login error in mobile app, redirect to login page with error message
        if (workflowUserManager.isCurrentUserAnonymous()) {
            return "redirect:/web/login?login_error=1";
        }
        
        // URL for Joget Workflow Mobile app backward compatibility
        return "redirect:/";
    }    
    
}
