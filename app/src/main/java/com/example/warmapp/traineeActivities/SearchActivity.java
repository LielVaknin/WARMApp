package com.example.warmapp.traineeActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.example.warmapp.R;
import com.example.warmapp.classes.MyAdapter;
import com.example.warmapp.classes.Training;
import com.example.warmapp.classes.TrainingModel;
import com.example.warmapp.classes.UserTrainer;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

public class SearchActivity extends AppCompatActivity implements Serializable {

    RecyclerView recyclerView;
    MyAdapter myAdapter;
    ArrayList<TrainingModel> trainings;

    Button mDatePickerBtn;
    //    TextView mSelectedDateText;
    RangeSlider rangeSlider;

    TextInputLayout textInputLayoutCity;
    AutoCompleteTextView autoCompleteTextCity;
    TextInputLayout textInputLayoutTitle;
    AutoCompleteTextView autoCompleteTextViewTitle;

    Chip chipGroupTraining;
    Chip chipOutdoorTraining;
    Chip chipForMenOnly;
    Chip chipForWomenOnly;
    Chip chipYouth;
    Chip chipChildren;
    Chip chipForPregnantWomen;
    Chip chipPostpartumWomen;
    Chip chipPreparationForCompetitions;
    Chip chipInjuryRehabilitation;
    ArrayList<String> selectedChips;

    DatabaseReference databaseReference;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        chipGroupTraining = findViewById(R.id.group_training);
        chipOutdoorTraining = findViewById(R.id.outdoor_training);
        chipForMenOnly = findViewById(R.id.for_men_only);
        chipForWomenOnly = findViewById(R.id.for_women_only);
        chipYouth = findViewById(R.id.youth);
        chipChildren = findViewById(R.id.children);
        chipForPregnantWomen = findViewById(R.id.for_pregnant_women);
        chipPostpartumWomen = findViewById(R.id.postpartum_women);
        chipPreparationForCompetitions = findViewById(R.id.preparation_for_competitions);
        chipInjuryRehabilitation = findViewById(R.id.injury_rehabilitation);

        selectedChips = new ArrayList<>();
        CompoundButton.OnCheckedChangeListener checkedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    selectedChips.add(buttonView.getText().toString());
                } else {
                    selectedChips.remove(buttonView.getText().toString());
                }
            }
        };

        chipGroupTraining.setOnCheckedChangeListener(checkedChangeListener);
        chipOutdoorTraining.setOnCheckedChangeListener(checkedChangeListener);
        chipForMenOnly.setOnCheckedChangeListener(checkedChangeListener);
        chipForWomenOnly.setOnCheckedChangeListener(checkedChangeListener);
        chipYouth.setOnCheckedChangeListener(checkedChangeListener);
        chipChildren.setOnCheckedChangeListener(checkedChangeListener);
        chipForPregnantWomen.setOnCheckedChangeListener(checkedChangeListener);
        chipPostpartumWomen.setOnCheckedChangeListener(checkedChangeListener);
        chipPreparationForCompetitions.setOnCheckedChangeListener(checkedChangeListener);
        chipInjuryRehabilitation.setOnCheckedChangeListener(checkedChangeListener);

        mDatePickerBtn = findViewById(R.id.date_picker_btn);
//        mSelectedDateText = findViewById(R.id.selected_date);

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.clear();

        long today = MaterialDatePicker.todayInUtcMilliseconds();

        calendar.setTimeInMillis(today);

        // CalendarConstraints
        CalendarConstraints.Builder constraintBuilder = new CalendarConstraints.Builder();
        constraintBuilder.setValidator(DateValidatorPointForward.now());

        // MaterialDatePicker
        MaterialDatePicker.Builder builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText("Select a date");
        builder.setSelection(today);
        builder.setCalendarConstraints(constraintBuilder.build());
        final MaterialDatePicker materialDatePicker = builder.build();

        mDatePickerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                materialDatePicker.show(getSupportFragmentManager(), "Date picker");
            }
        });
        materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener() {
            @Override
            public void onPositiveButtonClick(Object selection) {
                mDatePickerBtn.setText(materialDatePicker.getHeaderText());
//                mSelectedDateText.setText(materialDatePicker.getHeaderText());
            }
        });

        rangeSlider = findViewById(R.id.sliderRange);
        rangeSlider.setLabelFormatter(new LabelFormatter() {
            @NonNull
            @Override
            public String getFormattedValue(float value) {
                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
                currencyFormat.setCurrency(Currency.getInstance("ILS"));
                return currencyFormat.format(value);
            }
        });

        textInputLayoutTitle = findViewById(R.id.menu_drop2);
        autoCompleteTextViewTitle = findViewById(R.id.drop_titles);
        textInputLayoutCity = findViewById(R.id.menu_drop);
        autoCompleteTextCity = findViewById(R.id.drop_cities);
        recyclerView = findViewById(R.id.m_RecycleView);

        String[] titles = getResources().getStringArray(R.array.Titles);
        ArrayAdapter<String> titleAdapter = new ArrayAdapter<>(SearchActivity.this, R.layout.list, titles);
        autoCompleteTextViewTitle.setAdapter(titleAdapter);

        String[] cities = getResources().getStringArray(R.array.Cities);
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(SearchActivity.this, R.layout.list, cities);
        autoCompleteTextCity.setAdapter(cityAdapter);

//        databaseReference = FirebaseDatabase.getInstance().getReference("Trainings");
//        Training training = new Training("type.t" ,"Tel Aviv-Yafo","gg","3452432","43","43","localDate",534);
//        Training training1 = new Training("type.t" ,"Rehovot","gg","3452432","43","43","localDate",534);
//        Training training2 = new Training("type.t" ,"Jerusalem","gg","3452432","43","43","localDate",534);
//        databaseReference.child("train").setValue(training);
//        databaseReference.child("train2").setValue(training1);
//        databaseReference.child("train3").setValue(training2);
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Trainings");
        trainings = new ArrayList<>();
    }

    public void filterDataBase(String city, String title, String date, List<Float> values) {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                trainings.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Training training = dataSnapshot.getValue(Training.class);
                    TrainingModel trainingModel = new TrainingModel();
                    boolean flag = true;
                    for (int i = 0; i < selectedChips.size() && flag; i++){
                        HashMap<String, String> features = training.getFeatures();
                        if(features.get(selectedChips.get(i)) == null) {
                            flag = false;
                        }
                    }
                    if (city.equals(training.getCity()) && title.equals(training.getTitle()) && date.equals(training.getDate()) && training.getPrice() >= values.get(0) && training.getPrice() <= values.get(1) && flag) {
                        trainingModel.training = training;
                        String trainerId = training.getTrainerId();
                        FirebaseDatabase.getInstance().getReference().child("Users").child("Trainer").child(trainerId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                if (task.isSuccessful()) {
                                    UserTrainer userTrainer = task.getResult().getValue(UserTrainer.class);
                                    trainingModel.trainerName = userTrainer.getFirstName() + " " + userTrainer.getLastName();
                                    trainings.add(trainingModel);
                                }
                            }
                        });
                    }
                }
            }

            // && title.equals(training.getTitle()) && date.equals(training.getDate()) && training.getPrice() >= values.get(0) && training.getPrice() <= values.get(1)
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void onClickSearch(View view) throws InterruptedException {
        String city = autoCompleteTextCity.getText().toString();
        String title = autoCompleteTextViewTitle.getText().toString();
//        String date = mSelectedDateText.getText().toString();
        String date = mDatePickerBtn.getText().toString();
        List<Float> values = rangeSlider.getValues();
        filterDataBase(city, title, date, values);
//        Thread.sleep(5000);
        try {
            myAdapter = new MyAdapter(this, trainings);
            recyclerView.setAdapter(myAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
