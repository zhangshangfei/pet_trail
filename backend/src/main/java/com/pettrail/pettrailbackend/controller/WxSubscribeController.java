package com.pettrail.pettrailbackend.controller;

import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.service.SysConfigService;
import com.pettrail.pettrailbackend.service.WxSubscribeAuthorizationService;
import com.pettrail.pettrailbackend.util.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/wx-subscribe")
@RequiredArgsConstructor
@Tag(name = "微信订阅消息", description = "微信订阅消息模板配置与授权管理")
public class WxSubscribeController extends BaseController {

    private final SysConfigService sysConfigService;
    private final WxSubscribeAuthorizationService authorizationService;

    @GetMapping("/templates")
    @Operation(summary = "获取订阅消息模板ID列表")
    public Result<Map<String, String>> getTemplateIds() {
        Map<String, String> templates = new LinkedHashMap<>();
        templates.put("checkin", getCfg("wx.subscribe.template.checkin"));
        templates.put("vaccine", getCfg("wx.subscribe.template.vaccine"));
        templates.put("parasite", getCfg("wx.subscribe.template.parasite"));
        templates.put("feeding", getCfg("wx.subscribe.template.feeding"));
        return Result.success(templates);
    }

    @PostMapping("/authorize")
    @Operation(summary = "记录用户订阅消息授权（前端授权成功后调用）")
    public Result<Void> recordAuthorization(@RequestBody Map<String, Object> body) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error("未登录");
        }

        String templateType = (String) body.get("templateType");
        if (templateType == null || templateType.isEmpty()) {
            return Result.error("模板类型不能为空");
        }

        Integer count = (Integer) body.get("count");
        if (count == null || count <= 0) {
            count = 1;
        }

        authorizationService.addCredits(userId, templateType, count);
        return Result.success(null);
    }

    @GetMapping("/credits")
    @Operation(summary = "获取当前用户各模板的授权积分")
    public Result<Map<String, Integer>> getCredits() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.error("未登录");
        }

        Map<String, Integer> credits = new LinkedHashMap<>();
        String[] types = {"checkin", "vaccine", "parasite", "feeding"};
        for (String type : types) {
            credits.put(type, authorizationService.getCredits(userId, type));
        }
        return Result.success(credits);
    }

    private String getCfg(String key) {
        try {
            String val = sysConfigService.getValue(key);
            return val != null ? val : "";
        } catch (Exception e) {
            return "";
        }
    }
}
