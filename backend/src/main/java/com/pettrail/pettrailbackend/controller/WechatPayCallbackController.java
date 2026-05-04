package com.pettrail.pettrailbackend.controller;

import com.pettrail.pettrailbackend.dto.OrderVO;
import com.pettrail.pettrailbackend.service.WechatPayService;
import com.pettrail.pettrailbackend.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/pay")
@RequiredArgsConstructor
@Tag(name = "微信支付回调", description = "微信支付通知回调接口")
public class WechatPayCallbackController {

    private final WechatPayService wechatPayService;
    private final OrderService orderService;

    @PostMapping(value = "/wechat/callback", produces = MediaType.APPLICATION_XML_VALUE)
    @Operation(summary = "微信支付结果通知回调")
    public String wechatPayCallback(HttpServletRequest request) {
        String body = readRequestBody(request);
        log.info("[微信支付回调] 收到通知: {}", body);

        try {
            Map<String, String> params = parseXml(body);

            if (!wechatPayService.verifyCallback(params)) {
                log.warn("[微信支付回调] 签名验证失败");
                return buildFailResponse("签名验证失败");
            }

            String returnCode = params.get("return_code");
            String resultCode = params.get("result_code");

            if (!"SUCCESS".equals(returnCode) || !"SUCCESS".equals(resultCode)) {
                log.warn("[微信支付回调] 支付失败: return_code={}, result_code={}", returnCode, resultCode);
                return buildSuccessResponse();
            }

            String orderNo = params.get("out_trade_no");
            String wxTransactionId = params.get("transaction_id");

            if (orderNo == null || orderNo.isEmpty()) {
                log.warn("[微信支付回调] 缺少out_trade_no");
                return buildFailResponse("缺少订单号");
            }

            OrderVO result = orderService.handlePayCallback(orderNo, wxTransactionId);
            log.info("[微信支付回调] 处理成功: orderNo={}, status={}", orderNo, result.getStatus());

            return buildSuccessResponse();
        } catch (Exception e) {
            log.error("[微信支付回调] 处理异常", e);
            return buildFailResponse("处理异常");
        }
    }

    private String readRequestBody(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (Exception e) {
            log.error("[微信支付回调] 读取请求体失败", e);
        }
        return sb.toString();
    }

    private Map<String, String> parseXml(String xml) {
        Map<String, String> params = new HashMap<>();
        if (xml == null || xml.isEmpty()) {
            return params;
        }
        javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        try {
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
            org.w3c.dom.Document doc = builder.parse(new org.xml.sax.InputSource(new java.io.StringReader(xml)));
            org.w3c.dom.NodeList nodeList = doc.getDocumentElement().getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                org.w3c.dom.Node node = nodeList.item(i);
                if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    params.put(node.getNodeName(), node.getTextContent());
                }
            }
        } catch (Exception e) {
            log.error("[微信支付回调] XML解析失败", e);
        }
        return params;
    }

    private String buildSuccessResponse() {
        return "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
    }

    private String buildFailResponse(String msg) {
        return "<xml><return_code><![CDATA[FAIL]]></return_code><return_msg><![CDATA[" + msg + "]]></return_msg></xml>";
    }
}
