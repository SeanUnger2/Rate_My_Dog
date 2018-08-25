package w.sean.ratemydog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import w.sean.ratemydog.POJOs.Dog;
import w.sean.ratemydog.Utils.AlertImageUtils;
import w.sean.ratemydog.Utils.NetworkUtils;
import w.sean.ratemydog.Utils.OnDialogChanged;
import w.sean.ratemydog.Utils.OnGetDataListener;
import w.sean.ratemydog.Utils.ToolbarUtils;

public class MyPicsActivity extends AppCompatActivity {

    private ArrayList<Dog> arrMyDogs;
    private ListView listView;
    private static final String TAG = "MyPicsActivity";
    private long sizeOfMyDogs;
    private Dog currentDog;
    private int currentDialogType;
    private static final int IMAGE = 0;
    private static final int MORE_INFO = 1;
    private static final int NO_DIALOG = 2;
    private static final String DIALOG_TYPE = "dialog-type";
    private static final String CURRENT_DOG = "current-dog";
    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_pics);

        NetworkUtils.checkNetworkStatus(this);

        if(savedInstanceState!=null ){
            currentDialogType = savedInstanceState.getInt(DIALOG_TYPE);
            currentDog = (Dog)savedInstanceState.getSerializable(CURRENT_DOG);
            if(currentDog != null) {
                if (currentDialogType == MORE_INFO) {
                    showMoreInfoDialog(currentDog);
                }
            }
        }else{
            System.out.println("saved instance state is null");
            currentDialogType = NO_DIALOG;
            currentDog = null;
        }

        setupToolbar(ToolbarUtils.NO_ICON, "My Dogs");
        setBottomBar();
        retrieveMyDogs();
    }

    private void setupToolbar(int icon, String title){
        if(icon == ToolbarUtils.NO_ICON){
            ((ImageView) findViewById(R.id.iv_app_bar)).setVisibility(View.GONE);
        }else {
            ((ImageView) findViewById(R.id.iv_app_bar)).setImageResource(icon);
        }
        ((TextView)findViewById(R.id.tv_app_bar)).setText(title);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void setBottomBar() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bnv);
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        ToolbarUtils.setBottomNavigation(MyPicsActivity.this, item.getItemId());
                        return true;
                    }
                });
    }

    private void retrieveMyDogs(){
        Dog.getMyDogsNode(this, new OnGetDataListener() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                arrMyDogs = new ArrayList<>();
                sizeOfMyDogs = dataSnapshot.getChildrenCount();
                //first retrieve all the dog references in the My Dogs node, then for each one, retrieve the dog object from
                //the master Dog list
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String dogKey = snapshot.getKey();
                    Dog.getSpecificDog(MyPicsActivity.this, dogKey, new OnGetDataListener() {
                        @Override
                        public void onSuccess(DataSnapshot dataSnapshot) {
                            Dog dog = dataSnapshot.getValue(Dog.class);
                            if(dog != null) {
                                arrMyDogs.add(0, dog);
                            }else{  //dog is null, which means it no longer exists in the master list
                                sizeOfMyDogs -= 1;
                            }
                            System.out.println("here is the array size: " + arrMyDogs.size());
                            //wait to set the adapter until the array is full
                            if(arrMyDogs.size()==sizeOfMyDogs){
                                setAdapter();
                            }
                        }
                    });
                }
            }
        });
    }

    private void setAdapter(){
        MyDogsAdapter adapter = new MyDogsAdapter(this, arrMyDogs);
        listView = (ListView)findViewById(R.id.lv_my_dogs);
        listView.setAdapter(adapter);
        setListItemClick();
    }

    private void setListItemClick(){
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "Clicked Item " + position);
                showMoreInfoDialog(arrMyDogs.get(position));
            }
        });
    }

    private void showMoreInfoDialog(final Dog dog){
        alertDialog = AlertImageUtils.displayMoreInfo(MyPicsActivity.this, dog, new OnDialogChanged() {
            @Override
            public void onCreate() {
                currentDialogType = MORE_INFO;
                System.out.println("current dialog type more info");
                currentDog = dog;
            }

            @Override
            public void onDismiss() {
                currentDialogType = NO_DIALOG;
                currentDog = null;
                System.out.println("current dialog type none");
            }
        });
    }

    private class MyDogsAdapter extends BaseAdapter {

        private Context context; //context
        private ArrayList<Dog> dogs;

        public MyDogsAdapter(Context context, ArrayList<Dog> items) {
            this.context = context;
            this.dogs = items;
        }

        @Override
        public int getCount() {
            return dogs.size();
        }

        @Override
        public Object getItem(int position) {
            return dogs.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // inflate the layout for each list row
            if (convertView == null) {
                convertView = LayoutInflater.from(context).
                        inflate(R.layout.item_my_dogs_list, parent, false);
            }

            final Dog currentItem = (Dog) getItem(position);

            //fetch the views from item_allergy layout
            final ImageView ivDog = (ImageView) convertView.findViewById(R.id.iv_dog);
            final ProgressBar progressBar = convertView.findViewById(R.id.progress_bar);
            final TextView tvName = convertView.findViewById(R.id.tv_name);
            final TextView tvAge = convertView.findViewById(R.id.tv_age);
            final TextView tvBreed = convertView.findViewById(R.id.tv_breed);
            final TextView tvRating = convertView.findViewById(R.id.tv_rating);

            tvName.setText(currentItem.getName());
            if(currentItem.getAge().trim().length() > 0) {
                tvAge.setText(", " + currentItem.getAge());
            }else{
                tvAge.setVisibility(View.GONE);
            }
            if(currentItem.getBreed().trim().length() > 0){
                tvBreed.setText(currentItem.getBreed());
            }else{
                tvBreed.setVisibility(View.GONE);
            }
            if(currentItem.getStarsPossible() == 0){
                tvRating.setText("No ratings yet");
            }
            else {
                double overallRating = (double) currentItem.getStarsAccrued()/currentItem.getStarsPossible() * 5;
                tvRating.setText("Rating: " + round(overallRating,1));
            }

            ivDog.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            Picasso.get()
                    .load(currentItem.getPicLocation())
                    .into(ivDog, new Callback() {
                        @Override
                        public void onSuccess() {
                            progressBar.setVisibility(View.GONE);
                            ivDog.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onError(Exception e) {

                        }
                    });
            ivDog.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bitmap bitDog = ((BitmapDrawable)ivDog.getDrawable()).getBitmap();
                    showImageDialog(bitDog, currentItem);
                }
            });

            // returns the view for the current row
            return convertView;
        }
    }

    private void showImageDialog(Bitmap bitmap, final Dog dog){
        System.out.println("show image dialog");
        AlertImageUtils.inflateImage(MyPicsActivity.this, bitmap, new OnDialogChanged() {
            @Override
            public void onCreate() {
                currentDog = dog;
                currentDialogType = IMAGE;
                System.out.println("current dialog type image");
            }

            @Override
            public void onDismiss() {
                currentDialogType = NO_DIALOG;
                System.out.println("current dialog type none");
            }
        });
    }

    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my_pics_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_add_dog:
                startActivity(new Intent(MyPicsActivity.this, NewDogActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        if(alertDialog != null && alertDialog.isShowing()) {
            AlertImageUtils.dismissCurrentDialog();
            System.out.println("dialog type on save: " + currentDialogType);
            if (currentDialogType == MORE_INFO && currentDog != null) {
                outState.putInt(DIALOG_TYPE, currentDialogType);
                outState.putSerializable(CURRENT_DOG, currentDog);
            }
            super.onSaveInstanceState(outState);
        }
    }

}
