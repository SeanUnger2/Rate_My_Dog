package w.sean.ratemydog.Utils;

import android.content.Context;
import android.content.Intent;

import w.sean.ratemydog.FavoritesActivity;
import w.sean.ratemydog.MainActivity;
import w.sean.ratemydog.MyPicsActivity;
import w.sean.ratemydog.NewDogActivity;
import w.sean.ratemydog.R;

public class ToolbarUtils {

    public static final int NO_ICON = 2;

    public static void setBottomNavigation(Context context, int id){
        switch (id) {
            case R.id.action_favorites:
                Intent favoritesIntent = new Intent(context, FavoritesActivity.class);
                favoritesIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                context.startActivity(favoritesIntent);
                break;
            case R.id.action_home:
                Intent mainIntent = new Intent(context, MainActivity.class);
                mainIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                context.startActivity(mainIntent);
                break;
            case R.id.action_my_dogs:
                Intent myPicsIntent = new Intent(context, MyPicsActivity.class);
                myPicsIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                context.startActivity(myPicsIntent);
                break;

        }
    }
}