package com.arunana.socialcharity;

import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.location.DetectedActivity;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.io.ByteArrayOutputStream;
import java.util.List;

import butterknife.ButterKnife;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.SmartLocationOptions;
import io.nlopez.smartlocation.UpdateStrategy;

/**
 * Created by dashsell on 13/9/14.
 */
public class EventActivity extends ActionBarActivity implements AddEventFragment.OnSubmittedListener {

    private Event event;
    private List<Item> items;
    private Location location;
    private Menu menu;

    public Event getEvent() {
        return event;
    }

    public List<Item> getItems() {
        return items;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_event);
        // create our manager instance after the content view is set
        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        // enable navigation bar tint
        tintManager.setStatusBarTintEnabled(true);

        tintManager.setStatusBarTintResource(R.color.action_bar_background_color_dark);

        overridePendingTransition(R.anim.activity_transition_slide_in_new,
                R.anim.activity_transition_fade_out_prev);
        ButterKnife.inject(this);


        AddEventFragment fragment = AddEventFragment.newInstance(this);
        getFragmentManager().beginTransaction().replace(R.id.main_content, fragment, "addevent").commit();
    }

    public void takePicture() {
        AddEventFragment fragment = (AddEventFragment) getFragmentManager().findFragmentByTag("addevent");
        if (fragment != null) {
            fragment.takePicture();
        }
    }

    public void chooseImage() {
        AddEventFragment fragment = (AddEventFragment) getFragmentManager().findFragmentByTag("addevent");
        if (fragment != null) {
            fragment.chooseImage();
        }
    }

    @Override
    public void onSubmitted(Event event, List<Item> items) {
        this.event = event;
        this.items = items;

        menu.findItem(R.id.forward).setVisible(false);
        PublishFragment fragment = PublishFragment.newInstance(location);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
        ft.replace(R.id.main_content, fragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    public void publish() {
        new PublishTask().execute();
    }


    private boolean shouldShowRightIcon = true;

    public void hideRightIcon() {
        shouldShowRightIcon = false;
        invalidateOptionsMenu();

    }

    public void showRightIcon() {
        shouldShowRightIcon = true;
        invalidateOptionsMenu();
    }

    class PublishTask extends AsyncTask<Void, Void, Void> {
        private String eventName;

        @Override
        protected Void doInBackground(Void... events) {
            String name = event.getName();
            eventName = name;
            String desc = event.getDescription();
            ParseObject event = new ParseObject("CharityEvent");
            ParseUser currentUser = ParseUser.getCurrentUser();
            if (currentUser != null)
                event.put("user", ParseObject.createWithoutData("_User", currentUser.getObjectId()));
            event.put("name", name);
            event.put("desc", desc);

            ParseGeoPoint point = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
            Log.d("Location", point.getLatitude() + ", " + point.getLongitude());
            event.put("location", point);
            if (EventActivity.this.event.getImagePath() != null) {
                Bitmap bitmap = BitmapFactory.decodeFile(EventActivity.this.event.getImagePath());
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                // Compress image to lower quality scale 1 - 100
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
                byte[] image = stream.toByteArray();
                ParseFile file = new ParseFile(name + ".png", image);
                try {
                    file.save();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                event.put("eventImage", file);
            }

            try {
                event.save();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            String eventId = event.getObjectId();

            int count = items.size();
            for (int i = 0; i < count; i++) {
                Item item = items.get(i);
                ParseObject uploadedItem = new ParseObject("CharityItem");
                uploadedItem.put("name", item.getName());
                uploadedItem.put("price", Double.parseDouble(item.getPrice()));
                uploadedItem.put("is_donated", false);
                if (item.getImagePath() != null) {
                    Bitmap bitmap = BitmapFactory.decodeFile(item.getImagePath());
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    // Compress image to lower quality scale 1 - 100
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
                    byte[] image = stream.toByteArray();
                    ParseFile file = new ParseFile(item.getName() + ".png", image);
                    try {
                        file.save();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    uploadedItem.put("photo", file);
                }
                uploadedItem.put("charityEvent", ParseObject.createWithoutData("CharityEvent", eventId));

                try {
                    uploadedItem.save();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(EventActivity.this, "Upload finished!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });

            // Send notifications to whoever is not charity
//            ParseQuery userQuery = ParseUser.getQuery();
//            userQuery.whereNotEqualTo("isCharity", true);

// Find devices associated with these users
            ParseQuery pushQuery = ParseInstallation.getQuery();
//            pushQuery.whereMatchesQuery("user", userQuery);

// Send push notification to query
            ParsePush push = new ParsePush();
            push.setQuery(pushQuery); // Set our Installation query
            push.setMessage("New charity event: " + eventName + "!");
            push.sendInBackground();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        SmartLocationOptions options = new SmartLocationOptions();
        options.setDefaultUpdateStrategy(UpdateStrategy.BEST_EFFORT);

        SmartLocation.getInstance().start(
                this,
                options,
                new SmartLocation.OnLocationUpdatedListener() {
                    @Override
                    public void onLocationUpdated(Location location, DetectedActivity detectedActivity) {
                        // In here you have the location and the activity. Do whatever you want with them!
                        EventActivity.this.location = location;
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        SmartLocation.getInstance().cleanup(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        SmartLocation.getInstance().stop(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.event, menu);
        if (shouldShowRightIcon)
            menu.findItem(R.id.forward).setVisible(true);
        else
            menu.findItem(R.id.forward).setVisible(false);

        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // Catch back action and pops from backstack
        // (if you called previously to addToBackStack() in your transaction)
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
            showRightIcon();
        }
        // Default action on back pressed
        else
            super.onBackPressed();
    }
}
