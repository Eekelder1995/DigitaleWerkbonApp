package eekelder.digitalewerkbon;

import android.content.Intent;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SelectEquipment extends AppCompatActivity {

    private EditText equipmentSearch;
    private ListView equipmentView;
    private List<Equipment> equipmentListObject = new ArrayList<>();
    private List<Equipment> equipmentListSearch = new ArrayList<>();
    private List<Equipment> selectedEquipment = new ArrayList<>();


    private static final String clientCollection = "Equipment";

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection(clientCollection);
    private ArrayAdapter<Equipment> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_equipment);

        equipmentSearch = findViewById(R.id.equipment_search);
        equipmentView = findViewById(R.id.list_equipment);

        adapter = new ArrayAdapter<>(SelectEquipment.this, android.R.layout.simple_list_item_multiple_choice, equipmentListSearch);
        equipmentView.setAdapter(adapter);
        equipmentView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);

        equipmentView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Equipment equipment = (Equipment) equipmentView.getItemAtPosition(i);

                if (selectedEquipment.contains(equipment)) {
                    selectedEquipment.remove(equipment);
                } else {
                    selectedEquipment.add(equipment);
                }

            }
        });

        getEquipmentList();

        equipmentSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                String search = equipmentSearch.getText().toString();
                String searchtrim = search.replaceAll(" ", "");
                String searchlowcase = searchtrim.toLowerCase();

                equipmentListSearch.clear();

                for (Equipment e : equipmentListObject) {
                    if (e.getType().toLowerCase().contains(searchlowcase) && !searchlowcase.equals("")) {
                        equipmentListSearch.add(e);
                    }
                }

                if (searchlowcase.equals("")) {
                    equipmentListSearch.addAll(equipmentListObject);
                }


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                        for (int i = 0; i < adapter.getCount(); i++) {
                            Equipment equipment = adapter.getItem(i);
                            if (selectedEquipment.contains(equipment)) {
                                equipmentView.setItemChecked(i, true);
                            } else {
                                equipmentView.setItemChecked(i, false);

                            }
                        }
                    }
                });
            }
        });


    }


    public void getEquipmentList() {
        Database mydb = Database.getInstance(getApplicationContext());
        equipmentListObject.addAll(mydb.getEquipmentList());
        equipmentListSearch.addAll(equipmentListObject);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });

    }


    public void onBackPressed() {

        SparseBooleanArray checked = equipmentView.getCheckedItemPositions();
        List<Equipment> selectedEquipment = new ArrayList<>();

        for (int i = 0; i < equipmentView.getAdapter().getCount(); i++) {
            if (checked.get(i)) {
                Equipment equipment = adapter.getItem(i);
                selectedEquipment.add(equipment);
            }
        }

        Intent intent = new Intent();
        Bundle extras = new Bundle();
        setResult(1, intent);
        extras.putSerializable("Equipment", (Serializable) selectedEquipment);
        intent.putExtra("Bundle", extras);
        finish();

    }

}
