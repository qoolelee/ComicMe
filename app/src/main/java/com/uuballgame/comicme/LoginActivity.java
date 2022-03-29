package com.uuballgame.comicme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private Context context;
    private EditText idEditText;
    private EditText passwordEditText;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        context = this;

        // back arrow
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.login_actionbar_title);
        actionBar.setDisplayHomeAsUpEnabled(true);

        idEditText = findViewById(R.id.login_id_edittext);
        passwordEditText = findViewById(R.id.login_password_edittext);

        loginButton = findViewById(R.id.login_login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startLogin();
            }
        });
    }

    private void startLogin() {
        // check if input valid
        String idString = idEditText.getText().toString();
        String passwordString = passwordEditText.getText().toString();

        if(idString != null && passwordString != null){
            if(idString.length()>0 && passwordString.length()>0){
                loginGetToken(new NewUsername("success", idString, passwordString));
            }
            else{
                Alert(getResources().getString(R.string.login_error_no_id_password));
            }
        }
        else{
            Alert(getResources().getString(R.string.login_error_no_id_password));
        }
    }

    private void loginGetToken(NewUsername newUsername) {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.LOGIN_URL;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // set uuid
                        ResultToken resultToken = new Gson().fromJson(response, ResultToken.class);
                        if(resultToken.result.equals("success")) {
                            Constants.TOKEN = resultToken.token;

                            // save to local preference
                            Constants.NEW_USERNAME = newUsername;
                            Constants.saveToLocalPref(context, AllComicFiltersFragment.USERNAME_TAG, newUsername);
                        }
                        else{
                            // alert
                            Alert(getResources().getString(R.string.login_invalid_user));
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // alert
                Alert(getResources().getString(R.string.server_maintain_please_try_again));
            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("username", newUsername.username);
                map.put("passsword", newUsername.password);
                return map;
            }

        };

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    // this event will enable the back
    // function to the button on press
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void Alert(String alertMessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(alertMessage)
                .setTitle(R.string.error)
                .setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builder.create().show();
    }
}