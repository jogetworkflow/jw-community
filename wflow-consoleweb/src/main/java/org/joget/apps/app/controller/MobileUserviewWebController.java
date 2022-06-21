package org.joget.apps.app.controller;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class MobileUserviewWebController {

    /**
     * URL for Joget Workflow Mobile app backward compatibility
     */
    @RequestMapping("/mapp/xxx/xxx")
    public void mobileCheck(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws IOException {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }    
    
    /**
     * URL for Joget Workflow Mobile app backward compatibility
     */
    @RequestMapping({"/mobile", "/mobile/", "/mobile/apps"})
    public String mobileRunApps(HttpServletRequest request, HttpServletResponse response, ModelMap model) {
        // URL for Joget Workflow Mobile app backward compatibility
        return "redirect:/";
    }    
    
}