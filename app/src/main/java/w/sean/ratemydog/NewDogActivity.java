package w.sean.ratemydog;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import w.sean.ratemydog.POJOs.Dog;
import w.sean.ratemydog.Utils.FirebaseUtils;
import w.sean.ratemydog.Utils.SharedPrefUtils;
import w.sean.ratemydog.Utils.ToolbarUtils;

import static android.os.Environment.DIRECTORY_PICTURES;
import static android.os.Environment.getExternalStoragePublicDirectory;

public class NewDogActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 100;
    private static final int SELECT_PHOTO = 101;
    private ImageView ivDogPreview;
    private Bitmap bitDog;
    private static final String PHOTO_FILE = "photo_file";
    private static final String TAG = "NewDogActivity";
    private File currentPhotoFile;
    private ProgressBar progressBar;
    private Button btnSubmit;
    private ProgressBar pbSubmit;

    private final FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
    private final DatabaseReference mDatabaseReference = mFirebaseDatabase.getReference();
    private final FirebaseStorage mStorage = FirebaseStorage.getInstance();
    private final StorageReference storageRef = mStorage.getReference().child("DOG_PICS/");

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_dog);

        ivDogPreview = findViewById(R.id.iv_dog_preview);
        progressBar = findViewById(R.id.progress_bar);
        btnSubmit = (Button) findViewById(R.id.btn_submit);
        pbSubmit = findViewById(R.id.pb_submit);

        if(savedInstanceState != null){
            if(savedInstanceState.getSerializable(PHOTO_FILE) != null) {
                progressBar.setVisibility(View.VISIBLE);
                currentPhotoFile = (File)savedInstanceState.getSerializable(PHOTO_FILE);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            orientImage(currentPhotoFile);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setVisibility(View.GONE);
                                    ivDogPreview.setVisibility(View.VISIBLE);
                                    ivDogPreview.setImageBitmap(bitDog);
                                }
                            });
                        }catch (FileNotFoundException e){
                            e.printStackTrace();
                        }
                    }
                }).start();

            }
        }
        setupToolbar(ToolbarUtils.NO_ICON, "Add Dog");
        onClickTakePicture();
        onClickChoosePicture();
        onClickSubmit();
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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void onClickTakePicture(){
        findViewById(R.id.btn_take_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(NewDogActivity.this, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_DENIED){
                    ActivityCompat.requestPermissions(NewDogActivity.this, new String[] {Manifest.permission.CAMERA},
                            REQUEST_IMAGE_CAPTURE);
                }else {
                    try{
                        dispatchTakePictureIntent();
                    }catch (IOException e){
                        e.printStackTrace();
                    }

                }
            }
        });
    }

    private void dispatchTakePictureIntent() throws IOException{
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            currentPhotoFile = null;
            try {
                currentPhotoFile = createImageFile();
            } catch (IOException ex) {
                System.out.println("Error occurred while creating the File");
                return;
            }
            if (currentPhotoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        currentPhotoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }else{
            Toast.makeText(this, "Cannot take picture. Make sure you have a camera app downloaded.", Toast.LENGTH_LONG).show();
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        currentPhotoFile = image;
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            if(this.SELECT_PHOTO == requestCode){
                ivDogPreview.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                //on a background thread, create a new file, transfer the bitmap stream from the gallery file to the new file,
                //then retrieve the bitmap from the new file and orient it properly. Finally set the image on the ui thread.
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            try {
                                currentPhotoFile = createImageFile();
                            } catch (IOException e) {
                                Log.d(TAG, "Error occurred while creating the file");
                                e.printStackTrace();
                            }

                            InputStream inputStream = NewDogActivity.this.getContentResolver().openInputStream(data.getData());
                            FileOutputStream fileOutputStream = new FileOutputStream(currentPhotoFile);
                            // Copying
                            copyStream(inputStream, fileOutputStream);
                            fileOutputStream.close();
                            inputStream.close();

                            orientImage(currentPhotoFile);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setVisibility(View.GONE);
                                    ivDogPreview.setVisibility(View.VISIBLE);
                                    ivDogPreview.setImageBitmap(bitDog);
                                }
                            });
                        } catch (Exception e) {
                            Log.d(TAG, "onActivityResult: " + e.toString());
                        }
                    }
                }).start();
            }
            else if (this.REQUEST_IMAGE_CAPTURE == requestCode) {
                // retrieve the bitmap from the file, orient it properly, then set the bitmap in the image view.
                try {
                    ivDogPreview.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                    orientImage(currentPhotoFile);
                    progressBar.setVisibility(View.GONE);
                    ivDogPreview.setVisibility(View.VISIBLE);
                    ivDogPreview.setImageBitmap(bitDog);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void copyStream(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
    }

    private void orientImage(File file) throws FileNotFoundException{
        int orientation = ExifInterface.ORIENTATION_NORMAL;
        try {
            InputStream ims = new FileInputStream(file);
            bitDog = BitmapFactory.decodeStream(ims);

            ExifInterface ei = new ExifInterface(file.getAbsolutePath().toString());
            orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);
            System.out.println("orientation " + orientation);
        }catch(IOException e){
            e.printStackTrace();
        }
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                Log.i(TAG, "Roatation 90 degrees");
                bitDog = rotateBitmap(bitDog, 90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                Log.i(TAG, "Roatation 180 degrees");
                bitDog = rotateBitmap(bitDog, 180);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                Log.i(TAG, "Roatation 270 degrees");
                bitDog = rotateBitmap(bitDog, 270);
                break;
            default:
                System.out.println("default");
                break;
        }
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private void onClickChoosePicture(){
        findViewById(R.id.btn_choose_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, SELECT_PHOTO);
            }
        });
    }

    private void onClickSubmit(){
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSubmit.setEnabled(false);
                addDogToFirebase(getNewDog());
            }
        });
    }

    private void addDogToFirebase(final Dog newDog){
        if(newDog.getName().equals("")){
            findViewById(R.id.iv_required).setVisibility(View.VISIBLE);
            btnSubmit.setEnabled(true);
            Toast.makeText(NewDogActivity.this, "Name field is required.", Toast.LENGTH_LONG).show();
            return;
        }
        if(bitDog==null){
            Toast.makeText(NewDogActivity.this, "Make sure you have uploaded a picture.", Toast.LENGTH_LONG).show();
            btnSubmit.setEnabled(true);
            return;
        }
        pbSubmit.setVisibility(View.VISIBLE);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitDog.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] byteArray = baos.toByteArray();

        //upload the user profile picture to firebase storage
        final String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        FirebaseStorage.getInstance().setMaxUploadRetryTimeMillis(3000);
        UploadTask uploadTask = storageRef.child("Dog:"+timeStamp).putBytes(byteArray);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                pbSubmit.setVisibility(View.GONE);
                btnSubmit.setEnabled(true);
                exception.printStackTrace();
                Toast.makeText(NewDogActivity.this,
                        "Error uploading data. You may not be connected to the internet.",
                        Toast.LENGTH_LONG).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                storageRef.child("Dog:"+timeStamp).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        //image successfully loaded. Now get the url of the image and add it
                        //to the account details object we want to push to firebase database
                        Log.i(TAG, "Image loaded successfully");
                        newDog.setPicLocation(uri.toString());
                        //add dog to big list of dogs in firebase
                        String dogId = "Dog:"+timeStamp;
                        mDatabaseReference.child("DOGS").child(dogId).setValue(newDog);
                        SharedPrefUtils sharedPref = new SharedPrefUtils(NewDogActivity.this);
                        String userId = sharedPref.getUserId();
                        Dog dogRef = new Dog();
                        //add dog to "my dogs" node of current user's node in firebase
                        mDatabaseReference.child("USERS").child(userId).child("MY DOGS").child(dogId).setValue("");

                        pbSubmit.setVisibility(View.GONE);
                        Toast.makeText(NewDogActivity.this, getResources().getString(R.string.dog_submitted), Toast.LENGTH_LONG).show();
                        deleteCurrentFile();
                        exitActivity();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        pbSubmit.setVisibility(View.GONE);
                        btnSubmit.setEnabled(true);
                        Toast.makeText(NewDogActivity.this,
                                "Error uploading data. You may not be connected to the internet.",
                                Toast.LENGTH_LONG).show();
                        exception.printStackTrace();
                    }
                });
            }
        });
    }

    private Dog getNewDog(){
        EditText etName = findViewById(R.id.et_name);
        EditText etAge = findViewById(R.id.et_age);
        EditText etBreed = findViewById(R.id.et_breed);
        EditText etHobbies = findViewById(R.id.et_hobbies);
        EditText etDislikes = findViewById(R.id.et_dislikes);

        String name = etName.getText().toString();
        String age = etAge.getText().toString();
        String breed = etBreed.getText().toString();
        String hobbies = etHobbies.getText().toString();
        String dislikes = etDislikes.getText().toString();

        return new Dog(name, age, breed, hobbies, dislikes, "", 0, 0);

    }

    private void deleteCurrentFile(){
        if (currentPhotoFile !=null && currentPhotoFile.exists()) {
            if (currentPhotoFile.delete()) {
                Log.i(TAG, "File deleted: " + currentPhotoFile);
            } else {
                Log.i(TAG, "File not deleted: " + currentPhotoFile);
            }
        }
    }

    private void exitActivity(){
        Intent i = new Intent(NewDogActivity.this, MyPicsActivity.class);
        startActivity(i);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(PHOTO_FILE, currentPhotoFile);
        System.out.println("saved file: " + currentPhotoFile);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy(){

        super.onDestroy();
    }
}
