package com.example.recipes_project.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes_project.R;
import com.example.recipes_project.adapters.FavListAdapter;
import com.example.recipes_project.models.Recipes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FavFragment extends Fragment {

    private RecyclerView favRecyclerView;
    private ArrayList<Recipes> favList;
    private FavListAdapter favListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_fav, container, false);

        // Initialize RecyclerView
        favRecyclerView = view.findViewById(R.id.favRecyclerView);
        favRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        favList = new ArrayList<>();
        favListAdapter = new FavListAdapter(favList);
        favRecyclerView.setAdapter(favListAdapter);

        // Fetch the list of favorite recipes from Firebase
        loadFavList();

        return view;
    }

    private void loadFavList() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference favListRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("favList");

        favListRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                favList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Recipes recipe = snapshot.getValue(Recipes.class);
                    if (recipe != null) {
                        // Set the firebaseKey to the key of the snapshot
                        recipe.setFirebaseKey(snapshot.getKey());
                        favList.add(recipe);
                    }
                }
                favListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(getContext(), "Error loading data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
