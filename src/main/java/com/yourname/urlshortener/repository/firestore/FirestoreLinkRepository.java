package com.yourname.urlshortener.repository.firestore;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.yourname.urlshortener.model.Link;
import com.yourname.urlshortener.repository.LinkRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class FirestoreLinkRepository implements LinkRepository {

    private static final Logger logger = LoggerFactory.getLogger(FirestoreLinkRepository.class);
    private static final String COLLECTION = "links";

    private final Firestore firestore;

    public FirestoreLinkRepository(Firestore firestore) {
        this.firestore = firestore;
    }

    @Override
    public Optional<Link> findByCode(String code) {
        try {
            DocumentSnapshot snapshot = getDocument(code).get().get();
            if (!snapshot.exists()) {
                return Optional.empty();
            }
            String longUrl = snapshot.getString("longUrl");
            Long createdAt = snapshot.getLong("createdAt");
            Long clicks = snapshot.getLong("clicks");
            Link link = new Link(code, longUrl, createdAt == null ? 0L : createdAt, clicks == null ? 0L : clicks);
            return Optional.of(link);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to read link", ex);
        }
    }

    @Override
    public boolean existsByCode(String code) {
        try {
            DocumentSnapshot snapshot = getDocument(code).get().get();
            return snapshot.exists();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to check link", ex);
        }
    }

    @Override
    public void save(Link link) {
        Map<String, Object> data = new HashMap<>();
        data.put("longUrl", link.getLongUrl());
        data.put("createdAt", link.getCreatedAt());
        data.put("clicks", link.getClicks());

        try {
            getDocument(link.getCode()).set(data).get();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to save link", ex);
        }
    }

    @Override
    public void incrementClicks(String code) {
        try {
            ApiFuture<WriteResult> update = getDocument(code).update("clicks", FieldValue.increment(1));
            update.get();
        } catch (Exception ex) {
            logger.warn("Failed to increment clicks for {}", code, ex);
        }
    }

    private DocumentReference getDocument(String code) {
        return firestore.collection(COLLECTION).document(code);
    }
}
