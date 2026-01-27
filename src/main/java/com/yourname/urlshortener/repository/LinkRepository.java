package com.yourname.urlshortener.repository;

import com.yourname.urlshortener.model.Link;
import java.util.Optional;

public interface LinkRepository {

    Optional<Link> findByCode(String code);

    boolean existsByCode(String code);

    void save(Link link);

    void incrementClicks(String code);
}
