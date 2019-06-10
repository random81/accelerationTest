package com.example.g.testaccelerometer;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.location.Location;
import android.location.LocationListener;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;

//import java.time.Duration;
//import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.List;

import static android.os.SystemClock.uptimeMillis;

public class AccelerMeterService extends Service implements SensorEventListener {

    /*
         //create another activity, prob fragment, and let user know: "put phone down and please wait while caliberating.."
         //gather sensor data into 3 arrays, x,y,z.  time 5 seconds.
         //timer test for 5 seconds continue for loop.
         //create statistical range distriubtion. if you exceed that range, you have real movement.
         //so we create an activity  once the five seconds pass you will have a median and range variable, that will be passed to the rotation activity and then to this service.
     */
    private static SensorManager managerFindAllSensors;
    // private static Sensor mRotation;
    static float zAxisMonitor=0;
    static List<Float> readingsService=new ArrayList<Float>();
    static List<Float> priorGravity=new ArrayList<Float>();
    static float yAxisMonitor=0;
    //timer
    static long timer=0;//make it zero at first. timer starts as soon as we start moving, at first movement, and ends at last movement
    static long timerTotal=0;//make it zero at first. timer starts as soon as we start moving, at first movement, and ends at last movement
    //velocity initial
    static double velocity=0;
    //distance
    static double distance;
    private final IBinder binder= new AccelerMeterServiceBinder();
    private static SensorManager mSensorManager;
    private static Sensor mAccelerometer;

    @TargetApi(18)
    class TriggerListener extends TriggerEventListener {
        public void onTrigger(TriggerEvent event) {
            // Do Work.
            // As it is a one shot sensor, it will be canceled automatically.
            // SensorManager.requestTriggerSensor(this, mSigMotion); needs to
            // be called again, if needed.
        }
    }

    //Declare hardware that application requires using the <uses-feature> manifest element.
    @TargetApi(18)
    @Override
    public void onCreate() {
        managerFindAllSensors = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> deviceSensors = managerFindAllSensors.getSensorList(Sensor.TYPE_ALL);
        //thinking about using the array to initilze and register devices. need sensor.getType method to do that in a for loop.

        for (int i = 0 ; i<deviceSensors.size();i++){
            System.out.println(deviceSensors.get(i));
            System.out.println("this is a get of sensor from List");

        }


        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    //this seems to contradict our knowledge, a constructor would not be the way to do this, although android google docs show this done.
    public AccelerMeterService() {
//        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        //      mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (mAccelerometer!=null){
            //ANdroid documentation
            //here t is the constant? what does it mean>? t is rate of sampling?
            // alpha is calculated as t / (t + dT)
            // with t, the low-pass filter's time-constant // RC in  circuit, time constant related to frequency allowed to pass. bigger RC smaller frequency allowed
            // and dT, the event delivery rate= resolution??? time passed since last measurement??
            /*
           A small α implies that the output will decay quickly and will require large changes in the input (i.e., (x[i] - x[i-1]) is large)
            to cause the output to change much. By the relationship between parameter α and time constant R C {\displaystyle RC} RC above,
             a small α corresponds to a small R C {\displaystyle RC} RC and therefore a high corner frequency of the filter.
             Hence, this case corresponds to a high-pass filter with a very wide stop band. Because it requires large (i.e., fast) changes and tends to
              quickly forget its prior output values,
           it can only pass relatively high frequencies, as would be expected with a high-pass filter with a small R C {\displaystyle RC} RC.
             */
            float gravity[]={0,0,0};
            float linear_acceleration[]={0,0,0};
            //t is analoges to RC in a circuit. it is the latency the filter adds to the sensor event.. the time constant
            //alpha here is just a place holder! replace it with t/t+dt     dt=sensor's event delivery rate. =time between this and last event? looks like 1event/delay
            /*
            t=RC=1/(2*pi*delay)         //delay is the frequency of sampling by sensor
            alpha=t/t+dt
             */
             //not sure about dt=1/delay //
            //dt=1/delay
            //high pass filter α := RC / (RC + dt)  vs.  Low pass filter α := dt / (RC + dt)
            float dt=1f/ mAccelerometer.getMinDelay();

            //high pass filer implementation
            final float RC=(float)(1f/(2f*Math.PI*mAccelerometer.getMinDelay()));
            final float alpha=RC/(RC+dt);//not sure about time-uptimeMillies=dt ???

            gravity[0] =  sensorEvent.values[0];
            gravity[1] =  sensorEvent.values[1];
            gravity[2] =  sensorEvent.values[2];

            //linear_acceleration[0] = gravity[0];
           //linear_acceleration[1] = gravity[1];
            //linear_acceleration[2] = 9 - gravity[2];



            // Isolate the force of gravity with the low-pass filter.
           // gravity[0] = alpha * gravity[0] + (1 - alpha) * sensorEvent.values[0];
           // gravity[1] = alpha * gravity[1] + (1 - alpha) * sensorEvent.values[1];
           // gravity[2] = alpha * gravity[2] + (1 - alpha) * sensorEvent.values[2];
           final float dTgyroEx=(sensorEvent.timestamp-timer)*(1f/mAccelerometer.getMinDelay());
            //this might be a better way to get the dt value,
            // Remove the gravity contribution with the high-pass filter.
            //linear_acceleration[0] = sensorEvent.values[0] - gravity[0];
           // linear_acceleration[1] = sensorEvent.values[1] - gravity[1];
           // linear_acceleration[2] = sensorEvent.values[2] - gravity[2];
            readingsService.add(gravity[1]);
            //add to historical record list the meausrement:
            priorGravity.add(sensorEvent.values[1]);

            for (int i=1;i<=readingsService.size()-1;i++){
                readingsService.add(alpha*readingsService.get(i-1)+alpha*(gravity[i]-priorGravity.get(i-1)));
                //readingsService is the exprected filtered results!
                System.out.println("readingsService filtered entries: "+readingsService.get(i));
            }


            System.out.println(mAccelerometer.toString());
            System.out.println("accelartion values on x y z: ");
            System.out.println(sensorEvent.values[0]);
            System.out.println(sensorEvent.values[1]);
            System.out.println(sensorEvent.values[2]);

            System.out.println("accelartion values on x y z corrected: ");
            System.out.println(linear_acceleration[0]);
            System.out.println(linear_acceleration[1]);
            System.out.println(linear_acceleration[2]);
            // HOESTLY THINK WE SHOULD DO A 5 SECOND STATISTICAL SAMPLING SO THAT WE KNOW WHAT A OUTTLIER LOOKS LIKE.

            //if linear acceleartion is not the same sign as zAxisMonitor, then we now have a change in direction of acceleration
            //if  (Math.signum(sensorEvent.values[1])!=  Math.signum(zAxisMonitor)){

            //if ((sensorEvent.values[1]>0.50)||(sensorEvent.values[1]<0.16)){
            if ((sensorEvent.values[1]< CaliberateActivity.lowerRange)||(sensorEvent.values[1]>CaliberateActivity.upperRange)){
                //if ((sensorEvent.values[1]> Float.parseFloat(rotation_testing.EXTRA_LOWER_INFO))&&(sensorEvent.values[1]<Float.parseFloat(rotation_testing.EXTRA_RANGE_INFO))){
                //this is the time from timer uptime difference
                //take previous time reading, and current time reading, get the difference = time for this distance increment

                //begining of movement, trigger timer. first movement detected
                if (timer==0){
                    timer=uptimeMillis();
                    timerTotal= uptimeMillis();
                    yAxisMonitor= sensorEvent.values[1];
                    System.out.println("change in acceleration direction has been detected for first time!");
                    System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^6");
                    //this gives us initial accelration, and when it was detected, so we cau use yAxis to get next read s=0+1/2*at^2 for distance
                    //i think we should grab the accelration here, so that the next acceleration reading, we can measure time
                    //elapsed, and find out distance traveled.
                    //it would be better to know sampling rate, that way we can know how long this happs.
                    // we need to konw more about accelration here. accleartion starts at 0, so b/w the first reading of change
                    //and this accelartion rate, we get the anser for change in velocity as 1/2*bh = h=accelartin, base=sample time min, this is the first sensor detection
                    //we also have the problem that accelration can be 0 at constant speed, but that just means that when we detect the slow down
                    //we are just declerating.
                }
                else {
                    //change in time= in miliseconds. converted to seconds
                    double timerCng=(uptimeMillis()-timer)/1000;
                    //increasing or decreasing accelration triangular graph shapes+the rectuagle beneath them are area of change in velocity
                    double chngVelocity=0.0;
                    if  (yAxisMonitor < sensorEvent.values[1]){
                        chngVelocity=0.5*(sensorEvent.values[1]-yAxisMonitor)*timerCng+(yAxisMonitor*timerCng);
                    }
                    else if  (yAxisMonitor > sensorEvent.values[1]){
                        chngVelocity=0.5*(yAxisMonitor-sensorEvent.values[1])*timerCng+(sensorEvent.values[1]*timerCng);
                    }else{
                        //yaxis==currentsensorvalue
                        chngVelocity=yAxisMonitor*timerCng;
                    }



                    //change in time= in miliseconds. converted to seconds
                    //double timerCng=(uptimeMillis()-timer)/1000;

                    //change in velocity =a*change in time
                    //note that yaxis  monitor is actually the previous reading of accleration. on the next accelarion change
                    //double chngVelocity= yAxisMonitor*timerCng;// m/s
                    velocity=velocity+chngVelocity;// m/s
                    double distanceSns=velocity*timerCng; // should be meters
                    System.out.println(distanceSns/100+" distance in cm");
                    //total distance up until now:
                    distance+=distanceSns;//meters
                    System.out.println("change in acceleration direction has been detected!");
                    System.out.println("**************************************************************************************************");

                    //update time to be used as  next perivous time. timer is being used as pervious time
                    timer=uptimeMillis();
                    System.out.println("sensorEvent.val: "+sensorEvent.values[1]);
                    System.out.println("yAxisMonitor: "+yAxisMonitor);
                    System.out.println("velocity: "+velocity);
                    System.out.println("timer: "+timer/1000);
                    timerTotal=(uptimeMillis() -timerTotal )/1000;
                    System.out.println("TIMERTOTAL: " +timerTotal);
                    System.out.println("distance: "+distance);
                    System.out.println("timerCng: "+timerCng);
                    System.out.println("chngVelocity: "+chngVelocity);
                    System.out.println("distanceSns: "+distanceSns);
                    yAxisMonitor= sensorEvent.values[1];
                    //Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                    //Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                    //r.play();
                    /*

                    //change in time=
                    double timerTmp=uptimeMillis()-timer;
                    //
                    double distanceSns=(velocity*timerTmp)+0.5*(sensorEvent.values[1])*(timerTmp*timerTmp);
                    distance+=distanceSns;
                    System.out.println("change in acceleration direction has been detected!");
                    System.out.println("**************************************************************************************************");
                    velocity=distance/timerTmp;
                    //update time to be used as  next perivous time. timer is being used as pervious time
                    timer=uptimeMillis();
                    System.out.println("velocity: "+velocity);
                    System.out.println("distance: "+distance);
                    //Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                    //Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                    //r.play();
                     */
                }
            }else {
                //this is a pretty rough estimate of when the movement has stopped. prob need to compare with previous run as well to 2.7
                if((sensorEvent.values[1] >0.24) && (sensorEvent.values[1] <0.27) &&((yAxisMonitor+.02>sensorEvent.values[1])&&( yAxisMonitor-.02<sensorEvent.values[1])))
                    timer=0;
                //this is for the next triggered comparison to make sure that we are truly stopped  movement
                yAxisMonitor= sensorEvent.values[1];
            }
            //zAxisMonitor=sensorEvent.values[1];
        }

        /*
        if (mRotation!=null){
            System.out.println(mRotation.toString());
            System.out.println(sensorEvent.values[0]);
        }*/
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }




    //this defines a function that the activity can call because activity uses onBind connection method. returning AccelermeterServiceBInder obejct, then use getAccel method
    //this is an inner class. rotation testing will communicate with this service and communitcate with it using ServiceConnection object
    public class AccelerMeterServiceBinder extends Binder {
        AccelerMeterService getAccelData(){
            return AccelerMeterService.this;
        }
    }
    //this binder object will deliver through the servicecONNECTION  object to roataion testing this service object.
    @Override
    public IBinder onBind(Intent intent) {
        return binder;

        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
    }
}
