package de.librechurch.synagocli.Adapter;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import android.widget.TextView;

import org.matrix.androidsdk.MXSession;
import org.matrix.androidsdk.rest.model.Event;

import java.util.ArrayList;

import de.librechurch.synagocli.Matrix;
import de.librechurch.synagocli.R;

public class EventAdapter extends ArrayAdapter<Event> {

    private static final String LOG_TAG = EventAdapter.class.getSimpleName();
    private MXSession mSession;

    public EventAdapter(Context context, ArrayList<Event> rooms, MXSession mSession) {
        super(context, 0, rooms);
        this.mSession = mSession;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Event event = getItem(position);
        String message;
        LayoutInflater messageInflater = (LayoutInflater) getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        ViewEventHolder viewHolder = new ViewEventHolder();

        //Log.d(LOG_TAG, "Sender: "+event.getSender()+ "(myID: "+Matrix.getInstance().getSession().getMyUserId());
        //Log.d(LOG_TAG, "Content "+ event.getContent());

        try {
            message = event.getContent().getAsJsonObject().get("body").toString().replaceAll("\"", "");
        }catch (NullPointerException e) {
            message = "<< " + event.getType() + " >>";
        }

        // Message was sent by us
        if(TextUtils.equals(mSession.getMyUserId(), event.getSender())) {

            //If the Event is not yet send (e.g. queued to be) it gets a different color.
            if(!event.isSent()) {
                convertView = messageInflater.inflate(R.layout.my_message_sending, null);
            }else {
                convertView = messageInflater.inflate(R.layout.my_message, null);
            }

            viewHolder.messageBody = (TextView) convertView.findViewById(R.id.message_body);
            convertView.setTag(viewHolder);
            TextView messageBody =
                    ((ViewEventHolder) convertView.getTag()).messageBody;
            messageBody.setText(message);

        //Message was send by another party
        }else {

            convertView = messageInflater.inflate(R.layout.their_message, null);

            // Lookup view for data population
            viewHolder.senderName =
                    (TextView) convertView.findViewById(R.id.sender_name);
            viewHolder.senderAvatar =
                    (View) convertView.findViewById(R.id.sender_avatar);
            viewHolder.messageBody =
                    (TextView) convertView.findViewById(R.id.message_body);

            // Store results of findViewById
            convertView.setTag(viewHolder);

            // Populate the data into the template view using the data object
            TextView senderName =
                    ((ViewEventHolder) convertView.getTag()).senderName;
            View senderAvatar =
                    ((ViewEventHolder) convertView.getTag()).senderAvatar;
            TextView messageBody =
                    ((ViewEventHolder) convertView.getTag()).messageBody;


            senderName.setText(event.getSender());
            messageBody.setText(message);

            //GradientDrawable drawable = (GradientDrawable) viewHolder.senderAvatar.getBackground();
            //drawable.setColor(Color.parseColor("12"));
        }
        return convertView;
    }
}

// Class to hold our TextViews, so that we need to look only once
// ToDo: I don't quite understand, why this saves CPU Cycles... need to research further
// https://code.tutsplus.com/tutorials/android-from-scratch-understanding-adapters-and-adapter-views--cms-26646
class ViewEventHolder{
    public TextView senderName;
    public TextView messageBody;
    public View senderAvatar;
}
