package com.example.recipes_project.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes_project.R;
import com.example.recipes_project.adapters.RecipeAdapter;
import com.example.recipes_project.models.Recipes;
import com.example.recipes_project.services.DataService;

import java.util.ArrayList;

public class OnlineRecipes_Fragment extends Fragment {

    private RecyclerView recyclerView;
    private RecipeAdapter adapter;
    private ArrayList<Recipes> recipeList;
    private LinearLayoutManager layoutManager;
    private DataService dataService;
    private int currentPage = 0;
    private boolean isLoading = false; // To avoid multiple load requests at once

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_online_recipes_, container, false);

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recipesRecyclerView);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        // Initialize dataService and recipes list
        dataService = new DataService();
        recipeList = new ArrayList<>();

        // Set up the adapter
        adapter = new RecipeAdapter(recipeList);
        recyclerView.setAdapter(adapter);

        // Load the initial data
        loadRecipes();

        // Set up OnScrollListener to detect when the user scrolls to the bottom
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && (visibleItemCount + pastVisibleItems) >= totalItemCount) {
                    // Load more data when the user reaches the bottom
                    loadMoreData();
                }
            }
        });

        return view;
    }

    // load more becuz each time the api gives limited number of recipies
    private void loadRecipes() {
        ArrayList<Recipes> newRecipes = dataService.getArrRecipes(currentPage);
        recipeList.addAll(newRecipes);
        adapter.notifyDataSetChanged();
    }

    private void loadMoreData() {
        isLoading = true; // Prevent further loads
        currentPage++; // Move to the next page
        loadRecipes(); // Load more data
        isLoading = false; // Allow further loads
    }
}
