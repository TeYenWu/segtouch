package com.example.simpleui.ringstudy1;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import java.io.DataInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import android.os.Handler;

/**
 * Created by wudeyan on 8/26/16.
 */
public class MySocket implements Runnable {

    ServerSocket servSock;
    public Handler handler;
    boolean running = true;

    @Override
    public void run() {
        try{
            Log.e("Socket", "Wait For Socket");
            servSock=new ServerSocket(4000);

            Socket clntSock=servSock.accept();
            InputStream in=clntSock.getInputStream();
//            OutputStream out=clntSock.getOutputStream();
            Log.e("Socket", "Connectted");
//            String str = "java server string";
//            System.out.println("(Server端)傳送的字串:"+str);
//            byte[] sendstr = new byte[18];
//            System.arraycopy(str.getBytes(), 0, sendstr, 0, str.length());
//            out.write(sendstr);
//            handler.sendMessage(new Message())

            // Read input from client socket
            byte[] re = new byte[240];

            DataInputStream dis = new DataInputStream(in);
            while (!clntSock.isClosed() && running)
            {
                // Read a line
//                int n = 0;
//                double d = dis.readDouble();
//                char re = dis.readChar();
                int n = dis.read(re);
//                Log.d("Debug", new String(re));
//                Log.i(LOG_TAG, "Read client socket=[" + sLine + "]");
                if (n == -1)
                {
                    continue;
                }
                else
                {
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putByteArray("results", re);
                    message.setData(bundle);
                    handler.sendMessage(message);
                }

            }
        }catch(Exception e)
        {

            Log.d("Socket Error", e.getMessage());
        }
    }

    public void stop()
    {
        running = false;
    }


}
