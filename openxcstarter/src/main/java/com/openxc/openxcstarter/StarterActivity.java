package com.openxc.openxcstarter;

import android.app.Activity;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.openxc.VehicleManager;
import com.openxcplatform.openxcstarter.MyIntentService;
import com.openxcplatform.openxcstarter.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;



public class StarterActivity extends Activity {




    public Button button;
    public Button grbutton;
    public Button pButton;
    public String phoneNo;
    public String message;
    public int max_speed;
    public int max_rpms;
    public double max_kph;
    public String file = "reportFile";
    public String profile = "profile";
    public String profiledata = "";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starter);

        addListener();
        reportListener();
        profListener();

    }

    public void addListener(){
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText phoneNum = (EditText) findViewById(R.id.editText);
                EditText speed = (EditText) findViewById(R.id.editText2);
                EditText rpms = (EditText) findViewById(R.id.editText3);
                phoneNo = "" + phoneNum.getText();
                max_speed = Integer.parseInt(speed.getText().toString());
                max_kph = max_speed * 1.609;
                max_rpms = Integer.parseInt(rpms.getText().toString());

                //Begin profile creation
                profiledata = max_kph + ":" + phoneNo + ":" + max_rpms;
                File makeprofile = new File(getFilesDir(), profile);
                FileOutputStream os;
                try {
                    os = new FileOutputStream(makeprofile);
                    //os.write(string.getBytes());
                    os.write(profiledata.getBytes());
                    os.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //End Profile Creation

                //message = "Vehicle has gone faster than " + max_speed + " miles per hour.";
                String intro = "This number has been chosen as a VT Phone Home Guardian.";
               // Intent i = new Intent(StarterActivity.this, MyIntentService.class);
                //String push = max_kph + ":" + phoneNo + ":" + max_rpms;
                //String push = profiledata;
                //i.putExtra("txt", push);
                //startService(i);
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

    public void profListener() {
        pButton = (Button) findViewById(R.id.useProfile);
        pButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FileInputStream fis;
                profiledata = "";
                try {
                    fis = openFileInput(profile);
                    byte[] input = new byte[fis.available()];
                    while (fis.read(input) != -1) {}
                    profiledata += new String(input);
                    Toast.makeText(getApplicationContext(),
                            "Using Stored Profile",
                            Toast.LENGTH_LONG).show();
                    Intent i = new Intent(StarterActivity.this, MyIntentService.class);
                    String push = profiledata;
                    i.putExtra("txt", push);
                    startService(i);
                } catch (FileNotFoundException e) {
                    Toast.makeText(getApplicationContext(),
                            "No Profile Stored",
                            Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public void reportListener(){
        grbutton = (Button) findViewById(R.id.generateForm);
        grbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FileInputStream pGrabber;
                profiledata = "";
                try {
                    pGrabber = openFileInput(profile);
                    byte[] input = new byte[pGrabber.available()];
                    while (pGrabber.read(input) != -1) {}
                    profiledata += new String(input);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }


                FileInputStream fis;
                String content = "";
                try {
                    fis = openFileInput(file);
                    byte[] input = new byte[fis.available()];
                    while (fis.read(input) != -1) {}
                    content += new String(input);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                content += "End of Report\n";
                String[] parsed = profiledata.split(":");
                phoneNo = parsed[1];

                Toast.makeText(getApplicationContext(),
                        "Report Sent",
                        Toast.LENGTH_LONG).show();

                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNo, null, content, null, null);

                //File filex = new File(getFilesDir(), file);
                //filex.delete();
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
