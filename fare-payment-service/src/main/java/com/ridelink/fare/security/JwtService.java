package com.ridelink.fare.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;


@Service
public class JwtService {

    // =========================================================================
    // SECTION 1: SECRET KEY INITIALIZATION (HMAC-SHA)
    // =========================================================================
    // Cryptographic secret key used to mathematically verify incoming JWT signatures
    private final SecretKey key;

    public JwtService(@Value("${ridelink.jwt.secret}") String secret) {
        // Derives an HMAC-SHA signing key from the secret string in application.properties
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // =========================================================================
    // SECTION 2: TOKEN PARSING & SIGNATURE VERIFICATION
    // =========================================================================
    /**
     * Parses the Bearer JWT string, checks signature integrity using the SecretKey,
     * checks token expiration, and returns the Claims payload (sub, email, role, etc.).
     * Throws JwtException if the token is forged, tampered with, or expired.
     */
    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}

