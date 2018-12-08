package de.librechurch.synagocli.Helper;

import org.matrix.androidsdk.data.RoomSummary;

import java.util.Comparator;

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

}
