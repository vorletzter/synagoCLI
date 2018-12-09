package de.librechurch.synagocli;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import org.matrix.androidsdk.HomeServerConnectionConfig;
import org.matrix.androidsdk.MXDataHandler;
import org.matrix.androidsdk.MXSession;
import org.matrix.androidsdk.data.store.IMXStore;
import org.matrix.androidsdk.data.store.MXFileStore;
import org.matrix.androidsdk.data.store.MXStoreListener;
import org.matrix.androidsdk.listeners.MXEventListener;
import org.matrix.androidsdk.rest.model.login.Credentials;

import java.util.ArrayList;
import java.util.List;


public class Matrix {
    private static final String LOG_TAG = Matrix.class.getSimpleName();
    // The Instance of this Singleton
    private static Matrix instance = null;
    // Holds the Matrix Session
    //private MXSession session;
    // list of session
    private List<MXSession> mMXSessions;

    private Context mAppContext;

    // constructor
    private Matrix(Context context) {
        instance = this;
        mAppContext = context;
        mMXSessions = new ArrayList<>();
    }

    public static Matrix getInstance(Context mAppContext) {
        if (instance == null) {
            instance = new Matrix(mAppContext);
        }
        return(instance);
    }


    public MXSession getSession() {
        return this.mMXSessions.get(0);
    }


    public void addSession(MXSession mySession) {
        this.mMXSessions.add(mySession);
    }

    public List<MXSession> getSessions() {
        List<MXSession> sessions = new ArrayList<>();
        synchronized (LOG_TAG) {
            if (null != mMXSessions) {
                sessions = new ArrayList<>(mMXSessions);
            }
        }
        return sessions;
    }

    public synchronized MXSession getSessionByUserId(String userId) {
        if (null != userId) {
            List<MXSession> sessions;

            synchronized (this) {
                sessions = getSessions();
            }

            for (MXSession session : sessions) {
                Credentials credentials = session.getCredentials();

                if ((null != credentials) && (credentials.userId.equals(userId))) {
                    return session;
                }
            }
        }
        return null;
    }

    public synchronized MXSession activateSession(HomeServerConnectionConfig hsConfig) {
        Credentials credentials = hsConfig.getCredentials();

        //Create and Boot up a File Store
        IMXStore store;
        store = new MXFileStore(hsConfig, false, mAppContext);
        final MXDataHandler dataHandler = new MXDataHandler(store, credentials);
        ((MXFileStore) store).setDataHandler(dataHandler);

        //Start a new Matrix Session and store it in our Matrix Singleton
        final MXSession session = new MXSession.Builder(hsConfig, dataHandler, mAppContext)
                .build();

        session.getDataHandler().getStore().open();
        dataHandler.setLazyLoadingEnabled(true);

        String token = session.getDataHandler().getStore().getEventStreamToken();

        //addSession(session);

        // Everything is golden. Now we can start the EventStream
        // But before we can start or a resume the EventStream, we need to make sure, that the store is ready

        if (session.getDataHandler().getStore().isReady()) { // the store is ready (no data loading in progress...)
            Log.d(LOG_TAG, "Store is ready...");
            session.startEventStream(token);
            //startEventStreamForSession(session);
            if (store.isCorrupted()) {
                Log.e(LOG_TAG, "store error");
                System.exit(0);
            }
        } else {
            Log.d(LOG_TAG, "Waiting for store...");
            session.getDataHandler().getStore().addMXStoreListener(new MXStoreListener() { // We create a listener to wait for the store
                //The Store is now ready. We can resume the EventStream.
                @Override
                public void onStoreReady(String accountId) {
                    Log.d(LOG_TAG, "## onStoreReady");
                    //startEventStreamForSession(session);
                    String token = session.getDataHandler().getStore().getEventStreamToken();
                    session.startEventStream(token);
                }

                @Override
                public void onStoreCorrupted(String accountId, String description) {
                    // start a new initial sync
                    Log.e(LOG_TAG, "## onStoreCorrupted: " + description);
                    session.getDataHandler().getStore().clear();
                    // Show a Warning Toast
                    String message = "Store corrupted. Please restart App";
                    Toast.makeText(mAppContext,message,Toast.LENGTH_LONG).show();
                }

                @Override
                public void onStoreOOM(final String accountId, final String description) {
                    Log.e(LOG_TAG, "## onStoreOutOfMemory");
                    String message = "Store out of Memory";
                    Toast.makeText(mAppContext,message,Toast.LENGTH_LONG).show();
                }
            });

        }

        return session;
    }

    //Obsolete
    private void OBSstartEventStreamForSession(MXSession session){

        if(session.getDataHandler().isInitialSyncComplete()) {
            Log.d(LOG_TAG,"InitialSync completed. Starting HomeActivity.");
            //startActivity(intent);
            //startService();
            //finish();
        }else{
            Log.d(LOG_TAG,"waiting for InitialSync to finish....");
            session.getDataHandler().addListener(new MXEventListener(){
                @Override
                public void onInitialSyncComplete(String toToken) {
                    //super.onInitialSyncComplete(toToken);
                    //Matrix.getInstance().getSession().getDataHandler().getStore().commit();
                    Log.d(LOG_TAG," ##onInitalSynCompleted ## InitialSync completed. Starting homeActivity.");
                    //startActivity(intent);
                    //startService();
                    //finish();
                }
            });
        }
    }
}
