package com.example.weatherapp.startup;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.weatherapp.R;
import com.example.weatherapp.activities.MainActivity;
//import com.example.weatherapp.database.CreateDatabase;
import com.example.weatherapp.settings.BackgroundManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class SignIn extends AppCompatActivity {

    private ImageButton btnGoogle;
    private FirebaseAuth fAuth;
    private FirebaseDatabase database;
    private GoogleSignInClient mGoogleSignInClient;
    private DatabaseReference databaseReference;
    private static int RC_SIGN_IN = 9000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in);

//      Google Login
        btnGoogle = findViewById(R.id.btnGoogle);
        fAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleSignIn();
            }
        });
        FirebaseUser user = fAuth.getCurrentUser();

        if( user != null && user.isEmailVerified()){
            Intent intent = new Intent(SignIn.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
//      Login Email and Password
//        CreateDatabase MyDB = new CreateDatabase(this);
        EditText emailuser = findViewById(R.id.editEmailAddressSignIn);
        EditText password = findViewById(R.id.editPasswordSignIn);
        Button btnSignIn = findViewById(R.id.btnSignIn);
        Button btnSignUp = findViewById(R.id.btnSignUp);
        TextView resetpassword = findViewById(R.id.reset_password);

//      Reset Password
        resetpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText resetMail = new EditText(v.getContext());
                AlertDialog.Builder passwordResetdialog = new AlertDialog.Builder(v.getContext());
                passwordResetdialog.setTitle("Reset Password ?");
                passwordResetdialog.setMessage("Enter your email to received reset link!!!");
                passwordResetdialog.setView(resetMail);
                passwordResetdialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String mail = resetMail.getText().toString();
                        fAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(SignIn.this, "Reset link sent to your email!!!", Toast.LENGTH_SHORT).show();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(SignIn.this, "Error!! Reset link not sent to your email!!!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                passwordResetdialog.create().show();
            }
        });
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignIn.this, SignUp.class);
                startActivity(intent);
                finish();
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
                    fAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                FirebaseUser user = fAuth.getCurrentUser();
                                if(user != null && user.isEmailVerified()){
                                    Toast.makeText(SignIn.this, "Sign In is successfully!!!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(intent);
                                }
                                else {
                                    Toast.makeText(SignIn.this, "Error!! Please check Verification Email", Toast.LENGTH_SHORT).show();

                                }
                            }
                            else {
                                Toast.makeText(SignIn.this, "Please check email or password!!!", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

                }
            }
        });

        ImageView gif = findViewById(R.id.falling_stars1);
        Glide.with(this)
                .load(R.drawable.falling_stars)
                .into(gif);
    }

    //  Login database with google
    private void googleSignIn() {
        mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Intent intent = mGoogleSignInClient.getSignInIntent();
                    startActivityForResult(intent, RC_SIGN_IN);
                } else{
                    Log.d("Google", "Error sign in");
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuth(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuth(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        fAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser user = fAuth.getCurrentUser();
                            databaseReference = database.getReference("user");
                            databaseReference.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                    UserId da ton tai
                                    if(snapshot.exists()){
                                        Log.d("Firebase", "User da ton tai");
                                    } else {
                                        HashMap<String, String> map = new HashMap<>();
                                        map.put("id", user.getUid());
                                        map.put("name", user.getDisplayName());
                                        map.put("profile", "null");
                                        map.put("email", user.getEmail());
                                        database.getReference().child("user").child(user.getUid()).setValue(map);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.d("Firebase", "Failed to connect firebase");
                                }
                            });
                            Intent intent = new Intent(SignIn.this, MainActivity.class);
                            startActivity(intent);
                        }
                        else {
                            Toast.makeText(SignIn.this, "Something went wrong!!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}