package de.librechurch.synagocli;

import java.util.ArrayList;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import org.matrix.androidsdk.MXSession;
import de.librechurch.synagocli.Adapter.RoomListsAdapter;


//public class HomeActivity extends FragmentActivity {
public class HomeActivity extends AppCompatActivity {

    RoomListsAdapter mRoomListsAdapter;
    ViewPager mViewPager;
    private static final String LOG_TAG = HomeActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ArrayList test = new ArrayList<>();

        for (MXSession s : Matrix.getInstance(getApplicationContext()).getSessions()) {
            Log.d(LOG_TAG, "adding: " + s.getMyUserId());
            test.add(s.getMyUserId());
        }

        mRoomListsAdapter = new RoomListsAdapter(getSupportFragmentManager(), test);
        mViewPager = (ViewPager) findViewById(R.id.home_room_container);
        mViewPager.setAdapter(mRoomListsAdapter);


    }
}

