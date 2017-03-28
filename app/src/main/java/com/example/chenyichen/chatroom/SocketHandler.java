package com.example.chenyichen.chatroom;

/**
 * Created by adsl_chen on 2017/3/28.
 */
import io.socket.client.Socket;

public class SocketHandler {
    private static Socket socket;

    public static synchronized Socket getSocket(){
        return socket;
    }

    public static synchronized void setSocket(Socket socket){
        SocketHandler.socket = socket;
    }
}
