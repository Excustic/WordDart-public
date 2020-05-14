package com.example.worddart;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;
import androidx.preference.PreferenceManager;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class PlayMenu extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {
    Button[] btn;
    Dialog d;
    ImageButton ibTimed,ibElimination;
    Intent intent;
    String[] Mode;
    TextView tvOnline,tvOffline,tvTimed,tvElimination;
    final String DatabaseURL="https://word-dart.herokuapp.com";
    SharedPreferences sp;
    private RequestQueue queue;
    int gameMode,dialogMode;
    Handler handler;
    private static String TAG="PlayMenu";

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_menu);
        sp= PreferenceManager.getDefaultSharedPreferences(this);
        tvOffline= findViewById(R.id.Offline);
        tvOnline= findViewById(R.id.multi);
        gameMode=-1; // 0 - timed, 1 - elimination
        dialogMode=-1; //0 - createdialog(), 1 - createlobbydialog(), 2 - joinLobbydialog()
        intent=new Intent();
        Mode=new String[3];
        intent.setClass(this, GameActivity.class);
        btn=new Button[5];
        for(int i=0;i<btn.length;i++) {
            String nameid="btn"+ i;
            btn[i] = findViewById(getResources().getIdentifier(nameid,"id",getPackageName()));
            btn[i].setOnClickListener(this);
            btn[i].setOnTouchListener(this);
        }
        TextViewCompat.setAutoSizeTextTypeWithDefaults(tvOnline,TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
        TextViewCompat.setAutoSizeTextTypeWithDefaults(tvOffline,TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
        queue = Volley.newRequestQueue(this);
        Intent intent = new Intent();
        intent.setClass(this,UserActivator.class);
        intent.putExtra("URL",DatabaseURL+"/api/users/activate?userID="+sp.getString("TokenID",""));
        startService(intent);
        Log.d(TAG, "onCreate: startService called");
    }

    @Override
    public void onClick(View view) {
    switch(view.getId())
    {
        case R.id.btn0:
            //QuickLobby();
            Mode[0]=getResources().getString(R.string.ONLINE_MODE);
            break;
        case R.id.btn1:
            //FindLobby();
            Mode[0]=getResources().getString(R.string.ONLINE_MODE);
            dialogMode=2;
            joinLobbyDialog();
            break;
        case R.id.btn2:
            Mode[0]=getResources().getString(R.string.ONLINE_MODE);
            getAvailable();
            handler=new Handler(message -> {
                if (message.arg1 == 1){
                if(message.obj!=null)
                    Log.d("getAvailable", "onClick: data - "+message.obj.toString());
                dialogMode=1;
                if(message.obj==null)
                    CreateLobby();
                else ConnectLobby(message.obj.toString(),"-1");

                }
                return true;
            });


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
            Mode[2]=getResources().getString(R.string.GAMEMODE_TIMED);
            intent.putExtra("MODE",Mode);
            if(dialogMode==0) {
                startActivity(intent);
                d.dismiss();
            }
            else
            {
                gameMode=0;
                tvElimination.setTextColor(Color.BLACK);
                tvTimed.setTextColor(ContextCompat.getColor(this,R.color.colorPrimary));
            }
            break;
        case R.id.ibElimination:
            Mode[2]=getResources().getString(R.string.GAMEMODE_ELIMINATION);
            intent.putExtra("MODE",Mode);
            if(dialogMode==0) {
                startActivity(intent);
                d.dismiss();
            }
            else
            {
                gameMode=1;
                tvElimination.setTextColor(ContextCompat.getColor(this,R.color.colorPrimary));
                tvTimed.setTextColor(Color.BLACK);
            }
            break;

    }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int index=-1;
        view.performClick();
        for(int i=0;i<btn.length;i++) {
            if(btn[i]==view)
                index=i;
        }
        Drawable down= ContextCompat.getDrawable(this,R.drawable.button_bg_rounded_secondary);
        Drawable up=ContextCompat.getDrawable(this,R.drawable.button_bg_rounded_primary);
        if(index>=3) {
            Drawable help=down; down=up; up=help;
        }
        switch (motionEvent.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                btn[index].setBackground(down);
                break;
            case MotionEvent.ACTION_UP:
                btn[index].setBackground(up);
                break;
            default:
                break;
        }
        return false;
    }

    public void createDialog()
    {
        dialogMode=0;
        d= new Dialog(this);
        d.setContentView(R.layout.offlinedialog);
        d.setTitle("Select game mode");
        d.setCancelable(true);
        ibTimed= d.findViewById(R.id.ibTimed);
        ibElimination= d.findViewById(R.id.ibElimination);
        ibTimed.setOnClickListener(this);
        ibElimination.setOnClickListener(this);
        d.show();

    }
    public void createLobbyDialog(final String lobby_Title)
    {
        Log.d(TAG, "createLobbyDialog: dialog created");
        final Dialog dialog= new Dialog(this);
        dialog.setContentView(R.layout.createlobbydialog);
        dialog.setTitle("Lobby Settings");
        dialog.setCancelable(false);
        final EditText etPIN= dialog.findViewById(R.id.etPIN);
        ibTimed= dialog.findViewById(R.id.ibTimed);
        ibElimination= dialog.findViewById(R.id.ibElimination);
        Button btCreate= dialog.findViewById(R.id.btCreate);
        ibTimed.setOnClickListener(this);
        ibElimination.setOnClickListener(this);
        tvTimed=dialog.findViewById(R.id.tvTimed);
        tvElimination=dialog.findViewById(R.id.tvElimination);
        btCreate.setOnClickListener(view -> {
            if(gameMode!=-1&&(etPIN.getText().toString().length()==4||etPIN.getText().toString().length()==0)) {
                int pin;
                if(etPIN.getText().toString().length()==0)
                    pin=-1;
                else pin= Integer.parseInt(etPIN.getText().toString());
                updateSettings(lobby_Title,pin,gameMode);
                dialog.dismiss();
                Log.d(TAG, "onClick: lobbyTitle - "+lobby_Title);
                intent.putExtra("lobbyTitle",lobby_Title);
                intent.setClass(PlayMenu.this,WaitLobby.class);
                startActivity(intent);
            }
            else{
                if(etPIN.getText().toString().length()!=4&&etPIN.getText().toString().length()!=0)
                    Toast.makeText(PlayMenu.this,"Pin code has to be 4 or 0 (no code) digits long",Toast.LENGTH_LONG).show();
                if(gameMode==-1)
                    Toast.makeText(PlayMenu.this,"Choose a game mode",Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onClick: bad settings - pin: "+etPIN.getText().toString()+", gameMode: "+gameMode);
            }
        });
        dialog.show();

    }

    public void joinLobbyDialog(){
        final Dialog dialog= new Dialog(this);
        dialog.setContentView(R.layout.joinlobbydialog);
        dialog.setTitle("Join Lobby");
        dialog.setCancelable(true);
        final EditText etLobbyNum = dialog.findViewById(R.id.etLobbyNum);
        final EditText etPIN = dialog.findViewById(R.id.etPIN2);
        Button btJoin = dialog.findViewById(R.id.btJoin);
        btJoin.setOnClickListener(view -> {
            String PINCODE = etPIN.getText().toString().isEmpty() ? "-1" : etPIN.getText().toString();
            if(etLobbyNum.getText().toString().length()>0)
                    ConnectLobby("Lobby_"+etLobbyNum.getText().toString(),PINCODE);
            else Toast.makeText(PlayMenu.this,"Enter Lobby Number",Toast.LENGTH_SHORT).show();
        });
        dialog.show();
    }
    // TODO make a helper class that deals with all of these database-centred functions - pack it up nicely
    public void getAvailable()
    {
        final String url = DatabaseURL+"/api/lobbies/getAvailable";
        final String[] obj = {null};
        // prepare the Request
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, (String) null,
                response -> {
                    // display response
                    Log.d(TAG+" Response", "getAvailable: "+response.toString());
                    obj[0] =response.toString().substring(response.toString().indexOf("title")+8,response.toString().indexOf("isAvailable")-3);
                    Log.d(TAG+" Response", "onResponse: obj[0]: "+obj[0]);
                    if(handler!=null) {
                        Message msg=new Message();
                        msg.arg1=1;
                        msg.obj=obj[0];
                        handler.sendMessage(msg);
                    }
                },
                error -> Log.d(TAG+" : Error.Response",error.toString())
        );
        queue.add(getRequest);
    }

    public void updateSettings(String lobby_Title,int PIN,int gameMode)
    {
        String url=DatabaseURL;
        try {
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("PINCODE",PIN);
            String mode = (gameMode==0) ? "timed" : "elimination";
            final String mRequestBody = jsonBody.toString();
            url+="/api/lobbies/update?lobbyTitle="+lobby_Title;
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, response -> Log.i(TAG+" LOG_RESPONSE", response), error -> Log.e(TAG+" LOG_RESPONSE", error.toString())) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() {
                    return mRequestBody.getBytes(StandardCharsets.UTF_8);
                }

                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    String responseString;
                    if (response != null) {
                        responseString = String.valueOf(response.statusCode);
                        return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                    }
                    return Response.error(new VolleyError("Response null"));
                }
            };

            requestQueue.add(stringRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public void ConnectLobby(final String lobby_Title,final String PINCODE)
    {
        String url=DatabaseURL;
            String tokenid = sp.getString("TokenID", null);
            Log.d("lobbyTitle", "CreateLobby: Title - " + lobby_Title);
            url += "/api/lobbies/connect?lobbyTitle=" + lobby_Title + "&userID=" + tokenid + "&PINCODE=" + PINCODE;
            final StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                    response -> {
                        // response
                        intent.setClass(PlayMenu.this,WaitLobby.class);
                        intent.putExtra("lobbyTitle",lobby_Title);
                        if(dialogMode==-1 || dialogMode == 2) {
                            startActivity(intent);
                        }
                        else if(dialogMode==1)
                            createLobbyDialog(lobby_Title);

                        Log.d(TAG+" Response", response);
                    },
                    error -> {
                        // error
                        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                            //This indicates that the reuest has either time out or there is no connection
                            String message = (error.networkResponse!=null && error.networkResponse.data!=null) ? new String(error.networkResponse.data) : "Timeout Error";
                            Log.d(TAG, "onErrorResponse: " + message);
                            Toast.makeText(PlayMenu.this, message, Toast.LENGTH_SHORT).show();
                        } else if (error instanceof AuthFailureError) {
                            //Error indicating that there was an Authentication Failure while performing the request
                            String message = (error.networkResponse!=null && error.networkResponse.data!=null) ? new String(error.networkResponse.data) : "Authentication Error";
                            Log.d(TAG, "onErrorResponse: " + message);
                            Toast.makeText(PlayMenu.this, message, Toast.LENGTH_SHORT).show();
                        } else if (error instanceof ServerError) {
                            //Indicates that the server responded with a error response
                            String message = (error.networkResponse!=null && error.networkResponse.data!=null) ? new String(error.networkResponse.data) : "Server Error";
                            Log.d(TAG, "onErrorResponse: " + message);
                            Toast.makeText(PlayMenu.this, message, Toast.LENGTH_SHORT).show();

                        } else if (error instanceof NetworkError) {
                            //Indicates that there was network error while performing the request
                            String message = (error.networkResponse!=null && error.networkResponse.data!=null) ? new String(error.networkResponse.data) : "Network Error";
                            Log.d(TAG, "onErrorResponse: " + message);
                            Toast.makeText(PlayMenu.this, message, Toast.LENGTH_SHORT).show();

                        } else if (error instanceof ParseError) {
                            // Indicates that the server response could not be parsed
                            String message = (error.networkResponse!=null && error.networkResponse.data!=null) ? new String(error.networkResponse.data) : "Parse Error";
                            Log.d(TAG, "onErrorResponse: " + message);
                            Toast.makeText(PlayMenu.this, message, Toast.LENGTH_SHORT).show();

                        }
                    }
            ) {
                @Override
                protected Map<String, String> getParams()
                {
                    return new HashMap<>();
                }
            };
            queue.add(postRequest);

    }
    public void CreateLobby()
    {
        String url=DatabaseURL;
        try {
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("title", "Lobby");
            jsonBody.put("isAvailable", true);
            jsonBody.put("playerCount",0);
            jsonBody.put("playerArr",new JSONArray());
            jsonBody.put("PINCODE",-1);
            jsonBody.put("gameMode","");
            jsonBody.put("currentWord","");
            final String mRequestBody = jsonBody.toString();
            url+="/api/lobbies/insert";
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, response -> Log.i(TAG+" LOG_RESPONSE", response), error -> Log.e(TAG+" LOG_RESPONSE", error.toString())) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() {
                    return mRequestBody.getBytes(StandardCharsets.UTF_8);
                }

                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    String responseString;
                    if (response != null) {
                        responseString = String.valueOf(response.statusCode);

                        String tokenid;
                        tokenid = new String(response.data, StandardCharsets.UTF_8);
                        tokenid = tokenid.substring(tokenid.indexOf("_id") + 8, tokenid.indexOf("isAvailable") - 3);
                        Log.d(TAG, "token: " + tokenid);

                        ConnectLobby(tokenid,"-1");
                        return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                    }
                    return Response.error(new VolleyError("response null"));
                }
            };

            requestQueue.add(stringRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
