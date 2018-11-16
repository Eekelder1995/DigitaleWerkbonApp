package eekelder.digitalewerkbon;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AllTasks extends AppCompatActivity {

    private Button dayForward;
    private Button dayBackward;
    private Button today;
    private int dayNumber;
    private boolean allTasks;
    private int month;
    private int day;

    private List<Task> tasks = new ArrayList<>();

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;

    private TextView selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_tasks);

        selectedDate = findViewById(R.id.textView_selected_date);

        Calendar calendar = Calendar.getInstance();
        dayNumber = calendar.get(Calendar.DAY_OF_YEAR);
        allTasks = false;

        Database mydb = Database.getInstance(getApplicationContext());
        tasks.addAll(mydb.getTaskListDay(dayNumber,allTasks));

        recyclerView = findViewById(R.id.recyclerview_tasks);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new RecycleAdapter(tasks, this);
        recyclerView.setAdapter(adapter);


        dayBackward = findViewById(R.id.button_day_back);
        dayForward = findViewById(R.id.button_day_forward);
        today = findViewById(R.id.button_today);

        dayBackward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dayNumber--;
                updateView();
            }
        });

        dayForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dayNumber++;
                updateView();
            }
        });

        today.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                dayNumber = calendar.get(Calendar.DAY_OF_YEAR);
                updateView();
            }
        });

        final Switch allTaskSwitch = findViewById(R.id.switch_alltasks);
        allTaskSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if (b){
                    allTasks = false;
                } else {
                    allTasks = true;
                }

                updateView();

                Log.d("tag", "Taakoverzicht wordt aangepast");
            }
        });

        updateView();
    }

    private void updateView() {

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, dayNumber);
        int day = calendar.get(Calendar.DAY_OF_MONTH) ;
        int month = calendar.get(Calendar.MONTH) + 1;

        String monthString = convertNumberToMonth(month);

        selectedDate.setText("Datum: " + day + " " + monthString);

        tasks.clear();

        Database mydb = Database.getInstance(getApplicationContext());
        tasks.addAll(mydb.getTaskListDay(dayNumber,allTasks));

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    private String convertNumberToMonth(int month) {
        String monthString = "";
        if (month==1){
            monthString = "Januari";
        } else if (month==2){
            monthString = "Februari";
        }else if (month==3){
            monthString = "Maart";
        }else if (month==4){
            monthString = "April";
        }else if (month==5){
            monthString = "Mei";
        }else if (month==6){
            monthString = "Juni";
        }else if (month==7){
            monthString = "Juli";
        }else if (month==8){
            monthString = "Augustus";
        }else if (month==9){
            monthString = "September";
        }else if (month==10){
            monthString = "Oktober";
        }else if (month==11){
            monthString = "November";
        }else if (month==12){
            monthString = "December";
        }

        return monthString;
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateView();
    }
}
