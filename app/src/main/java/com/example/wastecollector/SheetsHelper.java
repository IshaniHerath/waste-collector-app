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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SheetsHelper {

    private static final String TAG = "SheetsHelper";
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private static List<List<Object>> FilledData;
    private static List<List<Object>> BinData;

    public interface DataFetchListener {
        void onDataFetched();
    }

    public static void readGoogleSheetAsync(final Context context, final DataFetchListener listener) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                readGoogleSheet(context);
                if (listener != null)
                    listener.onDataFetched();
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
            String Sheet1 = "BinFillLevels!B2:D30"; // Range And Sheet name
            String Sheet2 = "BinDetails!A2:D20"; // Range And Sheet name

            // Fetch data from the Google Sheet
            ValueRange BinFillLevelsDetails = sheetsService.spreadsheets().values()
                    .get(spreadsheetId, Sheet1)
                    .execute();

            // Fetch data from the Google Sheet
            ValueRange BinDetails = sheetsService.spreadsheets().values()
                    .get(spreadsheetId, Sheet2)
                    .execute();

            FilledData = BinFillLevelsDetails.getValues();
            BinData = BinDetails.getValues();

        } catch (Exception e) {
            Log.e(TAG, "Error reading Google Sheet", e);
        }
    }

    public static Map<String, Integer> getBinCountByArea(List<List<Object>> binData) {
        Map<String, Integer> binCountByLocation = new HashMap<>();

        if (binData != null) {
            for (List<Object> bin : binData) {
                String location = (String) bin.get(1);  //(0 = Bin_ID, 1 = Bin_Location)

                // If the location already exists in the map, increment the count
                if (binCountByLocation.containsKey(location)) {
                    binCountByLocation.put(location, binCountByLocation.get(location) + 1);
                } else {
                    // If the location is not in the map, add it with an initial count of 1
                    binCountByLocation.put(location, 1);
                }
            }
        }
        return binCountByLocation;
    }

    // Method to retrieve binCountByLocation
    public static Map<String, Integer> getBinCountByLocation() {
        return getBinCountByArea(BinData);
    }

    public static List<List<Object>> getBinDetails() {
        return BinData;
    }

    public static List<List<Object>> getBinFilledDetails() {
        // Result list to store the latest filled details
        List<List<Object>> latestFilledDetails = new ArrayList<>();

        if (FilledData != null && !FilledData.isEmpty()) {
            // Map to store the latest entry for each binId
            Map<String, List<Object>> latestEntries = new HashMap<>();

            for (List<Object> entry : FilledData) {
                if (entry.size() >= 3) {
                    String binId = entry.get(0).toString(); // Bin ID
                    String timestamp = entry.get(2).toString(); // Timestamp

                    // Check if this binId already exists in the map
                    if (latestEntries.containsKey(binId)) {
                        // Compare timestamps to determine the latest entry
                        String existingTimestamp = latestEntries.get(binId).get(2).toString();
                        if (timestamp.compareTo(existingTimestamp) > 0) { // Newer timestamp
                            latestEntries.put(binId, entry);
                        }
                    } else {
                        // Add new binId to the map
                        latestEntries.put(binId, entry);
                    }
                }
            }

            // Convert the map values to a list, excluding the timestamp
            for (List<Object> entry : latestEntries.values()) {
                List<Object> simplifiedEntry = List.of(entry.get(0), entry.get(1)); // Include only binId and filledLevel
                latestFilledDetails.add(simplifiedEntry);
            }
        }
        return latestFilledDetails;
    }
}
