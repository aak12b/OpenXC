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
import com.openxc.measurements.EngineSpeed;
import com.openxc.measurements.FuelLevel;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.VehicleSpeed;
import com.openxc.units.KilometersPerHour;
import com.openxc.units.Percentage;
import com.openxc.units.RotationsPerMinute;

import java.io.File;
import java.io.FileOutputStream;


public class MyIntentService extends IntentService {
    private static final String TAG = "MyIntentService";

    private boolean work = true;
    private VehicleManager mVehicleManager;
    private double max_kph;
    private double max_revs;
    private double redline;
    private double vspeed;
    private double rpms;
    private double pfuel;
    private double revThreshold = 0.75; //75% threshold from redline
    private double highSpeed = 0;
    private double highRevs = 0;
    private String message;
    private String phoneNo;
    private String busted = "The vehicle has exceeded the indicated maximum speed.";
    private String bustedHR = "The vehicle has exceeded 75% redline.";
    private String halfFuel = "The vehicle has less than 1/2 tank of fuel remaining.";
    private String quarterFuel = "The vehicle has less than 1/4 tank of fuel remaining.";
    private String gasmessage;
    private boolean caughtRevving = false;
    private boolean caughtSpeeding = false;
    private boolean HFnoted = false;
    private boolean QFnoted = false;
    String filename = "reportFile";
    String filetest = "Testing the file";
    String finalOut = "";

    //private int VScount = 0;
    //private int RevCount = 0;

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
                    if (vspeed > highSpeed)
                        highSpeed = vspeed;
                    if (vspeed > max_kph && !caughtSpeeding)
                    {
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(phoneNo, null, busted, null, null);
                        if (caughtRevving && (HFnoted || QFnoted))
                            work = false;

                        caughtSpeeding = true;
                    }
                }
            };

            EngineSpeed.Listener esListener = new EngineSpeed.Listener() {
                public void receive(Measurement measurement) {
                    final EngineSpeed revs = (EngineSpeed) measurement;
                    RotationsPerMinute rotes = revs.getValue();
                    rpms = rotes.doubleValue();
                    if (rpms > highRevs)
                        highRevs = rpms;
                    if (rpms > max_revs && !caughtRevving){
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(phoneNo, null, bustedHR, null, null);
                        if (caughtSpeeding && (HFnoted || QFnoted))
                            work = false;
                        caughtRevving = true;
                    }
                }
            };

            FuelLevel.Listener flListener = new FuelLevel.Listener() {
                public void receive(Measurement measurement) {
                    final FuelLevel fuelremn = (FuelLevel) measurement;
                    Percentage perFuel = fuelremn.getValue();
                    pfuel = perFuel.doubleValue();

                    if (pfuel < 25 && !QFnoted) {
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(phoneNo, null, quarterFuel, null, null);
                        QFnoted = true;
                        if (caughtRevving && caughtSpeeding)
                            work = false;
                    }
                    else if (pfuel < 50 && !HFnoted){
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(phoneNo, null, halfFuel, null, null);
                        HFnoted = true;
                        if (caughtRevving && caughtSpeeding)
                            work = false;
                    }

                }
            };

            mVehicleManager.addListener(VehicleSpeed.class, listener);
            mVehicleManager.addListener(EngineSpeed.class, esListener);
            mVehicleManager.addListener(FuelLevel.class, flListener);
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
        redline = Double.parseDouble(separated[2]);
        max_revs = revThreshold * redline;


        while(work) {
            //Dont do a damn thing.
        }
        highRevs = Math.ceil(highRevs);
        highSpeed = Math.ceil(highSpeed / 1.609);

        finalOut = "Latest Report \nHighest Speed: ";
        finalOut += String.valueOf(highSpeed) + " mph";
        finalOut += "\nFuel Level: ";
        finalOut += String.valueOf(pfuel) + "%";
        finalOut += "\nHighest RPMs: ";
        finalOut += String.valueOf(highRevs);
        finalOut += "\n";

        Log.i(TAG, "File write code below");
        File filex = new File(getFilesDir(), filename);
        if (filex.exists())
            filex.delete();

        FileOutputStream os;
        try {
            os = new FileOutputStream(filex, true);
            //os.write(string.getBytes());
            os.write(finalOut.getBytes());
            os.close();
            Log.i(TAG, "File write attempt");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy(){
        //Testing file write for generate report.
/*        Log.i(TAG, "File write code below");
        File filex = new File(getFilesDir(), filename);
        FileOutputStream os;
        try {
            os = new FileOutputStream(filex, true);
            //os.write(string.getBytes());
            os.write(filetest.getBytes());
            os.close();
            Log.i(TAG, "File write attempt");
        } catch (Exception e) {
            e.printStackTrace();
        }
        //if file does not exist, create file
        //else add to file
                //time of data added (trip?)


        //end file write
*/      Log.i(TAG, "OnDestroy accessed");
        unbindService(mConnection);
        super.onDestroy();
    }

}