package com.example.worddart;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class PlayMenu extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {
    Button[] btn;
    Dialog d;
    ImageButton ibTimed,ibElimination;
    Intent intent;
    String[] Mode;
    public final String TIMED="GAMEMODE_TIMED";
    public final String ELIMINATION="GAMEMODE_ELIMINATION";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_menu);
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
            Mode[0]=getResources().getResourceName(R.string.OFFLINE_MODE);
            Mode[1]=getResources().getResourceName(R.string.OFFLINE_SOLO_MODE);
            createDialog();
            break;
        case R.id.btn4:
            Mode[0]=getResources().getResourceName(R.string.OFFLINE_MODE);
            Mode[1]=getResources().getResourceName(R.string.OFFLINE_AI_MODE);
            createDialog();
            break;
        case R.id.ibTimed:
            d.dismiss();
            Mode[2]=getResources().getResourceName(R.string.GAMEMODE_TIMED);;
            intent.putExtra("MODE",Mode);
            startActivity(intent);
            break;
        case R.id.ibElimination:
            d.dismiss();
            Mode[2]=getResources().getResourceName(R.string.GAMEMODE_ELIMINATION);
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
        int down=getResources().getColor(R.color.colorSecondary);
        int up=getResources().getColor(R.color.colorPrimary);
        if(index>=3) {
            int help=down; down=up; up=help;
        }
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
            btn[index].setBackgroundColor(down);
        else btn[index].setBackgroundColor(up);
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
