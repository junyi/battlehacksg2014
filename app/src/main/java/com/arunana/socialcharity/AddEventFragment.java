package com.arunana.socialcharity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.arunana.socialcharity.widgets.LinearListView;
import com.arunana.socialcharity.widgets.RobotoFloatLabelEditText;
import com.kbeanie.imagechooser.api.ChooserType;
import com.kbeanie.imagechooser.api.ChosenImage;
import com.kbeanie.imagechooser.api.ImageChooserListener;
import com.kbeanie.imagechooser.api.ImageChooserManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by dashsell on 13/9/14.
 */
public class AddEventFragment extends Fragment implements ImageChooserListener, SimpleDialogInterface {

    private Event event;

    private static final int DIALOG_ADD_ITEM = 1;

    @InjectView(R.id.demo)
    View demo;

    @InjectView(R.id.image_dummy)
    View imageDummy;

    @InjectView(R.id.event_image_button)
    View eventImageButton;

    @InjectView(R.id.event_name)
    RobotoFloatLabelEditText eventName;

    @InjectView(R.id.event_desc)
    RobotoFloatLabelEditText eventDesc;

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


    private ImageChooserManager imageChooserManager;
    private int chooserType;
    private String filePath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    public static AddEventFragment newInstance(Context context) {
        AddEventFragment fragment = new AddEventFragment();

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_event, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        itemListAdapter = new ItemListAdapter(getActivity());
        itemList.setAdapter(itemListAdapter);

        eventImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isEventImage = true;
                final String[] options = {"Take photo", "Choose image"};
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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

        addItemButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                filePath = "/storage/emulated/0/myfolder/1410657214934_fact_1.jpg";

                String eventNameStr = "Raising donation for cause";
                String eventDescStr = "Donation will go to needy students in poverty.";
                eventName.setText(eventNameStr);
                eventDesc.setText(eventDescStr);
                eventImage.setImageURI(Uri.parse(filePath));
                imageDummy.setVisibility(View.GONE);

                event = new Event(eventNameStr, eventDescStr, filePath);

                List<Item> items = new ArrayList<Item>();
                Item i1 = new Item("Old clothes", "6.00", null);
                Item i2 = new Item("Old books", "3.00", null);
                items.add(i1);
                items.add(i2);

                itemListAdapter.setItems(items);
                itemListAdapter.notifyDataSetChanged();

                return false;
            }
        });
    }

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


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK
                && (requestCode == ChooserType.REQUEST_PICK_PICTURE || requestCode == ChooserType.REQUEST_CAPTURE_PICTURE)) {
            if (imageChooserManager == null) {
                reinitializeImageChooser();
            }
            imageChooserManager.submit(requestCode, data);
        }
    }


    private AddItemDialog dialog;

    private void onAddItemClicked() {
        dialog = AddItemDialog.newInstance(getActivity(), DIALOG_ADD_ITEM);
        dialog.setTargetFragment(this, DIALOG_ADD_ITEM);
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
                    Toast.makeText(getActivity(), "Item added!", Toast.LENGTH_SHORT).show();

            }
    }

    @Override
    public void onImageChosen(final ChosenImage image) {
        if (image != null) {
            filePath = new File(image
                    .getFileThumbnail()).toString();
            Log.d("Image path", filePath);
            getActivity().runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (!isEventImage)
                        dialog.onImageChosen(image);
                    else {
                        eventImage.setImageURI(Uri.parse(filePath));
                        isEventImage = false;
                        imageDummy.setVisibility(View.GONE);
                    }
                }
            });
        }
    }


    @Override
    public void onError(String reason) {

    }

    // Should be called if for some reason the ImageChooserManager is null (Due
    // to destroying of activity for low memory situations)
    private void reinitializeImageChooser() {
        imageChooserManager = new ImageChooserManager(this, chooserType,
                "myfolder", true);
        imageChooserManager.setImageChooserListener(this);
        imageChooserManager.reinitialize(filePath);
    }

    public interface OnSubmittedListener {
        public void onSubmitted(Event event, List<Item> items);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.forward:
                onForwarded();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onForwarded() {
        String name = eventName.getTextString();
        String desc = eventDesc.getTextString();
        event = new Event(name, desc, filePath);
        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(desc) && !TextUtils.isEmpty(filePath))
            ((OnSubmittedListener) getActivity()).onSubmitted(event, itemListAdapter.getItems());
        else
            Toast.makeText(getActivity(), "You are missing some info in the form!", Toast.LENGTH_SHORT).show();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onResume() {
        super.onResume();

        getActivity().getActionBar().setHomeAsUpIndicator(android.R.color.transparent);

        if ((event = ((EventActivity) getActivity()).getEvent()) != null) {
            eventName.setText(event.getName());
            eventDesc.setText(event.getDescription());
            eventImage.setImageURI(Uri.parse(event.getImagePath()));
            imageDummy.setVisibility(View.GONE);
        }

        List<Item> itemList;

        if ((itemList = ((EventActivity) getActivity()).getItems()) != null) {
            itemListAdapter.addItems(itemList);
        }
    }
}
