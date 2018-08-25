package w.sean.ratemydog;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.Rating;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Random;

import w.sean.ratemydog.POJOs.Dog;
import w.sean.ratemydog.Utils.AlertImageUtils;
import w.sean.ratemydog.Utils.OnGetDataListener;
import w.sean.ratemydog.Utils.SharedPrefUtils;

public class RandomDogFragment extends Fragment {
    private int position;
    private static ArrayList<Dog> mArrDogs;
    private static ArrayList<String> mArrDogKeys;
    private Boolean isFavorited;

    static RandomDogFragment newInstance(ArrayList arrDogs, ArrayList arrDogKeys, int position) {
        RandomDogFragment f = new RandomDogFragment();

        mArrDogs = arrDogs;
        mArrDogKeys = arrDogKeys;
        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt("num", position);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("on create");
        this.position = getArguments() != null ? getArguments().getInt("num") : 1;
        isFavorited = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.layout_random_dog, container, false);
        final ImageView ivFavorite = (ImageView)v.findViewById(R.id.iv_favorite);
        final RatingBar ratingBar = (RatingBar)v.findViewById(R.id.rating_bar);
        final Button btnSubmitRating = (Button)v.findViewById(R.id.btn_submit_rating);
        final ProgressBar progressBar = v.findViewById(R.id.progress_bar);
        TextView tvOverallRating = v.findViewById(R.id.tv_overall_rating);
        final CardView cvRounded = v.findViewById(R.id.cv_rounded);
        final ImageView ivDog = v.findViewById(R.id.iv_dog);
        final Dog currentDog = mArrDogs.get(position);

        setTextFields(v, currentDog);
        loadImage(cvRounded, ivDog, currentDog, progressBar);
        checkIfFavorited(ivFavorite);
        onClickFavorite(ivFavorite);
        setRating(ratingBar);
        onClickSubmitRating(btnSubmitRating, currentDog, tvOverallRating,ratingBar);

        return v;
    }

    private void loadImage(final CardView cvRounded, final ImageView ivDog, Dog currentDog, final ProgressBar progressBar){
        progressBar.setVisibility(View.VISIBLE);
        cvRounded.setVisibility(View.INVISIBLE);
        Picasso.get()
                .load(currentDog.getPicLocation())
                .into(ivDog, new Callback() {
                    @Override
                    public void onSuccess() {
                        progressBar.setVisibility(View.GONE);
                        cvRounded.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError(Exception e) {
                    }
                });

        ivDog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitDog = ((BitmapDrawable)ivDog.getDrawable()).getBitmap();
                AlertImageUtils.inflateImage(getActivity(), bitDog);
            }
        });
    }

    private void setTextFields(View v, Dog currentDog){
        TextView tvNameAge = v.findViewById(R.id.tv_name_age);
        TextView tvBreed = v.findViewById(R.id.tv_breed);
        TextView tvHobbies = v.findViewById(R.id.tv_hobbies);
        TextView tvDislikes = v.findViewById(R.id.tv_dislikes);
        TextView tvOverallRating = v.findViewById(R.id.tv_overall_rating);

        if(currentDog.getAge().trim().length()>0) {
            tvNameAge.setText(currentDog.getName() + ", " + currentDog.getAge());
        }else{
            tvNameAge.setText(currentDog.getName());
        }
        if(currentDog.getBreed().trim().length()>0) {
            tvBreed.setText(currentDog.getBreed());
        }else{
            tvBreed.setVisibility(View.GONE);
        }
        if(currentDog.getHobbies().trim().length()>0){
            tvHobbies.setText(currentDog.getHobbies());
        }else{
            v.findViewById(R.id.tv_hobbies_label).setVisibility(View.GONE);
            tvHobbies.setVisibility(View.GONE);
        }
        if(currentDog.getDislikes().trim().length() > 0){
            tvDislikes.setText(currentDog.getDislikes());
        }
        else{
            v.findViewById(R.id.tv_dislikes_label).setVisibility(View.GONE);
            tvDislikes.setVisibility(View.GONE);
        }
        if(currentDog.getStarsPossible() == 0){
            tvOverallRating.setText("No ratings yet");
        }
        else {
            double overallRating = (double) currentDog.getStarsAccrued()/currentDog.getStarsPossible() * 5;
            tvOverallRating.setText("Overall Rating: " + round(overallRating,1));
        }
    }

    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private void setRating(final RatingBar ratingBar){
        Dog.getSpecificRating(getContext(), mArrDogKeys.get(position), new OnGetDataListener() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                int rating;
                try {
                    rating = (int) dataSnapshot.getValue(Integer.class);
                    System.out.println("here is the rating: " + rating);
                    ratingBar.setRating(rating);
                }catch (Exception e){
                    e.printStackTrace();
                    ratingBar.setRating(0);
                }
            }
        });
    }

    private void checkIfFavorited(final ImageView ivFavorite){
        Dog.getFavoritesNode(getContext(), new OnGetDataListener() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String dogKey = snapshot.getKey();
                    if(dogKey.equals(mArrDogKeys.get(position))){
                        ivFavorite.setImageResource(R.drawable.ic_favorite_pink_32dp);
                        isFavorited = true;
                    }
                }
            }
        });
    }

    private void onClickFavorite(final ImageView ivFavorite){
        ivFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPrefUtils sharedPrefUtils = new SharedPrefUtils(getContext());
                DatabaseReference accountReference = FirebaseDatabase.getInstance().getReference();
                if(isFavorited){
                    accountReference.child("USERS").child(sharedPrefUtils.getUserId())
                            .child("FAVORITES").child(mArrDogKeys.get(position)).removeValue(new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            ivFavorite.setImageResource(R.drawable.ic_favorite_gray_32dp);
                            isFavorited = false;
                            Toast.makeText(getContext(), "Removed from Favorites!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }else{
                    try {
                        accountReference.child("USERS").child(sharedPrefUtils.getUserId())
                                .child("FAVORITES").child(mArrDogKeys.get(position)).setValue("", new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                ivFavorite.setImageResource(R.drawable.ic_favorite_pink_32dp);
                                isFavorited = true;
                                Toast.makeText(getContext(), "Added to Favorites!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        });
    }

    private void onClickSubmitRating(Button btnSubmitRating, final Dog currentDog, final TextView tvOverallRating, final RatingBar ratingBar){
        btnSubmitRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String dogKey = mArrDogKeys.get(position);
                Dog.getSpecificDog(getContext(), dogKey, new OnGetDataListener() {
                    @Override
                    public void onSuccess(DataSnapshot dataSnapshot) {
                        Dog.removeSpecificDogNodeValueEventListener(dogKey);
                        Dog dog = (Dog) dataSnapshot.getValue(Dog.class);
                        int oldStarsAccrued = dog.getStarsAccrued();
                        int oldStarsPossible = dog.getStarsPossible();
                        submitRating(dogKey, ratingBar, tvOverallRating, oldStarsAccrued, oldStarsPossible);
                    }
                });


            }
        });
    }

    private void submitRating(final String dogKey, final RatingBar ratingBar, final TextView tvOverallRating,
                              final int oldStarsAccrued, final int oldStarsPossible){

        final DatabaseReference accountReference = FirebaseDatabase.getInstance().getReference();
        Dog.getSpecificRating(getContext(), dogKey, new OnGetDataListener() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                Dog.removeSpecificRatingNodeValueEventListener(getContext(), dogKey);

                int oldRating;
                try {
                    oldRating = (int) dataSnapshot.getValue(Integer.class);
                }catch(NullPointerException e){
                    //user hasn't set any ratings yet
                    System.out.println("Couldn't locate the rating");
                    e.printStackTrace();
                    oldRating = -1;
                }
                int newRating = (int)ratingBar.getRating();
                try {
                    SharedPrefUtils sharedPrefUtils = new SharedPrefUtils(getContext());
                    //replace the rating of the dog in the user's ratings node with the new rating
                    accountReference.child("USERS").child(sharedPrefUtils.getUserId())
                            .child("RATINGS").child(dogKey).setValue(newRating);

                    //here the user is setting a rating for the first time
                    int newStarsAccrued;
                    int newStarsPossible;
                    if(oldRating<0) {
                        newStarsAccrued = oldStarsAccrued+newRating;
                        newStarsPossible = oldStarsPossible + 5;
                        accountReference.child("DOGS").child(dogKey)
                                .child("starsAccrued").setValue(newStarsAccrued);
                        accountReference.child("DOGS").child(dogKey)
                                .child("starsPossible").setValue(newStarsPossible);
                        Toast.makeText(getContext(), "Rating submitted!", Toast.LENGTH_SHORT).show();
                    }else{  // here the user has already set a rating for this dog, so we adjust it
                        newStarsAccrued = oldStarsAccrued+(newRating-oldRating);
                        newStarsPossible = oldStarsPossible;
                        accountReference.child("DOGS").child(dogKey)
                                .child("starsAccrued").setValue(newStarsAccrued);
                        Toast.makeText(getContext(), "Rating updated!", Toast.LENGTH_SHORT).show();
                    }
                    double overallRating = (double) newStarsAccrued/newStarsPossible * 5;
                    tvOverallRating.setText("Overall Rating: " + round(overallRating,1));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        System.out.println("on activity created");
    }


}
