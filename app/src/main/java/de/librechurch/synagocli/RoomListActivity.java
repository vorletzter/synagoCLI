package de.librechurch.synagocli;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.matrix.androidsdk.data.RoomState;
import org.matrix.androidsdk.data.RoomSummary;
import org.matrix.androidsdk.listeners.MXEventListener;
import org.matrix.androidsdk.rest.model.Event;

import java.util.ArrayList;

import de.librechurch.synagocli.Adapter.RoomAdapter;
import de.librechurch.synagocli.Helper.Helper;

public class RoomListActivity extends AppCompatActivity {

    private static final String LOG_TAG = RoomListActivity.class.getSimpleName();
    // Our Matrix Singleton
    private Matrix matrix;
    // All joined Groups and Conversations
    private static ArrayList<RoomSummary> roomSummaries;

    private RoomAdapter mAdapter;
    private ListView listView;

    boolean loaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "##onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        matrix = Matrix.getInstance();
        roomSummaries = new ArrayList<>(matrix.getSession().getDataHandler().getStore().getSummaries());

        //Using
        // https://guides.codepath.com/android/Using-an-ArrayAdapter-with-ListView
        // Create the adapter to convert the array to views
        //roomAdapter adapter = new RoomAdapter(this, roomSummaries);
        // Attach the adapter to a ListView
        mAdapter = new RoomAdapter(getBaseContext(), roomSummaries);
        listView = (ListView) findViewById(R.id.rooms_view);
        listView.setAdapter(mAdapter);

        //Collections.sort(roomSummaries, Helper.getRoomSummaryComparator(false));
        mAdapter.sort(Helper.getRoomSummaryComparator(false));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView,
                                    View view, int position, long rowId) {
                final Intent intent = new Intent(RoomListActivity.this, ChatActivity.class);
                intent.putExtra("roomId",mAdapter.getItem(position).getRoomId());
                startActivity(intent);
            }
        });

        // Create a new Listener and RoomListener to add incoming messages...
        MXEventListener mListener = new MXEventListener(){
            @Override
            public void onLiveEvent(Event event, RoomState roomState) {
                super.onLiveEvent(event, roomState);
                Log.d(LOG_TAG, "Live event caught: "+event.getType());
                if (event.getType().matches(Event.EVENT_TYPE_MESSAGE)) {
                    Log.d(LOG_TAG, "New Message somewhere; Updating roomSummaries");

                    roomSummaries = new ArrayList<>(matrix.getSession().getDataHandler().getStore().getSummaries());
                    //Collections.sort(roomSummaries, Helper.getRoomSummaryComparator(false));
                    mAdapter.sort(Helper.getRoomSummaryComparator(false));
                    //mAdapter.clear();
                    //mAdapter.addAll(roomSummaries);
                    //Does not play well with changed positions.
                    //mAdapter.notifyDataSetChanged();
                    }
            }
        };
        matrix.getSession().getDataHandler().addListener(mListener);
    }

    @Override
    protected void onResume() {

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();

        Log.d(LOG_TAG, "##onResume");
        super.onResume();

        if (!loaded) {
            //First time just set the loaded flag true
            loaded = true;
        } else {
            //We need to update the Summaries.
            roomSummaries = new ArrayList<>(matrix.getSession().getDataHandler().getStore().getSummaries());
            mAdapter.sort(Helper.getRoomSummaryComparator(false));
            //mAdapter.notifyDataSetChanged();
        }

    }
}


