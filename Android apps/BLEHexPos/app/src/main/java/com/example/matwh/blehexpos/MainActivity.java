package com.example.matwh.blehexpos;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.RelativeLayout;

import com.example.matwh.blehexpos.views.Triangle;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 1 = Center
 * 2 = Top center
 * 3 = Top right
 * 4 = Bottom right
 * 5 = Bottom center
 * 6 = Bottom left
 * 7 = Top left
 */

public class MainActivity extends AppCompatActivity implements BeaconConsumer {

    // Define the UUIDs for all the ESP32s used in the system
    private static final String ESP1_UUID = "36d9294e-c30d-b2f9-4e80-22b6fdb9effc";
    private static final String ESP2_UUID = "ccb28163-7774-9cb3-a61f-61e59e28a144";
    private static final String ESP3_UUID = "22f6888b-3bc4-c098-4b89-0aa86c0e7f35";
    private static final String ESP4_UUID = "820f3b20-be54-9b69-392c-474cf5ce656f";
    private static final String ESP5_UUID = "2b223954-2c93-f6bf-8959-fb1b690d1d2b";
    private static final String ESP6_UUID = "326864de-d169-3d6f-105b-587000f3bf2a";
    private static final String ESP7_UUID = "cbf27fb2-e12c-0962-7cf2-d9cd1e9ba78e";

    // Hashmap to store the current RSSI value of each ESP32
    private Map<String, Double> currentValues = new HashMap<String, Double>() {{
        put(ESP1_UUID, Double.MAX_VALUE);
        put(ESP2_UUID, Double.MAX_VALUE);
        put(ESP3_UUID, Double.MAX_VALUE);
        put(ESP4_UUID, Double.MAX_VALUE);
        put(ESP5_UUID, Double.MAX_VALUE);
        put(ESP6_UUID, Double.MAX_VALUE);
        put(ESP7_UUID, Double.MAX_VALUE);
    }};

    // Padding for UI
    final static int PADDING = 30;

    // Variables that will hold the triangles that make up the hexagon
    private Triangle topLeftTriangle, topRightTriangle, middleLeftTriangle, middleRightTriangle,
        bottomLeftTriangle, bottomRightTriangle;

    // Beacon manager for scanning ESP32s
    private BeaconManager beaconManager;

    // Class tag
    private static final String TAG = MainActivity.class.getName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hex_layout);

        // Register beacon scanner
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(this);

        // Create hexagon by making 6 triangles using the Triangle class
        createHex();

        // Update frontend and clear UI on a timer
        update();
    }

    /**
     * Create a hexagon in the center of the screen. Hexagon is made up of 6 {@link Triangle}s.
     * The triangles have a black outline and are filled with red by default.
     */
    private void createHex() {
        // Fetch the layout the put the hexagon in
        RelativeLayout l = findViewById(R.id.rl_hex);

        // Fetch screen width and height
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int deviceHeight = displayMetrics.heightPixels;
        int deviceWidth = displayMetrics.widthPixels;
        deviceHeight -= getStatusBarHeight(this);
        deviceHeight -= getNavigationBarHeight(this);

        // Define the boundaries of our hexagon
        final int MIN_HORIZ = PADDING;
        final int MAX_HORIZ = deviceWidth - PADDING;
        final int MIN_VERTICAL = PADDING;
        final int MAX_VERTICAL = deviceHeight - (PADDING * 2);

        // Set the points for our triangles and place these into an array
        Point centerPoint = new Point((MAX_HORIZ / 2), (MAX_VERTICAL / 2));
        Point topCenterPoint = new Point((MAX_HORIZ / 2), MIN_VERTICAL);
        Point topLeftPoint = new Point(MIN_HORIZ, (MAX_VERTICAL / 4));
        Point topRightPoint = new Point(MAX_HORIZ, (MAX_VERTICAL / 4));
        Point bottomLeftPoint = new Point(MIN_HORIZ, (MAX_VERTICAL / 4) * 3);
        Point bottomRightPoint = new Point(MAX_HORIZ, (MAX_VERTICAL / 4) * 3);
        Point bottomCenterPoint = new Point((MAX_HORIZ / 2), MAX_VERTICAL);

        Point[][] trianglePoints = new Point[][]{
                new Point[]{centerPoint, topCenterPoint, topLeftPoint},
                new Point[]{centerPoint, bottomLeftPoint, topLeftPoint},
                new Point[]{centerPoint, bottomLeftPoint, bottomCenterPoint},
                new Point[]{centerPoint, bottomRightPoint, bottomCenterPoint},
                new Point[]{centerPoint, bottomRightPoint, topRightPoint},
                new Point[]{centerPoint, topCenterPoint, topRightPoint}
        };

        // Create the six triangles from our calculated points
        topLeftTriangle = new Triangle(this, trianglePoints[0], Color.RED);
        middleLeftTriangle = new Triangle(this, trianglePoints[1], Color.RED);
        bottomLeftTriangle = new Triangle(this, trianglePoints[2], Color.RED);
        bottomRightTriangle = new Triangle(this, trianglePoints[3], Color.RED);
        middleRightTriangle = new Triangle(this, trianglePoints[4], Color.RED);
        topRightTriangle = new Triangle(this, trianglePoints[5], Color.RED);

        // Add the triangles to the relative layout
        l.addView(topLeftTriangle);
        l.addView(middleLeftTriangle);
        l.addView(bottomLeftTriangle);
        l.addView(topRightTriangle);
        l.addView(middleRightTriangle);
        l.addView(bottomRightTriangle);
    }

    /**
     * Set the whole hexagon to red. Set the triangle that contains the phone to green.
     */
    public void updateFrontend(ArrayList<String> esps) {
        // Set the whole hexagon to red
        topRightTriangle.setColor(Color.RED);
        middleRightTriangle.setColor(Color.RED);
        bottomLeftTriangle.setColor(Color.RED);
        bottomRightTriangle.setColor(Color.RED);
        middleLeftTriangle.setColor(Color.RED);
        topLeftTriangle.setColor(Color.RED);

        // Don't update anything if there weren't enough readings
        if(esps == null) return;

        // Depending on which ESPS are return, change the relevant triangle to green
        if(esps.contains(ESP2_UUID) && esps.contains(ESP3_UUID)) {
            topRightTriangle.setColor(Color.GREEN);
        } else if(esps.contains(ESP3_UUID) && esps.contains(ESP4_UUID)) {
            middleRightTriangle.setColor(Color.GREEN);
        } else if(esps.contains(ESP4_UUID) && esps.contains(ESP5_UUID)) {
            bottomRightTriangle.setColor(Color.GREEN);
        } else if(esps.contains(ESP5_UUID) && esps.contains(ESP6_UUID)) {
            bottomLeftTriangle.setColor(Color.GREEN);
        } else if(esps.contains(ESP6_UUID) && esps.contains(ESP7_UUID)) {
            middleLeftTriangle.setColor(Color.GREEN);
        } else if(esps.contains(ESP7_UUID) && esps.contains(ESP2_UUID)) {
            topLeftTriangle.setColor(Color.GREEN);
        }
    }

    /**
     * Return the UUIDs of the three ESPs with the strongest readings. Method will return
     * null if there aren't enough readings.
     * @return UUIDs of the three ESPs with the strongest readings.
     */
    @Nullable
    private ArrayList<String> getStrongestReadings() {
        // Fetch the lowest value
        Map.Entry<String, Double> min1 = null;
        for(Map.Entry<String, Double> entry : currentValues.entrySet()) {
            if(min1 == null || entry.getValue() < min1.getValue()) min1 = entry;
        }
        // If the lowest value was found, find the second lowest
        if(min1 != null) {
            Map.Entry<String, Double> min2 = null;
            for(Map.Entry<String, Double> entry : currentValues.entrySet()) {
                if((min2 == null || entry.getValue() < min2.getValue()) && !entry.getKey().equals(min1.getKey())) min2 = entry;
            }
            // If the second lowest was also found, find the third lowest
            if(min2 != null) {
                Map.Entry<String, Double> min3 = null;
                for(Map.Entry<String, Double> entry : currentValues.entrySet()) {
                    if((min3 == null || entry.getValue() < min3.getValue()) &&
                            !entry.getKey().equals(min1.getKey()) && !entry.getKey().equals(min2.getKey())) min3 = entry;
                }
                // If all three values were found, return the UUIDs of the three ESPs
                if(min3 != null && min1.getValue() != Double.MAX_VALUE && min2.getValue() != Double.MAX_VALUE && min3.getValue() != Double.MAX_VALUE) {
                    String[] minArray = new String[]{min1.getKey(), min2.getKey(), min3.getKey()};
                    return new ArrayList<>(Arrays.asList(minArray));
                }
            }
        }
        // If three values couldn't be found return null
        return null;
    }

    /**
     * Return the height of the status bar.
     * @param context Application context.
     * @return Height of the status bar.
     */
    public static int getStatusBarHeight( Context context ) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier( "status_bar_height", "dimen", "android" );
        return (resourceId > 0) ? resources.getDimensionPixelSize(resourceId) : 0;
    }

    /**
     * Return the height of the navigation bar.
     * @param context Application context.
     * @return Height of the navigation bar.
     */
    public static int getNavigationBarHeight( Context context ) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier( "navigation_bar_height", "dimen", "android" );
        return (resourceId > 0) ? resources.getDimensionPixelSize(resourceId) : 0;
    }

    /**
     * Unbind the beacon manager when the activity is destroyed.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }

    /**
     * Ran when beacons are found, updates distance values for all system beacons that are detected
     * and then start a new scan when that's done. Also runs the method to update the frontend.
     */
    @Override
    public void onBeaconServiceConnect() {
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                // For each detected beacon, check if it's part of our system and if it is
                // then update the distance value for it
                for (Beacon b : beacons) {
                    if(b.getId1() != null && currentValues.keySet().contains(b.getId1().toString())) {
                        currentValues.put(b.getId1().toString(), b.getDistance());
                    }
                }
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("matwh.rangingID", null, null, null));
        } catch (RemoteException e) {
            Log.e(TAG, "Beacon scan failed to start.");
        }
    }

    /**
     * Update the UI and reset distance values after doing so
     */
    private void update() {
        // Repeat the task every 3 seconds
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateFrontend(getStrongestReadings());
                clearReadings();

            }
        },0,5000);
    }

    /**
     * Reset all distance readings to Double.MAX_VALUE
     */
    private void clearReadings() {
        for(Map.Entry<String, Double> entry : currentValues.entrySet()) {
            entry.setValue(Double.MAX_VALUE);
        }
    }
}
