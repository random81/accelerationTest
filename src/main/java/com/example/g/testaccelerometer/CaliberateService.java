package com.example.g.testaccelerometer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;

import java.util.List;

import static android.os.SystemClock.uptimeMillis;

public class CaliberateService extends Service implements SensorEventListener {
    private static SensorManager cSensorManager;
    private static SensorManager managerFindAllSensors;
    private static Sensor mRotation;
    boolean firstRun=true;
    boolean caliBend=false;
    //distance
    static float lowerRange=0;
    static float upperRange=0;
    static float zAxisCalib=0;
    static float yAxisCalib=0;
    static float xAxisCalib=0;
    private static Sensor cAccelerometer;
    private final IBinder binder= new CaliberationServiceBinder();
    static long timerCaliberate=uptimeMillis();//make it zero at first. timer starts as soon as we start moving, at first movement, and ends at last movement
    String rangeData="rangeData data string placeholder";

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        //TriggerEventListener mListener = new TriggerEventListener();
        managerFindAllSensors = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> deviceSensors = managerFindAllSensors.getSensorList(Sensor.TYPE_ALL);
        //thinking about using the array to initilze and register devices. need sensor.getType method to do that in a for loop.

        for (int i = 0 ; i<deviceSensors.size();i++){
            System.out.println(deviceSensors.get(i));
            System.out.println("this is a get of sensor from List");

        }


        cSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        cAccelerometer = cSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        cSensorManager.registerListener(this, cAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        //SensorEvent sensorEvent= new SensorEvent();
        //  SensorManager sensorEvent= new SensorManager();
/*
        managerRotation = (SensorManager)getSystemService(SENSOR_SERVICE);
        mRotation = managerRotation.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        managerRotation.registerListener(this, mRotation, SensorManager.SENSOR_DELAY_NORMAL);
        */
    }
    public CaliberateService() {
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        /*
        what we want is a range, so we can have two variables, and update data initiallize to sensor value for first test, and from then on if item bigger or smaller than variable update
        //upper and lower range each time
         */

            if (firstRun) {

                lowerRange=sensorEvent.values[1];
                upperRange=sensorEvent.values[1];

                System.out.println(cAccelerometer.toString());
                System.out.println("caliberation values on x y z: ");
                System.out.println(sensorEvent.values[0]);
                System.out.println(sensorEvent.values[1]);
                System.out.println(sensorEvent.values[2]);
                System.out.println("startTime "+timerCaliberate);
                System.out.println("calibTime "+uptimeMillis());
                System.out.println("startTime - calibTime "+(uptimeMillis()-timerCaliberate));
                firstRun=false;
                //if ((sensorEvent.values[1]>0.50)||(sensorEvent.values[1]<0.16)){
            }else {
                if ((uptimeMillis()-timerCaliberate)/1000>=5){
                    //end the caliberation, 5 sec are up
                    caliBend=true;
                    System.out.println("trggered EndCaliberationService!!!");
                    System.out.println("startTime - calibTime "+(uptimeMillis()-timerCaliberate));
                    onDestroy();
                    //onEndCaliberationService();
                    //this doesnt really work from a service.



                }
                    //examine current values to update ranges
                    if (lowerRange>sensorEvent.values[1])
                        lowerRange=sensorEvent.values[1];
                    else if  (upperRange<sensorEvent.values[1])
                        upperRange=sensorEvent.values[1];
                    else
                        //do nothing
                System.out.println(cAccelerometer.toString());
                System.out.println("caliberation values on x y z: ");
                System.out.println("caliberation lower range on  y z: "+ lowerRange);
                System.out.println("caliberation upperRange on  y z: "+ upperRange);
                System.out.println(sensorEvent.values[0]);
                System.out.println(sensorEvent.values[1]);
                System.out.println(sensorEvent.values[2]);
            }
                    //this is a pretty rough estimate of when the movement has stopped. prob need to compare with previous run as well to 2.7
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public boolean getCaliberateEnd(){
        //true means end in caliberteActivity
        return caliBend;
    }

    //this defines a function that the activity can call because activity uses onBind connection method. returning AccelermeterServiceBInder obejct, then use getAccel method
    //this is an inner class. rotation testing will communicate with this service and communitcate with it using ServiceConnection object
    public class CaliberationServiceBinder extends Binder {
        CaliberateService getCaliberateData(){
            return CaliberateService.this;
        }
    }
    //this binder object will deliver through the servicecONNECTION  object to roataion testing this service object.
    //this defines a function that the activity can call because activity uses onBind connection method. returning AccelermeterServiceBInder obejct, then use getAccel method
    //this is an inner class. rotation testing will communicate with this service and communitcate with it using ServiceConnection object
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.

        return binder;
        //throw new UnsupportedOperationException("Not yet implemented");
    }

    public void onEndCaliberationService() {
        System.out.println("caliberation ender function activated !");
        //go to android monitor alt-6 to see this message when clicking test button
        //I think it makes sense to have a count down here.
        Intent intent= new Intent(this, rotation_testing.class);
        intent.putExtra(rotation_testing.EXTRA_LOWER_INFO,lowerRange);
        //intent.putExtra(rotation_testing.EXTRA_RANGE_INFO,rangeData);
        intent.putExtra(rotation_testing.EXTRA_UPPER_INFO,upperRange);
 //       intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        //setContentView(R.layout.activity_rotation_testing);
        //in addition to the new layout being loaded, we should launch the accelormeter testing class now. should we just call a new activity?
    }
}


