package com.example.chenyichen.chatroom;

import android.app.Activity;
import android.content.Intent;
//import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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


public class ChatActivity extends AppCompatActivity{


/*    private EditText messageET;
    private ListView messagesContainer;
    private Button sendBtn;
    private ChatAdapter adapter;
    private ArrayList<ChatMessage> chatHistory;
    */
    public static String[] titles = new String[] { "Strawberry",
            "Banana", "Orange", "Mixed" };

    public static String[] descriptions = new String[] {
            "It is an aggregate accessory fruit",
            "It is the largest herbaceous flowering plant", "Citrus Fruit",
            "Mixed Fruits" };

    public static Integer[] images = { R.drawable.chaton };

    public SocketHandler socketHandler;

    private EditText inputMessageView;
    private TextView myMessage, recvMessage;
    private Button send;




   // ListView listView;
    //List<RowItem> Messages;

    public static Socket socket ;
    public String _receiverId, _receiver;

    // Call when activity is first create
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
 /*       messagesContainer = (ListView) findViewById(R.id.messagesContainer);
        messageET = (EditText) findViewById(R.id.messageEdit);
        sendBtn = (Button) findViewById(R.id.chatSendButton);
        TextView meLabel = (TextView) findViewById(R.id.meLbl);
        TextView companionLabel = (TextView) findViewById(R.id.friendLabel);
        RelativeLayout container = (RelativeLayout) findViewById(R.id.container);
        companionLabel.setText("My Buddy");// Hard Coded
*/

        myMessage = (TextView) findViewById(R.id.text_myMsg);
        recvMessage = (TextView) findViewById(R.id.text_recvMsg) ;
        inputMessageView = (EditText) findViewById(R.id.editText);
        send = (Button) findViewById(R.id.button_send);

        // Get the bundle data from old activity
        Bundle _bundle = getIntent().getExtras();
        _receiverId = _bundle.getString("idTwo");
        //_receiver = _bundle.getString("_reveiver");
        //Log.v("receiver", _receiver);

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

        socket.on("chat", handleIncomingMessages);
        socket.on("err", handleErr);
        send.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
       /*         String messageText = messageET.getText().toString();
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
                */
                //String message = inputMessageView.getText().toString();
                //sendMessage(message);
                sendMessage();
                inputMessageView.setText("");

            }
        });




    }
 /*   public void displayMessage(ChatMessage message) {
        adapter.add(message);
        adapter.notifyDataSetChanged();
        scroll();
    }
    private void scroll() {
        messagesContainer.setSelection(messagesContainer.getCount() - 1);
    }
*/
    private void sendMessage(){//String message){
        String message = inputMessageView.getText().toString().trim();
        myMessage.setText(message);
        JSONObject sendText = new JSONObject();
        try{
            sendText.put("receiver",_receiverId);
            sendText.put("msg",message);
            socket.emit("chat", sendText);
        }catch(JSONException e){
            Log.v("Error","Cannot put json file");
        }

    }
    private Emitter.Listener handleIncomingMessages = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String message, errMsg;
                    //String imageText;
                    try {
                        message = data.getString("data").toString();
                        addMessage(message);

                    } catch (JSONException e) {

                    }


                }
            });
        }

    };
    private Emitter.Listener handleErr = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String message, errMsg;
                    //String imageText;
                    try {
                        message = data.getString("data").toString();
                        Log.d("Error", message);
                        //addMessage(message);

                    } catch (JSONException e) {

                    }


                }
            });
        }
    };

    private void addMessage(String message) {
  /*      ChatMessage msg = new ChatMessage();
        //msg.setId(1);
        msg.setMe(false);
        msg.setDate(DateFormat.getDateTimeInstance().format(new Date()));
        displayMessage(msg);
        */
        recvMessage.setText(message);
    }

}
