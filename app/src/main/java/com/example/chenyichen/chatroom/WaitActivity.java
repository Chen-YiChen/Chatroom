package com.example.chenyichen.chatroom;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import java.net.URISyntaxException;
import java.util.concurrent.RunnableFuture;

/**
 * Created by chenyichen on 3/22/17.
 */

public class WaitActivity extends Activity implements
        OnItemClickListener {

    public static ArrayList<String> users = new ArrayList<String>();
    public static ArrayList<String> status = new ArrayList<String>();
    public static ArrayList<String> ids = new ArrayList<String>();

    public static Integer[] images = { R.drawable.chaton };

    //public static Socket

    ListView listView;
    List<RowItem> rowItems;
    String url = null;
    String _id = null;
    Socket socket = null;
    //SocketHandler socketHandler;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait);

        Bundle _bundle = getIntent().getExtras();
        _id = _bundle.getString("_id");
        String username = _bundle.getString("username");
        String password = _bundle.getString("password");
        url = _bundle.getString("url");

        TextView text_username = (TextView)findViewById(R.id.text_username);
        text_username.setText("Welcome! " + username);


        try {
            socket = IO.socket(url);
        } catch (URISyntaxException e) {
            Log.d("Error", "cannnot initialize socket");
        }
        socket.connect();
        JSONObject sendMessage = new JSONObject();
        try {
            //Log.v("id", _id);
            sendMessage.put("_id",_id);
        } catch (JSONException e) {
            Log.d("Error", "cannot put message into json");
        }
        socket.emit("newUser",sendMessage);

        new WaitActivity.HttpAsyncTask().execute(url + "api/users/");

        socket.on("online", new Emitter.Listener(){
            @Override
            public void call(final Object... args){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONArray result = (JSONArray) args[0];
                        try {
                            Log.v("online", "online");
                            setArray(result);
                            setRowItem();
                        }catch(Exception e) {
                            Log.d("Error", e.getLocalizedMessage());
                        }
                    }
                } );
            }
        });

        socket.on("chat", new Emitter.Listener(){
            @Override
            public void call(Object... args){
                try {
                    JSONObject result = (JSONObject) args[0];
                    Log.v("chat", result.toString());
                    String idTwo = result.getString("sender");
                    switchToChatActivity(_id, idTwo);
                    Log.d("Error","Still have wait socket on chat");
                }catch(Exception e) {
                    Log.d("Error", e.getLocalizedMessage());
                }
            }
        });
    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            return GET(urls[0]);
        }
        /*
        // onPostExecute gets the results of the AsyncTask.
        @Override
        protected void onPostExecute(String dummy) {
        }
        */
    }

    public String GET(String _url){
        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(_url);

            HttpResponse httpResponse = httpClient.execute(httpGet);
            InputStream inputStream = httpResponse.getEntity().getContent();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder content = new StringBuilder();
            String line;
            while (null != (line = br.readLine())) {
                content.append(line);
            }
            JSONArray result = new JSONArray(content.toString());
            setArray(result);
            setRowItem();
        }catch(Exception e) {
            Log.d("Error", e.getLocalizedMessage());
        }
        return null;
    }

    public void setArray(JSONArray result) throws JSONException, IOException {
        users = new ArrayList<String>();
        status = new ArrayList<String>();
        ids = new ArrayList<String>();
        for (int i=0; i<result.length(); i++){
            JSONObject user = result.getJSONObject(i);
            if (user.getString("online").matches("true")) {
                status.add("ONLINE");
                users.add(user.getString("username"));
                ids.add(user.getString("_id"));
            }
        }
        for (int i=0; i<result.length(); i++){
            JSONObject user = result.getJSONObject(i);
            if (user.getString("online").matches("false")) {
                status.add("OFFLINE");
                users.add(user.getString("username"));
                ids.add(user.getString("_id"));
            }
        }
    }

    public void setRowItem(){
        rowItems = new ArrayList<RowItem>();
        for (int i = 0; i < users.size(); i++) {
            if (status.get(i).matches("ONLINE")) {
                RowItem item = new RowItem(images[0], users.get(i), status.get(i)/*, ids.get(i)*/);
                rowItems.add(item);
            }
        }
        for (int i = 0; i < users.size(); i++) {
            if (status.get(i).matches("OFFLINE")) {
                RowItem item = new RowItem(images[0], users.get(i), status.get(i)/*, ids.get(i)*/);
                rowItems.add(item);
            }
        }
        listView = (ListView) findViewById(R.id.list);
        CustomBaseAdapter adapter = new CustomBaseAdapter(this, rowItems);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        /*
        if (status.get(position).matches("OFFLINE")){
            Toast toast = Toast.makeText(getApplicationContext(),
                    "The user is OFFLINE...",
                    Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
            return;
        }
        */
        Toast toast = Toast.makeText(getApplicationContext(),
                "Chat with " + users.get(position) + " !",
                Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();
        switchToChatActivity(_id, ids.get(position));
    }

    private void switchToChatActivity(String idOne, String idTwo){

        Intent _intent = new Intent();
        _intent.setClass(WaitActivity.this, ChatActivity.class);
        //socketHandler.setSocket(socket);
        Bundle _bundle = new Bundle();
        _bundle.putString("idOne", idOne);
        _bundle.putString("idTwo", idTwo);

        _intent.putExtras(_bundle);
        socket.disconnect();
        startActivity(_intent);
    }
}
