package com.bank.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "idempotency_records", indexes = {
        @Index(name = "idx_idem_key", columnList = "idempotency_key", unique = true)
})
public class IdempotencyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "idempotency_key", unique = true, nullable = false, length = 100)
    private String idempotencyKey;

    @Column(nullable = false, length = 100)
    private String endpoint;

    @Column(name = "request_payload_hash", length = 128)
    private String requestPayloadHash;

    @Lob
    @Column(name = "response_payload", columnDefinition = "TEXT")
    private String responsePayload;

    @Column(name = "status_code", nullable = false)
    private int statusCode;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public IdempotencyRecord() {
    }

    public IdempotencyRecord(String idempotencyKey, String endpoint, String requestPayloadHash,
                             String responsePayload, int statusCode) {
        this.idempotencyKey = idempotencyKey;
        this.endpoint = endpoint;
        this.requestPayloadHash = requestPayloadHash;
        this.responsePayload = responsePayload;
        this.statusCode = statusCode;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getRequestPayloadHash() {
        return requestPayloadHash;
    }

    public void setRequestPayloadHash(String requestPayloadHash) {
        this.requestPayloadHash = requestPayloadHash;
    }

    public String getResponsePayload() {
        return responsePayload;
    }

    public void setResponsePayload(String responsePayload) {
        this.responsePayload = responsePayload;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
