package com.hitesh.marvelapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.hitesh.marvelapp.Phone_Login.PhoneVerification;


public class MainActivity extends AppCompatActivity {

    ViewPager viewPager;
    private FirebaseAuth firebaseAuth;
    private ImageButton phone, amazon;
    String first = "", email = "", mobile = "";
    private static int SPLASH_SCREEN_TIME_OUT = 2000;

    private String userID;

    //a constant for detecting the login intent result
    private static final int RC_SIGN_IN = 234;

    //Tag for the logs optional
    private static final String TAG = "Login";

    //creating a GoogleSignInClient object
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);


        firebaseAuth = FirebaseAuth.getInstance();
        phone = (ImageButton) findViewById(R.id.phone);

        //Then we need a GoogleSignInOptions object
        //And we need to build it as below
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("375385075035-hgm2ssq2sfe2uturn1g21kuclotgmg6d.apps.googleusercontent.com")
                .requestEmail()
                .build();

        //Then we will get the GoogleSignInClient object from GoogleSignIn class

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        if (firebaseAuth.getCurrentUser() != null) {

            first = firebaseAuth.getCurrentUser().getDisplayName();
            email = firebaseAuth.getCurrentUser().getEmail();
            mobile = firebaseAuth.getCurrentUser().getPhoneNumber();
            userID = firebaseAuth.getCurrentUser().getUid();
            Intent i1 = new Intent(MainActivity.this, UserSection.class);
            i1.putExtra("email", email);
            i1.putExtra("name", first);
            i1.putExtra("mobile", mobile);
            i1.putExtra("userid", userID);
            startActivity(i1);
            finish();

        } else {
        phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PhoneVerification.class);
               // intent.putExtra("userid", userID);
                startActivity(intent);
                finish();
            }
        });
        findViewById(R.id.google).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                signIn();


            }
        });
    }


    }



    //this method is called on click
    private void signIn() {
        //getting the google signin intent
        Intent signInIntent =  mGoogleSignInClient.getSignInIntent();

        //starting the activity for result
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //if the requestCode is the Google Sign In code that we defined at starting
        if (requestCode == RC_SIGN_IN) {

            //Getting the GoogleSign_In Task
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                //Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                account.hashCode();
                firebaseAuthWithGoogle(account);

                //authenticating with firebase
            } catch (ApiException e) {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d(TAG, e.getMessage());
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        //getting the auth credential
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        // else{

        //Now using firebase we are signing in the user here
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            userID = user.getUid();
                            first = user.getDisplayName();
                            email = user.getEmail();
                            mobile = user.getPhoneNumber();

                            Toast.makeText(MainActivity.this, "User Signed In", Toast.LENGTH_SHORT).show();
                            Intent i1 = new Intent(MainActivity.this, InstructionsActivity.class);
                            i1.putExtra("email", email);
                            i1.putExtra("name", first);
                            i1.putExtra("mobile", mobile);
                            i1.putExtra("userid", userID);
                            startActivity(i1);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();

                        }

                        // ...
                    }
                });
        //}
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Exit")
                .setMessage("Are you sure you want to close the application? Your Progress won't be saved")
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
