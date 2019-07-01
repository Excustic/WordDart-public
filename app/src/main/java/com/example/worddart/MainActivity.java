package com.example.worddart;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ShareCompat;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {
    private static final int RC_SIGN_IN =9000 ;
    private static final String TAG = "STATUS_MAIN";
    Button btnPlay,btnStats,btnSettings;
    SignInButton btnGoogle;
    TextView dispName;
    Intent intent;
    GoogleSignInClient mGoogleSignInClient;
    CircleImageView profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        intent=new Intent();

        profile=(CircleImageView)findViewById(R.id.circleImageView);
        dispName=(TextView)findViewById(R.id.dispName);
        btnPlay=(Button)findViewById(R.id.btnPlay);
        btnStats=(Button)findViewById(R.id.btnStats);
        btnSettings=(Button)findViewById(R.id.btnSettings);
        btnGoogle=(SignInButton)findViewById(R.id.sign_in_button);

        btnPlay.setOnClickListener(this);
        btnStats.setOnClickListener(this);
        btnSettings.setOnClickListener(this);
        btnGoogle.setOnClickListener(this);

        btnPlay.setOnTouchListener(this);
        btnStats.setOnTouchListener(this);
        btnSettings.setOnTouchListener(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        updateUI(account);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            case R.id.btnPlay:
                intent.setClass(this, PlayMenu.class);
                startActivity(intent);
                break;
            case R.id.btnStats:
                break;
            case R.id.btnSettings:
                intent.setClass(this, SettingsActivity.class);
                startActivity(intent);
                break;
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if(MotionEvent.ACTION_DOWN==motionEvent.getAction()) {
            switch (view.getId()) {
                case R.id.btnPlay:
                    btnPlay.setBackgroundColor(getResources().getColor(R.color.colorSecondary));
                    break;
                case R.id.btnStats:
                    btnStats.setBackgroundColor(getResources().getColor(R.color.colorSecondary));
                    break;
                case R.id.btnSettings:
                    btnSettings.setBackgroundColor(getResources().getColor(R.color.colorSecondary));
                    break;
            }
        }
        else
        {
            switch (view.getId()) {
                case R.id.btnPlay:
                    btnPlay.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    break;
                case R.id.btnStats:
                    btnStats.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    break;
                case R.id.btnSettings:
                    btnSettings.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    break;
            }
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }

    public void updateUI(GoogleSignInAccount account)
    {
        if(account==null)
        {
            btnGoogle.setVisibility(View.VISIBLE);
        }
        else
        {
            profile.setImageURI(account.getPhotoUrl());
            profile.setBorderColor(Color.WHITE);
            profile.setBorderWidth(3);
            dispName.setText(account.getDisplayName());
        }
    }
}
