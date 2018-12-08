package de.librechurch.synagocli;

import android.util.Log;

import org.matrix.androidsdk.data.MyUser;
import org.matrix.androidsdk.data.RoomState;
import org.matrix.androidsdk.data.RoomSummary;
import org.matrix.androidsdk.listeners.IMXEventListener;
import org.matrix.androidsdk.rest.model.Event;
import org.matrix.androidsdk.rest.model.MatrixError;
import org.matrix.androidsdk.rest.model.User;
import org.matrix.androidsdk.rest.model.bingrules.BingRule;

import java.util.ArrayList;
import java.util.List;

public class eventStreamManager implements IMXEventListener {

    private static final String LOG_TAG = eventStreamManager.class.getSimpleName();

    @Override
    public void onStoreReady() {
        Log.d(LOG_TAG, "onStoreReady");
    }

    @Override
    public void onPresenceUpdate(Event event, User user) {

    }

    @Override
    public void onAccountInfoUpdate(MyUser myUser) {

    }

    @Override
    public void onIgnoredUsersListUpdate() {

    }

    @Override
    public void onDirectMessageChatRoomsListUpdate() {

    }

    @Override
    public void onLiveEvent(Event event, RoomState roomState) {
        Log.d(LOG_TAG, "onLiveEvent");
        //Log.d(LOG_TAG, event.type);
        //Log.d(LOG_TAG, event.sender);
        //Log.d(LOG_TAG, event.roomId);
    }

    @Override
    public void onLiveEventsChunkProcessed(String s, String s1) {

    }

    @Override
    public void onBingEvent(Event event, RoomState roomState, BingRule bingRule) {

    }

    @Override
    public void onEventSentStateUpdated(Event event) {

    }

    @Override
    public void onEventSent(Event event, String s) {

    }

    @Override
    public void onEventDecrypted(Event event) {

    }

    @Override
    public void onBingRulesUpdate() {

    }

    @Override
    public void onInitialSyncComplete(String s) {
        Log.d(LOG_TAG, "onInitialSyncComplete: "+s);
    }

    @Override
    public void onSyncError(MatrixError matrixError) {

    }

    @Override
    public void onCryptoSyncComplete() {

    }

    @Override
    public void onNewRoom(String s) {

    }

    @Override
    public void onJoinRoom(String s) {

    }

    @Override
    public void onRoomFlush(String s) {

    }

    @Override
    public void onRoomInternalUpdate(String s) {

    }

    @Override
    public void onNotificationCountUpdate(String s) {

    }

    @Override
    public void onLeaveRoom(String s) {

    }

    @Override
    public void onRoomKick(String s) {

    }

    @Override
    public void onReceiptEvent(String s, List<String> list) {

    }

    @Override
    public void onRoomTagEvent(String s) {

    }

    @Override
    public void onReadMarkerEvent(String s) {

    }

    @Override
    public void onToDeviceEvent(Event event) {

    }

    @Override
    public void onNewGroupInvitation(String s) {

    }

    @Override
    public void onJoinGroup(String s) {

    }

    @Override
    public void onLeaveGroup(String s) {

    }

    @Override
    public void onGroupProfileUpdate(String s) {

    }

    @Override
    public void onGroupRoomsListUpdate(String s) {

    }

    @Override
    public void onGroupUsersListUpdate(String s) {

    }

    @Override
    public void onGroupInvitedUsersListUpdate(String s) {

    }
}
