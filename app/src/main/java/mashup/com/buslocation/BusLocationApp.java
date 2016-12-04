package mashup.com.buslocation;

import android.app.Application;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import io.fabric.sdk.android.Fabric;

public class BusLocationApp extends Application {

    private String TWITTER_KEY = "nkPiCTOziku9dZH5fmsdBvINC";
    private String TWITTER_SECRET = "4gyCi2XkK4F30NPH8bSFYu39atL53lgFDkysFlX9MPRmaGm2nc";

    @Override
    public void onCreate() {
        super.onCreate();
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
    }
}