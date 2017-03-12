package com.example.simpleui.ringstudy1;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class Study3Activity extends AppCompatActivity implements View.OnClickListener {

    enum Type {
      Home, Messenger, MusicPlayer, Painting
    }

    class Target{
        public Target(Type type, int fingerPos, int buttonPos)
        {
            this.type = type;
            this.fingerPos = fingerPos;
            this.buttonPos = buttonPos;
        }
        Type type;
        int fingerPos;
        int buttonPos;
        Long startTimeLong;
        Long endTimeLong;
    }

    int commandNumber = 3;
    int fingerMode = 3;
    int repeatedCount = 10;
    int appearTime = 250;
    int[][] nInCategoryArray = {{4,4,4,4,4}, {4,4,8,4,0}, {8,8,4,0,0}, {4,4,4,4,4}};
    int[] typesToFingerPos = {0,1,2,-1};
    int[][] indexOfButtonsInLayout = {{2,9,12}, {0,9,19}, {3, 5, 13}, {1,6,17}};

    ViewGroup defaultLayout;
    TextView appTextView;
    Button startButton;

    Type[] types = {Type.Home, Type.Messenger, Type.MusicPlayer, Type.Painting};
    List<List<Drawable>> imageList = new ArrayList<>();
    List<ImageButton> buttonList = new ArrayList<>();

    List<ImageView> targetIconList = new ArrayList<>();
    List<List<Target>> targets = new ArrayList<>();
    List<Target> currentTargets =null;

    int currentFingerIndex = -1;
    int currentTargetIndex = 0;

    SegTouch segTouch = new SegTouch();
    boolean isStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study3);

        //hiding default app icon
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setHomeButtonEnabled(false);
//displaying custom ActionBar
        View mActionBarView = getLayoutInflater().inflate(R.layout.study3_menu_layout, null);

        actionBar.setCustomView(mActionBarView);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        startButton = (Button)mActionBarView.findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start();
            }
        });
        targetIconList.add((ImageView) mActionBarView.findViewById(R.id.icon1));
        targetIconList.add((ImageView) mActionBarView.findViewById(R.id.icon2));
        targetIconList.add((ImageView) mActionBarView.findViewById(R.id.icon3));
        appTextView = (TextView) mActionBarView.findViewById(R.id.appTextView);

        defaultLayout = (ViewGroup) findViewById(R.id.root);

        GridLayout gridLayout = (GridLayout) defaultLayout.getChildAt(0);
        for(int index=0; index< gridLayout.getChildCount(); ++index) {
            View v = gridLayout.getChildAt(index);
            if(v instanceof ImageButton)
            {
                ImageButton imageButton = (ImageButton)v;
                imageButton.setOnClickListener(this);
                buttonList.add(imageButton);
            }
        }
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

        String[] permissions = {
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE"
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, 200);
        }

        setupLayout();

        setupRandom();

        startRandom();

        startSocket();

    }
    int currentRepeatCount = 0;

    public void start()
    {
        if(!isStarted) {

            startButton.setText("Start "+String.valueOf(targets.size()));

            currentTargetIndex = 0;

            currentTargets.get(currentTargetIndex).startTimeLong = System.currentTimeMillis();
            isStarted = true;
        }
//        view.setVisibility(View.GONE);
    }

    public void setupLayout()
    {
        for(Type type : types)
        {
            ViewGroup layout= (ViewGroup)getLayoutInflater().inflate(getLayoutID(type), null);
            GridLayout gridLayout = (GridLayout) layout.getChildAt(0);
            List<Drawable> images = new ArrayList<>();
            for(int index=0; index< gridLayout.getChildCount(); ++index) {
                View v = gridLayout.getChildAt(index);
                if(v instanceof ImageButton)
                {
                    ImageButton btn = (ImageButton)v;
                    images.add(btn.getDrawable());
                }
            }
            imageList.add(images);
        }
    }

    public void setupRandom()
    {
//        List<List<Integer>> indexOfButtonsInLayout = new ArrayList<>();
//        for (int i = 0; i < types.length; i++)
//        {
//            Random randomGenerator = new Random();
//            int baseNumber = 0;
//            List<Integer> indexOfButtons = new ArrayList<>();
//            for(int index = 0; index < 5; index ++)
//            {
//                int nButton = nInCategoryArray[i][index];
//                if(nButton == 0 )break;
//                int buttonIndex = randomGenerator.nextInt(nButton);
//                indexOfButtons.add(buttonIndex + baseNumber);
//                baseNumber += nButton;
//            }
//            indexOfButtonsInLayout.add(indexOfButtons);
//        }


        for (int i = 0; i < indexOfButtonsInLayout.length; i++)
        {
            Random randomGenerator = new Random();
            for(int j = 0; j < indexOfButtonsInLayout[i].length; j++) {

                ArrayList<ArrayList<Target>> tmpTargets = new ArrayList<>();
                Target target = new Target(types[i], typesToFingerPos[i], indexOfButtonsInLayout[i][j]);

                ArrayList<Target> commandList = new ArrayList<>();
                commandList.add(target);

                for (int k = 0; k < commandNumber; k++) {

                    if (k == 0) {
                        tmpTargets.add((ArrayList) commandList.clone());
                        if(i==indexOfButtonsInLayout.length-1)
                            break;
                    } else {
                        int indexOfLayout = i + k;
                        if (indexOfLayout > commandNumber-1)
                             indexOfLayout -= commandNumber;
                        int indexOfButton = randomGenerator.nextInt(indexOfButtonsInLayout[i].length-2);

                        Target tmpTarget = new Target(types[indexOfLayout], typesToFingerPos[indexOfLayout], indexOfButtonsInLayout[indexOfLayout][indexOfButton]);
                        commandList.add(tmpTarget);
                        tmpTargets.add((ArrayList) commandList.clone());
                    }


                }

                for (int x = 0; x < tmpTargets.size(); x++)
                {
                    targets.add((ArrayList)tmpTargets.get(x).clone());
                }
            }
        }

    }

    public int getFingerNumber(int fingerMode)
    {
        if(fingerMode == 0)
            return 0;
        else if(fingerMode == 1)
            return 3;
        else if(fingerMode == 2)
            return 4;
        else if(fingerMode == 3)
            return 7;
        else
            return 8;
    }

    @Override
    public void onClick(View v) {
        int index =  buttonList.indexOf((ImageButton)v);
        stop(index);
    }

    public int getLayoutID(Type type)
    {
        switch (type) {
            case Home:
                return R.layout.home_layout;
            case Messenger:
                return R.layout.messenger_layout;
            case MusicPlayer:
                return R.layout.music_player_layout;
            default:
                return R.layout.paint_layout;
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
        currentTargets= targets.get(index);

        targets.remove(index);


        for (int i = 0; i < commandNumber; i++)
        {
            if (i < currentTargets.size()) {
                Target currentTarget = currentTargets.get(i);
                if(currentTarget.fingerPos == typesToFingerPos[1] && currentTarget.buttonPos == 0)
                    targetIconList.get(i).setImageResource(R.drawable.last_contact_1);
                else if (currentTarget.fingerPos == typesToFingerPos[2] && currentTarget.buttonPos == 13)
                    targetIconList.get(i).setImageResource(R.drawable.s90_1);
                else
                {
                    List<Drawable> drawables = imageList.get(currentTarget.type.ordinal());
                    targetIconList.get(i).setImageDrawable(drawables.get(currentTarget.buttonPos));
                }
            }
            else
            {
                targetIconList.get(i).setImageDrawable(null);
            }
        }
    }

    ArrayList<Integer> window = new ArrayList<Integer>();
    public void startSocket()
    {
        MySocket mySocket = new MySocket();
        segTouch.mode = fingerMode;

        mySocket.handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (fingerMode == 0 || !isStarted)
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
                    segTouch.getposition();
                    int currentFingerBTNFromVibCom = segTouch.segcursor();
                    window.add(currentFingerBTNFromVibCom);

                    if(window.size() >= 10)
                        window.remove(0);
                    int majority = Utils.majority(window);

                    Log.d("DebugForVicam ", String.valueOf(majority));
                    if(!isShowResult)
                        setFingerIndex(majority);

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

    long touchFingerTime = 0;

    public void setFingerIndex(int index)
    {
        Long currentTimeMillis = System.currentTimeMillis();

        if(touchFingerTime ==0 && (index != -1 || currentTargets.get(0).fingerPos == -1))
            touchFingerTime = currentTimeMillis;
        else if(touchFingerTime !=0){
            long diff = currentTimeMillis - touchFingerTime;
            int type = fingerToTypeIndex(index);
            setLayout(type, diff > appearTime && isStarted);
        }

        currentFingerIndex = index;

        appTextView.setText("App "+String.valueOf(currentFingerIndex+1));
    }

    public void setLayout(int index, boolean visible)
    {
        List<Drawable> images = new ArrayList<>();
        if(index != -1)
            images = imageList.get(index);
        for (int i = 0; i < buttonList.size(); i++) {
            ImageButton ibtn = buttonList.get(i);
            if (visible && index != -1)
                ibtn.setImageDrawable(images.get(i));
            else
                ibtn.setImageDrawable(null);
        }
    }


    int bError = 0;
    int fError = 0;
    int error = 0;
    int diff = 0;
    private void stop(int index)
    {
        if(!isStarted || isShowResult) return;
        Target currentTarget =  currentTargets.get(currentTargetIndex);
        currentTarget.endTimeLong = System.currentTimeMillis();

        diff += currentTarget.endTimeLong - currentTarget.startTimeLong;
        if(currentFingerIndex != currentTarget.fingerPos) bError++;
        if(index != currentTarget.buttonPos) fError++;
        if(currentFingerIndex != currentTarget.fingerPos || index != currentTarget.buttonPos) error ++;

//        String result = String.valueOf(fingerMode) + ',' + String.valueOf(currentTargetIndex) + ',' + String.valueOf(diff)
//                +',' + String.valueOf(currentTarget.fingerPos)
//                +',' + String.valueOf(currentTarget.buttonPos)
//                +',' + String.valueOf(currentFingerIndex)
//                +',' + String.valueOf(index)
//                +',' + String.valueOf(fHit)
//                +',' + String.valueOf(hit);

        if(currentTargetIndex < currentTargets.size()-1)
        {
            currentTargetIndex++;
        }
        else
        {
            String result = String.valueOf(currentRepeatCount) +','+ String.valueOf(currentTargets.size()) + ','+ String.valueOf(currentTargets.get(0).fingerPos) + ',' + String.valueOf(diff)
                    +',' + String.valueOf(fError)
                    +',' + String.valueOf(bError)
                    +',' + String.valueOf(error);

//                        Log.e("Debug", result);

            Utils.writeFile(this, "results.csv", result + "\n");

            currentTargetIndex=0;
            diff = 0;
            bError = 0;
            fError = 0;
            error = 0;
            isStarted = false;
            if(targets.size() == 0 && currentRepeatCount <= repeatedCount) {
                setupRandom();
                currentRepeatCount++;
            };
            startRandom();
        }

        touchFingerTime = 0;
        showTapResult(currentFingerIndex, index);
//                        startRandom();
    }

    boolean isShowResult = false;
    void showTapResult(final int fingerIndex, final int index)
    {

        int type = fingerToTypeIndex(fingerIndex);
        final Target currentTarget = currentTargets.get(currentTargetIndex);
        if(type == -1) {
            currentTarget.startTimeLong = System.currentTimeMillis();
            return;
        }
        isShowResult = true;
        List<Drawable> images = imageList.get(type);
        for(int i = 0; i < buttonList.size(); i++) {
            final ImageButton imageButton = buttonList.get(i);
            if(index == i) {
                imageButton.setImageDrawable(images.get(i));
                imageButton.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        currentTarget.startTimeLong = System.currentTimeMillis();
                        isShowResult =false;
                        imageButton.setImageDrawable(null);
                        touchFingerTime = 0;

                    }
                }, appearTime*2);
            }
            else
                imageButton.setImageDrawable(null);

        }
    }

    int fingerToTypeIndex(int fingerIndex)
    {
        int type = -1;
        for(int i = 0; i < typesToFingerPos.length; i++) {
            if (typesToFingerPos[i] == fingerIndex) {
                type = i;
                break;
            }
        }
        return type;
    }

//    public void setupAssets()
//    {
//        AssetManager assetManager = getAssets();
//        for (String type: types) {
//            setupImages(assetManager, type);
//        }
//    }
//
//    public void setupImages(AssetManager assetManager, String type)
//    {
//        List<Drawable> list = getList(type);
//        try {
//            String[] fileNames = assetManager.list(type);
//            for (String fileName: fileNames) {
//                list.add(Utils.readImageFromAssets(assetManager, type + "/"+fileName));
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//

}
