package com.hitesh.marvelapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

public class UserSection extends AppCompatActivity {

    Button upload,video,logout;
    Uri videoUri;
    static final int REQUEST_VIDEO_CAPTURE = 1;
    StorageReference videoRef;
    String userID;
    private FirebaseAuth firebaseAuth;
    ProgressBar progressBar;
    long maxid;
    private DatabaseReference refinsert;
    Video_Database video_database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_section);
        firebaseAuth = FirebaseAuth.getInstance();
        progressBar = (ProgressBar) findViewById(R.id.pbar);
        upload = findViewById(R.id.Upload);
        video = findViewById(R.id.VIEW);
        logout = findViewById(R.id.logout);
        Intent intent = getIntent();
        userID = intent.getStringExtra("userid");
        video_database = new Video_Database("","");

        videoRef = FirebaseStorage.getInstance().getReference().child("users_database").child(userID);

        refinsert = FirebaseDatabase.getInstance().getReference().child("users_database").child(userID);
        refinsert.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    maxid = dataSnapshot.getChildrenCount();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }

        });


        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                videoUploader();

            }
        });

        video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i1 = new Intent(UserSection.this, VideoLibrary.class);
                i1.putExtra("userid",userID);
                startActivity(i1);
                finish();

            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                firebaseAuth.signOut();
                startActivity(new Intent(UserSection.this, MainActivity.class));
                Toast.makeText(getApplicationContext(), "Logged out", Toast.LENGTH_LONG).show();

            }
        });
    }


    private void videoUploader() {

        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_VIDEO_CAPTURE) {
            videoUri = data.getData();
            if(resultCode == RESULT_OK){

            //videoView.setVideoURI(videoUri);
            if(videoUri != null){
                StorageReference Ref = videoRef.child("Video"+String.valueOf(maxid + 1)+".3gp");
                UploadTask uploadTask = Ref.putFile(videoUri);
                long progress = (100 * 0);
                String url = "";//Ref.getDownloadUrl().toString();
                progressBar.setProgress((int) progress);

                uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                        updateProgress(taskSnapshot);

                    }
                });

                StorageTask <UploadTask.TaskSnapshot> snapshotStorageTask;
                snapshotStorageTask = Ref.putFile(videoUri);
                snapshotStorageTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return Ref.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(UserSection.this, "Upload Complete", Toast.LENGTH_SHORT).show();
                            Uri downloadUri = task.getResult();
                            String url = downloadUri.toString();
                            video_database.setTitle("Video"+String.valueOf(maxid + 1));
                            video_database.setUrl(url);
                            refinsert.child(String.valueOf(maxid + 1)).setValue(video_database);
                        }
                    }
                });

            }else{
                Toast.makeText(UserSection.this, "Nothing to Upload", Toast.LENGTH_SHORT).show();

            }}else if(resultCode == RESULT_CANCELED){
                Toast.makeText(UserSection.this, "Video recording cancelled", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(UserSection.this, "Failed to record Video", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private  void updateProgress(UploadTask.TaskSnapshot taskSnapshot){

        long fileSize = taskSnapshot.getTotalByteCount();
        long uploadBytes = taskSnapshot.getBytesTransferred();
        long progress = (100 * uploadBytes)/fileSize;

        progressBar.setProgress((int) progress);

    }

    @Override
    public void onBackPressed() {

        new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Exit")
                .setMessage("Are you sure you want to close the application?")
                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("EXIT", true);
                        startActivity(intent);
                        finishAffinity();
                    }
                }).setNegativeButton("no", null).show();
    }


}
