package com.example.worddart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WaitLobby extends AppCompatActivity {
final String DatabaseURL="https://word-dart.herokuapp.com";
Intent intent;
String lobbyID;
Button btnStart;
private static String TAG="WaitLobby";
private RequestQueue queue;
private ArrayList<UserPreview> userPreviews;
private RecyclerView recyclerView;
private String userID;
private SharedPreferences sp;
TextView tvPnum;
private Handler handler;
private UserPreviewAdapter userPreviewAdapter;
private ScheduledExecutorService task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait_lobby);
        intent=getIntent();
        lobbyID=intent.getStringExtra("lobbyID");
        Log.d(TAG, "1444onCreate: lobbyid - "+lobbyID);
        sp= PreferenceManager.getDefaultSharedPreferences(this);
        userID=sp.getString("TokenID","");
        tvPnum=findViewById(R.id.numOfPlayers);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        handler= new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message message) {
                JSONObject body = null;
                try {
                    body = new JSONObject(message.obj.toString());
                    Log.d(TAG, "handleMessage: body- "+body.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                switch (message.arg1)
                {
                    case 0:
                        try {
                            Log.d(TAG, "handleMessage: imageURL"+body.getString("imageURL"));
                            Bitmap b = getBitmapFromURL(body.getString("imageURL"));
                            String name = body.getString("fullName");
                            String id=body.getString("_id");
                            UserPreview userPreview = new UserPreview(b,name,id);
                            userPreviews.add(userPreview);
                            userPreviewAdapter.notifyItemInserted(userPreviews.size() - 1);
                            Log.d(TAG, "handleMessage: users"+userPreviews.toArray().toString());
                            tvPnum.setText("Number of players: "+userPreviews.size()+"/8");
                        }
                        catch (JSONException e)
                        {
                            Log.d(TAG, "handleMessage: error"+e.toString());
                        }
                        break;
                    case 1:
                        try {
                            JSONArray jsonArr = body.getJSONArray("playerArr");
                            String[] playerArr=new String[jsonArr.length()];
                            for(int i = 0; i < jsonArr.length(); i++){
                                playerArr[i]=jsonArr.get(i).toString();
                            }
                            Log.d(TAG, "handleMessage: playerArr"+ Arrays.toString(playerArr));
                            for (String p:playerArr
                                 ) {
                                Boolean isExist=false;
                                for (UserPreview user:userPreviews
                                     ) {
                                     if(user.getId().equals(p))
                                     {
                                         isExist=true;
                                     }
                                }
                                if(!isExist) {
                                    getUser(DatabaseURL + "/api/users/get/" + p);
                                    Log.d(TAG, "handleMessage: missing user " + p);
                                }
                            }
                        } catch (JSONException e) {
                            Log.d(TAG,e.toString());
                        }
                        break;
                }
                return true;
            }
        });
        InitRecyclerView();
        btnStart=findViewById(R.id.btnStartGame);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.setClass(WaitLobby.this,GameActivity.class);
                startActivity(intent);
            }
        });
        queue = Volley.newRequestQueue(this);

        runUI();

    }

    private void InitRecyclerView() {
        userPreviews=new ArrayList<>(8);
        recyclerView=findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        final String url = DatabaseURL+"/api/users/get/"+userID;
        Log.d(TAG, "InitRecyclerView: url-"+url);
        //getUser(url);
        userPreviewAdapter=new UserPreviewAdapter(this,userPreviews);
        recyclerView.setAdapter(userPreviewAdapter);
        recyclerView.setBackgroundColor(Color.TRANSPARENT);
    }

    private void runUI()
    {
        task= Executors.newScheduledThreadPool(5);
        task.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                //get current lobby body
                final String url = DatabaseURL + "/api/lobbies/get/" + lobbyID;
                final String[] obj = {null};
                // prepare the Request
                JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, (String) null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                // display response
                                Log.d(TAG + " Response", "getAvailable: " + response.toString());
                                obj[0] = response.toString();
                                Log.d(TAG + " Response", "onResponse: obj[0]: " + obj[0]);
                                if (handler != null) {
                                    Message msg = new Message();
                                    msg.arg1 = 1;
                                    msg.obj = obj[0];
                                    handler.sendMessage(msg);
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d(TAG + " Error.Response", error.toString());

                            }
                        }
                );
                queue.add(getRequest);
            }
        },0,3000, TimeUnit.MILLISECONDS);
    }
    public void getUser(String databaseURL)
    {
        // prepare the Request
        final String[] obj = {null};
        final JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, databaseURL, (String) null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        // display response
                        Log.d(TAG+" Response", "getAvailable: "+response.toString());
                        obj[0] =response.toString();
                        Log.d(TAG+" Response", "onResponse: obj[0]: "+obj[0]);
                        if(handler!=null) {
                            Message msg=new Message();
                            msg.arg1=0;
                            msg.obj=obj[0];
                            handler.sendMessage(msg);
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG+" Error.Response",error.toString());

                    }
                }
        );
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Write whatever to want to do after delay specified (1 sec)
                queue.add(getRequest);
            }
        }, 1000);
    }
    public Bitmap getBitmapFromURL(String src) {
        try {
            Log.d(TAG, "getBitmapFromURL: src - "+src);
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            Log.d(TAG, "getBitmapFromURL: bitmap"+myBitmap.toString());
            return myBitmap;
        } catch (IOException e) {
            Log.d(TAG, "getBitmapFromURL: "+e.toString());
            return null;
        }
    }
    public void onPause()
    {
        super.onPause();
        task.shutdown();
    }
    //TODO make a stable connection - server will check every 5 seconds if user is still online, if not, on the 3rd try it will kick
}
