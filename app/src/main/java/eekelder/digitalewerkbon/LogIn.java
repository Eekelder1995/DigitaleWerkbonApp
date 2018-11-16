package eekelder.digitalewerkbon;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LogIn extends AppCompatActivity {

    private Button logInButton;
    private ProgressBar loginProgress;

    private EditText userNameField;
    private EditText passwordField;
    private static final String employeeCollection = "Employees";
    private static final String passwordDocument = "password";


    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection(employeeCollection);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        logInButton = findViewById(R.id.button_login);
        loginProgress = findViewById(R.id.progressBar);
        loginProgress.setVisibility(View.INVISIBLE);

        userNameField = findViewById(R.id.edittext_username);
        passwordField = findViewById(R.id.edittext_password);

        logInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginProgress.setVisibility(View.VISIBLE);
                logInButton.setVisibility(View.INVISIBLE);

                String userName = userNameField.getText().toString().trim();
                String password = passwordField.getText().toString().trim();

                logIn(userName, password);

                //logInQuick();

            }
        });

    }

    private void logInQuick() {
        Intent intent = new Intent(LogIn.this, Main.class);
        LogIn.this.startActivity(intent);
        }

    public void logIn(final String userName, final String password){


        if(!userName.equals("")) {

            collectionReference.document(userName).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                String passwordDB = documentSnapshot.getString(passwordDocument);
                                if (password.equals(passwordDB)) {
                                    Toast.makeText(LogIn.this, "Combinatie is juist, u wordt ingelogd!", Toast.LENGTH_SHORT).show();
                                    passwordField.setActivated(false);
                                    userNameField.setActivated(false);

                                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(LogIn.this);
                                    SharedPreferences.Editor preferencesEditor = preferences.edit();
                                    preferencesEditor.putString("employee", userName).apply();
                                    preferencesEditor.putBoolean("login",true).apply();

                                    Intent intent = new Intent(LogIn.this, Main.class);
                                    LogIn.this.startActivity(intent);
                                    finish();

                                } else {
                                    Toast.makeText(LogIn.this, "Wachtwoord onjuist", Toast.LENGTH_SHORT).show();
                                    passwordField.setText("");
                                    loginProgress.setVisibility(View.INVISIBLE);
                                    logInButton.setVisibility(View.VISIBLE);


                                }
                            } else {
                                Toast.makeText(LogIn.this, "Gebruiker niet bekend", Toast.LENGTH_SHORT).show();
                                passwordField.setText("");
                                userNameField.setText("");
                                loginProgress.setVisibility(View.INVISIBLE);
                                logInButton.setVisibility(View.VISIBLE);

                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(LogIn.this, "Error", Toast.LENGTH_SHORT).show();
                            loginProgress.setVisibility(View.INVISIBLE);
                            logInButton.setVisibility(View.VISIBLE);

                        }
                    });
        } else {
            Toast.makeText(LogIn.this, "Oeps... probeer het nog eens", Toast.LENGTH_SHORT).show();
            loginProgress.setVisibility(View.INVISIBLE);
            logInButton.setVisibility(View.VISIBLE);
        }
    }
}



