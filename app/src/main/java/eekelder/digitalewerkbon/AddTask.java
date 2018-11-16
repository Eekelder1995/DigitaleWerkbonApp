package eekelder.digitalewerkbon;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.toIntExact;

public class AddTask extends AppCompatActivity {

    private Spinner daySpinner;
    private Spinner monthSpinner;
    private ProgressBar progressBar;

    private Button selectClientButton;
    private Button selectEquipmentButton;
    private Button selectEmployeeButton;
    private Button addTaskButton;

    private List<Equipment> equipmentList = new ArrayList<>();
    private ArrayList<Employee> employeeList = new ArrayList<>();
    private Client client = null;

    private TextView employeeText;
    private TextView equipmentText;
    private TextView clientText;
    private EditText description;
    private EditText remarks;

    private static final String collection = "Tasks";

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection(collection);
    private android.support.v7.app.AlertDialog alertbox;
    private int quitActivityCounter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        Toast.makeText(this, "Data wordt ververst, selectievakjes worden ieder ogenblik actief", Toast.LENGTH_LONG).show();
        
        addTaskButton = findViewById(R.id.button_add_task);
        selectClientButton = findViewById(R.id.button_select_client);
        selectEquipmentButton = findViewById(R.id.button_select_equipment);
        selectEmployeeButton = findViewById(R.id.button_select_employee);
        
        addTaskButton.setVisibility(View.INVISIBLE);
        selectEmployeeButton.setVisibility(View.INVISIBLE);
        selectClientButton.setVisibility(View.INVISIBLE);
        selectEquipmentButton.setVisibility(View.INVISIBLE);
        
        Database mydb = Database.getInstance(getApplicationContext());
        mydb.checkData();

        ExecuteStatus.addMyBooleanListener(new BooleanChangedListener() {
            @Override
            public void onMyBooleanChanged() {
                if (ExecuteStatus.getStatus() == 0) {
                    addTaskButton.setVisibility(View.VISIBLE);
                    selectEmployeeButton.setVisibility(View.VISIBLE);
                    selectClientButton.setVisibility(View.VISIBLE);
                    selectEquipmentButton.setVisibility(View.VISIBLE);
                } 
            }
        });

        daySpinner = findViewById(R.id.spinner_day);
        monthSpinner = findViewById(R.id.spinner_month);
        progressBar = findViewById(R.id.progressBar_addTask);

        employeeText = findViewById(R.id.textView_employee);
        equipmentText = findViewById(R.id.textView_equipment);
        clientText = findViewById(R.id.textView_client);
        description = findViewById(R.id.editText_description);
        remarks = findViewById(R.id.editText_remarks);

        progressBar.setVisibility(View.INVISIBLE);


        selectClientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddTask.this, SelectClient.class);
                startActivityForResult(intent, 1);
            }
        });

        selectEmployeeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddTask.this, SelectEmployee.class);
                startActivityForResult(intent, 2);
            }
        });

        selectEquipmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddTask.this, SelectEquipment.class);
                startActivityForResult(intent, 3);
            }
        });

        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                addTaskButton.setVisibility(View.INVISIBLE);
                boolean proceed = true;

                String descriptionString = description.getText().toString();
                int day = (int) daySpinner.getSelectedItem();
                String month = (String) monthSpinner.getSelectedItem();

                if (client == null || descriptionString.equals("") || day == 0 || month.equals("")) {
                    proceed = false;
                }

                if (proceed) {
                    Log.d("tag", "TASK ready to insert");

                    android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(AddTask.this);
                    builder.setTitle("Taak toevoegen");
                    //builder.setIcon(R.mipmap.ic_fendt);
                    builder.setMessage("Taak wordt toegevoegd, een moment geduld...");
                    alertbox = builder.create();
                    alertbox.show();

                    Calendar calendar = Calendar.getInstance();
                    int startTime = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
                    int endTime = startTime + 60;
                    long timeInMillis = calendar.getTimeInMillis();

                    int dayOfYear = getDayOfYear(day, month);
                    int year = calendar.get(Calendar.YEAR);
                    int dayOfYearYear = dayOfYear * 10000 + year;
                    final Task newTask = new Task();
                    newTask.setClient(client.getDocumentName());
                    newTask.setDayNumber(dayOfYear);
                    newTask.setDayNumberYear(dayOfYearYear);
                    newTask.setDescription(descriptionString);
                    newTask.setRemarks(remarks.getText().toString());
                    newTask.setStartTime(startTime);
                    newTask.setEndTime(endTime);
                    newTask.setEquipment(getListStringEquipment(equipmentList));
                    newTask.setEmployees(getListStringEmployee(employeeList));
                    newTask.setStatus(1);
                    newTask.setDocumentName(timeInMillis);

                    Database mydb = Database.getInstance(getApplicationContext());
                    boolean succes = mydb.insertTask(newTask);

                    if (!succes) {
                        alertbox.dismiss();
                        android.support.v7.app.AlertDialog.Builder alertDialogBuilder = new android.support.v7.app.AlertDialog.Builder(AddTask.this);
                        alertDialogBuilder.setTitle("Taak niet toegevoegd!");
                        alertDialogBuilder
                                .setMessage("Kon taak niet toevoegen!")
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                        android.support.v7.app.AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    } else {
                        writeTask(newTask);
                    }
//
//
//                        final int[] taskCounter = {dayOfYear};
//                        System.out.println("TASKCOUNTER1 " + taskCounter[0]);
//
//                        collectionReference.document(documentName).get()
//                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                                    @Override
//                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
//                                        if (documentSnapshot.exists()) {
//                                        } else {
//                                            Toast.makeText(AddTask.this, "Geen taaknummer kunnen krijgen ", Toast.LENGTH_SHORT).show();
//                                            progressBar.setVisibility(View.INVISIBLE);
//                                            addTaskButton.setVisibility(View.VISIBLE);
//
//                                        }
//                                    }
//                                })
//                                .addOnFailureListener(new OnFailureListener() {
//                                    @Override
//                                    public void onFailure(@NonNull Exception e) {
//                                        Toast.makeText(AddTask.this, "Error", Toast.LENGTH_SHORT).show();
//                                        progressBar.setVisibility(View.INVISIBLE);
//                                        addTaskButton.setVisibility(View.VISIBLE);
//                                    }
//                                })
//                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                                    @Override
//                                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<DocumentSnapshot> task) {
//                                        if (task.isSuccessful()) {
//                                            DocumentSnapshot document = task.getResult();
//                                            if (document.exists()){
//                                                taskCounter[0] = document.getLong(documentField).intValue();
//                                                taskCounter[0] = taskCounter[0] + 1;
//                                                writeTask(newTask, taskCounter[0]);
//                                            }
//
//                                        }
//                                    }
//                                });
//
//                    }
                } else {
                    
                }

            }
        });

        Integer[] days = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31};
        String[] months = {"", "Januari", "Februari", "Maart", "April", "Mei", "Juni", "Juli", "Augustus", "September", "Oktober", "November", "December"};

        ArrayAdapter<String> adapterMonths = new ArrayAdapter<String>(AddTask.this, android.R.layout.simple_spinner_item, months);
        adapterMonths.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        monthSpinner.setAdapter(adapterMonths);

        ArrayAdapter<Integer> adapterDays = new ArrayAdapter<Integer>(AddTask.this, android.R.layout.simple_spinner_item, days);
        adapterDays.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        daySpinner.setAdapter(adapterDays);



        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) +1;
        daySpinner.setSelection(day);
        monthSpinner.setSelection(month);

    }


    private void writeTask(Task task) {

        long taskNumber = task.getDocumentName();

        quitActivityCounter=0;

        collectionReference.document(String.valueOf(taskNumber))
                .set(task)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddTask.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                        Toast.makeText(AddTask.this, "Taak toegevoegd!", Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.INVISIBLE);
                        addTaskButton.setVisibility(View.INVISIBLE);
                        quitActivity();
                    }
                });

        long versionLong = task.getDocumentName();
        Map<String, Long> version = new HashMap<>();
        version.put("currentVersion", versionLong);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor preferencesEditor = preferences.edit();
        preferencesEditor.putLong("taskVersion", versionLong).apply();

        collectionReference.document("version")
                .set(version);

        db.collection("Clients").document(task.getClient())
                .update("taskNumbers", FieldValue.arrayUnion(taskNumber))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                        quitActivity();
                    }
                });

    }

    private void quitActivity(){
        quitActivityCounter++;

        if (quitActivityCounter==2) {
            alertbox.dismiss();
            finish();
        }
    }

    private List<String> getListStringEquipment(List<Equipment> equipmentList) {

        List<String> equipmentString = new ArrayList<>();
        for (Equipment e : equipmentList) {
            equipmentString.add(e.getDocumentName());
        }

        return equipmentString;

    }

    private ArrayList<String> getListStringEmployee(List<Employee> employeeList) {

        ArrayList<String> employeeString = new ArrayList<>();
        for (Employee e : employeeList) {
            employeeString.add(e.getDocumentName());
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String employeeLoggedIn = prefs.getString("employee", "employee");

        employeeString.add(employeeLoggedIn);

        Log.d("tag", "Adding employees to new task: " + Arrays.toString(employeeString.toArray()));

        return employeeString;

    }

    private int getDayOfYear(int day, String month) {

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, day);

        int month2 = 0;
        if (month.equals("Januari")) {
            month2 = 1;
        } else if (month.equals("Februari")) {
            month2 = 2;
        } else if (month.equals("Maart")) {
            month2 = 3;
        } else if (month.equals("April")) {
            month2 = 4;
        } else if (month.equals("Mei")) {
            month2 = 5;
        } else if (month.equals("Juni")) {
            month2 = 6;
        } else if (month.equals("Juli")) {
            month2 = 7;
        } else if (month.equals("Augustus")) {
            month2 = 8;
        } else if (month.equals("September")) {
            month2 = 9;
        } else if (month.equals("Oktober")) {
            month2 = 10;
        } else if (month.equals("November")) {
            month2 = 11;
        } else if (month.equals("December")) {
            month2 = 12;
        }

        calendar.set(Calendar.MONTH, month2 - 1);

        int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);

        return dayOfYear;

    }


    @Override
    protected void onResume() {
        super.onResume();

        if (client != null) {
            clientText.setText(client.getFirstName() + " " + client.getSurName());
        } else {
            clientText.setText("Nog niet gekozen");
        }
        if (!employeeList.isEmpty()) {
            String employeeString = "";
            for (Employee e : employeeList) {
                employeeString += e.getFirstName() + " - ";
            }
            employeeText.setText(employeeString);
        } else {
            employeeText.setText("Nog niet gekozen");
        }
        if (!equipmentList.isEmpty()) {
            String equipmentString = "";
            for (Equipment e : equipmentList) {
                equipmentString += e.getType() + " - ";
            }
            equipmentText.setText(equipmentString);
        } else {
            equipmentText.setText("Nog niet gekozen");
        }



    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == 1) { //Client
                client = (Client) data.getExtras().getSerializable("Client");
                System.out.println("CLIENT IS " + client.getDocumentName());
            }
        } else if (requestCode == 2) { //Employee
            if (resultCode == 1) {
                Bundle bundle = data.getBundleExtra("Bundle");
                employeeList = (ArrayList<Employee>) bundle.getSerializable("Employees");
            }

        } else {
            if (requestCode == 3) { //Equipment
                if (resultCode == 1) {
                    Bundle bundle = data.getBundleExtra("Bundle");
                    equipmentList = (List<Equipment>) bundle.getSerializable("Equipment");
                    System.out.println("EQUIPMENT IS ");
                    for (Equipment e : equipmentList) {
                        System.out.println(e.getType());
                    }
                }
            }

        }
    }

}
