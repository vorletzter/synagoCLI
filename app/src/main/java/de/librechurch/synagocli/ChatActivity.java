package de.librechurch.synagocli;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import org.matrix.androidsdk.MXSession;
import org.matrix.androidsdk.data.Room;
import org.matrix.androidsdk.data.RoomMediaMessage;
import org.matrix.androidsdk.data.RoomState;
import org.matrix.androidsdk.data.RoomSummary;
import org.matrix.androidsdk.listeners.MXEventListener;
import org.matrix.androidsdk.listeners.MXRoomEventListener;
import org.matrix.androidsdk.rest.callback.ApiCallback;
import org.matrix.androidsdk.rest.model.Event;
import org.matrix.androidsdk.rest.model.MatrixError;


// found some Magic Items from the SDK i could/should use maybe?
import org.matrix.androidsdk.adapters.AbstractMessagesAdapter;
import org.matrix.androidsdk.adapters.MessageRow;


import java.util.ArrayList;

import de.librechurch.synagocli.Adapter.MessagesAdapter;
import de.librechurch.synagocli.Adapter.RoomSummaryAdapter;

public class ChatActivity extends AppCompatActivity {

    private static final String LOG_TAG = ChatActivity.class.getSimpleName();

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
    //MessagesAdapter mAdapter;
    //The ListView for attaching the EventAdapter.
    RecyclerView mRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onNewIntent(Intent intent) {
        //Should be called, if user klicked on a Notifciation due
        // to android:launchMode="singleTop" in Manifest
        super.onNewIntent(intent);
        Log.d(LOG_TAG, "Chat was already open.. overwriting");

        loadUI();
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
        mLayoutManager = new LinearLayoutManager(getApplicationContext());

        this.roomId = getIntent().getExtras().getString("roomId");
        this.userId = getIntent().getExtras().getString("userId");
        Log.d(LOG_TAG, "opening for userId "+ this.userId+" and roomId "+roomId);

        mSession = Matrix.getInstance(getApplicationContext()).getSessionByUserId(userId);
        this.room = mSession.getDataHandler().getRoom(roomId);
        this.roomSummary = room.getRoomSummary();

        setTitle(this.roomSummary.getLatestRoomState().name);

        this.room.getDataHandler().setLazyLoadingEnabled(true);

        ArrayList<Event> events = new ArrayList<>(this.room.getStore().getRoomMessages(roomId));
        ArrayList<MessageRow> rows = new ArrayList();
        for (Event e : events) {
            rows.add(new MessageRow(e, room.getState()));
        }

        mAdapter = new MessagesAdapter(getApplicationContext(), mSession, rows);
        mRecyclerView = (RecyclerView) findViewById(R.id.message_row_view);
        // Performance Tweak be Preloading
        mRecyclerView.setItemViewCacheSize(25);
        mRecyclerView.setDrawingCacheEnabled(true);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.getLayoutManager().scrollToPosition(mAdapter.getItemCount() - 1);

        // Create a new Listener and RoomListener to add incoming messages...
        MXEventListener mListener = new MXEventListener(){
            @Override
            public void onLiveEvent(Event event, RoomState roomState) {
                super.onLiveEvent(event, roomState);
                Log.d(LOG_TAG, "Live event caught");
                processEvent(event);
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
            mAdapter.add(new MessageRow(event, room.getState()));
            mLayoutManager.scrollToPosition(mAdapter.getItemCount() - 1);
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
            room.sendMediaMessage(rm, 0,0,new RoomMediaMessage.EventCreationListener(){
                @Override
                public void onEventCreated(final RoomMediaMessage roomMediaMessage) {
                    Log.d(LOG_TAG,"Send Message event created and dispatched: " + roomMediaMessage.getEvent().getContent());
                    //processEvent(roomMediaMessage.getEvent());

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
                                    //adapter.remove(roomMediaMessage.getEvent());
                                    // scroll the ListView to the last added element
                                    //listView.setSelection(listView.getCount() - 1);
                                }
                            });
                        }

                        @Override
                        public void onNetworkError(Exception e) { }
                        @Override
                        public void onMatrixError(MatrixError matrixError) { }
                        @Override
                        public void onUnexpectedError(Exception e) { }

                    });
                }
                @Override
                public void onEventCreationFailed(RoomMediaMessage roomMediaMessage, String s) { }
                @Override
                public void onEncryptionFailed(RoomMediaMessage roomMediaMessage) { }
            });
            newMessageEditText.getText().clear();
        }

    }
}
