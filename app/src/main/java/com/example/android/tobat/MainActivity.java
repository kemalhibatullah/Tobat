package com.example.android.tobat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.text.format.DateFormat;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.DigitalClock;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.android.tobat.api.ApiService;
import com.example.android.tobat.api.ApiUrl;
import com.example.android.tobat.model.Model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private TextView lokasiShalat, jadwalSyuruq, jadwalSubuh, jadwalDzuhur, jadwalAshar, jadwalMagrib, jadwalIsya, jadwalSemua,
    lokasi;
    String url2;

    double latitude = 0.0;
    double longitude = 0.0;
    static String TAG = "MainActivity";
    Location gps_loc = null, network_loc = null, final_loc = null;

    ProgressDialog pDialog;
    String tag_json_obj = "json_obj_req";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getJadwal();
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        lokasi = (TextView) findViewById(R.id.toolbar_title);
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED){

            Toast.makeText(this, "not granted", Toast.LENGTH_SHORT).show();
        }

        else {
            Toast.makeText(this, "Granted", Toast.LENGTH_SHORT).show();
        }

        try {
            gps_loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            network_loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }catch (Exception e){
            e.printStackTrace();
        }

        if (gps_loc!=null){
            final_loc = gps_loc;
            latitude = final_loc.getLatitude();
            longitude = final_loc.getLongitude();
        } else if (network_loc != null){
            final_loc = network_loc;
            latitude = final_loc.getLatitude();
            longitude = final_loc.getLongitude();
        } else {
            latitude = 0.0;
            longitude= 0.0;
        }

        try {
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && addresses.size() > 0) {
                String address = addresses.get(0).getAddressLine(0);
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getSubAdminArea();
                String country = addresses.get(0).getCountryName();
                String postal_code = addresses.get(0).getPostalCode();
                String knowName = addresses.get(0).getFeatureName();

                lokasi.setText(state);
                url2 = "https://muslimsalat.com/"+state+".json?key=37815754ba4d2da37b272281fc3abf40";
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        TextView wktShalat = (TextView) findViewById(R.id.jadwalShalat);
        Calendar cal = Calendar.getInstance(); //Create Calendar-Object
        cal.setTime(new Date());               //Set the Calendar to now
        int hour = cal.get(Calendar.HOUR_OF_DAY);

        if (hour >= 5 && hour <= 12){
            wktShalat.setText("Dzuhur");
            getJadwalDzuhur();
        }else if (hour > 12 && hour <= 15){
            wktShalat.setText("Ashar");
            getJadwalAshar();
        }else if (hour > 15 && hour <= 18){
            wktShalat.setText("Magrib");
            getJadwalMagrib();
        }else if (hour > 18 && hour <= 19){
            wktShalat.setText("Isya");
            getJadwalIsya();
        }else {
            wktShalat.setText("Subuh");
            getJadwalSubuh();
        }

        jadwalSemua = findViewById(R.id.waktuShalat);

        jadwalSyuruq = findViewById(R.id.jadwalSyuruq);
        jadwalSubuh = findViewById(R.id.jadwalSubuh);
        jadwalDzuhur = findViewById(R.id.jadwalDzuhur);
        jadwalAshar = findViewById(R.id.jadwalAshar);
        jadwalMagrib = findViewById(R.id.jadwalMagrib);
        jadwalIsya = findViewById(R.id.jadwalIsya);


//        onLocationChanged(location);
//        loc_func(location);





    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.beranda) {
            // Handle the camera action
        } else if (id == R.id.kotaLain) {
            Intent intent = new Intent(getApplicationContext(), KotaLain.class);
            startActivity(intent);
        } else if (id == R.id.kompas) {
            Intent intent2 = new Intent(this, KompasActivity.class);
            startActivity(intent2);
        }
//        else if (id == R.id.pengaturan){
//
//        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void getJadwal () {
        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Proses...");
        pDialog.show();

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                url2, null,
                new com.android.volley.Response.Listener<JSONObject>() {



                    @Override
                    public void onResponse(JSONObject response) {
                        //getData from JSON
                        try {
                            //getlocation
                            String country = response.get("country").toString();
                            String state = response.get("state").toString();
                            String city = response.get("city").toString();
                            String location = country +", "+ state +", "+ city;
                            //get date
                            String date = response.getJSONArray("items").getJSONObject(0).get("date_for").toString();

                            String mFajar = response.getJSONArray("items").getJSONObject(0).get("fajr").toString();
                            String mShurroq = response.getJSONArray("items").getJSONObject(0).get("shurooq").toString();
                            String mDhuhr = response.getJSONArray("items").getJSONObject(0).get("dhuhr").toString();
                            String mAsr = response.getJSONArray("items").getJSONObject(0).get("asr").toString();
                            String mMaghrib = response.getJSONArray("items").getJSONObject(0).get("maghrib").toString();
                            String mIsha = response.getJSONArray("items").getJSONObject(0).get("isha").toString();

                            //set this data to TextViews
                            jadwalSyuruq.setText(mShurroq);
                            jadwalSubuh.setText(mFajar);
                            jadwalDzuhur.setText(mDhuhr);
                            jadwalAshar.setText(mAsr);
                            jadwalMagrib.setText(mMaghrib);
                            jadwalIsya.setText(mIsha);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        pDialog.hide();
                    }
                }, new com.android.volley.Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Toast.makeText(MainActivity.this, "Eror", Toast.LENGTH_SHORT).show();
                // hide the progress dialog
                pDialog.hide();
            }
        });

// Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);
    }

    private void getJadwalDzuhur () {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiUrl.URL_ROOT_HTTP)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        Call<Model> call = apiService.getJadwal();

        call.enqueue(new Callback<Model>() {
            @Override
            public void onResponse(Call<Model> call, Response<Model> response) {
                if (response.isSuccessful()){
                    jadwalSemua.setText(response.body().getItems().get(0).getDhuhr());
                } else {

                }
            }

            @Override
            public void onFailure(Call<Model> call, Throwable t) {

            }
        });
    }
    private void getJadwalAshar () {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiUrl.URL_ROOT_HTTP)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        Call<Model> call = apiService.getJadwal();

        call.enqueue(new Callback<Model>() {
            @Override
            public void onResponse(Call<Model> call, Response<Model> response) {
                if (response.isSuccessful()){
                    jadwalSemua.setText(response.body().getItems().get(0).getAsr());
                } else {

                }
            }

            @Override
            public void onFailure(Call<Model> call, Throwable t) {

            }
        });
    }
    private void getJadwalMagrib () {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiUrl.URL_ROOT_HTTP)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        Call<Model> call = apiService.getJadwal();

        call.enqueue(new Callback<Model>() {
            @Override
            public void onResponse(Call<Model> call, Response<Model> response) {
                if (response.isSuccessful()){
                    jadwalSemua.setText(response.body().getItems().get(0).getMaghrib());
                } else {

                }
            }

            @Override
            public void onFailure(Call<Model> call, Throwable t) {

            }
        });
    }
    private void getJadwalIsya () {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiUrl.URL_ROOT_HTTP)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        Call<Model> call = apiService.getJadwal();

        call.enqueue(new Callback<Model>() {
            @Override
            public void onResponse(Call<Model> call, Response<Model> response) {
                if (response.isSuccessful()){
                    jadwalSemua.setText(response.body().getItems().get(0).getIsha());
                } else {

                }
            }

            @Override
            public void onFailure(Call<Model> call, Throwable t) {

            }
        });
    }
    private void getJadwalSubuh () {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiUrl.URL_ROOT_HTTP)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        Call<Model> call = apiService.getJadwal();

        call.enqueue(new Callback<Model>() {
            @Override
            public void onResponse(Call<Model> call, Response<Model> response) {
                if (response.isSuccessful()){
                    jadwalSemua.setText(response.body().getItems().get(0).getFajr());
                } else {

                }
            }

            @Override
            public void onFailure(Call<Model> call, Throwable t) {

            }
        });
    }

//    @Override
//    public void onLocationChanged(Location location) {
//        longitude = location.getLongitude();
//        latitude = location.getLatitude();
//        //textView
//    }
//
//    @Override
//    public void onStatusChanged(String provider, int status, Bundle extras) {
//
//    }
//
//    @Override
//    public void onProviderEnabled(String provider) {
//
//    }
//
//    @Override
//    public void onProviderDisabled(String provider) {
//
//    }
//
//    private void loc_func(Location location){
//        try {
//            Geocoder geocoder = new Geocoder(this);
//            List<Address> addresses = null;
//            addresses = geocoder.getFromLocation(latitude, longitude, 1);
//            String country = addresses.get(0).getCountryName();
//            String city = addresses.get(0).getLocality();
//            lokasi.setText("Cou: " + country + "Cit: " + city);
//        }catch (IOException e){
//            e.printStackTrace();
//            Toast.makeText(getApplicationContext(), "Error:" + e, Toast.LENGTH_SHORT).show();
//        }
//    }
}
