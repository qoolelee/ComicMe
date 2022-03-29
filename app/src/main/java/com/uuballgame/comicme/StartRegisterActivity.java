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

public class StartRegisterActivity extends AppCompatActivity {
    protected Context context;

    private EditText emailEditText;
    private EditText idEditText;
    private EditText passwordEditText;
    private EditText cPasswordEditText;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_register);

        // back arrow
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.sregister_actionbar_title);
        actionBar.setDisplayHomeAsUpEnabled(true);

        context = this;

        emailEditText = findViewById(R.id.sregister_email_edittext);
        idEditText = findViewById(R.id.sregister_id_edittext);
        passwordEditText = findViewById(R.id.sregister_password_edittext);
        cPasswordEditText = findViewById(R.id.sregister_cpassword_edittext);
        registerButton = findViewById(R.id.sregister_register_button);

        registerButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                // check if all valid
                if(allValid()){
                    startRegister();
                }
                else{
                    Alert(context.getString(R.string.sregister_error_input), false);
                }
            }
        });
    }

    private void startRegister() {
        String emailString = emailEditText.getText().toString();
        String idString = idEditText.getText().toString();
        String passwordString = passwordEditText.getText().toString();

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.REGISTER_URL;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // set uuid
                        ResultToken resultToken = new Gson().fromJson(response, ResultToken.class);
                        if(resultToken.result.equals("success")) {
                            NewUsername newUsername = new NewUsername("success", idString, passwordString);

                            // save to local preference
                            Constants.NEW_USERNAME = newUsername;
                            Constants.saveToLocalPref(context, AllComicFiltersFragment.USERNAME_TAG, newUsername);

                            // alert
                            Alert(getResources().getString(R.string.sregister_register_success), true);
                        }
                        else{
                            // alert
                            Alert(getResources().getString(R.string.sregister_register_fail), false);
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // alert
                Alert(getResources().getString(R.string.server_maintain_please_try_again), false);
            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("email", emailString);
                map.put("username", idString);
                map.put("password", passwordString);
                return map;
            }

        };

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private boolean allValid() {
        boolean result = false;

        String emailString = emailEditText.getText().toString();
        String idString = idEditText.getText().toString();
        String passwordString = passwordEditText.getText().toString();
        String cPasswordString = cPasswordEditText.getText().toString();

        boolean result1 = (
                android.util.Patterns.EMAIL_ADDRESS.matcher(emailString).matches() &&
                        idString != null &&
                        passwordString != null &&
                        cPasswordString != null
        );

        if(result1){
            boolean result2 = (
                            idString.length()>0 &&
                            passwordString.length()>0 &&
                            cPasswordString.length()>0
            );

            if(result2){
                result = passwordString.equals(cPasswordString);
            }
        }

        return result;
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

    private void Alert(String alertMessage, boolean r) {
        String titleString = "Error";
        String okString = "RETRY";
        if(r){
            titleString = "Success";
            okString = "OK";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(alertMessage)
                .setTitle(titleString)
                .setPositiveButton(okString, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if(r)finish();
                    }
                });

        builder.create().show();
    }
}