package com.hitesh.marvelapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.widget.VideoView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

public class VideoLibrary extends AppCompatActivity {
    StorageReference videoRef;
    String userID;
    private FirebaseAuth firebaseAuth;
    RecyclerView mRecyclerView;
    FirebaseDatabase database;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_library);
        firebaseAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        userID = intent.getStringExtra("userid");

        videoRef = FirebaseStorage.getInstance().getReference().child("users_database").child(userID);
        mRecyclerView = findViewById(R.id.recycler_view_s);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        reference = FirebaseDatabase.getInstance().getReference().child("users_database").child(userID);





    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Video_Database, ViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Video_Database, ViewHolder>
                (Video_Database.class, R.layout.custom_cardlayout, ViewHolder.class, reference) {
            @Override
            protected void populateViewHolder(ViewHolder viewHolder, Video_Database database, int i) {

                viewHolder.setVideo(getApplication(),database.getTitle(),database.getUrl());
            }

        };
        mRecyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    @Override
    public void onBackPressed() {
        Intent i1 = new Intent(VideoLibrary.this, UserSection.class);
        i1.putExtra("userid", userID);
        startActivity(i1);
        finish();
    }
}
