package com.uuballgame.comicme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

public class RetrievePasswordActivity extends AppCompatActivity {
    private EditText emailEditText;
    private Button retrieveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retrieve_password);

        // back arrow
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.register_actionbar_title);
        actionBar.setDisplayHomeAsUpEnabled(true);

        // controls
        emailEditText = findViewById(R.id.retrieve_email_edittext);

        retrieveButton = findViewById(R.id.retrieve_retrieve_button);
        retrieveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(emailValidator(emailEditText)){
                    retrievePasswordToEmail(emailEditText);
                }
                else{
                    Toast.makeText(RetrievePasswordActivity.this
                            , RetrievePasswordActivity.this.getResources().getString(R.string.retrieve_email_warning)
                            , Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void retrievePasswordToEmail(EditText etMail) {
        String emailString = etMail.getText().toString();

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = Constants.RETRIEVE_ID_PASSWORD_URL;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // set uuid
                        if(response.equals("success")) {
                            Alert(getResources().getString(R.string.password_sent_to_email), true);
                        }
                        else{
                            Alert(getResources().getString(R.string.email_did_not_exist_please_try_again), true);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // alert
                Alert(getResources().getString(R.string.server_maintain_please_try_again), true);
            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put("email", emailString);
                return map;
            }

            //This is for Headers If You Needed
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Bearer " + Constants.TOKEN);
                return params;
            }

        };

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    // the function which triggered when the VALIDATE button is clicked
    // which validates the email address entered by the user
    public boolean emailValidator(EditText etMail) {

        // extract the entered data from the EditText
        String emailToText = etMail.getText().toString();

        // Android offers the inbuilt patterns which the entered
        // data from the EditText field needs to be compared with
        // In this case the the entered data needs to compared with
        // the EMAIL_ADDRESS, which is implemented same below
        if (!emailToText.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(emailToText).matches()) {
            return true;
        } else {
            return false;
        }
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(alertMessage)
                .setTitle(R.string.error)
                .setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if(r)finish();
                    }
                });

        builder.create().show();
    }
}