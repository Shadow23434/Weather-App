package com.example.weatherapp.SignIn_SignUp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.weatherapp.R;
import com.example.weatherapp.activity.MainActivity;
import com.example.weatherapp.database.createDatabase;

public class SignIn extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in);

        createDatabase MyDB = new createDatabase(this);
        EditText emailuser = findViewById(R.id.editEmailAddressSignIn);
        EditText password = findViewById(R.id.editPasswordSignIn);
        Button btnSignIn = findViewById(R.id.btnSignIn);
        Button btnSignUp = findViewById(R.id.btnSignUp);
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignIn.this, SignUp.class);
                startActivity(intent);
            }
        });
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailuser.getText().toString();
                String pass = password.getText().toString();

                if(email.isEmpty() || pass.isEmpty()){
                    Toast.makeText(SignIn.this, "Please enter all the fields", Toast.LENGTH_SHORT).show();
                }
                else {

                    if(MyDB.checkUser(email, pass)){
                        Toast.makeText(SignIn.this, "Sign In successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SignIn.this, MainActivity.class);
                        startActivity(intent);
                    }
                    else{
                        Toast.makeText(SignIn.this, "Please check email or password", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });
    }
}