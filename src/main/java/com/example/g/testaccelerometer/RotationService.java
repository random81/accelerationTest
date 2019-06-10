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

//public class AccelerMeterService extends Service implements SensorEventListener {
public class RotationService extends Service implements SensorEventListener {

    private static SensorManager managerRotation;
    private static SensorManager managerFindAllSensors;
    private static Sensor mRotation;
    private final IBinder binder= new RotationServiceBinder();

    public void onCreate() {
        //TriggerEventListener mListener = new TriggerEventListener();

        managerFindAllSensors = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        //List<Sensor> deviceSensors = managerFindAllSensors.getSensorList(Sensor.TYPE_ALL);

        managerRotation = (SensorManager)getSystemService(SENSOR_SERVICE);
        mRotation = managerRotation.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        managerRotation.registerListener(this, mRotation, SensorManager.SENSOR_DELAY_NORMAL);

        //SensorEvent sensorEvent= new SensorEvent();
        //  SensorManager sensorEvent= new SensorManager();
    }
    public RotationService() {
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (mRotation!=null){
            System.out.println(mRotation.toString());
            System.out.println(sensorEvent.values[0]);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    //this defines a function that the activity can call because activity uses onBind connection method. returning AccelermeterServiceBInder obejct, then use getAccel method
    //this is an inner class. rotation testing will communicate with this service and communitcate with it using ServiceConnection object
    public class RotationServiceBinder extends Binder {
        RotationService getRotateData(){
            return RotationService.this;
        }
    }
    //this binder object will deliver through the servicecONNECTION  object to roataion testing this service object.
    //this defines a function that the activity can call because activity uses onBind connection method. returning AccelermeterServiceBInder obejct, then use getAccel method
    //this is an inner class. rotation testing will communicate with this service and communitcate with it using ServiceConnection object
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
