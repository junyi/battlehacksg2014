package com.arunana.socialcharity;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.PushService;

/**
 * Created by dashsell on 13/9/14.
 */
public class CharityApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Add your initialization code here
        Parse.initialize(this, "rYbeMeAOupkwdO3nwVhnEdS1rnCOLQJUevTpbK9P", "rtq8QWLNrGzkaoQKp9f7QK95lDXAx7NSKU2fpNR5");

        // Set your Facebook App Id in strings.xml
        ParseFacebookUtils.initialize(getString(R.string.fb_app_id));
        PushService.setDefaultPushCallback(this, LoginActivity.class);
    }
}
