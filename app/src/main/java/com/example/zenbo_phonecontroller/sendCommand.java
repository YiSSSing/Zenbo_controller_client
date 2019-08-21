package com.example.zenbo_phonecontroller;

import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.* ;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class sendCommand extends AppCompatActivity {

    private Button endConnect , sendCommand ;
    private TextView dialogTV , test ;
    private EditText editMsg ;

    private Thread socketThread ;
    private Socket socketConnection ;
    private static String zenboIP ;
    private static int zenboPORT ;

    public static boolean socketConnected ;
    private String socketMsg = null , out_cmd = null ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_command);

        Bundle bundle = this.getIntent().getExtras() ;
        zenboIP = bundle.getString("IP") ;
        zenboPORT = bundle.getInt("PORT") ;

        socketThread = new Thread(connectToZenbo) ;
        socketThread.start() ;

        endConnect = findViewById(R.id.disconnect_server) ;
        sendCommand = findViewById(R.id.send_msg) ;
        dialogTV = findViewById(R.id.test) ;
        editMsg = findViewById(R.id.edit_msg) ;

        endConnect.setOnClickListener(endConnectOnClick);
        sendCommand.setOnClickListener(sendCommandOnClick);

    }

    private void scrollToBottom(TextView tv) {
        tv.scrollTo(0,tv.getLineCount()*tv.getLineHeight()-tv.getHeight()) ;
    }

    private Button.OnClickListener endConnectOnClick = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            socketThread.interrupt();
            socketConnected = false ;
            try {
                socketConnection.close() ;
                //socketConnection = null ;
                socketMsg = null ;
                out_cmd = null ;
            }catch ( Exception e ) {
                dialogTV.append("Connection Error : "+e.toString()+"\n") ;
            }
            Intent it = new Intent() ;
            it.setClass(sendCommand.this, MainActivity.class) ;
            startActivity(it);
        }
    } ;

    Button.OnClickListener sendCommandOnClick = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            out_cmd = editMsg.getText().toString()  ;
            if ( out_cmd == null || ! socketConnected ) return ;
            out_cmd += '\n' ;
            Thread t = new Thread(sendCmd) ;
            t.start() ;
            editMsg.setText("") ;
        }
    } ;

    private Runnable sendCmd = new Runnable() {
        @Override
        public void run() {
            try {
                out_cmd = out_cmd + '\n' ;
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socketConnection.getOutputStream())) ;
                bw.write(out_cmd);
                bw.flush();
            }catch ( Exception e ) {
                Log.e("Connection Error : ",e.toString()) ;
                socketThread.interrupt();
                socketThread = null;
                runOnUiThread(new Thread(new Runnable() {
                    @Override
                    public void run() {
                        dialogTV.append("Connection error \n");
                    }
                })) ;
            }
        }
    } ;


    private Runnable connectToZenbo = new Runnable() {
        @Override
        public void run() {
            BufferedReader br ;
            try {
                socketConnection = new Socket(InetAddress.getByName(zenboIP),zenboPORT) ;
                socketConnected = true ;
                br = new BufferedReader(new InputStreamReader(socketConnection.getInputStream())) ;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialogTV.append("Connect to Zenbo successfully.\n") ;
                    }
                });

                while ( socketConnection.isConnected() ) {
                    try {
                        socketMsg = br.readLine() ;
                        if ( socketMsg != null ) runOnUiThread(updateDialog);
                    }catch ( Exception e ) {
                        Log.e("Connection Error : ", e.toString()) ;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialogTV.append("Connection error, please reconnect. A") ;
                            }
                        });
                    }
                }
                socketConnected = false ;
            }catch ( Exception e ) {
                Log.e("Connection Error : ", e.toString() ) ;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialogTV.append("Connection error, please reconnect. B") ;
                    }
                });
            }
        }
    } ;

    private Runnable updateDialog = new Runnable() {
        @Override
        public void run() {
            if ( socketMsg != null ) {
                dialogTV.append(socketMsg+"\n");
                socketMsg = null ;
            }else if ( out_cmd != null ) {
                /*
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss") ;
                Date date = new Date(System.currentTimeMillis()) ;
                String sss = "[" + sdf.format(date) + "]" + " Command : " + out_cmd ;
                */
                editMsg.setText("") ;
            }
        }
    } ;

    protected void onDestory() {
        super.onDestroy();
        socketThread.interrupt();
        socketConnected = false ;
        try {
            socketConnection.close() ;
            socketConnection = null ;
            socketMsg = null ;
            out_cmd = null ;
        }catch ( Exception e ) {
            dialogTV.append("ERROR : "+e.toString());
        }
    }


}
