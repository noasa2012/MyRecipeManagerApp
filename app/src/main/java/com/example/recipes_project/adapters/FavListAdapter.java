package com.example.recipes_project.adapters;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class FavListAdapter extends RecyclerView.Adapter<FavListAdapter.ViewHolder> {

    private ArrayList<Recipes> favList;
    private Context context;

    public FavListAdapter(ArrayList<Recipes> favList) {
        this.favList = favList;
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
        Recipes recipe = favList.get(position);

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

        // Long click listener to show delete confirmation dialog
        holder.itemView.setOnLongClickListener(v -> {
            // Show delete option on long click
            showDeleteDialog(position, recipe);
            return true; // Consume the long click event
        });
    }

    @Override
    public int getItemCount() {
        return favList.size();
    }

    // Function to show a dialog for confirming deletion

    private void showDeleteDialog(int position, Recipes recipe) {
        // Confirm deletion and remove item from the list and Firebase
        new android.app.AlertDialog.Builder(context)
                .setMessage("Do you want to remove this item from your favorites?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Get the current user ID
                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference favListRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("favList");

                    // Get the Firebase key of the recipe
                    String recipeKey = recipe.getFirebaseKey();

                    // Remove the recipe from Firebase using its unique key
                    favListRef.child(recipeKey).removeValue()
                            .addOnSuccessListener(aVoid -> {
                                // Remove item from local list (favList) and update RecyclerView
                                favList.remove(position);
                                notifyItemRemoved(position);
                                Toast.makeText(context, "Removed from favorites", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> Toast.makeText(context, "Failed to remove from favorites", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }



    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView recipeImage;
        TextView recipeTitle, instructions;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeImage = itemView.findViewById(R.id.recipeImage);
            recipeTitle = itemView.findViewById(R.id.recipeTitle);
            instructions = itemView.findViewById(R.id.recipeSummary);
        }
    }
}
