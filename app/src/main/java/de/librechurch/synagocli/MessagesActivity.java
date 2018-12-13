package de.librechurch.synagocli;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import org.matrix.androidsdk.MXSession;
import org.matrix.androidsdk.adapters.MessageRow;
import org.matrix.androidsdk.data.Room;
import org.matrix.androidsdk.data.RoomMediaMessage;
import org.matrix.androidsdk.data.RoomState;
import org.matrix.androidsdk.data.RoomSummary;
import org.matrix.androidsdk.listeners.MXEventListener;
import org.matrix.androidsdk.listeners.MXRoomEventListener;
import org.matrix.androidsdk.rest.callback.ApiCallback;
import org.matrix.androidsdk.rest.model.Event;
import org.matrix.androidsdk.rest.model.MatrixError;

import java.util.ArrayList;

import de.librechurch.synagocli.Adapter.EventAdapter;
import de.librechurch.synagocli.Adapter.MessagesAdapter;

// found some Magic Items from the SDK i could/should use maybe?

public class MessagesActivity extends AppCompatActivity {

    private static final String LOG_TAG = MessagesActivity.class.getSimpleName();

    // Our Matrix Singleton
    private Matrix matrix;

    // "New-Message"-Edit
    private EditText newMessageEditText;

    // Information about The current room and Session User
    private Room room;
    private RoomSummary roomSummary;
    private MXSession mSession;
    private String roomId;
    private String userId;

    //Instance of our EventAdapterClass
    MessagesAdapter mAdapter;
    //The ListView for attaching the EventAdapter.
    ListView listView;

    private RoomState mRoomState;

    @Override
    protected void onNewIntent(Intent intent) {
        //Should be called, if user klicked on a Notifciation due
        // to android:launchMode="singleTop" in Manifest
        super.onNewIntent(intent);
        Log.d(LOG_TAG, "Chat was already open.. overwriting");

        loadUI();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        loadUI();
    }

    private void loadUI() {

        this.matrix = Matrix.getInstance(getApplicationContext());


        this.newMessageEditText = (EditText) findViewById(R.id.sendTextInput);

        this.roomId = getIntent().getExtras().getString("roomId");
        this.userId = getIntent().getExtras().getString("userId");
        this.mSession = Matrix.getInstance(getApplicationContext()).getSessionByUserId(userId);
        this.room = mSession.getDataHandler().getRoom(roomId);
        this.roomSummary = room.getRoomSummary();
        this.mRoomState = room.getState();

        Log.d(LOG_TAG, "opening for userId " + this.userId + " and roomId " + roomId);


        setTitle(this.roomSummary.getLatestRoomState().name);


        this.room.getDataHandler().setLazyLoadingEnabled(true);
        ArrayList<Event> events = new ArrayList<>(this.room.getStore().getRoomMessages(roomId));

        // Create the adapter to convert the array to views
        listView = (ListView) findViewById(R.id.events_view);
        mAdapter = new MessagesAdapter(this, 0, mSession);

        for (Event e : events) {
            mAdapter.add(new MessageRow(e, room.getState()));
        }
        // Attach the adapter to a ListView
        listView.setAdapter(mAdapter);
        listView.setSelection(listView.getCount());

        // Create a new Listener and RoomListener to add incoming messages...
        MXEventListener mListener = new MXEventListener() {
            @Override
            public void onLiveEvent(Event event, RoomState roomState) {
                super.onLiveEvent(event, roomState);
                Log.d(LOG_TAG, "Live event caught");
                //processEvent(event);
            }
        };
        mSession.getDataHandler().addListener(new MXRoomEventListener(room, mListener));
        room.sendReadReceipt();
    }


    /*
        Attach a new Event and Update the UI
        Called by the MXRoomEventListener.

    */
    public void processEvent(final Event event) {
        Log.d(LOG_TAG, "type:" + event.type);
        if (event.type.equals("m.room.message")) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAdapter.add(new MessageRow(event, mRoomState));
                    // scroll the ListView to the last added element
                    listView.setSelection(listView.getCount() - 1);
                }
            });
        }
    }


    /*
        Our "onClick"-Method
        The User wants to send a Message
        */

    public void sendMessage(View view) {
        //Get the Input from out Textvield
        String message = newMessageEditText.getText().toString();

        if (message.length() > 0) {

            //Create a new MediaMessage from the UsersInput
            RoomMediaMessage rm = new RoomMediaMessage(message, null, null);

            //Send the Media Message. Use a callback to check, if the Event for the Message could be created
            room.sendMediaMessage(rm, 0, 0, new RoomMediaMessage.EventCreationListener() {
                @Override
                public void onEventCreated(final RoomMediaMessage roomMediaMessage) {
                    Log.d(LOG_TAG, "Send Message event created and dispatched: " + roomMediaMessage.getEvent().getContent());
                    processEvent(roomMediaMessage.getEvent());

                    //Attach a Callback to check, if the Message was send to the Server
                    roomMediaMessage.setEventSendingCallback(new ApiCallback<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //for now, this nested Callbacks are kinda useless.
                            //Most Messengers would show "one Hook" at this point
                            //Later we could show the second hook, if the Server deliverd the
                            //Message back to us via the EventStream.
                            //ToDo: Implement this and figure out, how to identify this Event in the EvenStream
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mAdapter.remove(new MessageRow(roomMediaMessage.getEvent(), mRoomState));
                                    // scroll the ListView to the last added element
                                    listView.setSelection(listView.getCount() - 1);
                                }
                            });
                        }

                        @Override
                        public void onNetworkError(Exception e) {
                        }

                        @Override
                        public void onMatrixError(MatrixError matrixError) {
                        }

                        @Override
                        public void onUnexpectedError(Exception e) {
                        }

                    });
                }

                @Override
                public void onEventCreationFailed(RoomMediaMessage roomMediaMessage, String s) {
                }

                @Override
                public void onEncryptionFailed(RoomMediaMessage roomMediaMessage) {
                }
            });
            newMessageEditText.getText().clear();
        }
    }
}
