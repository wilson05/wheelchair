package com.quantum.gps2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class deviceAdapter extends ArrayAdapter<deviceblu> {

    public deviceAdapter(Context context, ArrayList<deviceblu> devices) {
        super(context, 0, devices);
    }

      @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        deviceblu mdeviceblu = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.items, parent, false);
        }
        // Lookup view for data population
        TextView name = (TextView) convertView.findViewById(R.id.name);
        TextView rssi = (TextView) convertView.findViewById(R.id.rssi);
        TextView msb = (TextView) convertView.findViewById(R.id.msb);
        TextView lsb = (TextView) convertView.findViewById(R.id.lsb);
        // Populate the data into the template view using the data object
        name.setText(mdeviceblu.mname);
        rssi.setText(mdeviceblu.mrssi);
        msb.setText(mdeviceblu.mmsb);
        lsb.setText(mdeviceblu.mlbs);
        // Return the completed view to render on screen
        return convertView;
    }
}
