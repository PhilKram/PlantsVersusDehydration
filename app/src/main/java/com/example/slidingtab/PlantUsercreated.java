package com.example.slidingtab;


import android.net.Uri;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class PlantUsercreated {
    private String Name;
    private String Date;
    private String Frequency;
    private String Picture;

    public PlantUsercreated(){

    }

    public PlantUsercreated(String Name, String Date, String Frequency, String Picture)
    {
        this.Name = Name;
        this.Date = Date;
        this.Frequency = Frequency;
        this.Picture = Picture;
    }

    public String getName() {
        return Name;
    }

    public String getDate() {
        return Date;
    }

    public String getFrequency() {
        return Frequency;
    }

    public String getPicture() {
        return Picture;
    }

    public void setName(String pName) {
        this.Name = pName;
    }

    public void setDate(String pDate) {
        this.Date = pDate;
    }

    public void setFrequency(String pFrequency) {
        this.Frequency = pFrequency;
    }

    public void setPicture(String picture) {
       Picture = picture;
    }
}
