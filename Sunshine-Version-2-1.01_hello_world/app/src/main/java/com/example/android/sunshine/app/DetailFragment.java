package com.example.android.sunshine.app;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.sunshine.app.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;
    static final int COL_WEATHER_HUMIDITY = 9;
    static final int COL_WEATHER_PRESSURE = 10;
    static final int COL_WIND_SPEED = 11;
    static final int COL_WIND_DEGREES = 12;

    private static final int LOADER_ID = 0;

    private ShareActionProvider shareActionProvider;

    private String SHARE_HASHTAG = " #SunshineApp";
    private String forecastData;

    private TextView dayTextView, dateTextView, maxTextView, minTextView, humidityTextView, windTextView, pressureTextView, forecastTextView;
    private ImageView iconView;

    private Uri mUri;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    public static DetailFragment newInstance(Uri dateUri) {
        DetailFragment df = new DetailFragment();

        Bundle args = new Bundle();
        args.putString("uri", dateUri.toString());
        df.setArguments(args);

        return df;
    }

    public Uri getParsedUri (){
        if (getArguments() != null) {
            return Uri.parse(getArguments().getString("uri", null));
        }

        return null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.detailfragment, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);

        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if ((shareActionProvider != null) && (forecastData != null)){
            shareActionProvider.setShareIntent(createShareForecastIntent());
        } else {
            Log.e("Share Options", "action provider is null");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        dayTextView = (TextView) rootView.findViewById(R.id.detail_day_textview);
        dateTextView = (TextView) rootView.findViewById(R.id.detail_date_textview);
        dateTextView = (TextView) rootView.findViewById(R.id.detail_date_textview);
        maxTextView = (TextView) rootView.findViewById(R.id.detail_max_textview);
        minTextView = (TextView) rootView.findViewById(R.id.detail_min_textview);
        humidityTextView = (TextView) rootView.findViewById(R.id.detail_humidity_textview);
        windTextView = (TextView) rootView.findViewById(R.id.detail_wind_textview);
        pressureTextView = (TextView) rootView.findViewById(R.id.detail_pressure_textview);
        forecastTextView = (TextView) rootView.findViewById(R.id.detail_forecast_textview);
        iconView = (ImageView) rootView.findViewById(R.id.detail_icon);

        return rootView;
    }

    private Intent createShareForecastIntent(){
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, forecastData + SHARE_HASHTAG);

        return shareIntent;
    }

    void onLocationChanged( String newLocation ) {
        // replace the uri, since the location has changed
        Uri uri = mUri;
        if (uri != null) {
            long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
            Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
            mUri = updatedUri;
            getLoaderManager().restartLoader(LOADER_ID, null, this);
        }
    }

    void onUnitsChanged(){
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        mUri = getParsedUri();

        if (mUri == null){
            return null;
        }

        return new CursorLoader(getActivity(),
                mUri,
                FORECAST_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst() == false){
            return;
        }

        Context context = getActivity();
        boolean isMetric = Utility.isMetric(context);

        iconView.setImageResource(Utility.getArtResourceForWeatherCondition(data.getInt(COL_WEATHER_CONDITION_ID)));

        dayTextView.setText(Utility.getDayName(context, data.getLong(COL_WEATHER_DATE)));
        dateTextView.setText(Utility.getFormattedMonthDay(context, data.getLong(COL_WEATHER_DATE)));

        maxTextView.setText(Utility.formatTemperature(context, data.getDouble(COL_WEATHER_MAX_TEMP), isMetric));
        minTextView.setText(Utility.formatTemperature(context, data.getDouble(COL_WEATHER_MIN_TEMP), isMetric));
        humidityTextView.setText(getString(R.string.format_humidity, data.getFloat(COL_WEATHER_HUMIDITY)));
        windTextView.setText(Utility.getFormattedWind(context, data.getFloat(COL_WIND_SPEED), data.getFloat(COL_WIND_DEGREES)));
        pressureTextView.setText(getString(R.string.format_pressure, data.getFloat(COL_WEATHER_PRESSURE)));
        forecastTextView.setText(data.getString(COL_WEATHER_DESC));


        // Keep this for sharing
        String date = Utility.formatDate(data.getLong(COL_WEATHER_DATE));
        String minTemp = Utility.formatTemperature(getActivity(), data.getDouble(COL_WEATHER_MIN_TEMP), Utility.isMetric(getActivity()));
        String maxTemp = Utility.formatTemperature(getActivity(), data.getDouble(COL_WEATHER_MAX_TEMP), Utility.isMetric(getActivity()));

        forecastData = date + " - " + data.getString(COL_WEATHER_DESC) + " - " + maxTemp + "/" + minTemp;

        if (shareActionProvider != null){
            shareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}