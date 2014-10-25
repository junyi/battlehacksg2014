package com.arunana.socialcharity;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.Icon;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.views.MapView;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by dashsell on 13/9/14.
 */
public class PublishFragment extends Fragment {

    private static final String ARG_LAT = "latitude";
    private static final String ARG_LNG = "longitude";
    @InjectView(R.id.mapview)
    MapView mapView;

    @InjectView(R.id.publish_button)
    View publishButton;

    @InjectView(R.id.progress)
    View progress;


    private double latitude, longitude;
    private LatLng latlng;

    public static PublishFragment newInstance(Location location) {
        PublishFragment fragment = new PublishFragment();
        Bundle bundle = new Bundle();
        if (location != null) {
            bundle.putDouble(ARG_LAT, location.getLatitude());
            bundle.putDouble(ARG_LNG, location.getLongitude());
        } else {
            bundle.putDouble(ARG_LAT, 1.2978217);
            bundle.putDouble(ARG_LNG, 103.8516989);
        }
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle b = getArguments();
        latitude = b.getDouble(ARG_LAT);
        longitude = b.getDouble(ARG_LNG);

        latlng = new LatLng(latitude, longitude, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_publish, container, false);
        ButterKnife.inject(this, view);

        return view;
    }

    private boolean isPublishing = false;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d("Map map map map", latlng.getLatitude() + ", " + latlng.getLongitude());
        mapView.setZoom(18);
        mapView.setCenter(latlng);
        Marker marker = new Marker("You", "Current location", latlng);
        marker.setIcon(new Icon(getResources().getDrawable(R.drawable.marker2)));
        mapView.addMarker(marker);

        publishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((EventActivity) getActivity()).publish();
                isPublishing = true;
                progress.setVisibility(View.VISIBLE);
                publishButton.setVisibility(View.GONE);
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onResume() {
        super.onResume();

        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        getActivity().getActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_dark);

        ((EventActivity) getActivity()).hideRightIcon();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
