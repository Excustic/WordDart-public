package com.example.worddart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.FlingAnimation;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Instrumentation;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;


import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

public class GameActivity extends AppCompatActivity implements View.OnClickListener, View.OnKeyListener {
    private static final String TAG = "STATUS_GAME";
    Intent intent;
    String[] Mode;
    String MODE_OFFLINE,MODE_ONLINE,MODE_SOLO,MODE_AI,MODE_TIMED,MODE_ELIMINATION;
    final long INTERVAL_MILLIS=5*60*1000, MAX_MILLIS=85*60*1000,MIN_TIMED_MILLIS=INTERVAL_MILLIS,MAX_ELIM_MILLIS=2*60*1000,MIN_ELIM_MILLIS=10*1000;
    static final String WIKI_API= "https://en.wiktionary.org/w/api.php?action=query&titles=";
    TextView tvTimer,tvMode,tvScore;
    Button btnAdd,btnSub,btnStart;
    Animation fadeOut,fadeIn;
    CountDownTimer timer;
    long timeLeftInMillis,setTimeMillis;
    Boolean timerIsRun;
    static final String JSON_ADDER="&format=json";
    final int CODE_SEND=200;
    EditText etAns;
    Handler handler;
    TextWatcher watcher;
    String jsonString;
    String url;
    HashMap<Character, ArrayList<String>> AIDictionary,usedWords;

    public static String getJsonFromServer(String url) throws IOException {
        Log.d(TAG,url);
        BufferedReader inputStream = null;

        URL jsonUrl = new URL(url);
        URLConnection dc = jsonUrl.openConnection();

        dc.setConnectTimeout(5000);
        dc.setReadTimeout(5000);

        inputStream = new BufferedReader(new InputStreamReader(
                dc.getInputStream()));

        // read the JSON results into a string
        String jsonResult = inputStream.readLine();
        Log.d(TAG,"jsonresult: "+jsonResult);
        return jsonResult;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        intent = getIntent();
        Mode = intent.getStringArrayExtra("MODE");
        assert Mode != null;
        Log.d(TAG, "Mode: " + Mode[0] + "_" + Mode[1] + "_" + Mode[2]);
        MODE_OFFLINE = getResources().getString(R.string.OFFLINE_MODE);
        MODE_ONLINE = getResources().getString(R.string.ONLINE_MODE);
        MODE_SOLO = getResources().getString(R.string.OFFLINE_SOLO_MODE);
        MODE_AI = getResources().getString(R.string.OFFLINE_AI_MODE);
        MODE_TIMED = getResources().getString(R.string.GAMEMODE_TIMED);
        MODE_ELIMINATION = getResources().getString(R.string.GAMEMODE_ELIMINATION);
        timeLeftInMillis=0;
        tvScore=(TextView)findViewById(R.id.tvScore);
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
        fadeIn=AnimationUtils.loadAnimation(this,R.anim.fadein);
        etAns=(EditText)findViewById(R.id.editText);
        AIDictionary=new HashMap<>();
        usedWords=new HashMap<>();
    }

    @Override
    protected void onStart() {
        super.onStart();
        etAns.setOnKeyListener(this);
        etAns.performClick();
        if (Mode[0].equals(MODE_OFFLINE)) {
            if(Mode[1].equals(MODE_SOLO))
            {
                if(Mode[2].equals(MODE_TIMED))
                {
                    tvTimer.setText(getResources().getString(R.string.TimerReset));
                    tvScore.setText(getResources().getString(R.string.StartScore_TIMED));

                }
                else if(Mode[2].equals(MODE_ELIMINATION))
                {
                    btnAdd.setVisibility(View.INVISIBLE);
                    btnSub.setVisibility(View.INVISIBLE);
                    tvScore.setText(getResources().getString(R.string.StartScore_ELIMINATION));
                }
            }
            else if(Mode[1].equals(MODE_AI))
            {
                if(Mode[2].equals(MODE_TIMED))
                {

                }
                else if(Mode[2].equals(MODE_ELIMINATION))
                {

                }
            }
        }
        else if(Mode[0].equals(MODE_ONLINE))
        {

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
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
                if(timeLeftInMillis>MIN_TIMED_MILLIS)
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
    if(Mode[2].equals(MODE_TIMED))
    {
        btnSub.startAnimation(fadeOut);
        btnAdd.startAnimation(fadeOut);
    }
    btnStart.startAnimation(fadeOut);
    fling.setStartVelocity(2000)
            .setMaxValue(2000)
            .setMinValue(-2000)
            .setFriction(0.3f)
            .start();
    Snackbar.make(findViewById(R.id.GameLayout), R.string.StartMessage,
            Snackbar.LENGTH_LONG)
            .show();

}
    public void execGame()
    {
        etAns.setVisibility(View.VISIBLE);
        handler=new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message message) {
                if(message.arg1==CODE_SEND)
                {
                    String word=message.obj.toString();
                    ArrayList<String> list=usedWords.get(word.charAt(0));
                    if(list==null)
                        list=new ArrayList<>();
                    list.add(word);
                    usedWords.put(word.charAt(0),list);
                    if(watcher!=null)
                        etAns.removeTextChangedListener(watcher);
                    Log.d(TAG,"word: "+word);
                    if(Mode[2].equals(MODE_TIMED)) {
                        String currentScore = tvScore.getText().toString();
                        int score = Integer.parseInt(currentScore.split(" : ")[1]);
                        Log.d(TAG, "Score " + score);
                        score += 10 * word.length();
                        currentScore = currentScore.split(" : ")[0];
                        currentScore += " : " + score;
                        tvScore.setText(currentScore);
                    }
                    if(Mode[2].equals(MODE_ELIMINATION))
                    {
                        if(setTimeMillis>MIN_ELIM_MILLIS) {
                            timeLeftInMillis = setTimeMillis;
                            timeLeftInMillis -= MIN_ELIM_MILLIS;
                            setTimeMillis=timeLeftInMillis;
                        }
                        else timeLeftInMillis=setTimeMillis;
                        stopTimer();
                        startCountDown();
                        String currentScore = tvScore.getText().toString();
                        int wave=Integer.parseInt(String.valueOf(tvScore.getText().toString().split(" ")[1]))+1;
                        currentScore=currentScore.split(" ")[0];
                        currentScore+=" "+wave;
                        tvScore.setText(currentScore);

                    }
                    final String newWord=String.valueOf(word.charAt(word.length()-1)).toUpperCase();
                    etAns.setText(newWord.toUpperCase());
                    Selection.setSelection(etAns.getText(), etAns.getText().length());
                    watcher=new TextWatcher() {

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before,
                                                  int count) {
                            // TODO Auto-generated method stub

                        }

                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count,
                                                      int after) {
                            // TODO Auto-generated method stub

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            if (!s.toString().startsWith(newWord)) {
                                etAns.setText(newWord);
                                Selection.setSelection(etAns.getText(), etAns
                                        .getText().length());

                            }

                        }

                    };
                    etAns.addTextChangedListener(watcher);

                }
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Instrumentation inst = new Instrumentation();
                                inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT);
                            } catch (Exception e) {
                                Log.d(TAG,e.toString());
                            }
                        }
                    }).start();
                     return false;

                }
        });

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
        final Boolean[] animStart = {false};
        timer=new CountDownTimer(4000,10) {
            @Override
            public void onTick(long l) {
                Log.d(TAG,"millisecs until start:"+l);
                if(l<1000&&!animStart[0]) {
                    tvScore.startAnimation(fadeIn);
                    animStart[0] =true;
                }
            }

            @Override
            public void onFinish() {
                if(Mode[2].equals(MODE_ELIMINATION))
                {
                    tvTimer.setText(getResources().getString(R.string.TimerElimination));
                    setTimeMillis=MAX_ELIM_MILLIS;
                    timeLeftInMillis=MAX_ELIM_MILLIS;
                }
                startCountDown();
            }
        }.start();
    }

    public void stopTimer()
    {
        timer.cancel();
        timerIsRun=false;
    }
public void startCountDown()
{
    execGame();
    if(Mode[2].equals(MODE_TIMED))
        timeLeftInMillis=Integer.parseInt(tvTimer.getText().toString().split("m")[0])*60*1000;
    timer=new CountDownTimer(timeLeftInMillis+1,1000) {
        @Override
        public void onTick(long l) {
            timeLeftInMillis=l;
            updateTimer();
        }

        @Override
        public void onFinish() {
                createDialog();
        }
    }.start();
    timerIsRun=true;
}

    @Override
    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        if (keyEvent.getAction() == KeyEvent.ACTION_DOWN){
            etAns.setTextColor(Color.BLACK);
            switch (keyEvent.getKeyCode()) {
                case KeyEvent.KEYCODE_ENTER:
                    // TODO make keyboard static, prevent it from hiding
                    if (isValid(etAns.getText().toString())) {
                        url = WIKI_API + etAns.getText().toString().toLowerCase() + JSON_ADDER;
                        new MyTask(this).execute();
                    } else {
                        Toast t = Toast.makeText(GameActivity.this, getResources().getString(R.string.ERROR_MESSAGE_1), Toast.LENGTH_SHORT);
                        t.setGravity(Gravity.CENTER, 0, 0);
                        t.show();
                        etAns.setTextColor(Color.RED);
                    }
                    break;
            }
    }
        return false;
    }

    public Boolean isValid(String s)
    {
        Log.d(TAG,"checking is valid, isexist:"+usedWords.containsValue(s));
        if(!s.isEmpty()) {
            ArrayList<String> list = usedWords.get(s.charAt(0));
            if (list != null)
                if (list.contains(s))
                    return false;
        }
        return s.matches("[a-zA-Z]+")&&!s.isEmpty()&&!s.contains(" ")&&s.length()<=45&&s.length()>1;
    }

    public void InitializeAI()
    {
        //Get the text file
        File file = new File("assets/","AIQuickDictionary.txt");
        try {
            FileInputStream fis = new FileInputStream(file);
            DataInputStream in = new DataInputStream(fis);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            String strLine;
            while ((strLine = br.readLine()) != null) {
                ArrayList<String> list=AIDictionary.get(strLine.charAt(0));
                if (list != null) {
                    list.add(strLine);
                }
                usedWords.put(strLine.charAt(0),list);
            }
            br.close();
            in.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void createDialog()
    {
        String msg="";
        if(Mode[2].equals(MODE_ELIMINATION))
            msg="Your score - "+tvScore.getText().toString();
        if(Mode[2].equals(MODE_TIMED))
            msg="Your score : "+tvScore.getText().toString().split(" : ")[1];
        new AlertDialog.Builder(this)
                .setTitle("Game Over")
        .setMessage(msg)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent =new Intent();
                        intent.setClass(GameActivity.this,PlayMenu.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Play Again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        GameActivity.this.recreate();
                    }
                }).setCancelable(false).show();
//        TextView tvMsg=(TextView)d.getWindow().findViewById(android.R.id.message);
//        tvMsg.setTypeface(ResourcesCompat.getFont(this,R.font.montserrat_regular));
//        TextView tvTitle=(TextView)d.getWindow().findViewById(android.R.id.title);
//        tvTitle.setTypeface(ResourcesCompat.getFont(this,R.font.bubble3d));
    }
    private static class MyTask extends AsyncTask<Void,Void,Void>
    {
        private WeakReference<GameActivity> activityReference;
        // only retain a weak reference to the activity
        MyTask(GameActivity context) {
            activityReference = new WeakReference<>(context);
        }
        @Override
        protected Void doInBackground(Void... params) {
            try {
                GameActivity activity=activityReference.get();
                if (activity == null || activity.isFinishing()) return null;
                activity.jsonString = getJsonFromServer(activity.url);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            GameActivity activity=activityReference.get();
            if (activity == null || activity.isFinishing()) return;
            Message msg = new Message();
            msg.arg1 = activity.CODE_SEND;
            msg.obj = activity.etAns.getText().toString();
            if (!activity.jsonString.contains("-1"))
                activity.handler.sendMessage(msg);
            else {
                Toast t = Toast.makeText(activity.getApplicationContext(), activity.getResources().getString(R.string.ERROR_MESSAGE_1), Toast.LENGTH_SHORT);
                t.setGravity(Gravity.CENTER, 0, 0);
                t.show();
                activity.etAns.setTextColor(Color.RED);
            }
        }

    }

    }


