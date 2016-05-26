package org.joget.commons.util;

import java.io.File;
import javax.servlet.http.HttpServletRequest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

public class JogetCommonsMultipartResolver extends CommonsMultipartResolver {
    
    @Override
    public MultipartHttpServletRequest resolveMultipart(final HttpServletRequest request) throws MultipartException {
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

                setUploadTempDir(new FileSystemResource(uploadTempDir));
            }
        } catch (Exception e) {
            LogUtil.error(JogetCommonsMultipartResolver.class.getName(), e, "");
        }
        
        return super.resolveMultipart(request);
    }
    
}
