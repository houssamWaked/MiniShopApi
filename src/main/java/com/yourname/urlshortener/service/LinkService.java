package com.yourname.urlshortener.service;

import com.yourname.urlshortener.exception.BadRequestException;
import com.yourname.urlshortener.exception.NotFoundException;
import com.yourname.urlshortener.model.Link;
import com.yourname.urlshortener.repository.LinkRepository;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LinkService {

    private static final int MAX_RETRIES = 5;
    private final LinkRepository linkRepository;
    private final CodeGenerator codeGenerator;

    public LinkService(LinkRepository linkRepository, CodeGenerator codeGenerator) {
        this.linkRepository = linkRepository;
        this.codeGenerator = codeGenerator;
    }

    public String shorten(String longUrl) {
        String normalizedUrl = validateLongUrl(longUrl);

        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            String code = codeGenerator.generate();
            if (!linkRepository.existsByCode(code)) {
                Link link = new Link(code, normalizedUrl, Instant.now().toEpochMilli(), 0L);
                linkRepository.save(link);
                return code;
            }
        }

        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate unique code");
    }

    public String getLongUrlOrThrow(String code) {
        return linkRepository.findByCode(code)
                .map(Link::getLongUrl)
                .orElseThrow(() -> new NotFoundException("Link not found"));
    }

    public void incrementClicks(String code) {
        linkRepository.incrementClicks(code);
    }

    private String validateLongUrl(String longUrl) {
        if (longUrl == null || longUrl.isBlank()) {
            throw new BadRequestException("longUrl is required");
        }

        try {
            URI uri = new URI(longUrl.trim());
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                throw new BadRequestException("longUrl must start with http or https");
            }
            if (uri.getHost() == null) {
                throw new BadRequestException("longUrl must be a valid URL");
            }
            return uri.toString();
        } catch (URISyntaxException ex) {
            throw new BadRequestException("longUrl must be a valid URL");
        }
    }
}
