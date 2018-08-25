package w.sean.ratemydog.Utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class NetworkUtils {

    public static boolean checkNetworkStatus(Context context){
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if(!isConnected){
            Toast.makeText(context, "You are not connected to the Internet", Toast.LENGTH_LONG).show();
        }
        return isConnected;
    }
}
