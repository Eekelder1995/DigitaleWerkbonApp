package eekelder.digitalewerkbon;

import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class ViewTask extends AppCompatActivity {

    private Task task;

    private Button startTask;
    private Button endTask;
    private Button startPause;
    private Button endPause;
    private Button setOutputs;
    private Button setRemarks;
    private Button removeTask;
    private Button addTask;
    private boolean pauseStarted;
    private boolean travelStarted;
    private boolean workdayStarted;
    private boolean taskStarted;
    private String employeeLoggedIn;
    protected EmployeeTimes currentEmployeeTimes;

    private EditText outputsText;
    private EditText remarksText;
    private TextView client;
    private TextView duty;
    private TextView times;
    private TextView remarks;
    private Spinner outputSpinner;

    private Button setTimes;
    private Spinner hoursSpinner;
    private Spinner minutesSpinner;
    private TextView popupTimes;

    private static String stringArraySeparator = ",";

    private static final String employeeTimesCollection = "employeeTimes";
    private static final String taskCollection = "Tasks";
    private FirebaseFirestore dbFire = FirebaseFirestore.getInstance();
    private CollectionReference taskCollectionReference = dbFire.collection(taskCollection);


    private boolean taskFinished;
    private long taskNumber;
    Dialog myDialog;
    private boolean finishActivity;
    private boolean remarksActivated;
    private boolean outputsActivated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_task);
        myDialog = new Dialog(this);
        finishActivity = false;
        task = (Task) getIntent().getSerializableExtra("task");

        startTask = findViewById(R.id.button_start_task);
        endTask = findViewById(R.id.button_end_task);
        startPause = findViewById(R.id.button_task_start_pause);
        endPause = findViewById(R.id.button_task_end_pause);
        setOutputs = findViewById(R.id.button_task_output);
        setRemarks = findViewById(R.id.button_set_remarks);
        removeTask = findViewById(R.id.button_task_remove);
        addTask = findViewById(R.id.button_add_employeetimes);
        client = findViewById(R.id.textView_task_client);
        duty = findViewById(R.id.textView_task_duty);
        times = findViewById(R.id.textView_task_time);
        remarks = findViewById(R.id.textView_task_remarks);

        outputsText = findViewById(R.id.editText_task_output);
        remarksText = findViewById(R.id.editText_task_remarks);
        outputSpinner = findViewById(R.id.spinner_task_output);

        String[] outputTypes = {"", "m3", "ha", "kg", "ton", "overig"};

        ArrayAdapter<String> adapterOutputs = new ArrayAdapter<String>(ViewTask.this, android.R.layout.simple_spinner_item, outputTypes);
        adapterOutputs.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        outputSpinner.setAdapter(adapterOutputs);
        outputSpinner.setSelection(0);

        startTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean proceed = true;
                if (!workdayStarted) {
                    proceed = false;
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ViewTask.this);
                    alertDialogBuilder.setTitle("Kan taak niet starten");
                    alertDialogBuilder
                            .setMessage("Kan taak niet starten. Werkdag moet gestart zijn!")
                            .setCancelable(true)
                            .setPositiveButton("WERKDAG STARTEN", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    Intent intent = new Intent(ViewTask.this, DailyHours.class);
                                    startActivity(intent);
                                }
                            });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }

                if (pauseStarted) {
                    proceed = false;
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ViewTask.this);
                    alertDialogBuilder.setTitle("Kan taak niet starten");
                    alertDialogBuilder
                            .setMessage("Kan taak niet starten. Pauze moet gestopt zijn!")
                            .setCancelable(true)
                            .setPositiveButton("PAUZE STOPPEN", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    Intent intent = new Intent(ViewTask.this, DailyHours.class);
                                    startActivity(intent);
                                }
                            });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }

                if (travelStarted) {
                    proceed = false;
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ViewTask.this);
                    alertDialogBuilder.setTitle("Kan taak niet starten");
                    alertDialogBuilder
                            .setMessage("Kan taak niet starten. Reizen moet gestopt zijn!")
                            .setCancelable(true)
                            .setPositiveButton("REIZEN STOPPEN", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    Intent intent = new Intent(ViewTask.this, DailyHours.class);
                                    startActivity(intent);
                                }
                            });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }

                if (taskStarted) {
                    proceed = false;
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ViewTask.this);
                    alertDialogBuilder.setTitle("Kan taak niet starten");
                    alertDialogBuilder
                            .setMessage("Kan taak niet starten. Andere taak moet gestopt zijn!")
                            .setCancelable(true);

                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }

                if (proceed) {
                    startTask();
                }
            }


        });

        endTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endTask(view);


            }
        });

        startPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startPause();

            }
        });

        endPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endPause();
            }
        });

        setOutputs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setOutputs();
            }
        });

        setRemarks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setRemarks();
            }
        });

        removeTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeTask();
            }
        });

        addTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addTask();
            }
        });


        ExecuteStatus.addMyBooleanListener(new BooleanChangedListener() {
            @Override
            public void onMyBooleanChanged() {
                Log.d("tag", "BOOLEAN CHANGED " + ExecuteStatus.getStatus());
                if (ExecuteStatus.getStatus() == 0) {

                    if (finishActivity) {
                        finish();
                    }

                    if (currentEmployeeTimes != null) {
                        if (currentEmployeeTimes.getEndTime() == 0) {
                            taskFinished = false;
                        } else {
                            taskFinished = true;
                        }
                    } else {
                        taskFinished = false;
                    }
                    setButtons();
                } else if (ExecuteStatus.getStatus() != 0) {
                }
            }

        });

        remarksActivated = false;
        outputsActivated = false;
        remarksText.setEnabled(false);
        outputsText.setEnabled(false);
        outputSpinner.setEnabled(false);


        Log.d("tag", "Opening ViewTask, task is " + task.getDescription() + " " + task.getClient());

    }

    private void addTask() {

        task.getEmployees().add(employeeLoggedIn);

        Database mydb = Database.getInstance(getApplicationContext());
        mydb.insertTask(task);
        UpdateThreadTask thread = new UpdateThreadTask(task);
        thread.start();

        setButtons();

    }

    private void removeTask() {

        Database mydb = Database.getInstance(getApplicationContext());
        task.getEmployees().remove(employeeLoggedIn);
        mydb.deleteMeFromTask(task);

        UpdateThreadTask thread = new UpdateThreadTask(task);
        thread.start();

        finish();
    }

    private void setRemarks() {

        if (remarksActivated) {
            String remarks = remarksText.getText().toString().trim();
            Log.d("tag", "Remarksactivated " + remarksActivated + " remarks " + remarks);
            currentEmployeeTimes.setRemarks(remarks);
            remarksText.setEnabled(false);
        } else {
            remarksText.setEnabled(true);
        }

        remarksActivated = !remarksActivated;

    }

    private void setOutputs() {

        if (outputsActivated) {
            String outputs = outputsText.getText().toString().trim();
            String selectedOutput = (String) outputSpinner.getSelectedItem();
            currentEmployeeTimes.setOutputs(outputs + "-" + selectedOutput);
            outputsText.setEnabled(false);
            outputSpinner.setEnabled(false);
        } else {
            outputsText.setEnabled(true);
            outputSpinner.setEnabled(true);
        }

        outputsActivated = !outputsActivated;

    }


    private void endPause() {
        Calendar calendar = Calendar.getInstance();
        int time = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
        int startPauseTime = currentEmployeeTimes.getPauseStartTime();
        int previousPauseTime = currentEmployeeTimes.getPauseTime();
        int newPauseTime = previousPauseTime + (time - startPauseTime);
        currentEmployeeTimes.setPauseTime(newPauseTime);
        currentEmployeeTimes.setPauseStartTime(0);
        Database mydb = Database.getInstance(getApplicationContext());
        mydb.endPauseOrTravel("pause");
        pauseStarted = false;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ViewTask.this);
        SharedPreferences.Editor preferencesEditor = preferences.edit();
        preferencesEditor.putBoolean("pauseStarted", false).apply();
        setButtons();
    }

    private void startPause() {
        Calendar calendar = Calendar.getInstance();
        int time = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
        currentEmployeeTimes.setPauseStartTime(time);
        Database mydb = Database.getInstance(getApplicationContext());
        mydb.startPauseOrTravel("pause");
        pauseStarted = true;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ViewTask.this);
        SharedPreferences.Editor preferencesEditor = preferences.edit();
        preferencesEditor.putBoolean("pauseStarted", true).apply();
        setButtons();
    }

    private void endTask(View view) {

        Calendar calendar = Calendar.getInstance();
        int endTime = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);

        currentEmployeeTimes.setEndTime(endTime);
        taskStarted = false;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ViewTask.this);
        SharedPreferences.Editor preferencesEditor = preferences.edit();
        preferencesEditor.putBoolean("taskStarted", false).apply();
        preferencesEditor.putLong("taskNumber", 0).apply();
        setPopOpHours(view);


    }

    private void setPopOpHours(View v) {

        myDialog.setContentView(R.layout.popup_window);

        hoursSpinner = myDialog.findViewById(R.id.spinner_popup_hours);
        minutesSpinner = myDialog.findViewById(R.id.spinner_popup_minutes);
        popupTimes = myDialog.findViewById(R.id.textview_popup_times);
        setTimes = myDialog.findViewById(R.id.button_popup_set);

        Integer[] hours = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24};
        Integer[] minutes = {0, 15, 30, 45};

        ArrayAdapter<Integer> adapterHours = new ArrayAdapter<Integer>(ViewTask.this, android.R.layout.simple_spinner_item, hours);
        adapterHours.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        hoursSpinner.setAdapter(adapterHours);

        ArrayAdapter<Integer> adapterMinutes = new ArrayAdapter<Integer>(ViewTask.this, android.R.layout.simple_spinner_item, minutes);
        adapterMinutes.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        minutesSpinner.setAdapter(adapterMinutes);


        int startTime = currentEmployeeTimes.getStartTime();
        int startMinutes = startTime % 60;
        int startHours = (startTime - startMinutes) / 60;
        String startMinutesString = String.valueOf(startMinutes);
        if (startMinutes < 10) {
            startMinutesString = "0" + startMinutes;
        }

        int endTime = currentEmployeeTimes.getEndTime();
        int endMinutes = endTime % 60;
        int endHours = (endTime - endMinutes) / 60;
        String endMinutesString = String.valueOf(endMinutes);
        if (endMinutes < 10) {
            endMinutesString = "0" + endMinutes;
        }

        int pauseTime = currentEmployeeTimes.getPauseTime();
        int pauseMinutes = pauseTime % 60;
        int pauseHours = (pauseTime - pauseMinutes) / 60;
        String pauseMinutesString = String.valueOf(pauseMinutes);
        if (pauseMinutes < 10) {
            pauseMinutesString = "0" + pauseMinutes;
        }

        int totalMinutes = currentEmployeeTimes.getEffectiveWorkTime();
        int effectiveMinutes = totalMinutes % 60;
        int effectiveHours = (totalMinutes - effectiveMinutes) / 60;
        String effectiveMinutesString = String.valueOf(effectiveMinutes);
        if (effectiveMinutes < 10) {
            effectiveMinutesString = "0" + effectiveMinutes;
        }

        int roundedMinutes;
        if (effectiveMinutes < 16) {
            roundedMinutes = 1;
        } else if (effectiveMinutes < 31) {
            roundedMinutes = 2;
        } else if (effectiveMinutes < 46) {
            roundedMinutes = 3;
        } else {
            roundedMinutes = 0;
            effectiveHours = effectiveHours + 1;
        }

        hoursSpinner.setSelection(effectiveHours);
        minutesSpinner.setSelection(roundedMinutes);

        popupTimes.setText("Taak gestart om " + startHours + ":" + startMinutesString + " , en gestopt om " + endHours + ":" + endMinutesString + " met een pauzetijd van " + pauseHours + ":" + pauseMinutesString + ". Dit geeft een totale werkduur van " + effectiveHours + ":" + effectiveMinutesString + ". De werkduur kan hieronder bijgesteld worden.");

        setTimes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int selectedHour = (int) hoursSpinner.getSelectedItem();
                int selectedMinute = (int) minutesSpinner.getSelectedItem();
                int selectedTotalTime = selectedHour * 60 + selectedMinute;
                currentEmployeeTimes.setEffectiveWorkTime(selectedTotalTime);
                myDialog.dismiss();
                setButtons();

            }
        });

        myDialog.setCancelable(true);
        myDialog.show();


    }

    private void startTask() {
        currentEmployeeTimes = new EmployeeTimes();
        Calendar calendar = Calendar.getInstance();
        int dayNumber = calendar.get(Calendar.DAY_OF_YEAR);
        currentEmployeeTimes.setDayNumber(dayNumber);
        currentEmployeeTimes.setEmployee(employeeLoggedIn);
        currentEmployeeTimes.setOutputs("");
        currentEmployeeTimes.setRemarks("");
        currentEmployeeTimes.setTaskNumber(task.getDocumentName());
        currentEmployeeTimes.setPauseStartTime(0);
        currentEmployeeTimes.setPauseTime(0);
        currentEmployeeTimes.setEndTime(0);
        int startTime = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
        currentEmployeeTimes.setStartTime(startTime);

        Toast.makeText(ViewTask.this, "Taak gestart!", Toast.LENGTH_SHORT).show();
        taskStarted = true;
        taskNumber = task.getDocumentName();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ViewTask.this);
        SharedPreferences.Editor preferencesEditor = preferences.edit();
        preferencesEditor.putBoolean("taskStarted", true).apply();
        preferencesEditor.putLong("taskNumber", task.getDocumentName()).apply();

        setButtons();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        pauseStarted = preferences.getBoolean("pauseStarted", false);
        travelStarted = preferences.getBoolean("travelStarted", false);
        taskStarted = preferences.getBoolean("taskStarted", false);
        workdayStarted = preferences.getBoolean("workdayStarted", false);
        taskNumber = preferences.getLong("taskNumber", 0);
        employeeLoggedIn = preferences.getString("employee", "employee");

        currentEmployeeTimes = null;

        ExecuteStatus.setStatus(ExecuteStatus.getStatus() + 1);
        Database mydb = Database.getInstance(getApplicationContext());
        currentEmployeeTimes = mydb.getEmployeeTimes(task.getDocumentName());

        int minutesStart = task.getStartTime() % 60;
        int hoursStart = (task.getStartTime() - minutesStart) / 60;
        String minutesStartString = String.valueOf(minutesStart);
        if (minutesStart < 10) {
            minutesStartString = "0" + minutesStart;
        }
        int minutesEnd = task.getEndTime() % 60;
        int hoursEnd = (task.getEndTime() - minutesEnd) / 60;
        String minutesEndString = String.valueOf(minutesEnd);
        if (minutesEnd < 10) {
            minutesEndString = "0" + minutesEnd;
        }

        client.setText("Klant : " + task.getClient());
        duty.setText("Taak : " + task.getDescription());
        times.setText("Geplande starttijd: " + hoursStart + ":" + minutesStartString + " , eindtijd: " + hoursEnd + ":" + minutesEndString);
        remarks.setText("Opmerkingen: " + task.getRemarks());

        if (currentEmployeeTimes!=null){
            if (!currentEmployeeTimes.getRemarks().equals("")) {
                remarksText.setText(currentEmployeeTimes.getRemarks());
            }
            String[] outputTypes = {"", "m3", "ha", "kg", "ton", "overig"};

            if (!currentEmployeeTimes.getOutputs().equals("")) {
                String result = currentEmployeeTimes.getOutputs();
                String[] output = result.split("-");
                outputsText.setText(output[0]);

                int counter = -1;
                for (String s : outputTypes) {
                    counter++;
                    if (s.equals(output[1])) {
                        outputSpinner.setSelection(counter);
                    }
                }

            }
        }

        setButtons();

    }

    private void setButtons() {

        if (currentEmployeeTimes != null) {


            if (currentEmployeeTimes.getEndTime() == 0) {
                taskFinished = false;
            } else {
                taskFinished = true;
            }
        } else {
            taskFinished = false;
        }

        startTask.setVisibility(View.INVISIBLE);
        endTask.setVisibility(View.INVISIBLE);
        startPause.setVisibility(View.INVISIBLE);
        endPause.setVisibility(View.INVISIBLE);
        setOutputs.setVisibility(View.INVISIBLE);
        setRemarks.setVisibility(View.INVISIBLE);
        removeTask.setVisibility(View.INVISIBLE);
        addTask.setVisibility(View.INVISIBLE);

        if (!taskFinished) {

            if (!task.getEmployees().contains(employeeLoggedIn)) {
                addTask.setVisibility(View.VISIBLE);

            } else {
                removeTask.setVisibility(View.VISIBLE);

                if (!taskStarted) {
                    startTask.setVisibility(View.VISIBLE);
                    removeTask.setVisibility(View.VISIBLE);

                } else if (taskNumber == task.getDocumentName()) {
                    setRemarks.setVisibility(View.VISIBLE);
                    setOutputs.setVisibility(View.VISIBLE);
                    removeTask.setVisibility(View.INVISIBLE);

                    if (!pauseStarted) {
                        startPause.setVisibility(View.VISIBLE);
                        endTask.setVisibility(View.VISIBLE);


                    } else if (pauseStarted) {
                        endPause.setVisibility(View.VISIBLE);
                    } else {

                    }

                }


            }

        } else {
            setRemarks.setVisibility(View.VISIBLE);
            setOutputs.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onBackPressed() {
        if (currentEmployeeTimes != null) {
            finishActivity = true;
            ExecuteStatus.setStatus(ExecuteStatus.getStatus() + 1);
            Database mydb = Database.getInstance(getApplicationContext());
            mydb.setEmployeeTimes(currentEmployeeTimes);
            Log.d("tag", "Setting EmployeeTimes to FireBase");
            taskCollectionReference.document(String.valueOf(currentEmployeeTimes.getTaskNumber())).collection(employeeTimesCollection).document(currentEmployeeTimes.getDocumentName())
                    .set(currentEmployeeTimes);
        } else {
            finish();
        }

    }

    class UpdateThreadEmployeeTimes extends Thread {


        EmployeeTimes threadEmployeeTimes;

        UpdateThreadEmployeeTimes(EmployeeTimes e) {
            this.threadEmployeeTimes = e;
        }


        @Override
        public void run() {

        }
    }

    class UpdateThreadTask extends Thread {

        private FirebaseFirestore db = FirebaseFirestore.getInstance();
        private static final String collection = "Tasks";
        private CollectionReference collectionReference = db.collection(collection);
        Task task;

        UpdateThreadTask(Task t) {
            this.task = t;
        }

        ;

        @Override
        public void run() {
            Log.d("tag", "Setting Task to FireBase");
            collectionReference.document(String.valueOf(task.getDocumentName()))
                    .set(task);
        }
    }
}
