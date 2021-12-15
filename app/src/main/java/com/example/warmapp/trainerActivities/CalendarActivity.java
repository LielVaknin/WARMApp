package com.example.warmapp.trainerActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;
import com.example.warmapp.R;
import com.example.warmapp.classes.MyTrainingsAdapter;
import com.example.warmapp.classes.Training;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.michaelmuenzer.android.scrollablennumberpicker.ScrollableNumberPicker;
import com.michaelmuenzer.android.scrollablennumberpicker.ScrollableNumberPickerListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class CalendarActivity extends AppCompatActivity {

    CalendarView calendarView;
    MaterialButton addTrainingButtonDialog;
    RecyclerView recyclerViewMyTrainings;
    MyTrainingsAdapter myTrainingsAdapter;
    ArrayList<Training> trainingsToShow;
    List<EventDay> events;
    Dialog dialogAddTraining;

    //dialog
    ImageView exitIcon;
    TextInputLayout spinnerTitle;
    AutoCompleteTextView autoCompleteTitle;
    ArrayAdapter<String> arrayAdapterSpinnerTitle;
    TextInputLayout textInputLayoutFeatures;
    TextInputEditText textInputEditFeatures;
    ScrollableNumberPicker scrollableNumberPicker;
    TextInputLayout textInputLayoutDate;
    TextInputEditText textInputEditDate;
    TextInputLayout textInputLayoutStartTime;
    TextInputEditText textInputEditStartTime;
    TextInputLayout textInputLayoutEndTime;
    TextInputEditText textInputEditEndTime;
    TextInputLayout spinnerCity;
    AutoCompleteTextView autoCompleteCity;
    ArrayAdapter<String> arrayAdapterSpinnerCity;
    TextInputEditText textInputEditAddress;
    Slider slider;
    TextInputEditText textInputEditDetails;
    MaterialButton addTrainingButton;

    //input user
    String title;
    HashMap<String, String> featuresUser;
    int participants;
    String date;
    String startTime;
    String endTime;
    String city;
    String address;
    int price;
    String details;
    boolean[] selectedFeatures;
    ArrayList<Integer> features;

    private FirebaseAuth auth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_calendar);

        initViews();
        putTrainingsDayOnCalendar();

        calendarView.setOnDayClickListener(new OnDayClickListener() {
            @Override
            public void onDayClick(@NonNull EventDay eventDay) {
                Calendar clickedDayCalendar = eventDay.getCalendar();
                SimpleDateFormat simpleFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
                String date = simpleFormat.format(clickedDayCalendar.getTime());
                showTrainingsOnSelectedDate(date);
            }
        });


        addTrainingButtonDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddTrainingDialog();
            }
        });

    }

    private void initViews() {
        //activity
        calendarView = findViewById(R.id.calendarView);
        calendarView.setCalendarDayLayout(R.layout.layout_custom_calendar);
        addTrainingButtonDialog = findViewById(R.id.button_add_training);
        recyclerViewMyTrainings = findViewById(R.id.recycle_view_my_trainings);

        //user
//        auth = FirebaseAuth.getInstance();
    }

    private void putTrainingsDayOnCalendar() {
        //get userID
        String userID = "Z1QT2iiO0ZVOMg7E8vph4TQQLT32";
        events = new ArrayList<>();

        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child("Trainer").child(userID).child("trainings");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            int counter = 0;
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshotMain) {
                for (DataSnapshot dataSnapshot : snapshotMain.getChildren()) {
                    FirebaseDatabase.getInstance().getReference("Trainings").child(Objects.requireNonNull(dataSnapshot.getKey()))
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    counter++;
                                    Training training = snapshot.getValue(Training.class);

                                    SimpleDateFormat simpleFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
                                    Calendar calendar = Calendar.getInstance();
                                    try {
                                        calendar.setTime(Objects.requireNonNull(simpleFormat.parse(Objects.requireNonNull(training).getDate())));
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                    calendar.add(Calendar.DAY_OF_MONTH,0);
                                    events.add(new EventDay(calendar, R.drawable.ic_minus));

                                    if(counter == snapshotMain.getChildrenCount()){
                                        calendarView.setEvents(events);
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void showTrainingsOnSelectedDate(String selectedDate) {
        //init recycleView
        recyclerViewMyTrainings.setHasFixedSize(true);
        recyclerViewMyTrainings.setLayoutManager(new LinearLayoutManager(this));

        trainingsToShow = new ArrayList<>();
        myTrainingsAdapter = new MyTrainingsAdapter(this, trainingsToShow);
        recyclerViewMyTrainings.setAdapter(myTrainingsAdapter);

        //get userID
        String userID = "Z1QT2iiO0ZVOMg7E8vph4TQQLT32";

        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child("Trainer").child(userID).child("trainings");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    FirebaseDatabase.getInstance().getReference("Trainings").child(Objects.requireNonNull(dataSnapshot.getKey()))
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    Training training = snapshot.getValue(Training.class);
                                    if(selectedDate.equals(training.getDate())){
                                        trainingsToShow.add(training);
                                    }
                                    myTrainingsAdapter.notifyDataSetChanged();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void showAddTrainingDialog() {
        dialogAddTraining = new Dialog(CalendarActivity.this);
        dialogAddTraining.setContentView(R.layout.layout_add_training);
        dialogAddTraining.getWindow().setBackgroundDrawable(getDrawable(R.drawable.edit_text_bg));
        dialogAddTraining.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogAddTraining.setCancelable(false);
        dialogAddTraining.getWindow().getAttributes().windowAnimations = R.style.animation;
        dialogAddTraining.show();

        //init views in dialog
        initViewsDialog();

        //exit dialog
        exitIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogAddTraining.dismiss();
            }
        });

        //init list in title
        autoCompleteTitle.setAdapter(arrayAdapterSpinnerTitle);
        autoCompleteTitle.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                title = parent.getItemAtPosition(position).toString();
            }
        });

        //init list in features
        features = new ArrayList<>();
        featuresUser = new HashMap<>();
        selectedFeatures = new boolean[getResources().getStringArray(R.array.Features).length];
        textInputEditFeatures.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPickerFeaturesDialog();
            }
        });

        //number picker
        scrollableNumberPicker.setListener(new ScrollableNumberPickerListener() {
            @Override
            public void onNumberPicked(int value) {
                participants = value;
            }
        });

        //date and time
        //keyboard won't open by clicking on the text field
        textInputEditDate.setInputType(InputType.TYPE_NULL);
        textInputEditStartTime.setInputType(InputType.TYPE_NULL);
        textInputEditEndTime.setInputType(InputType.TYPE_NULL);
        textInputEditFeatures.setInputType(InputType.TYPE_NULL);
        //the user not be able to input any text
        textInputEditDate.setKeyListener(null);
        textInputEditStartTime.setKeyListener(null);
        textInputEditEndTime.setKeyListener(null);
        textInputEditFeatures.setKeyListener(null);

        textInputEditDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateDialog();
            }
        });

        textInputEditStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showStartTimeDialog();
            }
        });

        textInputEditEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEndTimeDialog();
            }
        });

        //init list in city
        autoCompleteCity.setAdapter(arrayAdapterSpinnerCity);
        autoCompleteCity.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                city = parent.getItemAtPosition(position).toString();
            }
        });

        //seekbar
        slider.setLabelFormatter(new LabelFormatter() {
            @NonNull
            @Override
            public String getFormattedValue(float value) {
                price = (int) value;
                return (int) value + "₪";
            }
        });

        auth = FirebaseAuth.getInstance();
        //databaseReference = FirebaseDatabase.getInstance().getReference("Trainings");
        //String trainingID = auth.getCurrentUser().getUid();
        String trainerID = "Z1QT2iiO0ZVOMg7E8vph4TQQLT32";

        addTrainingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseReference = FirebaseDatabase.getInstance().getReference("Trainings");
                address = textInputEditAddress.getText().toString();
                details = textInputEditDetails.getText().toString();
                addTraining(title, city, address, featuresUser, trainerID, startTime, endTime, date, price, details, participants);
                Toast.makeText(CalendarActivity.this, "The training was added", Toast.LENGTH_LONG).show();
                dialogAddTraining.dismiss();
            }
        });
    }

    private void showDateDialog() {
//        Calendar calendar = Calendar.getInstance();
//        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
//            @SuppressLint("SetTextI18n")
//            @Override
//            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
//                month = month + 1;
//                String date = dayOfMonth + "/" + (month) + "/" + year;
//                dateText.setText(date);
//            }
//        };
//        new DatePickerDialog(CalendarActivity.this, dateSetListener,
//                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();

        MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select a date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setCalendarConstraints(new CalendarConstraints.Builder().setValidator(DateValidatorPointForward.now()).build())
                .build();

        materialDatePicker.show(getSupportFragmentManager(), "Date picker");

        materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener() {
            @Override
            public void onPositiveButtonClick(Object selection) {
                SimpleDateFormat simpleFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
                textInputEditDate.setText(simpleFormat.format(selection));
                date = simpleFormat.format(selection);
                //Log.d("picker date:" ,simpleFormat.format(selection));
            }
        });
    }

    private void showStartTimeDialog() {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                String startTimeTraining;
                if (minute <= 9) {
                    startTimeTraining = hourOfDay + ":" + "0" + minute;
                } else {
                    startTimeTraining = hourOfDay + ":" + minute;
                }
                textInputEditStartTime.setText(startTimeTraining);
                startTime = startTimeTraining;
            }
        };
        new TimePickerDialog(CalendarActivity.this, timeSetListener,
                calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show();
    }

    private void showEndTimeDialog() {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                String endTimeTraining;
                if (minute <= 9) {
                    endTimeTraining = hourOfDay + ":" + "0" + minute;
                } else {
                    endTimeTraining = hourOfDay + ":" + minute;
                }
                textInputEditEndTime.setText(endTimeTraining);
                endTime = endTimeTraining;
            }
        };
        new TimePickerDialog(CalendarActivity.this, timeSetListener,
                calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show();
    }

    private void showPickerFeaturesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                CalendarActivity.this
        );
        builder.setTitle("select features");
        builder.setCancelable(false);
        builder.setMultiChoiceItems(getResources().getStringArray(R.array.Features), selectedFeatures, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if (isChecked) {
                    features.add(which);
                } else {
                    features.remove((Object) which);
                }
            }
        });
        builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                StringBuilder stringBuilder = new StringBuilder();
                featuresUser.clear();
                String featureName;
                for (int i = 0; i < features.size(); i++) {
                    featureName = getResources().getStringArray(R.array.Features)[features.get(i)];
                    stringBuilder.append(featureName);
                    featuresUser.put(featureName, featureName);
                    if (i != features.size() - 1) {
                        stringBuilder.append(", ");
                    }
                }
                textInputEditFeatures.setText(stringBuilder.toString());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setNeutralButton("Clear All", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for (int i = 0; i < selectedFeatures.length; i++) {
                    selectedFeatures[i] = false;
                    features.clear();
                    textInputEditFeatures.setText("");
                }
            }
        });
        builder.show();
    }

    private void addTraining(String title, String city, String address, HashMap<String, String> features, String trainerId, String startTraining, String endTraining, String date, int price, String details, int maxParticipants) {
        String getUniqueTrainingID = databaseReference.push().getKey();
        Training training = new Training(getUniqueTrainingID, title, city, address, trainerId, startTraining, endTraining, date, details, price, maxParticipants, features);
        FirebaseDatabase.getInstance().getReference("Users").child(trainerId).child("trainings").child(getUniqueTrainingID).setValue(true);
        databaseReference.child(Objects.requireNonNull(getUniqueTrainingID)).setValue(training);
    }

    private void initViewsDialog() {
        //exit icon
        exitIcon = dialogAddTraining.findViewById(R.id.exit_dialog);
        //spinner title
        spinnerTitle = dialogAddTraining.findViewById(R.id.spinner_title);
        autoCompleteTitle = dialogAddTraining.findViewById(R.id.auto_complete_title);
        arrayAdapterSpinnerTitle = new ArrayAdapter<>(getApplicationContext(),
                R.layout.spinner_dropdown_item, getResources().getStringArray(R.array.Titles));
        //dialog select features
        textInputEditFeatures = dialogAddTraining.findViewById(R.id.text_input_edit_features);
        textInputLayoutFeatures = dialogAddTraining.findViewById(R.id.text_input_layout_features);
        //number picker
        scrollableNumberPicker = dialogAddTraining.findViewById(R.id.scroll_number_picker);
        //date and time
        textInputEditDate = dialogAddTraining.findViewById(R.id.text_input_edit_date);
        textInputLayoutDate = dialogAddTraining.findViewById(R.id.text_input_layout_date);
        textInputEditStartTime = dialogAddTraining.findViewById(R.id.text_input_edit_start_time);
        textInputLayoutStartTime = dialogAddTraining.findViewById(R.id.text_input_layout_start_time);
        textInputEditEndTime = dialogAddTraining.findViewById(R.id.text_input_edit_end_time);
        textInputLayoutEndTime = dialogAddTraining.findViewById(R.id.text_input_layout_end_time);
        //spinner city
        spinnerCity = dialogAddTraining.findViewById(R.id.spinner_cities);
        autoCompleteCity = dialogAddTraining.findViewById(R.id.auto_complete_cities);
        arrayAdapterSpinnerCity = new ArrayAdapter<>(getApplicationContext(),
                R.layout.spinner_dropdown_item, getResources().getStringArray(R.array.Cities));
        //address
        textInputEditAddress = dialogAddTraining.findViewById(R.id.text_input_edit_address);
        //slider
        slider = dialogAddTraining.findViewById(R.id.slider_money);
        //details
        textInputEditDetails = dialogAddTraining.findViewById(R.id.text_input_edit_details);
        //button add training
        addTrainingButton = dialogAddTraining.findViewById(R.id.button_add_training);
    }
}