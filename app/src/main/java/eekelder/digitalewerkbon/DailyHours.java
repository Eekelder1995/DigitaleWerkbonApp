package eekelder.digitalewerkbon;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class DailyHours extends AppCompatActivity {

    private TextView intro;
    private Button startDay;
    private Button endDay;
    private Button startPause;
    private Button endPause;
    private Button startTravel;
    private Button endTravel;
    private Button dayOverview;
    private boolean workdayStarted;
    private boolean pauseStarted;
    private boolean travelStarted;
    private boolean taskStarted;
    private long taskNumber;

    private static final String collectionEmployees = "Employees";
    private static final String collectionWorkDays = "workdays";


    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection(collectionEmployees);
    private EmployeeTimes employeeTimes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_hours);

        intro = findViewById(R.id.textview_dailyhours);
        intro.setText("In dit menu kun je de werktijden regelen. De werkdag dient gestart en beëidndigd te worden.");

        startDay = findViewById(R.id.button_startday);
        endDay = findViewById(R.id.button_endday);

        startDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startWorkDay();

            }
        });

        endDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endWorkDay();
                workdayStarted = false;
                setButtons();
            }
        });

        startPause = findViewById(R.id.button_startPause);
        endPause = findViewById(R.id.button_endPause);

        startPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPause();
                pauseStarted = true;
                setButtons();
            }
        });

        endPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endPause();
                pauseStarted = false;
                setButtons();
            }
        });

        startTravel = findViewById(R.id.button_start_travel);
        endTravel = findViewById(R.id.button_stop_travel);

        startTravel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTravel();
                travelStarted = true;
                setButtons();
            }
        });

        endTravel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endTravel();
                travelStarted = false;
                setButtons();
            }
        });

        dayOverview = findViewById(R.id.button_view_days);
        dayOverview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DailyHours.this, ViewDays.class);
                startActivity(intent);
            }
        });

    }

    private void setButtons() {

        startDay.setVisibility(View.INVISIBLE);
        endDay.setVisibility(View.INVISIBLE);
        startPause.setVisibility(View.INVISIBLE);
        endPause.setVisibility(View.INVISIBLE);
        startTravel.setVisibility(View.INVISIBLE);
        endTravel.setVisibility(View.INVISIBLE);

        if (!taskStarted) {

            if (!workdayStarted) { //workday not started
                startDay.setVisibility(View.VISIBLE);
            } else {

                if (pauseStarted) {
                    endPause.setVisibility(View.VISIBLE);
                } else if (!travelStarted) {
                    startPause.setVisibility(View.VISIBLE);
                }

                if (travelStarted) {
                    endTravel.setVisibility(View.VISIBLE);
                } else if (!pauseStarted) {
                    startTravel.setVisibility(View.VISIBLE);
                }

                if (!travelStarted && !pauseStarted) {
                    endDay.setVisibility(View.VISIBLE);
                }
            }
        } else {
            if (pauseStarted) {
                endPause.setVisibility(View.VISIBLE);
            } else {
                startPause.setVisibility(View.VISIBLE);
            }
        }

    }

    private void startWorkDay() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor preferencesEditor = preferences.edit();

        final Database mydb = Database.getInstance(getApplicationContext());
        boolean result = mydb.startWorkDay();

        if(result){
            preferencesEditor.putBoolean("workdayStarted", true).apply();
            Toast.makeText(DailyHours.this, "Werkdag gestart!", Toast.LENGTH_SHORT).show();
            workdayStarted = true;
            setButtons();
        } else {
            android.support.v7.app.AlertDialog.Builder alertDialogBuilder = new android.support.v7.app.AlertDialog.Builder(DailyHours.this);
            alertDialogBuilder.setTitle("Werkdag al gestart");
            alertDialogBuilder
                    .setMessage("Wil je de werkdag verlengen?! JA zal de werkdag weer activeren, NEE zal de huidige werkdag verwijderen")
                    .setCancelable(true)
                    .setNegativeButton("NEE", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            boolean result2 = mydb.resetWorkday();

                            if (result2){
                                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(DailyHours.this);
                                SharedPreferences.Editor preferencesEditor = preferences.edit();
                                preferencesEditor.putBoolean("workdayStarted", true).apply();
                                Toast.makeText(DailyHours.this, "Werkdag opnieuw gestart!", Toast.LENGTH_SHORT).show();
                                workdayStarted = true;
                                setButtons();
                            }


                        }
                    })
                    .setPositiveButton("JA", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            boolean result2 = mydb.continueWorkday();

                            if (result2){
                                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(DailyHours.this);
                                SharedPreferences.Editor preferencesEditor = preferences.edit();
                                preferencesEditor.putBoolean("workdayStarted", true).apply();
                                Toast.makeText(DailyHours.this, "Werkdag hervat!", Toast.LENGTH_SHORT).show();
                                workdayStarted = true;
                                setButtons();
                            }
                        }
                    });
            android.support.v7.app.AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();


        }
    }

    private void endWorkDay() {
        Database mydb = Database.getInstance(getApplicationContext());
        WorkingHours workingHours = mydb.endWorkDay();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor preferencesEditor = preferences.edit();
        preferencesEditor.putBoolean("workdayStarted", false).apply();
        String documentEmployee = preferences.getString("employee","");
        Toast.makeText(DailyHours.this, "Werkdag gestopt!", Toast.LENGTH_SHORT).show();



        collectionReference.document(documentEmployee).collection(collectionWorkDays).document(String.valueOf(workingHours.getDayOfYearYear()))
                .set(workingHours);


    }

    private void startPause() {

        Database mydb = Database.getInstance(getApplicationContext());
        mydb.startPauseOrTravel("pause");

        if (taskStarted) {
            Calendar calendar = Calendar.getInstance();
            int time = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
            employeeTimes.setPauseStartTime(time);
        }


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor preferencesEditor = preferences.edit();
        preferencesEditor.putBoolean("pauseStarted", true).apply();
    }

    private void endPause() {
        Database mydb = Database.getInstance(getApplicationContext());
        mydb.endPauseOrTravel("pause");

        if (taskStarted){
            Calendar calendar = Calendar.getInstance();
            int time = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
            int startPauseTime = employeeTimes.getPauseStartTime();
            int previousPauseTime = employeeTimes.getPauseTime();
            int newPauseTime  = previousPauseTime + (time - startPauseTime);
            employeeTimes.setPauseTime(newPauseTime);
            employeeTimes.setPauseStartTime(0);
        }


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor preferencesEditor = preferences.edit();
        preferencesEditor.putBoolean("pauseStarted", false).apply();
    }

    private void startTravel() {
        Database mydb = Database.getInstance(getApplicationContext());
        mydb.startPauseOrTravel("travel");

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor preferencesEditor = preferences.edit();
        preferencesEditor.putBoolean("travelStarted", true).apply();
    }

    private void endTravel() {
        Database mydb = Database.getInstance(getApplicationContext());
        mydb.endPauseOrTravel("travel");

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor preferencesEditor = preferences.edit();
        preferencesEditor.putBoolean("travelStarted", false).apply();
    }

    @Override
    protected void onResume() {

        super.onResume();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        pauseStarted = preferences.getBoolean("pauseStarted", false);
        travelStarted = preferences.getBoolean("travelStarted", false);
        taskStarted = preferences.getBoolean("taskStarted", false);
        workdayStarted = preferences.getBoolean("workdayStarted", false);
        taskNumber = preferences.getLong("taskNumber",0);

        if (taskStarted){
            ExecuteStatus.setStatus(ExecuteStatus.getStatus()+1);
            Database mydb = Database.getInstance(getApplicationContext());
            employeeTimes = mydb.getEmployeeTimes(taskNumber);
        } else {
            employeeTimes = null;
        }

        setButtons();
    }

    @Override
    protected void onStop() {

        if (employeeTimes!=null) {
            ExecuteStatus.setStatus(ExecuteStatus.getStatus()+1);
            Database mydb = Database.getInstance(getApplicationContext());
            mydb.setEmployeeTimes(employeeTimes);
        }
        super.onStop();
    }
}
