package com.hosung.note.noteandapp;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationSearchActivity extends AppCompatActivity {

    private static final int ERROR_DIALOG_REQUEST = 9001;
    private GoogleMap mMap;
    private Marker mMarker;

    private String mCurAddress = null;
    private LatLng mCurLocation = null;
    private ListView lvSearchLocationList = null;
    private LinearLayout llSearchMap = null;
    private RelativeLayout rlSearchList = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_search_location);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);

//        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (mCurLocation == null){
//                    setResult(DetailActivity.RESULT_OK);
//                    finish();
//                    return;
//                }
//                confirmToChangeLocation();
//            }
//        });

        Button btnYesItem = (Button) findViewById(R.id.btnYesItem);
        btnYesItem.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (mCurLocation == null || mCurAddress == null){
                    AlertDialog.Builder builder = new AlertDialog.Builder(LocationSearchActivity.this);
                    builder.setMessage("Please search new location!");
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
                else {
                    Intent intent = new Intent();
                    intent.putExtra("search_latitude", mCurLocation.latitude);
                    intent.putExtra("search_longitude", mCurLocation.longitude);
                    setResult(DetailActivity.RESULT_OK, intent);
                    finish();
                }
            }
        });

        Button btnNoItem = (Button) findViewById(R.id.btnNoItem);
        btnNoItem.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                setResult(DetailActivity.RESULT_OK);
                finish();
            }
        });

        llSearchMap = (LinearLayout) findViewById(R.id.llSearchMap);
        rlSearchList = (RelativeLayout) findViewById(R.id.rlSearchList);
        lvSearchLocationList = (ListView) findViewById(R.id.lvSearchLocationList);

        Button btnSearchLocation = (Button) findViewById(R.id.btnSearchLocation);
        btnSearchLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideSoftKeyboard(view);

                TextView txtSearchWord = (TextView) findViewById(R.id.txtSearchWord);
                String searchString = txtSearchWord.getText().toString();

                Geocoder gc = new Geocoder(LocationSearchActivity.this);
                List<Address> list = null;
                try {
                    list = gc.getFromLocationName(searchString, 50);
                } catch (IOException e) {
                    e.getStackTrace();
                    Toast.makeText(LocationSearchActivity.this, "No location of "+searchString+"", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (list == null) {
                    Toast.makeText(LocationSearchActivity.this, "No location of "+searchString+"", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (list.size() == 1) {
                    Address address = list.get(0);
                    mCurLocation = new LatLng (
                            address.getLatitude(),
                            address.getLongitude()
                    );
                    gotoLocation(mCurLocation, 15);
                    setLocationAddress(mCurLocation);
                    addMarker(address, mCurLocation);
                } else {
                    llSearchMap.setVisibility(View.GONE);
                    rlSearchList.setVisibility(View.VISIBLE);

                    SearchAddressAdapter adapter = new SearchAddressAdapter(LocationSearchActivity.this,
                            android.R.layout.simple_list_item_1, list);
                    lvSearchLocationList.setAdapter(adapter);
                    lvSearchLocationList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            Address address = (Address) lvSearchLocationList.getItemAtPosition(i);
                            mCurLocation = new LatLng (
                                    address.getLatitude(),
                                    address.getLongitude()
                            );
                            gotoLocation(mCurLocation, 15);
                            setLocationAddress(mCurLocation);
                            addMarker(address, mCurLocation);

                            rlSearchList.setVisibility(View.GONE);
                            llSearchMap.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        });

        if (servicesOK()) {
            if (!initMap()) {
                Toast.makeText(this, "Map not connected!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Search Location not available!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        confirmToChangeLocation();
        super.onBackPressed();
    }

    private void confirmToChangeLocation(){
        AlertDialog.Builder builder = new AlertDialog.Builder(LocationSearchActivity.this);
        builder.setMessage("Do you want to change a location ?");
        builder.setCancelable(true);

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                Intent intent = new Intent();
                intent.putExtra("search_latitude",mCurLocation.latitude);
                intent.putExtra("search_longitude",mCurLocation.longitude);
                setResult(DetailActivity.RESULT_OK, intent);
                finish();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                setResult(DetailActivity.RESULT_OK);
                finish();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public class SearchAddressAdapter extends ArrayAdapter<Address> {
        public List<Address> arrAddressList = null;

        public SearchAddressAdapter(Context context, int resource, List<Address> list) {
            super(context, resource, list);
            this.arrAddressList = list;
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            }
            TextView txt = (TextView) convertView.findViewById(android.R.id.text1);
            String place = getAddress(arrAddressList.get(position),false);
            txt.setText(place);
            return convertView;
        }
    }

    public boolean servicesOK() {

        int isAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (GooglePlayServicesUtil.isUserRecoverableError(isAvailable)) {
            Dialog dialog =
                    GooglePlayServicesUtil.getErrorDialog(isAvailable, this, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "Can't connect to mapping service", Toast.LENGTH_SHORT).show();
        }

        return false;
    }

    private boolean initMap() {
        if (mMap == null) {
            SupportMapFragment mapFragment =
                    (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fgSearchMap);
            mMap = mapFragment.getMap();

            if (mMap != null) {
                mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(LatLng latLng) {
                        Geocoder gc = new Geocoder(LocationSearchActivity.this);
                        List<Address> list = null;

                        try {
                            list = gc.getFromLocation(latLng.latitude, latLng.longitude, 1);
                        } catch (IOException e) {
                            e.printStackTrace();
                            return;
                        }

                        Address address = list.get(0);
                        mCurAddress = getAddress(address,false);
                        if (mCurAddress != null) {
                            String txtLoction = "Latitude: " + String.valueOf(latLng.latitude) +
                                    "\nLongitude: " + String.valueOf(latLng.longitude) +
                                    "\nLocation: " + mCurAddress;

                            TextView txtSearchLocation = (TextView) LocationSearchActivity.this.findViewById(R.id.txtSearchLocation);
                            txtSearchLocation.setText(txtLoction);

                            LocationSearchActivity.this.addMarker(address, latLng);
                            mMarker.showInfoWindow();

                            mCurLocation = new LatLng (
                                    latLng.latitude,
                                    latLng.longitude
                            );
                        }
                    }
                });

                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
//                        String msg = mMarker.getTitle() + " (" +
//                                mMarker.getPosition().latitude + ", " +
//                                mMarker.getPosition().longitude + ")";
//                        Toast.makeText(LocationSearchActivity.this, msg, Toast.LENGTH_SHORT).show();
                        mMarker.showInfoWindow();
                        return false;
                    }
                });

                mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                    @Override
                    public void onMarkerDragStart(Marker marker) {
                    }

                    @Override
                    public void onMarkerDrag(Marker marker) {
                    }

                    @Override
                    public void onMarkerDragEnd(Marker marker) {
                        Geocoder gc = new Geocoder(LocationSearchActivity.this);
                        List<Address> list = null;
                        LatLng latLng = marker.getPosition();
                        try {
                            list = gc.getFromLocation(latLng.latitude, latLng.longitude, 1);
                        } catch (IOException e) {
                            e.printStackTrace();
                            return;
                        }

                        Address address = list.get(0);
                        mCurAddress = getAddress(address,false);
                        if (mCurAddress != null) {
                            String txtLoction = "Latitude: " + String.valueOf(latLng.latitude) +
                                    "\nLongitude: " + String.valueOf(latLng.longitude) +
                                    "\nLocation: " + mCurAddress;

                            TextView txtSearchLocation = (TextView) findViewById(R.id.txtSearchLocation);
                            txtSearchLocation.setText(txtLoction);

                            marker.setTitle(address.getAddressLine(0));
                            marker.setSnippet(getAddress(address,true));
                            marker.showInfoWindow();

                            mCurLocation = new LatLng (
                                    latLng.latitude,
                                    latLng.longitude
                            );
                        }
                    }
                });
            }
        }
        return (mMap != null);
    }

    private void hideSoftKeyboard(View v) {
        InputMethodManager imm =
                (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    private void gotoLocation(LatLng latLng, float zoom) {
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        mMap.moveCamera(update);
    }

    private void setLocationAddress(LatLng latLng) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
        } catch (IOException e) {
            Toast.makeText(this, "Couldn't get address!", Toast.LENGTH_SHORT).show();
            e.getStackTrace();
            return;
        }

        Address address = addresses.get(0);
        mCurAddress = getAddress(address,false);
        if (mCurAddress != null) {
            String txtLoction = "Latitude: " + String.valueOf(latLng.latitude) +
                    "\nLongitude: " + String.valueOf(latLng.longitude) +
                    "\nLocation: " + mCurAddress;

            TextView txtSearchLocation = (TextView) findViewById(R.id.txtSearchLocation);
            txtSearchLocation.setText(txtLoction);
            addMarker(address, latLng);
        }
    }

    private String getAddress(Address address, Boolean subtitle) {
        String strAddress = null;

        if(!subtitle && address.getAddressLine(0)!=null) strAddress = address.getAddressLine(0);

        if(address.getLocality()!=null) {
            if (strAddress == null) strAddress = address.getLocality();
            else strAddress += ", " + address.getLocality();
        }
        if(address.getAdminArea()!=null) {
            if (strAddress == null) strAddress = address.getAdminArea();
            else strAddress += ", " + address.getAdminArea();
        }
        if(address.getPostalCode()!=null) {
            if (strAddress == null) strAddress = address.getPostalCode();
            else strAddress += ", " + address.getPostalCode();
        }
        if(address.getCountryName()!=null) {
            if (strAddress == null) strAddress = address.getCountryName();
            else strAddress += ", " + address.getCountryName();
        }
        return strAddress;
    }

    private void addMarker(Address address, LatLng latLng) {
        String title = null;
        if(address.getAddressLine(0)!=null) title = address.getAddressLine(0);
        if(address.getLocality()!=null) {
            if (title.equals("")) title = address.getLocality();
            else title += ", " + address.getLocality();
        }

        if (mMarker != null) {
            mMarker.remove();
        }

        MarkerOptions options = new MarkerOptions()
                .title(title)
                .position(new LatLng(latLng.latitude, latLng.longitude))
                .draggable(true);
        mMarker = mMap.addMarker(options);
        mMarker.setTitle(address.getAddressLine(0));
        mMarker.setSnippet(getAddress(address,true));

    }

}
