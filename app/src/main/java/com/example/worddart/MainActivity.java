package com.example.worddart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.provider.MediaStore;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.Buffer;
import java.nio.ByteBuffer;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {
    private static final int RC_SIGN_IN = 9000;
    private static final int RESULT_OK=200;
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
        Log.d(TAG, "onCreate: ");

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

       // Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.proficon_crab)
        //).apply(RequestOptions.circleCropTransform()).into(profile);

        queue = Volley.newRequestQueue(this);
        Log.d(TAG, "onCreate: token-"+(sp.getString("TokenID",null)==null)+" !userExist"+(!userExist()));
        if (sp.getString("TokenID", null)==null// || !userExist()
                 ) {
            createUser();
            Log.d(TAG, "no user found, creating new one");
        }
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
        if (resultCode == RESULT_OK)
        {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
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
            final Uri photo = account.getPhotoUrl();
            new GetImageFromURL(profile, this).execute(photo);
            profile.setBorderColor(Color.WHITE);
            profile.setBorderWidth(3);
            dispName.setText(account.getDisplayName());
            btnGoogle.setVisibility(View.INVISIBLE);
        }
    }

    public void createUser() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("userType", "anonymous");
            obj.put("fullName", "Random_Crab");
            obj.put("emailAddress", "");
//            Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.proficon_crab);
//            ByteBuffer buffer = ByteBuffer.allocate(b.getByteCount());
//            b.copyPixelsToBuffer(buffer);
            obj.put("profImage", "https://i.ibb.co/Wfvshgz/crab.png");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        final String mRequestBody = obj.toString();

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
        JSONObject obj = new JSONObject();
        try {
            obj.put("userType", "google");
            obj.put("fullName", account.getDisplayName());
            obj.put("emailAddress", account.getEmail());
//            Bitmap b = null;
//            new GetImageFromURL(b).execute(account.getPhotoUrl().toString()).get();
//            ByteBuffer buffer = ByteBuffer.allocate(b.getByteCount());
//            b.copyPixelsToBuffer(buffer);
            if(account.getPhotoUrl()!=null)
            obj.put("imageURL", account.getPhotoUrl().toString());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        //final String mRequestBody = "{\"userType\":\"google\",\"fullName\":\""+account.getDisplayName()+"\",\"emailAddress\":\""+account.getEmail()+"\"}";
        final String mRequestBody = obj.toString();
        String tokenid=sp.getString("TokenID",null);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, DatabaseURL+"/api/users/update?userID="+tokenid, response -> Log.i("LOG_RESPONSE", response), error -> Log.e("LOG_RESPONSE", error.toString())) {
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
    public Boolean userExist()
    {
        final String url=DatabaseURL+"/api/users/get/"+sp.getString("TokenID", null);
        // prepare the Request
        final String[] obj = {null};
        final Boolean[] resultReady={false};
        final JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url , (String) null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        // display response
                        Log.d(TAG+" Response", "userExist: "+response.toString());
                        obj[0] =response.toString();
                        Log.d(TAG+" Response", "onResponse: obj[0]: "+obj[0]);
                        resultReady[0]=true;
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG+"Error.Res",error.toString());
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
        //if(resultReady[0]) build asynctask
        Log.d(TAG, "userExist: obj[0]-"+(obj[0]!=null));
        return obj[0]!=null;
    }
//    public class GetImageFromURL extends AsyncTask<String,Void,Bitmap>{
//        private Bitmap bitmap;
//        public GetImageFromURL(Bitmap bitmap)
//        {
//            this.bitmap=bitmap;
//        }
//        @Override
//        protected Bitmap doInBackground(String... strings) {
//            String url=strings[0];
//            bitmap=null;
//            try {
//                InputStream is=new java.net.URL(url).openStream();
//                bitmap=BitmapFactory.decodeStream(is);
//            } catch (MalformedURLException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return bitmap;
//        }
//    }

   public static class GetImageFromURL extends AsyncTask<Uri,Void,Void>{
        private CircleImageView c;
        private WeakReference<MainActivity> activity;
        public GetImageFromURL(CircleImageView c, MainActivity activity)
        {
            this.c=c;
            this.activity= new WeakReference<>(activity);
        }

       @Override
       protected Void doInBackground(Uri... uris) {
            Uri url = uris[0];
            try{
                final Bitmap b = Glide.with(activity.get())
                        .asBitmap()
                        .load(url)
                        .submit().get();
                activity.get().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        c.setImageBitmap(b);
                    }
                });
                                    }
            catch(Exception e){
                e.printStackTrace();
           }
           return null;
       }
   }
}
