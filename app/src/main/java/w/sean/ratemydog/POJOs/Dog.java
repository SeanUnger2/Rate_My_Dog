package w.sean.ratemydog.POJOs;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Parcelable;
import android.support.annotation.Keep;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.io.Serializable;

import w.sean.ratemydog.Utils.OnGetDataListener;
import w.sean.ratemydog.Utils.SharedPrefUtils;

@Keep
public class Dog implements Serializable{

    private static final DatabaseReference accountReference = FirebaseDatabase.getInstance().getReference();
    private static ValueEventListener postListener;
    private String name, age, breed, hobbies, dislikes, picLocation;
    private int starsAccrued, starsPossible;

    public Dog(){

    }

    public Dog(String name, String age, String breed, String hobbies, String dislikes, String picLocation,
            int starsAccrued, int starsPossible) {
        this.name = name;
        this.age = age;
        this.breed = breed;
        this.hobbies = hobbies;
        this.dislikes = dislikes;
        this.picLocation = picLocation;
        this.starsAccrued = starsAccrued;
        this.starsPossible =starsPossible;
    }

    public void setName(String name){
        this.name = name;
    }
    public String getName(){
        return this.name;
    }
    public void setAge(String age){
        this.age = age;
    }
    public String getAge(){
        return this.age;
    }
    public void setBreed(String breed){
        this.breed = breed;
    }
    public String getBreed(){
        return this.breed;
    }
    public void setHobbies(String hobbies){
        this.hobbies = hobbies;
    }
    public String getHobbies(){
        return this.hobbies;
    }
    public void setDislikes(String dislikes){
        this.dislikes = dislikes;
    }
    public String getDislikes(){
        return this.dislikes;
    }
    public void setPicLocation(String picLocation){
        this.picLocation = picLocation;
    }
    public String getPicLocation(){
        return this.picLocation;
    }
    public void setStarsAccrued(int starsAccrued){
        this.starsAccrued = starsAccrued;
    }
    public int getStarsAccrued(){
        return this.starsAccrued;
    }
    public void setStarsPossible(int starsPossible){
        this.starsPossible = starsPossible;
    }
    public int getStarsPossible(){
        return this.starsPossible;
    }

    public static void getDogsNode(final Context context, final OnGetDataListener listener){
        postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("data change " + dataSnapshot);
                listener.onSuccess(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println(databaseError.toString());
                Toast.makeText(context,
                        "Could not retrieve data. You may not be connected to the internet.",
                        Toast.LENGTH_LONG).show();
            }
        };
        accountReference.child("DOGS").addValueEventListener(postListener);

    }

    public static void getMyDogsNode(Context context, final OnGetDataListener listener){
        postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("data change " + dataSnapshot);
                listener.onSuccess(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        SharedPrefUtils sharedPrefUtils = new SharedPrefUtils(context);
        String userId = sharedPrefUtils.getUserId();
        accountReference.child("USERS").child(userId).child("MY DOGS").addValueEventListener(postListener);

    }

    public static void getSpecificDog(Context context, String dogKey, final OnGetDataListener listener) {
        postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("data change " + dataSnapshot);
                listener.onSuccess(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        accountReference.child("DOGS").child(dogKey).addValueEventListener(postListener);
    }

    public static void getFavoritesNode(Context context, final OnGetDataListener listener){
        postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listener.onSuccess(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        SharedPrefUtils sharedPrefUtils = new SharedPrefUtils(context);
        String userId = sharedPrefUtils.getUserId();
        accountReference.child("USERS").child(userId).child("FAVORITES").addValueEventListener(postListener);

    }

    public static void getSpecificRating(Context context, String dogKey, final OnGetDataListener listener){
        postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listener.onSuccess(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        SharedPrefUtils sharedPrefUtils = new SharedPrefUtils(context);
        String userId = sharedPrefUtils.getUserId();
        accountReference.child("USERS").child(userId).child("RATINGS").child(dogKey).addValueEventListener(postListener);

    }

    public static void removeDogsNodeValueEventListener(){
        if(postListener != null){
            accountReference.child("DOGS").removeEventListener(postListener);
        }
    }

    public static void removeSpecificDogNodeValueEventListener(String dogKey){
        if(postListener != null){
            accountReference.child("DOGS").child(dogKey).removeEventListener(postListener);
        }
    }

    public static void removeSpecificRatingNodeValueEventListener(Context context, String dogKey){
        if(postListener != null){
            SharedPrefUtils sharedPrefUtils = new SharedPrefUtils(context);
            accountReference.child("USERS").child(sharedPrefUtils.getUserId()).child("RATINGS")
                    .child(dogKey).removeEventListener(postListener);
        }
    }


}
