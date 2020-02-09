package com.example.bolopakistan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity
{
    private Button UpdateAccountSettings;
    private EditText userName, userStatus;
    private CircleImageView userProfileImage;

    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    private static final int GalleryPick = 1;
    private StorageReference UserProfileImagesRef , mStorageReference;
    private ProgressDialog loadingBar;

    private Toolbar SettingsToolBar;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        UserProfileImagesRef = FirebaseStorage.getInstance().getReference();
        mStorageReference = FirebaseStorage.getInstance().getReference();


        InitializeFields();


        userName.setVisibility(View.INVISIBLE);


        UpdateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                UpdateSettings();
            }
        });


        RetrieveUserInfo();


        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                //Using an Intent to get all the images from the gallery.
                Intent galleryIntent = new Intent();

                //Setting up the path of the image source.
                galleryIntent.setType("image/*");

                //Getting the content(images) from the above path.
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                //Returning the Activity with a result(selected Image).
                startActivityForResult(Intent.createChooser(galleryIntent , "SELECT IMAGE") , GalleryPick);
            }
        });
    }



    private void InitializeFields()
    {
        UpdateAccountSettings = (Button) findViewById(R.id.update_settings_button);
        userName = (EditText) findViewById(R.id.set_user_name);
        userStatus = (EditText) findViewById(R.id.set_profile_status);
        userProfileImage = (CircleImageView) findViewById(R.id.set_profile_image);
        loadingBar = new ProgressDialog(this);

        SettingsToolBar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(SettingsToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Account Settings");
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Checking if the selected image is true or not and the result is Ok or not.
        if (requestCode == GalleryPick && resultCode == RESULT_OK){

            //Getting the Data of the Image and saving it in a Uri.
            Uri imageUri = data.getData();

            //Instantiating the cropImage feature and setting the ratio in 1:1.
            CropImage.activity(imageUri).setAspectRatio(1 , 1)
                    .start(SettingsActivity.this);

        }

        //Checking if the image is cropped or not.
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            //Checking if the result is Ok or not, if yes we will store the image in a uri.
            if (resultCode == RESULT_OK) {
                loadingBar.setTitle("Set Profile Image");
                loadingBar.setMessage("Please wait, your profile image is updating...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();
                Uri resultUri = result.getUri();
                //Getting the Current UID of the User and storing it in a String.
                final String uid_img = FirebaseAuth.getInstance().getCurrentUser().getUid();

                //Saving the image in the Firebase Storage and naming the child with the UID.
                final StorageReference filepath = UserProfileImagesRef.child("Profile_Images").child(uid_img+".jpg");
                //If the resultUri is nor Empty or NULL.
                if (resultUri != null) {
                    //We Will setup an OnCompleteListener to store the image in the desired location in the storage.
                    filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {
                            //If the task is Successful we will display a toast.
                            if (task.isSuccessful()){

                                UserProfileImagesRef.child("Profile_Images").child(uid_img+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String downloadUrl = uri.toString();
                                        RootRef.child("Users").child(currentUserID).child("image")
                                                .setValue(downloadUrl)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task)
                                                    {
                                                        if (task.isSuccessful())
                                                        {
                                                            Toast.makeText(SettingsActivity.this, "Image save in Database, Successfully...", Toast.LENGTH_SHORT).show();
                                                            loadingBar.dismiss();
                                                        }
                                                        else
                                                        {
                                                            String message = task.getException().toString();
                                                            Toast.makeText(SettingsActivity.this, "Error:1 " + message, Toast.LENGTH_SHORT).show();
                                                            loadingBar.dismiss();
                                                        }
                                                    }
                                                });

                                    }
                                });


                            }else {

                                String message = task.getException().toString();
                                Toast.makeText(SettingsActivity.this, "Error:2 " + message, Toast.LENGTH_LONG).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
                }

                //If the task is not successful then we will display an Error Message.
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();

            }
        }

    }





    private void UpdateSettings()
    {
        String setUserName = userName.getText().toString();
        String setStatus = userStatus.getText().toString();

        if (TextUtils.isEmpty(setUserName))
        {
            Toast.makeText(this, "Please write your user name first....", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(setStatus))
        {
            Toast.makeText(this, "Please write your status....", Toast.LENGTH_SHORT).show();
        }
        else
        {
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("uid", currentUserID);
            profileMap.put("name", setUserName);
            profileMap.put("status", setStatus);
            RootRef.child("Users").child(currentUserID).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if (task.isSuccessful())
                            {
                                SendUserToMainActivity();
                                Toast.makeText(SettingsActivity.this, "Profile Updated Successfully...", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                String message = task.getException().toString();
                                Toast.makeText(SettingsActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }



    private void RetrieveUserInfo()
    {
        RootRef.child("Users").child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name") && (dataSnapshot.hasChild("image"))))
                        {
                            String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                            String retrievesStatus = dataSnapshot.child("status").getValue().toString();
                            String retrieveProfileImage = dataSnapshot.child("image").getValue().toString();

                            userName.setText(retrieveUserName);
                            userStatus.setText(retrievesStatus);
                            Picasso.get().load(retrieveProfileImage).into(userProfileImage);
                        }
                        else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")))
                        {
                            String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                            String retrievesStatus = dataSnapshot.child("status").getValue().toString();

                            userName.setText(retrieveUserName);
                            userStatus.setText(retrievesStatus);
                        }
                        else
                        {
                            userName.setVisibility(View.VISIBLE);
                            Toast.makeText(SettingsActivity.this, "Please set & update your profile information...", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }



    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}