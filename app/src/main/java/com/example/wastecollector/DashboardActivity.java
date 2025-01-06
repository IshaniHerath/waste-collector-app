package com.example.wastecollector;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;


public class DashboardActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Spinner locationDropDown;
    private TextView txv_fullbins;
    private TextView txv_readybins;
    private TextView txv_notreadybins;
    private TextView txv_unfilledbins;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.dashboard_page);

        // Initialize views
        locationDropDown = findViewById(R.id.spinner);
        txv_fullbins = (TextView) findViewById(R.id.tv_total_bins_value);
        txv_readybins = (TextView) findViewById(R.id.tv_ready_collect_value);
        txv_notreadybins = (TextView) findViewById(R.id.tv_ready_soon_value);
        txv_unfilledbins = (TextView) findViewById(R.id.tv_unfilled_value);

        // Set initial values to empty
        txv_fullbins.setText("_");
        txv_readybins.setText("_");
        txv_notreadybins.setText("_");
        txv_unfilledbins.setText("_");

        // Call Sheets API to read the Google Sheet
        SheetsHelper.readGoogleSheetAsync(this);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.locations,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationDropDown.setAdapter(adapter);
        locationDropDown.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        String choice = adapterView.getItemAtPosition(position).toString();
        Toast.makeText(getApplicationContext(), choice, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}