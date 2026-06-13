package com.krithe.store.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final byte[] secretBytes;
    private final long expiration;
    private final ObjectMapper mapper = new ObjectMapper();

    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.expiration}") long expiration) {
        this.secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.expiration = expiration;
    }

    public String generateToken(Long userId, String email, String role) {
        try {
            Map<String, Object> header = new HashMap<>();
            header.put("alg", "HS256");
            header.put("typ", "JWT");

            Map<String, Object> payload = new HashMap<>();
            long now = Instant.now().toEpochMilli();
            payload.put("sub", email);
            payload.put("userId", userId);
            payload.put("role", role);
            payload.put("iat", now);
            payload.put("exp", now + expiration);

            String encodedHeader = base64Url(mapper.writeValueAsBytes(header));
            String encodedPayload = base64Url(mapper.writeValueAsBytes(payload));
            String unsigned = encodedHeader + "." + encodedPayload;
            String signature = base64Url(sign(unsigned.getBytes(StandardCharsets.UTF_8)));
            String token = unsigned + "." + signature;
            log.debug("JWT generated for userId={}", userId);
            return token;
        } catch (Exception e) {
            log.error("Failed to generate JWT", e);
            throw new RuntimeException("Failed to generate token");
        }
    }

    private Map<String, Object> parsePayload(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) throw new IllegalArgumentException("Invalid token format");
            String headerB64 = parts[0];
            String payloadB64 = parts[1];
            String sigB64 = parts[2];

            String unsigned = headerB64 + "." + payloadB64;
            byte[] sig = base64UrlDecode(sigB64);
            if (!verify(unsigned.getBytes(StandardCharsets.UTF_8), sig)) {
                throw new IllegalArgumentException("Invalid token signature");
            }

            byte[] payloadBytes = base64UrlDecode(payloadB64);
            return mapper.readValue(payloadBytes, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JWT token", e);
        }
    }

    public boolean validateToken(String token) {
        try {
            Map<String, Object> payload = parsePayload(token);
            Object exp = payload.get("exp");
            long expMillis = exp instanceof Number ? ((Number) exp).longValue() : Long.parseLong(String.valueOf(exp));
            return Instant.now().toEpochMilli() < expMillis;
        } catch (Exception e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public Long getUserId(String token) {
        Map<String, Object> payload = parsePayload(token);
        Object uid = payload.get("userId");
        if (uid instanceof Number) return ((Number) uid).longValue();
        return uid != null ? Long.valueOf(String.valueOf(uid)) : null;
    }

    public String getEmail(String token) {
        Map<String, Object> payload = parsePayload(token);
        return payload.get("sub") != null ? String.valueOf(payload.get("sub")) : null;
    }

    public String getRole(String token) {
        Map<String, Object> payload = parsePayload(token);
        return payload.get("role") != null ? String.valueOf(payload.get("role")) : null;
    }

    private byte[] sign(byte[] data) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secretBytes, "HmacSHA256"));
        return mac.doFinal(data);
    }

    private boolean verify(byte[] data, byte[] signature) throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] expected = sign(data);
        if (expected.length != signature.length) return false;
        for (int i = 0; i < expected.length; i++) if (expected[i] != signature[i]) return false;
        return true;
    }

    private String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private byte[] base64UrlDecode(String str) {
        return Base64.getUrlDecoder().decode(str);
    }
}