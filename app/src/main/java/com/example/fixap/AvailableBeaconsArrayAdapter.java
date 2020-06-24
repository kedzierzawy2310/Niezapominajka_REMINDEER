package com.example.fixap;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.estimote.coresdk.observation.region.beacon.BeaconRegion;

import java.util.List;
import java.util.Random;

public class AvailableBeaconsArrayAdapter extends ArrayAdapter<BeaconRegion> {

    private String availableBeacon = "0";
    private CompoundButton compoundButton = null;

    AvailableBeaconsArrayAdapter(@NonNull Context context, @NonNull List<BeaconRegion> objects) {
        super(context, R.layout.available_beacon_layout, R.id.availableBeaconText, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        super.getView(position, convertView, parent);
        final BeaconRegion beaconRegion = getItem(position);
        final ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.available_beacon_layout, parent, false);
            Random random = new Random();
            Integer[] images = {R.drawable.beacon_blue, R.drawable.beacon_green, R.drawable.beacon_purple};
            viewHolder.imageView = convertView.findViewById(R.id.availableBeaconImage);
            viewHolder.imageView.setImageResource(images[random.nextInt(images.length)]);
            viewHolder.beaconName = convertView.findViewById(R.id.availableBeaconText);
            viewHolder.radioButton = convertView.findViewById(R.id.availableBeaconRadioButton);
            viewHolder.radioButton.setOnCheckedChangeListener(null);
            viewHolder.radioButton.setChecked(false);
            viewHolder.radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        if (compoundButton != null) {
                            compoundButton.setChecked(false);
                        }
                        compoundButton = buttonView;
                        assert beaconRegion != null;
                        setBeacon(beaconRegion.getIdentifier());
                    }
                }
            });
            convertView.setTag(viewHolder);
        } else viewHolder = (ViewHolder) convertView.getTag();

        assert beaconRegion != null;
        viewHolder.beaconName.setText(beaconRegion.getIdentifier());

        return convertView;
    }

    void setBeacon(String beacon){
        this.availableBeacon = beacon;
    }

    String getBeacon(){
        return availableBeacon;
    }

    class ViewHolder{
        ImageView imageView;
        TextView beaconName;
        RadioButton radioButton;
    }
}
