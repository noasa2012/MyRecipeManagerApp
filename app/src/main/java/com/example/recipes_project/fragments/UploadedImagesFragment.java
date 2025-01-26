package com.example.recipes_project.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes_project.R;
import com.example.recipes_project.adapters.UploadedImagesAdapter;
import com.example.recipes_project.models.UploadedImage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class UploadedImagesFragment extends Fragment {
    private RecyclerView recyclerView;
    private Button addButton;
    private UploadedImagesAdapter adapter;
    private ArrayList<UploadedImage> imageList = new ArrayList<>();
    private DatabaseReference uploadedListRef;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_uploaded_images, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        addButton = view.findViewById(R.id.addButton);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        uploadedListRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("uploadedList");

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new UploadedImagesAdapter(getContext(), imageList, this::deleteImage);
        recyclerView.setAdapter(adapter);

        loadImages();

        addButton.setOnClickListener(v -> openImagePicker());

        return view;
    }

    private void loadImages() {
        uploadedListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                imageList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    UploadedImage image = dataSnapshot.getValue(UploadedImage.class);
                    if (image != null) {
                        image.setFirebaseKey(dataSnapshot.getKey());
                        imageList.add(image);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load images", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1000);
    }


    // checkk if the upload was successful... not relevant becuz we work in marshmellow that doesnt need
    // on activity resault
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1000 && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            uploadImageToFirebase(selectedImage);
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        try {
            // Convert image to Bitmap becuz we cant use storage for free in the firebase so
            // we put the image in real time database
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), imageUri);

            // Convert Bitmap to Base64
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] byteArray = baos.toByteArray();
            String base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT);

            // Create an image object with the Base64 data
            UploadedImage uploadedImage = new UploadedImage(null, base64Image);

            // Save the image to Firebase Realtime Database
            String newKey = uploadedListRef.push().getKey();
            if (newKey != null) {
                uploadedImage.setFirebaseKey(newKey);
                uploadedListRef.child(newKey).setValue(uploadedImage)
                        .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Image uploaded successfully!", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Failed to save metadata: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            } else {
                Toast.makeText(getContext(), "Failed to generate key for metadata!", Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            Toast.makeText(getContext(), "Failed to convert image: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }





    private void deleteImage(UploadedImage image) {
        uploadedListRef.child(image.getFirebaseKey()).removeValue()
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Image deleted", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to delete", Toast.LENGTH_SHORT).show());
    }
}
