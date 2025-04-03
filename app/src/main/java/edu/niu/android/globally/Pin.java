package edu.niu.android.globally;

public class Pin {
    private double latitude;
    private double longitude;
    private String country;

    public Pin(double latitude, double longitude, String country) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.country = country;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getCountry() {
        return country;
    }
}
