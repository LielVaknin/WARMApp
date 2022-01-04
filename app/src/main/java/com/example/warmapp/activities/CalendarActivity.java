package com.example.warmapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
import com.example.warmapp.adapters.MyTrainingsAdapter;
import com.example.warmapp.classes.Training;
import com.example.warmapp.classes.TrainingModel;
import com.example.warmapp.classes.User;
import com.example.warmapp.classes.UserTrainer;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.michaelmuenzer.android.scrollablennumberpicker.ScrollableNumberPicker;
import com.michaelmuenzer.android.scrollablennumberpicker.ScrollableNumberPickerListener;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class CalendarActivity extends AppCompatActivity {
    //activity
    private BottomNavigationView bottomNavigationView;
    private CalendarView calendarView;
    private Calendar calendar;
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
    private FloatingActionButton addTrainingButtonDialog;
    private RecyclerView recyclerViewMyTrainings;
    private ArrayList<TrainingModel> userTrainings;
    private List<EventDay> events;
    private Dialog dialogAddTraining;
    private ProgressDialog progressDialog;

    //dialog add training
    private ImageView exitIcon;
    private TextInputLayout spinnerTitle;
    private AutoCompleteTextView autoCompleteTitle;
    private ArrayAdapter<String> arrayAdapterSpinnerTitle;
    private TextInputEditText textInputEditFeatures;
    private ScrollableNumberPicker scrollableNumberPicker;
    private TextInputLayout textInputLayoutDate;
    private TextInputEditText textInputEditDate;
    private TextInputLayout textInputLayoutStartTime;
    private TextInputEditText textInputEditStartTime;
    private TextInputLayout textInputLayoutEndTime;
    private TextInputEditText textInputEditEndTime;
    private TextInputLayout spinnerCity;
    private AutoCompleteTextView autoCompleteCity;
    private ArrayAdapter<String> arrayAdapterSpinnerCity;
    private TextInputEditText textInputEditAddress;
    private Slider slider;
    private TextInputEditText textInputEditDetails;
    private MaterialButton addTrainingButton;

    //input user
    private String title = "";
    private HashMap<String, String> featuresUser;
    private int participants;
    private String date = "";
    private String startTime = "";
    private String endTime = "";
    private String city = "";
    private String address;
    private int price;
    private String details;
    private boolean[] selectedFeatures;
    private ArrayList<Integer> features;

    //firebase
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final String userID = Objects.requireNonNull(auth.getCurrentUser()).getUid();
    private User user;
    private Bitmap userImage;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_calendar);
        userTrainings = new ArrayList<>();
        getIntents();
        initViews();
        setUpBottomNavigation();
        checkTrainerOrTrainee();

        calendarView.setOnDayClickListener(new OnDayClickListener() {
            @Override
            public void onDayClick(@NonNull EventDay eventDay) {
                if (userTrainings != null) {
                    calendar = eventDay.getCalendar();
                    String date = simpleDateFormat.format(calendar.getTime());
                    showTrainingsOnSelectedDate(date);
                }
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
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        calendarView = findViewById(R.id.calendarView);
        calendarView.setCalendarDayLayout(R.layout.layout_custom_calendar);
        addTrainingButtonDialog = findViewById(R.id.button_add_training);
        recyclerViewMyTrainings = findViewById(R.id.recycle_view_my_trainings);
    }

    @SuppressLint("SetTextI18n")
    private void getIntents() {
        byte[] byteArray = getIntent().getByteArrayExtra("userImage");
        userImage = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        userName = getIntent().getStringExtra("firstName");
    }

    private void sendToIntent(Intent intent) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        userImage.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        intent.putExtra("firstName", userName);
        intent.putExtra("userImage",byteArray);
    }

    private void setUpBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.menu_schedule);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent;
                switch (item.getItemId()) {
                    case R.id.menu_profile:
                        intent = new Intent(CalendarActivity.this, AccountActivity.class);
                        sendToIntent(intent);
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.menu_search:
                        intent = new Intent(CalendarActivity.this, SearchActivity.class);
                        sendToIntent(intent);
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.menu_schedule:
                        return true;
                    case R.id.menu_requests:
                        intent = new Intent(CalendarActivity.this, RequestsActivity.class);
                        sendToIntent(intent);
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.menu_home:
                        intent = new Intent(CalendarActivity.this, HomeActivity.class);
                        sendToIntent(intent);
                        startActivity(intent);
                        finish();
                        return true;
                }
                return false;
            }
        });
    }

    private void checkTrainerOrTrainee() {
        progressDialog = new ProgressDialog(CalendarActivity.this);
        progressDialog.setMessage("Loading");
        progressDialog.show();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        },4000);

        FirebaseDatabase.getInstance().getReference("Users").child(userID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);
                //if trainer show add training button
                if (Objects.requireNonNull(user).getUserType().equals("trainer")) {
                    addTrainingButtonDialog.setVisibility(View.VISIBLE);
                }
                //if have trainings to trainer or trainee
                if(snapshot.hasChild("trainings")) {
                    putTrainingsDayOnCalendar();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void putTrainingsDayOnCalendar() {
        events = new ArrayList<>();


        //get list uniqueID of user trainings
        FirebaseDatabase.getInstance().getReference("Users").child(userID).child("trainings")
                .addListenerForSingleValueEvent(new ValueEventListener() {
            int countTrainings = 0;

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshotMain) {
                for (DataSnapshot dataSnapshot : snapshotMain.getChildren()) {
                    //over the list and get the training from database
                    FirebaseDatabase.getInstance().getReference("Trainings").child(Objects.requireNonNull(dataSnapshot.getKey()))
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    countTrainings++;
                                    Training training = snapshot.getValue(Training.class);
                                    setTrainingModel(Objects.requireNonNull(training));

                                    calendar = Calendar.getInstance();
                                    try {
                                        calendar.setTime(Objects.requireNonNull(simpleDateFormat.parse(Objects.requireNonNull(training.getDate()))));
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                    calendar.add(Calendar.DAY_OF_MONTH, 0);
                                    events.add(new EventDay(calendar, R.drawable.ic_more_horizontal));

                                    if (countTrainings == snapshotMain.getChildrenCount()) {
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

    public void setTrainingModel(Training training) {
        FirebaseDatabase.getInstance().getReference().child("Users").child(training.getTrainerId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserTrainer userTrainer = snapshot.getValue(UserTrainer.class);
                        String trainerName = Objects.requireNonNull(userTrainer).getFirstName() + " " + userTrainer.getLastName();
                        getTrainerImage(trainerName,training);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void getTrainerImage(String trainerName, Training training) {
        final long ONE_MEGABYTE = 1024 * 1024;
        FirebaseStorage.getInstance().getReference().child(training.getTrainerId() + ".jpg").getBytes(ONE_MEGABYTE)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap trainerImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                TrainingModel trainingModel = new TrainingModel(training,training.getTrainingID(),trainerName,trainerImage,"");
                userTrainings.add(trainingModel);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void showTrainingsOnSelectedDate(String selectedDate) {
        //init recycleView
        recyclerViewMyTrainings.setHasFixedSize(true);
        recyclerViewMyTrainings.setLayoutManager(new LinearLayoutManager(this));

        ArrayList<TrainingModel> trainingsToShowOnThisDate = new ArrayList<>();
        MyTrainingsAdapter myTrainingsAdapter = new MyTrainingsAdapter(this, trainingsToShowOnThisDate, user.getUserType(), userID);
        recyclerViewMyTrainings.setAdapter(myTrainingsAdapter);

        for(int i = 0; i < userTrainings.size(); i++){
            if(selectedDate.equals(userTrainings.get(i).getTraining().getDate())){
                trainingsToShowOnThisDate.add(userTrainings.get(i));
            }
        }
        myTrainingsAdapter.notifyDataSetChanged();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void showAddTrainingDialog() {
        dialogAddTraining = new Dialog(CalendarActivity.this);
        dialogAddTraining.setContentView(R.layout.layout_add_training_dialog);
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
                title = "";
                featuresUser = null;
                participants = 1;
                date = "";
                startTime = "";
                endTime = "";
                city = "";
                address = "";
                price = 0;
                details = "";
                selectedFeatures = null;
                features = null;
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

        addTrainingButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                address = Objects.requireNonNull(textInputEditAddress.getText()).toString();
                details = Objects.requireNonNull(textInputEditDetails.getText()).toString();

                if (title.isEmpty()){
                    spinnerTitle.setError("This field is required");
                }else {
                    spinnerTitle.setError("");
                }
                if (date.isEmpty()){
                    textInputLayoutDate.setError("This field is required");
                }else {
                    textInputLayoutDate.setError("");
                }
                if (startTime.isEmpty()){
                    textInputLayoutStartTime.setError("This field is required");
                }else {
                    textInputLayoutStartTime.setError("");
                }
                if (endTime.isEmpty()){
                    textInputLayoutEndTime.setError("This field is required");
                }else {
                    textInputLayoutEndTime.setError("");
                }
                if (city.isEmpty()){
                    spinnerCity.setError("This field is required");
                }else {
                    spinnerCity.setError("");
                }

                if(title.isEmpty() || date.isEmpty() || startTime.isEmpty() || endTime.isEmpty() || city.isEmpty()){
                    Toast.makeText(CalendarActivity.this, "Not all fields are full", Toast.LENGTH_LONG).show();
                } else if (checkTrainingIsOverlapping()){
                    textInputLayoutDate.setError("You already have a training at this time");
                    textInputLayoutStartTime.setError(" ");
                    textInputLayoutEndTime.setError(" ");
                }else if (checkTimeBetweenTrainings(startTime,endTime)){
                    textInputLayoutStartTime.setError("error");
                    textInputLayoutEndTime.setError("error");
                }else {
                    addTraining(title, city, address, featuresUser, startTime, endTime, date, price, details, participants);
                    Toast.makeText(CalendarActivity.this, "The training was added", Toast.LENGTH_LONG).show();
                    dialogAddTraining.dismiss();
                }
            }
        });
    }

    private void showDateDialog() {
        MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select a date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setCalendarConstraints(new CalendarConstraints.Builder().setValidator(DateValidatorPointForward.now()).build())
                .build();

        materialDatePicker.show(getSupportFragmentManager(), "Date picker");

        materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener() {
            @Override
            public void onPositiveButtonClick(Object selection) {
                textInputEditDate.setText(simpleDateFormat.format(selection));
                date = simpleDateFormat.format(selection);
            }
        });
    }

    private void showStartTimeDialog() {
        calendar = Calendar.getInstance();
        TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                String startTimeTraining = fixMinuteDisplay(hourOfDay) + ":" + fixMinuteDisplay(minute);
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
                String endTimeTraining = fixMinuteDisplay(hourOfDay) + ":" + fixMinuteDisplay(minute);
                textInputEditEndTime.setText(endTimeTraining);
                endTime = endTimeTraining;
            }
        };
        new TimePickerDialog(CalendarActivity.this, timeSetListener,
                calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show();
    }

    private String fixMinuteDisplay(int minute) {
        String fixMinute;
        if (minute <= 9) {
            fixMinute = "0" + minute;
        } else {
            fixMinute = minute + "";
        }
        return fixMinute;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private boolean checkTrainingIsOverlapping() {
        for (int i = 0; i < userTrainings.size(); i++){
            if (date.equals(userTrainings.get(i).getTraining().getDate())){
                String startTrainingEqualDate = userTrainings.get(i).getTraining().getStartTraining();
                String endTrainingEqualDate = userTrainings.get(i).getTraining().getEndTraining();
                if (isOverlapping(startTrainingEqualDate,endTrainingEqualDate,startTime,endTime)){
                    return true;
                }
            }
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static boolean isOverlapping(String start1, String end1, String start2, String end2) {
        LocalTime startTraining1 = LocalTime.parse(start1);
        LocalTime endTraining1 = LocalTime.parse(end1);
        LocalTime startTraining2 = LocalTime.parse(start2);
        LocalTime endTraining2 = LocalTime.parse(end2);
        return (startTraining1.isBefore(endTraining2) && startTraining2.isBefore(endTraining1));

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private boolean checkTimeBetweenTrainings(String startTime,String endTime) {
        LocalTime start = LocalTime.parse(startTime);
        LocalTime end = LocalTime.parse(endTime);
        return !start.isBefore(end);
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

    private void addTraining(String title, String city, String address, HashMap<String, String> features, String startTraining, String endTraining, String date, int price, String details, int maxParticipants) {
        String getUniqueTrainingID = FirebaseDatabase.getInstance().getReference("Trainings").push().getKey();
        Training training = new Training(getUniqueTrainingID, title, city, address, userID, startTraining, endTraining, date, details, price, maxParticipants, features);
        FirebaseDatabase.getInstance().getReference("Users").child(userID).child("trainings").child(Objects.requireNonNull(getUniqueTrainingID)).setValue(true);
        FirebaseDatabase.getInstance().getReference("Trainings").child(Objects.requireNonNull(getUniqueTrainingID)).setValue(training);
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
        //number picker
        scrollableNumberPicker = dialogAddTraining.findViewById(R.id.scroll_number_picker);
        //date and time
        textInputLayoutDate = dialogAddTraining.findViewById(R.id.text_input_layout_date);
        textInputEditDate = dialogAddTraining.findViewById(R.id.text_input_edit_date);
        textInputLayoutStartTime = dialogAddTraining.findViewById(R.id.text_input_layout_start_time);
        textInputEditStartTime = dialogAddTraining.findViewById(R.id.text_input_edit_start_time);
        textInputLayoutEndTime = dialogAddTraining.findViewById(R.id.text_input_layout_end_time);
        textInputEditEndTime = dialogAddTraining.findViewById(R.id.text_input_edit_end_time);
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