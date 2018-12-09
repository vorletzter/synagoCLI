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

    int sessions = 0;
    MXSession session = null;

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
        sessions = hsConfigList.size();

        if(hsConfigList.size() > 0) {
            // Store needs full hsConfig (inc. AccessToken), otherwise Matrix declares store as "corrupted".
            // Start all Sessions..
            Log.d(LOG_TAG, "found hsConfigs in storage, activating sessions");
            for (HomeServerConnectionConfig hs : hsConfigList) {
                Log.d(LOG_TAG, "Starting Session: " + hs);
                session = Matrix.getInstance(getApplicationContext()).activateSession(hs);
                Matrix.getInstance(getApplicationContext()).addSession(session);

                if (session.getDataHandler().isInitialSyncComplete()) {
                    Log.d(LOG_TAG, "(" + session.getMyUserId() + ") completed initial sync");
                    Log.d(LOG_TAG, "Waiting for " + sessions + "Sessions");
                    sessions--;
                    if (MainActivity.this.sessions == 0) {
                        final Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    Log.d(LOG_TAG, "(" + session.getMyUserId() + ") initial sync in progess");
                    session.getDataHandler().addListener(new MXEventListener() {
                        @Override
                        public void onInitialSyncComplete(String toToken) {
                            //super.onInitialSyncComplete(toToken);
                            //Matrix.getInstance().getSession().getDataHandler().getStore().commit();
                            MainActivity.this.sessions--;
                            Log.d(LOG_TAG, "Waiting for " + sessions + "Sessions");
                            if (MainActivity.this.sessions == 0) {
                                final Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                startActivity(intent);
                                finish();

                            }
                            Log.d(LOG_TAG, "(" + MainActivity.this.session.getMyUserId() + ") completed initial sync");
                        }
                    });
                }
            }
            startService();
        }else{
            Log.d(LOG_TAG,"no valid sessions found. Prompting");
                final Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
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
