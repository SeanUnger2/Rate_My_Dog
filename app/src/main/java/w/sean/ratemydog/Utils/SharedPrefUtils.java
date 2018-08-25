package w.sean.ratemydog.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SharedPrefUtils {

    private SharedPreferences appSharedPrefs;
    private SharedPreferences.Editor prefsEditor;

    private static final String TAG = "SharedPrefUtils";
    private static final String APP_SHARED_PREFS = "app_shared_preferences";
    private static final String ID_KEY = "id_key";

    //user id's are generated in this class and saved in shared preferences since there is no
    //login featre to this app
    public SharedPrefUtils(Context context) {
        this.appSharedPrefs = context.getSharedPreferences(APP_SHARED_PREFS, Context.MODE_PRIVATE);
        this.prefsEditor = appSharedPrefs.edit();
    }

    private void setUserId(String userId){
        prefsEditor.putString(ID_KEY, userId);
        prefsEditor.commit();
    }

    public String getUserId() {
        if(appSharedPrefs.getString(ID_KEY, "").equals("")){
            Log.i(TAG, "Creating new user Id...");
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmssSS").format(new Date());
            setUserId(timeStamp);
        }
        Log.i(TAG, "User Id: " + appSharedPrefs.getString(ID_KEY, ""));
        return appSharedPrefs.getString(ID_KEY, "");
    }
}
