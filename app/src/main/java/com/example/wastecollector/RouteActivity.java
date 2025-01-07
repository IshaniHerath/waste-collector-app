package com.example.wastecollector;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.api.IMapController;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;

import java.util.ArrayList;
import java.util.List;

public class RouteActivity extends AppCompatActivity {
    private ImageButton btn_menu;
    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.route_page);


        // Initialize OSMDroid configuration
        Configuration.getInstance().setUserAgentValue(getPackageName());
        mapView = findViewById(R.id.map);

        // Set up the map
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        IMapController controller = mapView.getController();
        controller.setZoom(12);
        controller.setCenter(new GeoPoint(59.3293, 18.0686)); // Center on Stockholm

        // Add markers for starting location and destinations
        addMarker(59.4014, 17.9397); // Kista (Starting point)
        addMarker(59.4018, 17.9307); // Spånga
        addMarker(59.4014, 17.9046); // Barkarby
        addMarker(59.3416, 18.0391); // Odenplan

        // Initialize Retrofit and the OSRM API Service
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://router.project-osrm.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        OSRMApiService osrmService = retrofit.create(OSRMApiService.class);

        // Call your API to get the optimized route
        getOptimizedRoute(osrmService);


        btn_menu = findViewById(R.id.btn_menu);
        btn_menu.setOnClickListener(v -> showPopupMenu(v));
    }

    // Popup Menu
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



    private void getOptimizedRoute(OSRMApiService osrmService) {
        // Coordinates for Kista, Spånga, Barkarby, Odenplan (in lon,lat format)
        String coordinates = "17.9397,59.4014;17.9307,59.4018;17.9046,59.4014;18.0391,59.3416";

        // Make the request to get the optimized route
        Call<RouteResponse> call = osrmService.getRoute(coordinates);
        call.enqueue(new Callback<RouteResponse>() {
            @Override
            public void onResponse(@NonNull Call<RouteResponse> call, @NonNull Response<RouteResponse> response) {
                if (response.isSuccessful()) {
                    RouteResponse route = response.body();
                    if (route != null && route.getRoutes() != null && !route.getRoutes().isEmpty()) {
                        String geometry = route.getRoutes().get(0).getGeometry();
                        Log.d("OSRM", "Geometry: " + geometry); // Log to verify geometry
                        drawRoute(geometry); // Decode and draw the route
                    }
                } else {
                    Log.e("OSRM", "API request failed: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<RouteResponse> call, Throwable t) {
                Log.e("OSRM", "Request failed", t);
            }
        });
    }

    private void addMarker(double lat, double lon) {
        // Create a marker and set its position
        Marker marker = new Marker(mapView);
        marker.setPosition(new GeoPoint(lat, lon));
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        // Add the marker to the map
        mapView.getOverlays().add(marker);
    }

    public interface OSRMApiService {
        @GET("route/v1/driving/{coordinates}?overview=full&alternatives=true")
        Call<RouteResponse> getRoute(
                @Path("coordinates") String coordinates
        );
    }

    public void drawRoute(String geometry) {
        // Decode the polyline geometry
        List<GeoPoint> points = decodePolyline(geometry);

        // Create and add the polyline to the map
        Polyline polyline = new Polyline();
        polyline.setPoints(points);
        polyline.setColor(Color.RED);  // Optional: Set polyline color
        polyline.setWidth(8f);  // Optional: Set polyline width

        // Add polyline to map overlays
        mapView.getOverlays().add(polyline);
        mapView.invalidate(); // Refresh the map
    }

    // Polyline decoding method (OSRM Polyline Encoding)
    public List<GeoPoint> decodePolyline(String encoded) {
        List<GeoPoint> decodedPoints = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lon = 0;

        while (index < len) {
            int shift = 0, result = 0;
            while (true) {
                int b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
                if (b < 0x20) break;
            }
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            while (true) {
                int b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
                if (b < 0x20) break;
            }
            int dlon = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lon += dlon;

            decodedPoints.add(new GeoPoint((lat / 1E5), (lon / 1E5)));
        }
        return decodedPoints;
    }

    // Define RouteResponse and Route classes for Retrofit response parsing
    public static class RouteResponse {
        private List<Route> routes;

        public List<Route> getRoutes() {
            return routes;
        }

        public static class Route {
            private String geometry;

            public String getGeometry() {
                return geometry;
            }
        }
    }

    private void logoutUser() {
        Intent intent = new Intent(RouteActivity.this, LoginActivity.class);
        startActivity(intent);
    }
}