package com.example.chenyichen.chatroom;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import java.text.DateFormat;
import java.util.Date;
import android.view.View;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.RelativeLayout;


import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.List;
import java.util.StringTokenizer;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import java.net.URISyntaxException;


/**
 * Created by adsl_chen on 2017/3/28.
 */


public class ChatActivity extends AppCompatActivity {


    private EditText messageET;
    private ListView messagesContainer;
    private Button sendBtn;
    private ChatAdapter adapter;
    private ArrayList<ChatMessage> chatHistory;

    /*    public static String[] titles = new String[] { "Strawberry",
                "Banana", "Orange", "Mixed" };

        public static String[] descriptions = new String[] {
                "It is an aggregate accessory fruit",
                "It is the largest herbaceous flowering plant", "Citrus Fruit",
                "Mixed Fruits" };

        public static Integer[] images = { R.drawable.chaton };
    */
    public SocketHandler socketHandler;
    /*
        private EditText inputMessageView;
        private TextView myMessage, recvMessage;
        private Button send;
    */
    String urls = "http://140.112.18.195:8080/api/messages/";


    // ListView listView;
    //List<RowItem> Messages;

    public static Socket socket;
    public String _receiverId, _receiver, _myId;
    public JSONArray histories;

    // Call when activity is first create
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatt);
        messagesContainer = (ListView) findViewById(R.id.messagesContainer);
        messageET = (EditText) findViewById(R.id.messageEdit);
        sendBtn = (Button) findViewById(R.id.chatSendButton);
        TextView meLabel = (TextView) findViewById(R.id.meLbl);
        TextView companionLabel = (TextView) findViewById(R.id.friendLabel);
        RelativeLayout container = (RelativeLayout) findViewById(R.id.container);
        companionLabel.setText("My Buddy");// Hard Coded
        //adapter = new ChatAdapter(ChatActivity.this, new ArrayList<ChatMessage>());
        //messagesContainer.setAdapter(adapter);
/*
        myMessage = (TextView) findViewById(R.id.text_myMsg);
        recvMessage = (TextView) findViewById(R.id.text_recvMsg) ;
        inputMessageView = (EditText) findViewById(R.id.editText);
        send = (Button) findViewById(R.id.button_send);
*/
        // Get the bundle data from old activity
        Bundle _bundle = getIntent().getExtras();
        _receiverId = _bundle.getString("idTwo");
        _myId = _bundle.getString("idOne");

        urls = urls + "/" + _receiverId + "/" + _myId;
        //_receiver = _bundle.getString("_reveiver");
        Log.v("receiver", _receiverId);
        Log.v("my ID", _myId);

        socket = socketHandler.getSocket();
        /*try{
            //socket = IO.socket("http://140.112.18.195:8080");
            ///socket.connect();
            JSONObject sendText = new JSONObject();
            //sendText.put("data", _id);
            //socket.emit("newUser",sendText);

        }catch(URISyntaxException e){

            Log.d("Error","Cannot connect to server!");
        } catch (JSONException e) {
            Log.v("Error","Cannot put json file.")
        }*/



                //Messages = new ArrayList<RowItem>();
        new HttpAsyncTask().execute(urls);
        loadDummyHistory();
        socket.on("chat", handleIncomingMessages);
        socket.on("err", handleErr);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageET.getText().toString();
                if (TextUtils.isEmpty(messageText)) {
                    return;
                }

                ChatMessage chatMessage = new ChatMessage();
                //chatMessage.setId(122);//dummy
                chatMessage.setMessage(messageText);
                chatMessage.setDate(DateFormat.getDateTimeInstance().format(new Date()));
                chatMessage.setMe(true);

                messageET.setText("");
                displayMessage(chatMessage);

                //String message = inputMessageView.getText().toString();
                //sendMessage(message);
                sendMessage(messageText);
                //inputMessageView.setText("");

            }
        });

    }

    public void displayMessage(ChatMessage message) {
        adapter.add(message);
        adapter.notifyDataSetChanged();
        scroll();
    }

    private void scroll() {
        messagesContainer.setSelection(messagesContainer.getCount() - 1);
    }

    private void sendMessage(String message) {
//        String message = inputMessageView.getText().toString().trim();
//        myMessage.setText(message);
        JSONObject sendText = new JSONObject();
        try {

            sendText.put("receiver", _receiverId);
            sendText.put("msg", message);
            socket.emit("chat", sendText);
            Log.d("Success!", "Successfully send text");
        } catch (JSONException e) {
            Log.v("Error", "Cannot put json file");
        }

    }

    private Emitter.Listener handleIncomingMessages = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("Success", "Successfully run the thread");
                    JSONObject data = (JSONObject) args[0];
                    String message, ID;
                    //String imageText;
                    try {
                        message = data.getString("msg").toString();
                        addMessage(message);
                        ID = data.getString("sender").toString();
                        Log.d("Success!", "Successfully get data");
                        Log.d("Get my ID from server", ID);

                    } catch (JSONException e) {
                        Log.d("Error", "Get json error.");

                    }


                }
            });
        }

    };
    private Emitter.Listener handleErr = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String message, errMsg;
                    //String imageText;
                    try {
                        message = data.getString("error").toString();
                        Log.d("Error", message);
                        //addMessage(message);

                    } catch (JSONException e) {
                        Log.d("Error", "Cannot get json data");

                    }


                }
            });
        }
    };

    private void addMessage(String messageText) {
        ChatMessage msg = new ChatMessage();
        //msg.setId(1);
        msg.setMessage(messageText);
        msg.setMe(false);
        msg.setDate(DateFormat.getDateTimeInstance().format(new Date()));
        displayMessage(msg);

        //recvMessage.setText(message);
    }




    private class HttpAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            return GET(urls[0]);
        }

        // onPostExecute gets the results of the AsyncTask.
        @Override
        protected void onPostExecute(String content) {
            try {
                histories = new JSONArray(content);
            } catch (JSONException e) {

            }


        }
    }


    public String GET(String _url) {
        StringBuilder content = new StringBuilder();
        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(_url);

            HttpResponse httpResponse = httpClient.execute(httpGet);
            InputStream inputStream = httpResponse.getEntity().getContent();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while (null != (line = br.readLine())) {
                content.append(line);
            }

            //setArray(inputStream);
        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }
        return content.toString();
    }

    private void loadDummyHistory() {

        chatHistory = new ArrayList<ChatMessage>();


        for (int i = 0; i < histories.length(); i++) {
            ChatMessage msg = new ChatMessage();
            try {
                JSONObject data = histories.getJSONObject(i);
                if (data.getString("user").matches(_myId)){
                    msg.setMe(true);
                    msg.setMessage(data.getString("message"));
                    msg.setDate(DateFormat.getDateTimeInstance().format(new Date()));
                }
                else if (data.getString("user").matches(_receiverId)){
                    msg.setMe(false);
                    msg.setMessage(data.getString("message"));

                }

            } catch (JSONException e) {
                Log.d("Error","Cannot get history json.");
            }
            chatHistory.add(msg);


        }
     /*   msg.setId(1);
        msg.setMe(false);
        msg.setMessage("Hi");
        msg.setDate(DateFormat.getDateTimeInstance().format(new Date()));
        chatHistory.add(msg);
        ChatMessage msg1 = new ChatMessage();
        msg1.setId(2);
        msg1.setMe(false);
        msg1.setMessage("How r u doing???");
        msg1.setDate(DateFormat.getDateTimeInstance().format(new Date()));

        chatHistory.add(msg1);
*/
        adapter = new ChatAdapter(ChatActivity.this, new ArrayList<ChatMessage>());
        messagesContainer.setAdapter(adapter);

        for (int i = 0; i < chatHistory.size(); i++) {
            ChatMessage message = chatHistory.get(i);
            displayMessage(message);
        }
    }

}
