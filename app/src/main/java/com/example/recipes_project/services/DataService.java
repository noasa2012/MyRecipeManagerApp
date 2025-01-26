package com.example.recipes_project.services;

import android.os.StrictMode;
import com.example.recipes_project.models.Recipes;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class DataService {

    private ArrayList<Recipes> arrRecipes = new ArrayList<>();
    private static final String API_KEY = "d29ff10c8b9441ef8fec12578002c7db";
    private static final String BASE_URL = "https://api.spoonacular.com/recipes/complexSearch?apiKey=" + API_KEY + "&instructionsRequired=true&addRecipeInformation=true&number=20";

    // Function to load recipes based on page number
    public ArrayList<Recipes> getArrRecipes(int page) {
        String sURL = BASE_URL + "&offset=" + (page * 20); // The API provides 20 items per page, so we calculate the offset
        URL url = null;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            url = new URL(sURL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        try {
            HttpURLConnection request = (HttpURLConnection) url.openConnection();
            request.connect();

            JsonParser jp = new JsonParser();
            JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent()));

            JsonObject jsonObject = root.getAsJsonObject();
            JsonArray array = jsonObject.getAsJsonArray("results");

            for (JsonElement je : array) {
                JsonObject obj = je.getAsJsonObject();
                JsonElement title = obj.get("title");
                JsonElement image = obj.get("image");

                String titleS = title.toString().replace("\"", "").trim();
                String imageS = image.toString().replace("\"", "").trim();

                ArrayList<String> arrInstructions = new ArrayList<>();
                JsonElement JInstructions = obj.get("analyzedInstructions");

                if (JInstructions != null) {
                    JsonArray instructionsArray = JInstructions.getAsJsonArray();

                    for (JsonElement instructionElement : instructionsArray) {
                        JsonObject instructionObj = instructionElement.getAsJsonObject();

                        // Get the steps array for each instruction
                        JsonArray stepsArray = instructionObj.getAsJsonArray("steps");

                        for (JsonElement stepElement : stepsArray) {
                            JsonObject stepObj = stepElement.getAsJsonObject();

                            // Get step number and description
                            int stepNumber = stepObj.get("number").getAsInt();
                            String stepDescription = stepObj.get("step").getAsString();

                            // Get ingredients for this step
                            JsonArray ingredientsArray = stepObj.getAsJsonArray("ingredients");
                            StringBuilder ingredients = new StringBuilder();

                            for (JsonElement ingredientElement : ingredientsArray) {
                                JsonObject ingredientObj = ingredientElement.getAsJsonObject();
                                String ingredientName = ingredientObj.get("name").getAsString();
                                ingredients.append(ingredientName).append(" ");
                            }

                            // Format the instruction with ingredients
                            String instructionText = " Step " + stepNumber + ": " + stepDescription + "\n" + "\n";
                            instructionText += " Ingredients: " + ingredients.toString().trim() + "\n";
                            arrInstructions.add(instructionText);
                        }
                    }
                }

                StringBuilder finalInstructions = new StringBuilder();
                for (String instruction : arrInstructions) {
                    finalInstructions.append(instruction).append("\n");  // Add newline after each instruction
                }

// The finalInstructions.toString() will now contain all instructions as a single string
                String allInstructions = finalInstructions.toString();

// At this point, arrInstructions will contain all the steps and ingredients as required

                arrRecipes.add(new Recipes(titleS, allInstructions, imageS));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return arrRecipes;
    }
}
