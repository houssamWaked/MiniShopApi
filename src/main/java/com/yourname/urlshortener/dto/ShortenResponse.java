package com.yourname.urlshortener.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ShortenResponse {

    private String code;
    private Map<String, String> shortUrls;

    public ShortenResponse(String code, Map<String, String> shortUrls) {
        this.code = code;
        this.shortUrls = shortUrls;
    }

    public String getCode() {
        return code;
    }

    public Map<String, String> getShortUrls() {
        return shortUrls;
    }
}
