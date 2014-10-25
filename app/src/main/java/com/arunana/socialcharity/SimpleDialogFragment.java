package com.arunana.socialcharity;


import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.TextView;

public class SimpleDialogFragment extends DialogFragment implements View.OnClickListener {
    private static final String NAME = SimpleDialogFragment.class.getSimpleName();

    public static final int RESULT_OK = 1;
    public static final int RESULT_CANCELLED = 2;
    public static final int RESULT_DISMISSED = 4;

    public static final int FLAG_DISPLAY_POSITIVE = 1;
    public static final int FLAG_DISPLAY_NEGATIVE = 2;
    public static final int FLAG_DISPLAY_NEUTRAL = 4;


    protected static final String KEY_REQUEST_CODE = NAME + "_requestCode";
    protected static final String KEY_BUNDLE = NAME + "_bundle";
    protected static final String KEY_TITLE = NAME + "_title";
    protected static final String KEY_MESSAGE = NAME + "_message";
    protected static final String KEY_LAYOUT_RES_ID = NAME + "_layoutResourceId";
    protected static final String KEY_POSITIVE_RES_ID = NAME + "_positiveTextId";
    protected static final String KEY_NEGATIVE_RES_ID = NAME + "_negativeTextId";
    protected static final String KEY_NEUTRAL_RES_ID = NAME + "_neutralTextId";
    protected static final String KEY_DISPLAY_OPTIONS = NAME + "_displayOptions";


    private Button positiveButton;
    private Button negativeButton;
    private Button neutralButton;

    private int requestCode;
    protected Bundle bundle;

    public SimpleDialogFragment() {
        setStyle(STYLE_NO_TITLE, R.style.DialogTheme);
    }


    private void setup(Button button, int textId, boolean isVisible) {
        if (textId != 0) {
            button.setText(textId);
            button.setOnClickListener(this);
            button.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        }
    }

    private void setup(TextView textView, CharSequence charSequence) {
        if (TextUtils.isEmpty(charSequence)) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setText(charSequence);
            textView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle b = getArguments();
        requestCode = b.getInt(KEY_REQUEST_CODE);
        bundle = b.getBundle(KEY_BUNDLE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_fragment, container, false);

        if (getArguments() != null) {
            Bundle requestBundle = getArguments();
            int displayOptions = requestBundle.getInt(KEY_DISPLAY_OPTIONS, 0);
            setup((TextView) view.findViewById(R.id.title), requestBundle.getString(KEY_TITLE, null));
            setup((TextView) view.findViewById(R.id.message), requestBundle.getString(KEY_MESSAGE, null));

            int layoutResId = requestBundle.getInt(KEY_LAYOUT_RES_ID, 0);
            if (layoutResId != 0) {
                ViewStub viewStub = (ViewStub) view.findViewById(R.id.content_stub);
                viewStub.setLayoutResource(layoutResId);
                viewStub.inflate();
            }

            positiveButton = (Button) view.findViewById(R.id.positive);
            int positiveTextId = requestBundle.getInt(KEY_POSITIVE_RES_ID, 0);
            setup(positiveButton, positiveTextId, (displayOptions & FLAG_DISPLAY_POSITIVE) == FLAG_DISPLAY_POSITIVE);

            negativeButton = (Button) view.findViewById(R.id.negative);
            int negativeTextId = requestBundle.getInt(KEY_NEGATIVE_RES_ID, 0);
            setup(negativeButton, negativeTextId, (displayOptions & FLAG_DISPLAY_NEGATIVE) == FLAG_DISPLAY_NEGATIVE);

            neutralButton = (Button) view.findViewById(R.id.neutral);
            int neutralTextId = requestBundle.getInt(KEY_NEUTRAL_RES_ID, 0);
            setup(neutralButton, neutralTextId, (displayOptions & FLAG_DISPLAY_NEUTRAL) == FLAG_DISPLAY_NEUTRAL);

        }

        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.positive:
                handlePositive();
                break;
            case R.id.negative:
                handleNegative();
                break;
            default:
                handleDismiss();
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        dialog.dismiss();
    }

    protected void handlePositive() {
        setResult(RESULT_OK);
        dismiss();
    }

    protected void handleNegative() {
        setResult(RESULT_CANCELLED);
        dismiss();
    }

    protected void handleDismiss() {
        setResult(RESULT_DISMISSED);
        dismiss();
    }

    protected void setResult(int resultCode) {
        SimpleDialogInterface fragment;

        if (getTargetFragment() instanceof SimpleDialogInterface) {
            fragment = (SimpleDialogInterface) getTargetFragment();
        } else if (getActivity() instanceof SimpleDialogInterface) {
            fragment = (SimpleDialogInterface) getActivity();
        } else {
            return;
        }

        if (fragment != null) {
            Bundle resultBundle = new Bundle();
            if (this.bundle != null) {
                resultBundle.putAll(this.bundle);
            }
            fragment.onDialogResult(requestCode, resultCode, resultBundle);
        }

    }

    public void show(FragmentManager fm, String tag) {
        fm.beginTransaction().add(this, tag).commitAllowingStateLoss();
    }
}
