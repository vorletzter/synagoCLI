package de.librechurch.synagocli.Adapter;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.matrix.androidsdk.MXSession;
import org.matrix.androidsdk.adapters.AbstractMessagesAdapter;
import org.matrix.androidsdk.adapters.MessageRow;
import org.matrix.androidsdk.rest.model.Event;
import org.matrix.androidsdk.rest.model.RoomMember;
import org.matrix.androidsdk.rest.model.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.librechurch.synagocli.Helper.AvatarHelper;
import de.librechurch.synagocli.R;

public class MessagesAdapter extends AbstractMessagesAdapter {

    private MXSession mSession;
    private Context mContext;

    // current date : used to compute the day header
    // Check Source on how
    private Date mReferenceDate = new Date();
    // day date of each message
    // the hours, minutes and seconds are removed
    private List<Date> mMessagesDateList = new ArrayList<>();

    private final Map<String, MessageRow> mEventRowMap = new HashMap<>();
    //private final List<MessageRow> mMessagRows = new ArrayList<>;

    public MessagesAdapter(Context context, int view, MXSession session) {
        super(context, view);
        mContext = context;
        mSession = session;
    }

    //Add a row and refresh the adapter if it is required.
    @Override
    public void add(MessageRow messageRow, boolean b) {

    }

    //Add a message row to the top.
    @Override
    public void addToFront(MessageRow messageRow) {

    }

    @Override
    public MessageRow getMessageRow(String s) {
        return null;
    }

    //Provides the messageRow from an event Id.
    @Override
    public MessageRow getClosestRow(Event event) {
        return null;
    }

    @Override
    public MessageRow getClosestRowFromTs(String s, long l) {
        return null;
    }

    @Override
    public MessageRow getClosestRowBeforeTs(String s, long l) {
        return null;
    }

    @Override
    public void updateEventById(Event event, String s) {

    }

    @Override
    public void removeEventById(String s) {

    }

    @Override
    public void setIsPreviewMode(boolean b) {

    }

    @Override
    public void setIsUnreadViewMode(boolean b) {

    }

    @Override
    public boolean isUnreadViewMode() {
        return false;
    }

    @Override
    public void setSearchPattern(String s) {

    }

    @Override
    public void resetReadMarker() {

    }

    @Override
    public void updateReadMarker(String s, String s1) {

    }

    @Override
    public int getMaxThumbnailWidth() {
        return 0;
    }

    @Override
    public int getMaxThumbnailHeight() {
        return 0;
    }

    @Override
    public void onBingRulesUpdate() {

    }

    @Override
    public void setLiveRoomMembers(List<RoomMember> list) {

    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Event event = getItem(position).getEvent();
        User user = mSession.getDataHandler().getUser(event.getSender());
        String message;

        LayoutInflater messageInflater = (LayoutInflater) getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        ViewEventHolder viewHolder = new ViewEventHolder();

        //Log.d(LOG_TAG, "Sender: "+event.getSender()+ "(myID: "+Matrix.getInstance().getSession().getMyUserId());
        //Log.d(LOG_TAG, "Content "+ event.getContent());

        if (event.isEncrypted()) {
        }

        try {
            message = event.getContent().getAsJsonObject().get("body").toString().replaceAll("\"", "");
        } catch (NullPointerException e) {
            message = "<< " + event.getType() + " >>";
        }

        // Message was sent by us
        if (TextUtils.equals(mSession.getMyUserId(), event.getSender())) {

            //If the Event is not yet send (e.g. queued to be) it gets a different color.
            if (!event.isSent()) {
                convertView = messageInflater.inflate(R.layout.my_message_sending, null);
            } else {
                convertView = messageInflater.inflate(R.layout.my_message, null);
            }

            viewHolder.messageBody = (TextView) convertView.findViewById(R.id.message_body);
            convertView.setTag(viewHolder);
            TextView messageBody =
                    ((ViewEventHolder) convertView.getTag()).messageBody;
            messageBody.setText(message);

            //Message was send by another party
        } else {

            convertView = messageInflater.inflate(R.layout.their_message, null);

            // Lookup view for data population
            viewHolder.senderName =
                    (TextView) convertView.findViewById(R.id.sender_name);
            viewHolder.senderAvatar =
                    (ImageView) convertView.findViewById(R.id.sender_avatar);
            viewHolder.messageBody =
                    (TextView) convertView.findViewById(R.id.message_body);

            // Store results of findViewById
            convertView.setTag(viewHolder);

            // Populate the data into the template view using the data object
            TextView senderName =
                    ((ViewEventHolder) convertView.getTag()).senderName;
            ImageView senderAvatar =
                    ((ViewEventHolder) convertView.getTag()).senderAvatar;
            TextView messageBody =
                    ((ViewEventHolder) convertView.getTag()).messageBody;


            AvatarHelper.loadUserAvatar(getContext(), mSession, senderAvatar, user);
            senderName.setText(user.displayname);
            messageBody.setText(message);


            //GradientDrawable drawable = (GradientDrawable) viewHolder.senderAvatar.getBackground();
            //drawable.setColor(Color.parseColor("12"));
        }
        return convertView;
    }

}
