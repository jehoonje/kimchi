package com.kimchi.service;

import com.kimchi.entity.Recipe;
import com.kimchi.repository.RecipeRepository;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 1) 식품안전나라 API 호출 → XML 데이터 수신
 * 2) XML 파싱 (jackson-dataformat-xml)
 * 3) 필요한 필드(한글) 추출 → 파파고로 영문 번역
 * 4) Recipe 엔티티에 담아 DB 저장
 * 5) getAllRecipes()로 조회 시 JSON 반환
 */
@Service
public class RecipeService {

    private final TranslationService translationService;
    private final RecipeRepository recipeRepository;

    // application.properties 에서
    @Value("${food.safety.key}")
    private String apiKey; // 식품안전나라 인증키
    @Value("${food.safety.serviceId}")
    private String serviceId; // COOKRCP01
    @Value("${food.safety.baseUrl}")
    private String baseUrl; // http://openapi.foodsafetykorea.go.kr/api

    public RecipeService(TranslationService translationService, RecipeRepository recipeRepository) {
        this.translationService = translationService;
        this.recipeRepository = recipeRepository;
    }

    /**
     * 식품안전나라 OpenAPI에서 특정 범위 레시피를 가져와 번역 후 DB에 저장
     */
    @Transactional
    public void fetchAndStoreRecipes(int startIdx, int endIdx) {
        String xmlData = getXmlFromFoodSafety(startIdx, endIdx);

        // XML 파싱
        List<Map<String, Object>> rows = parseCookRcpXml(xmlData);
        if (rows == null || rows.isEmpty()) {
            return;
        }

        List<Recipe> recipeList = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            // row는 <row> 태그 내부 데이터를 key-value 로 담은 맵
            // 예) RCP_SEQ, RCP_NM, RCP_PARTS_DTLS, MANUAL01, ... 등

            Recipe recipe = new Recipe();

            // 일련번호
            Integer seq = parseInt(row.get("RCP_SEQ"));
            recipe.setRcpSeq(seq);

            // 메뉴 이름 (번역)
            String rcpNm = (String) row.get("RCP_NM");
            String nameEn = translationService.translateKoreanToEnglish(rcpNm);
            recipe.setName(nameEn);

            // 이미지 - ATT_FILE_NO_MK
            String image = (String) row.get("ATT_FILE_NO_MK");
            recipe.setImage(image);

            // 재료 (번역)
            String parts = (String) row.get("RCP_PARTS_DTLS");
            if (parts == null) parts = "";
            String partsEn = translationService.translateKoreanToEnglish(parts);
            recipe.setIngredients(partsEn);

            // manual01 ~ manual19 (번역)
            for (int i = 1; i <= 19; i++) {
                String key = String.format("MANUAL%02d", i);
                String val = (String) row.get(key);
                if (val == null) val = "";
                val = translationService.translateKoreanToEnglish(val);

                switch (i) {
                    case 1: recipe.setManual01(val); break;
                    case 2: recipe.setManual02(val); break;
                    case 3: recipe.setManual03(val); break;
                    // ... 필요 시 case 4~19
                    default: break;
                }
            }

            // TIP (번역)
            String tip = (String) row.get("RCP_NA_TIP");
            if (tip == null) tip = "";
            tip = translationService.translateKoreanToEnglish(tip);
            recipe.setTip(tip);

            recipeList.add(recipe);
        }

        // DB 저장
        recipeRepository.saveAll(recipeList);
    }

    /**
     * DB에 저장된 레시피 전부 조회 (예시)
     */
    @Transactional(readOnly = true)
    public List<Recipe> getAllRecipes() {
        return recipeRepository.findAll();
    }

    // ---------------- 내부 메서드들 --------------------

    /**
     * 식품안전나라 OpenAPI 호출하여 XML 문자열 받아오기
     */
    private String getXmlFromFoodSafety(int startIdx, int endIdx) {
        // 예: http://openapi.foodsafetykorea.go.kr/api/인증키/서비스명/xml/startIdx/endIdx
        // 파라미터 없이 단순 호출 (추가 조건 필요시 "&RCP_NM=~~" 등)
        String url = String.format("%s/%s/%s/xml/%d/%d",
                baseUrl, apiKey, serviceId, startIdx, endIdx);

        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(url, String.class);
    }

    /**
     * XML → jackson-dataformat-xml → Map 구조로 파싱
     * "COOKRCP01" 루트 내부의 <row> 목록을 List<Map> 형태로 반환
     */
    private List<Map<String, Object>> parseCookRcpXml(String xmlData) {
        try {
            XmlMapper xmlMapper = new XmlMapper();
            // 최상위 노드가 <COOKRCP01> 이므로, 일단 Map으로 파싱
            Map<String, Object> root = xmlMapper.readValue(xmlData, Map.class);

            // root 안에 <row>가 여러 개 있을 것 -> "row" 키에서 가져오기
            Object rowObj = root.get("row");
            if (rowObj instanceof List) {
                // row가 여러개
                return (List<Map<String, Object>>) rowObj;
            } else if (rowObj instanceof Map) {
                // row가 1개
                List<Map<String, Object>> singleList = new ArrayList<>();
                singleList.add((Map<String, Object>) rowObj);
                return singleList;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Object -> Integer 변환 헬퍼
     */
    private Integer parseInt(Object obj) {
        if (obj == null) return null;
        try {
            return Integer.parseInt(obj.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
