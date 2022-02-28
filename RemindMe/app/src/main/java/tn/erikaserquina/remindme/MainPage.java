package tn.erikaserquina.remindme;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import tn.erikaserquina.remindme.classes.Reminder;


public class MainPage extends AppCompatActivity {
    ArrayList<Reminder> reminderArrayList = new ArrayList<>();
    DatabaseReference databaseReference;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    String email;
    private FloatingActionButton add;
    private RecyclerView recyclerView;
    private Dialog dialog;
    AdapterReminders adapterReminders;
    TextView tvEmpty;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Getting data");
        progressDialog.setMessage("Loading please wait...");
        progressDialog.show();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        email = firebaseUser.getEmail();
        add = findViewById(R.id.floatingButton);
        tvEmpty = findViewById(R.id.empty);
        databaseReference = FirebaseDatabase.getInstance().getReference("reminders");
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addReminder();
            }
        });

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainPage.this);
        recyclerView.setLayoutManager(linearLayoutManager);
        getReminders();

    }

    public void getReminders() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                reminderArrayList.clear();
                Reminder sample = new Reminder("Hello! Welcome to Remind.Me","Sample@gmail.com","1","Sat Feb 19 11:30:00 GMT+08:00 2022");
                reminderArrayList.add(sample);
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Reminder reminder = dataSnapshot.getValue(Reminder.class);
                    Log.d("String", reminder.getMessage());
                    String _email = reminder.getEmail();
                    if (email.equalsIgnoreCase(_email)) {
                        reminderArrayList.add(reminder);
                    }
                }
                adapterReminders = new AdapterReminders(MainPage.this, reminderArrayList);
                recyclerView.setAdapter(adapterReminders);

                if (reminderArrayList.size() == 0) {
                    recyclerView.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    tvEmpty.setVisibility(View.GONE);
                }
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), error.getMessage().toString(), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }


    private void addReminder() {

        dialog = new Dialog(MainPage.this);
        dialog.setContentView(R.layout.floating_popup);

        final TextView textView = dialog.findViewById(R.id.date);
        Button add, cancel;
        LinearLayout selectDate;
        cancel = dialog.findViewById(R.id.cancelButton);
        selectDate = dialog.findViewById(R.id.DnT);
        add = dialog.findViewById(R.id.addButton);
        final EditText message = dialog.findViewById(R.id.message);

        final Calendar newCalender = Calendar.getInstance();
        selectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dialog = new DatePickerDialog(MainPage.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, final int year, final int month, final int dayOfMonth) {

                        final Calendar newDate = Calendar.getInstance();
                        Calendar newTime = Calendar.getInstance();
                        TimePickerDialog time = new TimePickerDialog(MainPage.this, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                                newDate.set(year, month, dayOfMonth, hourOfDay, minute, 0);
                                Calendar tem = Calendar.getInstance();
                                Log.w("TIME", System.currentTimeMillis() + "");
                                if ((newDate.getTimeInMillis() - tem.getTimeInMillis() > 0))
                                    textView.setText(newDate.getTime().toString());
                                else
                                    Toast.makeText(MainPage.this, "Invalid time", Toast.LENGTH_SHORT).show();

                            }
                        }, newTime.get(Calendar.HOUR), newTime.get(Calendar.MINUTE), false);
                        time.show();

                    }
                }, newCalender.get(Calendar.YEAR), newCalender.get(Calendar.MONTH), newCalender.get(Calendar.DAY_OF_MONTH));

                dialog.getDatePicker().setMinDate(System.currentTimeMillis());
                dialog.show();

            }
        });


        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //new code for firebase
                if (textView.getText().toString().isEmpty()) {
                    textView.setError("Select valid date");
                    textView.requestFocus();
                } else {
                    Date remind = new Date(textView.getText().toString().trim());

                    int random_id = getTime();

                    Reminder reminders1 = new Reminder(message.getText().toString(), email, String.valueOf(random_id), textView.getText().toString());
                    databaseReference.child(String.valueOf(random_id)).setValue(reminders1);

                    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8:00"));
                    calendar.setTime(remind);
                    calendar.set(Calendar.SECOND, 0);
                    Intent intent = new Intent(MainPage.this, NotifierAlarm.class);
                    intent.putExtra("Message", message.getText().toString());
                    intent.putExtra("RemindDate", remind);
                    intent.putExtra("id", String.valueOf(random_id));


                    PendingIntent intent1 = PendingIntent.getBroadcast(MainPage.this, random_id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), intent1);

                    Toast.makeText(MainPage.this, "Reminder Inserted Successfully", Toast.LENGTH_SHORT).show();
                    getReminders();
                    dialog.dismiss();

                }


            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.setCancelable(false);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    //MENU TOP BARRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    public void AccountBtn(MenuItem item) {
        Intent intent = new Intent(MainPage.this, UserAcc.class);
        startActivity(intent);
    }

    public void APIbtn(MenuItem item) {
        Intent intent = new Intent(MainPage.this, APIActivity.class);
        startActivity(intent);
    }

    public static int getTime() {
        return (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
    }


}