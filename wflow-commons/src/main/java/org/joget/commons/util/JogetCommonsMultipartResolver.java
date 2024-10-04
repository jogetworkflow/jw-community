package org.joget.commons.util;

import java.io.File;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

public class JogetCommonsMultipartResolver extends StandardServletMultipartResolver {
    
    @Override
    public MultipartHttpServletRequest resolveMultipart(HttpServletRequest request) throws MultipartException {
        Assert.notNull(request, "Request must not be null");
        
        try {
            // reset profile and set hostname
            HostManager.initHost();

            if (HostManager.isVirtualHostEnabled()) {
                String path = SetupManager.getBaseDirectory() + File.separator + "temp";
                File uploadTempDir = new File(path);
                if (!uploadTempDir.isDirectory()) {
                    uploadTempDir.mkdir();
                }

//                setUploadTempDir(new FileSystemResource(uploadTempDir));
            }
        } catch (Exception e) {
            LogUtil.error(JogetCommonsMultipartResolver.class.getName(), e, "");
        }
        
        return super.resolveMultipart(request);
    }
    
}
