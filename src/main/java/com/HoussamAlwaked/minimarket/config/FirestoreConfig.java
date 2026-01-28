package com.HoussamAlwaked.minimarket.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.cloud.firestore.Firestore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
public class FirestoreConfig {

    @Bean
    public Firestore firestore() throws IOException {
        String projectId = System.getenv("FIREBASE_PROJECT_ID");
        if (projectId == null || projectId.isBlank()) {
            throw new IllegalStateException("FIREBASE_PROJECT_ID is required");
        }

        String saJson = System.getenv("FIREBASE_SERVICE_ACCOUNT_JSON");
        if (saJson == null || saJson.isBlank()) {
            throw new IllegalStateException("FIREBASE_SERVICE_ACCOUNT_JSON is required on Railway");
        }

        GoogleCredentials creds = GoogleCredentials.fromStream(
                new ByteArrayInputStream(saJson.getBytes(StandardCharsets.UTF_8))
        );

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(creds)
                .setProjectId(projectId)
                .build();

        FirebaseApp app;
        List<FirebaseApp> apps = FirebaseApp.getApps();
        if (apps == null || apps.isEmpty()) {
            app = FirebaseApp.initializeApp(options);
        } else {
            app = apps.get(0);
        }

        return FirestoreClient.getFirestore(app);
    }
}
