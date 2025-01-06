package com.kimchi.service;

import com.kimchi.entity.ManualStep;
import com.kimchi.entity.Recipe;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.*;

@Service
public class RecipeService {

    // 전체 레시피 수 (실제 API 문서를 참고하여 정확히 설정)
    private static final int TOTAL_RECIPES = 1136;
    // 한 번에 가져올 레시피 수 (API에서 허용하는 최대 값으로 설정)
    private static final int BATCH_SIZE = 100;

    /**
     * 모든 레시피를 가져오는 메서드
     */
    public List<Recipe> fetchAllRecipes() {
        List<Recipe> allRecipes = new ArrayList<>();
        int start = 1;
        int end = BATCH_SIZE;

        while (start <= TOTAL_RECIPES) {
            // end가 총 레시피 수를 초과하지 않도록 조정
            if (end > TOTAL_RECIPES) {
                end = TOTAL_RECIPES;
            }

            System.out.println(String.format("Fetching recipes %d to %d...", start, end));
            List<Recipe> batchRecipes = fetchRecipesRange(start, end);

            if (batchRecipes != null && !batchRecipes.isEmpty()) {
                allRecipes.addAll(batchRecipes);
                System.out.println(String.format("Fetched %d recipes.", batchRecipes.size()));
            } else {
                System.out.println("No recipes fetched in this batch.");
            }

            // 다음 배치로 이동
            start += BATCH_SIZE;
            end += BATCH_SIZE;

            // (선택 사항) API 호출 사이에 지연을 추가하여 과도한 요청을 방지
            try {
                Thread.sleep(500); // 0.5초 지연
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Thread interrupted during sleep.");
            }
        }

        System.out.println(String.format("Total recipes fetched: %d", allRecipes.size()));
        return allRecipes;
    }

    /**
     * 특정 범위의 레시피를 가져오는 기존 메서드
     */
    public List<Recipe> fetchRecipesRange(int start, int end) {
        // API 키와 서비스 ID 설정 (properties에서 로드하도록 변경 권장)
        String apiKey = "5616cca20865497f9133"; // 실제 API 키로 변경
        String serviceId = "COOKRCP01";
        String baseUrl = "http://openapi.foodsafetykorea.go.kr/api";

        String url = String.format("%s/%s/%s/xml/%d/%d",
                baseUrl, apiKey, serviceId, start, end);

        RestTemplate restTemplate = new RestTemplate();
        String xmlData = restTemplate.getForObject(url, String.class);

        if (xmlData == null || xmlData.isEmpty()) {
            System.out.println("API 응답 데이터가 비어 있습니다.");
            return Collections.emptyList();
        }

        System.out.println("API 응답 데이터 길이: " + xmlData.length());
        if (xmlData.length() > 200) {
            System.out.println("API 응답 데이터 샘플: " + xmlData.substring(0, 200) + "...");
        }

        return parseRecipeXml(xmlData);
    }

    /**
     * XML 데이터를 파싱하여 Recipe 객체 리스트로 변환
     */
    private List<Recipe> parseRecipeXml(String xmlData) {
        List<Recipe> recipes = new ArrayList<>();
        if (xmlData == null) {
            System.out.println("API로부터 받은 XML 데이터가 null입니다.");
            return recipes;
        }
        try {
            XmlMapper xmlMapper = new XmlMapper();
            Map<String, Object> root = xmlMapper.readValue(xmlData, Map.class);

            Object rowObj = root.get("row");
            List<Map<String, Object>> rowList;
            if (rowObj instanceof List) {
                rowList = (List<Map<String, Object>>) rowObj;
            } else if (rowObj instanceof Map) {
                rowList = new ArrayList<>();
                rowList.add((Map<String, Object>) rowObj);
            } else {
                System.out.println("XML 데이터에서 'row' 요소를 찾을 수 없습니다.");
                return recipes;
            }

            System.out.println("'row' 요소 수: " + rowList.size());

            for (Map<String, Object> row : rowList) {
                recipes.add(mapToRecipe(row));
            }
        } catch (Exception e) {
            System.out.println("XML 파싱 중 오류 발생:");
            e.printStackTrace();
        }
        return recipes;
    }

    /**
     * row(Map)에 있는 데이터를 Recipe 객체로 변환
     */
    private Recipe mapToRecipe(Map<String, Object> row) {
        Recipe recipe = new Recipe();
        recipe.setRcpSeq(parseInt(row.get("RCP_SEQ")));
        recipe.setRcpNm((String) row.get("RCP_NM"));
        recipe.setRcpWay2((String) row.get("RCP_WAY2"));
        recipe.setRcpPat2((String) row.get("RCP_PAT2"));
        recipe.setRcpPartsDtls((String) row.get("RCP_PARTS_DTLS"));
        recipe.setHashTag((String) row.get("HASH_TAG"));
        recipe.setAttFileNoMain((String) row.get("ATT_FILE_NO_MAIN"));
        recipe.setAttFileNoMk((String) row.get("ATT_FILE_NO_MK"));
        recipe.setRcpNaTip((String) row.get("RCP_NA_TIP"));

        List<ManualStep> steps = new ArrayList<>();
        int idx = 0;
        for (int i = 1; i <= 20; i++) { // 필요에 따라 범위 조정
            String key = String.format("MANUAL%02d", i);
            Object valObj = row.get(key);
            if (valObj == null) continue;
            String content = valObj.toString().trim();
            if (!content.isEmpty()) {
                steps.add(new ManualStep(++idx, content));
            }
        }
        recipe.setManualSteps(steps);

        return recipe;
    }

    private Integer parseInt(Object obj) {
        if (obj == null) return null;
        try {
            return Integer.parseInt(obj.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 모든 레시피를 로컬 JSON 파일로 저장
     * @param recipes 저장할 레시피 목록
     * @param filename 저장할 파일 이름 (경로 포함 가능)
     */
    public void saveRecipesToJson(List<Recipe> recipes, String filename) {
        try {
            // 원하는 절대 경로를 지정 (사용자 환경에 맞게 수정)
            String directoryPath = "/Users/jehoon/Desktop/kimchi/output"; // 예시 절대 경로
            File directory = new File(directoryPath);

            // 디렉토리가 없으면 생성
            if (!directory.exists()) {
                boolean dirsCreated = directory.mkdirs();
                if (dirsCreated) {
                    System.out.println("디렉토리 생성 완료: " + directory.getAbsolutePath());
                } else {
                    System.out.println("디렉토리 생성 실패: " + directory.getAbsolutePath());
                    return; // 디렉토리 생성 실패 시 파일 저장 중단
                }
            } else {
                System.out.println("디렉토리 존재: " + directory.getAbsolutePath());
            }

            // 파일 저장
            File file = new File(directory, filename);
            ObjectMapper mapper = new ObjectMapper();
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, recipes);

            System.out.println("파일 성공적으로 저장됨: " + file.getAbsolutePath());
        } catch (Exception e) {
            System.out.println("JSON 파일 저장 중 오류 발생:");
            e.printStackTrace();
        }
    }
}
