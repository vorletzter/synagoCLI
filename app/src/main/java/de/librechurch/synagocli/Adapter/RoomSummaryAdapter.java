package de.librechurch.synagocli.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.matrix.androidsdk.MXSession;
import org.matrix.androidsdk.data.Room;
import org.matrix.androidsdk.data.RoomSummary;

import java.util.ArrayList;
import java.util.List;

import de.librechurch.synagocli.ChatActivity;
import de.librechurch.synagocli.Helper.AvatarHelper;
import de.librechurch.synagocli.R;


public class RoomSummaryAdapter extends RecyclerView.Adapter<RoomSummaryAdapter.ViewHolder> {

    // Log Tag for nicer Debug
    private static final String LOG_TAG = RoomSummaryAdapter.class.getSimpleName();

    private MXSession mSession;
    private Context mContext;
    private List<RoomSummary> mRoomSummaries;

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder {
        // The ViewHolder contains a direct reference to all elements
        // in "room.xml" View
        View mainView;
        TextView roomName;
        TextView roomTopic;
        TextView roomMID;
        TextView notificationBadge;
        TextView last_chat_time;
        ImageView room_avatar_image_view;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);
            // Lookup view for data population
            mainView = itemView;
            roomName =
                    (TextView) itemView.findViewById(R.id.room_name);
            roomTopic =
                    (TextView) itemView.findViewById(R.id.room_topic);
            roomMID =
                    (TextView) itemView.findViewById(R.id.room_mid);
            notificationBadge =
                    (TextView) itemView.findViewById(R.id.notification_badge);
            room_avatar_image_view =
                    (ImageView) itemView.findViewById(R.id.room_avatar_image_view);
            last_chat_time =
                    (TextView) itemView.findViewById(R.id.last_chat_time);
        }

    }


    /**
     * Overrides for the RecylerView
     */

    // Inflate the "room" Layout
    @Override
    public RoomSummaryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View roomView = inflater.inflate(R.layout.room, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(roomView);
        return viewHolder;
    }


    // We initalise the Adapter with some default Items
    public RoomSummaryAdapter(Context context, ArrayList<RoomSummary> rooms, MXSession session) {
        mSession = session;
        mContext = context;
        mRoomSummaries = rooms;
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return mRoomSummaries.size();
    }

    // Involves populating data into the item through holder
    //ToDO: Improve performance, e.g. using an interface in Create() instead of onClickListener here
    @Override
    public void onBindViewHolder(final RoomSummaryAdapter.ViewHolder viewHolder, final int position) {
        // Get the data model based on position
        RoomSummary roomSummary = mRoomSummaries.get(position);
        Room room = mSession.getDataHandler().getRoom(roomSummary.getRoomId());

        TextView roomName = viewHolder.roomName;
        TextView roomTopic = viewHolder.roomTopic;
        TextView roomMID = viewHolder.roomMID;
        TextView roomLastChat = viewHolder.last_chat_time;
        TextView notificationBadge = viewHolder.notificationBadge;
        ImageView roomAvatar = viewHolder.room_avatar_image_view;

        if (roomSummary.getNotificationCount() > 0) {
            notificationBadge.setText("" + roomSummary.getNotificationCount());
            notificationBadge.setVisibility(View.VISIBLE);
        } else {
            notificationBadge.setVisibility(View.INVISIBLE);
        }

        AvatarHelper.loadRoomAvatar(mContext, mSession, roomAvatar, room);
        String name = room.getRoomDisplayName(mContext);
        roomName.setText(name);
        roomMID.setText(room.getRoomId());
        roomTopic.setText(room.getTopic());

        //Set an onClick Listener for each element.
        viewHolder.mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(viewHolder.mainView.getContext(), ChatActivity.class);
                intent.putExtra("roomId", mRoomSummaries.get(position).getRoomId());
                intent.putExtra("userId", mSession.getMyUserId());
                viewHolder.mainView.getContext().startActivity(intent);
            }
        });

    }

    /**
     * Adapter Methods
     **/

    public void add(RoomSummary room) {
        mRoomSummaries.add(room);
        notifyDataSetChanged();
    }

    // For now we use the "Wooden-Hammer" Method
    // and just update the whole List
    // ToDO: Use "ListAdapater" and "DiffUtil" (https://guides.codepath.com/android/using-the-recyclerview)
    public void updateList(ArrayList<RoomSummary> rooms) {
        mRoomSummaries.clear();
        mRoomSummaries.addAll(rooms);
        notifyDataSetChanged();
    }

    View.OnClickListener onItemClickListener;

    public void setItemClickListener(View.OnClickListener clickListener) {
        onItemClickListener = clickListener;
    }

}
