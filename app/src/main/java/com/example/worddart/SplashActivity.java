package com.example.worddart;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class SplashActivity extends AppCompatActivity {
private static final int SPLASH_TIME=2000;
Animation anim_load;
ImageView circle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        circle=(ImageView)findViewById(R.id.Circle);

    }
    protected void onStart()
    {
        super.onStart();
        anim_load= AnimationUtils.loadAnimation(this,R.anim.rotate);
        circle.startAnimation(anim_load);
        updateUI();
    }
    public void updateUI()
    {
//            try {
//                FirebaseUser f= (FirebaseUser) readObject(this, "fbU");
//            } catch (IOException e) {
//                e.printStackTrace();
//                SaveUser();
//            } catch (ClassNotFoundException e) {
//                e.printStackTrace();
//            }
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                Intent MainIntent=new Intent();
                MainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                MainIntent.setClass(SplashActivity.this, MainActivity.class);
                startActivity(MainIntent);
                finish();
            }
        }, SPLASH_TIME);
        // GoogleSign.setVisibility(View.INVISIBLE);
        // AnonSign.setVisibility(View.INVISIBLE);
    }
}
