package com.example.zenbo_phonecontroller;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.* ;

public class MainActivity extends AppCompatActivity {

    private EditText editIp , editPort ;
    private Button readyToConnect ;
    public String zenboIP ;
    public int zenboPort ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editIp = findViewById(R.id.edit_zenboName) ;
        editPort = findViewById(R.id.edit_zenboPort) ;
        readyToConnect = findViewById(R.id.finishIpInput) ;
        readyToConnect.setOnClickListener(readyToConnectOnClick);

    }

    Button.OnClickListener readyToConnectOnClick = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            zenboIP = editIp.getText().toString() ;
            zenboPort = Integer.parseInt(editPort.getText().toString()) ;

            Bundle bundle = new Bundle() ;
            bundle.putString("IP",zenboIP) ;
            bundle.putInt("PORT",zenboPort) ;

            Intent it = new Intent() ;
            it.setClass(MainActivity.this , sendCommand.class) ;
            it.putExtras(bundle) ;
            startActivity(it) ;
        }
    } ;


}
