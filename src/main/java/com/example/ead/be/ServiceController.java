package com.example.ead.be;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
public class ServiceController {

	@Autowired
	private Environment env;

	private Persistence p;

	//This method is needed because the application.properties is read AFTER all injections have happened.
	@PostConstruct
	private void postConstruct() {
		System.out.println("******************************************************");
		System.out.println("******************************************************");
		System.out.println("******************************************************");
		System.out.println("env mongoUri:"+env.getProperty("databaseUrl"));
		System.out.println("env dbName:"+env.getProperty("databaseName"));
		System.out.println("env dbName:"+env.getProperty("databaseCollection"));
		System.out.println("******************************************************");
		System.out.println("******************************************************");
		System.out.println("******************************************************");

		 p = new Persistence(env.getProperty("databaseUrl"),
				env.getProperty("databaseName"), env.getProperty("databaseCollection"));
	}

	@GetMapping("/")
	public String index() {
		p.main2();
		return "Greetings from EAD CA2 Template project 2023-24!";
	}

	@GetMapping("/recipes")
	public List<Recipe> getAllRecipes()
	{
		System.out.println("About to get all the recipes in MongoDB!");
		return p.getAllRecipes();
	}

	@DeleteMapping("/recipe/{name}")
	private int deleteRecipe(@PathVariable("name") String name)
	{
		System.out.println("About to delete all the recipes named "+name);
		return p.deleteRecipesByName(Arrays.asList(name));
	}

	@PostMapping("/recipe")
	@ResponseStatus(HttpStatus.CREATED)
	public int saveRecipe(@RequestBody Recipe rec)
	{
		System.out.println("About to add the following recipe: "+rec);
		return p.addRecipes(Arrays.asList(rec));
	}

}
