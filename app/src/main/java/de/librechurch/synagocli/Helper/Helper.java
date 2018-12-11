package de.librechurch.synagocli.Helper;

import android.content.Context;

import org.matrix.androidsdk.data.RoomSummary;

import java.util.Comparator;

import de.librechurch.synagocli.R;

public class Helper {

    /**
     * Return a comparator to sort summaries by latest event
     *
     * @param reverseOrder true/false
     * @return ordered list
     */
    public static Comparator<RoomSummary> getRoomSummaryComparator(final boolean reverseOrder) {
        return new Comparator<RoomSummary>() {
            public int compare(RoomSummary leftRoomSummary, RoomSummary rightRoomSummary) {
                int retValue;
                long deltaTimestamp;

                if ((null == leftRoomSummary) || (null == leftRoomSummary.getLatestReceivedEvent())) {
                    retValue = 1;
                } else if ((null == rightRoomSummary) || (null == rightRoomSummary.getLatestReceivedEvent())) {
                    retValue = -1;
                } else if ((deltaTimestamp = rightRoomSummary.getLatestReceivedEvent().getOriginServerTs()
                        - leftRoomSummary.getLatestReceivedEvent().getOriginServerTs()) > 0) {
                    retValue = 1;
                } else if (deltaTimestamp < 0) {
                    retValue = -1;
                } else {
                    retValue = 0;
                }
                return reverseOrder ? -retValue : retValue;
            }
        };
    }

    /**
     * Format a time interval in seconds to a string
     *
     * @param context         the context.
     * @param secondsInterval the time interval.
     * @return the formatted string
     */
    public static String formatSecondsIntervalFloored(Context context, long secondsInterval) {
        String formattedString;

        if (secondsInterval < 0) {
            formattedString = context.getResources().getQuantityString(R.plurals.format_time_s, 0, 0);
        } else {
            if (secondsInterval < 60) {
                formattedString = context.getResources().getQuantityString(R.plurals.format_time_s,
                        (int) secondsInterval,
                        (int) secondsInterval);
            } else if (secondsInterval < 3600) {
                formattedString = context.getResources().getQuantityString(R.plurals.format_time_m,
                        (int) (secondsInterval / 60),
                        (int) (secondsInterval / 60));
            } else if (secondsInterval < 86400) {
                formattedString = context.getResources().getQuantityString(R.plurals.format_time_h,
                        (int) (secondsInterval / 3600),
                        (int) (secondsInterval / 3600));
            } else {
                formattedString = context.getResources().getQuantityString(R.plurals.format_time_d,
                        (int) (secondsInterval / 86400),
                        (int) (secondsInterval / 86400));
            }
        }

        return formattedString;
    }

}
