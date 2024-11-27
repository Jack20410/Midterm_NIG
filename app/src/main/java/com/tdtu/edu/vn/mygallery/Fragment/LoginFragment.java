package com.tdtu.edu.vn.mygallery.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
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
import com.google.firebase.auth.FirebaseUser;
import com.tdtu.edu.vn.mygallery.OnlineActivity;
import com.tdtu.edu.vn.mygallery.R;

public class LoginFragment extends Fragment {

    private FirebaseAuth mAuth;
    private EditText emailField, passwordField;
    private Button loginButton, registerButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activity_login, container, false);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // Redirect to OnlineActivity if already logged in
            redirectToOnlineActivity();
        }

        emailField = view.findViewById(R.id.emailField);
        passwordField = view.findViewById(R.id.passwordField);
        loginButton = view.findViewById(R.id.loginButton);
        registerButton = view.findViewById(R.id.registerButton);

        loginButton.setOnClickListener(v -> loginUser());
        registerButton.setOnClickListener(v -> registerUser());

        return view;
    }

    private void loginUser() {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        if (!validateInputs(email, password)) return;

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show();
                        redirectToOnlineActivity();
                    } else {
                        Toast.makeText(requireContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void registerUser() {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        if (!validateInputs(email, password)) return;

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(requireContext(), "Registration successful!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean validateInputs(String email, String password) {
        if (email.isEmpty()) {
            emailField.setError("Email is required");
            emailField.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailField.setError("Enter a valid email");
            emailField.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            passwordField.setError("Password is required");
            passwordField.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            passwordField.setError("Password must be at least 6 characters");
            passwordField.requestFocus();
            return false;
        }

        return true;
    }

    private void redirectToOnlineActivity() {
        Intent intent = new Intent(requireContext(), OnlineActivity.class);
        startActivity(intent);
        requireActivity().finish(); // Prevent back navigation to LoginFragment
    }
}
