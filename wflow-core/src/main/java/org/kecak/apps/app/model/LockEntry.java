package org.kecak.apps.app.model;

import java.util.Date;

public class LockEntry {
    private String url;
    private String username;

    private Date date;

    public LockEntry(String url, String username) {
        this(url, username, new Date());
    }

    public LockEntry(String url, String username, Date date) {
        this.url = url;
        this.username = username;
        this.date = date;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof String) {
            return url.equals(obj);
        }

        if(obj instanceof LockEntry) {
            return url.equals(((LockEntry) obj).url);
        }

        return false;
    }

    public String getUrl() {
        return url;
    }
    public String getUsername() {
        return username;
    }

    public Date getDate() {
        return date;
    }
}
