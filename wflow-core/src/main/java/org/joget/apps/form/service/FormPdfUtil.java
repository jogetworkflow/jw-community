package org.joget.apps.form.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.lang.StringEscapeUtils;
import org.joget.apps.app.dao.FormDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.FormDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.model.FormData;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.StringUtil;
import com.lowagie.text.pdf.ITextCustomFontResolver;
import com.lowagie.text.pdf.ITextCustomOutputDevice;
import javax.servlet.http.HttpServletRequest;
import org.joget.commons.util.SetupManager;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.util.WorkflowUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.resource.FSEntityResolver;

/**
 * Utility class used to generate PDF file based on a form and its data
 * 
 */
public class FormPdfUtil {
    private static final int MIN_ESCAPE = 2;
    private static final int MAX_ESCAPE = 6;
    public final static String PDF_GENERATION = "_FORM_PDF_GENERATION";
    private static String DEFAULT_FONTS = "";
    
    /**
     * Gets the renderer
     * @return 
     */
    public static ITextRenderer getRenderer() {
        float dpp = 20f * 4f / 3f;
        ITextCustomOutputDevice outputDevice = new ITextCustomOutputDevice(dpp);
        ITextRenderer renderer = new ITextRenderer(dpp, 20, outputDevice);
            
        SharedContext sharedContext = renderer.getSharedContext();
        CustomITexResourceLoaderUserAgent callback = new CustomITexResourceLoaderUserAgent(renderer.getOutputDevice());
        callback.setSharedContext(sharedContext);
        sharedContext.setUserAgentCallback(callback);
        ITextCustomFontResolver resolver = new ITextCustomFontResolver(sharedContext);
        sharedContext.setFontResolver(resolver);
        DEFAULT_FONTS = resolver.getDefaultFonts();
        return renderer;
    }
    
    /**
     * Create PDF file based on form
     * @param formId
     * @param primaryKey
     * @param appDef
     * @param assignment
     * @param hideEmpty
     * @param header
     * @param footer
     * @param css
     * @param showAllSelectOptions
     * @param repeatHeader
     * @param repeatFooter
     * @return 
     */
    public static byte[] createPdf(String formId, String primaryKey, AppDefinition appDef, WorkflowAssignment assignment, Boolean hideEmpty, String header, String footer, String css, Boolean showAllSelectOptions, Boolean repeatHeader, Boolean repeatFooter) {
        try {
            String html = getSelectedFormHtml(formId, primaryKey, appDef, assignment, hideEmpty);
            
            header = AppUtil.processHashVariable(header, assignment, null, null);
            footer = AppUtil.processHashVariable(footer, assignment, null, null);
            
            return createPdf(html, header, footer, css, showAllSelectOptions, repeatHeader, repeatFooter);
        } catch (Exception e) {
            LogUtil.error(FormPdfUtil.class.getName(), e, "");
        }
        return null;
    }
    
    /**
     * Create PDF file based on Form HTML
     * @param html
     * @param header
     * @param footer
     * @param css
     * @param showAllSelectOptions
     * @param repeatHeader
     * @param repeatFooter
     * @return 
     */
    public static byte[] createPdf(String html, String header, String footer, String css, Boolean showAllSelectOptions, Boolean repeatHeader, Boolean repeatFooter) {
        return createPdf(html, header, footer, css, showAllSelectOptions, repeatHeader, repeatFooter, true);
    }
    
    /**
     * Create PDF file based on Form HTML
     * @param html
     * @param header
     * @param footer
     * @param css
     * @param showAllSelectOptions
     * @param repeatHeader
     * @param repeatFooter
     * @param cleanForm
     * @return 
     */
    public static byte[] createPdf(String html, String header, String footer, String css, Boolean showAllSelectOptions, Boolean repeatHeader, Boolean repeatFooter, Boolean cleanForm) {
        try {
            ITextRenderer r = getRenderer();
            synchronized (r) {
                html = formatHtml(html, header, footer, css, showAllSelectOptions, repeatHeader, repeatFooter, cleanForm);

                // XML 1.0
                // #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]
                String pattern = "[^"
                        + "\u0009\r\n"
                        + "\u0020-\uD7FF"
                        + "\uE000-\uFFFD"
                        + "\ud800\udc00-\udbff\udfff"
                        + "]";
                
                String legalHtml = html.replaceAll(pattern, "");
                legalHtml = toXHTML(legalHtml);
            
                final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                documentBuilderFactory.setValidating(false);
                DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
                builder.setEntityResolver(FSEntityResolver.instance());
                org.w3c.dom.Document xmlDoc = builder.parse(new ByteArrayInputStream(legalHtml.getBytes("UTF-8")));

                r.setDocument(xmlDoc, null);

                ByteArrayOutputStream os = new ByteArrayOutputStream();
                r.layout();
                r.createPDF(os);
                byte[] output = os.toByteArray();
                os.close();
                return output;
            }
        } catch (Exception e) {
            LogUtil.error(FormPdfUtil.class.getName(), e, "");
        }
        return null;
    }
    
    public static String toXHTML(String html) {
        final Document document = Jsoup.parse(html);
        document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);    
        return document.html();
    }
    
    /**
     * Get the HTML of a form
     * @param formId
     * @param primaryKey
     * @param appDef
     * @param assignment
     * @param hideEmpty
     * @return 
     */
    public static String getSelectedFormHtml(String formId, String primaryKey, AppDefinition appDef, WorkflowAssignment assignment, Boolean hideEmpty) {
        String html = "";

        AppService appService = (AppService) AppUtil.getApplicationContext().getBean("appService");
        FormData formData = new FormData();
        formData.addFormResult(PDF_GENERATION, "");
        if (primaryKey != null && !primaryKey.isEmpty()) {
            formData.setPrimaryKeyValue(primaryKey);
        } else if (assignment != null) {
            formData.setPrimaryKeyValue(appService.getOriginProcessId(assignment.getProcessId()));
        }
        if (assignment != null) {
            formData.setAssignment(assignment);
            formData.setProcessId(assignment.getProcessId());
        } else if (primaryKey != null && !primaryKey.isEmpty()) {
            //create an mock assignment for hash variable 
            assignment = new WorkflowAssignment();
            assignment.setProcessId(primaryKey);
        }
        
        Form form = null;
        FormDefinitionDao formDefinitionDao = (FormDefinitionDao) AppUtil.getApplicationContext().getBean("formDefinitionDao");
        FormService formService = (FormService) AppUtil.getApplicationContext().getBean("formService");
        FormDefinition formDef = formDefinitionDao.loadById(formId, appDef);
        String formJson = formDef.getJson();

        if (formJson != null) {
            formJson = AppUtil.processHashVariable(formJson, assignment, StringUtil.TYPE_JSON, null, appDef);
            form = (Form) formService.loadFormFromJson(formJson, formData);
            
            if (form != null) {
                //set form to readonly
                    FormUtil.setReadOnlyProperty(form, true, false);

                if (hideEmpty != null && hideEmpty) {
                    form = (Form) removeEmptyValueChild(form, form, formData);
                }

                if (form != null) {
                    html = formService.retrieveFormHtml(form, formData);
                }
            }
        }
        
        return html;
    }
    
    /**
     * Prepare the HTML for PDF generation
     * @param html
     * @param header
     * @param footer
     * @param css
     * @param showAllSelectOptions
     * @param repeatHeader
     * @param repeatFooter
     * @return 
     */
    public static String formatHtml(String html, String header, String footer, String css, Boolean showAllSelectOptions, Boolean repeatHeader, Boolean repeatFooter) {
        return formatHtml(html, header, footer, css, showAllSelectOptions, repeatHeader, repeatFooter, true);
    }
    
    /**
     * Prepare the HTML for PDF generation
     * @param html
     * @param header
     * @param footer
     * @param css
     * @param showAllSelectOptions
     * @param repeatHeader
     * @param repeatFooter
     * @param cleanForm
     * @return 
     */
    public static String formatHtml(String html, String header, String footer, String css, Boolean showAllSelectOptions, Boolean repeatHeader, Boolean repeatFooter, Boolean cleanForm) {
        //taking care special characters
        html = convertSpecialHtmlEntity(html);
        
        //remove script
        html = html.replaceAll("(?s)<script[^>]*>.*?</script>", "");

        //remove style
        html = html.replaceAll("(?s)<style[^>]*>.*?</style>", "");
        
        if (cleanForm != null && cleanForm) {
            html = cleanFormHtml(html, showAllSelectOptions);
        }
        
        String englishFont = "\"Times\",";
        SetupManager setupManager = (SetupManager) AppUtil.getApplicationContext().getBean("setupManager");
        String locale = setupManager.getSettingValue("systemLocale");
        if (locale != null && (locale.startsWith("zh") || locale.startsWith("hu") 
                || locale.startsWith("ar") || locale.startsWith("th") 
                || locale.startsWith("ja") || locale.startsWith("ko"))) {
            englishFont = "";
        }
        
        //append style
        String style = "<style type='text/css'>";
        style += "*{font-size:12px;font-family:"+englishFont+DEFAULT_FONTS+";}";
        style += formPdfCss();
        style += ".quickEdit{display:none;}";
        style += ".pdf_visible{display:block !important; height: auto !important; width: 100% !important;}";
        style += ".pdf_hidden{display:none !important;}";
        
        if (repeatHeader != null && repeatHeader) {
            style += "div.header{display: block;position: running(header);}";
            style += "@page { @top-center { content: element(header) }}";
        }
        if (repeatFooter != null && repeatFooter) {    
            style += "div.footer{display: block;position: running(footer);}";
            style += "@page { @bottom-center { content: element(footer) }}";
        }
        
        if (css != null && !css.isEmpty()) {
            style += css;
        }
        
        style += "</style>";
        
        String headerHtml = ""; 
        if (header != null && !header.isEmpty()) {
            headerHtml = "<div class=\"header\">" + header + "</div>";
        }
        
        String footerHtml = ""; 
        if (footer != null && !footer.isEmpty()) {
            footerHtml = "<div class=\"footer\">" + footer + "</div>";
        }
        
        String htmlMeta = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        htmlMeta += "<!DOCTYPE html>";
        htmlMeta += "<html>";
        htmlMeta += "<head>";
        htmlMeta += "<meta http-equiv=\"content-type\" content=\"application/xhtml+xml; charset=UTF-8\" />";
        htmlMeta += style + "</head><body>";
                
        if (repeatFooter != null && repeatFooter) { 
            html = htmlMeta + headerHtml + footerHtml + html;
        } else {
            html = htmlMeta + headerHtml + html + footerHtml;
        }
        
        html += "</body></html>";
        
        return html;
    }
    
    /**
     * Get the styles of form in PDF
     * 
     * @return 
     */
    public static String formPdfCss() {
        String style = "";
        style += ".form-section, .subform-section {position: relative;overflow: hidden;margin-bottom: 10px;}";
        style += ".form-section-title span, .subform-section-title span {padding: 10px;margin-bottom: 10px;font-weight: bold;font-size: 16px;background: #efefef;display: block;}";
        style += ".form-column, .subform-column {position: relative;float: left;min-height: 20px;}";
        style += ".form-cell, .subform-cell {position: relative;min-height: 15px;color: black;clear: left;padding:3px 0px;}";
        style += ".form-cell > .label, .subform-cell > .label {width: 40%;display: block;float: left;font-weight:bold;}";
        style += "table {clear:both;}";
        style += "p {margin:5px 0;}";
        style += ".form-cell table td, .form-cell table th, .subform-cell table td, .subform-cell table th {border: solid 1px silver;padding: 3px;margin: 0px;}";
        style += ".subform-container{ border: 5px solid #dfdfdf;padding: 3px;margin-top:5px;}";
        style += ".subform-container, .subform-section {background: #efefef;}";
        style += ".subform-title{background: #efefef;position:relative;top:-12px;}";
        style += ".form-fileupload {float: left;}";
        style += ".form-cell-value, .subform-cell-value {float: left;width: 60%;}";
        style += ".form-cell-value.form-cell-full, .subform-cell-value.subform-cell-full {width: 100%;}";
        style += ".form-cell-value label, .subform-cell-value label {display: block;float: left;width: 50%;}";
        style += "ul.form-cell-value, ul.subform-cell-value {padding:0; list-style-type:none;}";
        style += "th.grid-action-header, td.grid-action-cell { display: none !important;}";
        style += ".subform-container.no-frame{border: 0; padding: 0; margin-top:10px; }";
        style += ".subform-container.no-frame, .subform-container.no-frame .subform-section { background: transparent;}";
        style += ".richtexteditor { float:left;}";
        return style;
    }
    
    /**
     * Clean the form HTML for PDF generation
     * 
     * @param html
     * @param showAllSelectOptions
     * @return 
     */
    public static String cleanFormHtml(String html, Boolean showAllSelectOptions) {
        HttpServletRequest request = WorkflowUtil.getHttpServletRequest();
        //remove hidden field
        html = html.replaceAll("<input[^>]*type=\"hidden\"[^>]*>", "");
        html = html.replaceAll("<input[^>]*type=\'hidden\'[^>]*>", "");
        
        //remove <br>
        html = html.replaceAll("<br>", "<br/>");
        
        //remove form tag
        html = html.replaceAll("<form[^>]*>", "");
        html = html.replaceAll("</\\s?form>", "");

        //remove button
        html = html.replaceAll("<button[^>]*>[^>]*</\\s?button>>", "");

        //remove validator decorator
        html = html.replaceAll("<span\\s?class=\"[^\"]*cell-validator[^\"]?\"[^>]*>[^>]*</\\s?span>", "");

        //remove link
        html = html.replaceAll("<link[^>]*>", "");

        //remove id
        html = html.replaceAll("id=\"([^\\\"]*)\"", "");
        
        //remove hidden td
        html = html.replaceAll("<td\\s?style=\\\"[^\\\"]*display:none;[^\\\"]?\\\"[^>]*>.*?</\\s?td>", "");
        
        //convert Image if image doesnt end with />
        Pattern ImagePattern = Pattern.compile("<img[^>]*[^\\/]>");
        Matcher ImageMatcher = ImagePattern.matcher(html);
        while (ImageMatcher.find()) {
            String image = ImageMatcher.group(0);
            String replace = image.replaceAll(StringUtil.escapeRegex(">"), StringUtil.escapeRegex("/>"));
            html = html.replaceAll(StringUtil.escapeRegex(image), StringUtil.escapeRegex(replace));
        }
        
        //convert label for checkbox and radio
        Pattern formdiv = Pattern.compile("<div class=\"form-cell-value\" >.*?</div>", Pattern.DOTALL);
        Matcher divMatcher = formdiv.matcher(html);
        while (divMatcher.find()) {
            String divString = divMatcher.group(0);

            Pattern tempPatternLabel = Pattern.compile("<label.*?>.*?</label>", Pattern.DOTALL);
            Matcher tempMatcherLabel = tempPatternLabel.matcher(divString);
            int count = 0;
            String inputStringLabel = "";
            String replaceLabel = "";
            while (tempMatcherLabel.find()) {

                inputStringLabel = tempMatcherLabel.group(0);
                //get the input field
                Pattern patternInput = Pattern.compile("<input[^>]*>");
                Matcher matcherInput = patternInput.matcher(inputStringLabel);
                String tempLabel = "";
                if (matcherInput.find()) {
                    tempLabel = matcherInput.group(0);
                }

                //get the type
                Pattern patternType = Pattern.compile("type=\"([^\\\"]*)\"");
                Matcher matcherType = patternType.matcher(tempLabel);
                String type = "";
                if (matcherType.find()) {
                    type = matcherType.group(1);
                }

                if (type.equalsIgnoreCase("checkbox") || type.equalsIgnoreCase("radio")) {
                    if (showAllSelectOptions != null && showAllSelectOptions) {
                        replaceLabel += inputStringLabel.replaceAll("<label(.*?)>", "");
                        replaceLabel = replaceLabel.replaceAll("</label(.*?)>", "");
                    } else {
                        if (inputStringLabel.contains("checked")) {
                            if (count > 0) {
                                replaceLabel += ", ";
                            }
                            String label = "";
                            Pattern patternLabel = Pattern.compile("</i>.*?</label>", Pattern.DOTALL);
                            Matcher matcherLabel = patternLabel.matcher(inputStringLabel);
                            if (matcherLabel.find()) {
                                label = matcherLabel.group(0);
                                label = label.replaceAll("<(.*?)i>", "");
                                label = label.replaceAll("</label(.*?)>", "");
                                label = label.trim();
                            }
                            replaceLabel += label;
                            count += 1;
                        }
                    }
                } else {
                    if (count > 0) {
                        replaceLabel += ", ";
                    }
                    String span = "";
                    Pattern patternSpan = Pattern.compile("<span(.*?)>(.|\\s)*?</span>");
                    Matcher matcherSpan = patternSpan.matcher(inputStringLabel);
                    if (matcherSpan.find()) {
                        span = matcherSpan.group(0);
                        span = span.replaceAll("<(.*?)span>", "");
                        span = span.replaceAll("</span(.*?)>", "");
                        span = span.trim();
                    }
                    replaceLabel += span;
                    count += 1;
                }
            }
            if (count > 0) {
                replaceLabel = "<span>" + replaceLabel + "</span>";
            }
            html = html.replaceAll(StringUtil.escapeRegex(divString), StringUtil.escapeRegex(replaceLabel));
        }
        
        //convert input field
        Pattern pattern = Pattern.compile("<input[^>]*>");
        Matcher matcher = pattern.matcher(html);
        while (matcher.find()) {
            String inputString = matcher.group(0);

            //get the type
            Pattern patternType = Pattern.compile("type=\"([^\\\"]*)\"");
            Matcher matcherType = patternType.matcher(inputString);
            String type = "";
            if (matcherType.find()) {
                type = matcherType.group(1);
            }

            //get the value
            Pattern patternValue = Pattern.compile("value=\"([^\\\"]*)\"");
            Matcher matcherValue = patternValue.matcher(inputString);
            String replace;
            String value = "";
            if (matcherValue.find()) {
                value = matcherValue.group(1);
            }

            if (type.equalsIgnoreCase("text")) {
                html = html.replaceAll(StringUtil.escapeRegex(inputString), "<span>" + StringUtil.escapeRegex(value) + "</span>");
            } else if (type.equalsIgnoreCase("file") || type.equalsIgnoreCase("button") || type.equalsIgnoreCase("submit") || type.equalsIgnoreCase("reset") || type.equalsIgnoreCase("image")) {
                html = html.replaceAll(StringUtil.escapeRegex(inputString), "");
            } else if (type.equalsIgnoreCase("checkbox") || type.equalsIgnoreCase("radio")) {
                if (showAllSelectOptions != null && showAllSelectOptions) {
                    if (inputString.contains("checked")) {
                        if (request != null) {
                            replace = "<img style=\"padding-left:20px\" alt=\"\" src=\"" + request.getContextPath() + "/plugin/org.joget.apps.app.lib.BeanShellTool/images/black_tick.png\"/>";
                        }else{
                            replace = "<img style=\"padding-left:20px\" alt=\"\" src=\"" + getResourceURL("/images/black_tick.png") + "\"/>";
                        }
                    } else {
                        if (request != null) {
                            replace = "<img style=\"padding-left:20px\" alt=\"\" src=\"" + request.getContextPath() + "/plugin/org.joget.apps.app.lib.BeanShellTool/images/black_tick_n.png\"/>";
                        }else{
                            replace = "<img style=\"padding-left:20px\" alt=\"\" src=\"" + getResourceURL("/images/black_tick_n.png") + "\"/>";
                        }
                    }
                    html = html.replaceAll(StringUtil.escapeRegex(inputString), StringUtil.escapeRegex(replace));
                } else {
                    html = html.replaceAll(StringUtil.escapeRegex(inputString), StringUtil.escapeRegex(""));
                }
            } else if (type.equalsIgnoreCase("password")) {
                html = html.replaceAll(StringUtil.escapeRegex(inputString), "<span>**********</span>");
            }
        }

        //convert selectbox
        Pattern patternSelect = Pattern.compile("<select[^>]*>.*?</select>", Pattern.DOTALL);
        Matcher matcherSelect = patternSelect.matcher(html);
        while (matcherSelect.find()) {
            String selectString = matcherSelect.group(0);
            String replace = ""; 
            int counter = 0;

            //get the type
            Pattern patternOption = Pattern.compile("<option[^>]*>(.*?)</option>");
            Matcher matcherOption = patternOption.matcher(selectString);
            while (matcherOption.find()) {
                String optionString = matcherOption.group(0);
                String label = matcherOption.group(1);
                if (showAllSelectOptions != null && showAllSelectOptions) {
                    if (optionString.contains("selected")) {
                        if (request != null) {
                            replace += "<img style=\"padding-left:20px\" alt=\"\" src=\"" + request.getContextPath() + "/plugin/org.joget.apps.app.lib.BeanShellTool/images/black_tick.png\"/>";
                        } else {
                            replace += "<img style=\"padding-left:20px\" alt=\"\" src=\"" + getResourceURL("/images/black_tick.png") + "\"/>";
                        }
                    } else {
                        if (request != null) {
                            replace += "<img style=\"padding-left:20px\" alt=\"\" src=\"" + request.getContextPath() + "/plugin/org.joget.apps.app.lib.BeanShellTool/images/black_tick_n.png\"/>";
                        } else {
                            replace += "<img style=\"padding-left:20px\" alt=\"\" src=\"" + getResourceURL("/images/black_tick_n.png") + "\"/>";
                        }
                    }
                    replace += label;
                } else {
                    if (optionString.contains("selected")) {
                        if (counter > 0) {
                            replace += ", ";
                        }
                        replace += label;
                        counter += 1;
                    }
                }
            }

            if (counter > 0) {
                replace = "<span>" + replace + "</span>";
            }
            html = html.replaceAll(StringUtil.escapeRegex(selectString), StringUtil.escapeRegex(replace));
        }

        //convert textarea
        Pattern patternTextarea = Pattern.compile("<textarea[^>]*>.*?</textarea>", Pattern.DOTALL);
        Matcher matcherTextarea = patternTextarea.matcher(html);
        while (matcherTextarea.find()) {
            String textareaString = matcherTextarea.group(0);
            String replace = textareaString;
            replace = replace.replaceAll("<textarea[^>]*>", "");
            replace = replace.replaceAll("</textarea>", "");
            replace = replace.replaceAll("&lt;", "<");
            replace = replace.replaceAll("&gt;", ">");
            replace = replace.replaceAll("&nbsp;", " ");
            replace = replace.replaceAll("&quot;", "\"");
            replace = replace.replaceAll(StringUtil.escapeRegex("&nbsp;"), " ");

            if (!replace.contains("<p")) {
                String[] newline = replace.split("\\\n");
                if (newline.length > 1) {
                    replace = "";
                    for (String n : newline) {
                        if (!n.isEmpty()) {
                            String r = n.replaceAll("\\\n", "");
                            replace += "<p>" + r + "</p>";
                        }
                    }
                }
            }

            replace = "<div style=\"float:left;\">" + replace + "</div>";

            html = html.replaceAll(StringUtil.escapeRegex(textareaString), StringUtil.escapeRegex(replace));
        }
        
        //remove &nbsp;
        html = html.replaceAll(StringUtil.escapeRegex("&nbsp;"), " ");
        
        //escape special character in html
        html = escapeSpecialCharacter(html, "label");
        html = escapeSpecialCharacter(html, "span");
        html = escapeSpecialCharacter(html, "th");
        html = escapeSpecialCharacter(html, "td");
        html = escapeSpecialCharacter(html, "p");
        
        //remove br
        html = html.replaceAll("</\\s?br>", "");

        return html;
    }
    
    protected static String escapeSpecialCharacter(String html, String tag) {
        //convert label
        Pattern pattern = Pattern.compile("<"+tag+"[^>]*>(.*?)</"+tag+">", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(html);
        while (matcher.find()) {
            String tagHtml = matcher.group(0);
            String text = matcher.group(1);
            if (text.contains("&") && !text.contains("&amp;") && !text.contains("&lt;") && !text.contains("&gt;") && !text.contains("&quot;") && !text.contains("&#")) {
                String replace = StringEscapeUtils.escapeXml(text);
                replace = tagHtml.replaceAll(StringUtil.escapeRegex(text), StringUtil.escapeRegex(replace));
                html = html.replaceAll(StringUtil.escapeRegex(tagHtml), StringUtil.escapeRegex(replace));
            }
        }
        return html;
    }
    
    /**
     * Removed the field elements which has empty value from the form
     * @param form
     * @param element
     * @param formData
     * @return 
     */
    public static Element  removeEmptyValueChild(Form form, Element element, FormData formData) {
        Collection<Element> childs = element.getChildren();
        if (childs != null && childs.size() > 0) {
            for (Iterator<Element> it = childs.iterator(); it.hasNext();) {
                Element c = it.next();
                if (removeEmptyValueChild(form, c, formData) == null) {
                    it.remove();
                } 
            }
            
            if (childs.isEmpty()) {
                return null;
            }
        } else {
            if (element.getLoadBinder() != null) {
                if (formData.getLoadBinderData(element).isEmpty()) {
                    return null;
                }
            } else {
                String value = FormUtil.getElementPropertyValue(element, formData);
                if (value == null || value.isEmpty()) {
                    return null;
                }
            }
        }
        
        return element;
    } 
    
    /** 
     * Gets the full URL of a resource
     * @param resourceUrl
     * @return 
     */
    public static URL getResourceURL(String resourceUrl) {
        URL url = null;

        url = FormPdfUtil.class.getResource(resourceUrl);
        
        return url;
    }
    
    public static final String convertSpecialHtmlEntity(final String input) {
        StringWriter writer = null;
        int len = input.length();
        int i = 1;
        int st = 0;
        while (true) {
            // look for '&'
            while (i < len && input.charAt(i-1) != '&')
                i++;
            if (i >= len)
                break;

            // found '&', look for ';'
            int j = i;
            while (j < len && j < i + MAX_ESCAPE + 1 && input.charAt(j) != ';')
                j++;
            if (j == len || j < i + MIN_ESCAPE || j == i + MAX_ESCAPE + 1) {
                i++;
                continue;
            }

            // found escape 
            if (input.charAt(i) == '#') {
                // numeric escape
                int k = i + 1;
                int radix = 10;

                final char firstChar = input.charAt(k);
                if (firstChar == 'x' || firstChar == 'X') {
                    k++;
                    radix = 16;
                }

                try {
                    int entityValue = Integer.parseInt(input.substring(k, j), radix);

                    if (writer == null) 
                        writer = new StringWriter(input.length());
                    writer.append(input.substring(st, i - 1));

                    if (entityValue > 0xFFFF) {
                        final char[] chrs = Character.toChars(entityValue);
                        writer.write(chrs[0]);
                        writer.write(chrs[1]);
                    } else {
                        writer.write(entityValue);
                    }

                } catch (NumberFormatException ex) { 
                    i++;
                    continue;
                }
            }
            else {
                // named escape
                CharSequence value = lookupMap.get(input.substring(i, j));
                if (value == null) {
                    i++;
                    continue;
                }

                if (writer == null) 
                    writer = new StringWriter(input.length());
                writer.append(input.substring(st, i - 1));

                writer.append(value);
            }

            // skip escape
            st = j + 1;
            i = st;
        }

        if (writer != null) {
            writer.append(input.substring(st, len));
            return writer.toString();
        }
        return input;
    }

    private static final String[][] ESCAPES = {
        // Mapping to escape ISO-8859-1 characters to their named HTML 3.x equivalents.
        {"\u0027", "apos"}, //	apostrophe (apostrophe-quote); see below
        {"\u00A1", "iexcl"}, //¡	inverted exclamation mark
        {"\u00A2", "cent"}, //¢	cent sign
        {"\u00A3", "pound"}, //£	pound sign
        {"\u00A4", "curren"}, //¤	currency sign
        {"\u00A5", "yen"}, //¥	yen sign
        {"\u00A6", "brvbar"}, //¦	broken bar
        {"\u00A7", "sect"}, //§	section sign
        {"\u00A8", "uml"}, //¨	diaeresis (spacing diaeresis); see Germanic umlaut
        {"\u00A9", "copy"}, //©	copyright symbol
        {"\u00AA", "ordf"}, //ª	feminine ordinal indicator
        {"\u00AB", "laquo"}, //«	left-pointing double angle quotation mark
        {"\u00AC", "not"}, //¬	not sign
        {"\u00AD", "shy"}, //	soft hyphen
        {"\u00AE", "reg"}, //®	registered sign
        {"\u00AF", "macr"}, //¯	macron
        {"\u00B0", "deg"}, //°	degree symbol
        {"\u00B1", "plusmn"}, //±	plus-minus sign
        {"\u00B2", "sup2"}, //²	superscript two
        {"\u00B3", "sup3"}, //³	superscript three
        {"\u00B4", "acute"}, //´	acute accent
        {"\u00B5", "micro"}, //µ	micro sign
        {"\u00B6", "para"}, //¶	pilcrow sign 
        {"\u00B7", "middot"}, //·	middle dot 
        {"\u00B8", "cedil"}, //¸	cedilla 
        {"\u00B9", "sup1"}, //¹	superscript one 
        {"\u00BA", "ordm"}, //º	masculine ordinal indicator
        {"\u00BB", "raquo"}, //»	right-pointing double angle quotation mark 
        {"\u00BC", "frac14"}, //¼	vulgar fraction one quarter 
        {"\u00BD", "frac12"}, //½	vulgar fraction one half 
        {"\u00BE", "frac34"}, //¾	vulgar fraction three quarters 
        {"\u00BF", "iquest"}, //¿	inverted question mark 
        {"\u00C0", "Agrave"}, //À	Latin capital letter A with grave accent 
        {"\u00C1", "Aacute"}, //Á	Latin capital letter A with acute accent
        {"\u00C2", "Acirc"}, //Â	Latin capital letter A with circumflex
        {"\u00C3", "Atilde"}, //Ã	Latin capital letter A with tilde
        {"\u00C4", "Auml"}, //Ä	Latin capital letter A with diaeresis
        {"\u00C5", "Aring"}, //Å	Latin capital letter A with ring above 
        {"\u00C6", "AElig"}, //Æ	Latin capital letter AE 
        {"\u00C7", "Ccedil"}, //Ç	Latin capital letter C with cedilla
        {"\u00C8", "Egrave"}, //È	Latin capital letter E with grave accent
        {"\u00C9", "Eacute"}, //É	Latin capital letter E with acute accent
        {"\u00CA", "Ecirc"}, //Ê	Latin capital letter E with circumflex
        {"\u00CB", "Euml"}, //Ë	Latin capital letter E with diaeresis
        {"\u00CC", "Igrave"}, //Ì	Latin capital letter I with grave accent
        {"\u00CD", "Iacute"}, //Í	Latin capital letter I with acute accent
        {"\u00CE", "Icirc"}, //Î	Latin capital letter I with circumflex
        {"\u00CF", "Iuml"}, //Ï	Latin capital letter I with diaeresis
        {"\u00D0", "ETH"}, //Ð	Latin capital letter Eth
        {"\u00D1", "Ntilde"}, //Ñ	Latin capital letter N with tilde
        {"\u00D2", "Ograve"}, //Ò	Latin capital letter O with grave accent
        {"\u00D3", "Oacute"}, //Ó	Latin capital letter O with acute accent
        {"\u00D4", "Ocirc"}, //Ô	Latin capital letter O with circumflex
        {"\u00D5", "Otilde"}, //Õ	Latin capital letter O with tilde
        {"\u00D6", "Ouml"}, //Ö	Latin capital letter O with diaeresis
        {"\u00D7", "times"}, //×	multiplication sign
        {"\u00D8", "Oslash"}, //Ø	Latin capital letter O with stroke 
        {"\u00D9", "Ugrave"}, //Ù	Latin capital letter U with grave accent
        {"\u00DA", "Uacute"}, //Ú	Latin capital letter U with acute accent
        {"\u00DB", "Ucirc"}, //Û	Latin capital letter U with circumflex
        {"\u00DC", "Uuml"}, //Ü	Latin capital letter U with diaeresis
        {"\u00DD", "Yacute"}, //Ý	Latin capital letter Y with acute accent
        {"\u00DE", "THORN"}, //Þ	Latin capital letter THORN
        {"\u00DF", "szlig"}, //ß	Latin small letter sharp s ; see German Eszett
        {"\u00E0", "agrave"}, //à	Latin small letter a with grave accent
        {"\u00E1", "aacute"}, //á	Latin small letter a with acute accent
        {"\u00E2", "acirc"}, //â	Latin small letter a with circumflex
        {"\u00E3", "atilde"}, //ã	Latin small letter a with tilde
        {"\u00E4", "auml"}, //ä	Latin small letter a with diaeresis
        {"\u00E5", "aring"}, //å	Latin small letter a with ring above
        {"\u00E6", "aelig"}, //æ	Latin small letter ae 
        {"\u00E7", "ccedil"}, //ç	Latin small letter c with cedilla
        {"\u00E8", "egrave"}, //è	Latin small letter e with grave accent
        {"\u00E9", "eacute"}, //é	Latin small letter e with acute accent
        {"\u00EA", "ecirc"}, //ê	Latin small letter e with circumflex
        {"\u00EB", "euml"}, //ë	Latin small letter e with diaeresis
        {"\u00EC", "igrave"}, //ì	Latin small letter i with grave accent
        {"\u00ED", "iacute"}, //í	Latin small letter i with acute accent
        {"\u00EE", "icirc"}, //î	Latin small letter i with circumflex
        {"\u00EF", "iuml"}, //ï	Latin small letter i with diaeresis
        {"\u00F0", "eth"}, //ð	Latin small letter eth
        {"\u00F1", "ntilde"}, //ñ	Latin small letter n with tilde
        {"\u00F2", "ograve"}, //ò	Latin small letter o with grave accent
        {"\u00F3", "oacute"}, //ó	Latin small letter o with acute accent
        {"\u00F4", "ocirc"}, //ô	Latin small letter o with circumflex
        {"\u00F5", "otilde"}, //õ	Latin small letter o with tilde
        {"\u00F6", "ouml"}, //ö	Latin small letter o with diaeresis
        {"\u00F7", "divide"}, //÷	division sign 
        {"\u00F8", "oslash"}, //ø	Latin small letter o with stroke 
        {"\u00F9", "ugrave"}, //ù	Latin small letter u with grave accent
        {"\u00FA", "uacute"}, //ú	Latin small letter u with acute accent
        {"\u00FB", "ucirc"}, //û	Latin small letter u with circumflex
        {"\u00FC", "uuml"}, //ü	Latin small letter u with diaeresis
        {"\u00FD", "yacute"}, //ý	Latin small letter y with acute accent
        {"\u00FE", "thorn"}, //þ	Latin small letter thorn
        {"\u00FF", "yuml"}, //ÿ	Latin small letter y with diaeresis
        {"\u0152", "OElig"}, //Œ	Latin capital ligature oe[e]
        {"\u0153", "oelig"}, //œ	Latin small ligature oe[e]
        {"\u0160", "Scaron"}, //Š	Latin capital letter s with caron
        {"\u0161", "scaron"}, //š	Latin small letter s with caron
        {"\u0178", "Yuml"}, //Ÿ	Latin capital letter y with diaeresis
        {"\u0192", "fnof"}, //ƒ	Latin small letter f with hook 
        {"\u02C6", "circ"}, //ˆ	modifier letter circumflex accent
        {"\u02DC", "tilde"}, //˜	small tilde
        {"\u0391", "Alpha"}, //Α	Greek capital letter Alpha
        {"\u0392", "Beta"}, //Β	Greek capital letter Beta
        {"\u0393", "Gamma"}, //Γ	Greek capital letter Gamma
        {"\u0394", "Delta"}, //Δ	Greek capital letter Delta
        {"\u0395", "Epsilon"}, //Ε	Greek capital letter Epsilon
        {"\u0396", "Zeta"}, //Ζ	Greek capital letter Zeta
        {"\u0397", "Eta"}, //Η	Greek capital letter Eta
        {"\u0398", "Theta"}, //Θ	Greek capital letter Theta
        {"\u0399", "Iota"}, //Ι	Greek capital letter Iota
        {"\u039A", "Kappa"}, //Κ	Greek capital letter Kappa
        {"\u039B", "Lambda"}, //Λ	Greek capital letter Lambda
        {"\u039C", "Mu"}, //Μ	Greek capital letter Mu
        {"\u039D", "Nu"}, //Ν	Greek capital letter Nu
        {"\u039E", "Xi"}, //Ξ	Greek capital letter Xi
        {"\u039F", "Omicron"}, //Ο	Greek capital letter Omicron
        {"\u03A0", "Pi"}, //Π	Greek capital letter Pi
        {"\u03A1", "Rho"}, //Ρ	Greek capital letter Rho
        {"\u03A3", "Sigma"}, //Σ	Greek capital letter Sigma
        {"\u03A4", "Tau"}, //Τ	Greek capital letter Tau
        {"\u03A5", "Upsilon"}, //Υ	Greek capital letter Upsilon
        {"\u03A6", "Phi"}, //Φ	Greek capital letter Phi
        {"\u03A7", "Chi"}, //Χ	Greek capital letter Chi
        {"\u03A8", "Psi"}, //Ψ	Greek capital letter Psi
        {"\u03A9", "Omega"}, //Ω	Greek capital letter Omega
        {"\u03B1", "alpha"}, //α	Greek small letter alpha
        {"\u03B2", "beta"}, //β	Greek small letter beta
        {"\u03B3", "gamma"}, //γ	Greek small letter gamma
        {"\u03B4", "delta"}, //δ	Greek small letter delta
        {"\u03B5", "epsilon"}, //ε	Greek small letter epsilon
        {"\u03B6", "zeta"}, //ζ	Greek small letter zeta
        {"\u03B7", "eta"}, //η	Greek small letter eta
        {"\u03B8", "theta"}, //θ	Greek small letter theta
        {"\u03B9", "iota"}, //ι	Greek small letter iota
        {"\u03BA", "kappa"}, //κ	Greek small letter kappa
        {"\u03BB", "lambda"}, //λ	Greek small letter lambda
        {"\u03BC", "mu"}, //μ	Greek small letter mu
        {"\u03BD", "nu"}, //ν	Greek small letter nu
        {"\u03BE", "xi"}, //ξ	Greek small letter xi
        {"\u03BF", "omicron"}, //ο	Greek small letter omicron
        {"\u03C0", "pi"}, //π	Greek small letter pi
        {"\u03C1", "rho"}, //ρ	Greek small letter rho
        {"\u03C2", "sigmaf"}, //ς	Greek small letter final sigma
        {"\u03C3", "sigma"}, //σ	Greek small letter sigma
        {"\u03C4", "tau"}, //τ	Greek small letter tau
        {"\u03C5", "upsilon"}, //υ	Greek small letter upsilon
        {"\u03C6", "phi"}, //φ	Greek small letter phi
        {"\u03C7", "chi"}, //χ	Greek small letter chi
        {"\u03C8", "psi"}, //ψ	Greek small letter psi
        {"\u03C9", "omega"}, //ω	Greek small letter omega
        {"\u03D1", "thetasym"}, //ϑ	Greek theta symbol
        {"\u03D2", "upsih"}, //ϒ	Greek Upsilon with hook symbol
        {"\u03D6", "piv"}, //ϖ	Greek pi symbol
        {"\u2002", "ensp"}, //	en space[d]
        {"\u2003", "emsp"}, //	em space[d]
        {"\u2009", "thinsp"}, //	thin space[d]
        {"\u200C", "zwnj"}, //	zero-width non-joiner
        {"\u200D", "zwj"}, //	zero-width joiner
        {"\u200E", "lrm"}, //	left-to-right mark
        {"\u200F", "rlm"}, //	right-to-left mark
        {"\u2013", "ndash"}, //–	en dash
        {"\u2014", "mdash"}, //—	em dash
        {"\u2018", "lsquo"}, //‘	left single quotation mark
        {"\u2019", "rsquo"}, //’	right single quotation mark
        {"\u201A", "sbquo"}, //‚	single low-9 quotation mark
        {"\u201C", "ldquo"}, //“	left double quotation mark
        {"\u201D", "rdquo"}, //”	right double quotation mark
        {"\u201E", "bdquo"}, //„	double low-9 quotation mark
        {"\u2020", "dagger"}, //†	dagger, obelisk
        {"\u2021", "Dagger"}, //‡	double dagger, double obelisk
        {"\u2022", "bull"}, //•	bullet [f]
        {"\u2026", "hellip"}, //…	horizontal ellipsis 
        {"\u2030", "permil"}, //‰	per mille sign
        {"\u2032", "prime"}, //′	prime 
        {"\u2033", "Prime"}, //″	double prime 
        {"\u2039", "lsaquo"}, //‹	single left-pointing angle quotation mark[g]
        {"\u203A", "rsaquo"}, //›	single right-pointing angle quotation mark[g]
        {"\u203E", "oline"}, //‾	overline 
        {"\u2044", "frasl"}, //⁄	fraction slash 
        {"\u20AC", "euro"}, //€	euro sign
        {"\u2111", "image"}, //ℑ	black-letter capital I 
        {"\u2118", "weierp"}, //℘	script capital P 
        {"\u211C", "real"}, //ℜ	black-letter capital R 
        {"\u2122", "trade"}, //™	trademark symbol
        {"\u2135", "alefsym"}, //ℵ	alef symbol [h]
        {"\u2190", "larr"}, //←	leftwards arrow
        {"\u2191", "uarr"}, //↑	upwards arrow
        {"\u2192", "rarr"}, //→	rightwards arrow
        {"\u2193", "darr"}, //↓	downwards arrow
        {"\u2194", "harr"}, //↔	left right arrow
        {"\u21B5", "crarr"}, //↵	downwards arrow with corner leftwards 
        {"\u21D0", "lArr"}, //⇐	leftwards double arrow[i]
        {"\u21D1", "uArr"}, //⇑	upwards double arrow
        {"\u21D2", "rArr"}, //⇒	rightwards double arrow[j]
        {"\u21D3", "dArr"}, //⇓	downwards double arrow
        {"\u21D4", "hArr"}, //⇔	left right double arrow
        {"\u2200", "forall"}, //∀	for all
        {"\u2202", "part"}, //∂	partial differential
        {"\u2203", "exist"}, //∃	there exists
        {"\u2205", "empty"}, //∅	empty set ; see also {"\u8960, ⌀
        {"\u2207", "nabla"}, //∇	del or nabla 
        {"\u2208", "isin"}, //∈	element of
        {"\u2209", "notin"}, //∉	not an element of
        {"\u220B", "ni"}, //∋	contains as member
        {"\u220F", "prod"}, //∏	n-ary product [k]
        {"\u2211", "sum"}, //∑	n-ary summation[l]
        {"\u2212", "minus"}, //−	minus sign
        {"\u2217", "lowast"}, //∗	asterisk operator
        {"\u221A", "radic"}, //√	square root 
        {"\u221D", "prop"}, //∝	proportional to
        {"\u221E", "infin"}, //∞	infinity
        {"\u2220", "ang"}, //∠	angle
        {"\u2227", "and"}, //∧	logical and 
        {"\u2228", "or"}, //∨	logical or 
        {"\u2229", "cap"}, //∩	intersection 
        {"\u222A", "cup"}, //∪	union 
        {"\u222B", "int"}, //∫	integral
        {"\u2234", "there4"}, //∴	therefore sign
        {"\u223C", "sim"}, //∼	tilde operator [m]
        {"\u2245", "cong"}, //≅	congruent to
        {"\u2248", "asymp"}, //≈	almost equal to 
        {"\u2260", "ne"}, //≠	not equal to
        {"\u2261", "equiv"}, //≡	identical to; sometimes used for 'equivalent to'
        {"\u2264", "le"}, //≤	less-than or equal to
        {"\u2265", "ge"}, //≥	greater-than or equal to
        {"\u2282", "sub"}, //⊂	subset of
        {"\u2283", "sup"}, //⊃	superset of[n]
        {"\u2284", "nsub"}, //⊄	not a subset of
        {"\u2286", "sube"}, //⊆	subset of or equal to
        {"\u2287", "supe"}, //⊇	superset of or equal to
        {"\u2295", "oplus"}, //⊕	circled plus 
        {"\u2297", "otimes"}, //⊗	circled times 
        {"\u22A5", "perp"}, //⊥	up tack [o]
        {"\u22C5", "sdot"}, //⋅	dot operator[p]
        {"\u2308", "lceil"}, //⌈	left ceiling 
        {"\u2309", "rceil"}, //⌉	right ceiling
        {"\u230A", "lfloor"}, //⌊	left floor 
        {"\u230B", "rfloor"}, //⌋	right floor
        {"\u2329", "lang"}, //〈	left-pointing angle bracket [q]
        {"\u232A", "rang"}, //〉	right-pointing angle bracket [r]
        {"\u25CA", "loz"}, //◊	lozenge
        {"\u2660", "spades"}, //♠	black spade suit[f]
        {"\u2663", "clubs"}, //♣	black club suit [f]
        {"\u2665", "hearts"}, //♥	black heart suit [f]
        {"\u2666", "diams"} //♦	black diamond suit[f]
    };

    private static final HashMap<String, CharSequence> lookupMap;
    static {
        lookupMap = new HashMap<String, CharSequence>();
        for (final CharSequence[] seq : ESCAPES) 
            lookupMap.put(seq[1].toString(), seq[0]);
    }
}