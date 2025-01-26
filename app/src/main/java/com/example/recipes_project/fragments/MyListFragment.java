package com.example.recipes_project.fragments;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.recipes_project.R;
import com.example.recipes_project.adapters.MyListAdapter;
import com.example.recipes_project.models.Recipes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MyListFragment extends Fragment {
    private RecyclerView recyclerView;
    private MyListAdapter adapter;
    private List<Recipes> recipeList;
    private DatabaseReference database;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_list, container, false);

        recyclerView = view.findViewById(R.id.myListRecyclerView);
        Button addRecipeButton = view.findViewById(R.id.addRecipeButton);
        TextView randomIngredients = view.findViewById(R.id.randomIngredients);

        // Sample ingredients
        randomIngredients.setText("Ingredients available: Sugar, Flour, Butter");

        recipeList = new ArrayList<>();
        adapter = new MyListAdapter(getContext(), recipeList);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        database = FirebaseDatabase.getInstance().getReference("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("mylist");

        loadRecipes();

        addRecipeButton.setOnClickListener(v -> showAddRecipeDialog());

        return view;
    }

    private void loadRecipes() {
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                recipeList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Recipes recipe = dataSnapshot.getValue(Recipes.class);
                    recipeList.add(recipe);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MyListFragment", "Firebase error: ", error.toException());
            }
        });
    }

    private void showAddRecipeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_recipe, null);
        builder.setView(dialogView);

        EditText nameInput = dialogView.findViewById(R.id.recipeNameInput);
        EditText instructionsInput = dialogView.findViewById(R.id.recipeInstructionsInput);

        builder.setTitle("Add Recipe")
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = nameInput.getText().toString();
                    String instructions = instructionsInput.getText().toString();
                    int imageResId = R.drawable.generalrecipeimage;

                    Recipes newRecipe = new Recipes(name, instructions, String.valueOf(imageResId) );
                    String id = database.push().getKey();
                    newRecipe.setFirebaseKey(id);

                    database.child(id).setValue(newRecipe).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("MyListFragment", "Recipe added successfully.");
                        } else {
                            Log.e("MyListFragment", "Failed to add recipe", task.getException());
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
