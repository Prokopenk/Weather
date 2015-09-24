package com.emamontov.stormy.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.emamontov.stormy.LocationProvider;
import com.emamontov.stormy.R;
import com.emamontov.stormy.weather.Current;
import com.emamontov.stormy.weather.Day;
import com.emamontov.stormy.weather.Forecast;
import com.emamontov.stormy.weather.Hour;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity implements LocationProvider.LocationCallback {

    public static final String TAG = MainActivity.class.getSimpleName();
    public static final String DAILY_FORECAST = "DAILY_FORECAST";
    public static final String HOURLY_FORECAST = "HOURLY_FORECAST";

    private Forecast mForecast;

    private LocationProvider mLocationProvider;

    public double mLatitude =  55.8346;
    public double mLongitude = 37.3234;
    private String mLocationName = "Default Locale";

    @InjectView(R.id.timeLabel) TextView mTimeLabel;
    @InjectView(R.id.temperatureLabel) TextView mTemperatureLabel;
    @InjectView(R.id.humidityValue) TextView mHumidityValue;
    @InjectView(R.id.precipValue) TextView mPrecipValue;
    @InjectView(R.id.summaryLabel) TextView mSummaryLabel;
    @InjectView(R.id.iconImageView) ImageView mIconImageView;
    @InjectView(R.id.refreshImageView) ImageView mRefershImageView;
    @InjectView(R.id.progressBar) ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        mProgressBar.setVisibility(View.INVISIBLE);

        mLocationProvider = new LocationProvider(this, this);

        //final
        //double
//        mLatitude =  55.8346;    //mCurrentLocation.getLatitude();
//        mLongitude = 37.3234;     //mCurrentLocation.getLongitude();

        mRefershImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getForecast(mLatitude, mLongitude);
            }
        });

        getForecast(mLatitude, mLongitude);
            Log.d(TAG, "Main UI code is running!");
    }

    private void getForecast(double latitude, double longitude) {
        String apiKey = "1c0d2252c3cda1e0f98678b0c33ec91e";
        String forecastUrl = "https://api.forecast.io/forecast/" + apiKey +
                "/"  + latitude + "," + longitude;

        if (isNetworkAvailable()) {
            toggleRefresh();

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(forecastUrl).build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                             @Override
                             public void onFailure(Request request, IOException e) {
                                 runOnUiThread(new Runnable() {
                                     @Override
                                     public void run() {
                                         toggleRefresh();

                                     }
                                 });
                                 alertUserAboutError();
                             }

                             @Override
                             public void onResponse(Response response) throws IOException {
                                 runOnUiThread(new Runnable() {
                                     @Override
                                     public void run() {
                                         toggleRefresh();

                                     }
                                 });
                                 try {
                                     String jsonData = response.body().string();
                                     Log.v(TAG, jsonData);
                                     if (response.isSuccessful()) {
                                         mForecast = parseForecastDetails(jsonData);
                                         runOnUiThread(new Runnable() {
                                             @Override
                                             public void run() {
                                                 updateDisplay();
                                             }
                                         });
                                     } else {
                                         alertUserAboutError();
                                     }
                                 } catch (IOException | JSONException e) {
                                     Log.e(TAG, getString(R.string.network_unawailable_message), e);
                                 }

                             }
                         }

            );
        }
        else {
            Toast.makeText(this, "Network is unawailable!", Toast.LENGTH_LONG).show();
        }
    }

    private void toggleRefresh() {
        if (mProgressBar.getVisibility() == View.INVISIBLE) {
            mProgressBar.setVisibility(View.VISIBLE);
            mRefershImageView.setVisibility(View.INVISIBLE);
        }
        else {
            mProgressBar.setVisibility(View.INVISIBLE);
            mRefershImageView.setVisibility(View.VISIBLE);
        }
    }

    private void updateDisplay() {
        Current current = mForecast.getCurrent();
        mTemperatureLabel.setText("" + current.getTemperature());
        mTimeLabel.setText("At " + current.getFormattedTime());
        mHumidityValue.setText("" + current.getHumidity());
        mPrecipValue.setText("" + current.getPercipChance() + "%");
        mSummaryLabel.setText(current.getSummary());
//        Log.d(TAG, "Image ID " + mCurrentWeather.getIconId());

        Drawable drawable = ContextCompat.getDrawable(this, current.getIconId());
        mIconImageView.setImageDrawable(drawable);
    }

    private Forecast parseForecastDetails(String jsonData) throws JSONException {
        Forecast forecast = new Forecast();

        forecast.setCurrent(getCurrentDetails(jsonData));
        forecast.setHourlyForecast(getHourlyForecast(jsonData));
        forecast.setDailyForecast(getDaylyForecast(jsonData));

        return forecast;
    }

    private Day[] getDaylyForecast(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        JSONObject hourly = forecast.getJSONObject("daily");
        JSONArray data = hourly.getJSONArray("data");

        Day[] days = new Day[data.length()];
        for (int i = 0; i<data.length(); i++) {
            JSONObject jsonDay = data.getJSONObject(i);
            Day day = new Day();

            day.setSummary(jsonDay.getString("summary"));
            day.setTemperatureMax(jsonDay.getDouble("temperatureMax"));
            day.setIcon(jsonDay.getString("icon"));
            day.setTime(jsonDay.getLong("time"));
            day.setTimezone(timezone);

            days[i] = day;
        }

        return days;
    }

    private Hour[] getHourlyForecast(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        JSONObject hourly = forecast.getJSONObject("hourly");
        JSONArray data = hourly.getJSONArray("data");

        Hour[] hours = new Hour[data.length()];
        for (int i = 0; i<data.length(); i++) {
            JSONObject jsonHour = data.getJSONObject(i);
            Hour hour = new Hour();

            hour.setSummary(jsonHour.getString("summary"));
            hour.setTemperature(jsonHour.getDouble("temperature"));
            hour.setIcon(jsonHour.getString("icon"));
            hour.setTime(jsonHour.getLong("time"));
            hour.setTimezone(timezone);

            hours[i] = hour;
        }

        return hours;
    }

    private Current getCurrentDetails(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        String timezone = forecast.getString("timezone");
        Log.i(TAG, "From JSON: " + timezone);

        JSONObject currently = forecast.getJSONObject("currently");

        Current current = new Current();
        current.setHumidity(currently.getDouble("humidity"));
        current.setTime(currently.getLong("time"));
        current.setIcon(currently.getString("icon"));
        current.setPercipChance(currently.getDouble("precipProbability"));
        current.setSummary(currently.getString("summary"));
        current.setTemperature((currently.getDouble("temperature") - 32.0) * 5.0 / 9.0);
        current.setTimeZone(timezone);

        Log.d(TAG, current.getFormattedTime());
        Log.d(TAG, "Temp: " + current.getTemperature());

        return current;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }
        return isAvailable;
    }

    private void alertUserAboutError() {
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getFragmentManager(), "error_dialog");
    }

    @Override
    public void handleNewLocation(Location location) {
        mLatitude = location.getLatitude();
        mLongitude = location.getLongitude();
    }

    @OnClick (R.id.dailyButton)
    public void startDailyActivity(View view) {
        Intent intent = new Intent(this,DailyForecastActivity.class);
        intent.putExtra(DAILY_FORECAST, mForecast.getDailyForecast());
        startActivity(intent);
    }

    @OnClick(R.id.hourlyButton)
    public void startHourlyActivity(View view) {
        Intent intent = new Intent(this,HourlyForecastActivity.class);
        intent.putExtra(HOURLY_FORECAST, mForecast.getHourlyForecast());
        startActivity(intent);
    }
}
