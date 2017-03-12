package com.example.simpleui.ringstudy1;

import android.Manifest;
import android.app.Dialog;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.github.pavlospt.CircleView;

import org.json.JSONObject;

import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity {

    int fingerMode = 4;

//    int fingerNumber = 5;

    int columnNumber = 3;
    int rowNumber = 5;
    int margin = 100;
    Long tsLong;
    float smallMM = 7f;
    float bigMM = 10f;

    GridLayout gridLayout;
    FrameLayout frameLayout;
    RelativeLayout finger=null;

    List<Button> buttonList = new ArrayList<>();
    List<CircleView> fingerBtnList = new ArrayList<>();


    List<Integer> targets = new ArrayList<>();
    SegTouch segTouch = new SegTouch();


    int targetBtnPos = -1;
    int targetRep = -1;
    int targetSize = -1;

    Button targetButton = null;

    int targetFingerBTN = -1;
    int currentFingerBTN = -1;

    boolean isStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gridLayout = (GridLayout) findViewById(R.id.root);
        frameLayout = (FrameLayout) findViewById(R.id.frameLayout);

        setupButton();

        setupFinger();

        setupRandom();

//        fingerList.get(1).setVisibility(View.VISIBLE);

        startSocket();

//        ActivityCompat.requestPermissions(MainActivity.this,
//                new String[] {
//                        Manifest.permission.READ_EXTERNAL_STORAGE,
//                        Manifest.permission.WRITE_EXTERNAL_STORAGE
//                },
//                100);

        gridLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                    // the user raised his finger, cancel the Runnable

                    stop(-1);
//                    startRandom();

                }
                return true;
            }
        });




    }

    public void start(View view)
    {

        if(!isStarted) {
            startRandom();
        }
//        view.setVisibility(View.GONE);
    }

    public void setupRandom()
    {
        for(int size = 0; size < 2; size++)
        {
            for(int rep = 0; rep < 2; rep++)
            {
                for(int pos = 0; pos < columnNumber * rowNumber; pos++)
                {
                    targets.add(size*2*columnNumber * rowNumber+rep*columnNumber * rowNumber+pos);
                }

            }

        }
    }

    public void setupButton()
    {
        gridLayout.setColumnCount(columnNumber);
        gridLayout.setRowCount(rowNumber);
        gridLayout.setUseDefaultMargins(false);
        gridLayout.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
        gridLayout.setRowOrderPreserved(false);

        float density  = getResources().getDisplayMetrics().density;

        Rect rectgle= new Rect();
        Window window= getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
        int contentViewTop =
                window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
        int titleBarHeight= contentViewTop - rectgle.top;

        int width = rectgle.width();
        int height = rectgle.height() - (int) (120 * density)  - titleBarHeight;

        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, smallMM,
                getResources().getDisplayMetrics());

        for(int j = 0 ; j < columnNumber ; j++)
        {
            for (int i = 0; i<rowNumber;i++)
            {
                Button btn = new Button(this);
//                btn.setBackgroundColor(Color.RED);

                GridLayout.LayoutParams tr = new GridLayout.LayoutParams();
//                tr.width = width / columnNumber - 2 * margin;
//                tr.height = height / rowNumber - 2 * margin;
                tr.width = (int) px;
                tr.height = (int)px;
                int correct_Xmagin = (width / columnNumber - tr.width)/2;
                int correct_Ymagin = (height / rowNumber  - tr.height)/2;

                tr.setMargins(correct_Xmagin,correct_Ymagin,correct_Xmagin,correct_Ymagin);

                tr.columnSpec = (GridLayout.spec(j,1));
                tr.rowSpec = GridLayout.spec(i,1);


                btn.setLayoutParams(tr);
                btn.setBackgroundResource(R.drawable.button);
//                btn.setBackground(this.getResources().getDrawable( R.drawable.button));
                btn.setHighlightColor(Color.YELLOW);

                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Button button = (Button) v;
                        int currentBTN = buttonList.indexOf(button);
                        stop(currentBTN);
                    }
                });
                btn.setPadding(0,0,0,0);
                gridLayout.addView(btn,tr);
                buttonList.add(btn);


            }
        }
    }

    public void startRandom()
    {
        if(targets.size() == 0)
        {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    this);

            // set title
            alertDialogBuilder.setTitle("Success");

            alertDialogBuilder.create().show();
            return;
        }


        Random randomGenerator = new Random();
        int index = randomGenerator.nextInt(targets.size());
        int pos = targets.get(index);

        targets.remove(index);

        targetSize = pos /  (columnNumber * rowNumber * 2);
        targetRep = pos / (columnNumber * rowNumber) - 2*targetSize;
        targetBtnPos = pos % (columnNumber * rowNumber);

        if (targetSize == 0)
        {
            setBigButtonsLayout();
        }
        else if (targetSize == 1)
        {
            setSmallButtonsLayout();
        }
        targetButton = buttonList.get(targetBtnPos);
        targetButton.setBackgroundResource(R.drawable.button1);



        if(fingerBtnList.size() != 0) {
//            if(targetFingerBTN != -1 )
//            {
//                if(targetFingerBTN != currentFingerBTN) {
//                    fingerBtnList.get(targetFingerBTN).setFillColor((Color.BLACK));
//
//                }
//                fingerBtnList.get(targetFingerBTN).setStrokeColor((Color.BLACK));
//
//            }
            targetFingerBTN = randomGenerator.nextInt(fingerBtnList.size());
            CircleView circleView = fingerBtnList.get(targetFingerBTN);
            if(currentFingerBTN != targetFingerBTN)
                circleView.setFillColor(Color.GREEN);
            circleView.setStrokeColor(Color.GREEN);

        }
        else
            targetFingerBTN = -1;


//
        tsLong = System.currentTimeMillis();
//
        if(finger!=null)
        {
//            segTouch.getposition();
            int currentFingerBTNFromVibCom = targetFingerBTN;
//            int normalRadius = fingerMode == 5 ? 20 : 30;
            if(currentFingerBTN!=-1 && currentFingerBTN != currentFingerBTNFromVibCom) {
                CircleView circleView = fingerBtnList.get(currentFingerBTN);
                circleView.setFillRadius(1.1f);
                if(currentFingerBTN == targetFingerBTN)
                {
                    circleView.setFillColor(Color.GREEN);
                    circleView.setStrokeColor(Color.GREEN);
//                                fingerBtnList.get(currentFingerBTN).setFillColor(Color.GREEN);
                }
                else {
                    circleView.setFillColor(Color.BLACK);
                    circleView.setStrokeColor(Color.BLACK);
//                                fingerBtnList.get(currentFingerBTN).setFillColor(Color.BLACK);
                }
            }

//                        segTouch.segcursor();
            currentFingerBTN = currentFingerBTNFromVibCom;
            if(currentFingerBTN != -1) {
                CircleView circleView = fingerBtnList.get(currentFingerBTN);
                circleView.setFillRadius(1f);
                circleView.setFillColor(Color.RED);

//                circleView.setStrokeColor(Color.WHITE);
            }
        }
        isStarted = true;
    }

    public void setSmallButtonsLayout()
    {

        float density  = getResources().getDisplayMetrics().density;

        Rect rectgle= new Rect();
        Window window= getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
        int contentViewTop =
                window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
        int titleBarHeight= contentViewTop - rectgle.top;
        int width = rectgle.width();
        int height = rectgle.height() - (int) (120 * density)  - titleBarHeight;

        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, smallMM,
                getResources().getDisplayMetrics());

        for(int j = 0 ; j < columnNumber ; j++)
        {
            for (int i = 0; i<rowNumber;i++)
            {
                Button btn = buttonList.get(i + j *rowNumber);
//                btn.setBackgroundColor(Color.RED);

                GridLayout.LayoutParams tr = new GridLayout.LayoutParams();
//                tr.width = width / columnNumber - 2 * margin;
//                tr.height = height / rowNumber - 2 * margin;
                tr.width = (int) px;
                tr.height = (int)px;
                int correct_Xmagin = (width / columnNumber - tr.width)/2;
                int correct_Ymagin = (height / rowNumber  - tr.height)/2;

                tr.setMargins(correct_Xmagin,correct_Ymagin,correct_Xmagin,correct_Ymagin);

                tr.columnSpec = (GridLayout.spec(j,1));
                tr.rowSpec = GridLayout.spec(i,1);


                btn.setLayoutParams(tr);
                btn.setPadding(0,0,0,0);
            }
        }
    }

    public void setBigButtonsLayout()
    {

        float density  = getResources().getDisplayMetrics().density;

        Rect rectgle= new Rect();
        Window window= getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
        int contentViewTop =
                window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
        int titleBarHeight= contentViewTop - rectgle.top;
        int width = rectgle.width();
        int height = rectgle.height() - (int) (120 * density)  - titleBarHeight;

        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, bigMM,
                getResources().getDisplayMetrics());

        for(int j = 0 ; j < columnNumber ; j++)
        {
            for (int i = 0; i<rowNumber;i++)
            {
                Button btn = buttonList.get(i + j *rowNumber);
//                btn.setBackgroundColor(Color.RED);

                GridLayout.LayoutParams tr = new GridLayout.LayoutParams();
//                tr.width = width / columnNumber - 2 * margin;
//                tr.height = height / rowNumber - 2 * margin;
                tr.width = (int) px;
                tr.height = (int)px;
                int correct_Xmagin = (width / columnNumber - tr.width)/2;
                int correct_Ymagin = (height / rowNumber  - tr.height)/2;

                tr.setMargins(correct_Xmagin,correct_Ymagin,correct_Xmagin,correct_Ymagin);

                tr.columnSpec = (GridLayout.spec(j,1));
                tr.rowSpec = GridLayout.spec(i,1);


                btn.setLayoutParams(tr);
                btn.setPadding(0,0,0,0);
            }
        }
    }

    public void setupFinger()
    {
//        for(int i =0; i <fingerNumber ; i ++)
//        {
//            String id = "finger" + String.valueOf(i);
//            int resID = getResources().getIdentifier(id,
//                    "layout", getPackageName());
//            View finger = View.inflate(this, resID, null);
//            finger.setVisibility(View.GONE);
//            frameLayout.addView(finger);
//            fingerList.add(finger);
//        }

        String id = "finger" + String.valueOf(fingerMode);
        int resID = getResources().getIdentifier(id,
                "layout", getPackageName());
        finger = (RelativeLayout)View.inflate(this, resID, null);


//        finger.setVisibility(View.GONE);
        frameLayout.addView(finger);
//        fingerList.add(finger);

        findAllBtns(finger);

        segTouch.mode = fingerMode;
    }


    private void findAllBtns(ViewGroup viewGroup) {

        int count = viewGroup.getChildCount();
        for (int i = 0; i < count; i++) {
            View view = viewGroup.getChildAt(i);
            if (view instanceof ViewGroup)
                findAllBtns((ViewGroup) view);
            else if (view instanceof CircleView) {
                CircleView btn = (CircleView) view;
                fingerBtnList.add(btn);
            }
        }

    }

    public void startSocket()
    {
        MySocket mySocket = new MySocket();

        mySocket.handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (fingerMode == 0 || isStarted == false)
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
                    }
                    if(finger!=null)
                    {
                        segTouch.getposition();
                        int currentFingerBTNFromVibCom = segTouch.segcursor();
                        if(currentFingerBTN!=-1 && currentFingerBTN != currentFingerBTNFromVibCom) {
                            CircleView circleView = fingerBtnList.get(currentFingerBTN);
                            circleView.setFillRadius(1.1f);
                            if(currentFingerBTN == targetFingerBTN)
                            {
                                circleView.setFillColor(Color.GREEN);
                                circleView.setStrokeColor(Color.GREEN);
//                                fingerBtnList.get(currentFingerBTN).setFillColor(Color.GREEN);
                            }
                            else {
                                circleView.setFillColor(Color.BLACK);
                                circleView.setStrokeColor(Color.BLACK);
//                                fingerBtnList.get(currentFingerBTN).setFillColor(Color.BLACK);
                            }
                        }

//                        segTouch.segcursor();
                        currentFingerBTN = currentFingerBTNFromVibCom;
                        if(currentFingerBTN != -1) {
//                            CircleView BTN = fingerBtnList.get(currentFingerBTN);
                            CircleView circleView = fingerBtnList.get(currentFingerBTN);
                            circleView.setFillRadius(1f);
                            circleView.setFillColor(Color.RED);
                        }
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
    private void stop(int index)
    {


        Long resultLong = System.currentTimeMillis();
        Long diff = resultLong - tsLong;
        int fHit = 0;
        if(targetFingerBTN==currentFingerBTN)
            fHit = 1;
        int hit = 0;
        if(targetBtnPos==index)
            hit = 1;

        String s = targetSize == 0 ? "s" : "b";

        String result = String.valueOf(fingerMode) + ',' + String.valueOf(s) + ',' + String.valueOf(diff)
                +',' + String.valueOf(targetFingerBTN)
                +',' + String.valueOf(targetBtnPos)
                +',' + String.valueOf(currentFingerBTN)
                +',' + String.valueOf(index)
                +',' + String.valueOf(fHit)
                +',' + String.valueOf(hit);

//                        Log.e("Debug", result);
        Utils.writeFile(MainActivity.this, "results" + String.valueOf(fingerMode) + ".csv", result + "\n");
        if(targetBtnPos != -1) {
            Button btn = buttonList.get(targetBtnPos);
            btn.setBackgroundResource(R.drawable.button);
            targetBtnPos = -1;
        }
//        if(index != -1) {
//            Button btn = buttonList.get(index);
//            btn.setBackgroundResource(R.drawable.button);
//            targetBtnPos = -1;
//        }

        if(targetFingerBTN != -1)
        {
            CircleView circleView = fingerBtnList.get(targetFingerBTN);
            circleView.setFillColor(Color.BLACK);
            circleView.setStrokeColor(Color.BLACK);
            targetFingerBTN = -1;
        }

        if(currentFingerBTN != -1) {
//                            CircleView BTN = fingerBtnList.get(currentFingerBTN);
            CircleView circleView = fingerBtnList.get(currentFingerBTN);
            circleView.setFillRadius(1.1f);
            circleView.setFillColor(Color.BLACK);
            circleView.setStrokeColor(Color.BLACK);
        }

        isStarted = false;


//                        startRandom();
    }

}
