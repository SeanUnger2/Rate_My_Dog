package w.sean.ratemydog;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabItem;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.util.ArrayUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import w.sean.ratemydog.POJOs.Dog;
import w.sean.ratemydog.Utils.NetworkUtils;
import w.sean.ratemydog.Utils.OnGetDataListener;
import w.sean.ratemydog.Utils.ToolbarUtils;

public class MainActivity extends AppCompatActivity {

    MyAdapter mAdapter;
    ViewPager mPager;
    private static ArrayList<Dog> arrDogs;
    private static ArrayList<String> arrDogKeys;
    private boolean activityReseting;
    private static int currentItem;
    private int startPosition;
    private static final String START_POSITION= "start_position";

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //if the user kills the activity and restarts it, make sure we pick up where we left off
        if(savedInstanceState !=null && savedInstanceState.getInt(START_POSITION)>-1){
            startPosition = savedInstanceState.getInt(START_POSITION);
        }else{
            startPosition = 0;
        }


        //make sure we're connected to the internet
        NetworkUtils.checkNetworkStatus(this);
        retrieveRecentlyAddedDogs();
        setBottomBar();
        onTabClick();

        activityReseting = true;
    }

    private void onTabClick(){
        TabLayout tabLayout = (TabLayout)findViewById(R.id.tab_layout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                NetworkUtils.checkNetworkStatus(getApplicationContext());
                switch (tab.getPosition()){
                    case 0:
                        activityReseting = true;
                        retrieveRecentlyAddedDogs();
                        break;
                    case 1:
                        activityReseting = true;
                        retrieveRandomDogs();
                        break;
                    default:
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }
            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });
    }

    private void retrieveRecentlyAddedDogs(){
        Dog.getDogsNode(this, new OnGetDataListener() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                arrDogs = new ArrayList<>();
                arrDogKeys = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Dog dog = snapshot.getValue(Dog.class);
                    arrDogs.add(0, dog);
                    //array of dog id's in firebase
                    arrDogKeys.add(0, snapshot.getKey());
                }
                //we don't need to reset the whole adapter every time the database gets updated or the
                //user pauses and restarts the activity
                if(activityReseting) {
                    setAdapter();
                    activityReseting = false;
                }

            }
        });
    }

    private void retrieveRandomDogs(){
        Dog.getDogsNode(this, new OnGetDataListener() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                arrDogs = new ArrayList<>();
                arrDogKeys = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Dog dog = snapshot.getValue(Dog.class);
                    double randomIndex =  Math.random() * (arrDogs.size()+1);
                    arrDogs.add( (int)randomIndex, dog);
                    arrDogKeys.add( (int)randomIndex, snapshot.getKey());
                }
                if(activityReseting) {
                    setAdapter();
                    activityReseting = false;
                }

            }
        });
    }

    private void setAdapter(){
        mAdapter = new MyAdapter(getSupportFragmentManager());

        mPager = (ViewPager)findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
        mPager.setCurrentItem(startPosition);
    }

    public static class MyAdapter extends FragmentStatePagerAdapter {
        public MyAdapter(FragmentManager fm) { super(fm); }

        @Override
        public int getCount() {
            return arrDogs.size(); }

        @Override
        public Fragment getItem(int position) {
            //the adapter loads one element in advance, so the item the user is looking at is actually the previous
            currentItem = position-1;
            return RandomDogFragment.newInstance(arrDogs, arrDogKeys, position);
        }
    }

    private void setBottomBar() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bnv);
        bottomNavigationView.setOnNavigationItemSelectedListener(
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    ToolbarUtils.setBottomNavigation(MainActivity.this, item.getItemId());
                    return true;
                }
            });
    }

    @Override
    protected void onSaveInstanceState(Bundle outstate){
        outstate.putInt(START_POSITION, currentItem);
        super.onSaveInstanceState(outstate);
    }

    @Override
    protected void onStop(){
        Dog.removeDogsNodeValueEventListener();
        super.onStop();
    }
}
