package com.example.worddart;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

public class GameActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "STATUS_GAME";
    Intent intent;
    String[] Mode;
    String MODE_OFFLINE,MODE_ONLINE,MODE_SOLO,MODE_AI,MODE_TIMED,MODE_ELIMINATION;
    Button btnAdd,btnSub;
    TextView tvTimer,tvMode;

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
        btnAdd=(Button)findViewById(R.id.btAdd);
        btnSub=(Button)findViewById(R.id.btSub);
        btnAdd.setOnClickListener(this);
        btnSub.setOnClickListener(this);
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

                break;
        }
    }
}

