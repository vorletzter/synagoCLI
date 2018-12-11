package de.librechurch.synagocli.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.matrix.androidsdk.MXSession;
import org.matrix.androidsdk.data.Room;
import org.matrix.androidsdk.data.RoomSummary;
import org.matrix.androidsdk.rest.model.RoomMember;
import java.util.ArrayList;

import de.librechurch.synagocli.Helper.AvatarHelper;
import de.librechurch.synagocli.Helper.Helper;
import de.librechurch.synagocli.Matrix;
import de.librechurch.synagocli.R;


public class RoomAdapter extends ArrayAdapter<RoomSummary> {

    // Log Tag for nicer Debug
    private static final String LOG_TAG = RoomAdapter.class.getSimpleName();

    private MXSession mSession;

    public RoomAdapter(Context context, ArrayList<RoomSummary> rooms, MXSession session) {
        super(context, 0, rooms);
        mSession = session;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        RoomSummary roomSummary = getItem(position);
        Room room = mSession.getDataHandler().getRoom(roomSummary.getRoomId());

        //Log.d(LOG_TAG,"Room +'"+room.getRoomId()+"' has "+room.getNotificationCount()+" notifications");

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.room, parent, false);
        }

        // Lookup view for data population
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.roomName =
                (TextView)convertView.findViewById(R.id.room_name);
        viewHolder.roomTopic =
                (TextView)convertView.findViewById(R.id.room_topic);
        viewHolder.roomMID =
                (TextView)convertView.findViewById(R.id.room_mid);
        viewHolder.notificationBadge =
                (TextView)convertView.findViewById(R.id.notification_badge);
        viewHolder.room_avatar_image_view =
                (ImageView) convertView.findViewById(R.id.room_avatar_image_view);
        viewHolder.last_chat_time =
                (TextView) convertView.findViewById(R.id.last_chat_time);

        // Store results of findViewById
        convertView.setTag(viewHolder);

        TextView roomName =
                ((ViewHolder)convertView.getTag()).roomName;
        TextView roomTopic =
                ((ViewHolder)convertView.getTag()).roomTopic;
        TextView roomMID =
                ((ViewHolder)convertView.getTag()).roomMID;
        TextView roomLastChat =
                ((ViewHolder) convertView.getTag()).last_chat_time;
        TextView notificationBadge =
                ((ViewHolder)convertView.getTag()).notificationBadge;
        ImageView roomAvatar =
                ((ViewHolder) convertView.getTag()).room_avatar_image_view;

        if (roomSummary.getNotificationCount() > 0) {
            notificationBadge.setText("" + roomSummary.getNotificationCount());
            notificationBadge.setVisibility(View.VISIBLE);
        }else{
            notificationBadge.setVisibility(View.INVISIBLE);
        }

        AvatarHelper.loadRoomAvatar(getContext(), mSession, roomAvatar, room);
        roomName.setText(room.getRoomDisplayName(getContext()));
        roomMID.setText(room.getRoomId());
        roomTopic.setText(room.getTopic());

        // Wrong Time.. getAge does not return the correct value for this!?
        //roomLastChat.setText(Helper.formatSecondsIntervalFloored(getContext(), roomSummary.getLatestReceivedEvent().getAge()));




        return convertView;
    }
}

// Class to hold our TextViews, so that we need to look only once
// ToDo: I don't quite understand, why this saves CPU Cycles... need to research further
// https://code.tutsplus.com/tutorials/android-from-scratch-understanding-adapters-and-adapter-views--cms-26646
class ViewHolder{
    TextView roomName;
    TextView roomTopic;
    TextView roomMID;
    TextView notificationBadge;
    TextView last_chat_time;
    ImageView room_avatar_image_view;
}
