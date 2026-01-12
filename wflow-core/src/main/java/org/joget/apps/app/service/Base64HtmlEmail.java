package org.joget.apps.app.service;


import java.util.Base64;
import java.util.UUID;
import javax.mail.util.ByteArrayDataSource;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.joget.commons.util.LogUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Wrapper class for HtmlEmail to automatically convert base64 image data URLs to CIDs and attach the images.
 */
public class Base64HtmlEmail extends HtmlEmail {

    @Override
    public HtmlEmail setHtmlMsg(String msg) throws EmailException {
        if (msg != null && (msg.contains("src=\"data:image") || msg.contains("src='data:image"))) {
            try {
                // Parse HTML to process base64 images
                Document doc = Jsoup.parse(msg);
                Elements images = doc.select("img[src^=data:image]");
                
                boolean modified = false;
                for (Element image : images) {
                    String src = image.attr("src");
                    if (src.startsWith("data:image")) {
                        try {
                            String cid = embedBase64Image(src);
                            image.attr("src", "cid:" + cid);
                            modified = true;
                        } catch (Exception e) {
                             LogUtil.error(Base64HtmlEmail.class.getName(), e, "Failed to embed base64 image");
                        }
                    }
                }
                
                if (modified) {
                    // Check if original message was a full HTML document
                    if (msg.toLowerCase().contains("<html")) {
                        msg = doc.outerHtml();
                    } else {
                        // Return only the body content if it was a fragment
                        msg = doc.body().html();
                    }
                }
            } catch (Exception e) {
                LogUtil.error(Base64HtmlEmail.class.getName(), e, "Failed to parse HTML for base64 images");
            }
        }
        return super.setHtmlMsg(msg);
    }

    private String embedBase64Image(String dataUrl) throws EmailException {
        try {
            String encodingPrefix = "base64,";
            int contentStartIndex = dataUrl.indexOf(encodingPrefix) + encodingPrefix.length();
            
            // Extract mimetype, e.g. "data:image/png;base64," -> "image/png"
            String mimeType = dataUrl.substring(5, dataUrl.indexOf(";"));
            String base64Data = dataUrl.substring(contentStartIndex);
            
            byte[] imageData = Base64.getDecoder().decode(base64Data);
            String cid = UUID.randomUUID().toString();
            String name = "image_" + cid; 
            
            // Use ByteArrayDataSource to create the attachment
            ByteArrayDataSource dataSource = new ByteArrayDataSource(imageData, mimeType);
            
            // embed returns the CID. We pass our own CID.
            // embed(DataSource dataSource, String name, String cid)
            return embed(dataSource, name, cid);
        } catch (Exception e) {
            throw new EmailException(e);
        }
    }
}
