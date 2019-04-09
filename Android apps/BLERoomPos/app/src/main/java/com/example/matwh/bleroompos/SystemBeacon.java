package com.example.matwh.bleroompos;

/**
 * Class to represent a physical beacon that is placed in each room.
 */
class SystemBeacon {

    private String uuid;
    private double rssi;
    private String label;

    /* Initialise the beacon with RSSI = -Double.MAX_VALUE */
    SystemBeacon(String uuid, String label) {
        this.uuid = uuid;
        this.rssi = -Double.MAX_VALUE;
        this.label = label;
    }

    String getUuid() {
        return uuid;
    }

    double getRssi() {
        return rssi;
    }

    void setRssi(double rssi) {
        this.rssi = rssi;
    }

    String getLabel() {
        return label;
    }
}
