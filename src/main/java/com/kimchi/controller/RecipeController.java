package com.kimchi.controller;

import com.kimchi.entity.Recipe;
import com.kimchi.service.RecipeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    /**
     * 예) /api/fetch?start=1&end=5
     * 식품안전나라에서 start~end 개수만큼 레시피 가져와 번역 후 DB에 저장
     */
    @GetMapping("/fetch")
    public String fetchRecipes(@RequestParam(defaultValue = "1") int start,
                               @RequestParam(defaultValue = "5") int end) {
        recipeService.fetchAndStoreRecipes(start, end);
        return "Fetched & Stored recipes: " + start + " to " + end;
    }

    /**
     * DB에 저장된 모든 레시피(영어) 조회
     */
    @GetMapping("/recipes")
    public List<Recipe> getAllRecipes() {
        return recipeService.getAllRecipes();
    }
}
