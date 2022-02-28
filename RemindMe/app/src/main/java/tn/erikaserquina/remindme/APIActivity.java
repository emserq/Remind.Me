package tn.erikaserquina.remindme;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class APIActivity extends AppCompatActivity {
    public static ArrayList<String> monthArrayList = new ArrayList<>();
    private DatePickerDialog.OnDateSetListener dateSetListener;
    ProgressDialog progressDialog;
    Button btnDate, btnMonth;
    StringRequest stringRequest;
    RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apiactivity);
        progressDialog = new ProgressDialog(this);
        btnDate = findViewById(R.id.btnDate);
        btnMonth = findViewById(R.id.btnMonth);

        btnMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addMonth();
                ListView listView = new ListView(APIActivity.this);
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(APIActivity.this, android.R.layout.simple_list_item_1, monthArrayList);
                listView.setAdapter(arrayAdapter);

                android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(APIActivity.this);
                alert.setCancelable(false);
                alert.setView(listView);
                final android.app.AlertDialog dialog = alert.create();

                dialog.show();

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        getHolidayByMonth(i + 1);
                        dialog.dismiss();
                    }
                });
            }
        });

        btnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(APIActivity.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth, dateSetListener, year, month, day);
                datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                datePickerDialog.show();


            }
        });
        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                month = month + 1;
                getHolidays(month, currentYear - 1, day);

            }
        };

    }

    private void addMonth() {
        monthArrayList.clear();
        monthArrayList.add("JANUARY");
        monthArrayList.add("FEBRUARY");
        monthArrayList.add("MARCH");
        monthArrayList.add("APRIL");
        monthArrayList.add("MAY");
        monthArrayList.add("JUNE");
        monthArrayList.add("JULY");
        monthArrayList.add("AUGUST");
        monthArrayList.add("SEPTEMBER");
        monthArrayList.add("OCTOBER");
        monthArrayList.add("NOVEMBER");
        monthArrayList.add("DECEMBER");
    }

    private void getHolidayByMonth(int month) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setTitle("Getting data");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        int currentYear = Calendar.getInstance().get(Calendar.YEAR) - 1;
        String sMonth = String.valueOf(month);


        final String url = "https://holidayapi.com/v1/holidays?pretty&key=617c4124-f7ce-4526-982e-912670661372&country=PH&month=" + sMonth + "&year=" + currentYear;

        stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);

                    JSONArray holidays = jsonObject.getJSONArray("holidays");

                    if (holidays.length() == 0) {
                        AlertDialog.Builder alert = new AlertDialog.Builder(APIActivity.this);
                        alert.setMessage("No holiday for today");
                        alert.setTitle("No Holiday Found");
                        alert.setCancelable(true);
                        alert.show();
                    } else {
                        String output = "";
                        for (int i = 0; i < holidays.length(); i++) {
                            JSONObject oHoliday = holidays.getJSONObject(i);

                            String name = oHoliday.getString("name");
                            String date = oHoliday.getString("date");

                            String holiday = name+" on "+date;

                            output=output+holiday+"\n";

                        }


                        AlertDialog.Builder alert = new AlertDialog.Builder(APIActivity.this);
                        alert.setMessage(output);
                        alert.setTitle(monthArrayList.get(month-1));
                        alert.setCancelable(true);
                        alert.show();

                    }
                    progressDialog.dismiss();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(APIActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse.statusCode == 404) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(APIActivity.this);
                    alert.setMessage(error.getMessage().toUpperCase(Locale.ROOT) + "!");
                    alert.setTitle("ERROR");
                    alert.setCancelable(true);
                    alert.show();

                    progressDialog.dismiss();
                } else {
                    AlertDialog.Builder alert = new AlertDialog.Builder(APIActivity.this);
                    alert.setMessage("Something went wrong, try again!");
                    alert.setTitle("ERROR");
                    alert.setCancelable(true);
                    alert.show();
                    progressDialog.dismiss();
                }
            }
        });
        requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void getHolidays(int month, int year, int day) {

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setTitle("Getting data");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        String sMonth = String.valueOf(month);
        String sYear = String.valueOf(year);
        String sDay = String.valueOf(day);

        final String url = "https://holidayapi.com/v1/holidays?pretty&key=617c4124-f7ce-4526-982e-912670661372&country=PH&month=" + sMonth + "&year=" + sYear + "&day=" + sDay;

        stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);

                    JSONArray holidays = jsonObject.getJSONArray("holidays");

                    if (holidays.length() == 0) {
                        AlertDialog.Builder alert = new AlertDialog.Builder(APIActivity.this);
                        alert.setMessage("No holiday on "+sYear+"-"+sMonth+"-"+sDay);
                        alert.setTitle("No Holiday Found");
                        alert.setCancelable(true);
                        alert.show();
                    } else {
                        JSONObject oHoliday = holidays.getJSONObject(0);

                        String name = oHoliday.getString("name");

                        AlertDialog.Builder alert = new AlertDialog.Builder(APIActivity.this);
                        alert.setMessage(name);
                        alert.setTitle("Holiday Found");
                        alert.setCancelable(true);
                        alert.show();

                    }
                    progressDialog.dismiss();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(APIActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse.statusCode == 404) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(APIActivity.this);
                    alert.setMessage(error.getMessage().toUpperCase(Locale.ROOT) + "!");
                    alert.setTitle("ERROR");
                    alert.setCancelable(true);
                    alert.show();

                    progressDialog.dismiss();
                } else {
                    AlertDialog.Builder alert = new AlertDialog.Builder(APIActivity.this);
                    alert.setMessage("Something went wrong, try again!");
                    alert.setTitle("ERROR");
                    alert.setCancelable(true);
                    alert.show();
                    progressDialog.dismiss();
                }
            }
        });
        requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

//    public void test() {
//        final String url = "https://holidayapi.com/v1/holidays?pretty&key=617c4124-f7ce-4526-982e-912670661372&country=PH&month=4&year=2021&day=1";
//        word = etWord.getText().toString();
//        if (word.isEmpty()) {
//            etWord.setError("Enter word to search");
//            etWord.requestFocus();
//        } else {
//            progressDialog = new ProgressDialog(this);
//            progressDialog.setMessage("Please wait...");
//            progressDialog.setTitle("Getting meaning");
//            progressDialog.setCanceledOnTouchOutside(false);
//            progressDialog.show();
//            String urlQuery = url + word;
//            stringRequest = new StringRequest(Request.Method.GET, urlQuery, new Response.Listener<String>() {
//                @Override
//                public void onResponse(String response) {
//                    word = "";
//                    etWord.getText().clear();
//                    Log.d("response", urlQuery);
//                    try {
//                        JSONArray jsonArray = new JSONArray(response);
//                        JSONObject jsonObject = jsonArray.getJSONObject(0);
//
//                        String word = jsonObject.getString("word");
//
//                        JSONArray meanings = jsonObject.getJSONArray("meanings");
//
//                        JSONObject meaning1 = meanings.getJSONObject(0);
//                        String partOfSpeech = meaning1.getString("partOfSpeech");
//
//                        JSONArray definitions = meaning1.getJSONArray("definitions");
//
//                        JSONObject definition = definitions.getJSONObject(0);
//
//                        String _definition = definition.getString("definition");
//                        progressDialog.dismiss();
//                        openDialog(word, _definition);
//
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }, new Response.ErrorListener() {
//                @Override
//                public void onErrorResponse(VolleyError error) {
//                    if (error.networkResponse.statusCode == 404) {
//                        AlertDialog.Builder alert = new AlertDialog.Builder(DictionaryActivity.this);
//                        alert.setMessage(word + " not found!");
//                        alert.setTitle("ERROR");
//                        alert.setCancelable(true);
//                        alert.show();
//                        word = "";
//                        etWord.getText().clear();
//                        progressDialog.dismiss();
//                    } else {
//                        AlertDialog.Builder alert = new AlertDialog.Builder(DictionaryActivity.this);
//                        alert.setMessage("Something went wrong, try again!");
//                        alert.setTitle("ERROR");
//                        alert.setCancelable(true);
//                        alert.show();
//                        word = "";
//                        etWord.getText().clear();
//                        progressDialog.dismiss();
//                    }
//                }
//            });
//            requestQueue = Volley.newRequestQueue(this);
//            requestQueue.add(stringRequest);
//        }
//    }


}