package com.quantum.gps2;
public class deviceblu {
    String mname;
    String mrssi;
    String mlbs;
    String mmsb;

    public deviceblu(String name, String rssi, String msb, String lsb){
        mname=name;
        mrssi=rssi;
        mmsb=msb;
        mlbs=lsb;
    }

    public String getMname() {
        return mname;
    }

    public void setMname(String mname) {
        this.mname = mname;
    }

    public String getMrssi() {
        return mrssi;
    }

    public void setMrssi(String mrssi) {
        this.mrssi = mrssi;
    }

    public String getMlbs() {
        return mlbs;
    }

    public void setMlbs(String mlbs) {
        this.mlbs = mlbs;
    }

    public String getMmsb() {
        return mmsb;
    }

    public void setMmsb(String mmsb) {
        this.mmsb = mmsb;
    }

    public void getIfExists(String nombre) {


    }
}
