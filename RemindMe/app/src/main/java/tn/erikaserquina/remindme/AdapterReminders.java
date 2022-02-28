package tn.erikaserquina.remindme;


import static android.content.Context.ALARM_SERVICE;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import tn.erikaserquina.remindme.classes.Reminder;

public class AdapterReminders extends RecyclerView.Adapter<AdapterReminders.MyViewHolder> {
    private static ArrayList<Reminder> reminderArrayList = new ArrayList<>();
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("reminders");
    Dialog dialog;
    private Context context;

    public AdapterReminders(Context context, ArrayList<Reminder> reminderArrayList) {
        this.context = context;
        this.reminderArrayList = reminderArrayList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(context).inflate(R.layout.reminder_item, viewGroup, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {

        myViewHolder.tv2.setText(reminderArrayList.get(i).getRemindDate().toString());
        myViewHolder.tv1.setText(reminderArrayList.get(i).getMessage());
        myViewHolder.tvId.setText(reminderArrayList.get(i).getId());

    }

    @Override
    public int getItemCount() {
        return reminderArrayList.size();
    }

    private void updateReminder(String id, String updateMessage, Date updateDate, Dialog dialog) {
        HashMap user = new HashMap();
        user.put("id", id);
        user.put("message", updateMessage);
        user.put("remindDate", updateDate.toString());

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00"));
        calendar.setTime(updateDate);
        calendar.set(Calendar.SECOND, 0);
        Intent intent = new Intent(context, NotifierAlarm.class);
        intent.putExtra("Message", updateMessage);
        intent.putExtra("RemindDate", updateDate);
        intent.putExtra("id", String.valueOf(id));


        PendingIntent intent1 = PendingIntent.getBroadcast(context, Integer.parseInt(id), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getApplicationContext().getSystemService(ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), intent1);

        databaseReference.child(id).updateChildren(user).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    Toast.makeText(context, "Updated Successfully", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(context, "Not updated", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            }
        });
    }

    private void deleteReminder(String id, Dialog dialog) {
        databaseReference.child(id).removeValue();
        dialog.dismiss();
        //
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00"));
        calendar.set(Calendar.SECOND, 0);
        Intent intent = new Intent(context, NotifierAlarm.class);

        PendingIntent intent1 = PendingIntent.getBroadcast(context, Integer.parseInt(id), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getApplicationContext().getSystemService(ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), intent1);

        alarmManager.cancel(intent1);

        Toast.makeText(context, "Reminder Removed Successfully", Toast.LENGTH_SHORT).show();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tv1, tv2, tvId;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tv1 = itemView.findViewById(R.id.textView1);
            tv2 = itemView.findViewById(R.id.textView2);
            tvId = itemView.findViewById(R.id.tvId);
            Calendar newDate = Calendar.getInstance();
            Calendar newTime = Calendar.getInstance();
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog = new Dialog(context);
                    dialog.setContentView(R.layout.floating_popup_update_delete);
                    String _date = tv2.getText().toString();
                    String _message = tv1.getText().toString();
                    String _id = tvId.getText().toString();

                    TextView textView = dialog.findViewById(R.id.date);
                    LinearLayout select = dialog.findViewById(R.id.DnTUpdate);
                    Button cancel = dialog.findViewById(R.id.cancelButton);
                    Button update = dialog.findViewById(R.id.updateButton);
                    Button delete = dialog.findViewById(R.id.deleteButton);
                    EditText message = dialog.findViewById(R.id.message);

                    textView.setText(_date);
                    message.setText(_message);

                    Calendar newCalender = Calendar.getInstance();
                    select.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            DatePickerDialog dialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker view, final int year, final int month, final int dayOfMonth) {


                                    TimePickerDialog time = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                                        @Override
                                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                                            newDate.set(year, month, dayOfMonth, hourOfDay, minute, 0);
                                            Calendar tem = Calendar.getInstance();
                                            Log.w("TIME", System.currentTimeMillis() + "");
                                            if (newDate.getTimeInMillis() - tem.getTimeInMillis() > 0)
                                                textView.setText(newDate.getTime().toString());
                                            else
                                                Toast.makeText(context, "Invalid time", Toast.LENGTH_SHORT).show();

                                        }
                                    }, newTime.get(Calendar.HOUR), newTime.get(Calendar.MINUTE), false);
                                    time.show();

                                }
                            }, newCalender.get(Calendar.YEAR), newCalender.get(Calendar.MONTH), newCalender.get(Calendar.DAY_OF_MONTH));

                            dialog.getDatePicker().setMinDate(System.currentTimeMillis());
                            dialog.show();

                        }
                    });
                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                        }
                    });
                    delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            deleteReminder(_id, dialog);
                        }
                    });
                    update.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String updateMessage = message.getText().toString();
                            updateReminder(_id, updateMessage, newDate.getTime(), dialog);
                        }
                    });
                    dialog.setCancelable(false);
                    dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.show();
                }
            });
        }
    }


}