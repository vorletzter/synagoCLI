package de.librechurch.synagocli;

import android.content.Context;

import org.matrix.androidsdk.MXSession;


public class Matrix {
    private static final String LOG_TAG = Matrix.class.getSimpleName();
    // The Instance of this Singleton
    private static Matrix instance = null;
    // Holds the Matrix Session
    private MXSession session;

    public static Matrix getInstance() {
        if (instance == null) {
            instance = new Matrix();
        }
        return(instance);
    }

    public MXSession getSession() {
        return session;
    }

    public void setSession(MXSession mySession) {
        this.session = mySession;
    }
}
