package com.example.recipes_project.models;

public class Recipes {

    private String name;
    private String analyzedInstructions;  // Change to a single string
 //   private String summary;
    private String image;

    private String firebaseKey; // New field to store the Firebase key


    public Recipes() {
    }

    public Recipes(String name, String analyzedInstructions, String image) {
        this.name = name;
        this.analyzedInstructions = analyzedInstructions;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAnalyzedInstructions() {
        return analyzedInstructions;
    }

    public void setAnalyzedInstructions(String analyzedInstructions) {
        this.analyzedInstructions = analyzedInstructions;
    }

  //  public String getSummary() {
    //    return summary;
   // }

  //  public void setSummary(String summary) {
   //     this.summary = summary;
  //  }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getFirebaseKey() {
        return firebaseKey;
    }

    public void setFirebaseKey(String firebaseKey) {
        this.firebaseKey = firebaseKey;
    }
}
