package com.rood.whatschat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;


public class SettingsActivity extends AppCompatActivity {

    private TextView mDisplayName;
    private TextView mStatus;
    private CircleImageView mProfileImage;
    private DatabaseReference mDatabase;
    private Button mStatusBtn;
    private Button mImageBtn;
    private String current_status;
    private ProgressDialog pd;
    private StorageReference mStorageImage;
    private StorageReference mStorageThumb;

    private FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mDisplayName = findViewById(R.id.setting_display_name);
        mStatus = findViewById(R.id.setting_status);
        mStatusBtn = findViewById(R.id.setting_status_btn);
        mImageBtn = findViewById(R.id.setting_change_image_btn);
        mProfileImage = findViewById(R.id.setting_profile_image);

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = mUser.getUid();



        // STATUS BUTTON ONCLICK
        mStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent statusIntent = new Intent(SettingsActivity.this, StatusActivity.class);
                statusIntent.putExtra("current_status", current_status);
                startActivity(statusIntent);
            }
        });

        // IMAGE BUTTON ONCLICK
        mImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CropImage.activity()
                        .setAspectRatio(1,1)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(SettingsActivity.this);

            }
        });

        // SET STATUS AND IMAGE VIEW USING FB DB
        //Query
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        mDatabase.keepSynced(true);  // for OFFLINE

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String text_name = snapshot.child("name").getValue().toString();
                String text_status = snapshot.child("status").getValue().toString();
                String imageURL = snapshot.child("image").getValue().toString();  // Image URL not actual image

                mDisplayName.setText(text_name);
                mStatus.setText(text_status);

                // Load Image
                final String imageUri = imageURL;
                if (!imageUri.equals("default")){

                    //Picasso.get().load(imageUri).placeholder(R.drawable.harry).into(mProfileImage);

                    Picasso.get().load(imageUri).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.harry).into(mProfileImage, new Callback() {
                        @Override
                        public void onSuccess() {
                            // mean successfully loaded OFFLINE -> do nothing
                        }

                        @Override
                        public void onError(Exception e) {

                            // Image OFFLINE not available
                            Picasso.get().load(imageUri).placeholder(R.drawable.harry).into(mProfileImage);

                        }
                    });


                }

                current_status = text_status;
                /*
                Log.i("SNAPSHOT", snapshot.toString()); // ALL DATA
                Log.i("SNAPSHOT KEY",snapshot.getKey()); // UID
                 */
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                Uri imageUri = result.getUri();

                //// UPLOAD
                uploadImageFile(imageUri);  // Upload will result in change of imageView

                ////  UPLOAD THUMB
                uploadThumb(imageUri);


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void uploadThumb(Uri imageUri) {

        // Storage Reference
        mStorageThumb = FirebaseStorage.getInstance().getReference().child("thumb_images").child(mUser.getUid()+".jpg");

        // Create FILE from Uri
        File actualImageFile = new File(imageUri.getPath());

        //File compressedImageFile = new Compressor(this).compressToFile(actualImageFile);

        // Store as Bitmap to upload in Firebase Storage
        Bitmap compressedImageBitmap = null;
        try {
            compressedImageBitmap = new Compressor(this)
                    .setMaxHeight(200)
                    .setMaxWidth(200)
                    .setQuality(75)
                    .compressToBitmap(actualImageFile);

            // Byte conversion
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            compressedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] thumb_byte = baos.toByteArray();

            UploadTask uploadTask = mStorageThumb.putBytes(thumb_byte);

            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return mStorageThumb.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();

                        // Update DB
                        mDatabase.child("thumb_image").setValue(downloadUri.toString());
                    } else {
                        // Handle failures
                        // ...
                    }
                }
            });


        } catch (IOException e) {
            Log.e("FAILED!", "COMPRESSION FAILED");
            e.printStackTrace();
        }


    }

    private void uploadImageFile(final Uri imageUri) {
        // Set PROGRESS DIALOG
        pd = new ProgressDialog(this);
        pd.setTitle("Updating");
        pd.setMessage("Profile is being updated..");
        pd.show();

        // Image Filename
        String filename = mUser.getUid()+".jpg";

        // Storage Reference
        mStorageImage = FirebaseStorage.getInstance().getReference().child("profile_images").child(filename); // Profile_images/UniqueNumber/
        // Database Reference
         final DatabaseReference imageDB = FirebaseDatabase.getInstance().getReference().child("Users").child(mUser.getUid()).child("image");

        // PUT FILE -> Handle Success and Failure
        mStorageImage.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                pd.dismiss();

                // GET Download URL
                mStorageImage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String DownloadURL = uri.toString();

                        //Set CircleImageView
                        //mProfileImage.setImageURI(imageUri);

                        // Put Image URL in DB
                        imageDB.setValue(DownloadURL);

                        Log.i("SUCCESS", "");
                        Toast.makeText(SettingsActivity.this, "Upload Successful!", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {

                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Log.e("FAILED", "Could Not Download the image URL");

                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {

            @Override
            public void onFailure(@NonNull Exception e) {

                pd.dismiss();

                Log.e("FAILED", "Could Not PUT file");
                Toast.makeText(SettingsActivity.this, "Something went Wrong!", Toast.LENGTH_LONG).show();
            }
        });

    }
}