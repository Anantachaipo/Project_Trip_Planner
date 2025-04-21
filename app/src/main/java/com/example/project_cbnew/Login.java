package com.example.project_cbnew;

import com.google.android.libraries.places.api.Places;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Login extends AppCompatActivity {

    TextInputEditText textInputEmail, textInputPassword;
    Button login, createAccount;

    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance(); // Firestore Database

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        // ✅ Initialize Places SDK
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyArFeH3SFY8MUXxuoRjr4i-T5LQkn5j6qo");
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.ImageUser), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        textInputEmail = findViewById(R.id.email);
        textInputPassword = findViewById(R.id.password);
        login = findViewById(R.id.buttonLogin);
        createAccount = findViewById(R.id.buttonCreate);

        // ไปที่หน้า Create Account
        createAccount.setOnClickListener(v -> {
            startActivity(new Intent(Login.this, Create_account.class));
            finish();
        });

        // Login
        login.setOnClickListener(v -> {
            String email = textInputEmail.getText().toString().trim();
            String password = textInputPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(Login.this, "Enter Email", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(Login.this, "Invalid Email Format", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(Login.this, "Enter Password", Toast.LENGTH_SHORT).show();
                return;
            }

            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String userId = firebaseAuth.getCurrentUser().getUid();
                            getUserData(userId); // ดึงข้อมูลจาก Firestore
                        } else {
                            Toast.makeText(Login.this, "Authentication Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    // ฟังก์ชันดึงข้อมูลจาก Firestore Collection "customer"
    private void getUserData(String userId) {
        db.collection("customer").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        DocumentSnapshot document = task.getResult();
                        String email = document.getString("email");
                        String password = document.getString("password");
                        String role = document.getString("role"); // ✅ Get role

                        Log.d("Firestore", "User Data: " + document.getData());

                        if (role != null) {
                            switch (role) {
                                case "admin":
                                    startActivity(new Intent(Login.this, Home_Page_Manager.class));
                                    break;
                                case "driver":
                                    startActivity(new Intent(Login.this, Home_Page_Driver.class));
                                    break;
                                default:
                                    startActivity(new Intent(Login.this, Home_Page.class)); // customer
                            }
                            finish();
                        } else {
                            Toast.makeText(Login.this, "Invalid role assigned.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(Login.this, "User Data Not Found", Toast.LENGTH_SHORT).show();
                        Log.e("Firestore", "Error: ", task.getException());
                    }
                });
    }

}

