package com.myproject.movie_ticket.domain.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortOneService {

    @Value("${portone.api-secret}")
    private String apiSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    public Integer getPaymentAmount(String paymentId) {
        String url ="https://api.portone.io/payments/"+paymentId;

        HttpHeaders headers = new HttpHeaders();

        headers.set("Authorization", "PortOne "+apiSecret);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET,
                    entity, Map.class);
            Map<String, Object> body = response.getBody();

            Map<String, Object> amountInfo = (Map<String, Object>) body.get("amount");
            return (Integer) amountInfo.get("total");
        } catch (Exception e) {
            log.error("포트원 결제 조회 실패 paymentId: {}", paymentId, e);
            throw new IllegalArgumentException("결제 내역 조회에 실패했습니다.");
        }
    }
    public void cancelPayment(String paymentId, String reason) {
        String url = "https://api.portone.io/payments/"+paymentId+"/cancel";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "PortOne "+apiSecret);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("reason", reason);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

        try {
            restTemplate.postForEntity(url, entity, Map.class);
            log.warn("비정상 결제 감지 자동 환불 완료. paymentId: {}", paymentId);
        } catch (Exception e) {
            log.error("자동 환불 실패 paymentIdL {}", paymentId, e);
        }
    }
}
