package com.kimchi;

import com.kimchi.entity.Recipe;
import com.kimchi.service.RecipeService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.SpringApplication;

import java.util.List;

@SpringBootApplication
public class KimchiApplication {

	public static void main(String[] args) {
		SpringApplication.run(KimchiApplication.class, args);
	}

	@Bean
	public CommandLineRunner runOnStartup(RecipeService recipeService) {
		return args -> {
			System.out.println("[Startup] Fetching all recipes...");
			List<Recipe> allRecipes = recipeService.fetchAllRecipes();
			if (allRecipes.isEmpty()) {
				System.out.println("No recipes fetched from the API.");
			} else {
				System.out.println("Total recipes fetched: " + allRecipes.size());
				recipeService.saveRecipesToJson(allRecipes, "recipes.json");
				System.out.println("[Startup] Saved all recipes to recipes.json");
			}
		};
	}
}
