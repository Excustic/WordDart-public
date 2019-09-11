package com.example.worddart;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;
import androidx.preference.PreferenceManager;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    TextView tvOnline,tvOffline;
    final String DatabaseURL="https://word-dart.herokuapp.com";
    SharedPreferences sp;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_menu);
        sp= PreferenceManager.getDefaultSharedPreferences(this);
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
        queue = Volley.newRequestQueue(this);
    }

    @Override
    public void onClick(View view) {
    switch(view.getId())
    {
        case R.id.btn0:
            //QuickLobby();
            break;
        case R.id.btn1:
            //FindLobby();
            break;
        case R.id.btn2:
            String data=getAvailable();
            if(data==null)
                CreateLobby();
            else ConnectLobby(data);
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
    // TODO make a helper class that deals with all of these database-centred functions - pack it up nicely
    public String getAvailable()
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
                        Log.d("Response", response.toString());
                        obj[0] =response.toString();
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response",error.toString());

                    }
                }
        );
        queue.add(getRequest);
        return obj[0];
    }
    public void ConnectLobby(String lobby_id)
    {
        String url=DatabaseURL;
        String body;
        if(lobby_id!=null)
        {
                String tokenid=sp.getString("TokenID",null);
                Log.d("lobbyID", "CreateLobby: id - "+lobby_id);
                url += "/api/lobbies/connect?lobbyID="+lobby_id+"&userID="+tokenid;
                StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>()
                        {
                            @Override
                            public void onResponse(String response) {
                                // response
                                Log.d("Response", response);
                            }
                        },
                        new Response.ErrorListener()
                        {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // error
                                Log.d("Error.Response", error.toString());
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
            jsonBody.put("currentWord","");
            final String mRequestBody = jsonBody.toString();
            url+="/api/lobbies/insert";
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.i("LOG_RESPONSE", response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("LOG_RESPONSE", error.toString());
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
                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", mRequestBody, "utf-8");
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
                        Log.d("debuggo", "token: " + tokenid);
                        ConnectLobby(tokenid);
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
