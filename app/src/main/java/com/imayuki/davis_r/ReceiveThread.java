package com.imayuki.davis_r;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

public class ReceiveThread extends Thread {
    public Handler h;
    public DatagramSocket sock;
    public DatagramPacket rp;
    public InetSocketAddress Cip;

    ReceiveThread(Handler h2)
    {
        h = h2;
        try
        {
            sock=new DatagramSocket(null);
            Log.v("MyLog", "Client Socket step1");
            Cip = new InetSocketAddress("0.0.0.0", 7776);//Hotspot device....
            Log.v("MyLog", "Client Socket step2");
            sock.bind(Cip);
            Log.v("MyLog", "Client Socket Created....");

        }
        catch(Exception e)
        {
            Log.v("MyLog", e.toString());
        }
    }

    public  void  run()
    {
        Log.v("MyLog","Client Run...");


        //Toast==
        Message msg = h.obtainMessage();
        /*msg.what = 1;
        String handlerMsg1 = "Inside Run....";
        msg.obj = handlerMsg1;
        msg.arg1 = 1;
        h.sendMessage(msg);
        //End Toast ==
        */

        byte[] receiveData= new byte[1024];;
        rp=new DatagramPacket(receiveData, receiveData.length);
        Log.v("MyLog","Receivepacket created...");
        String s="abcd";
        while(s!="exit")
        {

            try
            {
                sock.receive(rp);
            }
            catch (IOException e)
            {
                Log.v("MyLog", "Receive failed....");
            }

            s = new String(rp.getData(), 0, rp.getLength());
            Log.v("MyLog", "Msg :" + s);

            //Toast==
            msg = h.obtainMessage();
            msg.what = 1;
            String handlerMsg2 = "Received Msg : " + s;
            msg.obj = handlerMsg2;
            msg.arg1 = 1;
            h.sendMessage(msg);
            //End Toast ==

        }
    }

}
