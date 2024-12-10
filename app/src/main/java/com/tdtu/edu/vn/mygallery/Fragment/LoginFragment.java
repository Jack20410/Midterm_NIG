package com.tdtu.edu.vn.mygallery.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tdtu.edu.vn.mygallery.OnlineActivity;
import com.tdtu.edu.vn.mygallery.R;
import com.tdtu.edu.vn.mygallery.RegisterActivity;

public class LoginFragment extends Fragment {

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private EditText inputField, passwordField;
    private Button loginButton, registerButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activity_login, container, false);

        // Initialize Firebase and UI elements
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance("https://midtermnig-default-rtdb.firebaseio.com/")
                .getReference("users");

        inputField = view.findViewById(R.id.emailField); // Generic input field for username/email
        passwordField = view.findViewById(R.id.passwordField);
        loginButton = view.findViewById(R.id.loginButton);
        registerButton = view.findViewById(R.id.registerButton);

        // Login logic
        loginButton.setOnClickListener(v -> loginUser());

        // Navigate to RegisterActivity
        registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), RegisterActivity.class);
            startActivity(intent);
        });

        return view;
    }

    private void loginUser() {
        String input = inputField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        if (input.isEmpty()) {
            inputField.setError("Username or email is required");
            inputField.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            passwordField.setError("Password is required");
            passwordField.requestFocus();
            return;
        }

        if (input.contains("@")) {
            // Input is an email
            authenticateUser(input, password);
        } else {
            // Input is a username
            fetchEmailByUsername(input, password);
        }
    }

    private void fetchEmailByUsername(String username, String password) {
        // Check if user is authenticated
        if (mAuth.getCurrentUser() == null) {
            Log.e("LoginFragment", "User is not authenticated!");
            Toast.makeText(getContext(), "Please log in to continue.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Debugging log for query initiation
        Log.d("LoginFragment", "Querying database for username: " + username);

        usersRef.orderByChild("username").equalTo(username)
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Log.d("LoginFragment", "Username exists in database: " + username);

                            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                String email = userSnapshot.child("email").getValue(String.class);

                                if (email != null) {
                                    Log.d("LoginFragment", "Email retrieved: " + email);
                                    authenticateUser(email, password);
                                    return;
                                } else {
                                    Log.e("LoginFragment", "Email field is null for username: " + username);
                                    Toast.makeText(getContext(), "Email not found for this username.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            Log.e("LoginFragment", "No user found with username: " + username);
                            Toast.makeText(getContext(), "No user found with that username.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("LoginFragment", "Database query failed: " + error.getMessage());
                        Toast.makeText(getContext(), "Error fetching data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void authenticateUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String uid = mAuth.getCurrentUser().getUid();
                usersRef.child(uid).get().addOnCompleteListener(userTask -> {
                    if (userTask.isSuccessful()) {
                        String username = userTask.getResult().child("username").getValue(String.class);
                        if (username != null) {
                            Toast.makeText(getContext(), "Welcome, " + username + "!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getContext(), OnlineActivity.class);
                            intent.putExtra("username", username);
                            startActivity(intent);
                            requireActivity().finish();
                        }
                    }
                });
            } else {
                Toast.makeText(getContext(), "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}