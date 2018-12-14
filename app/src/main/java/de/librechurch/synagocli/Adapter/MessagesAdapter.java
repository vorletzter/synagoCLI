package de.librechurch.synagocli.Adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
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

public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private MXSession mSession;
    private Context mContext;
    private List<MessageRow> mMessagRows = new ArrayList();
    private final String mThisUser;

    static final int ROW_MY_MESSAGE = 0;
    static final int ROW_THEIR_MESSAGE = 1;
    static final int ROW_ROOM_STATUS_CHANGE = 2;


    //****************  VIEW MY_MESSAGE ViewHolder ******************//
    public class ViewHolderMyMessage extends RecyclerView.ViewHolder {

        public TextView messageBody;
        boolean sent;
        //Contains the View itself
        View mainView;

        public ViewHolderMyMessage(View itemView) {
            super(itemView);
            messageBody = (TextView) itemView.findViewById(R.id.message_body);
            mainView = itemView;
        }
    }

    //****************  THEIR_MESSAGE ViewHolder ******************//
    public class ViewHolderTheirMessage extends RecyclerView.ViewHolder {
        public TextView senderName;
        public TextView messageBody;
        public ImageView senderAvatar;
        View mainView;

        public ViewHolderTheirMessage(View itemView) {
            super(itemView);
            // Lookup view for data population
            senderName = (TextView) itemView.findViewById(R.id.sender_name);
            senderAvatar = (ImageView) itemView.findViewById(R.id.sender_avatar);
            messageBody = (TextView) itemView.findViewById(R.id.message_body);
            mainView = itemView;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        RecyclerView.ViewHolder viewHolder = null;

        if (viewType == ROW_MY_MESSAGE) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_message, parent, false);
            viewHolder = new ViewHolderMyMessage(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.their_message, parent, false);
            viewHolder = new ViewHolderTheirMessage(view);
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {

        MessageRow mRow = mMessagRows.get(position);
        User user = mSession.getDataHandler().getUser(mRow.getSender().getUserId());
        String message;
        try {
            message = mRow.getEvent().getContent().getAsJsonObject().get("body").toString().replaceAll("\"", "");
        } catch (NullPointerException e) {
            message = "<< " + mRow.getEvent().getType() + " >>";
        }

        if (viewHolder.getItemViewType() == ROW_MY_MESSAGE) {
            //Typecast Viewholder
            ViewHolderMyMessage viewHolderMyMessage = (ViewHolderMyMessage) viewHolder;
            viewHolderMyMessage.messageBody.setText(message);

        } else {
            //Typecast Viewholder
            ViewHolderTheirMessage viewHolderTheirMessage = (ViewHolderTheirMessage) viewHolder;
            AvatarHelper.loadUserAvatar(viewHolderTheirMessage.mainView.getContext(), mSession, viewHolderTheirMessage.senderAvatar, user);
            viewHolderTheirMessage.senderName.setText(user.displayname);
            viewHolderTheirMessage.messageBody.setText(message);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mMessagRows.get(position).getSender().getUserId().equals(mThisUser)) {
            return ROW_MY_MESSAGE;
        } else
            return ROW_THEIR_MESSAGE;
    }

    public MessagesAdapter(Context context, MXSession session, ArrayList<MessageRow> rows) {
        mContext = context;
        mSession = session;
        mThisUser = session.getMyUserId();
        mMessagRows = rows;
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return mMessagRows.size();
    }

    public void add(MessageRow row) {
        int lastPos = mMessagRows.size();
        mMessagRows.add(row);
        notifyItemInserted(lastPos);
    }
}
