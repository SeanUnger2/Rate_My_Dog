package w.sean.ratemydog;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

import w.sean.ratemydog.POJOs.Dog;
import w.sean.ratemydog.Utils.AlertImageUtils;
import w.sean.ratemydog.Utils.NetworkUtils;
import w.sean.ratemydog.Utils.OnDialogChanged;
import w.sean.ratemydog.Utils.OnGetDataListener;
import w.sean.ratemydog.Utils.SharedPrefUtils;
import w.sean.ratemydog.Utils.ToolbarUtils;

public class FavoritesActivity extends AppCompatActivity {

    private ArrayList<Dog> arrFavorites;
    private long favoritesSize;
    private static final String TAG = "FavoritesActivity";
    private static final String ALERT = "alert";
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
        setContentView(R.layout.activity_favorites);

        NetworkUtils.checkNetworkStatus(this);

        System.out.println("on create");
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

        setupToolbar(ToolbarUtils.NO_ICON, "Favorites");
        setBottomBar();
        retrieveFavorites();
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
                        ToolbarUtils.setBottomNavigation(FavoritesActivity.this, item.getItemId());
                        return true;
                    }
                });
    }

    private void retrieveFavorites(){
        Dog.getFavoritesNode(this, new OnGetDataListener() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                favoritesSize = dataSnapshot.getChildrenCount();
                arrFavorites = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    final String dogKey = snapshot.getKey();
                    Dog.getSpecificDog(FavoritesActivity.this, dogKey, new OnGetDataListener() {
                        @Override
                        public void onSuccess(DataSnapshot dataSnapshot) {
                            Dog dog = dataSnapshot.getValue(Dog.class);
                            if(dog!=null) {
                                arrFavorites.add(0, dog);
                            }else{ //the dog is null, which means the dog has been deleted from the master list
                                favoritesSize -= 1;
                                //let's delete it from favorites too
                                SharedPrefUtils sharedPrefUtils = new SharedPrefUtils(FavoritesActivity.this);
                                DatabaseReference accountReference = FirebaseDatabase.getInstance().getReference();
                                accountReference.child("USERS").child(sharedPrefUtils.getUserId())
                                        .child("FAVORITES").child(dogKey).removeValue();
                            }
                            if(arrFavorites.size() == favoritesSize){
                                setAdapter();
                            }
                        }
                    });
                }
            }
        });
    }

    private void setAdapter(){
        FavoritesAdapter adapter = new FavoritesAdapter(this, arrFavorites);
        ListView listView = (ListView)findViewById(R.id.lv_favorites);
        listView.setAdapter(adapter);
        setListItemClick(listView);
    }

    private void setListItemClick(ListView listView){
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "Clicked Item " + position);
                showMoreInfoDialog(arrFavorites.get(position));
            }
        });
    }

    private void showMoreInfoDialog(final Dog dog){
        alertDialog = AlertImageUtils.displayMoreInfo(FavoritesActivity.this, dog, new OnDialogChanged() {
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

    private class FavoritesAdapter extends BaseAdapter {

        private Context context; //context
        private ArrayList<Dog> dogs;

        public FavoritesAdapter(Context context, ArrayList<Dog> items) {
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
        public View getView(final int position, View convertView, ViewGroup parent) {
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
                    Bitmap bitDog = ((BitmapDrawable) ivDog.getDrawable()).getBitmap();
                    showImageDialog(bitDog, currentItem);
                }

            });

            // returns the view for the current row
            return convertView;
        }
    }

    private void showImageDialog(Bitmap bitmap, final Dog dog){
        System.out.println("show image dialog");
        AlertImageUtils.inflateImage(FavoritesActivity.this, bitmap, new OnDialogChanged() {
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
