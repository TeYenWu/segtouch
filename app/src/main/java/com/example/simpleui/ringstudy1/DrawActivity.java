package com.example.simpleui.ringstudy1;

import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.github.pavlospt.CircleView;

import java.util.ArrayList;
import java.util.List;

public class DrawActivity extends AppCompatActivity {

    int fingerMode = 4;

    DrawTextView drawTextView=null;

    SegTouch segTouch = new SegTouch();

    FrameLayout finger=null;

    List<ImageView> fingers = new ArrayList<>();

    int currentCursor = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);
        startSocket();
        drawTextView = (DrawTextView) findViewById(R.id.drawTextView);
        drawTextView.setMode(2);

        finger = (FrameLayout) findViewById(R.id.finger);

        for(int i=0; i < finger.getChildCount(); i++)
        {
            View view = finger.getChildAt(i);
            if(view.getId() == R.id.imageView)
                continue;
            if(view instanceof ImageView)
            {
                fingers.add((ImageView)view);
            }
        }
//        segTouch.setup();
        setCursor(4);

    }


    MySocket mySocket;
    public void startSocket()
    {
        mySocket = new MySocket();

        mySocket.handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (fingerMode == 0)
                    return false;
                if(msg != null)
                {
                    Bundle bundle = msg.getData();
                    if(bundle!=null)
                    {
                        byte[] result = bundle.getByteArray("results");
                        String re = new String(result);
//                        re.
                        String[] strings = re.split(",");
//                        ByteBuffer buffer = ByteBuffer.wrap(result);
                        double[] args = new double[12];

                        for(int i = 0; i < 12; i++)
                        {
//                            args[i] = buffer.getDouble();
                            try {
                                args[i] = Double.valueOf(strings[i]);
                            }
                            catch (NumberFormatException e)
                            {
                                Log.e("exception", e.getMessage());
                                Log.e("result", strings[i]);
                                Log.e("result", re);
                                return false;
                            }

                        }
                        segTouch.middle[0] = args[0];
                        segTouch.middle[1] = args[1];
                        segTouch.middle[2] = args[2];
                        segTouch.base[0] = args[3];
                        segTouch.base[1] = args[4];
                        segTouch.base[2] = args[5];
                        segTouch.thumb[0] = args[6];
                        segTouch.thumb[1] = args[7];
                        segTouch.thumb[2] = args[8];
                        segTouch.top[0] = args[9];
                        segTouch.top[1] = args[10];
                        segTouch.top[2] = args[11];
//                        Log.d("DebugForVicam middle", String.valueOf(segTouch.middle[0]));
//                        Log.d("DebugForVicam middle", String.valueOf(segTouch.middle[1]));
//                        Log.d("DebugForVicam middle", String.valueOf(segTouch.middle[2]));
//                        Log.d("DebugForVicam base", String.valueOf(segTouch.base[0]));
//                        Log.d("DebugForVicam base", String.valueOf(segTouch.base[1]));
//                        Log.d("DebugForVicam base", String.valueOf(segTouch.base[2]));
//                        Log.d("DebugForVicam top", String.valueOf(segTouch.top[0]));
//                        Log.d("DebugForVicam top", String.valueOf(segTouch.top[1]));
//                        Log.d("DebugForVicam top", String.valueOf(segTouch.top[2]));
//                        Log.d("DebugForVicam thumb", String.valueOf(segTouch.thumb[0]));
//                        Log.d("DebugForVicam thumb", String.valueOf(segTouch.thumb[1]));
//                        Log.d("DebugForVicam thumb", String.valueOf(segTouch.thumb[2]));
                        segTouch.getposition();
                        int index = segTouch.segcursor();
                        setCursor(index);
                    }
                }
                return false;
            }
        });
        Thread thread = new Thread(mySocket);
        thread.start();

//        Socket socket = null;
//        try {
//            socket = IO.socket("http://localhost");
//            socket.on("foo", new Emitter.Listener() {
//                @Override
//                public void call(Object... args) {
//                    JSONObject obj = (JSONObject)args[0];
//
//                }
//            });
//        } catch (URISyntaxException e) {
//            e.printStackTrace();
//        }

    }

    public  void setCursor(int index)
    {

        if(currentCursor != -1 && finger != null && fingers.size() > 0)
        {
            ImageView imageView = fingers.get(currentCursor);
            imageView.setPadding(0, 0, 0, 0);
            imageView.setBackgroundColor(Color.WHITE);
        }

        currentCursor = index;

        if(drawTextView !=null) {
            drawTextView.setMode(currentCursor);
        }

        if(finger != null && fingers.size() > 0)
        {
            if(currentCursor == -1)
            {
                finger.setVisibility(View.GONE);
            }
            else
            {
                finger.setVisibility(View.VISIBLE);
                ImageView imageView = fingers.get(currentCursor);
                imageView.setPadding(15, 15, 15, 15);
                imageView.setBackgroundColor(Color.GREEN);
            }

        }

    }


    @Override
    protected void onStop() {
        mySocket.stop();
        super.onStop();
    }
}
