package com.hosung.note.noteandapp;

/**
 * Created by Hosung, Lee on 2016. 12. 7..
 */

public class NoteInfo {

    private long noteno;
    private String note;
    private String photofile;
    private double latitude;
    private double longitude;
    private String address;

    public long getNoteno() { return noteno; }

    public void setNoteno(long noteno) { this.noteno = noteno; }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getPhotofile() { return photofile; }

    public void setPhotofile(String photofile) { this.photofile = photofile; }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public NoteInfo() {
        this.noteno = 0;
        this.note = "";
        this.photofile = "";
        this.latitude = 0;
        this.longitude = 0;
        this.address = "";
    }

}
