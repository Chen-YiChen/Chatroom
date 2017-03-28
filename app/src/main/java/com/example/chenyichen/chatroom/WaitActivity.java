package com.example.chenyichen.chatroom;

import android.app.Activity;
import android.content.Intent;
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

import java.util.ArrayList;
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

import io.socket.client.IO;
import io.socket.client.Socket;
import java.net.URISyntaxException;
/**
 * Created by chenyichen on 3/22/17.
 */

public class WaitActivity extends Activity implements
        OnItemClickListener {

    public static String[] titles = new String[] { "Strawberry",
            "Banana", "Orange", "Mixed" };

    public static String[] descriptions = new String[] {
            "It is an aggregate accessory fruit",
            "It is the largest herbaceous flowering plant", "Citrus Fruit",
            "Mixed Fruits" };

    public static Integer[] images = { R.drawable.chaton };

    ListView listView;
    List<RowItem> rowItems;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait);

        Bundle _bundle = getIntent().getExtras();
        String _id = _bundle.getString("_id");
        String username = _bundle.getString("username");
        String password = _bundle.getString("password");
        Log.v("username", username);

        TextView text_username = (TextView)findViewById(R.id.text_username);
        text_username.setText("Welcome! " + username);
        Log.v("test", "test");

        Socket socket;

        try{
            socket = IO.socket("http://140.112.18.195:8080");
            socket.connect();
            socket.emit("foo","Hi !! Success !");

        }catch(URISyntaxException e){

            Log.d("Error","Cannot connect to server!");
        }

        getUsers("http://140.112.18.195:8080/api/users/");

        rowItems = new ArrayList<RowItem>();
        for (int i = 0; i < titles.length; i++) {
            RowItem item = new RowItem(images[0], titles[i], descriptions[i]);
            rowItems.add(item);
        }

        listView = (ListView) findViewById(R.id.list);
        CustomBaseAdapter adapter = new CustomBaseAdapter(this, rowItems);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }

    public void getUsers(String url){
        HttpClient http = new DefaultHttpClient();
        StringBuilder buffer = null;
        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(url);

            HttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity httpEntity = httpResponse.getEntity();
            String output = EntityUtils.toString(httpEntity);
        }catch(Exception e) {
            Log.d("Error", "getUsers Errored!");
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        Toast toast = Toast.makeText(getApplicationContext(),
                "Chat with " + rowItems.get(position) + " !",
                Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();

        // switchToChatActivity
    }

    /*private void switchToChatActivity(String _id, String username, String password){
        Intent _intent = new Intent();
        _intent.setClass(WaitActivity.this, ChatActivity.class);

        Bundle _bundle = new Bundle();
        _bundle.putString("_id", _id);
        _bundle.putString("username", username);
        _bundle.putString("password", password);

        _intent.putExtras(_bundle);
        startActivity(_intent);
    }*/
}
