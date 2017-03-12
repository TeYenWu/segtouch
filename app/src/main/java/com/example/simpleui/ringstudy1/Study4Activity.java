package com.example.simpleui.ringstudy1;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.github.pavlospt.CircleView;
import com.github.pwittchen.swipe.library.Swipe;
import com.github.pwittchen.swipe.library.SwipeEvent;
import com.github.pwittchen.swipe.library.SwipeListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import rx.Subscription;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class Study4Activity extends AppCompatActivity {

    class Target{
        public Target(int repetition, int commandType, int gesture)
        {
            this.rep = repetition;
            this.commandType = commandType;
            this.gesture = gesture;

        }
        int rep;
        int gesture;
        int commandType;
        Long startTimeLong;
        Long endTimeLong;
    }

    RelativeLayout layout;
    RelativeLayout finger;
    RelativeLayout content;
    ImageView swipeImageView;
    ImageView dragImageView;
    ImageView dropImageView;
    Button startButton;
    List<CircleView> fingerBtnList = new ArrayList<>();
    List<Target> targets = new ArrayList<>();
    Target currentTarget = null;
    int[] typesToLayout = {R.drawable.study4_app1_border, R.drawable.study4_app2_border, R.drawable.study4_app3_border,
            R.drawable.app1, R.drawable.app2, R.drawable.app3}; // Default, Blue, Red, Cyber, Home, Messenger, Music
    int[] typesToFingerIndex = {0,1,2,3,4,5};
    SegTouch segTouch = new SegTouch();
    boolean isStarted = false;
    int currentTargetIndex= 0;
    int currentFingerIndex = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study4);

        //hiding default app icon
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setHomeButtonEnabled(false);
//displaying custom ActionBar
        View mActionBarView = getLayoutInflater().inflate(R.layout.study4_menu_layout, null);
        mActionBarView.setMinimumHeight(300);
        finger = (RelativeLayout) mActionBarView.findViewById(R.id.menuRoot);

        ActionBar.LayoutParams lp = new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL);

        actionBar.setCustomView(mActionBarView, lp);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);

        startButton = (Button)mActionBarView.findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start();
            }
        });

        layout = (RelativeLayout) findViewById(R.id.root);
        content = (RelativeLayout) findViewById(R.id.content);
        swipeImageView = (ImageView)findViewById(R.id.swipe);
        dragImageView = (ImageView)findViewById(R.id.dragImageView);
        dropImageView = (ImageView)findViewById(R.id.dropImageView);

        String[] permissions = {
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE"
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && this.checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") == PackageManager.PERMISSION_DENIED) {
            requestPermissions(permissions, 200);
        }

        setupFinger();

        setupSwipe();

        setupDragAndDrop();

        setupRandom();

        startSocket();

    }

    public void setupFinger()
    {
        segTouch.mode = 3;
        findAllBtns(finger);
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
    Swipe swipe;
    void setupSwipe(){
        swipe = new Swipe();
        swipe.addListener(new SwipeListener() {
            @Override
            public void onSwipingLeft(MotionEvent event) {

            }

            @Override
            public void onSwipedLeft(MotionEvent event) {

                stop(2);
            }

            @Override
            public void onSwipingRight(MotionEvent event) {

            }

            @Override
            public void onSwipedRight(MotionEvent event) {
                stop(0);
            }

            @Override
            public void onSwipingUp(MotionEvent event) {

            }

            @Override
            public void onSwipedUp(MotionEvent event) {
                stop(3);
            }

            @Override
            public void onSwipingDown(MotionEvent event) {

            }

            @Override
            public void onSwipedDown(MotionEvent event) {
                stop(1);
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(isStarted && currentTarget != null && currentTarget.gesture < 4)
            swipe.dispatchTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    public void start()
    {
        if(!isStarted) {

            startButton.setText("Start "+String.valueOf(targets.size()));

            startRandom();

            setupTargetLayout();

            currentTarget.startTimeLong = System.currentTimeMillis();

            isStarted = true;
        }
//        view.setVisibility(View.GONE);
    }

    public void setupDragAndDrop()
    {
        dragImageView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    // Construct draggable shadow for view
                    View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                    // Start the drag of the shadow
                    view.startDrag(null, shadowBuilder, view, 0);
                    // Hide the actual view as shadow is being dragged
                    view.setVisibility(View.INVISIBLE);
                    return true;
                } else {
                    return false;
                }
            }
        });

        dropImageView.setOnDragListener(new View.OnDragListener() {
//            // Drawable for when the draggable enters the drop target
//            Drawable enteredZoneBackground = getResources().getDrawable(R.drawable.shape_border_green);
//            // Drawable for the default background of the drop target
//            Drawable defaultBackground = getResources().getDrawable(R.drawable.shape_border_red);

            @Override
            public boolean onDrag(View v, DragEvent event) {
                // Get the dragged view being dropped over a target view
                final View draggedView = (View) event.getLocalState();
                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        // Signals the start of a drag and drop operation.
                        // Code for that event here
                        break;
                    case DragEvent.ACTION_DRAG_ENTERED:
                        // Signals to a View that the drag point has
                        // entered the bounding box of the View.
//                        v.setBackground(enteredZoneBackground);
                        break;
                    case DragEvent.ACTION_DRAG_EXITED:
                        // Signals that the user has moved the drag shadow
                        // outside the bounding box of the View.
//                        v.setBackground(defaultBackground);
                        break;
                    case DragEvent.ACTION_DROP:
                        // Signals to a View that the user has released the drag shadow,
                        // and the drag point is within the bounding box of the View.
                        // Get View dragged item is being dropped on
                        View dropTarget = v;
                        // Make desired changes to the drop target below
                        dropTarget.setTag("dropped");
                        // Get owner of the dragged view and remove the view (if needed)
//                        ViewGroup owner = (ViewGroup) draggedView.getParent();
//                        owner.removeView(draggedView);
                        draggedView.setVisibility(View.INVISIBLE);
                        stop(currentTarget.gesture);
                        break;
                    case DragEvent.ACTION_DRAG_ENDED:
                        // Signals to a View that the drag and drop operation has concluded.
                        // If event result is set, this means the dragged view was dropped in target
                        if (event.getResult()) { // drop succeeded
                            stop(currentTarget.gesture);
                        } else { // drop did not occur
                            stop(-1);
                        }
                    default:
                        break;
                }
                return true;
            }
        });
    }

    int repetition = 5;
    int nGestures = 12;
    int nCommandType = 3;

    public void setupRandom()
    {
        for(int rep = 0; rep < repetition; rep++)
        {
            for(int commandType = 0; commandType < nCommandType; commandType++)
            {
                for(int gesture = 0; gesture < nGestures; gesture++)
                {
                    Target target = new Target(rep, commandType, gesture);
                    targets.add(target);
                }
            }

        }
    }
    int trialCount =0;
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
        if(trialCount >= nGestures*nCommandType){
            trialCount = 0;
        }
        int index = randomGenerator.nextInt(nGestures*nCommandType - trialCount);
        currentTarget= targets.get(index);

        trialCount++;
        targets.remove(index);
    }

    void setupTargetLayout()
    {
        int gesture = currentTarget.gesture;
        if(gesture < 4) // swipe
        {
            swipeImageView.setVisibility(View.VISIBLE);
            dropImageView.setVisibility(View.INVISIBLE);
            dragImageView.setVisibility(View.INVISIBLE);

            RectF drawableRect = new RectF(0, 0, swipeImageView.getWidth(), swipeImageView.getHeight());
            RectF viewRect = new RectF(0, 0, content.getWidth(), content.getHeight());
            float leftOffset = (swipeImageView.getMeasuredWidth() - 200) / 2f;
            float topOffset = (swipeImageView.getMeasuredHeight() - 200) / 2f;


            Matrix matrix = new Matrix();
            matrix.postScale(0.5f,0.5f);
            matrix.postTranslate(leftOffset, topOffset);
            if(currentTarget.commandType == 0)
                swipeImageView.setBackground(ContextCompat.getDrawable(this,R.drawable.study4_image_border1));
            else if (currentTarget.commandType == 1)
                swipeImageView.setBackground(ContextCompat.getDrawable(this,R.drawable.study4_image_border2));
            else if (currentTarget.commandType ==2)
                swipeImageView.setBackground(ContextCompat.getDrawable(this,R.drawable.study4_image_border3));

            matrix.postRotate((float) gesture * 90, swipeImageView.getWidth()/2, swipeImageView.getHeight()/2);
            swipeImageView.setImageMatrix(matrix);
        }
        else //drag
        {
            swipeImageView.setVisibility(View.INVISIBLE);
            dropImageView.setVisibility(View.VISIBLE);
            dragImageView.setVisibility(View.VISIBLE);

            if(currentTarget.commandType == 0)
                dropImageView.setBackground(ContextCompat.getDrawable(this,R.drawable.study4_image_border1));
            else if (currentTarget.commandType == 1)
                dropImageView.setBackground(ContextCompat.getDrawable(this,R.drawable.study4_image_border2));
            else if (currentTarget.commandType ==2)
                dropImageView.setBackground(ContextCompat.getDrawable(this,R.drawable.study4_image_border3));
            double x,y =0;
            x =  50 * Math.cos(Math.toRadians((gesture - 4)*45))+ dragImageView.getPivotX();
            y = 50 * Math.sin(Math.toRadians((gesture - 4)*45))+ dragImageView.getPivotY();
            dropImageView.setPivotX((float)x );
            dropImageView.setPivotY((float)y);
        }

    }

    public void startSocket()
    {
        MySocket mySocket = new MySocket();

        mySocket.handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (isStarted == false)
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
//                        segTouch.segcursor();
                        if(currentFingerIndex != -1) {
//                            CircleView BTN = fingerBtnList.get(currentFingerBTN);
                            CircleView circleView = fingerBtnList.get(currentFingerIndex);
                            circleView.setFillRadius(1f);
                            circleView.setFillColor(Color.BLACK);
                        }

                        currentFingerIndex = currentFingerBTNFromVibCom;
                        if(currentFingerIndex != -1) {
//                            CircleView BTN = fingerBtnList.get(currentFingerBTN);
                            CircleView circleView = fingerBtnList.get(currentFingerIndex);
                            circleView.setFillRadius(1f);
                            circleView.setFillColor(Color.RED);
                        }
                        setupLayout(currentFingerIndex);
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


    private void setupLayout(int index){

        for(int i = 0; i <typesToFingerIndex.length; i++)
        {
            if(index == typesToFingerIndex[i])
            {
                layout.setBackground(ContextCompat.getDrawable(this, typesToLayout[i]));
                if(i < 3)
                    content.setVisibility(View.VISIBLE);
                else
                    content.setVisibility(View.INVISIBLE);

            }

        }
    }

    private void stop(int index)
    {
        if(!isStarted) return;

        if(swipeImageView != null && currentTarget.gesture < 4)
            swipeImageView.setVisibility(View.INVISIBLE);

        currentTarget.endTimeLong = System.currentTimeMillis();

        Long diff = currentTarget.endTimeLong - currentTarget.startTimeLong;

        String result = String.valueOf(currentTarget.rep) + ',' + String.valueOf(currentTarget.commandType) + ',' + String.valueOf(currentTarget.gesture)
                +',' + String.valueOf(currentFingerIndex)
                +',' + String.valueOf(index)
                +',' + String.valueOf(diff);
        Utils.writeFile(this, "results.csv", result + "\n");
        isStarted = false;
//                        startRandom();
    }

}
