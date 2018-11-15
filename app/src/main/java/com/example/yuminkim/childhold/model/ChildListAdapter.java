package com.example.yuminkim.childhold.model;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.yuminkim.childhold.R;
import com.example.yuminkim.childhold.activity.DriverActivity;

import java.io.IOException;
import java.sql.Driver;
import java.util.ArrayList;

public class ChildListAdapter extends BaseAdapter {
    Context context;
    ArrayList<Child> childArrayList;
    TextView child_name, child_location;

    public ChildListAdapter(Context context, ArrayList<Child> childArrayList) {
        this.context = context;
        this.childArrayList = childArrayList;
    }

    @Override
    public int getCount() {
        return this.childArrayList.size();
    }

    @Override
    public Object getItem(int i) {
        return childArrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        String location = null;

        if(view == null){
            view = LayoutInflater.from(context).inflate(R.layout.child_list, null);
            child_location = (TextView)view.findViewById(R.id.child_location);
            child_name = (TextView)view.findViewById(R.id.child_name);
            Geocoder geocoder = new Geocoder(context);
            try {
                Address address = geocoder.getFromLocation(childArrayList.get(i).getLatLng().getLat(), childArrayList.get(i).getLatLng().getLng(), 1).get(0);

                location = address.getLocality() + " " + address.getThoroughfare() + " " +  address.getFeatureName().trim() ;
            } catch (IOException e) {
                e.printStackTrace();
            }

            child_location.setText(location);
            child_name.setText(childArrayList.get(i).getName());
        }
        return view;
    }
}
