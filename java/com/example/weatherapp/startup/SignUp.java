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
import com.example.weatherapp.database.createDatabase;

public class SignUp extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        createDatabase MyDB = new createDatabase(this);
        EditText edtFullName = findViewById(R.id.editFullNameSignUp);
        EditText edtEmail = findViewById(R.id.editEmailAddressSignUp);
        EditText edtPhoneNumber = findViewById(R.id.editPhoneNumber);
        EditText edtPassword = findViewById(R.id.editPasswordSignUp);
        EditText edtConfirmPassword = findViewById(R.id.editConfirmPassword);
        Button btnSignUp = findViewById(R.id.btnSignUp1);


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
                    if(strPhoneNumber.length() != 10 || !strPhoneNumber.startsWith("0")){
                        Toast.makeText(SignUp.this, "Incorrectly formatted phone number", Toast.LENGTH_SHORT).show();
                    } else if (strPassword.length() < 6) {
                        Toast.makeText(SignUp.this, "Your password is too weak. Please change another password", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        if(strCfPassword.equals(strPassword)){
                            if(MyDB.checkEmail(strEmail)){
                                if(MyDB.addUser(strFullName, strEmail, strPhoneNumber, strPassword)){
                                    Toast.makeText(SignUp.this, "Register Successfully!!!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(SignUp.this, SignIn.class);
                                    startActivity(intent);
                                }
                                else{
                                    Toast.makeText(SignUp.this, "Register Failed!!!", Toast.LENGTH_SHORT).show();
                                }
                            }
                            else{
                                Toast.makeText(SignUp.this, "Email has been registered", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else{
                            Toast.makeText(SignUp.this, "Password not matching", Toast.LENGTH_SHORT).show();
                        }

                    }
                }
            }
        });


    }
}