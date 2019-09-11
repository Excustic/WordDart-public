package com.example.worddart;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {
    private static final int RC_SIGN_IN = 9000;
    private static final String TAG = "STATUS_MAIN";
    final String DatabaseURL="https://word-dart.herokuapp.com";
    SignInButton btnGoogle;
    TextView dispName;
    Intent intent;
    GoogleSignInClient mGoogleSignInClient;
    CircleImageView profile;
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    Button btnPlay, btnStats, btnSettings;
    RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        intent = new Intent();

        profile = (CircleImageView) findViewById(R.id.circleImageView);
        dispName = (TextView) findViewById(R.id.dispName);
        btnPlay = (Button) findViewById(R.id.btnPlay);
        btnStats = (Button) findViewById(R.id.btnStats);
        btnSettings = (Button) findViewById(R.id.btnSettings);
        btnGoogle = (SignInButton) findViewById(R.id.sign_in_button);

        btnPlay.setOnClickListener(this);
        btnStats.setOnClickListener(this);
        btnSettings.setOnClickListener(this);
        btnGoogle.setOnClickListener(this);

        btnPlay.setOnTouchListener(this);
        btnStats.setOnTouchListener(this);
        btnSettings.setOnTouchListener(this);

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sp.edit();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        updateUI(account);

        Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.proficon_crab)
        ).apply(RequestOptions.circleCropTransform()).into(profile);

        queue = Volley.newRequestQueue(this);
        if (sp.getString("TokenID", null)==null)
            createUser();
            Log.d(TAG,"no user found, creating new one");
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            case R.id.btnPlay:
                intent.setClass(this, PlayMenu.class);
                startActivity(intent);
                break;
            case R.id.btnStats:
                break;
            case R.id.btnSettings:
                intent.setClass(this, SettingsActivity.class);
                startActivity(intent);
                break;
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (MotionEvent.ACTION_DOWN == motionEvent.getAction()) {
            switch (view.getId()) {
                case R.id.btnPlay:
                    btnPlay.setBackground(ContextCompat.getDrawable(this, R.drawable.button_bg_rounded_secondary));
                    break;
                case R.id.btnStats:
                    btnStats.setBackground(ContextCompat.getDrawable(this, R.drawable.button_bg_rounded_secondary));
                    break;
                case R.id.btnSettings:
                    btnSettings.setBackground(ContextCompat.getDrawable(this, R.drawable.button_bg_rounded_secondary));
                    break;
            }
        } else {
            switch (view.getId()) {
                case R.id.btnPlay:
                    btnPlay.setBackground(ContextCompat.getDrawable(this, R.drawable.button_bg_rounded_primary));
                    break;
                case R.id.btnStats:
                    btnStats.setBackground(ContextCompat.getDrawable(this, R.drawable.button_bg_rounded_primary));
                    break;
                case R.id.btnSettings:
                    btnSettings.setBackground(ContextCompat.getDrawable(this, R.drawable.button_bg_rounded_primary));
                    break;
            }
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            updateUserInfo(account);
            // Signed in successfully, show authenticated UI.
            updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }

    public void updateUI(GoogleSignInAccount account) {
        if (account == null) {
            btnGoogle.setVisibility(View.VISIBLE);
        } else {
            if (account.getPhotoUrl() != null)
                editor.putString("ImageURL", account.getPhotoUrl().toString());
            editor.putString("profName", account.getDisplayName());
            editor.apply();
            profile.setImageURI(account.getPhotoUrl());
            profile.setBorderColor(Color.WHITE);
            profile.setBorderWidth(3);
            dispName.setText(account.getDisplayName());
        }
    }

    public void createUser() {
        final String mRequestBody = "{\"userType\":\"anonymous\",\"fullName\":\"Random_Crab\",\"emailAddress\":\"\"}";

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.POST, DatabaseURL+"/api/users/insert", (JSONArray) null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Log.i("LOG_RESPONSE", response.toString());

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
            public byte[] getBody() {
                try {
                    return mRequestBody == null ? null : mRequestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", mRequestBody, "utf-8");
                    return null;
                }
            }

            @Override
            protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
                JSONArray jsonArray = new JSONArray();
                if (response != null) {
                    jsonArray.put(String.valueOf(response.statusCode));

                    String tokenid = "";
                    try {
                        tokenid = new String(response.data, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG, "RES_BODY: " + tokenid);
                    tokenid = tokenid.substring(tokenid.indexOf("_id") + 6, tokenid.indexOf("userType") - 3);
                    Log.d(TAG, "tokenid: " + tokenid);
                    editor.putString("TokenID", tokenid);
                    editor.apply();
                }
                return Response.success(jsonArray, HttpHeaderParser.parseCacheHeaders(response));
            }
        };
        queue.add(jsonArrayRequest);
    }

    public void updateUserInfo(GoogleSignInAccount account)
    {
        final String mRequestBody = "{\"userType\":\"google\",\"fullName\":\""+account.getDisplayName()+"\",\"emailAddress\":\""+account.getEmail()+"\"}";
        String tokenid=sp.getString("TokenID",null);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, DatabaseURL+"/api/users/update?userID="+tokenid, new Response.Listener<String>() {
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

                    String tokenid = null;
                    try {
                        tokenid = new String(response.data, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    tokenid = tokenid.substring(tokenid.indexOf("_id") + 6, tokenid.indexOf("userType") - 3);
                    Log.d(TAG, "tokenid: " + tokenid);
                    editor.putString("TokenID", tokenid);
                    editor.apply();
                }
                return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
            }
        };
        queue.add(stringRequest);
    }
}
