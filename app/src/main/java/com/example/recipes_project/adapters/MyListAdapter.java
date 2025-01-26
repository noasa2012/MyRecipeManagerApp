package com.example.recipes_project.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes_project.R;
import com.example.recipes_project.models.Recipes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class MyListAdapter extends RecyclerView.Adapter<MyListAdapter.ViewHolder> {
    private List<Recipes> recipeList;
    private Context context;

    public MyListAdapter(Context context, List<Recipes> recipeList) {
        this.context = context;
        this.recipeList = recipeList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.cardview_recipe, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recipes recipe = recipeList.get(position);
        holder.recipeTitle.setText(recipe.getName());
        holder.recipeSummary.setText(recipe.getAnalyzedInstructions());
        holder.recipeImage.setImageResource(R.drawable.generalrecipeimage);

        // Handle item click for showing recipe details
        holder.itemView.setOnClickListener(v -> {
            showRecipeDetailsDialog(recipe);  // Show details on item click
        });

        // Handle item long click for editing or deleting
        holder.itemView.setOnLongClickListener(v -> {
            showEditOrDeleteDialog(recipe, position);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    // ViewHolder class
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView recipeTitle, recipeSummary;
        ImageView recipeImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeTitle = itemView.findViewById(R.id.recipeTitle);
            recipeSummary = itemView.findViewById(R.id.recipeSummary);
            recipeImage = itemView.findViewById(R.id.recipeImage);
        }
    }

    private void showEditOrDeleteDialog(Recipes recipe, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose Action")
                .setItems(new String[]{"Edit", "Delete"}, (dialog, which) -> {
                    if (which == 0) {
                        // Edit action
                        showEditDialog(recipe, position);
                    } else {
                        // Delete action
                        deleteItem(position, recipe);
                    }
                })
                .show();
    }

    private void showEditDialog(Recipes recipe, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit Recipe");

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_recipe, null);
        builder.setView(dialogView);

        EditText recipeNameInput = dialogView.findViewById(R.id.recipeNameInput);
        EditText recipeInstructionsInput = dialogView.findViewById(R.id.recipeInstructionsInput);

        recipeNameInput.setText(recipe.getName());
        recipeInstructionsInput.setText(recipe.getAnalyzedInstructions());

        builder.setPositiveButton("Save", (dialog, which) -> {
            String updatedName = recipeNameInput.getText().toString().trim();
            String updatedInstructions = recipeInstructionsInput.getText().toString().trim();

            if (updatedName.isEmpty() || updatedInstructions.isEmpty()) {
                Toast.makeText(context, "All fields must be filled!", Toast.LENGTH_SHORT).show();
                return;
            }

            recipe.setName(updatedName);
            recipe.setAnalyzedInstructions(updatedInstructions);

            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(userId)
                    .child("mylist");

            databaseReference.child(recipe.getFirebaseKey()).setValue(recipe).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(context, "Recipe updated!", Toast.LENGTH_SHORT).show();
                    recipeList.set(position, recipe);
                    notifyItemChanged(position);
                } else {
                    Toast.makeText(context, "Failed to update recipe.", Toast.LENGTH_SHORT).show();
                }
            });
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }


    private void deleteItem(int position, Recipes selectedItem) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .child("mylist");

        if (position < 0 || position >= recipeList.size()) {
            Log.e("DeleteItem", "Invalid position: " + position);
            return;
        }

        database.orderByChild("firebaseKey").equalTo(selectedItem.getFirebaseKey()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                        itemSnapshot.getRef().removeValue().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Ensure the item is removed from the list correctly
                                recipeList.remove(selectedItem);
                                notifyItemRemoved(position); // This will notify only the removed item
                                Log.d("DeleteItem", "Item deleted successfully.");
                            } else {
                                Log.e("DeleteItem", "Failed to delete item", task.getException());
                            }
                        });
                    }
                } else {
                    Log.e("DeleteItem", "Item not found in Firebase.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("DeleteItem", "Firebase error: ", error.toException());
            }
        });
    }

    private void showRecipeDetailsDialog(Recipes recipe) {
        // Create the dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Recipe Details");

        // Inflate your dialog layout
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_recipe_details, null);
        builder.setView(dialogView);

        // Get the views from the layout
        ImageView dialogRecipeImage = dialogView.findViewById(R.id.dialogRecipeImage);
        TextView dialogRecipeTitle = dialogView.findViewById(R.id.dialogRecipeTitle);
        TextView dialogRecipeSummary = dialogView.findViewById(R.id.dialogRecipeSummary);

        // Set the data in the dialog views
        dialogRecipeTitle.setText(recipe.getName());  // Set the recipe title
        dialogRecipeSummary.setText(recipe.getAnalyzedInstructions());  // Set the recipe instructions

        // Optional: If you have an image URL or resource, set it here
        dialogRecipeImage.setImageResource(R.drawable.generalrecipeimage);  // Replace with actual image if needed

        // Show the dialog
        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());  // Close button
        builder.create().show();
    }




}
