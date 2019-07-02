package com.example.worddart;

import androidx.appcompat.app.AppCompatActivity;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.FlingAnimation;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import org.w3c.dom.Text;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

public class GameActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "STATUS_GAME";
    Intent intent;
    String[] Mode;
    String MODE_OFFLINE,MODE_ONLINE,MODE_SOLO,MODE_AI,MODE_TIMED,MODE_ELIMINATION;
    final long INTERVAL_MILLIS=5*60*1000, MAX_MILLIS=85*60*1000,MIN_MILLIS=INTERVAL_MILLIS;
    TextView tvTimer,tvMode;
    Button btnAdd,btnSub,btnStart;
    Animation fadeOut;
    CountDownTimer timer;
    long timeLeftInMillis;
    Boolean timerIsRun;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        intent = getIntent();
        Mode = intent.getStringArrayExtra("MODE");
        assert Mode != null;
        Log.d(TAG, "Mode: " + Mode[0] + "_" + Mode[1] + "_" + Mode[2]);
        MODE_OFFLINE = getResources().getResourceName(R.string.OFFLINE_MODE);
        MODE_ONLINE = getResources().getResourceName(R.string.ONLINE_MODE);
        MODE_SOLO = getResources().getResourceName(R.string.OFFLINE_SOLO_MODE);
        MODE_AI = getResources().getResourceName(R.string.OFFLINE_AI_MODE);
        MODE_TIMED = getResources().getResourceName(R.string.GAMEMODE_TIMED);
        MODE_ELIMINATION = getResources().getResourceName(R.string.GAMEMODE_ELIMINATION);
        timeLeftInMillis=0;
        tvTimer=(TextView)findViewById(R.id.tvTimer);
        tvMode=(TextView)findViewById(R.id.TITLE_MODE);
        tvMode.setText(Mode[2]);
        btnAdd=(Button)findViewById(R.id.btAdd);
        btnSub=(Button)findViewById(R.id.btSub);
        btnStart=(Button)findViewById(R.id.btnStart);
        btnAdd.setOnClickListener(this);
        btnSub.setOnClickListener(this);
        btnStart.setOnClickListener(this);
        fadeOut= AnimationUtils.loadAnimation(this,R.anim.fadeout);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Mode[0] == MODE_OFFLINE) {
            if(Mode[1]==MODE_SOLO)
            {
                if(Mode[2]==MODE_TIMED)
                {

                }
                else if(Mode[2]==MODE_ELIMINATION)
                {

                }
            }
            else if(Mode[1]==MODE_AI)
            {
                if(Mode[2]==MODE_TIMED)
                {

                }
                else if(Mode[2]==MODE_ELIMINATION)
                {

                }
            }
        }
        else if(Mode[0]==MODE_ONLINE)
        {

        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.btAdd:
                if(timeLeftInMillis<=MAX_MILLIS)
                timeLeftInMillis+=INTERVAL_MILLIS;
                updateTimer();
                break;
            case R.id.btSub:
                if(timeLeftInMillis>MIN_MILLIS)
                    timeLeftInMillis-=INTERVAL_MILLIS;
                updateTimer();
                break;
            case R.id.btnStart:
                AnimateFadeOut();
                startTimer();
                break;
        }
    }
public void AnimateFadeOut()
{
    FlingAnimation fling = new FlingAnimation(btnStart, DynamicAnimation.TRANSLATION_Y);
    btnStart.startAnimation(fadeOut);
    btnSub.startAnimation(fadeOut);
    btnAdd.startAnimation(fadeOut);
    fling.setStartVelocity(2000)
            .setMaxValue(2000)
            .setMinValue(-2000)
            .setFriction(0.3f)
            .start();
    Snackbar.make(findViewById(R.id.GameLayout), R.string.StartMessage,
            Snackbar.LENGTH_LONG)
            .show();

}

public void startCountDown()
{
    timeLeftInMillis=Integer.parseInt(tvTimer.getText().toString().split("m")[0])*60*1000;
    timer=new CountDownTimer(timeLeftInMillis+1,1000) {
        @Override
        public void onTick(long l) {
            timeLeftInMillis=l;
            updateTimer();
        }

        @Override
        public void onFinish() {

        }
    }.start();
    timerIsRun=true;
}
public void stopTimer()
{
    timer.cancel();
    timerIsRun=false;
}
public void updateTimer()
{
    int minutes= (int)timeLeftInMillis/60000;
    int seconds=(int)timeLeftInMillis%60000/1000;
    String setTime=String.format(Locale.getDefault(),"%02d"+"m "+"%02d"+"s", minutes, seconds);
    tvTimer.setText(setTime);
}
//Used for delaying the timer, giving the user some time to get ready
    private void startTimer(){
        timer=new CountDownTimer(4000,10) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                startCountDown();
            }
        }.start();
    }

}

