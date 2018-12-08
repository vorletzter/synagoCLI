package de.librechurch.synagocli;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.matrix.androidsdk.HomeServerConnectionConfig;
import org.matrix.androidsdk.MXDataHandler;
import org.matrix.androidsdk.MXSession;
import org.matrix.androidsdk.data.store.IMXStore;
import org.matrix.androidsdk.data.store.MXFileStore;
import org.matrix.androidsdk.data.store.MXStoreListener;
import org.matrix.androidsdk.listeners.MXEventListener;
import org.matrix.androidsdk.rest.callback.SimpleApiCallback;
import org.matrix.androidsdk.rest.client.LoginRestClient;
import org.matrix.androidsdk.rest.model.MatrixError;
import org.matrix.androidsdk.rest.model.login.Credentials;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    // Log Tag for nicer Debug
    private static final String LOG_TAG = LoginActivity.class.getSimpleName();

    // UI references.
    private EditText mLoginNameView;
    private EditText mPasswordView;
    private EditText mHomeServerView;
    private View mProgressView;
    private View mLoginFormView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set up the Form Data for showProgress()
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        // Set up the login form.
        mLoginNameView = (EditText) findViewById(R.id.loginName);
        mPasswordView = (EditText) findViewById(R.id.password);
        mHomeServerView = (EditText) findViewById(R.id.toHomeServer);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
            attemptLogin();
                        return true;
                    }
                    return false;
                }
            });
            Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
            mEmailSignInButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    attemptLogin();
                }
            });
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        // Reset errors.
        mLoginNameView.setError(null);
        mPasswordView.setError(null);
        mHomeServerView.setError(null);

        // Store values at the time of the login attempt.
        String loginName = mLoginNameView.getText().toString();
        String password = mPasswordView.getText().toString();
        String homeServer = mHomeServerView.getText().toString();


        boolean cancel = false;
        View focusView = null;

        // Check for a valid HomeServer
        if (TextUtils.isEmpty(homeServer)) {
            mHomeServerView.setError(getString(R.string.error_field_required));
            focusView = mHomeServerView;
            cancel = true;
        }

        // Check for a valid password
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        /* Try creating a valid URL */
        try {
            new URL(homeServer).toURI();
        }
        catch (Exception e) {
            mHomeServerView.setError(getString(R.string.error_uri_parse));
            focusView = mHomeServerView;
            cancel = true;
        }

        // Check for a valid name.
        if (TextUtils.isEmpty(loginName)) {
            mLoginNameView.setError(getString(R.string.error_field_required));
            focusView = mLoginNameView;
            cancel = true;
        }
        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);

            // We build a Matrix HomeServerConnectionConfig to Store all required information.
            final HomeServerConnectionConfig hsConfig = new HomeServerConnectionConfig.Builder()
                    .withHomeServerUri(Uri.parse(homeServer))
                    .build();

            // We create a LoginClient
            new LoginRestClient(hsConfig).loginWithUser(loginName, password, new SimpleApiCallback<Credentials>(this) {
                @Override
                public void onSuccess(Credentials c) {
                    Log.d(LOG_TAG, "Login With User/Password succesfull: Got Logintoken ");
                    // Add the Credentials to hsConfig.
                    hsConfig.setCredentials(c);
                    LoginStorage loginStorage = new LoginStorage(getApplicationContext());
                    loginStorage.addCredentials(hsConfig);
                    //We just kill the Activity here and let onResume() in MainActivity Handle the login
                    finish();
                }

                public void onMatrixError(MatrixError e) {
                    Log.e(LOG_TAG, "## onNetworkError: "+e.getMessage());
                    Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                    showProgress(false);
                }

                public void onNetworkError(java.lang.Exception e) {
                    Log.e(LOG_TAG, "## onNetworkError: " +  e.getMessage());
                    Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                    showProgress(false);
                }

                public void onUnexpectedError(java.lang.Exception e) {
                    Log.e(LOG_TAG, "## onUnexpectedError: " + e.getMessage());
                    Toast.makeText(getApplicationContext(),"unexpected error. Maybe no Homesever? : "+e.getMessage(),Toast.LENGTH_LONG).show();
                    showProgress(false);
                }
            });

        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}

