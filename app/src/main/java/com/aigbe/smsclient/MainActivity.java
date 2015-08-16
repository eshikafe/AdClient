package com.aigbe.smsclient;

/**
 * Created by Austin Aigbe on 8/9/2015.
 */

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity { // lollipop compatible
    public static final String DEFAULT_IP = "127.0.0.1";
    public static final String DEFAULT_PORT = "8080";
    public static final String DEFAULT_LOCATION = "Lagos";
    private String ServerIP = DEFAULT_IP;
    private String ServerPort = DEFAULT_PORT;
    private String URL, imsi;
    private String location = DEFAULT_LOCATION;
    TextView info;
    private ProgressDialog pDialog;
    JSONParser jParser = new JSONParser();
    String m, advert ="";
    ImageButton dialButton;
    EditText shortCodeText;
    String msg = "Hello, your monthly credit limit is 30000.\nYou have N14778.56 available to use for the month. Thank you.\n";

    public static final String REGEX_IP_ADDRESS =
            "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
    //final AsyncTask<String, String, String> asyncAdvert = new GetAdvert();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        info = (TextView) findViewById(R.id.textView);
        TelephonyManager mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        imsi = mTelephonyMgr.getSubscriberId(); // imsi

        dialButton = (ImageButton) findViewById(R.id.dial);
        shortCodeText = (EditText) findViewById(R.id.editText);

        dialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (shortCodeText.getText().toString().equals("")) {
                    shortCodeText.setText("*111#");
                    return;
                }
                if (!shortCodeText.getText().toString().equals("*111#")) {
                    Toast.makeText(getApplicationContext(), "UNKNOWN APPLICATION. Dial *111#", Toast.LENGTH_SHORT).show();
                }else{
                    getSettings();
                    new GetAdvert().execute();
                }
            }
        });
        info.setText("Dial *111# to check your balance");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            Intent s = new Intent(this, Settings.class);
            startActivityForResult(s, 1);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void getSettings() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        ServerIP = sharedPrefs.getString("adServerIp", "NULL");
        if (sharedPrefs.getString("adServerPort", "NULL") != "NULL"){
            ServerPort = sharedPrefs.getString("adServerPort", "NULL");
        }
        if (sharedPrefs.getString("location", "NULL") != "NULL"){
            location = sharedPrefs.getString("location", "NULL");
        }
        URL = "http://" + ServerIP + ":" + ServerPort + "/get_add";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                getSettings();
                break;
        }
    }


    // Connect to the Ad Server and retrieve the advert
    class GetAdvert extends AsyncTask<String, String, String> {
        final AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        // Before starting background thread Show Progress Dialog
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Short code running");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        //get server response
        protected String doInBackground(String... args) {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("imsi", imsi));
            params.add(new BasicNameValuePair("location", location));
            // getting JSON string from URL
            //URL = "https://api.github.com/users/mralexgray/repo";
            JSONObject json = jParser.makeHttpRequest(URL, "GET", params);

            if (json == null) {
                advert = "";
                return null;
            }

            try {
                //m = json.getString("msisdn");
                //advert = json.getString("documentation_url");
                advert = json.getString("advert");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        // After completing background task Dismiss the progress dialog
        protected void onPostExecute(String file_url) {
            pDialog.dismiss();
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    shortCodeText.setText("");
                    alert.setTitle("");
                    alert.setMessage(msg + advert);
                    alert.setPositiveButton("OK", null);
                    alert.show();
                }
            });

        }

    }
}
