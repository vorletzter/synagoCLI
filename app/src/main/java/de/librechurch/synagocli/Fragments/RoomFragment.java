package de.librechurch.synagocli.Fragments;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import org.matrix.androidsdk.MXSession;
import org.matrix.androidsdk.data.RoomState;
import org.matrix.androidsdk.data.RoomSummary;
import org.matrix.androidsdk.listeners.MXEventListener;
import org.matrix.androidsdk.rest.model.Event;

import java.util.ArrayList;

import de.librechurch.synagocli.Adapter.RoomAdapter;
import de.librechurch.synagocli.ChatActivity;
import de.librechurch.synagocli.Helper.Helper;
import de.librechurch.synagocli.Matrix;
import de.librechurch.synagocli.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 */
public class RoomFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    private static final String LOG_TAG = RoomFragment.class.getSimpleName();

    // Store instance variables
    private String userId;
    private MXSession mSession;

    // All joined Groups and Conversations for Session.
    private static ArrayList<RoomSummary> roomSummaries;

    private RoomAdapter mAdapter;
    private ListView listView;

    boolean loaded;

    public RoomFragment() {
        // Required empty public constructor
    }

    // newInstance constructor for creating fragment with arguments
    public static RoomFragment newInstance(String userId) {
        RoomFragment fragmentFirst = new RoomFragment();
        Bundle args = new Bundle();
        args.putString("userId", userId);
        fragmentFirst.setArguments(args);
        return fragmentFirst;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString("userId");
            Log.d(LOG_TAG,"## onCreate: Creating Room Fragment for: " + userId);
            mSession = Matrix.getInstance(getContext()).getSessionByUserId(userId);
            if(mSession == null ){
                Log.e(LOG_TAG, " ##OnCreate -> Internal error: "+userId+ "has no session.");
                System.exit(0);
            }
        }else{
            Log.e(LOG_TAG, " ##OnCreate -> Internal error: No Arguments passed to onCreate");
            System.exit(0);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(
                R.layout.fragment_room, container, false);

        Log.d(LOG_TAG,"## onCreateView for  " + mSession.getMyUserId());
        roomSummaries = new ArrayList<>(mSession.getDataHandler().getStore().getSummaries());

        //Using
        // https://guides.codepath.com/android/Using-an-ArrayAdapter-with-ListView
        // Create the adapter to convert the array to views
        //roomAdapter adapter = new RoomAdapter(this, roomSummaries);
        // Attach the adapter to a ListView
        mAdapter = new RoomAdapter(rootView.getContext(), roomSummaries, mSession);
        listView = (ListView) rootView.findViewById(R.id.rooms_view);
        listView.setAdapter(mAdapter);

        //Collections.sort(roomSummaries, Helper.getRoomSummaryComparator(false));
        mAdapter.sort(Helper.getRoomSummaryComparator(false));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView,
                                    View view, int position, long rowId) {
                final Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra("roomId",mAdapter.getItem(position).getRoomId());
                intent.putExtra("userId",userId);
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
                    //Log.d(LOG_TAG, "New Message somewhere; Updating roomSummaries");

                    roomSummaries = new ArrayList<>(mSession.getDataHandler().getStore().getSummaries());
                    //Collections.sort(roomSummaries, Helper.getRoomSummaryComparator(false));
                    mAdapter.sort(Helper.getRoomSummaryComparator(false));
                    mAdapter.clear();
                    mAdapter.addAll(roomSummaries);
                    //Does not play well with changed positions.
                    //mAdapter.notifyDataSetChanged();
                }
            }
        };
        mSession.getDataHandler().addListener(mListener);

        return rootView;
        //return inflater.inflate(R.layout.fragment_room, container, false);
    }

    @Override
    public void onResume() {
        NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();

        Log.d(LOG_TAG, "##onResume");
        super.onResume();

        if (!loaded) {
            //First time just set the loaded flag true
            loaded = true;
        } else {
            //We need to update the Summaries.
            roomSummaries = new ArrayList<>(mSession.getDataHandler().getStore().getSummaries());
            mAdapter.sort(Helper.getRoomSummaryComparator(false));
            mAdapter.notifyDataSetChanged();
        }

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }



}
