package de.librechurch.synagocli;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import de.librechurch.synagocli.Helper.LoginStorage;
import de.librechurch.synagocli.Services.ListenerService;

public class MainActivity extends AppCompatActivity {

    // Log Tag for nicer Debug
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    Matrix matrix;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Check for valid Login Data...
        LoginStorage loginStorage = new LoginStorage(getApplicationContext());
        ArrayList<HomeServerConnectionConfig> hsConfigList = loginStorage.getCredentialsList();

        if(hsConfigList.size() > 0) {
            // Store needs full hsConfig (inc. AccessToken), otherwise Matrix declares store as "corrupted".
            Log.d(LOG_TAG, "Using stored Connection: " + hsConfigList.get(0));
            startMatrixSession(hsConfigList.get(0));
        }else{
            final Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
    }

    public void startMatrixSession(HomeServerConnectionConfig hsConfig) {
        Credentials credentials = hsConfig.getCredentials();

        //Create and Boot up a File Store
        IMXStore store;
        store = new MXFileStore(hsConfig, false, getApplicationContext());
        final MXDataHandler dataHandler = new MXDataHandler(store, credentials);
        ((MXFileStore) store).setDataHandler(dataHandler);

        //Start a new Matrix Session and store it in our Matrix Singleton
        final MXSession session = new MXSession.Builder(hsConfig, dataHandler, getApplicationContext())
                .build();
        Matrix.getInstance().setSession(session);
        session.getDataHandler().getStore().open();
        dataHandler.setLazyLoadingEnabled(true);

        // Everything is golden. Now we can start the EventStream
        // But before we can start or a resume the EventStream, we need to make sure, that the store is ready
        if (session.getDataHandler().getStore().isReady()) { // the store is ready (no data loading in progress...)
            Log.d(LOG_TAG, "Store is ready...");
            startEventStream();
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
                    startEventStream();
                }

                @Override
                public void onStoreCorrupted(String accountId, String description) {
                    // start a new initial sync
                    Log.e(LOG_TAG, "## onStoreCorrupted: " + description);
                    session.getDataHandler().getStore().clear();
                    // Show a Warning Toast
                    String message = "Store corrupted. Please restart App";
                    Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();
                }

                @Override
                public void onStoreOOM(final String accountId, final String description) {
                    Log.e(LOG_TAG, "## onStoreOutOfMemory");
                    String message = "Store out of Memory";
                    Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();
                }
            });

        }
    }

    public void startEventStream(){
        Matrix matrix = Matrix.getInstance();
        final Intent intent = new Intent(this, RoomListActivity.class);

        String token = matrix.getSession().getDataHandler().getStore().getEventStreamToken();
        Log.d(LOG_TAG, "Token: " + token);

        if (token != null) {
            matrix.getSession().startEventStream(token);
        }else {
            matrix.getSession().startEventStream(null);
        }

        if(matrix.getSession().getDataHandler().isInitialSyncComplete()) {
            Log.d(LOG_TAG,"InitialSync completed. Starting RoomListActivity.");
            startActivity(intent);
            startService();
            finish();
        }else{
            Log.d(LOG_TAG,"waiting for InitialSync to finish....");
            matrix.getSession().getDataHandler().addListener(new MXEventListener(){
                @Override
                public void onInitialSyncComplete(String toToken) {
                    //super.onInitialSyncComplete(toToken);
                    //Matrix.getInstance().getSession().getDataHandler().getStore().commit();
                    Log.d(LOG_TAG," ##onInitalSynCompleted ## InitialSync completed. Starting RoomListActivity.");
                    startActivity(intent);
                    startService();
                    finish();
                }
            });
        }
    }

    public void startService() {
        // use this to start and trigger a service
        Intent i= new Intent(getApplicationContext(), ListenerService.class);
        // potentially add data to the intent
        i.putExtra("KEY1", "Value to be used by the service");
        getApplicationContext().startService(i);
    }
}
