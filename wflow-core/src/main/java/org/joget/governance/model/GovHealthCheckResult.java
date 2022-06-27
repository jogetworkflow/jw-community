package org.joget.governance.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class GovHealthCheckResult {
    public enum Status {
        PASS, FAIL, WARN, INFO
    };
    
    public static class Detail {
        private String detail;
        private String link;
        private String linkLabel;
        private String appId;
        private Boolean suppressed = false;

        public Detail(String detail, String link, String linkLabel, String appId) {
            this.detail = detail;
            this.link = link;
            this.linkLabel = linkLabel;
            this.appId = appId;
        }
        
        public String getDetail() {
            return detail;
        }

        public void setDetail(String detail) {
            this.detail = detail;
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }

        public String getLinkLabel() {
            return linkLabel;
        }

        public void setLinkLabel(String linkLabel) {
            this.linkLabel = linkLabel;
        }

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public Boolean getSuppressed() {
            return suppressed;
        }

        public void setSuppressed(Boolean suppressed) {
            this.suppressed = suppressed;
        }
    }
    
    private Status status;
    private Integer score;
    private Date timestamp;
    private Collection<Detail> details;
    private String moreInfo;
    private Map<String, String> datas;
    private Boolean suppressable = false;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        if (score > 100) {
            score = 100;
        } else if (score < 0) {
            score = 0;
        }
        
        if (score >= 75) {
            status = Status.PASS;
        } else if (score >= 45) {
            status = Status.WARN;
        } else {
            status = Status.FAIL;
        }          
                        
        this.score = score;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Collection<Detail> getDetails() {
        if (this.details == null) {
            this.details = new ArrayList<Detail>();
        }
        return details;
    }

    public void setDetails(Collection<Detail> details) {
        this.details = details;
    }
    
    public void addDetail(String detail) {
        addDetail(detail, null, null);
    }
    
    public void addDetail(String detail, String link, String linkLabel) {
        if (this.details == null) {
            this.details = new ArrayList<Detail>();
        }
        this.details.add(new Detail(detail, link, linkLabel, null));
    }
    
    public void addDetailWithAppId(String detail, String link, String linkLabel, String appId) {
        if (this.details == null) {
            this.details = new ArrayList<Detail>();
        }
        this.details.add(new Detail(detail, link, linkLabel, appId));
    }

    public String getMoreInfo() {
        return moreInfo;
    }

    public void setMoreInfo(String moreInfo) {
        this.moreInfo = moreInfo;
    }

    public Map<String, String> getDatas() {
        if (this.datas == null) {
            this.datas = new HashMap<String, String>();
        }
        return datas;
    }

    public void setDatas(Map<String, String> datas) {
        this.datas = datas;
    }
    
    public void setData(String key, String data) {
        if (this.datas == null) {
            this.datas = new HashMap<String, String>();
        }
        this.datas.put(key, data);
    }
    
    public String getData(String key) {
        if (this.datas != null) {
            return this.datas.get(key);
        }
        return null;
    }

    public Boolean getSuppressable() {
        return suppressable;
    }

    public void setSuppressable(Boolean suppressable) {
        this.suppressable = suppressable;
    }
}

