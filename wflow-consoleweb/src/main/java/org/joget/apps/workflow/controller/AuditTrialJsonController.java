package org.joget.apps.workflow.controller;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joget.apps.app.dao.AuditTrailDao;
import org.joget.apps.app.model.AuditTrail;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.TimeZoneUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuditTrialJsonController {

    @Autowired
    private AuditTrailDao auditTrailDao;

    @RequestMapping("/json/workflow/audittrail/list")
    public void auditTrailList(Writer writer, @RequestParam(value = "callback", required = false) String callback,  @RequestParam(value = "search", required = false) String search, @RequestParam(value = "dateFrom", required = false) String dateFrom, @RequestParam(value = "dateTo", required = false) String dateTo, @RequestParam(value = "sort", required = false) String sort, @RequestParam(value = "desc", required = false) Boolean desc, @RequestParam(value = "start", required = false) Integer start, @RequestParam(value = "rows", required = false) Integer rows) throws IOException, JSONException {

        List<AuditTrail> auditTrailList;
        String condition = "";
        Collection<Object> args = new ArrayList<Object>();

        if (search != null && !search.isEmpty()) {
            condition = "(e.username like ? or e.clazz like ? or e.method like ? or e.message like ?)";
            args.add("%" + search + "%");
            args.add("%" + search + "%");
            args.add("%" + search + "%");
            args.add("%" + search + "%");
        }
        
        if (dateFrom != null && dateFrom.trim().length() > 0 && dateTo != null && dateTo.trim().length() > 0) {
            String[] dateFroms = dateFrom.split("-");
            String[] dateTos = dateTo.split("-");

            Calendar dateFromCal = Calendar.getInstance();
            dateFromCal.set(Integer.parseInt(dateFroms[0]), Integer.parseInt(dateFroms[1]) - 1, Integer.parseInt(dateFroms[2]), 0, 0, 0);

            Calendar dateToCal = Calendar.getInstance();
            dateToCal.set(Integer.parseInt(dateTos[0]), Integer.parseInt(dateTos[1]) - 1, Integer.parseInt(dateTos[2]), 23, 59, 59);

            if (!condition.isEmpty()) {
                condition = condition + " and ";
            }
            condition += "e.timestamp >= ? and e.timestamp <= ?";
            args.add(dateFromCal.getTime());
            args.add(dateToCal.getTime());
        }
        
        if (!condition.isEmpty()) {
            condition = "where " + condition;
        }
        
        auditTrailList = auditTrailDao.getAuditTrails(condition, args.toArray(), sort, desc, start, rows);

        JSONObject jsonObject = new JSONObject();
        for (AuditTrail auditTrail : auditTrailList) {
            Map data = new HashMap();
            data.put("id", auditTrail.getId());
            data.put("username", auditTrail.getUsername());
            data.put("clazz", ResourceBundleUtil.getMessage(auditTrail.getClazz(), auditTrail.getClazz()));
            data.put("method", ResourceBundleUtil.getMessage(auditTrail.getMethod(), auditTrail.getMethod()));
            data.put("message", auditTrail.getMessage());
            data.put("timestamp", TimeZoneUtil.convertToTimeZone(auditTrail.getTimestamp(), null, AppUtil.getAppDateFormat()));
            jsonObject.accumulate("data", data);
        }
        
        jsonObject.accumulate("total", auditTrailDao.count(condition, args.toArray()));
        jsonObject.accumulate("start", start);
        jsonObject.accumulate("sort", sort);
        jsonObject.accumulate("desc", desc);

        AppUtil.writeJson(writer, jsonObject, callback);
    }
}
