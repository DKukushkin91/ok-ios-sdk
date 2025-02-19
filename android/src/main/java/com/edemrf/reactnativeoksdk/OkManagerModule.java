package com.edemrf.reactnativeoksdk;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import com.facebook.react.bridge.*;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableMap;

import org.json.JSONObject;

import ru.ok.android.sdk.Odnoklassniki;
import ru.ok.android.sdk.OkListener;
import ru.ok.android.sdk.Shared;
import ru.ok.android.sdk.util.OkAuthType;
import ru.ok.android.sdk.OkRequestMode;

public class OkManagerModule extends ReactContextBaseJavaModule implements ActivityEventListener {
    private static final String LOG = "OkManager";
    private static final String E_LOGIN_ERROR = "E_LOGIN_ERROR";
    private static final String E_GET_USER_FAILED = "E_GET_USER_FAILED";

    private Odnoklassniki odnoklassniki;
    private String redirectUri;
    private Promise loginPromise;
    private boolean isInitialized = false;

    public OkManagerModule(final ReactApplicationContext reactContext) {
        super(reactContext);
        reactContext.addActivityEventListener(this);
    }

    @Override
    public String getName() {
        return "OkManager";
    }

    @ReactMethod
    public void initialize(final String appId, final String appKey) {
        Log.d(LOG, "Inititalizing app " + appId + " with key " + appKey);
        odnoklassniki = Odnoklassniki.createInstance(getReactApplicationContext(), appId, appKey);
        isInitialized = true;
        redirectUri = "okauth://ok" + appId;
    }

    @ReactMethod
    public void login(final ReadableArray scope, final Promise promise) {
        int scopeSize = scope.size();
        final String[] scopeArray = new String[scopeSize];
        for (int i = 0; i < scopeSize; i++) {
            scopeArray[i] = scope.getString(i);
        }
        loginPromise = promise;
        odnoklassniki.checkValidTokens(new OkListener() {
            @Override
            public void onSuccess(JSONObject json) {
                Log.d(LOG, "Check valid token success");
                resolveWithCurrentUser(json.optString(Shared.PARAM_ACCESS_TOKEN), json.optString(Shared.PARAM_SESSION_SECRET_KEY));
            }

            @Override
            public void onError(String error) {
                Log.d(LOG, "Valid token wasn't found at login, requesting authorization");
                odnoklassniki.requestAuthorization(getCurrentActivity(), redirectUri, OkAuthType.ANY, scopeArray);
            }
        });
    }

    @ReactMethod
    public void logout(Promise promise) {
        Log.d(LOG, "Logout");
        odnoklassniki.clearTokens();
        promise.resolve(null);
    }

    @Override
    public void onActivityResult(final Activity activity, final int requestCode, final int resultCode, final Intent data) {
        if (isInitialized) {
            if (Odnoklassniki.getInstance().isActivityRequestOAuth(requestCode)) {
                Odnoklassniki.getInstance().onAuthActivityResult(requestCode, resultCode, data, getAuthListener());
            }
        }
    }

    private OkListener getAuthListener() {
        return new OkListener() {
            @Override
            public void onSuccess(final JSONObject json) {
                Log.d(LOG, "Activity auth success");
                resolveWithCurrentUser(json.optString(Shared.PARAM_ACCESS_TOKEN), json.optString(Shared.PARAM_SESSION_SECRET_KEY));
            }

            @Override
            public void onError(String error) {
                Log.d(LOG, "OK Oauth error " + error);
                loginPromise.reject(E_LOGIN_ERROR, error);
            }
        };
    }

    private void resolveWithCurrentUser(final String accessToken, final String sessionSecretKey) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String userStr = odnoklassniki.request("users.getCurrentUser", null, OkRequestMode.DEFAULT);
                    JSONObject user = new JSONObject(userStr);
                    WritableMap result = Arguments.createMap();
                    result.putString(Shared.PARAM_ACCESS_TOKEN, accessToken);
                    result.putString(Shared.PARAM_SESSION_SECRET_KEY, sessionSecretKey);
                    result.putMap("user", JSONUtil.convertMap(user));
                    loginPromise.resolve(result);
                } catch (Exception e) {
                    loginPromise.reject(E_GET_USER_FAILED, "users.getLoggedInUser failed: " + e.getLocalizedMessage());
                }
            }
        }).start();
    }

    @Override
    public void onNewIntent(Intent intent) {

    }
}
