package com.alumniconnect.identityservice.service;

import java.io.ByteArrayInputStream;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.alumniconnect.identityservice.config.MinioProperties;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.ErrorResponseException;
import jakarta.annotation.PostConstruct;

@Component
public class AvatarStore {

    public record Entry(byte[] data, String contentType) {}

    private static final Logger log = LoggerFactory.getLogger(AvatarStore.class);

    private final MinioClient client;
    private final String bucket;

    public AvatarStore(MinioProperties props) {
        this.client = MinioClient.builder()
                .endpoint(props.endpoint())
                .credentials(props.accessKey(), props.secretKey())
                .build();
        this.bucket = props.bucket();
    }

    @PostConstruct
    public void ensureBucket() {
        try {
            boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("Created MinIO bucket '{}'", bucket);
            }
        } catch (Exception e) {
            log.warn("Could not connect to MinIO at startup — avatar upload will fail until MinIO is available: {}", e.getMessage());
        }
    }

    public void put(String userId, byte[] data, String contentType) {
        try {
            client.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(userId)
                    .stream(new ByteArrayInputStream(data), data.length, -1)
                    .contentType(contentType)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to store avatar in MinIO", e);
        }
    }

    public Optional<Entry> get(String userId) {
        try {
            GetObjectResponse response = client.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(userId)
                    .build());
            byte[] data = response.readAllBytes();
            String contentType = response.headers().get("Content-Type");
            return Optional.of(new Entry(data, contentType));
        } catch (ErrorResponseException e) {
            if ("NoSuchKey".equals(e.errorResponse().code())) {
                return Optional.empty();
            }
            throw new RuntimeException("Failed to retrieve avatar from MinIO", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve avatar from MinIO", e);
        }
    }
}
