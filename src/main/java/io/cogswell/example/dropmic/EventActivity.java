package io.cogswell.example.dropmic;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import io.cogswell.dropmic.R;
import io.cogswell.dropmic.notifications.QuickstartPreferences;
import io.cogswell.dropmic.notifications.RegistrationIntentService;
import io.cogswell.sdk.GambitSDKService;
import io.cogswell.sdk.message.GambitRequestMessage;
import io.cogswell.sdk.message.GambitResponseMessage;
import io.cogswell.sdk.push.GambitRequestPush;
import io.cogswell.sdk.push.GambitResponsePush;
import io.cogswell.sdk.request.GambitRequestEvent;
import io.cogswell.sdk.response.GambitResponseEvent;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.cogswell.dropmic.table.GambitAttribute;

public class EventActivity extends AppCompatActivity  {
    private String accessKey;
    private String namespaceName;
    private String attributesJSONAsString;
    private String platform;
    private String enviornment;
    private String platform_app_id;
    private int campaign_id = 1;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private String debug_directive = "";
    private String message = "";
    public EditText editTextEventName;
    private String clientSalt = null;
    private String clientSecret = null;

    private String randomUUID = null;
    private String randomUUIDBody = null;
    private ArrayList<GambitAttribute> namespaceAttributs = null;
    private String namespaceBody = null;
    private String eventBody = null;
    private String receivedMessage = null;
    private boolean pushServiceStarted = false;
    MediaPlayer sounds[];

    private Button buttonRegisterPush;
    private Button buttonUnregisterPush;
    private Button buttonSaveEventName;

    private Sensor mAccelerometer ;
    private SensorManager mSensorManager;
    private SensorEventListener mSensorEventListener;
    double magLowPass=0;
    double magAveNoise=0;
    double magAlpha = 0.01;
    static long timestampDebounce =0;
    long timestampDebounceTimeNs = 1000000000;
    //long timestampDebounceTimeNs = 100000000;

    private String UDID;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    protected final ExecutorService executor = Executors.newCachedThreadPool();
    private String android_id;


    private class message extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                // This will throw an exception if the json is invalid.
                JSONObject attributes = new JSONObject(attributesJSONAsString);

                GambitRequestMessage.Builder builder = new GambitRequestMessage.Builder(
                        accessKey, clientSalt, clientSecret
                ).setUDID(receivedMessage)
                        .setAttributes(attributes)
                        .setNamespace(namespaceName);
                Log.d("accessKey", accessKey);
                Log.d("clientSalt", clientSalt);
                Log.d("clientSecret", clientSecret);
                Log.d("receivedMessage", receivedMessage);
                Log.d("attributesJSONAsString", attributesJSONAsString.toString());
                Log.d("namespaceName", namespaceName);
                Future<io.cogswell.sdk.GambitResponse> future = null;
                try {
                    future = executor.submit(builder.build());
                } catch (Exception e) {
                    e.printStackTrace();
                    Utils.alert(activity, "Error building message request", null, e, null);
                }

                Log.d("future", String.valueOf(future));
                GambitResponseMessage response;
                try {
                    response = (GambitResponseMessage) future.get();
                    Log.d("response message", String.valueOf(response.getRawBody()));
                    final String responseMessage = response.getRawBody();
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            if (!responseMessage.equals("") && responseMessage != null && !responseMessage.isEmpty()) {
                                String prettyResponseMessage = responseMessage;
                                String eventNameIncoming = "Someone";
                                try {
                                    // Attempt to pretty-print the JSON.
                                    JSONObject responseJSON = new JSONObject(responseMessage);
                                    String message = responseJSON.getString("message");
                                    JSONObject responseMessageJSON = new JSONObject(message);
                                    eventNameIncoming = responseMessageJSON.getString("event_name");
                                    prettyResponseMessage = Utils.unescapeJavaString(responseMessageJSON.toString(2));
                                } catch (JSONException je) {
                                    Log.w("response message", "Invalid JSON: "+responseMessage, je);
                                    // If we can't parse the json, default to just showing the response text.
                                }
                                Utils.alert(activity, eventNameIncoming + " dropped the mic!", "", new Runnable() {
                                    @Override
                                    public void run() {
                                        int soundIndex = new Random().nextInt(sounds.length);
                                        if (!sounds[soundIndex].isPlaying()) {
                                            sounds[soundIndex].start();
                                        }
                                    }
                                });
                            }
                        }
                    });

                    Log.d("response message", String.valueOf(response.getRawBody()));

                } catch (Exception ex) {
                    Log.d("extest", ex.getLocalizedMessage());
                    ex.printStackTrace();

                    //\ failed to connect to api.cogswell.io/63.231.127.225 (port 443) after 10000ms
                    //Utils.alert(activity, "Error getting message details", null, ex, null);
                }
            } catch (Exception e) {
                e.printStackTrace();
                final String messageFinal;
                if (e instanceof JSONException) {
                    messageFinal = "The JSON syntax for Attributes as JSON is invalid.";
                } else {
                    messageFinal = "Please confirm your keys, ids, and namespace are correct.";
                }

                Utils.alert(activity, "Invalid data used to query for message details", messageFinal,null);
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {

            //Log.d("executed", "executed");
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    private class event extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            try {
                // This will throw an exception if the json is invalid.
                String eventName = EventActivity.this.editTextEventName.getText().toString();
                if (eventName==null || eventName.length()==0) eventName="Someone";
                EventActivity.this.saveFields();
                JSONObject attributes = new JSONObject(attributesJSONAsString);

                GambitRequestEvent.Builder builder = new GambitRequestEvent.Builder(accessKey, clientSalt, clientSecret);
                builder.setEventName(eventName);
                builder.setNamespace(namespaceName);
                builder.setAttributes(attributes);
                builder.setCampaignId(campaign_id);
                builder.setForwardAsMessage(true);

                String timestamp = null;

                TimeZone tz = TimeZone.getTimeZone("UTC");
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
                df.setTimeZone(tz);
                timestamp = df.format(new Date());


                builder.setTimestamp(timestamp);

                builder.setForwardAsMessage(true);

                builder.setDebugDirective(debug_directive);


                Future<io.cogswell.sdk.GambitResponse> future = null;
                try {
                    future = GambitSDKService.getInstance().sendGambitEvent(builder);
                } catch (Exception e) {
                    e.printStackTrace();
                    Utils.alert(activity, "Error sending event", null, e, null);
                    return null;
                }

                GambitResponseEvent response;
                try {
                    response = (GambitResponseEvent) future.get();

                    eventBody = response.getRawBody();

                    //Log.d("eventBody", eventBody);
                    message = response.getMessage();
                    //Log.d("response", response.getMessage());

                } catch (Exception ex) {
                    ex.printStackTrace();
                    Utils.alert(activity, "Error getting event response", null, ex, null);
                }
            } catch (Exception e) {
                e.printStackTrace();
                final String messageFinal;
                if (e instanceof JSONException) {
                    messageFinal = "The JSON syntax for Attributes as JSON is invalid.";
                } else {
                    messageFinal = "Please confirm your keys, ids, and namespace are correct.";
                }
                Utils.alert(activity, "Invalid event data", messageFinal, null);
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d("EventActivity", "message: "+message);
            Log.d("EventActivity", "result: " + result);
            //textViewMessageDescription.setText(message);
            //Log.d("executed", "executed");
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    private Activity activity;


    public void saveFields() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        sharedPreferences.edit().putString("eventName", editTextEventName.getText().toString()).apply();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        activity = this;

        initPushNotifications();
        //if (isSubscribed()) {
        //    unregisterSavedSubscription(null);
        //}
        sounds = new MediaPlayer[]{
                MediaPlayer.create(this, R.raw.homer_beginning),
                MediaPlayer.create(this, R.raw.homer_oopsy),
                MediaPlayer.create(this, R.raw.homer_outta_here),
                MediaPlayer.create(this, R.raw.homer_woohoo),
        };

        ImageView cogswellLogo = (ImageView) findViewById(R.id.cogswellLogo);
        buttonRegisterPush = (Button) findViewById(R.id.buttonRegisterPush);
        buttonUnregisterPush = (Button) findViewById(R.id.buttonUnregisterPush);
        buttonSaveEventName = (Button) findViewById(R.id.buttonSaveEventName);

        cogswellLogo.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://cogswell.io"));
                startActivity(browserIntent);
                return true;
            }
        });

        buttonUnregisterPush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Handler handlerTimer = new Handler();
                handlerTimer.postDelayed(new Runnable() {
                    public void run() {
                        if (isSubscribed()) {
                            unregisterSavedSubscription(null);
                        } else {
                            Utils.alert(EventActivity.this, "You are already unsubscribed", "", null);
                        }
                    }
                }, 500);
            }
        });
        buttonRegisterPush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                saveFields();
                if (isSubscribed()) {
                    Utils.alert(EventActivity.this,"You are already subscribed","",null);
                    //unregisterSavedSubscription(new Runnable() {
                    //    public void run() {
                    //        new registerPushService().execute("");
                    //    }
                    //});
                } else {
                    new registerPushService().execute("");
                }
            }
        });

        buttonSaveEventName.setOnClickListener(
                new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {
                       EventActivity.this.saveFields();

                }
            }
        );

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                String token = sharedPreferences.getString("token", "");
                if (sentToken) {
                    UDID = token;
                    Log.d("UDID", UDID);
                    Log.d("variant1", "variant1");
                } else {
                    Log.d("variant2", "variant2");
                }
            }
        };
        android_id = Settings.Secure.getString(this.getContentResolver(),Settings.Secure.ANDROID_ID);

        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //            setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayShowTitleEnabled(false);



        editTextEventName = (EditText) findViewById(R.id.editTextEventName);
        RelativeLayout toolbar_start = (RelativeLayout) findViewById(R.id.toolbar_start);
        //new CleintSecretCall().execute("");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        String eventName = sharedPreferences.getString("eventName", null);
        if (eventName==null) {
            eventName = "John Doe";
        }
        editTextEventName.setText(eventName);
        //debug_directive = "echo-as-message";
        accessKey = "a66ec003338b6ef20d2bab20d79e11ae";
        clientSalt = "308e2c9758e098521037e0d286cb153a08ac881f475464255bde8df9000ee9a2";
        clientSecret = "c47e882b43d6e7982ae43224fe6be90d65f2e94e2f9efaa1b384cd81a1d4c262";

        attributesJSONAsString = "{\"name\":\""+android_id+"\",\"delta_a\":123}";
        Log.d("EventActivity","attributesJSONAsString:"+attributesJSONAsString);
        namespaceName = "dropmic";
        platform_app_id = "gambit.gambit";
        enviornment = "dev";
        platform = "android";

        Intent intent = getIntent();
        String message_received = intent.getStringExtra("message_received");
        String message_received_id = intent.getStringExtra("message_received_id");
        if (message_received != null) {

            receivedMessage = message_received_id;
            // Get the message details.
            new message().execute("");

        }

        RelativeLayout toolbar_execute = (RelativeLayout) findViewById(R.id.toolbar_execute);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.debug_strings, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
/*
        toolbar_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fillData();

                Intent mainIntent = new Intent(EventActivity.this, StartActivity.class);
                EventActivity.this.startActivity(mainIntent);
                EventActivity.this.finish();
            }
        });
        toolbar_execute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            saveFields();

            new event().execute("");
            }
        });
*/

        mSensorManager = (SensorManager) getSystemService(this.SENSOR_SERVICE);
        mAccelerometer  = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        //SensorEvent.values[0]
        if (mSensorEventListener==null) {
            mSensorEventListener = new SensorEventListener() {

                @Override
                public void onSensorChanged(SensorEvent sensorEvent) {

                    double mag = Math.sqrt(
                            sensorEvent.values[0] * sensorEvent.values[0] +
                                    sensorEvent.values[1] * sensorEvent.values[1] +
                                    sensorEvent.values[2] * sensorEvent.values[2]);
                    // TODO: Consider: sensorEvent.timestamp Weight the low pass filter by time?
                    double magRelative = mag - magLowPass;
                    magLowPass = magLowPass + magAlpha * magRelative;
                    magAveNoise = magAveNoise + magAlpha * (Math.abs(magRelative) - magAveNoise);
                    double magThreshold = magAveNoise * 4 + 5;
                    //long timestamp = sensorEvent.timestamp;
                    long timestamp = System.currentTimeMillis();

                    synchronized (EventActivity.class) {
                        if (magRelative > magThreshold && timestamp > timestampDebounce) {
                            long timestampDebounceTimeMs = 600;
                            timestampDebounce = timestamp + timestampDebounceTimeMs;
                            //timestampDebounce = timestamp + timestampDebounceTimeNs;
                            int magRelativeInt = (int) magRelative;
                            TextView deltaAccelerationFromLastEvent = (TextView) EventActivity.this.findViewById(R.id.deltaAccelerationFromLastEvent);
                            deltaAccelerationFromLastEvent.setText("Drop the Mic!   Last Impact Rating: "+magRelativeInt);
                            attributesJSONAsString = "{\"name\":\"" + EventActivity.this.android_id + "\",\"delta_a\":" + magRelativeInt + "}";
                            Log.d("Accelerometer", "Time:" + timestamp + " Hit:" + magRelative + " Event: " + attributesJSONAsString);
                            new event().execute("");
                        }
                    }
               }

                @Override
                public void onAccuracyChanged(Sensor sensor, int i) {
                }
            };

            mSensorManager.registerListener(mSensorEventListener, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        }
/*
http://developer.android.com/guide/topics/sensors/sensors_overview.html

SENSOR_DELAY_FASTEST 0 microsecond
SENSOR_DELAY_GAME 20,000 microsecond
SENSOR_DELAY_UI 60,000 microsecond
SENSOR_DELAY_NORMAL 200,000 microseconds(200 milliseconds)
 */


    }
    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch(NumberFormatException e) {
            return false;
        } catch(NullPointerException e) {
            return false;
        }
        // only got here if we didn't return false
        return true;
    }

    @Override
    protected void onPause() {
        saveFields();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    public void unregisterSavedSubscription(Runnable handleUnregisterComplete) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        String accessKey = sharedPreferences.getString("subscribed.accessKey", null);
        String clientSalt = sharedPreferences.getString("subscribed.clientSalt", null);
        String clientSecret = sharedPreferences.getString("subscribed.clientSecret", null);
        String platform_app_id = sharedPreferences.getString("subscribed.platform_app_id", null);
        String namespaceName = sharedPreferences.getString("subscribed.namespaceName", null);
        String platform = sharedPreferences.getString("subscribed.platform", null);
        String UDID = sharedPreferences.getString("subscribed.UDID", null);
        String attributesJSONAsString = sharedPreferences.getString("subscribed.attributes", null);
        String enviornment = "dev";
        try {
            // This will throw an exception if the json is invalid.
            JSONObject attributes = new JSONObject(attributesJSONAsString);

            GambitRequestPush.Builder builder = new GambitRequestPush.Builder(
                    accessKey, clientSalt, clientSecret
            ).setNamespace(namespaceName)
                    .setAttributes(attributes)
                    .setUDID(UDID)
                    .setEnviornment(enviornment)
                    .setPlatform(platform)
                    .setPlatformAppID(platform_app_id)
                    .setMethodName(GambitRequestPush.unregister);

            new unRegisterPushService(builder, handleUnregisterComplete).execute("");
        } catch (JSONException e) {
            Utils.alert(activity, "Invalid un-subscription data:", "The JSON syntax for Attributes as JSON is invalid.", null);
        }
    }


    private class unRegisterPushService extends AsyncTask<String, Void, String> {
        GambitRequestPush.Builder builder;
        Runnable handleUnregisterComplete;
        public unRegisterPushService(GambitRequestPush.Builder builder, Runnable handleUnregisterComplete) {
            this.builder = builder;
            this.handleUnregisterComplete = handleUnregisterComplete;
        }

        @Override
        protected String doInBackground(String... params) {

            Future<io.cogswell.sdk.GambitResponse> future = null;
            try {

                future = executor.submit(builder.build());

                //Log.d("future", String.valueOf(future));
                GambitResponsePush response;
                try {
                    response = (GambitResponsePush) future.get();

                    Log.d("response 2", String.valueOf(response.getMessage()));
                    String message = response.getRawBody();
                    Log.d("response", String.valueOf(response.getRawBody()));

                    String alertTitle;
                    if (response.getRawCode()==200) {
                        alertTitle = "Successfully un-subscribed";
                    } else {
                        alertTitle = "Un-subscription request failed with code "+response.getRawCode();
                    }
                    if (message==null || message.equals("") || message.isEmpty()) {
                        message = "The server did not send any further details.";
                    }
                    message="Attempting to un-subscribing from:\n"+builder.getAttributes().toString(2)+"\nResponse from server:\n"+message;
                    deleteFieldsForCurrentSubscription();
                    // If something is waiting for an unsubscribe, call it back.
                    // For example, a subscription request may want to wait for an un-subscribe first.
                    Utils.alert(activity, alertTitle, message, handleUnregisterComplete);

                } catch (InterruptedException | ExecutionException ex) {
                    ex.printStackTrace();
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    ex.printStackTrace(pw);
                    String stackTrace = sw.toString();
                    Utils.alert(activity, "Error executing the un-subscription request", ex.getMessage() + "\n" + stackTrace, null);
                }
            } catch (Exception e) {
                e.printStackTrace();
                final String messageFinal;
                if (e instanceof JSONException) {
                    messageFinal = "The JSON syntax for Attributes as JSON is invalid.";
                } else {
                    messageFinal = "Please confirm your keys, ids, and namespace are correct.";
                }
                Utils.alert(activity, "Invalid un-subscription data:", messageFinal, null);
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {

            Log.d("test2", "test2");
            //Log.d("executed", "executed");
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    private class registerPushService extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            Future<io.cogswell.sdk.GambitResponse> future = null;
            try {
                // This will throw an exception if the json is invalid.
                JSONObject attributes = new JSONObject(attributesJSONAsString);

                GambitRequestPush.Builder builder = new GambitRequestPush.Builder(
                        accessKey, clientSalt, clientSecret
                ).setNamespace(namespaceName)
                        .setAttributes(attributes)
                        .setUDID(UDID)
                        .setEnviornment(enviornment)
                        .setPlatform(platform)
                        .setPlatformAppID(platform_app_id)
                        .setMethodName(GambitRequestPush.register);

                future = executor.submit(builder.build());

                //Log.d("future", String.valueOf(future));
                GambitResponsePush response;
                try {
                    response = (GambitResponsePush) future.get();
                    String message = response.getRawBody();
                    Log.d("response", String.valueOf(response.getMessage()));
                    String alertTitle;
                    if (response.getRawCode()==200) {
                        saveFieldsAsCurrentSubscription();
                        alertTitle = "Successfully subscribed";
                    } else {
                        alertTitle = "Subscription request failed with code "+response.getRawCode();
                    }
                    if (message==null || message.equals("") || message.isEmpty()) {
                        message = "The server did not send any further details.";
                    }
                    message="Attempted to subscribe to:\n"+attributesJSONAsString+"\nResponse from server:\n"+message;
                    Utils.alert(activity, alertTitle, message, null);
                } catch (InterruptedException | ExecutionException ex) {
                    ex.printStackTrace();
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    ex.printStackTrace(pw);
                    String stackTrace = sw.toString();
                    Utils.alert(activity, "Error executing the subscription request", ex.getMessage() + "\n" + stackTrace, null);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Utils.alert(activity, "Invalid un-subscription data:", "Please confirm your keys, ids, and namespace are correct. Details: [" + e.getMessage() + "]", null);
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d("onPostExecute", "result");
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    public void saveFieldsAsCurrentSubscription() {
        /*
        accessKey = editTextAccessKey.getText().toString();
        clientSalt = editTextClientSalt.getText().toString();
        clientSecret = editTextClientSecret.getText().toString();
        platform = "android";//editTextApplication.getText().toString();
        platform_app_id = editTextApplicationID.getText().toString();
        UDID = editTextUUID.getText().toString();
        namespaceName = editTextNamespace.getText().toString();
        attributesJSONAsString = editTextAttributes.getText().toString();*/

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        sharedPreferences.edit().putString("subscribed.accessKey", accessKey).apply();
        sharedPreferences.edit().putString("subscribed.clientSalt", clientSalt).apply();
        sharedPreferences.edit().putString("subscribed.clientSecret", clientSecret).apply();
        sharedPreferences.edit().putString("subscribed.platform", platform).apply();
        sharedPreferences.edit().putString("subscribed.platform_app_id", platform_app_id).apply();
        sharedPreferences.edit().putString("subscribed.UDID", UDID).apply();
        sharedPreferences.edit().putString("subscribed.namespaceName", namespaceName).apply();
        sharedPreferences.edit().putString("subscribed.attributes", attributesJSONAsString).apply();
    }

    public void deleteFieldsForCurrentSubscription() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        sharedPreferences.edit().remove("subscribed.accessKey").apply();
        sharedPreferences.edit().remove("subscribed.clientSalt").apply();
        sharedPreferences.edit().remove("subscribed.clientSecret").apply();
        sharedPreferences.edit().remove("subscribed.platform").apply();
        sharedPreferences.edit().remove("subscribed.platform_app_id").apply();
        sharedPreferences.edit().remove("subscribed.UDID").apply();
        sharedPreferences.edit().remove("subscribed.namespaceName").apply();
        sharedPreferences.edit().remove("subscribed.attributes").apply();
    }
    public boolean isSubscribed() {
        // If one "subscribed" value is saved, then they are all saved.
        // "subscribed" attributes are only saved after a successful subscription,
        // and deleted after un-subsciption.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        return sharedPreferences.getString("subscribed.accessKey", null) != null;
    }
    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
    }
    private void initPushNotifications() {
        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intentRegistration = new Intent(this, RegistrationIntentService.class);
            startService(intentRegistration);

        }
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                finish();
            }
            return false;
        }
        return true;
    }
}
