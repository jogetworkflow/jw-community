package org.joget.governance.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

public class GovHealthCheckResult {
    public enum Status {
        PASS, FAIL, WARN, INFO
    };
    
    public class Detail {
        private String detail;
        private String link;
        private String linkLabel;

        public Detail(String detail, String link, String linkLabel) {
            this.detail = detail;
            this.link = link;
            this.linkLabel = linkLabel;
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
    }
    
    private Status status;
    private Integer score;
    private Date timestamp;
    private Collection<Detail> details;
    private String moreInfo;

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
        this.details.add(new Detail(detail, link, linkLabel));
    }

    public String getMoreInfo() {
        return moreInfo;
    }

    public void setMoreInfo(String moreInfo) {
        this.moreInfo = moreInfo;
    }
}
