package com.example.android.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {

    private final String FORECASTFRAGMENT_TAG = "FORECAST_FRAGMENT";

    private String mLocation;
    private boolean isMetric;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ForecastFragment(), FORECASTFRAGMENT_TAG)
                    .commit();
        }

        mLocation = Utility.getPreferredLocation(this);
        isMetric = Utility.isMetric(this);

        Log.d("Lifecycle", "Created");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("Lifecycle", "Started");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("Lifecycle", "Stopped");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("Lifecycle", "Paused");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Lifecycle", "Resumed");
        String currentLocationSetting = Utility.getPreferredLocation(this);

        if (!(currentLocationSetting.equalsIgnoreCase(mLocation))){
            ForecastFragment forecastFragment = (ForecastFragment) getSupportFragmentManager().findFragmentByTag(FORECASTFRAGMENT_TAG);
            forecastFragment.onLocationChanged();
            mLocation = Utility.getPreferredLocation(this);
        }

        if (isMetric != Utility.isMetric(this)) {
            ForecastFragment forecastFragment = (ForecastFragment) getSupportFragmentManager().findFragmentByTag(FORECASTFRAGMENT_TAG);
            forecastFragment.onUnitsChanged();
            isMetric = Utility.isMetric(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Lifecycle", "Destroyed");
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
            Intent showSettings = new Intent(this, SettingsActivity.class);
            startActivity(showSettings);
            return true;
        } else if (id == R.id.action_pref_location){
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            Intent showLocation = new Intent(Intent.ACTION_VIEW);
            showLocation.setData(Uri.parse("geo:0,0?q=" + preferences.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default))));

            if (showLocation.resolveActivity(getPackageManager()) != null){
                startActivity(showLocation);
            } else {
                Toast.makeText(this, "Unable to view location", Toast.LENGTH_LONG).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }
}
