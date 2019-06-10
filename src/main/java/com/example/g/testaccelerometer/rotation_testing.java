package com.example.g.testaccelerometer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class rotation_testing extends Activity {

    public static final String EXTRA_RANGE_INFO="";
    public static String EXTRA_LOWER_INFO="";
    public static String EXTRA_UPPER_INFO="";
    //creating accelerrmotere service binding variables
    private AccelerMeterService accelerMeter;
    private RotationService rotationMeter;
    private GyroscopeService gyroMeter;
    private boolean bound=false;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            AccelerMeterService.AccelerMeterServiceBinder  AccelerMeterServiceBinder = (AccelerMeterService.AccelerMeterServiceBinder) iBinder;
            accelerMeter = AccelerMeterServiceBinder.getAccelData();
            //create also a rotation binder
            //THE BINDER OBJECT IS NOW ACCELERMETERBINDER OBJECT, YOU CANNOT CAST AGAIN HERE TO OTHER SERVICES, MAY NEED ANOTHER ACITVITY?
            //BINDER IS ABSTRACT,

            //IBinder iBinderUncast = (IBinder) AccelerMeterServiceBinder;
            //RotationService.RotationServiceBinder  RotationServiceBinder = (RotationService.RotationServiceBinder) iBinderUncast;

            //GyroscopeService.GyroscopeServiceBinder  GyroscopeServiceBinder = (GyroscopeService.GyroscopeServiceBinder) iBinder;

            //rotationMeter = RotationServiceBinder.getRotateData();
            //gyroMeter = GyroscopeServiceBinder.getRGyroData();
            bound=true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bound=false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent= new Intent(this, AccelerMeterService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (bound){
            unbindService(connection);
            bound=false;
        }
    }
    //AT THIS POINT we need to ask the service for the data!
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rotation_testing);
    }

    /*
    what we need to do here is to onCreate begin documenting the accelrometer data.
    that data will be sent back to the home screen using intent.putExra("message",value)
    in the home screen use getIntent method to retrieve the data


    looks like we might be use implicit intent action, new intent(action_blabla) format to get acclerator to work?  this is usually used for activating other apps. not API.

    for data collection:
    possibly use handler.postDelayed to control the time increments of data collection from the accelerometer.
     */

    String rotateData="rotate data string placeholder";

    //save rotation data incase of activity destroy, BUT WE WILL ASK USER TO LOCK ORIENTATION PROBABLY!
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(MainActivity.EXTRA_ROTATE_INFO,rotateData);
    }

    public void onClickEndTest(View view) {
        System.out.println("testing 123... you have clicked the end button!");
        //go to android monitor alt-6 to see this message when clicking test button
        //I think it makes sense to have a count down here.
        Intent intent= new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_ROTATE_INFO,rotateData);
        startActivity(intent);
        //setContentView(R.layout.activity_rotation_testing);
        //in addition to the new layout being loaded, we should launch the accelormeter testing class now. should we just call a new activity?
    }
}
