package com.openxc.openxcstarter;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.telephony.SmsManager;
import android.widget.Toast;

import com.openxc.measurements.VehicleSpeed;
import com.openxc.units.KilometersPerHour;
import com.openxcplatform.openxcstarter.R;
import com.openxc.VehicleManager;
import com.openxc.measurements.Measurement;


public class StarterActivity extends Activity {
    private static final String TAG = "StarterActivity";

    private VehicleManager mVehicleManager;

    public Button button;
    public String phoneNo;
    public String message;
    public int max_speed;
    public double max_kph;
    public boolean derp = true;

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
                String intro = "This number has been chosen as a VTHax Guardian. The maximum MPH of the vehicle was entered as "
                        + max_speed + ".";
                derp = false;

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
    public void onPause() {
        super.onPause();
        // When the activity goes into the background or exits, we want to make
        // sure to unbind from the service to avoid leaking memory
        if(mVehicleManager != null) {
            Log.i(TAG, "Unbinding from Vehicle Manager");
            // Remember to remove your listeners, in typical Android
            // fashion.
            mVehicleManager.removeListener(VehicleSpeed.class,
                    mSpeedListener);
            unbindService(mConnection);
            mVehicleManager = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // When the activity starts up or returns from the background,
        // re-connect to the VehicleManager so we can receive updates.
        if(mVehicleManager == null) {
            Intent intent = new Intent(this, VehicleManager.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    /* This is an OpenXC measurement listener object - the type is recognized
     * by the VehicleManager as something that can receive measurement updates.
     * Later in the file, we'll ask the VehicleManager to call the receive()
     * function here whenever a new VehicleSpeed value arrives.
     */

    int counter = 0;
    VehicleSpeed.Listener mSpeedListener = new VehicleSpeed.Listener() {
        @Override
        public void receive(Measurement measurement) {
            if(!derp){
            final VehicleSpeed speed = (VehicleSpeed) measurement;

            KilometersPerHour kilometersPerHour = speed.getValue();
            if(kilometersPerHour.doubleValue() > max_kph &&  counter == 0){
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNo, null, message, null, null);

                counter++;
            }

        }}

    };



    private ServiceConnection mConnection = new ServiceConnection() {
        // Called when the connection with the VehicleManager service is
        // established, i.e. bound.
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            Log.i(TAG, "Bound to VehicleManager");
            // When the VehicleManager starts up, we store a reference to it
            // here in "mVehicleManager" so we can call functions on it
            // elsewhere in our code.
            mVehicleManager = ((VehicleManager.VehicleBinder) service)
                    .getService();

            // We want to receive updates whenever the VehicleSpeed changes. We
            // have an VehicleSpeed.Listener (see above, mSpeedListener) and here
            // we request that the VehicleManager call its receive() method
            // whenever the VehicleSpeed changes
            mVehicleManager.addListener(VehicleSpeed.class, mSpeedListener);
        }

        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className) {
            Log.w(TAG, "VehicleManager Service  disconnected unexpectedly");
            mVehicleManager = null;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.starter, menu);
        return true;
    }
}
