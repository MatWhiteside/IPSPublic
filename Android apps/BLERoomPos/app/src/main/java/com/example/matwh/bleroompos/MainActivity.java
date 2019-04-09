package com.example.matwh.bleroompos;

import android.os.Handler;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Entry activity for the application.
 */
public class MainActivity extends AppCompatActivity implements BeaconConsumer {

    // Android components
    private TextView roomText;
    private Handler uiHandler = new Handler();

    // Beacon components
    private BeaconManager beaconManager;

    // Define beacons that are placed in each room and create a HashMap of these
    final SystemBeacon ESP1 = new SystemBeacon("36d9294e-c30d-b2f9-4e80-22b6fdb9effc", "Bedroom");
    final SystemBeacon ESP3 = new SystemBeacon("22f6888b-3bc4-c098-4b89-0aa86c0e7f35", "Hallway");
    final SystemBeacon ESP4 = new SystemBeacon("820f3b20-be54-9b69-392c-474cf5ce656f", "Bathroom");
    private final Map<String, SystemBeacon> systemBeacons = new HashMap<String, SystemBeacon>() {{
        put(ESP1.getUuid(), ESP1);
        put(ESP3.getUuid(), ESP3);
        put(ESP4.getUuid(), ESP4);
    }};

    // Class tag
    private static final String TAG = MainActivity.class.getName();

    /**
     * Setup UI and start the update() method that will run while the app is alive.
     * @param savedInstanceState {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find the text that is displayed on the screen so we can set it later
        roomText = findViewById(R.id.tv_room);

        // Setup beacon manager with the beacon layout for iBeacon
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(this);

        // Start out update method that will update the screen and clear values
        // every 3 seconds
        update();
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
     * Ran when beacons are found, updates RSSI values for all system beacons that are detected
     * and then start a new scan when that's done.
     */
    @Override
    public void onBeaconServiceConnect() {
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                // For each detected beacon, check if it's part of our system and if it is
                // then update the RSSI value for it
                for (Beacon b : beacons) {
                    if(systemBeacons.get(b.getId1().toString()) != null) {
                        try {
                            Objects.requireNonNull(systemBeacons.get(b.getId1().toString())).setRssi(b.getRssi());
                        } catch (NullPointerException e) {
                            Log.e(TAG, "Null object has been detected in the system beacons HashMap.");
                        }
                    }
                }
            }
        });

        // Start new scan
        try {
            beaconManager.startRangingBeaconsInRegion(new Region("matwh.rangingID", null, null, null));
        } catch (RemoteException e) {
            Log.e(TAG, "Beacon scan failed to start.");
        }
    }


    /**
     * Update the UI and reset RSSI values after doing so
     */
    private void update() {
        // Repeat the task every 3 seconds
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Find the beacon with the strongest RSSI value
                SystemBeacon closestBeacon = null;
                for(SystemBeacon beacon : systemBeacons.values()) {
                    if(closestBeacon == null || beacon.getRssi() > closestBeacon.getRssi()) {
                        closestBeacon = beacon;
                    }
                }
                // Update the UI with the beacons label and reset all RSSI readings to
                // -Double.MAX_VALUE
                final SystemBeacon finalClosestBeacon = closestBeacon;
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(finalClosestBeacon != null && finalClosestBeacon.getRssi() != -Double.MAX_VALUE) {
                            roomText.setText(finalClosestBeacon.getLabel());
                        }
                        clearReadings();
                    }
                });

            }
        },0,3000);
    }

    /**
     * Reset all RSSI readings to -Double.MAX_VALUE
     */
    private void clearReadings() {
        for(SystemBeacon beacon : systemBeacons.values()) {
            beacon.setRssi(-Double.MAX_VALUE);
        }
    }
}
