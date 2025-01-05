package com.kimchi.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class TranslationService {

    @Value("${papago.client.id}")
    private String papagoClientId;

    @Value("${papago.client.secret}")
    private String papagoClientSecret;

    public String translateKoreanToEnglish(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text; // 아무 내용 없으면 그대로 반환
        }

        try {
            // 파파고 NMT API URL
            String url = "https://openapi.naver.com/v1/papago/n2mt";

            // RestTemplate 사용
            RestTemplate restTemplate = new RestTemplate();

            // HTTP Headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("X-Naver-Client-Id", papagoClientId);
            headers.set("X-Naver-Client-Secret", papagoClientSecret);

            // Body (source=ko, target=en, text=...)
            Map<String, String> bodyMap = new HashMap<>();
            bodyMap.put("source", "ko");
            bodyMap.put("target", "en");
            bodyMap.put("text", text);

            // Form Data 형식으로 보낼 것이므로 queryString 형태로 변환
            String requestBody = UriComponentsBuilder.newInstance()
                    .queryParam("source", "ko")
                    .queryParam("target", "en")
                    .queryParam("text", text)
                    .build().toString().substring(1);
            // substring(1)은 "?source=ko&target=en&text=..." 에서 ? 제거용

            HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

            // POST 요청
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );

            // 응답 파싱
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                // Papago 응답 구조: { 'message': { 'result': { 'translatedText': '...' } } }
                Map<String, Object> message = (Map<String, Object>) body.get("message");
                if (message != null) {
                    Map<String, Object> result = (Map<String, Object>) message.get("result");
                    if (result != null) {
                        String translatedText = (String) result.get("translatedText");
                        return translatedText;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 실패 시 원문 반환 (또는 다른 처리)
        return text;
    }
}
