package com.example.worddart;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class PlayMenu extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {
    Button[] btn;
    Dialog d;
    ImageButton ibTimed,ibElimination;
    Intent intent;
    String[] Mode;
    TextView tvOnline,tvOffline;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_menu);
        tvOffline=(TextView)findViewById(R.id.Offline);
        tvOnline=(TextView)findViewById(R.id.multi);
        intent=new Intent();
        Mode=new String[3];
        intent.setClass(this, GameActivity.class);
        btn=new Button[5];
        for(int i=0;i<btn.length;i++) {
            String nameid="btn"+ i;
            btn[i] = (Button) findViewById(getResources().getIdentifier(nameid,"id",getPackageName()));
            btn[i].setOnClickListener(this);
            btn[i].setOnTouchListener(this);
        }
        TextViewCompat.setAutoSizeTextTypeWithDefaults(tvOnline,TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
        TextViewCompat.setAutoSizeTextTypeWithDefaults(tvOffline,TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);

    }

    @Override
    public void onClick(View view) {
    switch(view.getId())
    {
        case R.id.btn0:
            break;
        case R.id.btn1:
            break;
        case R.id.btn2:
            break;
        case R.id.btn3:
            Mode[0]=getResources().getString(R.string.OFFLINE_MODE);
            Mode[1]=getResources().getString(R.string.OFFLINE_SOLO_MODE);
            createDialog();
            break;
        case R.id.btn4:
            Mode[0]=getResources().getString(R.string.OFFLINE_MODE);
            Mode[1]=getResources().getString(R.string.OFFLINE_AI_MODE);
            createDialog();
            break;
        case R.id.ibTimed:
            d.dismiss();
            Mode[2]=getResources().getString(R.string.GAMEMODE_TIMED);;
            intent.putExtra("MODE",Mode);
            startActivity(intent);
            break;
        case R.id.ibElimination:
            d.dismiss();
            Mode[2]=getResources().getString(R.string.GAMEMODE_ELIMINATION);
            intent.putExtra("MODE",Mode);
            startActivity(intent);
            break;

    }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int index=-1;
        for(int i=0;i<btn.length;i++) {
            if(btn[i]==view)
                index=i;
        }
        Drawable down= ContextCompat.getDrawable(this,R.drawable.button_bg_rounded_secondary);
        Drawable up=ContextCompat.getDrawable(this,R.drawable.button_bg_rounded_primary);
        if(index>=3) {
            Drawable help=down; down=up; up=help;
        }
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
            btn[index].setBackground(down);
        else btn[index].setBackground(up);
        return false;
    }

    public void createDialog()
    {


        d= new Dialog(this);
        d.setContentView(R.layout.offlinedialog);
        d.setTitle("Select game mode");
        d.setCancelable(true);
        ibTimed=(ImageButton)d.findViewById(R.id.ibTimed);
        ibElimination=(ImageButton)d.findViewById(R.id.ibElimination);
        ibTimed.setOnClickListener(this);
        ibElimination.setOnClickListener(this);
        d.show();

    }

}
