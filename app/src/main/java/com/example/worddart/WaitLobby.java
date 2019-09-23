package com.example.worddart;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.ArrayList;

public class WaitLobby extends AppCompatActivity {
final String DatabaseURL="https://word-dart.herokuapp.com";
Intent intent;
String lobbyID;
Button btnStart;
private RequestQueue queue;
private ArrayList<UserPreview> userPreviews;
private RecyclerView recyclerView;
private String userID;
private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait_lobby);
        intent=new Intent();
        intent.setClass(this,GameActivity.class);
        lobbyID=intent.getStringExtra("lobbyID");
        sp= PreferenceManager.getDefaultSharedPreferences(this);
        userID=sp.getString("TokenID","");
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
        // prepare the Request
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, (String) null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        // display response
                        Log.d("Response", "getAvailable: "+response.toString());
                        obj[0] =response.toString();
                        Log.d("Response", "onResponse: obj[0]: "+obj[0]);
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
                        Log.d("Error.Response",error.toString());

                    }
                }
        );
        queue.add(getRequest);
        UserPreviewAdapter userPreviewAdapter=new UserPreviewAdapter(this,userPreviews);
        recyclerView.setAdapter(userPreviewAdapter);
    }

    private void runUI()
    {
        new Thread() {
            @Override
            public void run() {
                final String url = DatabaseURL+"/api/lobbies/get/"+lobbyID;
                final String[] obj = {null};
                // prepare the Request
                JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, (String) null,
                        new Response.Listener<JSONObject>()
                        {
                            @Override
                            public void onResponse(JSONObject response) {
                                // display response
                                Log.d("Response", "getAvailable: "+response.toString());
                                obj[0] =response.toString();
                                Log.d("Response", "onResponse: obj[0]: "+obj[0]);
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
                                Log.d("Error.Response",error.toString());

                            }
                        }
                );
                queue.add(getRequest);
            }
        };
    }
}
