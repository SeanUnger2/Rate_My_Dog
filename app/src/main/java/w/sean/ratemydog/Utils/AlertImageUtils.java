package w.sean.ratemydog.Utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import w.sean.ratemydog.MyPicsActivity;
import w.sean.ratemydog.POJOs.Dog;
import w.sean.ratemydog.R;

public class AlertImageUtils {

    private static AlertDialog alertDialog;

    public static void inflateImage(Activity activity, Bitmap bitmap, final OnDialogChanged listener){
        LayoutInflater inflater = activity.getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.alert_image, null);
        ImageView ivInflatedDog = (ImageView)alertLayout.findViewById(R.id.iv_inflated_dog);
        ivInflatedDog.setImageBitmap(bitmap);

        alertDialog = new AlertDialog.Builder(activity).create();
        alertDialog.setView(alertLayout);
        alertDialog.setCancelable(true);
        alertDialog.show();

        listener.onCreate();

        alertLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                listener.onDismiss();
            }
        });
    }

    public static void inflateImage(Activity activity, Bitmap bitmap){
        LayoutInflater inflater = activity.getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.alert_image, null);
        ImageView ivInflatedDog = (ImageView)alertLayout.findViewById(R.id.iv_inflated_dog);
        ivInflatedDog.setImageBitmap(bitmap);

        alertDialog = new AlertDialog.Builder(activity).create();
        alertDialog.setView(alertLayout);
        alertDialog.setCancelable(true);
        alertDialog.show();

        alertLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
    }

    public static AlertDialog displayMoreInfo(Activity activity, Dog dog, final OnDialogChanged listener){
        LayoutInflater inflater = activity.getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.alert_more_info, null);
        TextView tvName = (TextView) alertLayout.findViewById(R.id.tv_name);
        TextView tvAge = (TextView) alertLayout.findViewById(R.id.tv_age);
        TextView tvBreed = (TextView) alertLayout.findViewById(R.id.tv_breed);
        TextView tvHobbies = (TextView) alertLayout.findViewById(R.id.tv_hobbies);
        TextView tvDislikes = (TextView) alertLayout.findViewById(R.id.tv_dislikes);

        tvName.setText(dog.getName());
        if(dog.getAge().trim().length() > 0) {
            tvAge.setText(", " + dog.getAge());
        }else{
            tvAge.setVisibility(View.GONE);
        }
        if(dog.getBreed().trim().length() > 0){
            tvBreed.setText(dog.getBreed());
        }else{
            tvBreed.setVisibility(View.GONE);
        }
        if(dog.getHobbies().trim().length() > 0){
            tvHobbies.setText("Hobbies: " + dog.getHobbies());
        }else{
            tvHobbies.setVisibility(View.GONE);
        }
        if(dog.getDislikes().trim().length() > 0){
            tvDislikes.setText("Dislikes: " + dog.getDislikes());
        }else{
            tvDislikes.setVisibility(View.GONE);
        }

        alertDialog = new AlertDialog.Builder(activity).create();
        alertDialog.setView(alertLayout);
        alertDialog.setTitle("More Info:");
        alertDialog.setCancelable(true);
        alertDialog.show();

        listener.onCreate();

        alertLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                listener.onDismiss();
            }
        });

        return alertDialog;
    }

    public static void dismissCurrentDialog(){
        alertDialog.dismiss();
    }
}
