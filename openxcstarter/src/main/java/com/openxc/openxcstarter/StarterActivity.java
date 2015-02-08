package com.openxc.openxcstarter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.openxc.VehicleManager;
import com.openxcplatform.openxcstarter.MyIntentService;
import com.openxcplatform.openxcstarter.R;


public class StarterActivity extends Activity {




    public Button button;
    public String phoneNo;
    public String message;
    public int max_speed;
    public double max_kph;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starter);

        addListener();

    }

    public void addListener(){
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText phoneNum = (EditText) findViewById(R.id.editText);
                EditText speed = (EditText) findViewById(R.id.editText2);
                phoneNo = "" + phoneNum.getText();
                max_speed = Integer.parseInt(speed.getText().toString());
                max_kph = max_speed * 1.609;
                message = "Vehicle has gone faster than " + max_speed + " miles per hour.";
                String intro = "This number has been chosen as a VT Phone Home Guardian. The maximum MPH of the vehicle was entered as "
                        + max_speed + ".";
                Intent i = new Intent(StarterActivity.this, MyIntentService.class);
                String push = max_kph + ":" + phoneNo;
                i.putExtra("txt", push);
                startService(i);
                try {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(phoneNo, null, intro, null, null);

                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(),
                            "SMS failed, please try again.",
                            Toast.LENGTH_LONG).show();
                    e.printStackTrace();//
                }//
            }
        });
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.starter, menu);
        return true;
    }
}
