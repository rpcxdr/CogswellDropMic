package io.cogswell.example.notifications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import io.cogswell.example.EventActivity;
import io.cogswell.example.PushActivity;
import io.cogswell.example.R;

public class GcmService extends GcmListenerService {

    public GcmService() {

    }
    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    @Override
    public void onMessageReceived(String from, Bundle data) {
Log.d("GcmService", "onMessageReceived");
        String message_received_id = data.getString("aviata_gambit_message_id");

        sendNotification(data.toString(), message_received_id);
    }

    /**
     * Called when message is deleted.
     */
    @Override
    public void onDeletedMessages() {
        sendNotification("Deleted messages on server", "");
        Log.d("GcmService", "onDeletedMessages");
    }

    /**
     * Called when message is sent.
     */
    @Override
    public void onMessageSent(String msgId) {
        Log.d("GcmService", "onMessageSent");
        sendNotification("Upstream message sent. Id=" + msgId, "");
    }

    /**
     * Called when there is an error.
     */
    @Override
    public void onSendError(String msgId, String error) {
        Log.d("GcmService", "onSendError");
        sendNotification("Upstream message send error. Id=" + msgId + ", error" + error, "");
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg, String messageId) {
        Log.d("GcmService", "sendNotification");
        Intent intent = new Intent(this, EventActivity.class);
        intent.putExtra("message_received", msg);
        intent.putExtra("message_received_id", messageId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager
                .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
/*
        int soundIndex = new Random().nextInt(3);
        if (soundIndex==1) {
            defaultSoundUri = Uri.parse("android.resource://io.cogswell.example/" + R.raw.homer_beginning);
        } else if (soundIndex==2) {
            defaultSoundUri = Uri.parse("android.resource://io.cogswell.example/" + R.raw.homer_oopsy);
        } else if (soundIndex==0) {
            defaultSoundUri = Uri.parse("android.resource://io.cogswell.example/" + R.raw.homer_outta_here);
        } else  {
            defaultSoundUri = Uri.parse("android.resource://io.cogswell.example/" + R.raw.homer_woohoo);
        }*/
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
                this).setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Someone Dropped The Mic").setContentText("Cogswell.io")
                .setAutoCancel(true).setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }
}
