package com.ridelink.ride.security;

/**
 * Holds the inbound Bearer token for the current request thread so outbound
 * service calls can forward the same JWT. In this demo, internal calls reuse
 * the authenticated user's token rather than a dedicated service-to-service credential.
 */
public final class AuthTokenHolder {

    private static final ThreadLocal<String> TOKEN = new ThreadLocal<>();

    private AuthTokenHolder() {
    }

    public static void set(String token) {
        TOKEN.set(token);
    }

    public static String get() {
        return TOKEN.get();
    }

    public static void clear() {
        TOKEN.remove();
    }
}
