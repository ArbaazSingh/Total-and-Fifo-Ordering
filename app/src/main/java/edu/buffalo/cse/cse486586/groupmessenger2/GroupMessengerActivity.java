package edu.buffalo.cse.cse486586.groupmessenger2;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.PriorityQueue;


/**
 * GroupMessengerActivity is the main Activity for the assignment.
 *
 * @author stevko
 *
 */
public class GroupMessengerActivity extends Activity {
    private Uri mUri=Uri.parse("content://edu.buffalo.cse.cse486586.groupmessenger2.provider");
    static final String TAG = GroupMessengerActivity.class.getSimpleName();
    static final String REMOTE_PORT0 = "11108";
    static final String REMOTE_PORT1 = "11112";
    int incr=0;
    int counter=0;
    int counter2=0;
    static final String REMOTE_PORT2 = "11116";
    static final String REMOTE_PORT3 = "11120";
    String msgkeeper[] = new String[5];
    static final String REMOTE_PORT4 = "11124";
    static final int SERVER_PORT = 10000;
    HashMap<String, Integer> hashmap= new HashMap<String, Integer>();
    Socket[] portcheck= new Socket[5];
    static String tempport="";
    PriorityQueue<String> pq=new PriorityQueue<String>(10, new Comparator<String>() {
        @Override
        public int compare(String lhs, String rhs) {
            String m1[]=lhs.split("-");
            String m2[]=rhs.split("-");
            int p1=Integer.parseInt(m1[1] + m1[2]);
            int p2=Integer.parseInt(m2[1]+m2[2]);
            if (p1<p2)
                return -1;
            if (p1>p2)
                return 1;
            if (p1==p2)
                return 0;


            return 0;
        }
    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);

        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));
        tempport=myPort;
        try {

            ServerSocket serverSocket = new ServerSocket(SERVER_PORT, 25);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
            Log.e(TAG, "Can't create a ServerSocket");
            return;
        }



        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */
        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());
        final EditText editText = (EditText) findViewById(R.id.editText1);

        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));


        final Button rightButton = (Button) findViewById(R.id.button4);
        rightButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {

                EditText editText = (EditText) findViewById(R.id.editText1);
                String msg = editText.getText().toString() + "\n";
                editText.setText(""); // This is one way to reset the input box.
                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);

                //perform action
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }


    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];
            String port = "";
            Log.e(TAG, "SERVER TASK- Started");
            try{
                while(true) {


                    Socket socket = serverSocket.accept();
                    //socket.setSoTimeout(2000);
                    BufferedReader in =
                            new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String inputMsg = in.readLine();
                    Log.e(TAG, "SERVER TASK- Recieved initial message : " + inputMsg);
                    String parts[]= inputMsg.split("-");
                    port = parts[2];
                    int recieved_priority=Integer.parseInt(parts[1]);
                    if (recieved_priority>counter2){
                        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                        String msg = parts[0]+"-"+recieved_priority+"-"+parts[2]+"-"+parts[3]+"\n";
                        out.print(msg);
                        out.flush();
                        pq.add(msg);

                        counter2=recieved_priority;
                        counter2++;
                        Log.e(TAG, "SERVER TASK- Sending proposed priority : " + msg);
                    }
                    else{
                        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                        String msg = parts[0] + "-" + counter2 + "-" + tempport + "-" + parts[3]+"\n";
                        out.print(msg);
                        out.flush();
                        pq.add(msg);
                        counter2++;
                        Log.e(TAG, "SERVER TASK- Sending proposed priority : " +msg);
                    }

                    BufferedReader incoming =
                            new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String newinputMsg = incoming.readLine();
                    Log.e(TAG, "SERVER TASK- Recieved message with agreed priority : " + newinputMsg);
                    String newparts[]= inputMsg.split("-");
                    newparts[3]="d";
                    Iterator<String> iter = pq.iterator();
                    while (iter.hasNext()){
                        String curr=iter.next();
                        String current[]=curr.split("-");
                        Log.e(TAG,"current[0] : " + current[0]+ " newparts[0] : " + newparts[0] + " current[2] : " + current[2] + " newparts[2] : " + newparts[2]);
                        if (current[0].equals(newparts[0])&&(current[2].equals(newparts[2]))){
                            pq.remove(curr);
                            String msg = newparts[0]+"-"+newparts[1]+"-"+newparts[2]+"-"+newparts[3];
                            Log.e(TAG, "SERVER TASK- Adding message to priority queue : " + msg);
                            pq.add(msg);
                        }


                    }
                    String finalmsg=pq.peek();
                    if (finalmsg.endsWith("d")){
                        String q= pq.poll();
                        Log.e(TAG, "SERVER TASK- Storing message : " + q);
                        publishProgress(q);

                    }
                    socket.close();
                }
                /*
             * TODO: Fill in your server code that receives messages and passes them
             * to onProgressUpdate().
             */}
            catch(Exception e){
                cleanup(port);
                e.printStackTrace();
            }


            return null;

        }
        protected void cleanup(String port){
            Iterator<String> iter = pq.iterator();
            while (iter.hasNext()){
                String curr=iter.next();
                String current[]=curr.split("-");
                if (current[2].equals(port)){
                    pq.remove(curr);
                }

            }

        }
        protected void onProgressUpdate(String...strings) {
            /*
             * The following code displays what is received in doInBackground().
             */

            String strReceived = strings[0].trim();
            String StrRequired[]=strReceived.split("-");
            TextView localTextView = (TextView) findViewById(R.id.textView1);
            localTextView.append(StrRequired[0]+"\n");
            Log.e(TAG, "Message storing... Key : "+ incr + " val : " +StrRequired[0]);

            ContentValues keyValueToInsert = new ContentValues();

            keyValueToInsert.put("key", String.valueOf(incr));
            keyValueToInsert.put("value", StrRequired[0]);

            incr=incr+1;

            Uri newUri = getContentResolver().insert(
                    mUri,    // assume we already created a Uri object with our provider URI
                    keyValueToInsert
            );


            /*
             * The following code creates a file in the AVD's internal storage and stores a file.
             ******************************************************************************
             * For more information on file I/O on Android, please take a look at
             * http://developer.android.com/training/basics/data-storage/files.html
             */



            return;
        }
    }

    /***
     * ClientTask is an AsyncTask that should send a string over the network.
     * It is created by ClientTask.executeOnExecutor() call whenever OnKeyListener.onKey() detects
     * an enter key press event.
     *
     * @author stevko
     *
     */
    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {

            Log.e(TAG, "CLIENT TASK- Started");
            String remotePort[] = {REMOTE_PORT0,REMOTE_PORT1,REMOTE_PORT2,REMOTE_PORT3,REMOTE_PORT4};

            String msgToSend = msgs[0];
            msgToSend = msgToSend.replaceAll("\n","");
            int tempcounter=0;
            counter++;
            for (int i=0;i<5;i++) {
                try {
                    Log.e(TAG, "CLIENT TASK- Multicast Started sending to remote port : " + remotePort[i]);

                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(remotePort[i]));
                    //socket.setSoTimeout(2000);
                    portcheck[i]=socket;



                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    String msg = msgToSend+"-"+counter+"-"+remotePort[i]+"-"+"n\n";
                    out.print(msg);
                    Log.e(TAG, "CLIENT TASK- sending message : " + msg);
                    out.flush();

                    BufferedReader in =
                            new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String inputMsg = in.readLine();
                    Log.e(TAG, "CLIENT TASK- Recieved Message : "  + inputMsg);
                    msgkeeper[tempcounter]=inputMsg;

                    tempcounter++;
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                    Log.e(TAG, "ClientTask UnknownHostException");
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "ClientTask socket IOException");
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                /*

                 * TODO: Fill in your client code that sends out a message.
                 */

            }
            String maxprioritiwali="";
            int sabsebadaa=0;
            for (int i=0;i<5;i++){
                String t[];
                if(msgkeeper[i]!=null) {
                    t = msgkeeper[i].split("-");

                    int number = Integer.parseInt(t[1] + t[2]);
                    if (number > sabsebadaa) {
                        sabsebadaa = number;
                        maxprioritiwali = msgkeeper[i];
                    }
                }
            }
            Log.e(TAG, "CLIENT TASK- Message with Agreed priority : "  + maxprioritiwali);
            for(int i=0;i<5;i++){
                try {
                    Log.e(TAG, "CLIENT TASK- Sending Agreed to : " + portcheck[i].getPort());
                    PrintWriter out = new PrintWriter(portcheck[i].getOutputStream(), true);
                    out.print(maxprioritiwali);
                    out.flush();
                    portcheck[i].close();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                    Log.e(TAG, "ClientTask UnknownHostException");
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "ClientTask socket IOException");
                }
                catch (Exception e){
                    e.printStackTrace();
                }

            }
            return null;
        }
    }
}
