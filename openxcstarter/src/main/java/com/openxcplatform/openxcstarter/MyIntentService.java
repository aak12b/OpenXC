package com.openxcplatform.openxcstarter;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;

import com.openxc.VehicleManager;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.VehicleSpeed;
import com.openxc.units.KilometersPerHour;


public class MyIntentService extends IntentService {
    private static final String TAG = "MyIntentService";

    private boolean work = true;
    private VehicleManager mVehicleManager;
    private double max_kph;
    private double vspeed;
    private String message;
    private String phoneNo;
    private String busted = "The vehicle has surpassed the indicated maximum speed.";

    private int count = 0;

    public MyIntentService() {
        super("MyIntentService");
    }

    public void onCreate() {
        Intent i = new Intent(this, VehicleManager.class);
        bindService(i, mConnection, Context.BIND_AUTO_CREATE);
        super.onCreate();
    }



    private ServiceConnection mConnection = new ServiceConnection() {
        // Called when the connection with the service is established
        public void onServiceConnected(ComponentName className, IBinder service) {
            mVehicleManager = ((VehicleManager.VehicleBinder) service).getService();

            VehicleSpeed.Listener listener = new VehicleSpeed.Listener() {
                public void receive(Measurement measurement) {
                    final VehicleSpeed speed = (VehicleSpeed) measurement;
                    KilometersPerHour derp = speed.getValue();
                    vspeed = derp.doubleValue();
                    // do stuff with the measurement
                    if (vspeed > max_kph && count == 0)
                    {
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(phoneNo, null, busted, null, null);
                        work = false;
                        count++;
                    }
                }
            };

            mVehicleManager.addListener(VehicleSpeed.class, listener);
        }

        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className) {
            Log.i(TAG, "Disconnected");
            mVehicleManager = null;
        }
    };



    @Override
    protected void onHandleIntent(Intent intent) {

        Log.i(TAG, "service started");

        if(mVehicleManager == null){
            Log.i(TAG, "Null manager");
        } else {
            Log.i(TAG, "Not Null manager");
        }


        message = intent.getStringExtra("txt");
        String[] separated = message.split(":");
        max_kph = Double.parseDouble(separated[0]);
        phoneNo = separated[1];

        while(work) {
            //Dont do a damn thing.
        }


    }

    @Override
    public void onDestroy(){
        unbindService(mConnection);
        super.onDestroy();
    }

}