package eekelder.digitalewerkbon;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.ViewHolder> {

    private List<Task> tasks;
    private Context context;

    public RecycleAdapter(List<Task> tasks, Context context) {
        this.tasks = tasks;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycleview_list_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Task task = tasks.get(position);

        Database mydb = Database.getInstance(context);
        ExecuteStatus.setStatus(ExecuteStatus.getStatus()+1);

        EmployeeTimes employeeTimes = mydb.getEmployeeTimes(task.getDocumentName());

        int minutesStart = task.getStartTime() % 60;
        int hoursStart = (task.getStartTime() - minutesStart) / 60;
        String minutesStartString = String.valueOf(minutesStart);
        if (minutesStart < 10) {
            minutesStartString = "0" + minutesStart;
        }

        String status = "Niet aangemeld voor deze taak";

        int selectedColor = ContextCompat.getColor(context, R.color.cardview_light_background);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String usernameLoggedIn = prefs.getString("employee", "employee");
        if (task.getEmployees().contains(usernameLoggedIn)) {
            if (employeeTimes==null) {
                status = "Deze taak is nog niet begonnen";
                selectedColor =ContextCompat.getColor(context, R.color.greentask);
            } else if (employeeTimes.getEndTime()==0) {

                if (employeeTimes.getPauseStartTime()==0) {

                    int minutesStart2 = employeeTimes.getStartTime() % 60;
                    int hoursStart2 = (employeeTimes.getStartTime() - minutesStart2) / 60;
                    String minutesStartString2 = String.valueOf(minutesStart2);
                    if (minutesStart2 < 10) {
                        minutesStartString2 = "0" + minutesStart2;
                    }
                    status = "Begonnen om " + hoursStart2 + ":" + minutesStartString2;
                    selectedColor = ContextCompat.getColor(context, R.color.orangetask);
                } else {
                    int minutesStart2 = employeeTimes.getStartTime() % 60;
                    int hoursStart2 = (employeeTimes.getStartTime() - minutesStart2) / 60;
                    String minutesStartString2 = String.valueOf(minutesStart2);
                    if (minutesStart2 < 10) {
                        minutesStartString2 = "0" + minutesStart2;
                    }
                    status = "Begonnen om " + hoursStart2 + ":" + minutesStartString2 + ", NU GEPAUZEERD";
                    selectedColor = ContextCompat.getColor(context, R.color.pausetask);
                }
            } else if (employeeTimes.getEndTime()!= 0) {
                status = "Deze taak is voltooid";
                selectedColor = ContextCompat.getColor(context, R.color.redtask);
            }
        }

        holder.header.setText(hoursStart + ":" + minutesStartString + " - " + task.getClient() + " - " + task.getDescription());
        holder.description.setText(status);
        holder.linearLayout.setBackgroundColor(selectedColor);

        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ViewTask.class);
                intent.putExtra("task",task);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView header;
        public TextView description;
        public LinearLayout linearLayout;

        public ViewHolder(View itemView) {
            super(itemView);

            header = itemView.findViewById(R.id.textViewHead);
            description = itemView.findViewById(R.id.textViewDescription);
            linearLayout = itemView.findViewById(R.id.linear_layout_recycle);
        }
    }

}
