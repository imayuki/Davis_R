package com.imayuki.davis_r;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.freedesktop.gstreamer.GStreamer;

import java.io.IOException;
import java.net.DatagramSocket;

public class MainActivity extends AppCompatActivity  {

    private native void nativeInit(String input_address, String output_address1, String output_address2);     // Initialize native code, build pipeline, etc
    private native void nativeFinalize(); // Destroy pipeline and shutdown native code
    private static native boolean nativeClassInit(); // Initialize native class: cache Method IDs for callbackse
    private native void nativePlay();     // Set pipeline to PLAYING
    private native void nativePause();    // Set pipeline to PAUSED
    private native void nativeSurfaceInit(Object surface);
    private native void nativeSurfaceFinalize();
    private long native_custom_data;

    private boolean is_playing_desired;   // Whether the user asked to go to PLAYING

    static String output_address1;
    static String output_address2;

    static String video = "rtsp://192.168.1.168:5540/ch0";
    static String video1 = "rtsp://192.168.1.167:5540/ch0";
    static String video2 = "rtsp://192.168.1.165:5540/ch0";
    static String video3 = "rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mov";

    String serverip;
    ReceiveThread r1;
    DatagramSocket sock;
    MyAsyncTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // 隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //禁止屏保
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Initialize GStreamer and warn if it fails
        try {
            GStreamer.init(this);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        serverip = intent.getStringExtra("serverIp");

        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        String myip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

        //送信スレッド
        task = new MyAsyncTask(sock);

        task.setmsgstring(myip);
        task.setipstring(serverip);
        task.execute();

        while (task.bool==false)
        {

        }
        task.cancel(true);
        Log.v("MyLog","Thread stopped....");

        //受信スレッド
        r1=new ReceiveThread(handler);
        r1.start();

        Log.v("MyLog", "Creating Server socket....");
        try
        {
            sock = new DatagramSocket(7777);

            Log.v("MyLog", "Socket Created....");
        }
        catch(IOException e)
        {
            Log.v("MyLog",e.toString());
        }

        output_address1 = serverip + ":5000";
        output_address2 = serverip + ":5001";

        // ボタン押下イベントリスナーの登録
        Button button = (Button)findViewById(R.id.button1);
        button.setOnClickListener(mButton1Listener);

        button = (Button)findViewById(R.id.button2);
        button.setOnClickListener(mButton2Listener);

        button = (Button)findViewById(R.id.button3);
        button.setOnClickListener(mButton3Listener);

        if (savedInstanceState != null) {
            is_playing_desired = savedInstanceState.getBoolean("playing");
            Log.i ("GStreamer", "Activity created. Saved state is playing:" + is_playing_desired);
        } else {
            is_playing_desired = false;
            Log.i ("GStreamer", "Activity created. There is no saved state, playing: false");
        }

        // add feed fragment to view
        this.showFragment(VideoFragment.newInstance(video), R.id.video_container);
        this.showFragment(VideoFragment.newInstance(video1), R.id.video_container1);
        this.showFragment(VideoFragment.newInstance(video2), R.id.video_container2);
        this.showFragment(VideoFragment.newInstance(video3), R.id.video_container3);


    }

    final Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what==1){
                Toast.makeText(MainActivity.this,msg.obj.toString(), Toast.LENGTH_SHORT).show();
            }
            super.handleMessage(msg);
        }
    };

    //Clickイベント
    // Button 1
    private OnClickListener mButton1Listener = new OnClickListener() {
        public void onClick(View v) {
            //Toast.makeText(MainActivity.this, "Button 1が押されました", Toast.LENGTH_LONG).show();
            nativePause();
            String input_address = "192.168.1.168:5540/ch0";
            nativeInit(input_address, output_address1, output_address2);
        }
    };

    private OnClickListener mButton2Listener = new OnClickListener() {
        public void onClick(View v) {
            //Toast.makeText(MainActivity.this, "Button 2が押されました", Toast.LENGTH_LONG).show();
            nativePause();
            String input_address = "192.168.1.167:5540/ch0";
            nativeInit(input_address, output_address1, output_address2);
        }
    };

    private OnClickListener mButton3Listener = new OnClickListener() {
        public void onClick(View v) {
            //Toast.makeText(MainActivity.this, "Button 3が押されました", Toast.LENGTH_LONG).show();
            nativePause();
            String input_address = "192.168.1.165:5540/ch0";
            nativeInit(input_address, output_address1, output_address2);
        }
    };


    protected void onSaveInstanceState (Bundle outState) {
        Log.d ("GStreamer", "Saving state, playing:" + is_playing_desired);
        outState.putBoolean("playing", is_playing_desired);
    }

    private void setMessage(final String message){
        final TextView tv = (TextView) this.findViewById(R.id.textview_message);
        runOnUiThread(new Runnable() {
            public void run() {
                tv.setText(message);
            }
        });
    }

    private void onGStreamerInitialized () {
        Log.i("GStreamer", "Gst initialized. Restoring state, playing:" + is_playing_desired);
        // Restore previous playing state
        if (is_playing_desired) {
            nativePlay();
        } else {
            nativePause();
        }
    }

    static {
        System.loadLibrary("gstreamer_android");
        System.loadLibrary("tutorial-3");
        nativeClassInit();
    }

    protected void showFragment(Fragment newFragment, int container) {
        FragmentTransaction transaction = this.getFragmentManager().beginTransaction();
        transaction.add(container, newFragment);
        transaction.commit();
    }

}
