package com.imayuki.davis_r;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

class MyAsyncTask extends AsyncTask {
    DatagramSocket sock;
    DatagramPacket sp;
    public InetAddress ip;
    public String s1,s2;
    public boolean bool;

    MyAsyncTask(DatagramSocket s)
    {
        sock=s;
    }

    public void setipstring(String str)
    {
        s2=str;
    }

    public void setmsgstring(String str)
    {
        s1=str;
    }


    @Override
    protected Object doInBackground(Object... params) {
        // Do some background work
        Log.d("MyLog", "MyAsyncTask@doInBackground from another thread");
        bool=false;

        try
        {
            ip = InetAddress.getByName(s2);
            //ip = InetAddress.getByName("192.168.43.60");//Receivers ip.... Hotspot creater=43.1
            Log.v("MyLog","ip created!!!!");
        }
        catch(IOException e)
        {
            Log.v("MyLog","ip creation failed!!!!");
        }

        try {
            sp = new DatagramPacket(s1.getBytes(), s1.getBytes().length, ip, 7776);
            Log.v("MyLog", "Send Packet created!!!!");

            sock.send(sp);
            //Toast.makeText(getBaseContext(), "Msg Sent....", Toast.LENGTH_SHORT).show();
            Log.v("MyLog", "Sent ...|"+ip.toString()+"|"+s2+"|"+s1);
        } catch (IOException e) {
            Log.v("MyLog", "Sending failed....");
        }

        bool=true;
        return new Object();

    }
}
