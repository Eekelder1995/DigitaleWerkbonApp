package eekelder.digitalewerkbon;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SelectEmployee extends AppCompatActivity {

    private EditText employeeSearch;
    private ListView employeeView;
    private ArrayList<Employee> employeeListObject = new ArrayList<>();
    private ArrayList<Employee> employeeListSearch = new ArrayList<>();
    private ArrayList<Employee> employeeEquipment = new ArrayList<>();


    private static final String employeeCollection = "Employees";

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection(employeeCollection);
    private ArrayAdapter<Employee> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_employee);

        employeeSearch = findViewById(R.id.search_employee);
        employeeView = findViewById(R.id.list_employee);

        adapter = new ArrayAdapter<>(SelectEmployee.this, android.R.layout.simple_list_item_multiple_choice, employeeListSearch);
        employeeView.setAdapter(adapter);
        employeeView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);

        employeeView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Employee employee = (Employee) employeeView.getItemAtPosition(i);

                if (employeeEquipment.contains(employee)) {
                    employeeEquipment.remove(employee);
                } else {
                    employeeEquipment.add(employee);
                }

            }
        });


        employeeSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                String search = employeeSearch.getText().toString();
                String searchtrim = search.replaceAll(" ", "");
                String searchlowcase = searchtrim.toLowerCase();

                employeeListSearch.clear();

                for (Employee e : employeeListObject) {
                    if (e.getDocumentName().toLowerCase().contains(searchlowcase) && !searchlowcase.equals("")) {
                        employeeListSearch.add(e);
                    }
                }

                if (searchlowcase.equals("")) {
                    employeeListSearch.addAll(employeeListObject);
                }


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                        for (int i = 0; i < adapter.getCount(); i++) {
                            Employee employee = adapter.getItem(i);
                            if (employeeEquipment.contains(employee)) {
                                employeeView.setItemChecked(i, true);
                            } else {
                                employeeView.setItemChecked(i, false);

                            }
                        }
                    }
                });
            }
        });

        getEmployeeList();
        adapter.notifyDataSetChanged();

    }

    public void getEmployeeList() {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String usernameLoggedIn = prefs.getString("employee", "employee");


        employeeListObject.clear();
        Database mydb = Database.getInstance(getApplicationContext());
        employeeListObject.addAll(mydb.getEmployeeList());

//        Employee employeeLoggedIn = mydb.searchEmployeeByUserName(usernameLoggedIn);
//

        Employee employeeToRemove = null;
        for (Employee e : employeeListObject){
            if (e.getDocumentName().equals(usernameLoggedIn)){
                employeeToRemove = e;
            }
        }

        if (employeeToRemove!=null){
            employeeListObject.remove(employeeToRemove);
            Log.d("tag", "Removing Username for SelectEmployee " + usernameLoggedIn);
        }


        employeeListSearch.addAll(employeeListObject);

//        if (employeeListSearch.contains(employeeLoggedIn)){
//            Log.d("tag", "NOT Removed Username for EmployeeListSearch " + employeeLoggedIn.getDocumentName());
//            employeeListSearch.remove(employeeLoggedIn);
//        } else {
//            Log.d("tag", "SUCCESFULLY Removed Username for SelectEmployee " + employeeLoggedIn.getDocumentName());
//        }
//
//        for (Employee e : employeeListSearch){
//            Log.d("tag", "Employee " + e.getDocumentName());
//        }

//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                adapter.notifyDataSetChanged();
//            }
//        });


    }


    public void onBackPressed() {

        SparseBooleanArray checked = employeeView.getCheckedItemPositions();
        List<Employee> selectedEmployee = new ArrayList<>();

        for (int i = 0; i < employeeView.getAdapter().getCount(); i++) {
            if (checked.get(i)) {
                Employee employee = adapter.getItem(i);
                selectedEmployee.add(employee);
            }
        }

        Intent intent = new Intent();
        Bundle extras = new Bundle();
        setResult(1, intent);
        extras.putSerializable("Employees", (Serializable) selectedEmployee);
        intent.putExtra("Bundle", extras);
        finish();

    }

}
