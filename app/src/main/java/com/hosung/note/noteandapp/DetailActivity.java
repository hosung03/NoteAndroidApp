package com.hosung.note.noteandapp;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private String TAG = DetailActivity.class.getSimpleName();

    public static final short PICKUP_CAMERA_PHOTO = 0;
    public static final short PICKUP_GALLARY_PHOTO = 1;
    public static final short SEARCH_LOCATION = 2;

    private EditText txtNote = null;
    private ImageView ivPhoto = null;
    private TextView tvLocation = null;

    private Boolean isEditNote = false;
    private NoteInfo mNoteInfo = null;
    private String curPhotoFile = null;
    private Bitmap curPhotoBmp = null;
    private String mCurAddress = null;
    private LatLng mCurLocation = null;
    private LatLng mGeoLocation = null;

    private static final int ERROR_DIALOG_REQUEST = 9001;
    private GoogleMap mMap;
    private GoogleApiClient mLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_detail);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveNoteInfo();
                setResult(MainActivity.RESULT_OK);
                finish();
            }
        });

        isEditNote = getIntent().getBooleanExtra("isEditNote", false);

        TextView toolbar_detail_title = (TextView) findViewById(R.id.toolbar_detail_title);
        if(isEditNote){
            toolbar_detail_title.setText("Edit Note");
        } else {
            toolbar_detail_title.setText("Add Note");
        }

        mNoteInfo = MainActivity.arrNoteList.get(MainActivity.currentIndex);

        // note
        txtNote = (EditText) findViewById(R.id.txtNote);
        if (isEditNote && !mNoteInfo.getNote().equals(MainActivity.BLANK_NOTE) && !mNoteInfo.getNote().equals("")) {
            txtNote.setText(mNoteInfo.getNote());
        }

        ivPhoto = (ImageView) findViewById(R.id.ivPhoto);
        if (isEditNote && !mNoteInfo.getPhotofile().equals("")) {
            curPhotoFile = mNoteInfo.getPhotofile();
            new LoadImageTask(ivPhoto, curPhotoFile).execute();
        }

        Button btnAddChangeImageFromGallary = (Button) findViewById(R.id.btnAddChangeImageFromGallary);
        btnAddChangeImageFromGallary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICKUP_GALLARY_PHOTO);
            }
        });

        Button btnAddChangeImageFromCamera = (Button) findViewById(R.id.btnAddChangeImageFromCamera);
        btnAddChangeImageFromCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, PICKUP_CAMERA_PHOTO);
            }
        });

        tvLocation = (TextView) findViewById(R.id.tvLocation);

        if (servicesOK() && initMap()) {
                mLocationClient = new GoogleApiClient.Builder(this)
                        .addApi(LocationServices.API)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .build();

                mLocationClient.connect();
//                mMap.setMyLocationEnabled(true);
        } else {
            Toast.makeText(this, "Map not connected!", Toast.LENGTH_SHORT).show();
        }

        Button btnChangeLocation = (Button) findViewById(R.id.btnChangeLocation);
        btnChangeLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DetailActivity.this,LocationSearchActivity.class);
                startActivityForResult(intent, SEARCH_LOCATION);
            }
        });

        if (isEditNote){
            txtNote.setFocusable(false);
            txtNote.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    view.setFocusable(true);
                    view.setFocusableInTouchMode(true);
                    return false;
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        saveNoteInfo();
        super.onBackPressed();
    }

    private void saveNoteInfo() {
        if(!txtNote.getText().toString().equals("")) {
            mNoteInfo.setNote(txtNote.getText().toString());
        }

        if(savePhotoThumb()) {
            mNoteInfo.setPhotofile(curPhotoFile);
        }

        if(mGeoLocation != mCurLocation
                || !txtNote.getText().toString().equals("")
                || curPhotoFile != null){
            if (mCurLocation != null) {
                mNoteInfo.setLatitude(mCurLocation.latitude);
                mNoteInfo.setLongitude(mCurLocation.longitude);
                if (mCurAddress != null) {
                    mNoteInfo.setAddress(mCurAddress);
                }
            }
        }

        MainActivity.saveTODB();
    }

    private Boolean savePhotoThumb() {
        if(curPhotoBmp == null || curPhotoFile == null || curPhotoFile.equals(mNoteInfo.getPhotofile()) )
            return false;

        String path = Environment.getExternalStorageDirectory().getAbsolutePath().toString() + "/NoteAndApp";
        File dir = new File(path);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Toast.makeText(this, "Problem creating folder", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        OutputStream fOut = null;
        try {
            File file = new File(path, "/" + curPhotoFile);
            fOut = new FileOutputStream(file);
            curPhotoBmp.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case PICKUP_CAMERA_PHOTO:
            case PICKUP_GALLARY_PHOTO:
                if(resultCode == RESULT_OK){
                    Uri uri = data.getData();
                    //ivPhoto.setImageURI(uri);
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    try {
                        BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, options);
                        options.inSampleSize = calculateInSampleSize(options, 100, 100);
                        options.inJustDecodeBounds = false;
                        curPhotoBmp = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, options);
                        ivPhoto.setImageBitmap(curPhotoBmp);

                        Long tsLong = System.currentTimeMillis() / 1000;
                        curPhotoFile = tsLong.toString() + ".png";
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case SEARCH_LOCATION:
                if(data != null) {
                    double lat = data.getDoubleExtra("search_latitude", 0.0);
                    double log = data.getDoubleExtra("search_longitude", 0.0);
                    if (lat != 0 && log != 0) {
                        mCurLocation = new LatLng(lat, log);
                        gotoLocation(mCurLocation, 15);
                        setLocationAddres(mCurLocation);
                    }
                }
                //Toast.makeText(this,"Result Search Location: "+search_location,Toast.LENGTH_SHORT).show();
                break;
        }
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public boolean servicesOK() {
        int result = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (result == ConnectionResult.SUCCESS) {
            return true;
        } else if (GooglePlayServicesUtil.isUserRecoverableError(result)) {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(result, this, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, getString(R.string.error_connect_to_services), Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private boolean initMap() {
        if (mMap == null) {
            SupportMapFragment mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fgNoteMap);
            mMap = mapFrag.getMap();
        }
        return (mMap != null);
    }

    @Override
    public void onConnected(Bundle bundle) {
        //Toast.makeText(this, "Ready to map!", Toast.LENGTH_SHORT).show();
        if (mNoteInfo.getLatitude() != 0 && mNoteInfo.getLongitude() != 0) {
            // go to location
            mCurLocation = new LatLng(
                    mNoteInfo.getLatitude(),
                    mNoteInfo.getLongitude()
            );
            gotoLocation(mCurLocation,15);
            setLocationAddres(mCurLocation);
        } else {
            // current location
            Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(mLocationClient);
            if (currentLocation == null) {
                Toast.makeText(this, "Couldn't connect!", Toast.LENGTH_SHORT).show();
            } else {
                mCurLocation = new LatLng(
                        currentLocation.getLatitude(),
                        currentLocation.getLongitude()
                );
                mGeoLocation = mCurLocation;
                gotoLocation(mCurLocation,15);
                setLocationAddres(mCurLocation);
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private void gotoLocation(LatLng latLng, float zoom) {
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        mMap.moveCamera(update);
    }

    private void setLocationAddres(LatLng latLng) {
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
        mCurAddress = getAddress(address);
        if (mCurAddress != null) {
            String txtLoction = "Latitude: " + String.valueOf(latLng.latitude) +
                    "\nLongitude: " + String.valueOf(latLng.longitude) +
                    "\nLocation: " + mCurAddress ;
            tvLocation.setText(txtLoction);
            addMarker(address, latLng);
        }
    }

    private String getAddress(Address address) {
        String strAddress = null;

        if(address.getAddressLine(0)!=null) strAddress = address.getAddressLine(0);
        if(address.getLocality()!=null) {
            if (address.equals("")) strAddress = address.getLocality();
            else strAddress += ", " + address.getLocality();
        }
        if(address.getAdminArea()!=null) {
            if (address.equals("")) strAddress = address.getAdminArea();
            else strAddress += ", " + address.getAdminArea();
        }
        if(address.getPostalCode()!=null) {
            if (address.equals("")) strAddress = address.getPostalCode();
            else strAddress += ", " + address.getPostalCode();
        }
        if(address.getCountryName()!=null) {
            if (address.equals("")) strAddress = address.getCountryName();
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

        MarkerOptions options = new MarkerOptions()
                .title(title)
                .position(new LatLng(latLng.latitude, latLng.longitude));
        mMap.addMarker(options);
    }

}
