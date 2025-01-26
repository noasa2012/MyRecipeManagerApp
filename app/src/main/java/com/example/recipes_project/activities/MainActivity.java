package com.example.recipes_project.activities;
import com.example.recipes_project.R;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.Navigation;

import com.example.recipes_project.R;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
    }



    // Register method to create a new user
    public void register() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        String email = ((EditText) findViewById(R.id.email)).getText().toString();
        String password = ((EditText) findViewById(R.id.pass)).getText().toString();

        // Check if email and password are not empty
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(MainActivity.this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new user with Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Registration successful
                            Toast.makeText(MainActivity.this, "Registration successful", Toast.LENGTH_LONG).show();



                            // Get the user ID of the newly registered user
                            String userId = task.getResult().getUser().getUid();

                            // Initialize the user's data in Firebase Database
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference usersReference = database.getReference("users");

                            // Default data for the new user
                            HashMap<String, Object> defaultData = new HashMap<>();
                            defaultData.put("uploadedList", new ArrayList<>()); // Empty list
                            defaultData.put("Mylist", new ArrayList<>()); // Empty list
                            defaultData.put("favList", new ArrayList<>()); // Empty list

                            // Save default data in Firebase Database
                            usersReference.child(userId).setValue(defaultData)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(MainActivity.this, "User initialized in database", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(MainActivity.this, "Failed to initialize user: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    });

                            navController.navigate(R.id.action_signIn_Fragment_to_login_Fragment);



                            // noa we should add case if reg is failed




                        } else {
                            // Registration failed
                            Toast.makeText(MainActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    // Login method to sign in an existing user
    public void login() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);


        String email = ((EditText) findViewById(R.id.email)).getText().toString();
        String password = ((EditText) findViewById(R.id.passlog)).getText().toString();

        // Check if email and password are not empty
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(MainActivity.this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Sign in the user with Firebase Authentication
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {


                    @Override
                    public void onComplete(Task<AuthResult> task) {
                       if (task.isSuccessful()) {
                            // Login successful

                            Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_LONG).show();
                           navController.navigate(R.id.action_login_Fragment_to_menu_Fragment);


                       } else {
                            // Login failed
                            Toast.makeText(MainActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                       }
                    }

                });
    }
}