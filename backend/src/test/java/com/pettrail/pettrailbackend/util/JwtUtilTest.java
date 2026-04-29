package com.pettrail.pettrailbackend.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "pet-trail-test-secret-key-must-be-at-least-256-bits-long-for-hmac-sha256");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 604800L);
    }

    @Test
    void generateToken_shouldReturnValidToken() {
        String token = jwtUtil.generateToken(1L, "test_openid_123");
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    void getUserIdFromToken_shouldReturnCorrectUserId() {
        Long userId = 42L;
        String token = jwtUtil.generateToken(userId, "test_openid");
        Long extractedUserId = jwtUtil.getUserIdFromToken(token);
        assertEquals(userId, extractedUserId);
    }

    @Test
    void getOpenidFromToken_shouldReturnCorrectOpenid() {
        String openid = "oTest123456789";
        String token = jwtUtil.generateToken(1L, openid);
        String extractedOpenid = jwtUtil.getOpenidFromToken(token);
        assertEquals(openid, extractedOpenid);
    }

    @Test
    void validateToken_shouldReturnTrueForValidToken() {
        String token = jwtUtil.generateToken(1L, "test_openid");
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void validateToken_shouldReturnFalseForInvalidToken() {
        assertFalse(jwtUtil.validateToken("invalid.token.string"));
    }

    @Test
    void validateToken_shouldReturnFalseForEmptyToken() {
        assertFalse(jwtUtil.validateToken(""));
    }

    @Test
    void validateToken_shouldReturnFalseForNullToken() {
        assertFalse(jwtUtil.validateToken(null));
    }

    @Test
    void getRemainingTimeInSeconds_shouldBePositive() {
        String token = jwtUtil.generateToken(1L, "test_openid");
        long remaining = jwtUtil.getRemainingTimeInSeconds(token);
        assertTrue(remaining > 0);
        assertTrue(remaining <= 604800);
    }
}
