package com.example.wastecollector;

import android.content.Context;
import android.util.Log;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.InputStream;
import java.util.List;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SheetsHelper {

    private static final String TAG = "SheetsHelper";

    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public static void readGoogleSheetAsync(final Context context) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                // Network operation here (reading the Google Sheet)
                readGoogleSheet(context);
            }
        });
    }

    public static void readGoogleSheet(Context context) {
        try {
            // Load credentials from the JSON file in res/raw
            InputStream credentialsStream = context.getResources().openRawResource(R.raw.storage_credentials); // Replace 'credentials' with your actual filename
            GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream)
                    .createScoped(List.of("https://www.googleapis.com/auth/spreadsheets.readonly"));

            // Build Sheets API client
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            Sheets sheetsService = new Sheets.Builder(
                    new NetHttpTransport(),
                    jsonFactory,
                    new HttpCredentialsAdapter(credentials)
            ).setApplicationName("WasteCollector").build();

            // Google Sheet ID and range
            String spreadsheetId = "1pCTE0LoIWiNHdUKFZ59DbThcM_xU59h7xQBLw5JKv0Y"; // Sheet ID - from the share link
            String Sheet1 = "BinFillLevels!A1:D10"; // Range And Sheet name
            String Sheet2 = "BinDetails!A1:D10"; // Range And Sheet name

            // Fetch data from the Google Sheet
            ValueRange BinFillLevelsDetails = sheetsService.spreadsheets().values()
                    .get(spreadsheetId, Sheet1)
                    .execute();

            // Fetch data from the Google Sheet
            ValueRange BinDetails = sheetsService.spreadsheets().values()
                    .get(spreadsheetId, Sheet2)
                    .execute();

            List<List<Object>> FilledData = BinFillLevelsDetails.getValues();
            List<List<Object>> BinData = BinDetails.getValues();
            System.out.println("GGGGGGGGGGGGGG");
            getBinCountByArea(BinData);


            //TODO - take this out to a separate method
//            // Process the rows
//            if (FilledData == null || FilledData.isEmpty()) {
//                Log.d(TAG, "No data found in the sheet.");
//            } else {
//                for (List<Object> row : FilledData) {
//                    Log.d(TAG, "Row: " + row.toString());
//                }
//            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading Google Sheet", e);
        }
    }

    public static void getBinCountByArea(List<List<Object>> binData) {
        if (binData != null) {
            for (List<Object> bin : binData) {
                //TODO
                //if(bin.get(1) == '')
                Log.d(TAG, "Row: " + bin.get(1));
            }
        }
    }
}
