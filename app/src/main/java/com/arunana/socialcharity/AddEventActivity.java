package com.arunana.socialcharity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.arunana.socialcharity.widgets.FloatLabelEditText;
import com.arunana.socialcharity.widgets.LinearListView;
import com.google.android.gms.location.DetectedActivity;
import com.kbeanie.imagechooser.api.ChooserType;
import com.kbeanie.imagechooser.api.ChosenImage;
import com.kbeanie.imagechooser.api.ImageChooserListener;
import com.kbeanie.imagechooser.api.ImageChooserManager;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;
import java.io.File;

import butterknife.ButterKnife;
import butterknife.InjectView;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.SmartLocationOptions;
import io.nlopez.smartlocation.UpdateStrategy;

/**
 * Created by dashsell on 13/9/14.
 */
public class AddEventActivity extends Activity implements SimpleDialogInterface, ImageChooserListener {
    private static final int DIALOG_ADD_ITEM = 1;

    @InjectView(R.id.event_image_button)
    View eventImageButton;

    @InjectView(R.id.event_name)
    FloatLabelEditText eventName;

    @InjectView(R.id.event_desc)
    FloatLabelEditText eventDesc;

    @InjectView(R.id.event_image)
    ImageView eventImage;

    @InjectView(R.id.item_list)
    LinearListView itemList;

    @InjectView(R.id.add_item_button)
    Button addItemButton;

    @InjectView(R.id.content_frame)
    View contentFrame;

    @InjectView(R.id.upload_progress)
    View progressBar;

    private ItemListAdapter itemListAdapter;
    private boolean isEventImage = false;

    private Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        ButterKnife.inject(this);

        itemListAdapter = new ItemListAdapter(this);
        itemList.setAdapter(itemListAdapter);

        eventImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isEventImage = true;
                final String[] options = {"Take photo", "Choose image"};
                AlertDialog.Builder builder = new AlertDialog.Builder(AddEventActivity.this);
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0:
                                takePicture();
                                break;
                            case 1:
                            default:
                                chooseImage();
                        }
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        addItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onAddItemClicked();
            }
        });
    }

    private ImageChooserManager imageChooserManager;
    private int chooserType;
    private String filePath;

    public void chooseImage() {
        chooserType = ChooserType.REQUEST_PICK_PICTURE;
        imageChooserManager = new ImageChooserManager(this,
                chooserType, "myfolder", true);
        imageChooserManager.setImageChooserListener(this);
        try {
//            pbar.setVisibility(View.VISIBLE);
            filePath = imageChooserManager.choose();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void takePicture() {
        chooserType = ChooserType.REQUEST_CAPTURE_PICTURE;
        imageChooserManager = new ImageChooserManager(this,
                chooserType, "myfolder", true);
        imageChooserManager.setImageChooserListener(this);
        try {
//            pbar.setVisibility(View.VISIBLE);
            filePath = imageChooserManager.choose();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void onPublishEvent() {
        progressBar.setVisibility(View.VISIBLE);
        String name = eventName.getTextString();
        String desc = eventDesc.getTextString();

        new PublishTask().execute(new String[]{name, desc});
    }

    class PublishTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            String name = strings[0];
            String desc = strings[1];
            ParseObject event = new ParseObject("CharityEvent");
            ParseUser currentUser = ParseUser.getCurrentUser();
            if (currentUser != null)
                event.put("user", ParseObject.createWithoutData("_User", currentUser.getObjectId()));
            event.put("name", name);
            event.put("desc", desc);

            ParseGeoPoint point = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
            Log.d("Location", point.getLatitude() + ", " + point.getLongitude());
            event.put("location", point);
            if (filePath != null) {
                Bitmap bitmap = BitmapFactory.decodeFile(filePath);
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

            int count = itemListAdapter.getCount();
            for (int i = 0; i < count; i++) {
                Item item = (Item) itemListAdapter.getItem(i);
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
                    Toast.makeText(AddEventActivity.this, "Upload finished!", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    finish();
                }
            });

            // Send notifications to whoever is not charity
            ParseQuery userQuery = ParseUser.getQuery();
            userQuery.whereNotEqualTo("isCharity", true);

// Find devices associated with these users
            ParseQuery pushQuery = ParseInstallation.getQuery();
            pushQuery.whereMatchesQuery("user", userQuery);

// Send push notification to query
            ParsePush push = new ParsePush();
            push.setQuery(pushQuery); // Set our Installation query
            push.setMessage("Free hotdogs at the Parse concession stand!");
            push.sendInBackground();
        }
    }


    private AddItemDialog dialog;

    private void onAddItemClicked() {
        dialog = AddItemDialog.newInstance(this, DIALOG_ADD_ITEM);
        dialog.show(getFragmentManager(), null);

    }

    @Override
    public void onDialogResult(int requestCode, int resultCode, Bundle resultBundle) {
        if (resultCode == SimpleDialogFragment.RESULT_OK)
            switch (requestCode) {
                case DIALOG_ADD_ITEM:
                    String itemName = resultBundle.getString(AddItemDialog.KEY_ITEM_NAME);
                    String itemPrice = resultBundle.getString(AddItemDialog.KEY_ITEM_PRICE);
                    String itemImagePath = resultBundle.getString(AddItemDialog.KEY_ITEM_IMAGE_PATH);
                    Item item = new Item(itemName, itemPrice, itemImagePath);
                    itemListAdapter.addItem(item);

                default:
                    Toast.makeText(this, "Item added!", Toast.LENGTH_SHORT).show();

            }
    }

    @Override
    public void onImageChosen(final ChosenImage image) {
        if (image != null) {
            filePath = new File(image
                    .getFileThumbnail()).toString();
            Log.d("Image path", filePath);
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (!isEventImage)
                        dialog.onImageChosen(image);
                    else {
                        eventImage.setImageURI(Uri.parse(filePath));
                        isEventImage = false;
                    }
                }
            });
        }
    }

    @Override
    public void onError(String reason) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK
                && (requestCode == ChooserType.REQUEST_PICK_PICTURE || requestCode == ChooserType.REQUEST_CAPTURE_PICTURE)) {
            if (imageChooserManager == null) {
                reinitializeImageChooser();
            }
            imageChooserManager.submit(requestCode, data);
        }
    }

    // Should be called if for some reason the ImageChooserManager is null (Due
    // to destroying of activity for low memory situations)
    private void reinitializeImageChooser() {
        imageChooserManager = new ImageChooserManager(this, chooserType,
                "myfolder", true);
        imageChooserManager.setImageChooserListener(this);
        imageChooserManager.reinitialize(filePath);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("chooser_type", chooserType);
        outState.putString("media_path", filePath);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("chooser_type")) {
                chooserType = savedInstanceState.getInt("chooser_type");
            }

            if (savedInstanceState.containsKey("media_path")) {
                filePath = savedInstanceState.getString("media_path");
            }
        }
        super.onRestoreInstanceState(savedInstanceState);
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
                        AddEventActivity.this.location = location;
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
}
