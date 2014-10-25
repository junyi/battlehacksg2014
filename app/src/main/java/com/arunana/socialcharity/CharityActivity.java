package com.arunana.socialcharity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.parse.ParseFacebookUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by dashsell on 13/9/14.
 */
public class CharityActivity extends Activity {

    @InjectView(R.id.start_event)
    View eventButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.activity_transition_slide_in_new,
                R.anim.activity_transition_fade_out_prev);

        setContentView(R.layout.activity_charity);

        ButterKnife.inject(this);
        // Fetch Facebook user info if the session is active
        Session session = ParseFacebookUtils.getSession();
        if (session != null && session.isOpened()) {
            makeMeRequest();
        }

        eventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onAddEventClicked();
            }
        });
    }


    private void makeMeRequest() {
        Request request = Request.newMeRequest(ParseFacebookUtils.getSession(),
                new Request.GraphUserCallback() {
                    @Override
                    public void onCompleted(GraphUser user, Response response) {
                        // handle response
                    }
                });
        request.executeAsync();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.charity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.add_event:
                onAddEventClicked();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onAddEventClicked() {
        Intent intent = new Intent(this, EventActivity.class);
        startActivity(intent);
    }
}
