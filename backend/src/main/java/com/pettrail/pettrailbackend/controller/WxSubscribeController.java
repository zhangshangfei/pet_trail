package com.pettrail.pettrailbackend.controller;

import com.pettrail.pettrailbackend.dto.Result;
import com.pettrail.pettrailbackend.service.SysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/wx-subscribe")
@RequiredArgsConstructor
@Tag(name = "微信订阅消息", description = "微信订阅消息模板配置")
public class WxSubscribeController extends BaseController {

    private final SysConfigService sysConfigService;

    @GetMapping("/templates")
    @Operation(summary = "获取订阅消息模板ID列表")
    public Result<Map<String, String>> getTemplateIds() {
        Map<String, String> templates = new LinkedHashMap<>();
        templates.put("checkin", getCfg("wx.subscribe.template.checkin"));
        templates.put("vaccine", getCfg("wx.subscribe.template.vaccine"));
        templates.put("parasite", getCfg("wx.subscribe.template.parasite"));
        return Result.success(templates);
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
