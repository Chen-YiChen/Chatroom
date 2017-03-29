package com.example.chenyichen.chatroom;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;

public class LoginActivity extends Activity implements OnClickListener {

    EditText et_username, et_password;
    Button btnPost;
    String url = "http://140.112.18.195:8080/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // get reference to the views
        et_username = (EditText) findViewById(R.id.input_username);
        et_password = (EditText) findViewById(R.id.input_password);
        btnPost = (Button) findViewById(R.id.btn_login);

        // add click listener to Button "POST"
        btnPost.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {

        switch(view.getId()){
            case R.id.btn_login:
                if(!validate())
                    return;
                // call AsynTask to perform network operation on separate thread
                new HttpAsyncTask().execute(url + "api/users/");
                break;
        }

    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {

        String username = et_username.getText().toString();
        String password = et_password.getText().toString();

        @Override
        protected String doInBackground(String... urls) {
            return POST(urls[0], username, password);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String _id) {
            if(_id.matches("401")) {
                //Log.v("status", _id);
                Toast.makeText(getBaseContext(), "Wrong username or password!", Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(getBaseContext(), "Successful login!", Toast.LENGTH_LONG).show();
                switchToWaitActivity(_id, username, password);
            }
        }
    }

    public static String POST(String _url, String username, String password){
        InputStream inputStream = null;
        String _id = null;
        try {

            // 1. create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // 2. make POST request to the given URL
            HttpPost httpPost = new HttpPost(_url);

            String json = "";

            // 3. build jsonObject
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("username", username);
            jsonObject.accumulate("password", password);

            // 4. convert JSONObject to JSON to String
            json = jsonObject.toString();

            // ** Alternative way to convert Person object to JSON string usin Jackson Lib
            // ObjectMapper mapper = new ObjectMapper();
            // json = mapper.writeValueAsString(person);

            // 5. set json to StringEntity
            StringEntity se = new StringEntity(json);

            // 6. set httpPost Entity
            httpPost.setEntity(se);

            // 7. set some headers to inform server about the type of the content
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            httpPost.addHeader("content-type", "application/x-www-form-urlencoded");

            // 8. execute POST request to the given URL
            HttpResponse httpResponse = httpclient.execute(httpPost);

            // 9. receive status code
            int code = httpResponse.getStatusLine().getStatusCode();
            //Log.v("status code", String.valueOf(code));
            if (code != 200) {
                return String.valueOf(code);
            }

            // 10. receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder content = new StringBuilder();
            String line;
            while (null != (line = br.readLine())) {
                content.append(line);
            }
            JSONObject result = new JSONObject(content.toString());
            _id = result.getString("_id");

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        // 11. return result
        return _id;
    }

    private void switchToWaitActivity(String _id, String username, String password){
        Intent _intent = new Intent();
        _intent.setClass(LoginActivity.this, WaitActivity.class);

        Bundle _bundle = new Bundle();
        _bundle.putString("_id", _id);
        _bundle.putString("username", username);
        _bundle.putString("password", password);
        _bundle.putString("url", url);

        _intent.putExtras(_bundle);
        startActivity(_intent);
    }

    public boolean validate() {
        boolean valid = true;

        String username = et_username.getText().toString();
        String password = et_password.getText().toString();

        if (username.isEmpty()) {
            et_username.setError("enter a valid username.");
            valid = false;
        } else {
            et_username.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            et_password.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            et_password.setError(null);
        }

        return valid;
    }
}