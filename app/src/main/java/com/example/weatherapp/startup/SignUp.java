package com.example.weatherapp.startup;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.weatherapp.R;
//import com.example.weatherapp.database.CreateDatabase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SignUp extends AppCompatActivity {

    private FirebaseAuth fAuth;
    private FirebaseDatabase database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        fAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
//        CreateDatabase MyDB = new CreateDatabase(this);
        EditText edtFullName = findViewById(R.id.editFullNameSignUp);
        EditText edtEmail = findViewById(R.id.editEmailAddressSignUp);
        EditText edtPhoneNumber = findViewById(R.id.editPhoneNumber);
        EditText edtPassword = findViewById(R.id.editPasswordSignUp);
        EditText edtConfirmPassword = findViewById(R.id.editConfirmPassword);
        Button btnSignUp = findViewById(R.id.btnSignUp1);

//        if(fAuth.getCurrentUser() != null){
//            Intent intent = new Intent(SignUp.this, MainActivity.class);
//            startActivity(intent);
//            finish();
//        }
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String strFullName = edtFullName.getText().toString();
                String strEmail = edtEmail.getText().toString();
                String strPhoneNumber = edtPhoneNumber.getText().toString();
                String strPassword = edtPassword.getText().toString();
                String strCfPassword = edtConfirmPassword.getText().toString();

                if(strFullName.isEmpty() || strEmail.isEmpty() || strPhoneNumber.isEmpty() || strPassword.isEmpty()){
                    Toast.makeText(SignUp.this, "Please enter all the fields", Toast.LENGTH_SHORT).show();
                }
                else {
                    if(!strEmail.contains("@gmail.com")){
                        Toast.makeText(SignUp.this, "Incorrectly formatted email address", Toast.LENGTH_SHORT).show();
                    }
                    else if(strPhoneNumber.length() != 10 || !strPhoneNumber.startsWith("0")){
                        Toast.makeText(SignUp.this, "Incorrectly formatted phone number", Toast.LENGTH_SHORT).show();
                    } else if (strPassword.length() < 6) {
                        Toast.makeText(SignUp.this, "Your password is too sort. Please change another password", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        if(strCfPassword.equals(strPassword)){

                            fAuth.createUserWithEmailAndPassword(strEmail, strPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        FirebaseUser user = fAuth.getCurrentUser();
                                        user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Toast.makeText(SignUp.this, "Verification Email has been sent!!!", Toast.LENGTH_SHORT).show();
                                                Toast.makeText(SignUp.this, "Register Successfully!!!", Toast.LENGTH_SHORT).show();

                                                HashMap<String, String> map = new HashMap<>();
                                                map.put("id", user.getUid());
                                                map.put("name", strFullName);
                                                map.put("profile", user.getPhotoUrl().toString());
                                                map.put("email", strEmail);
                                                map.put("password", strPassword);
                                                database.getReference().child("user").child(user.getUid()).setValue(map);
                                                Intent intent = new Intent(SignUp.this, SignIn.class);
                                                startActivity(intent);
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(SignUp.this, "Error!! Verification Email not sent!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                    else {
                                        Toast.makeText(SignUp.this, "Error!!!" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }

                        else{
                            Toast.makeText(SignUp.this, "Password not matching", Toast.LENGTH_SHORT).show();
                        }

                    }
                }
            }
        });

        ImageView gif = findViewById(R.id.falling_stars2);
        Glide.with(this)
                .load(R.drawable.falling_stars)
                .into(gif);

    }
}