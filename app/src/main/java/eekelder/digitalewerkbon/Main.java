package eekelder.digitalewerkbon;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.net.URI;

public class Main extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;


    private TextView introText;
    private AlertDialog alertbox;
    private FirebaseFirestore dbFire = FirebaseFirestore.getInstance();
    private FloatingActionButton addPhoto;
    private Uri mImageUri;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private ProgressBar progressBar;

    private static final int IMAGE_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        progressBar = findViewById(R.id.progressBar2);
        mStorageRef = FirebaseStorage.getInstance().getReference("photos");

        addPhoto = findViewById(R.id.floatingActionButton);
        addPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, IMAGE_REQUEST_CODE);
            }
        });

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        dbFire.setFirestoreSettings(settings);


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(Main.this);
        boolean loggedIn = preferences.getBoolean("login", false);

        if (!loggedIn) {
            this.deleteDatabase("Planning.db");
            Log.d("tag", "On Create MAIN : Niet ingelogd --> gaat naar LogIn");
            Intent intent = new Intent(Main.this, LogIn.class);
            startActivity(intent);
            finish();
        } else {
            Log.d("tag", "On Create MAIN : Ingelogd --> blijft in Main");
            AlertDialog.Builder builder = new AlertDialog.Builder(Main.this);
            builder.setTitle("Data ophalen");
            //builder.setIcon(R.mipmap.ic_fendt);
            builder.setMessage("Data wordt opgehaald, even geduld");
            alertbox = builder.create();
            alertbox.show();

            mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
            mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
            mDrawerLayout.addDrawerListener(mToggle);
            mToggle.syncState();
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            NavigationView navigationView = findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    item.setChecked(true);
                    String checkedItem = item.getTitle().toString();
                    if (checkedItem.contains("toevoegen")) {
                        Intent intent = new Intent(Main.this, AddTask.class);
                        startActivity(intent);
                    } else if (checkedItem.contains("registratie")) {
                        Intent intent = new Intent(Main.this, DailyHours.class);
                        startActivity(intent);
                    } else if (checkedItem.contains("taken")) {
                        Intent intent = new Intent(Main.this, AllTasks.class);
                        startActivity(intent);
                    } else if (checkedItem.contains("Huid")) {
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(Main.this);
                        boolean taskStarted = preferences.getBoolean("taskStarted", false);
                        if (taskStarted) {
                            long taskNumber = preferences.getLong("taskNumber", 0);
                            Database mydb = Database.getInstance(getApplicationContext());
                            Task task = mydb.getTask(taskNumber);
                            if (task != null) {
                                Intent intent = new Intent(Main.this, ViewTask.class);
                                intent.putExtra("task", task);
                                startActivity(intent);
                            }
                        } else {
                            Toast.makeText(Main.this, "Geen taak actief", Toast.LENGTH_SHORT).show();
                        }

                    } else if (checkedItem.contains("loggen")) {

                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(Main.this);
                        SharedPreferences.Editor preferencesEditor = preferences.edit();
                        preferencesEditor.putString("employee", "").apply();
                        preferencesEditor.putBoolean("login", false).apply();

                        Intent intent = new Intent(Main.this, LogIn.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }

                    mDrawerLayout.closeDrawers();
                    return true;
                }
            });


            ExecuteStatus.addMyBooleanListener(new BooleanChangedListener() {
                @Override
                public void onMyBooleanChanged() {
                    System.out.println("BOOLEAN CHANGED " + ExecuteStatus.getStatus());
                    if (ExecuteStatus.getStatus() == 0) {
                        alertbox.dismiss();
                    } else if (ExecuteStatus.getStatus() == -1) {
                        alertbox.dismiss();
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Main.this);
                        alertDialogBuilder.setTitle("Data niet opgehaald!");
                        alertDialogBuilder
                                .setMessage("Kon data niet verversen!")
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    }
                }
            });

            updateStaticData();

            introText = findViewById(R.id.textview_main);

            introText.setText("Welkom op het dashboard van de app! Door op het icoon linksboven te klikken kun je verschillende functies kiezen. ");
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            Toast.makeText(this, "Foto wordt geupload...", Toast.LENGTH_SHORT).show();
                mImageUri = data.getData();
                Log.d("tag", "Foto uploaden naar Firebase");

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(Main.this);
            String employeeLoggedIn = preferences.getString("employee", "employee");

                StorageReference fileReference = mStorageRef.child(employeeLoggedIn).child(System.currentTimeMillis() + "." + getFileExtension(mImageUri));
                fileReference.putFile(mImageUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                progressBar.setProgress(0);
                                Toast.makeText(Main.this, "Foto geupload", Toast.LENGTH_SHORT).show();

                            }
                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                                progressBar.setProgress((int) progress);
                                Log.d("progress", String.valueOf(progress));
                            }
                        });


        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mToggle.onOptionsItemSelected(item)) {

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateStaticData() {

        Database mydb = Database.getInstance(getApplicationContext());
        mydb.checkData();

    }
}
