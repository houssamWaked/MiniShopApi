package com.yourname.urlshortener.controller;

import com.yourname.urlshortener.dto.ShortenRequest;
import com.yourname.urlshortener.dto.ShortenResponse;
import com.yourname.urlshortener.service.LinkService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class ShortenController {

    private final LinkService linkService;

    public ShortenController(LinkService linkService) {
        this.linkService = linkService;
    }

    @PostMapping(path = "/shorten", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ShortenResponse shorten(@RequestBody ShortenRequest request) {
        String code = linkService.shorten(request.getLongUrl());
        String shortUrl = "/r/" + code;
        return new ShortenResponse(code, shortUrl);
    }
}
