package org.joget.apps.workflow.controller;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.PagedList;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.model.service.WorkflowManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Controller to output RSS
 */
@Controller
public class WorkflowRssController {

    @Autowired
    private WorkflowManager workflowManager;
    public static final String INBOX_TITLE_KEY = "client.rss.inbox.label.title";
    public static final String INBOX_DESC_KEY = "client.rss.inbox.label.description";
    public static final String INBOX_LINK = "/web/console/run/inbox";
    public static final String ASSIGNMENT_LINK = "/web/client/app/assignment/";

    @RequestMapping("/rss/client/inbox")
    public void assignmentPendingAndAcceptedList(Writer writer, HttpServletRequest request, @RequestParam(value = "packageId", required = false) String packageId, @RequestParam(value = "processDefId", required = false) String processDefId, @RequestParam(value = "processId", required = false) String processId, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows, @RequestParam(value = "j_username", required = false) String username, @RequestParam(value = "hash", required = false) String hash) throws IOException {
        String urlPrefix = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();

        if (sort == null) {
            sort = "dateCreated";
        }

        if (desc == null) {
            desc = true;
        }

        if (rows == null) {
            rows = 10;
        }

        String autoLogin = "";
        if (username != null && hash != null) {
            autoLogin = "?j_username=" + username + "&hash=" + hash;
        }

        PagedList<WorkflowAssignment> assignmentList = workflowManager.getAssignmentPendingAndAcceptedList(packageId, processDefId, processId, sort, desc, start, rows);
        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM yyyy, h:mm a");

        try {
            OutputFormat of = new OutputFormat("XML", "ISO-8859-1", true);
            of.setIndent(1);
            of.setIndenting(true);
            XMLSerializer serializer = new XMLSerializer(writer, of);

            ContentHandler hd = serializer.asContentHandler();
            hd.startDocument();

            AttributesImpl atts = new AttributesImpl();
            atts.addAttribute("", "", "version", "CDATA", "2.0");
            hd.startElement("", "", "rss", atts);

            atts.clear();
            hd.startElement("", "", "channel", atts);
            hd.startElement("", "", "title", atts);
            String inboxTitle = ResourceBundleUtil.getMessage(INBOX_TITLE_KEY);
            hd.characters(inboxTitle.toCharArray(), 0, inboxTitle.length());
            hd.endElement("", "", "title");

            hd.startElement("", "", "description", atts);
            String inboxDescription = ResourceBundleUtil.getMessage(INBOX_DESC_KEY);
            hd.characters(inboxDescription.toCharArray(), 0, inboxDescription.length());
            hd.endElement("", "", "description");

            hd.startElement("", "", "link", atts);
            String inboxLink = urlPrefix + INBOX_LINK + autoLogin;
            hd.characters(inboxLink.toCharArray(), 0, inboxLink.length());
            hd.endElement("", "", "link");

            hd.startElement("", "", "pubDate", atts);
            hd.characters(dateFormat.format(new Date()).toCharArray(), 0, dateFormat.format(new Date()).length());
            hd.endElement("", "", "pubDate");

            for (WorkflowAssignment assignment : assignmentList) {
                hd.startElement("", "", "item", atts);
                hd.startElement("", "", "title", atts);
                hd.characters(assignment.getActivityName().toCharArray(), 0, assignment.getActivityName().length());
                hd.endElement("", "", "title");

                String description = assignment.getProcessName() + " (ver " + assignment.getProcessVersion() + ")";
                hd.startElement("", "", "description", atts);
                hd.characters(description.toCharArray(), 0, description.length());
                hd.endElement("", "", "description");

                String assignmentLink = urlPrefix + ASSIGNMENT_LINK + assignment.getActivityId() + autoLogin;
                hd.startElement("", "", "link", atts);
                hd.characters(assignmentLink.toCharArray(), 0, assignmentLink.length());
                hd.endElement("", "", "link");

                hd.startElement("", "", "pubDate", atts);
                hd.characters(dateFormat.format(assignment.getDateCreated()).toCharArray(), 0, dateFormat.format(assignment.getDateCreated()).length());
                hd.endElement("", "", "pubDate");

                hd.startElement("", "", "guid", atts);
                hd.characters(assignment.getActivityId().toCharArray(), 0, assignment.getActivityId().length());
                hd.endElement("", "", "guid");
                hd.endElement("", "", "item");
            }

            hd.endElement("", "", "channel");
            hd.endElement("", "", "rss");

            writer.flush();
        } catch (Exception ex) {
            LogUtil.error(getClass().getName(), ex, "Error retrieving assignment RSS");
        }
    }
}
