package com.example.recipes_project.models;

public class UploadedImage {
    private String firebaseKey;
  //  private String imageUrl;
    private String imageBase64;


    public UploadedImage() {
        // Default constructor required for calls to DataSnapshot.getValue(UploadedImage.class)
    }

    public UploadedImage(String firebaseKey, String imageBase64) {
        this.firebaseKey = firebaseKey;
        this.imageBase64 = imageBase64;

    }

    public String getFirebaseKey() {
        return firebaseKey;
    }

    public void setFirebaseKey(String firebaseKey) {
        this.firebaseKey = firebaseKey;
    }




    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }
}
