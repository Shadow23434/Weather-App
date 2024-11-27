package com.example.weatherapp.profile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.weatherapp.R;
import com.example.weatherapp.activities.AccountActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePassword extends AppCompatActivity {
    private FirebaseAuth fAuth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_password);
        EditText oldPassword = findViewById(R.id.oldPass);
        EditText newPassword = findViewById(R.id.newPass);
        EditText rePassword = findViewById(R.id.reNewPass);
        Button cfChange = findViewById(R.id.updatePass);
        fAuth = FirebaseAuth.getInstance();
        cfChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldPass = oldPassword.getText().toString();
                String newPass = newPassword.getText().toString();
                String re_newPass = rePassword.getText().toString();
                if (!oldPass.isEmpty() && !newPass.isEmpty() && !re_newPass.isEmpty()) {
                    if (!oldPass.equals(newPass)) {
                        if (newPass.length() < 6) {
                            Toast.makeText(ChangePassword.this, "Password must be 6 character", Toast.LENGTH_SHORT).show();
                        } else {
                            if (newPass.equals(re_newPass)) {
                                updatePassword(oldPass, newPass);
                            } else {
                                Toast.makeText(ChangePassword.this, "New password not matching!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        Toast.makeText(ChangePassword.this, "The new password must be different from the old password", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ChangePassword.this, "Please enter all the filed", Toast.LENGTH_SHORT).show();

                }
            }
        });

    }

    private void updatePassword(String oldPass, String newPass) {
        user = fAuth.getCurrentUser();
        if(user != null){
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPass);
            user.reauthenticate(credential).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {

                    user.updatePassword(newPass).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            Toast.makeText(ChangePassword.this, "Updated Password", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ChangePassword.this, AccountActivity.class);
                            startActivity(intent);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ChangePassword.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(ChangePassword.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }
}



