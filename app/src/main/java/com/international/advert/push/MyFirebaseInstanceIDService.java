package com.international.advert.push;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.international.advert.model.APHandler;
import com.international.advert.model.NormalResponseModel;
import com.international.advert.utility.App;
import com.international.advert.utility.Constant;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by softm on 19-Sep-17.
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseIIDService";

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(refreshedToken);
    }
    // [END refresh_token]

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {

        App.appPrefs.edit().putString(Constant.USER_TOKEN, token).apply();

        if (App.appPrefs.getString(Constant.USER_ID, "").equals(""))
            return;

        APHandler.apiInterface.update_fcm_token(App.appPrefs.getString(Constant.USER_ID, ""),
                token)
                .enqueue(new Callback<NormalResponseModel>() {
                    @Override
                    public void onResponse(Call<NormalResponseModel> call, Response<NormalResponseModel> response) {

                    }

                    @Override
                    public void onFailure(Call<NormalResponseModel> call, Throwable t) {

                    }
                });
        
    }
}
