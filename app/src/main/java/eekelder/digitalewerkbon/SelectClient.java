package eekelder.digitalewerkbon;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
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

public class SelectClient extends AppCompatActivity {

    private EditText clientSearch;
    private ListView clientsView;
    private List<Client> clientListObject = new ArrayList<>();
    private List<Client> clientListSearch = new ArrayList<>();


    private static final String clientCollection = "Clients";

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection(clientCollection);
    private ArrayAdapter<Client> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_client);

        clientSearch = findViewById(R.id.client_search);
        clientsView = findViewById(R.id.list_client);

        adapter = new ArrayAdapter<>(SelectClient.this, android.R.layout.simple_list_item_single_choice, clientListSearch);
        clientsView.setAdapter(adapter);
        clientsView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

        clientsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Client client = (Client) clientsView.getItemAtPosition(i);

                Intent intent = new Intent();
                intent.putExtra("Client", client);
                setResult(1, intent);
                finish();
            }
        });

        getClientList();

        clientSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                String search = clientSearch.getText().toString();
                String searchtrim = search.replaceAll(" ", "");
                String searchlowcase = searchtrim.toLowerCase();

                clientListSearch.clear();

                for (Client c : clientListObject) {
                    if (c.getDocumentName().toLowerCase().contains(searchlowcase) && !searchlowcase.equals("")) {
                        clientListSearch.add(c);
                    }
                }

                if (searchlowcase.equals("")) {
                    clientListSearch.addAll(clientListObject);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });

    }


    public void getClientList() {

        Database mydb = Database.getInstance(getApplicationContext());
        clientListObject.addAll(mydb.getClientList());
        clientListSearch.addAll(clientListObject);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });

    }

}
