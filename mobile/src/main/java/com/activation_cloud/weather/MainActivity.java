package com.activation_cloud.weather;

import android.Manifest;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    EditText cityName;
    TextView resultWeather;
    ImageView iconWeather;
    Button search;


    boolean isConnection = true;

    DownloadTask jsonTask = null;
    DownloadIconWeather iconTask = null;

    String id = "981d90339ec2fb4304d8ef6a9959fdc0";
    String icon ="";

    public class DownloadTask extends AsyncTask<String, Void, String>
    {

        @Override
        protected String doInBackground(String... urls) {

            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection)url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);

                int data = reader.read();
                while(data != -1)
                {
                    char current = (char)data;
                    result += current;
                    data = reader.read();
                }
                return result;

            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.i("INFO","Bad url convertion");

            } catch (IOException e) {
                e.printStackTrace();
                Log.i("INFO","Fail open connection");
                isConnection = false;
            }

            return null;
        }

        @Override
        protected  void onPreExecute()
        {
            super.onPreExecute();
            iconWeather.setVisibility(View.INVISIBLE);
            resultWeather.setVisibility(View.INVISIBLE);

        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(isConnection) {
                initResultStringJson(result);
                resultWeather.setVisibility(View.VISIBLE);
                downloadIconWeather();
                Log.i("INFO", "onPostExecute TaskDownloader");
            }
            else
                Toast.makeText(getApplicationContext(),"Please, check your access to the Internet and restart application",Toast.LENGTH_LONG).show();
        }
    }

    protected class DownloadIconWeather extends AsyncTask<String,Integer,Bitmap>
    {
        @Override
        protected Bitmap doInBackground(String... urls) {

            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.connect();
                InputStream inp = connection.getInputStream();
                Bitmap bm = BitmapFactory.decodeStream(inp);
                return bm;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
                Log.i("INFO","Fail open connection");
                isConnection = false;
            }

            return null;
        }

        @Override
        protected  void onPreExecute()
        {
            super.onPreExecute();
            Log.i("INFO", "onPreExecute");
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {

        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            if(isConnection) {
                showIconWeather(result);
                iconWeather.setVisibility(View.VISIBLE);

                Log.i("INFO", "onPostExecute");
            }
            else
                Toast.makeText(getApplicationContext(),"Please, check your access to the Internet and restart application",Toast.LENGTH_LONG).show();
        }

    }

    private void searchWeather()
    {

        //cityName.clearFocus();
        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        InputMethodManager mnr = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        mnr.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);

        try {
            String encodedCityName = URLEncoder.encode(cityName.getText().toString(),"UTF-8");
            if(jsonTask == null)
                jsonTask = new DownloadTask();
            jsonTask.execute("http://api.openweathermap.org/data/2.5/weather?q="+ encodedCityName+"&units=metric&APPID="+id);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),"Could not find weather",Toast.LENGTH_LONG).show();
        }
    }

    private void showIconWeather(Bitmap result) {
        iconWeather.setImageBitmap(result);
    }

    private void downloadIconWeather() {
        if(iconTask == null)
            iconTask = new DownloadIconWeather(); //http://openweathermap.org/img/w/10d.png
        iconTask.execute("http://openweathermap.org/img/w/"+ icon+".png");
    }

    private void initResultStringJson(String result) {


        String msg = "";
        try {
            JSONObject jobj = new JSONObject(result);


            String name = jobj.getString("name");

            msg = "City:"+ name + ".\r\n";

            String weatherInfo = jobj.getString("weather");

            JSONArray arr = new JSONArray(weatherInfo);

            for(int i = 0; i < arr.length();i++)
            {
                JSONObject jsonPart = arr.getJSONObject(i);
                String main ="";
                String description = "";
                main = jsonPart.getString("main");
                description = jsonPart.getString("description");
                icon = jsonPart.getString("icon");

                if(!main.isEmpty() && !description.isEmpty())
                {
                    msg += (main + ":" + description + ".\r\n");
                }

            }

            JSONObject jsonPartTemp = jobj.getJSONObject("main");
            String temp = jsonPartTemp.getString("temp");
            String temp_max = jsonPartTemp.getString("temp_max");
            String temp_min = jsonPartTemp.getString("temp_min");

            //if(!temp.isEmpty() && !temp_max.isEmpty() && !temp_min.isEmpty())
            {
                msg += ("Temperature:" + temp + ", max:" + temp_max + ", min:" + temp_min + ".\r\n");
            }


            if(!msg.isEmpty())
            {
                resultWeather.setText(msg);
            }
            else
            {
                Toast.makeText(getApplicationContext(),"Could not find weather",Toast.LENGTH_LONG).show();
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        int RESULT = 105;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //requestPermissions(new String[]{Manifest.permission.INTERNET,Manifest.permission.ACCESS_NETWORK_STATE}, RESULT);
            ActivityCompat.requestPermissions (this,
                    new String [] {Manifest.permission.INTERNET,Manifest.permission.ACCESS_NETWORK_STATE},RESULT);
            //Toast.makeText(this,"Add permision",Toast.LENGTH_LONG).show();
        }

        cityName = (EditText)findViewById(R.id.editTextCity);
        resultWeather = (TextView)findViewById(R.id.textViewResult);
        iconWeather = (ImageView)findViewById(R.id.imageViewIcon);

        search = (Button)findViewById(R.id.buttonWeather);
        search.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {

        int id = view.getId();

        switch (id)
        {
            case R.id.buttonWeather:
                searchWeather();
                break;
        }
    }
}
