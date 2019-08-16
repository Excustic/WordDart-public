package com.example.worddart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.FlingAnimation;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;


import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

import static com.example.worddart.R.color.colorPrimary;

public class GameActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "STATUS_GAME";
    SharedPreferences sp;
    Intent intent;
    String[] Mode;
    String MODE_OFFLINE,MODE_ONLINE,MODE_SOLO,MODE_AI,MODE_TIMED,MODE_ELIMINATION;
    final long INTERVAL_MILLIS=5*60*1000, MAX_MILLIS=85*60*1000,MIN_TIMED_MILLIS=INTERVAL_MILLIS,MAX_ELIM_MILLIS=2*60*1000,MIN_ELIM_MILLIS=10*1000;
    static final String WIKI_API= "https://en.wiktionary.org/w/api.php?action=query&titles=";
    TextView tvTimer,tvMode,tvScore;
    Button btnAdd,btnSub,btnStart,btnCheck;
    Animation fadeOut,fadeIn;
    CountDownTimer timer;
    long timeLeftInMillis,setTimeMillis;
    Boolean timerIsRun;
    static final String JSON_ADDER="&format=json";
    final int CODE_SEND=200;
    EditText etAns;
    Handler handler;
    SharedPreferences.Editor editor;
    ArrayList<String> profiles,names;
    TextWatcher watcher;
    String jsonString;
    String url;
    String FinalWord; // this variable is the exact word that is being checked, which means the program won't depend on the edittext.text property that can be changed during the task
    HashMap<Character, ArrayList<String>> AIDictionary,usedWords;
    private Boolean AIsTurn;
    private int mIndex;
    private Handler mHandler;
    private Runnable characterAdder;
    private CharSequence mText;
    private long mDelay;

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
        btnCheck=(Button)findViewById(R.id.btnCheck);
        btnStart=(Button)findViewById(R.id.btnStart);
        btnAdd.setOnClickListener(this);
        btnSub.setOnClickListener(this);
        btnStart.setOnClickListener(this);
        fadeOut= AnimationUtils.loadAnimation(this,R.anim.fadeout);
        fadeIn=AnimationUtils.loadAnimation(this,R.anim.fadein);
        etAns=(EditText)findViewById(R.id.editText);
        profiles=new ArrayList<>(); names=new ArrayList<>();
        AIDictionary=new HashMap<>();
        usedWords=new HashMap<>();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        sp= PreferenceManager.getDefaultSharedPreferences(this);
        editor=sp.edit();
        InitDictionary();
    }

    @Override
    protected void onStart() {
        super.onStart();

        //etAns.performClick();
        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        btnCheck.setOnClickListener(this);
//        etAns.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
//                boolean handled = false;
//                if(i== EditorInfo.IME_ACTION_DONE||((keyEvent.getKeyCode()==KeyEvent.KEYCODE_ENTER)&&keyEvent.getAction()==KeyEvent.ACTION_DOWN))
//                {
//                        etAns.setTextColor(Color.BLACK);
//                                // TODO make keyboard static, prevent it from hiding
//                                if (isValid(etAns.getText().toString())) {
//                                    url = WIKI_API + etAns.getText().toString().toLowerCase() + JSON_ADDER;
//                                    new MyTask(GameActivity.this).execute();
//                                } else {
//                                    Toast t = Toast.makeText(GameActivity.this, getResources().getString(R.string.ERROR_MESSAGE_1), Toast.LENGTH_SHORT);
//                                    t.setGravity(Gravity.CENTER, 0, 0);
//                                    t.show();
//                                    etAns.setTextColor(Color.RED);
//                                }
//                    handled=true;
//                    etAns.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
//                        }
//                    })      ;
//                    }
//                return handled;
//            }
//        });
        if (Mode[0].equals(MODE_OFFLINE)) {
            if(Mode[1].equals(MODE_SOLO))
            {
                findViewById(R.id.recycler_view).setVisibility(View.INVISIBLE);
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
                AIsTurn=false;
                InitBitmaps();

                if(Mode[2].equals(MODE_TIMED))
                {
                    tvTimer.setText(getResources().getString(R.string.TimerReset));
                    tvScore.setText(getResources().getString(R.string.StartScore_TIMED_AI));
                }
                else if(Mode[2].equals(MODE_ELIMINATION))
                {
                    btnAdd.setVisibility(View.INVISIBLE);
                    btnSub.setVisibility(View.INVISIBLE);
                    tvScore.setText(getResources().getString(R.string.StartScore_ELIMINATION));
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
            case R.id.btnCheck:
                CheckWord();
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
        btnCheck.setVisibility(View.VISIBLE);
        etAns.setVisibility(View.VISIBLE);
        etAns.setText("");
        handler=new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message message) {
                if(message.arg1==CODE_SEND)
                {
                    etAns.setTextColor(Color.BLACK);
                    String word=message.obj.toString();
                    ArrayList<String> list=usedWords.get(word.charAt(0));
                    if(list==null)
                        list=new ArrayList<>();
                    list.add(word);
                    usedWords.put(word.charAt(0),list);
                    if(watcher!=null)
                        etAns.removeTextChangedListener(watcher);
                    Log.d(TAG,"word: "+FinalWord);
                    final String newWord=String.valueOf(FinalWord.charAt(FinalWord.length()-1)).toUpperCase();
                    etAns.setText(newWord.toUpperCase());
                    etAns.setSelection(1);
                    etAns.setCursorVisible(true);
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
                    if(Mode[2].equals(MODE_TIMED)) {
                        if(Mode[1].equals(MODE_SOLO)) {
                            String currentScore = tvScore.getText().toString();
                            int score = Integer.parseInt(currentScore.split(" : ")[1]);
                            Log.d(TAG, "Score " + score);
                            score += 10 * word.length();
                            currentScore = currentScore.split(" : ")[0];
                            currentScore += " : " + score;
                            tvScore.setText(currentScore);
                        }
                        else{
                            String currentScore = tvScore.getText().toString();
                            //Ai's score
                            String AI = currentScore.split(" : ")[1].split("/")[1].replace(" ","");
                            //player's score
                            String Player= currentScore.split(" : ")[1].split("/")[0].replace(" ","");
                            int score;
                            if(AIsTurn) {
                                score = Integer.parseInt(AI);
                                Log.d(TAG, "ScoreAI " + score);
                                score += 10 * word.length();
                                currentScore = currentScore.split(" : ")[0];
                                currentScore += " : " + Player+" / " + score;
                                tvScore.setText(currentScore);
                                AIsTurn=false;
                                etAns.setEnabled(true);
                                MarkTurn("Player");
                                Refocus();

                            }
                            else {
                                score = Integer.parseInt(Player);
                                Log.d(TAG, "ScorePlayer" + score);
                                score += 10 * word.length();
                                currentScore = currentScore.split(" : ")[0];
                                currentScore += " : " + score + " / " + AI;
                                tvScore.setText(currentScore);
                                MarkTurn("AI");
                                AIPlay();
                            }

                        }
                    }
                    if(Mode[2].equals(MODE_ELIMINATION))
                    {
                        Log.d(TAG, "handleMessage: AisTurn-"+AIsTurn);
                        if(Mode[1].equals(MODE_AI))
                        {
                            if(!AIsTurn) {
                                timeLeftInMillis = setTimeMillis;
                                stopTimer();
                                startCountDown();
                                AIPlay();
                                MarkTurn("AI");
                                return false;


                            }
                        }
                        if(Mode[1].equals(MODE_SOLO)||(Mode[1].equals(MODE_AI)&&AIsTurn)) {
                                Log.d(TAG, "handleMessage: players turn");
                                if (setTimeMillis > MIN_ELIM_MILLIS) {
                                    timeLeftInMillis = setTimeMillis;
                                    timeLeftInMillis -= MIN_ELIM_MILLIS;
                                    setTimeMillis = timeLeftInMillis;
                                } else timeLeftInMillis = setTimeMillis;
                                stopTimer();
                                startCountDown();
                                String currentScore = tvScore.getText().toString();
                                int wave = Integer.parseInt(String.valueOf(tvScore.getText().toString().split(" ")[1])) + 1;
                                currentScore = currentScore.split(" ")[0];
                                currentScore += " " + wave;
                                tvScore.setText(currentScore);
                                etAns.setEnabled(true);
                                if(Mode[1].equals(MODE_AI)&&AIsTurn)
                                {
                                    MarkTurn("Player");
                                    Refocus();
                                }
                            AIsTurn=false;
                        }

                    }
                }
                     return false;
                }
        });

    }

    private void MarkTurn(String player) {
        int index;
        if(player.equals("Player"))
        {
            index=0;
        }
        else{
            index=1;
        }
        Log.d(TAG, "handleMessage: erase");
        //Erase the mark of AI's/Player's turn
        {
            RecyclerView rcv = findViewById(R.id.recycler_view);
            View item = rcv.findViewHolderForAdapterPosition(1-index).itemView;
            CardView cv = item.findViewById(R.id.cardview);
            cv.setCardBackgroundColor(Color.WHITE);
            TextView tv = cv.findViewById(R.id.list_name);
            tv.setTextColor(Color.BLACK);
        }
        Log.d(TAG, "handleMessage: mark");
        //Mark the Player's/AI's turn
        {
            RecyclerView rcv=findViewById(R.id.recycler_view);
            View item=rcv.findViewHolderForAdapterPosition(index).itemView;
            CardView cv=item.findViewById(R.id.cardview);
            cv.setCardBackgroundColor(ContextCompat.getColor(GameActivity.this,colorPrimary));
            TextView tv=cv.findViewById(R.id.list_name);
            tv.setTextColor(Color.WHITE);
        }
    }

    private void Refocus()
    {
        etAns.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(etAns, InputMethodManager.SHOW_IMPLICIT);
        etAns.requestFocus();
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
                //Log.d(TAG,"millisecs until start:"+l);
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
        if(timer!=null) {
            timer.cancel();
            timerIsRun = false;
        }
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

    public Boolean isValid(String s)
    {
        Log.d(TAG, "isValid: word "+s);
        if(!s.isEmpty()) {
            ArrayList<String> list = usedWords.get(s.charAt(0));
            if (list != null)
                if (list.contains(s))
                    return false;
        }
        Log.d(TAG, "isValid: regex"+s.matches("[a-zA-Z]+"));
        Log.d(TAG, "isValid: spaces"+!s.contains(" "));
        Log.d(TAG, "isValid: length"+(s.length()<=45&&s.length()>1)+"len "+s.length());
        return s.matches("[a-zA-Z]+")&&(!s.contains(" "))&&(s.length()<=45&&s.length()>1);
    }

    public void InitDictionary()
    {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(getAssets().open("raw/Nouns.txt"), "UTF-8"));

            // do reading, usually loop until end of file reading
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                //process line
                mLine=mLine.toLowerCase();
                ArrayList<String> list=AIDictionary.get(mLine.charAt(0));
                if(list==null) {
                    list=new ArrayList<>();
                    if(!mLine.contains("-"))
                    list.add(mLine);
                }
                else{
                    if(!mLine.contains("-"))
                        list.add(mLine);
                }
                AIDictionary.put(mLine.charAt(0),list);
            }
        } catch (IOException e) {
            //log the exception
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    //log the exception
                }
            }
        }
    }

    private void CheckWord()
    {
        if(Mode[1].equals(MODE_SOLO)||(Mode[1].equals(MODE_AI)&&!AIsTurn))
        FinalWord=etAns.getText().toString().toLowerCase();
        if (isValid(FinalWord)) {
            Log.d(TAG, "CheckWord: valid word");
            url = WIKI_API + etAns.getText().toString().toLowerCase() + JSON_ADDER;
            etAns.setTextColor(Color.GREEN);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    new MyTask(GameActivity.this).execute();
                }
            }, 500);
        } else {
            Log.d(TAG, "CheckWord: invalid word");
            Toast t = Toast.makeText(GameActivity.this, getResources().getString(R.string.ERROR_MESSAGE_1), Toast.LENGTH_SHORT);
            t.setGravity(Gravity.CENTER, 0, 0);
            t.show();
            etAns.setTextColor(Color.RED);
        }
    }

    public void AIPlay()
    {
        AIsTurn=true;
        mHandler = new Handler();
        setCharacterDelay(300);
        characterAdder=new Runnable() {
            @Override
            public void run() {
                etAns.setText(mText.subSequence(0, mIndex++));
                Log.d(TAG, "run: etAns "+etAns.getText().toString()+" | mIndex: "+mIndex+" | mText: "+mText);
                if(mIndex <= mText.length()) {
                    mHandler.postDelayed(characterAdder, mDelay);
                }
                else {
                    etAns.setText(mText);
                    CheckWord();}
            }
        };
        new Runnable(){

            @Override
            public void run() {
                Log.d(TAG, "CheckWord: finalword- "+FinalWord);
                ArrayList<String> words=AIDictionary.get(FinalWord.charAt(FinalWord.length()-1));
                if(words==null)
                    Log.d(TAG, "run: words null");
                Random random=new Random();
                final String aiword=words.get(random.nextInt(words.size()-1));
                Log.d(TAG,"aiword: "+aiword);
                animateText(aiword);
                FinalWord=aiword;
                etAns.setEnabled(false);
            }
        }.run();
    }

    public void animateText(CharSequence text) {
        mText = text.subSequence(0,1).toString().toUpperCase()+text.subSequence(1,text.length());
        mIndex = 0;

        etAns.setText("");
        mHandler.removeCallbacks(characterAdder);
        mHandler.postDelayed(characterAdder, mDelay);
    }

    public void setCharacterDelay(long millis) {
        mDelay = millis;
    }

    private void InitBitmaps()
    {
        profiles.add(sp.getString("ImageURL","https://image.flaticon.com/icons/svg/875/875010.svg"));
        names.add(sp.getString("profName","You"));

        if(Mode[0].equals(MODE_OFFLINE)&&Mode[1].equals(MODE_AI))
        {
            Log.d(TAG,"AI detected");
            profiles.add("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR_vuywhucFjrkIUnQehKzEnVhsEUSBzN_8H4AFVx7WcH4I8zvpiA");
            names.add("AI");
        }
        InitRecyclerView();
    }

    private void InitRecyclerView()
    {
        RecyclerView recyclerView=findViewById(R.id.recycler_view);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(profiles,names,this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
    }

    public void createDialog()
    {
        String msg="";
        if(Mode[2].equals(MODE_ELIMINATION))
            msg="Your score F- "+tvScore.getText().toString();
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
//            try {
//                GameActivity activity=activityReference.get();
//                if (activity == null || activity.isFinishing()) return null;
//                //activity.jsonString = getJsonFromServer(activity.url);
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            GameActivity activity=activityReference.get();
            if (activity == null || activity.isFinishing()) return;
            Message msg = new Message();
            Log.d(TAG, "onPostExecute: "+activity.FinalWord);
            msg.arg1 = activity.CODE_SEND;
            msg.obj = activity.FinalWord;
//            if (!activity.jsonString.contains("-1")) {
//                activity.handler.sendMessage(msg);
//            }
//            else {
//                Toast t = Toast.makeText(activity.getApplicationContext(), activity.getResources().getString(R.string.ERROR_MESSAGE_1), Toast.LENGTH_SHORT);
//                t.setGravity(Gravity.CENTER, 0, 0);
//                t.show();
//                activity.etAns.setTextColor(Color.RED);
//            }
            String word=activity.FinalWord.toLowerCase();
            ArrayList<String> list = activity.AIDictionary.get(word.charAt(0));
            if (list != null) {
                if (list.contains(word))
                {
                    activity.handler.sendMessage(msg);
                    return;
                }

            }
            Toast t = Toast.makeText(activity.getApplicationContext(), activity.getResources().getString(R.string.ERROR_MESSAGE_1), Toast.LENGTH_SHORT);
            t.setGravity(Gravity.CENTER, 0, 0);
            t.show();
            activity.etAns.setTextColor(Color.RED);

        }

    }

    }


