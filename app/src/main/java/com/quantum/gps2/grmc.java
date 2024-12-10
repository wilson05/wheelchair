package com.quantum.gps2;

public class grmc {

    //"GPRMC,203446.000,A,0441.8612,N,07408.5924,W,0.01,232.52,030920,,
    //$GPRMC,142538.000,A,3.3691532,N,76.5218082,W,0.007142381,231.89026,090920,,

    public String create_rmc(double lat, double long1, String hora,String fecha2, String speed1, String heading1)
    {
        String  rmc=null;
        rmc+="$GPRMC,";
        rmc+=hora;
        rmc+=",A,";
        rmc+=lat;
        rmc+=",N,";
        rmc+=long1;
        rmc+=",W,";
        rmc+=speed1;
        rmc+=",";
        rmc+=heading1;
        rmc+=",";
        rmc+=fecha2;
        rmc+=",,";

        return rmc;
    }


}
