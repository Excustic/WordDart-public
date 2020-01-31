package com.example.worddart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.TextViewCompat;
import androidx.preference.PreferenceManager;

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
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.load.engine.Resource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_menu);
        sp= PreferenceManager.getDefaultSharedPreferences(this);
        tvOffline=(TextView)findViewById(R.id.Offline);
        tvOnline=(TextView)findViewById(R.id.multi);
        gameMode=-1; // 0 - timed, 1 - elimination
        dialogMode=-1; //0 - createdialog(), 1 - createlobbydialog(), 2 - joinLobbydialog()
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
            handler=new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(@NonNull Message message) {
                    if (message.arg1 == 1){
                    if(message.obj!=null)
                        Log.d("getAvailable", "onClick: data - "+message.obj.toString());
                    dialogMode=1;
                    if(message.obj==null)
                        CreateLobby();
                    else ConnectLobby(message.obj.toString(),null,"-1");

                    }
                    return true;
                }
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
        dialogMode=0;
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
    public void createLobbyDialog(final String lobby_id)
    {
        final Dialog dialog= new Dialog(this);
        dialog.setContentView(R.layout.createlobbydialog);
        dialog.setTitle("Lobby Settings");
        dialog.setCancelable(false);
        final EditText etPIN=(EditText)dialog.findViewById(R.id.etPIN);
        ibTimed=(ImageButton)dialog.findViewById(R.id.ibTimed);
        ibElimination=(ImageButton)dialog.findViewById(R.id.ibElimination);
        Button btCreate=(Button)dialog.findViewById(R.id.btCreate);
        ibTimed.setOnClickListener(this);
        ibElimination.setOnClickListener(this);
        tvTimed=dialog.findViewById(R.id.tvTimed);
        tvElimination=dialog.findViewById(R.id.tvElimination);
        btCreate.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(gameMode!=-1&&(etPIN.getText().toString().length()==4||etPIN.getText().toString().length()==0)) {
                    int pin;
                    if(etPIN.getText().toString().length()==0)
                        pin=-1;
                    else pin= Integer.parseInt(etPIN.getText().toString());
                    updateSettings(lobby_id,pin,gameMode);
                    dialog.dismiss();
                    Log.d(TAG, "1444onClick: lobbyid - "+lobby_id);
                    intent.putExtra("lobbyID",lobby_id);
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
            }
        });
        dialog.show();

    }

    public void joinLobbyDialog(){
        final Dialog dialog= new Dialog(this);
        dialog.setContentView(R.layout.joinlobbydialog);
        dialog.setTitle("Join Lobby");
        dialog.setCancelable(false);
        final EditText etLobbyNum = (EditText)dialog.findViewById(R.id.etLobbyNum);
        final EditText etPIN = (EditText)dialog.findViewById(R.id.etPIN2);
        Button btJoin = (Button)dialog.findViewById(R.id.btJoin);
        btJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(etLobbyNum.getText().toString().length()>0)
                    ConnectLobby(null,"Lobby_"+etLobbyNum.getText().toString(),etPIN.getText().toString());
                else Toast.makeText(PlayMenu.this,"Enter Lobby Number",Toast.LENGTH_SHORT).show();
            }
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
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        // display response
                        Log.d(TAG+" Response", "getAvailable: "+response.toString());
                        obj[0] =response.toString().substring(response.toString().indexOf("_id")+6,response.toString().indexOf("title")-3);
                        Log.d(TAG+" Response", "onResponse: obj[0]: "+obj[0]);
                        if(handler!=null) {
                            Message msg=new Message();
                            msg.arg1=1;
                            msg.obj=obj[0];
                            handler.sendMessage(msg);
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG+" : Error.Response",error.toString());

                    }
                }
        );
        queue.add(getRequest);
    }

    public void updateSettings(String lobby_id,int PIN,int gameMode)
    {
        String url=DatabaseURL;
        try {
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("PINCODE",PIN);
            String mode = (gameMode==0) ? "timed" : "elimination";
            final String mRequestBody = jsonBody.toString();
            url+="/api/lobbies/update?lobbyID="+lobby_id;
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.i(TAG+" LOG_RESPONSE", response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG+" LOG_RESPONSE", error.toString());
                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        return mRequestBody == null ? null : mRequestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf(TAG+" Unsupported Encoding while trying to get the bytes of %s using %s", mRequestBody, "utf-8");
                        return null;
                    }
                }

                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    String responseString = "";
                    if (response != null) {
                        responseString = String.valueOf(response.statusCode);
                    }
                    return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                }
            };

            requestQueue.add(stringRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public void ConnectLobby(final String lobby_id,final String lobby_title,final String PINCODE)
    {
        String url=DatabaseURL;
        if(lobby_id!=null && lobby_title!=null)
        {
            if(lobby_id!=null) {
                String tokenid = sp.getString("TokenID", null);
                Log.d("lobbyID", "CreateLobby: id - " + lobby_id);
                url += "/api/lobbies/connect?lobbyID=" + lobby_id + "&userID=" + tokenid;
            }
            else if (lobby_title!=null)
            {
                String tokenid = sp.getString("TokenID", null);
                Log.d("lobbyID", "CreateLobby: title - " + lobby_title);
                url += "/api/lobbies/connect?lobbyTitle=" + lobby_title + "&userID=" + tokenid;
            }
            url+="&PINCODE="+PINCODE;
                final StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>()
                        {
                            @Override
                            public void onResponse(String response) {
                                // response
                                Log.d(TAG+" Response", response);
                            }
                        },
                        new Response.ErrorListener()
                        {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // error
                                Log.d(TAG+" Error.Response", error.toString());
                            }
                        }
                ) {
                    @Override
                    protected Map<String, String> getParams()
                    {
                        Map<String, String> params = new HashMap<>();
                        return params;
                    }
                };
                queue.add(postRequest);
                RequestQueue.RequestFinishedListener listener =
                    new RequestQueue.RequestFinishedListener()
                    { @Override public void onRequestFinished(Request request)
                    {
                        if(request.equals(postRequest))
                        {
                            if(request.hasHadResponseDelivered())
                                intent.setClass(PlayMenu.this,WaitLobby.class);
                                if(dialogMode==-1) {
                                    startActivity(intent);
                                }
                                else if(dialogMode==1)
                                    createLobbyDialog(lobby_id);

                        }
                    }
                    };
                queue.addRequestFinishedListener(listener);
        }
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
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.i(TAG+" LOG_RESPONSE", response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG+" LOG_RESPONSE", error.toString());
                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        return mRequestBody == null ? null : mRequestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf(TAG+" Unsupported Encoding while trying to get the bytes of %s using %s", mRequestBody, "utf-8");
                        return null;
                    }
                }

                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    String responseString = "";
                    if (response != null) {
                        responseString = String.valueOf(response.statusCode);

                        String tokenid = "";
                        try {
                            tokenid = new String(response.data, "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        tokenid = tokenid.substring(tokenid.indexOf("_id") + 6, tokenid.indexOf("title") - 3);
                        Log.d(TAG, "token: " + tokenid);

                        ConnectLobby(tokenid,null,"-1");
                    }
                    return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                }
            };

            requestQueue.add(stringRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
