package com.pettrail.pettrailbackend.websocket;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.pettrail.pettrailbackend.service.NotificationService;
import com.pettrail.pettrailbackend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    private final NotificationService notificationService;
    private final JwtUtil jwtUtil;

    private static final ConcurrentHashMap<Long, WebSocketSession> USER_SESSIONS = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = extractUserId(session);
        if (userId == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }

        WebSocketSession oldSession = USER_SESSIONS.put(userId, session);
        if (oldSession != null && oldSession.isOpen()) {
            try {
                oldSession.close(CloseStatus.NORMAL);
            } catch (IOException e) {
                log.warn("关闭旧WebSocket连接失败: userId={}", userId);
            }
        }

        session.getAttributes().put("userId", userId);
        log.info("WebSocket连接建立: userId={}", userId);

        try {
            int unreadCount = notificationService.getUnreadCount(userId);
            List<?> systemMsgs = notificationService.getNotifications(userId, 1, 5, "system");
            Map<String, Object> initData = Map.of(
                "type", "init",
                "unreadCount", unreadCount,
                "systemMessages", systemMsgs
            );
            sendMessage(session, initData);
        } catch (Exception e) {
            log.warn("发送初始数据失败: userId={}, error={}", userId, e.getMessage());
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long userId = extractUserId(session);
        if (userId == null) return;

        try {
            JSONObject payload = JSON.parseObject(message.getPayload());
            String action = payload.getString("action");

            if ("ping".equals(action)) {
                sendMessage(session, Map.of("type", "pong"));
            } else if ("getUnreadCount".equals(action)) {
                int count = notificationService.getUnreadCount(userId);
                sendMessage(session, Map.of("type", "unreadCount", "count", count));
            }
        } catch (Exception e) {
            log.warn("处理WebSocket消息失败: userId={}, error={}", userId, e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            USER_SESSIONS.remove(userId, session);
            log.info("WebSocket连接关闭: userId={}, status={}", userId, status);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        Long userId = (Long) session.getAttributes().get("userId");
        log.warn("WebSocket传输错误: userId={}, error={}", userId, exception.getMessage());
        if (session.isOpen()) {
            try {
                session.close(CloseStatus.SERVER_ERROR);
            } catch (IOException e) {
                log.warn("关闭WebSocket失败: userId={}", userId);
            }
        }
    }

    public void sendToUser(Long userId, Map<String, Object> data) {
        WebSocketSession session = USER_SESSIONS.get(userId);
        if (session != null && session.isOpen()) {
            sendMessage(session, data);
        }
    }

    public void notifyNewNotification(Long userId, String notifyType, String content, Long targetId) {
        Map<String, Object> data = Map.of(
            "type", "notification",
            "notifyType", notifyType,
            "content", content != null ? content : "",
            "targetId", targetId != null ? targetId : 0
        );
        sendToUser(userId, data);
    }

    public void notifyUnreadCount(Long userId) {
        try {
            int count = notificationService.getUnreadCount(userId);
            Map<String, Object> data = Map.of(
                "type", "unreadCount",
                "count", count
            );
            sendToUser(userId, data);
        } catch (Exception e) {
            log.warn("发送未读数失败: userId={}", userId);
        }
    }

    public boolean isUserOnline(Long userId) {
        WebSocketSession session = USER_SESSIONS.get(userId);
        return session != null && session.isOpen();
    }

    private void sendMessage(WebSocketSession session, Map<String, Object> data) {
        try {
            String json = JSON.toJSONString(data);
            session.sendMessage(new TextMessage(json));
        } catch (Exception e) {
            log.warn("发送WebSocket消息失败: error={}", e.getMessage());
        }
    }

    private Long extractUserId(WebSocketSession session) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) return userId;

        try {
            URI uri = session.getUri();
            if (uri == null) return null;

            String query = uri.getQuery();
            if (query == null) return null;

            for (String param : query.split("&")) {
                String[] kv = param.split("=", 2);
                if ("token".equals(kv[0]) && kv.length == 2) {
                    String token = kv[1];
                    if (jwtUtil.validateToken(token)) {
                        userId = jwtUtil.getUserIdFromToken(token);
                        session.getAttributes().put("userId", userId);
                        return userId;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("提取用户ID失败: {}", e.getMessage());
        }
        return null;
    }
}
