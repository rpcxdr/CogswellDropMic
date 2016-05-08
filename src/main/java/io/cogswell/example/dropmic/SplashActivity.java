package io.cogswell.example.dropmic;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.cogswell.dropmic.R;

public class SplashActivity extends Activity {

    private Activity activity;

    /** Duration of wait **/
    private final int SPLASH_DISPLAY_LENGTH = 2500;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.splash_screen);

        activity = this;

        /* New Handler to start the Menu-Activity
         * and close this Splash-Screen after some seconds.*/

        Uri data = getIntent().getData();
        if (data != null) {
            String scheme = data.getScheme();
            String host = data.getHost();
            String params = data.getQuery();

            Map<String, String> map = getQueryMap(params);
            Set<String> keys = map.keySet();

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);

            for (String key : keys)
            {
                //Log.d("key", key);
                //Log.d("value", map.get(key));
                String value = map.get(key);
                if (key.equals("access_key")){
                    sharedPreferences.edit().putString("accessKey", value).apply();
                } else if (key.equals("namespace")) {
                    sharedPreferences.edit().putString("namespaceName", value).apply();
                } else if (key.equals("event")) {
                    sharedPreferences.edit().putString("eventName", value).apply();
                } else if (key.equals("platform")) {
                    sharedPreferences.edit().putString("platform", value).apply();
                //Environment is currently hard coded to "dev".  When we add UI control, uncomment this.
                //} else if (key.equals("enviornment")) {
                //    sharedPreferences.edit().putString("enviornment", value).apply();
                } else if (key.equals("application_id")) {
                    sharedPreferences.edit().putString("platform_app_id", value).apply();
                } else if (key.equals("campaign_id")) {
                    sharedPreferences.edit().putString("campaign_id", value).apply();
                } else if (key.equals("client_salt")) {
                    sharedPreferences.edit().putString("clientSalt", value).apply();
                } else if (key.equals("client_secret")) {
                    sharedPreferences.edit().putString("clientSecret", value).apply();
                } else if (key.equals("debug_directive")) {
                    sharedPreferences.edit().putString("accessKey", value).apply();
                } else if (key.equals("udid")) {
                    sharedPreferences.edit().putString("UDID", value).apply();
                }
            }
        }
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                Intent mainIntent = new Intent(SplashActivity.this,StartActivity.class);
                SplashActivity.this.startActivity(mainIntent);
                SplashActivity.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }

    public static Map<String, String> getQueryMap(String query)
    {
        String[] params = query.split("&");
        Map<String, String> map = new HashMap<String, String>();
        for (String param : params)
        {
            String name = param.split("=")[0];
            String value = param.split("=")[1];
            map.put(name, value);
        }
        return map;
    }
}
