package com.yourname.urlshortener.model;

public class Link {

    private String code;
    private String longUrl;
    private long createdAt;
    private long clicks;

    public Link() {
    }

    public Link(String code, String longUrl, long createdAt, long clicks) {
        this.code = code;
        this.longUrl = longUrl;
        this.createdAt = createdAt;
        this.clicks = clicks;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLongUrl() {
        return longUrl;
    }

    public void setLongUrl(String longUrl) {
        this.longUrl = longUrl;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getClicks() {
        return clicks;
    }

    public void setClicks(long clicks) {
        this.clicks = clicks;
    }
}
