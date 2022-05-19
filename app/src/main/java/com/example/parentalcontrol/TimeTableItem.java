package com.example.parentalcontrol;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.Serializable;

public class TimeTableItem implements  Serializable {
    private String from;
    private String to;
    private String duration;
    private String interval;
    private String sum;
    public Boolean isSelected = false;

    public TimeTableItem(String from, String to, String duration, String interval, String sum) {
        this.from = from;
        this.to = to;
        this.duration = duration;
        this.interval = interval;
        this.sum = sum;
    }

    public TimeTableItem() {
        this.from = "";
        this.to = "";
        this.duration = "";
        this.interval = "";
        this.sum = "";
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public String getSum() {
        return sum;
    }

    public void setSum(String sum) {
        this.sum = sum;
    }
    public String toString(){
        StringBuilder sb = new StringBuilder();
        if(!from.equals("")){
            sb.append("F").append(from).append(" ");
        }
        if(!to.equals("")){
            sb.append("T").append(to).append(" ");
        }
        if(!duration.equals("")){
            sb.append("D").append(duration).append(" ");
        }
        if(!interval.equals("")){
            sb.append("I").append(interval).append(" ");
        }
        if(!sum.equals("")){
            sb.append("S").append(sum).append(" ");
        }
        sb.append("\n");
        return sb.toString();
    }

}
