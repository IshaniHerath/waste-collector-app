package com.example.wastecollector;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.PopupMenu;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DashboardActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Spinner locationDropDown;
    private TextView txv_fullbins;
    private TextView txv_readybins;
    private TextView txv_notreadybins;
    private TextView txv_unfilledbins;
    private Button routeNavigatButton;
    private ImageButton btn_menu;
    List<List<Object>> filteredBins = new ArrayList<>(); //TODO: check if List<List<Object>> can convert to List<Object>
    List<Object>  binFilledValues = new ArrayList<>();
    List<Object> readyForCollectBins = new ArrayList<>();
    Map<String, Integer> binCountByLocation;
    List<List<Object>> binDetails;
    List<List<Object>> binFilledDetails;
    String selectedLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_page);

        // Initialize views
        locationDropDown = findViewById(R.id.spinner);
        txv_fullbins = (TextView) findViewById(R.id.tv_total_bins_value);
        txv_readybins = (TextView) findViewById(R.id.tv_ready_collect_value);
        txv_notreadybins = (TextView) findViewById(R.id.tv_ready_soon_value);
        txv_unfilledbins = (TextView) findViewById(R.id.tv_unfilled_value);
        routeNavigatButton = (Button) findViewById(R.id.btn_get_route);
        btn_menu = findViewById(R.id.btn_menu);

        // Set initial values to empty
        txv_fullbins.setText("0");
        txv_readybins.setText("0");
        txv_notreadybins.setText("0");
        txv_unfilledbins.setText("0");

        // Call Sheets API and handle callback
        SheetsHelper.readGoogleSheetAsync(this, new SheetsHelper.DataFetchListener() {
            @Override
            public void onDataFetched() {
                // Access binCountByLocation once data is ready
                binCountByLocation = SheetsHelper.getBinCountByLocation();
                binDetails = SheetsHelper.getBinDetails();
                binFilledDetails = SheetsHelper.getBinFilledDetails();
            }
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.locations,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationDropDown.setAdapter(adapter);
        locationDropDown.setOnItemSelectedListener(this);

        routeNavigatButton.setOnClickListener(v -> {

            ArrayList<ArrayList<Object>> serializableBinList = new ArrayList<>();
            for (List<Object> sublist : filteredBins) {
                serializableBinList.add(new ArrayList<>(sublist));
            }

            ArrayList<ArrayList<Object>> serializableFilledList = new ArrayList<>();
            for (Object obj : readyForCollectBins) {
                serializableFilledList.add(new ArrayList<>((List<Object>) obj));
            }

            // Navigate to RouteActivity
            Intent intent = new Intent(DashboardActivity.this, RouteActivity.class);
            intent.putExtra("binDetails", serializableBinList);
            intent.putExtra("binFilledDetails", serializableFilledList);

            startActivity(intent);
        });

        btn_menu.setOnClickListener(v -> showPopupMenu(v));
    }

    // New Method: Show Popup Menu
    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenu().add("Go Back");
        popupMenu.getMenu().add("Logout");

        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getTitle().toString()) {
                case "Go Back":
                    onBackPressed(); // Navigate back
                    return true;
                case "Logout":
                    logoutUser();
                    return true;
                default:
                    return false;
            }
        });

        popupMenu.show();
    }

    private void logoutUser() {
        Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        selectedLocation = adapterView.getItemAtPosition(position).toString();
        // Clear lists to avoid carrying over old data
        filteredBins.clear();
        binFilledValues.clear();
        readyForCollectBins.clear();

        updateTotalBins();
        updateFillDetails();
    }

    public void updateTotalBins() {
        if (binCountByLocation != null && binCountByLocation.containsKey(selectedLocation))
            txv_fullbins.setText(String.valueOf(binCountByLocation.get(selectedLocation)));
        else
           txv_fullbins.setText("0");
    }

    public void updateFillDetails() {
        if (binDetails == null || selectedLocation == null) {
            Log.d("", "Bin details or selected location is null");
            return;
        }

        Map<String, List<Object>> binIdToDetailsMap = new HashMap<>();

        // Filter binDetails by selectedLocation
        for (List<Object> bin : binDetails) {
            if (bin.size() >= 2 && selectedLocation.equals(bin.get(1).toString()))
                filteredBins.add(bin);
        }

        // Populate the map using binFilledDetails
        for (List<Object> filledBin : binFilledDetails) {
            if (filledBin.size() >= 1) {
                String binId = filledBin.get(0).toString();
                binIdToDetailsMap.put(binId, filledBin);
            }
        }

        // Match binIDs in filteredBins with the binFilledDetails
        for (List<Object> bin : filteredBins) {
            if (bin.size() >= 1) {
                String binId = bin.get(0).toString();

                if (binIdToDetailsMap.containsKey(binId)) {
                    binFilledValues.add(binIdToDetailsMap.get(binId));
                }
            }
        }
        getFillLevelBinCounts(binFilledValues);
    }

    public void getFillLevelBinCounts(List<Object> binFilledValues) {
        // Classify based on the fill percentage
        int readyForCollectCount = 0;
        int unfilledBinsCount = 0;
        int readySoonCount = 0;

        for (Object filledBin : binFilledValues) {
            if (filledBin instanceof List) {
                List<Object> binData = (List<Object>) filledBin;

                if (binData.size() > 1) {
                    try {
                        double fillPercentage = Double.parseDouble(binData.get(1).toString());

                        // Classify based on fill percentage
                        if (fillPercentage > 75) {
                            readyForCollectCount++;
                            readyForCollectBins.add(binData);

                        } else if (fillPercentage < 50) {
                            unfilledBinsCount++;
                        } else if (fillPercentage >= 50 && fillPercentage <= 75) {
                            readySoonCount++;
                        }
                    } catch (NumberFormatException e) {
                        Log.d("", "Error parsing fill percentage: " + e.getMessage());
                    }
                }
            }
        }

        txv_readybins.setText(String.valueOf(readyForCollectCount));
        txv_unfilledbins.setText(String.valueOf(unfilledBinsCount));
        txv_notreadybins.setText(String.valueOf(readySoonCount));
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}