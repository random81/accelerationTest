package com.example.g.testaccelerometer;

import android.annotation.TargetApi;
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

public class GyroscopeService extends Service implements SensorEventListener {

    private static SensorManager ManagerGyroscope;
    private static SensorManager managerFindAllSensors;
    private static Sensor mGyroscope;
    private final IBinder binder= new GyroscopeServiceBinder();

    @TargetApi(18)
    @Override
    public void onCreate() {
        //TriggerEventListener mListener = new TriggerEventListener();

        managerFindAllSensors = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        List<Sensor> deviceSensors = managerFindAllSensors.getSensorList(Sensor.TYPE_ALL);

        ManagerGyroscope = (SensorManager)getSystemService(SENSOR_SERVICE);
        mGyroscope = ManagerGyroscope.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        ManagerGyroscope.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        //SensorEvent sensorEvent= new SensorEvent();
        //  SensorManager sensorEvent= new SensorManager();
    }
    public GyroscopeService() {
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (mGyroscope!=null){
            System.out.println(mGyroscope.toString());
            System.out.println(sensorEvent.values[0]);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public class GyroscopeServiceBinder extends Binder {
        GyroscopeService getRGyroData(){
            return GyroscopeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
