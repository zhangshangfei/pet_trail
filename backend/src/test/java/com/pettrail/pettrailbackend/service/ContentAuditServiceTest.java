package com.pettrail.pettrailbackend.service;

import com.pettrail.pettrailbackend.util.HttpUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContentAuditServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private ContentAuditService contentAuditService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(contentAuditService, "appId", "test-app-id");
        ReflectionTestUtils.setField(contentAuditService, "appSecret", "test-app-secret");
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void auditText_shouldPassWhenContentIsNull() {
        assertTrue(contentAuditService.auditText(null));
    }

    @Test
    void auditText_shouldPassWhenContentIsEmpty() {
        assertTrue(contentAuditService.auditText(""));
        assertTrue(contentAuditService.auditText("   "));
    }

    @Test
    void auditText_shouldRejectLocalSensitiveWord() {
        assertFalse(contentAuditService.auditText("法轮功是邪教"));
        assertFalse(contentAuditService.auditText("这里有卖淫信息"));
        assertFalse(contentAuditService.auditText("代开发票联系我"));
    }

    @Test
    void auditText_shouldPassNormalContent() {
        try (MockedStatic<HttpUtil> httpUtilMock = mockStatic(HttpUtil.class)) {
            when(valueOperations.get("wechat:access_token")).thenReturn("test-token");
            httpUtilMock.when(() -> HttpUtil.doPost(anyString(), anyString()))
                    .thenReturn("{\"errcode\":0,\"errmsg\":\"ok\"}");

            assertTrue(contentAuditService.auditText("今天天气真好"));
        }
    }

    @Test
    void auditText_shouldRejectRiskyContent() {
        try (MockedStatic<HttpUtil> httpUtilMock = mockStatic(HttpUtil.class)) {
            when(valueOperations.get("wechat:access_token")).thenReturn("test-token");
            httpUtilMock.when(() -> HttpUtil.doPost(anyString(), anyString()))
                    .thenReturn("{\"errcode\":0,\"detail\":{\"suggest\":\"risky\",\"label\":\"20001\"}}");

            assertFalse(contentAuditService.auditText("测试内容"));
        }
    }

    @Test
    void auditText_shouldRefreshTokenWhenExpired() {
        try (MockedStatic<HttpUtil> httpUtilMock = mockStatic(HttpUtil.class)) {
            when(valueOperations.get("wechat:access_token")).thenReturn("expired-token");
            httpUtilMock.when(() -> HttpUtil.doPost(contains("expired-token"), anyString()))
                    .thenReturn("{\"errcode\":40001,\"errmsg\":\"invalid credential\"}");
            httpUtilMock.when(() -> HttpUtil.doPost(contains("new-token"), anyString()))
                    .thenReturn("{\"errcode\":0,\"errmsg\":\"ok\"}");

            when(valueOperations.get("wechat:access_token")).thenReturn("expired-token");
            assertTrue(contentAuditService.auditText("正常内容"));

            verify(redisTemplate).delete("wechat:access_token");
        }
    }

    @Test
    void auditText_shouldPassWhenAccessTokenNull() {
        when(valueOperations.get("wechat:access_token")).thenReturn(null);
        try (MockedStatic<HttpUtil> httpUtilMock = mockStatic(HttpUtil.class)) {
            httpUtilMock.when(() -> HttpUtil.doGet(anyString()))
                    .thenReturn("{\"errcode\":40013,\"errmsg\":\"invalid appid\"}");

            assertTrue(contentAuditService.auditText("正常内容"));
        }
    }

    @Test
    void auditImage_shouldPassWhenUrlIsNull() {
        assertTrue(contentAuditService.auditImage(null));
        assertTrue(contentAuditService.auditImage(""));
    }

    @Test
    void auditImage_shouldRejectViolentImage() {
        try (MockedStatic<HttpUtil> httpUtilMock = mockStatic(HttpUtil.class)) {
            when(valueOperations.get("wechat:access_token")).thenReturn("test-token");
            httpUtilMock.when(() -> HttpUtil.doPost(anyString(), anyString()))
                    .thenReturn("{\"errcode\":87014,\"errmsg\":\"risky content\"}");

            assertFalse(contentAuditService.auditImage("http://bad-image.jpg"));
        }
    }

    @Test
    void auditImage_shouldPassNormalImage() {
        try (MockedStatic<HttpUtil> httpUtilMock = mockStatic(HttpUtil.class)) {
            when(valueOperations.get("wechat:access_token")).thenReturn("test-token");
            httpUtilMock.when(() -> HttpUtil.doPost(anyString(), anyString()))
                    .thenReturn("{\"errcode\":0,\"errmsg\":\"ok\"}");

            assertTrue(contentAuditService.auditImage("http://normal-image.jpg"));
        }
    }

    @Test
    void getAccessToken_shouldReturnFromCache() {
        when(valueOperations.get("wechat:access_token")).thenReturn("cached-token");

        String token = contentAuditService.getAccessToken();
        assertEquals("cached-token", token);
    }

    @Test
    void getAccessToken_shouldFetchNewWhenCacheMiss() {
        when(valueOperations.get("wechat:access_token")).thenReturn(null);
        try (MockedStatic<HttpUtil> httpUtilMock = mockStatic(HttpUtil.class)) {
            httpUtilMock.when(() -> HttpUtil.doGet(anyString()))
                    .thenReturn("{\"access_token\":\"new-token\",\"expires_in\":7200}");

            String token = contentAuditService.getAccessToken();
            assertEquals("new-token", token);
            verify(valueOperations).set(eq("wechat:access_token"), eq("new-token"), eq(110L), eq(TimeUnit.MINUTES));
        }
    }

    @Test
    void getAccessToken_shouldReturnNullWhenFetchFails() {
        when(valueOperations.get("wechat:access_token")).thenReturn(null);
        try (MockedStatic<HttpUtil> httpUtilMock = mockStatic(HttpUtil.class)) {
            httpUtilMock.when(() -> HttpUtil.doGet(anyString()))
                    .thenReturn("{\"errcode\":40013,\"errmsg\":\"invalid appid\"}");

            String token = contentAuditService.getAccessToken();
            assertNull(token);
        }
    }

    @Test
    void auditText_noOpenid_shouldPass() {
        try (MockedStatic<HttpUtil> httpUtilMock = mockStatic(HttpUtil.class)) {
            when(valueOperations.get("wechat:access_token")).thenReturn("test-token");
            httpUtilMock.when(() -> HttpUtil.doPost(anyString(), anyString()))
                    .thenReturn("{\"errcode\":0,\"errmsg\":\"ok\"}");

            assertTrue(contentAuditService.auditText("正常内容"));
        }
    }
}
