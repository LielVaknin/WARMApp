package com.example.warmapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.example.warmapp.R;
import com.example.warmapp.adapters.SearchTrainingsAdapter;
import com.example.warmapp.adapters.SearchTrainerProfileAdapter;
import com.example.warmapp.classes.Request;
import com.example.warmapp.classes.TrainerModel;
import com.example.warmapp.classes.Training;
import com.example.warmapp.classes.TrainingModel;
import com.example.warmapp.classes.User;
import com.example.warmapp.classes.UserTrainer;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class SearchActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    private int radioId;
    private Button mDatePickerBtn;
    private TextInputEditText textInputEditSearchText;
    private FloatingActionButton searchButton;
    private ImageView imageViewFiltersSearch;
    private ChipGroup chipGroupSelectedFilters;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;

    //training adapter
    private SearchTrainingsAdapter searchTrainingsAdapter;
    private ArrayList<TrainingModel> trainings;
    private HashMap<String, HashMap<String, Training>> allTrainings;
    private ArrayList<String> userTrainingsID;
    private ArrayList<Request> userRequests;
    private boolean hasUserRequests;
    private boolean hasUserTrainings;

    //trainer adapter
    private SearchTrainerProfileAdapter searchTrainerProfileAdapter;
    private ArrayList<TrainerModel> trainers;

    //BottomSheetDialogFilters
    private BottomSheetDialog bottomSheetDialog;
    private ImageView imageViewCloseBottomSheetDialog;
    private AutoCompleteTextView autoCompleteTextViewCity;
    private ArrayAdapter<String> arrayAdapterSpinnerCity;
    private AutoCompleteTextView autoCompleteTextViewTitle;
    private ArrayAdapter<String> arrayAdapterSpinnerTitle;
    private Chip chipGroupTraining;
    private Chip chipOutdoorTraining;
    private Chip chipForMenOnly;
    private Chip chipForWomenOnly;
    private Chip chipYouth;
    private Chip chipChildren;
    private Chip chipForPregnantWomen;
    private Chip chipPostpartumWomen;
    private Chip chipPreparationForCompetitions;
    private Chip chipInjuryRehabilitation;
    private HashMap<String, ArrayList<String>> selectedFilters;
    private RangeSlider rangeSlider;
    private MaterialButton materialButtonSaveFilters;

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final String userID = Objects.requireNonNull(auth.getCurrentUser()).getUid();
    private String userType;
    private Bitmap userImage;
    private String userName;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_search);

        getIntents();
        initViews();
        setUpBottomNavigation();

        //if user want trainer search no need filters
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @SuppressLint({"NonConstantResourceId", "NotifyDataSetChanged"})
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.search_activity_radio_search_training:
                        imageViewFiltersSearch.setVisibility(View.VISIBLE);
                        clearRecyclerViewFromTrainers();
                        break;
                    case R.id.search_activity_radio_search_trainer:
                        imageViewFiltersSearch.setVisibility(View.GONE);
                        clearRecyclerViewFromTrainings();
                        break;
                }
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String searchText = Objects.requireNonNull(textInputEditSearchText.getText()).toString();
                radioId = radioGroup.getCheckedRadioButtonId();
                radioButton = findViewById(radioId);
                progressBar.setVisibility(View.VISIBLE);
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                if (radioButton.getText().toString().equals("Trainer Search")) {
                    fireBaseTrainerSearch(searchText);
                } else {
                    getUserDetails(searchText);
                }
            }
        });


        imageViewFiltersSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomSheetDialog();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void getIntents() {
        byte[] byteArray = getIntent().getByteArrayExtra("userImage");
        if (byteArray != null) {
            userImage = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        }
        userName = getIntent().getStringExtra("firstName");
    }

    private void initViews() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        radioGroup = findViewById(R.id.search_activity_radio_group);
        recyclerView = findViewById(R.id.search_activity_recycle_view);
        searchButton = findViewById(R.id.search_activity_search_button);
        textInputEditSearchText = findViewById(R.id.search_activity_edit_search);
        imageViewFiltersSearch = findViewById(R.id.search_activity_filters_button);
        progressBar = findViewById(R.id.search_activity_progress_bar);
        chipGroupSelectedFilters = findViewById(R.id.search_activity_chip_group_selected_filters);
    }

    private void sendToIntent(Intent intent) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        if (userImage != null) {
            userImage.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            intent.putExtra("userImage", byteArray);
        }

        intent.putExtra("firstName", userName);

    }

    private void setUpBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.menu_search);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent;
                switch (item.getItemId()) {
                    case R.id.menu_profile:
                        intent = new Intent(SearchActivity.this, AccountActivity.class);
                        sendToIntent(intent);
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.menu_search:
                        return true;
                    case R.id.menu_schedule:
                        intent = new Intent(SearchActivity.this, CalendarActivity.class);
                        sendToIntent(intent);
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.menu_requests:
                        intent = new Intent(SearchActivity.this, RequestsActivity.class);
                        sendToIntent(intent);
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.menu_home:
                        intent = new Intent(SearchActivity.this, HomeActivity.class);
                        sendToIntent(intent);
                        startActivity(intent);
                        finish();
                        return true;
                }
                return false;
            }
        });
    }

    private void getUserDetails(String searchText) {
        FirebaseDatabase.getInstance().getReference().child("Users").child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                userType = user.getUserType();
                userRequests = new ArrayList<>();
                userTrainingsID = new ArrayList<>();
                if (snapshot.hasChild("requests")) {
                    hasUserRequests = true;
                }
                if (snapshot.hasChild("trainings")) {
                    hasUserTrainings = true;
                }
                fireBaseTrainingSearch(searchText);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void fireBaseTrainingSearch(String searchText) {
        //init recycleView
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        trainings = new ArrayList<>();
        searchTrainingsAdapter = new SearchTrainingsAdapter(this, trainings, userType);
        recyclerView.setAdapter(searchTrainingsAdapter);

        allTrainings = new HashMap<>();

        FirebaseDatabase.getInstance().getReference().child("Trainings")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshotTrainings) {
                        int countTrainings = 0;
                        for (DataSnapshot dataSnapshot : snapshotTrainings.getChildren()) {
                            countTrainings++;
                            Training training = dataSnapshot.getValue(Training.class);
                            String trainingID = dataSnapshot.getKey();
                            int finalCountTrainings = countTrainings;
                            FirebaseDatabase.getInstance().getReference().child("Users").child(Objects.requireNonNull(training).getTrainerId())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            UserTrainer userTrainer = snapshot.getValue(UserTrainer.class);
                                            String trainerName = Objects.requireNonNull(userTrainer).getFirstName() + " " + userTrainer.getLastName();
                                            if (trainingsFilters(searchText, trainerName, training)) {
                                                HashMap<String, Training> trainerNameAndTraining = new HashMap<>();
                                                trainerNameAndTraining.put(trainerName, training);
                                                allTrainings.put(trainingID, trainerNameAndTraining);
                                            }
                                            if (finalCountTrainings == snapshotTrainings.getChildrenCount()) {
                                                if (hasUserRequests) {
                                                    getUserRequests();
                                                } else if (hasUserTrainings) {
                                                    getUserTrainings();
                                                } else {
                                                    setTrainingModel();
                                                }
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

    private boolean trainingsFilters(String searchText, String trainerName, Training training) {
        HashMap<String, ArrayList<Boolean>> checkSelectedFilters = new HashMap<>();
        boolean overCheckSelectedFilters = true;
        String searchTextLowerCase = searchText.toLowerCase();

        if (selectedFilters != null) {
            for (Map.Entry<String, ArrayList<String>> filterType : selectedFilters.entrySet()) {
                ArrayList<String> filters = filterType.getValue();
                switch (filterType.getKey()) {
                    case "date":
                        ArrayList<Boolean> dateList = new ArrayList<>();
                        for (String filter : filters) {
                            if (training.getDate().equals(filter)) {
                                dateList.add(true);
                            } else {
                                dateList.add(false);
                            }
                            checkSelectedFilters.put("date", dateList);
                        }
                        break;
                    case "titles":
                        ArrayList<Boolean> titlesList = new ArrayList<>();
                        for (String filter : filters) {
                            if (training.getTitle().equals(filter)) {
                                titlesList.add(true);
                            } else {
                                titlesList.add(false);
                            }
                            checkSelectedFilters.put("titles", titlesList);
                        }
                        break;
                    case "citys":
                        ArrayList<Boolean> citysList = new ArrayList<>();
                        for (String filter : filters) {
                            if (training.getCity().equals(filter)) {
                                citysList.add(true);
                            } else {
                                citysList.add(false);
                            }
                            checkSelectedFilters.put("citys", citysList);
                        }
                        break;
                    case "chips":
                        ArrayList<Boolean> chipsList = new ArrayList<>();
                        if (training.getFeatures() != null) {
                            for (String filter : filters) {
                                if (training.getFeatures().containsKey(filter)) {
                                    chipsList.add(true);
                                } else {
                                    chipsList.add(false);
                                }
                                checkSelectedFilters.put("chips", chipsList);
                            }
                        }
                        break;
                    case "price":
                        ArrayList<Boolean> priceList = new ArrayList<>();
                        for (String filter : filters) {
                            String[] arrOfStr = filter.split("[₪ -]+");
                            if (filter.contains("-")) {
                                if ((Integer.parseInt(arrOfStr[0]) <= training.getPrice()
                                        && Integer.parseInt(arrOfStr[1]) >= training.getPrice())) {
                                    priceList.add(true);
                                }
                            } else if ((training.getPrice() == Integer.parseInt(arrOfStr[0]))) {
                                priceList.add(true);
                            } else {
                                priceList.add(false);
                            }
                            checkSelectedFilters.put("price", priceList);
                        }
                        break;
                }
            }
        }

        if (!checkSelectedFilters.isEmpty()) {
            for (Map.Entry<String, ArrayList<Boolean>> featureCheck : checkSelectedFilters.entrySet()) {
                Log.d(featureCheck.getKey(), featureCheck.getValue().toString());
                if (featureCheck.getValue().contains(false)) {
                    overCheckSelectedFilters = false;
                    break;
                }
            }
        }

        return (training.getTitle().toLowerCase().contains(searchTextLowerCase)
                || training.getAddress().toLowerCase().contains(searchTextLowerCase)
                || training.getCity().toLowerCase().contains(searchTextLowerCase)
                || training.getDetails().toLowerCase().contains(searchTextLowerCase)
                || training.getDate().toLowerCase().contains(searchTextLowerCase)
                || training.getEndTraining().toLowerCase().contains(searchTextLowerCase)
                || training.getStartTraining().toLowerCase().contains(searchTextLowerCase)
                || String.valueOf(training.getPrice()).contains(searchTextLowerCase)
                || training.getFeatures().containsKey(searchTextLowerCase)
                || trainerName.contains(searchTextLowerCase))
                && overCheckSelectedFilters;
    }

    public void getUserRequests() {


        FirebaseDatabase.getInstance().getReference().child("Users").child(userID).child("requests")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    int countRequests = 0;

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshotMain) {
                        for (DataSnapshot dataSnapshot : snapshotMain.getChildren()) {
                            countRequests++;
                            FirebaseDatabase.getInstance().getReference().child("Requests").child(Objects.requireNonNull(dataSnapshot.getKey()))
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            Request request = snapshot.getValue(Request.class);
                                            userRequests.add(request);
                                            if (countRequests == snapshotMain.getChildrenCount()) {
                                                if (hasUserTrainings) {
                                                    getUserTrainings();
                                                } else {
                                                    setTrainingModel();
                                                }

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

    private void getUserTrainings() {

        FirebaseDatabase.getInstance().getReference("Users").child(userID).child("trainings")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    int countTrainings = 0;

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            countTrainings++;
                            userTrainingsID.add(dataSnapshot.getKey());
                            if (countTrainings == snapshot.getChildrenCount()) {
                                setTrainingModel();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public void setTrainingModel() {
        int countTrainings = 0;
        for (HashMap.Entry<String, HashMap<String, Training>> trainingID : allTrainings.entrySet()) {
            countTrainings++;
            for (HashMap.Entry<String, Training> trainerNameAndTraining : trainingID.getValue().entrySet()) {
                Training training = trainerNameAndTraining.getValue();
                String trainingIDKey = trainingID.getKey();
                String trainerName = trainerNameAndTraining.getKey();
                final long ONE_MEGABYTE = 1024 * 1024;
                int finalCountTrainings = countTrainings;
                FirebaseStorage.getInstance().getReference().child(trainerNameAndTraining.getValue().getTrainerId() + ".jpg").getBytes(ONE_MEGABYTE)
                        .addOnSuccessListener(new OnSuccessListener<byte[]>() {

                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @SuppressLint("NotifyDataSetChanged")
                            @Override
                            public void onSuccess(byte[] bytes) {
                                Bitmap trainerImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                TrainingModel trainingModel;
                                if (userTrainingsID.contains(trainingID.getKey())) {
                                    trainingModel = new TrainingModel(training, trainingIDKey, trainerName, trainerImage, "registered");
                                    trainings.add(trainingModel);
                                } else if (checkUserRequests(trainingID.getKey())) {
                                    trainingModel = new TrainingModel(training, trainingIDKey, trainerName, trainerImage, "request");
                                    trainings.add(trainingModel);
                                } else if (checkTrainingIsOverlapping(training)) {
                                    trainingModel = new TrainingModel(training, trainingIDKey, trainerName, trainerImage, "overlapping");
                                    trainings.add(trainingModel);
                                } else {
                                    trainingModel = new TrainingModel(training, trainingIDKey, trainerName, trainerImage, "");
                                    trainings.add(trainingModel);
                                }
                                if (finalCountTrainings == allTrainings.size()) {
                                    progressBar.setVisibility(View.GONE);
                                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                    searchTrainingsAdapter.notifyDataSetChanged();
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                    }
                });
            }
        }
    }

    private boolean checkUserRequests(String trainingID) {
        for (Request request : userRequests) {
            if (request.getTrainingID().equals(trainingID)) {
                return true;
            }
        }
        return false;
    }

    private void fireBaseTrainerSearch(String searchText) {
        //init recycleView
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        trainers = new ArrayList<>();
        searchTrainerProfileAdapter = new SearchTrainerProfileAdapter(this, trainers);
        recyclerView.setAdapter(searchTrainerProfileAdapter);


        FirebaseDatabase.getInstance().getReference().child("Users").orderByChild("userType").equalTo("trainer")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    int countTrainers;

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            countTrainers++;
                            String trainerID = dataSnapshot.getKey();
                            UserTrainer userTrainer = dataSnapshot.getValue(UserTrainer.class);
                            if (Objects.requireNonNull(userTrainer).getLastName().toLowerCase().contains(searchText.toLowerCase())
                                    || userTrainer.getFirstName().toLowerCase().contains(searchText.toLowerCase())) {
                                setTrainerModel(userTrainer, trainerID);
                            }
                            if (countTrainers == snapshot.getChildrenCount()) {
                                progressBar.setVisibility(View.GONE);
                                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void setTrainerModel(UserTrainer userTrainer, String trainerID) {
        TrainerModel trainerModel = new TrainerModel(userTrainer, trainerID);

        final long ONE_MEGABYTE = 1024 * 1024;
        FirebaseStorage.getInstance().getReference().child(trainerID + ".jpg").getBytes(ONE_MEGABYTE)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onSuccess(byte[] bytes) {
                        trainerModel.setTrainerImage(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                        trainers.add(trainerModel);
                        searchTrainerProfileAdapter.notifyDataSetChanged();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
            }
        });
    }

    private void showBottomSheetDialog() {
        selectedFilters = new HashMap<>();

        bottomSheetDialog = new BottomSheetDialog(SearchActivity.this, R.style.BottomSheetStyle);
        @SuppressLint("InflateParams") View bottomSheetView = getLayoutInflater().inflate(R.layout.layout_search_filters, null, false);

        initViewsBottomSheetDialog(bottomSheetView);
        chipGroupSelectedFilters.removeAllViews();

        //close bottom sheet dialog
        imageViewCloseBottomSheetDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
            }
        });

        //title and city
        autoCompleteTextViewTitle.setAdapter(arrayAdapterSpinnerTitle);
        autoCompleteTextViewTitle.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ArrayList<String> titles = new ArrayList<>();
                titles.add(parent.getItemAtPosition(position).toString());
                selectedFilters.put("titles", titles);
            }
        });
        autoCompleteTextViewCity.setAdapter(arrayAdapterSpinnerCity);
        autoCompleteTextViewCity.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ArrayList<String> cities = new ArrayList<>();
                cities.add(parent.getItemAtPosition(position).toString());
                selectedFilters.put("cities", cities);
            }
        });

        //Chips listener
        ArrayList<String> selectedChips = new ArrayList<>();
        CompoundButton.OnCheckedChangeListener checkedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    selectedChips.add(buttonView.getText().toString());
                } else {
                    selectedChips.remove(buttonView.getText().toString());
                }
                selectedFilters.put("chips", selectedChips);
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

        //Calender date picker
        mDatePickerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateDialog();
            }
        });

        // Range slider
        rangeSlider.setLabelFormatter(new LabelFormatter() {
            @NonNull
            @Override
            public String getFormattedValue(float value) {
                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
                currencyFormat.setCurrency(Currency.getInstance("ILS"));
                return currencyFormat.format((int) value);
            }
        });

        materialButtonSaveFilters.setOnClickListener(new View.OnClickListener() {
            @SuppressLint({"UseCompatLoadingForDrawables", "ResourceAsColor"})
            @Override
            public void onClick(View v) {
                getRangeSliderPrice(rangeSlider);

                LayoutInflater layoutInflater = LayoutInflater.from(SearchActivity.this);
                for (HashMap.Entry<String, ArrayList<String>> filterMap : selectedFilters.entrySet()) {
                    ArrayList<String> filterList = filterMap.getValue();
                    for (String filter : filterList) {
                        @SuppressLint({"ResourceType", "InflateParams"}) Chip chip = (Chip) layoutInflater.inflate(R.layout.layout_filter_chip, null, false);
                        chip.setText(filter);

                        chip.setOnCloseIconClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                chipGroupSelectedFilters.removeView(v);
                                for (HashMap.Entry<String, ArrayList<String>> filterMap : selectedFilters.entrySet()) {
                                    ArrayList<String> filterList = filterMap.getValue();
                                    if (filterList.remove(chip.getText().toString())) {
                                        if (filterList.isEmpty()) {
                                            selectedFilters.remove(filterMap.getKey());
                                        }
                                        break;
                                    }
                                }
                            }
                        });
                        chipGroupSelectedFilters.addView(chip);
                    }
                }
                bottomSheetDialog.dismiss();
            }
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }

    private void initViewsBottomSheetDialog(View bottomSheetView) {
        imageViewCloseBottomSheetDialog = bottomSheetView.findViewById(R.id.bottom_sheet_search_filters_image_view_close);
        //chips
        chipGroupTraining = bottomSheetView.findViewById(R.id.bottom_sheet_search_filters_chip_group_training);
        chipOutdoorTraining = bottomSheetView.findViewById(R.id.bottom_sheet_search_filters_chip_outdoor_training);
        chipForMenOnly = bottomSheetView.findViewById(R.id.bottom_sheet_search_filters_chip_for_men_only);
        chipForWomenOnly = bottomSheetView.findViewById(R.id.bottom_sheet_search_filters_chip_for_women_only);
        chipYouth = bottomSheetView.findViewById(R.id.bottom_sheet_search_filters_chip_youth);
        chipChildren = bottomSheetView.findViewById(R.id.bottom_sheet_search_filters_chip_children);
        chipForPregnantWomen = bottomSheetView.findViewById(R.id.bottom_sheet_search_filters_chip_for_pregnant_women);
        chipPostpartumWomen = bottomSheetView.findViewById(R.id.bottom_sheet_search_filters_chip_postpartum_women);
        chipPreparationForCompetitions = bottomSheetView.findViewById(R.id.bottom_sheet_search_filters_chip_preparation_for_competitions);
        chipInjuryRehabilitation = bottomSheetView.findViewById(R.id.bottom_sheet_search_filters_chip_injury_rehabilitation);
        //title and city
        autoCompleteTextViewTitle = bottomSheetView.findViewById(R.id.bottom_sheet_search_filters_auto_complete_title);
        arrayAdapterSpinnerTitle = new ArrayAdapter<>(getApplicationContext(),
                R.layout.spinner_dropdown_item, getResources().getStringArray(R.array.Titles));
        autoCompleteTextViewCity = bottomSheetView.findViewById(R.id.bottom_sheet_search_filters_auto_complete_city);
        arrayAdapterSpinnerCity = new ArrayAdapter<>(getApplicationContext(),
                R.layout.spinner_dropdown_item, getResources().getStringArray(R.array.Cities));
        //date picker
        mDatePickerBtn = bottomSheetView.findViewById(R.id.bottom_sheet_search_filters_date_picker_button);
        //range slider
        rangeSlider = bottomSheetView.findViewById(R.id.bottom_sheet_search_filters_slider_range);
        //save filters
        materialButtonSaveFilters = bottomSheetView.findViewById(R.id.bottom_sheet_search_filters_save_button);
    }

    private void getRangeSliderPrice(RangeSlider rangeSlider) {
        List<Float> getRangePrice = rangeSlider.getValues();
        int priceFrom = getRangePrice.get(0).intValue();
        int priceTo = getRangePrice.get(1).intValue();

        ArrayList<String> rangePrice = new ArrayList<>();
        if (priceFrom == priceTo) {
            rangePrice.add(priceFrom + "₪");
        } else {
            rangePrice.add(priceFrom + "₪" + " - " + priceTo + "₪");
        }
        selectedFilters.put("price", rangePrice);
    }

    private void showDateDialog() {
        ArrayList<String> date = new ArrayList<>();
        MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select a date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setCalendarConstraints(new CalendarConstraints.Builder().setValidator(DateValidatorPointForward.now()).build())
                .build();

        materialDatePicker.show(getSupportFragmentManager(), "Date picker");

        materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener() {
            @Override
            public void onPositiveButtonClick(Object selection) {
                mDatePickerBtn.setText(simpleDateFormat.format(selection));
                date.add(simpleDateFormat.format(selection));
                selectedFilters.put("date", date);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private boolean checkTrainingIsOverlapping(Training training) {
        for (int i = 0; i < userTrainingsID.size(); i++) {
            if (allTrainings.get(userTrainingsID.get(i)) != null) {
                for (HashMap.Entry<String, Training> trainingUser : Objects.requireNonNull(allTrainings.get(userTrainingsID.get(i))).entrySet()) {
                    if (training.getDate().equals(trainingUser.getValue().getDate())) {
                        String startTrainingEqualDate = trainingUser.getValue().getStartTraining();
                        String endTrainingEqualDate = trainingUser.getValue().getEndTraining();
                        if (isOverlapping(startTrainingEqualDate, endTrainingEqualDate, training.getStartTraining(), training.getEndTraining())) {
                            return true;
                        }
                    }
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

    private void clearRecyclerViewFromTrainers() {
        if (trainers != null) {
            int size = trainers.size();
            trainers.clear();
            searchTrainerProfileAdapter.notifyItemRangeRemoved(0, size);
        }
    }

    private void clearRecyclerViewFromTrainings() {
        if (trainings != null) {
            int size = trainings.size();
            trainings.clear();
            searchTrainingsAdapter.notifyItemRangeRemoved(0, size);
        }
    }
}