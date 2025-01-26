package com.example.recipes_project.adapters;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.recipes_project.R;
import com.example.recipes_project.models.Recipes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    private ArrayList<Recipes> recipeList;
    private ArrayList<Recipes> favList = new ArrayList<>();
    private Context context;

    public RecipeAdapter(ArrayList<Recipes> recipeList) {
        this.recipeList = recipeList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.cardview_recipe, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recipes recipe = recipeList.get(position);

        // Bind data to the views
        holder.recipeTitle.setText(recipe.getName());
        holder.instructions.setText(recipe.getAnalyzedInstructions());
        Glide.with(context).load(recipe.getImage()).into(holder.recipeImage);

        // Handle Recipe Item Click (Show Details in a Dialog)
        holder.itemView.setOnClickListener(v -> {
            // Create a new dialog to show recipe details
            Dialog recipeDialog = new Dialog(context);
            recipeDialog.setContentView(R.layout.dialog_recipe_details);
            recipeDialog.setCancelable(true);

            // Get views from dialog
            ImageView recipeImageDialog = recipeDialog.findViewById(R.id.dialogRecipeImage);
            TextView recipeTitleDialog = recipeDialog.findViewById(R.id.dialogRecipeTitle);
            TextView instructionsDialog = recipeDialog.findViewById(R.id.dialogRecipeSummary);

            // Set the data in the dialog
            Glide.with(context).load(recipe.getImage()).into(recipeImageDialog);
            recipeTitleDialog.setText(recipe.getName());
            instructionsDialog.setText(recipe.getAnalyzedInstructions());  // Set the analyzed instructions

            // Show the dialog
            recipeDialog.show();
        });

        // Long click listener to add to favorites
        holder.itemView.setOnLongClickListener(v -> {
            // Add recipe to the favorite list and update in Firebase
            addToFavorites(recipe);
            return true; // Consume the long click event
        });
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    public ArrayList<Recipes> getFavList() {
        return favList;
    }

    private void addToFavorites(Recipes recipe) {
        // Get the current user ID from Firebase Authentication
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Reference to the user's favorite list in Firebase
        DatabaseReference favListRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("favList");

        // Add the recipe to Firebase
        favListRef.push().setValue(recipe)
                .addOnSuccessListener(aVoid -> {
                    // Add the recipe to the local favList in the adapter
                    favList.add(recipe);
                    notifyDataSetChanged();  // Refresh the list
                    Toast.makeText(context, "Added to favorites!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Failed to add to favorites", Toast.LENGTH_SHORT).show());
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView recipeImage;
        TextView recipeTitle, instructions;
        ImageButton heartButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeImage = itemView.findViewById(R.id.recipeImage);
            recipeTitle = itemView.findViewById(R.id.recipeTitle);
            instructions = itemView.findViewById(R.id.recipeSummary);
            heartButton = itemView.findViewById(R.id.heartButton);
        }
    }
}
