package eekelder.digitalewerkbon;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ViewDays extends AppCompatActivity {

    private TextView monday;
    private TextView tuesday;
    private TextView wednesday;
    private TextView thursday;
    private TextView friday;
    private TextView saturday;
    private TextView sunday;
    private TextView intro;
    private TextView currentWeek;
    private TextView totalofWeek;
    private Button thisWeek;
    private Button nextWeek;
    private Button previousWeek;
    private int weekNumber;
    private int year;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_days);

        Calendar calendar = Calendar.getInstance();
        weekNumber = calendar.get(Calendar.WEEK_OF_YEAR);
        year = calendar.get(Calendar.YEAR);

        monday = findViewById(R.id.textView_monda);
        tuesday = findViewById(R.id.textView_tuesday);
        wednesday = findViewById(R.id.textView_wednesday);
        thursday = findViewById(R.id.textView_thursday);
        friday = findViewById(R.id.textView_friday);
        saturday = findViewById(R.id.textView_saturday);
        sunday = findViewById(R.id.textView_sunday);

        thisWeek = findViewById(R.id.button_this_week);
        thisWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                weekNumber = calendar.get(Calendar.WEEK_OF_YEAR);
                year = calendar.get(Calendar.YEAR);
                updateView();

            }
        });

        previousWeek = findViewById(R.id.button_week_back);
        previousWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (weekNumber == 1){
                    weekNumber = 52;
                    year--;
                } else {
                    weekNumber--;
                }
                updateView();
            }
        });

        nextWeek = findViewById(R.id.button_next_week);
        nextWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (weekNumber == 52){
                    weekNumber = 1;
                    year++;
                } else {
                    weekNumber++;
                }
                updateView();
            }
        });

        intro = findViewById(R.id.textView_viewdays_intro);
        intro.setText("In dit overzicht kunnen per week de gewerkte uren bekeken worden.");

        currentWeek = findViewById(R.id.textView_selected_week);
        totalofWeek = findViewById(R.id.textView_totalofweek);

        updateView();

    }

    private void updateView() {

        Database mydb = Database.getInstance(getApplicationContext());
        int counter = 0;
        for (int i=1; i<8; i++){

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.WEEK_OF_YEAR, weekNumber);
            calendar.set(Calendar.DAY_OF_WEEK,i+1);
            int dayNumber = calendar.get(Calendar.DAY_OF_YEAR);
            int dayNumberYear = dayNumber * 10000 + year;

            WorkingHours workhous = mydb.getWorkDay(dayNumberYear, dayNumber);
            String text = "";
            Date date = calendar.getTime();
            String fullDate = new SimpleDateFormat("dd/MM").format(date);
            text = getStringDayName(i) + " " + fullDate;

            if (workhous != null){
                int beginTime = workhous.getStartTime();
                int endTime = workhous.getEndTime();
                int pauseTime = workhous.getPauseTime();
                int duration = endTime - beginTime - pauseTime;
                counter += duration;
                int minutesStart = duration % 60;
                int hoursStart = (duration - minutesStart) / 60;
                String minutesStartString = String.valueOf(minutesStart);
                if (minutesStart < 10) {
                    minutesStartString = "0" + minutesStart;
                }

                text += ": " + hoursStart + ":" + minutesStartString + " uur." ;
            } else {

            }

            if (i==1){
                monday.setText(text);
            } else if (i==2) {
                tuesday.setText(text);
            } else if (i==3) {
                wednesday.setText(text);
            } else if (i==4) {
                thursday.setText(text);
            } else if (i==5) {
                friday.setText(text);
            } else if (i==6) {
                saturday.setText(text);
            } else if (i==7) {
                sunday.setText(text);
            }

        }

        currentWeek.setText("Geselecteerde week is: " + weekNumber + ", jaar is " + year);

        if (counter != 0) {
            int minutesStart = counter % 60;
            int hoursStart = (counter - minutesStart) / 60;
            String minutesStartString = String.valueOf(minutesStart);
            if (minutesStart < 10) {
                minutesStartString = "0" + minutesStart;
            }
            totalofWeek.setText("Totaal werkuren deze week: " + hoursStart + ":" + minutesStartString + " uur." );

        } else {
            totalofWeek.setText("Totaal werkuren deze week: 0.");
        }


    }

    private String getStringDayName(int i) {

        String dayName ="";
        if (i==1){
            dayName = "Maandag";
        } else if (i==2) {
            dayName = "Dinsdag";
        } else if (i==3) {
            dayName = "Woensdag";
        } else if (i==4) {
            dayName = "Donderdag";
        } else if (i==5) {
            dayName = "Vrijdag";
        } else if (i==6) {
            dayName = "Zaterdag";
        } else if (i==7) {
            dayName = "Zondag";
        }

        return dayName;

    }
}
