package de.librechurch.synagocli.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import org.matrix.androidsdk.data.RoomSummary;
import org.matrix.androidsdk.rest.model.RoomMember;
import java.util.ArrayList;

import de.librechurch.synagocli.R;


public class RoomAdapter extends ArrayAdapter<RoomSummary> {

    // Log Tag for nicer Debug
    private static final String LOG_TAG = RoomAdapter.class.getSimpleName();

    public RoomAdapter(Context context, ArrayList<RoomSummary> rooms) {
        super(context, 0, rooms);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        RoomSummary room = getItem(position);

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

        // Store results of findViewById
        convertView.setTag(viewHolder);

        TextView roomName =
                ((ViewHolder)convertView.getTag()).roomName;
        TextView roomTopic =
                ((ViewHolder)convertView.getTag()).roomTopic;
        TextView roomMID =
                ((ViewHolder)convertView.getTag()).roomMID;
        TextView notificationBadge =
                ((ViewHolder)convertView.getTag()).notificationBadge;

        if(room.getNotificationCount() > 0) {
            notificationBadge.setText(""+room.getNotificationCount());
            notificationBadge.setVisibility(View.VISIBLE);
        }else{
            notificationBadge.setVisibility(View.INVISIBLE);
        }

        if(room.getLatestRoomState().name == null )  {
            roomName.setText("no name");
            String members = "";
            for (RoomMember m : room.getLatestRoomState().getLoadedMembers()) {
                members = members+m.displayname+" & ";
            }
            roomTopic.setText(members);
            roomMID.setText(room.getLatestRoomState().roomId);
        }else {
            roomName.setText(room.getLatestRoomState().name);
            roomTopic.setText(room.getLatestRoomState().topic);
            roomMID.setText(room.getLatestRoomState().roomId);
        }
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
}
