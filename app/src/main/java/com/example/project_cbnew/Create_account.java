package com.example.project_cbnew;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.project_cbnew.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class Create_account extends AppCompatActivity {

    TextInputEditText textInputEmail, textInputPassword;
    ImageButton backToLogin;
    Button createAccount;
    TextInputEditText textInputName, textInputTel;


    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();  // เชื่อมต่อกับ Firestore

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_account);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.ImageUser), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        textInputEmail = findViewById(R.id.email);
        textInputPassword = findViewById(R.id.password);
        backToLogin = findViewById(R.id.backToLogin);
        createAccount = findViewById(R.id.goToLogin);
        TextView tvTerms = findViewById(R.id.tvTerms2);
        CheckBox checkboxAccept = findViewById(R.id.checkboxAccept);
        Button createAccount = findViewById(R.id.goToLogin);
        textInputName = findViewById(R.id.name);      // ช่องกรอกชื่อ
        textInputTel = findViewById(R.id.tel);        // ช่องกรอกเบอร์โทร



        tvTerms.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(Create_account.this);
            builder.setTitle("ข้อตกลงการใช้งาน");

            String termsText = "📋 ข้อตกลงการใช้งาน\n\n" +
                    "1. ผู้ใช้จะต้องกรอกข้อมูลที่ถูกต้องและครบถ้วน\n\n" +
                    "2. บริษัทสงวนสิทธิ์ในการปรับเปลี่ยนข้อมูลหรือยกเลิกบริการได้โดยไม่ต้องแจ้งให้ทราบล่วงหน้า\n\n" +
                    "3. ผู้ใช้ต้องไม่ใช้ระบบเพื่อกระทำการที่ผิดกฎหมายหรือขัดต่อจริยธรรม\n\n" +
                    "4. การใช้งานระบบนี้ถือว่าคุณยอมรับนโยบายความเป็นส่วนตัวของบริษัท\n\n" +
                    "หากคุณไม่ยอมรับข้อตกลง กรุณายกเลิกการสมัครใช้งาน";

            builder.setMessage(termsText);
            builder.setPositiveButton("ตกลง", (dialog, which) -> dialog.dismiss());
            builder.show();
        });

        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email, password, name, tel;
                email = String.valueOf(textInputEmail.getText()).trim();
                password = String.valueOf(textInputPassword.getText()).trim();
                name = String.valueOf(textInputName.getText()).trim();
                tel = String.valueOf(textInputTel.getText()).trim();

                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(Create_account.this, "Enter Name", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(tel)) {
                    Toast.makeText(Create_account.this, "Enter Tel", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(Create_account.this, "Enter Email", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(Create_account.this, "Invalid Email Format", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(Create_account.this, "Enter Password", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (password.length() < 6) {
                    Toast.makeText(Create_account.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!checkboxAccept.isChecked()) {
                    Toast.makeText(Create_account.this, "กรุณายอมรับข้อตกลงการใช้งานก่อน", Toast.LENGTH_SHORT).show();
                    return;
                }


                // สร้างบัญชีผู้ใช้ใน Firebase Authentication
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // ถ้าการสร้างบัญชีสำเร็จ เพิ่มข้อมูลลง Firestore
                                    String userId = firebaseAuth.getCurrentUser().getUid();  // UID ของผู้ใช้ที่สร้างบัญชี

                                    // ข้อมูลที่ต้องการเก็บใน Firestore
                                    User user = new User(email, password, name, tel);
                                    user.setUserId(userId); // ✅ เพิ่มบรรทัดนี้เพื่อไม่ให้ userId เป็น null


                                    // เก็บข้อมูลใน Firestore Collection "customer"
                                    db.collection("customer").document(userId)  // สร้าง document ตาม userId
                                            .set(user)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(Create_account.this, "Account Created Successfully!", Toast.LENGTH_SHORT).show();
                                                        startActivity(new Intent(Create_account.this, Login.class));
                                                        finish();
                                                    } else {
                                                        Toast.makeText(Create_account.this, "Failed to store user data in Firestore.", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                } else {
                                    Toast.makeText(Create_account.this, "Authentication Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        // กลับไปที่หน้าล็อกอิน
        backToLogin.setOnClickListener(v -> {
            startActivity(new Intent(Create_account.this, Login.class));
        });
    }
}

