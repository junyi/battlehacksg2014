package com.arunana.socialcharity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.kbeanie.imagechooser.api.ChosenImage;
import com.kbeanie.imagechooser.api.ImageChooserListener;

import java.io.File;

/**
 * Created by dashsell on 13/9/14.
 */
public class AddItemDialog extends SimpleDialogFragment implements PopupWindow.OnDismissListener, ImageChooserListener {
    public AddItemDialog() {

    }

    public static final String KEY_ITEM_NAME = "item_name";
    public static final String KEY_ITEM_PRICE = "item_price";
    public static final String KEY_ITEM_IMAGE_PATH = "item_image_path";

    private EditText name;
    private EditText price;
    private View imageButton;
    private ImageView itemImage;
    private View imageDummy;

    public static AddItemDialog newInstance(Context context, int requestCode) {
        AddItemDialog f = new AddItemDialog();

        Bundle bundle = new Bundle();
        bundle.putInt(KEY_REQUEST_CODE, requestCode);
        bundle.putString(KEY_TITLE, context.getString(R.string.dialog_add_item_title));
//        bundle.putString(KEY_MESSAGE, context.getString(R.string.dialog_add_item_message));
        bundle.putInt(KEY_LAYOUT_RES_ID, R.layout.dialog_add_item);
        bundle.putInt(KEY_POSITIVE_RES_ID, android.R.string.ok);
        bundle.putInt(KEY_NEGATIVE_RES_ID, android.R.string.cancel);
        bundle.putInt(KEY_DISPLAY_OPTIONS, FLAG_DISPLAY_POSITIVE | FLAG_DISPLAY_NEGATIVE);
        f.setArguments(bundle);

        return f;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        name = (EditText) view.findViewById(R.id.item_name);
        price = (EditText) view.findViewById(R.id.item_price);
        imageButton = view.findViewById(R.id.add_item_image_button);
        itemImage = (ImageView) view.findViewById(R.id.item_image);
        imageDummy = view.findViewById(R.id.image_dummy);

        final String[] options = {"Take photo", "Choose image"};
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0:
                                ((EventActivity) getActivity()).takePicture();
                                break;
                            default:
                                ((EventActivity) getActivity()).chooseImage();
                        }
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    @Override
    protected void setResult(int resultCode) {
        this.bundle = new Bundle();
        String itemName, itemPrice;
        if (!TextUtils.isEmpty(itemName = name.getText().toString()))
            this.bundle.putString(KEY_ITEM_NAME, itemName);
        if (!TextUtils.isEmpty(itemPrice = price.getText().toString()))
            this.bundle.putString(KEY_ITEM_PRICE, itemPrice);
        if (!TextUtils.isEmpty(filePath))
            this.bundle.putString(KEY_ITEM_IMAGE_PATH, filePath);

        super.setResult(resultCode);

    }


    @Override
    public void onDismiss() {

    }


    private String filePath;

    @Override
    public void onImageChosen(final ChosenImage image) {

        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
//                pbar.setVisibility(View.GONE);
                if (image != null) {
                    Log.d("AddItemDialog", "Image received!");
//                    textViewFile.setText(image.getFilePathOriginal());
//                    imageViewThumbnail.setImageURI(Uri.parse(new File(image
//                            .getFileThumbnail()).toString()));
                    itemImage.setImageURI(Uri.parse(filePath = new File(image
                            .getFileThumbnail()).toString()));
                    imageDummy.setVisibility(View.GONE);
                }
            }
        });
    }


    @Override
    public void onError(final String reason) {
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
//                pbar.setVisibility(View.GONE);
                Toast.makeText(getActivity(), reason,
                        Toast.LENGTH_LONG).show();
            }
        });
    }


}
