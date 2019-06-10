package com.example.g.testaccelerometer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import static android.os.SystemClock.uptimeMillis;
import static com.example.g.testaccelerometer.R.layout.activity_rotation_testing;
import static com.example.g.testaccelerometer.R.layout.begin_testing;

public class CaliberateActivity extends Activity implements SensorEventListener {


    static List<Float> sensorReadings=new ArrayList<Float>();
    static List<Float> readingsService=new ArrayList<Float>();
    static List<Float > timeStamps=new ArrayList<Float>();//time is in NANOSECONDS!
    static List<Float> priorGravity=new ArrayList<Float>();
    static float yAxisMonitor=0;
    //timer
    static long timer=0;//make it zero at first. timer starts as soon as we start moving, at first movement, and ends at last movement
    static long timerTotal=0;//make it zero at first. timer starts as soon as we start moving, at first movement, and ends at last movement
    static double velocity=0;
    static double velocityZ=0;
    static double velocityX=0;
    //distance
    static double distance;
    static double distanceZ;
    static double distanceX;
    private static SensorManager cSensorManager;
    private static SensorManager managerFindAllSensors;
    private static Sensor mRotation;
    boolean firstRun=true;
    boolean caliBend=false;
    boolean caliberating=true;
    //distance
    static float lowerRange=0;
    static float upperRange=0;
    static float startTime=uptimeMillis();
    static float zAxisCalib=0;
    static float yAxisCalib=0;
    static float xAxisCalib=0;
    private static Sensor cAccelerometer;
    String rangeData="rangeData data string placeholder";
    //creating accelerrmotere service binding variables
    private CaliberateService CaliberateMeter;
    boolean caliberationComplete=false;
    private RotationService rotationMeter;
    private GyroscopeService gyroMeter;

    static List<Float> readings=new ArrayList<Float>();
    //thinking about using the array to initilze and register devices. need sensor.getType method to do that in a for loop.
    private boolean bound=false;


    //we need to ask the service for the data!
    @Override
    public void onCreate(Bundle savedInstanceState) {

        //TriggerEventListener mListener = new TriggerEventListener();
        managerFindAllSensors = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> deviceSensors = managerFindAllSensors.getSensorList(Sensor.TYPE_ALL);
        //thinking about using the array to initilze and register devices. need sensor.getType method to do that in a for loop.

        /*

        for (int i = 0 ; i<deviceSensors.size();i++){
            System.out.println(deviceSensors.get(i));
            System.out.println("this is a get of sensor from List");

        }
         */


        distance=0;
        distanceX=0;
        distanceZ=0;
        cSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        cAccelerometer = cSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        cSensorManager.registerListener(this, cAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        setContentView(R.layout.activity_caliberate);
        new runTimer().execute();

        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume()
    {
        //Since on create only happens once, this should make things work when we come back for another test
        super.onResume();
        //TriggerEventListener mListener = new TriggerEventListener();

        /*

        for (int i = 0 ; i<deviceSensors.size();i++){
            System.out.println(deviceSensors.get(i));
            System.out.println("this is a get of sensor from List");

        }
         */

        firstRun=true;
        caliBend=false;
        caliberating=true;
        caliberationComplete=false;

        distance=0;
        distanceX=0;
        distanceZ=0;
        //reinitilalize variables for next test:

        yAxisMonitor=0;
        xAxisCalib=0;
        yAxisCalib=0;
        //timer
        timer=0;//make it zero at first. timer starts as soon as we start moving, at first movement, and ends at last movement
        timerTotal=0;//make it zero at first. timer starts as soon as we start moving, at first movement, and ends at last movement
        //velocity initial

        velocityZ=0;
        velocityX=0;
        velocity=0;
        lowerRange=0;
        upperRange=0;
        startTime=uptimeMillis();
        zAxisCalib=0;
        yAxisCalib=0;
        xAxisCalib=0;
        managerFindAllSensors = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> deviceSensors = managerFindAllSensors.getSensorList(Sensor.TYPE_ALL);
        //thinking about using the array to initilze and register devices. need sensor.getType method to do that in a for loop.
        cSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        cAccelerometer = cSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        cSensorManager.registerListener(this, cAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        setContentView(R.layout.activity_caliberate);
        new runTimer().execute();
    }

    //save rotation data incase of activity destroy, BUT WE WILL ASK USER TO LOCK ORIENTATION PROBABLY!
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString( rotation_testing.EXTRA_RANGE_INFO,rangeData);
        savedInstanceState.putFloat( rotation_testing.EXTRA_LOWER_INFO,lowerRange);
        savedInstanceState.putFloat( rotation_testing.EXTRA_UPPER_INFO,upperRange);
    }
    private class runTimer extends AsyncTask<Void, Void, String>{
        @Override
        protected String doInBackground(Void... params){

            //for (int i=0; i<=readingsService.size()-1;i++)
            //    System.out.println("readingsService filtered entries REAL INTERACTION: "+readingsService.get(i));
            do {

                //System.out.println("do in background!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                float timeDiff= uptimeMillis() - startTime;
                if (timeDiff >= 5000 ) {
                    for (int i = (readingsService.size() - 1); i >=0 ; i--) {
                        readingsService.remove(i);
                        //priorGravity.remove(i);
                        //System.out.println("5 seconds zeroing out!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    }
                    //startTime= uptimeMillis();
                    caliberationComplete=true;
                    //reset
                    //caliberationComplete=true;
                    //System.out.println("zeroing out timer ");
                    //timer=0; //this will make timer 0 mid sensor change function, really messings things up asynchoroneasouly.
                }

            }while(!caliberationComplete);//TODO: should change this boolean to a more updated approp name
            return "bla";//I dont understand this null return from a void. weird.
        }
        protected void onPostExecute(String result) {

            onEndCaliberation();
            System.out.println(result);
            System.out.println("zeroing outzeroing outzeroing outzeroing outzeroing outzeroing out      ");

        }

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        /*
        for (int i=0;i<=timeStamps.size()-1;i++){
            System.out.println(timeStamps.get(i)+" timStamps "+i);
            System.out.println(sensorReadings.get(i)+" sensorReadings "+i);
        }
        what we want is a range, so we can have two variables, and update data initiallize to sensor value for first test, and from then on if item bigger or smaller than variable update
        //upper and lower range each time
         */
        //this is the caliberation phase
        //because we cannot use static in an else statement, just use modulus 5 seconds to empty up until x[1] the growing arrays
        //+5 on timeCliberate because 5 sec of caliberation
        //NON CALIBERATION PART ACTUAL HUMAN INTERACTION PART
        float gravity[]={0,0,0};
        float linear_acceleration[]={0,0,0};
        //Android documentation:
        //t is analoges to RC in a circuit. it is the latency the filter adds to the sensor event.. the time constant
        //alpha here is just a place holder! replace it with t/t+dt     dt=sensor's event delivery rate. =time between this and last event? looks like 1event/delay
        //t=RC=1/(2*pi*delay)         //delay is the frequency of sampling by sensor
        //alpha=t/t+dt
        //not sure about dt=1/delay //
        //dt=1/delay
        //high pass filter α := RC / (RC + dt)  vs.  Low pass filter α := dt / (RC + dt)
        //is alpha the low threashold freqeuency??? that threashold is suppose to filter out the low freq.

        gravity[0] =  sensorEvent.values[0];
        gravity[1] =  sensorEvent.values[1];
        gravity[2] =  sensorEvent.values[2];
        timeStamps.add((float)sensorEvent.timestamp);
        sensorReadings.add(sensorEvent.values[1]);
        //adding remaining sensor values x and z probably. use modulous to extract as needed
        sensorReadings.add(sensorEvent.values[0]);
        sensorReadings.add(sensorEvent.values[2]);
        if (timer==0){
            timer=uptimeMillis();
            timerTotal= uptimeMillis();
            yAxisMonitor= sensorEvent.values[1];
            //adding remaining value
            zAxisCalib=sensorEvent.values[2];
            xAxisCalib=sensorEvent.values[0];
            System.out.println("change in acceleration direction has been detected for first time!");
            System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
            System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
            System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
            System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
            System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
            //this gives us initial accelration, and when it was detected, so we cau use yAxis to get next read s=0+1/2*at^2 for distance
            //i think we should grab the accelration here, so that the next acceleration reading, we can measure time
            //elapsed, and find out distance traveled.
            //it would be better to know sampling rate, that way we can know how long this happs.
            // we need to konw more about accelration here. accleartion starts at 0, so b/w the first reading of change
            //and this accelartion rate, we get the anser for change in velocity as 1/2*bh = h=accelartin, base=sample time min, this is the first sensor detection
            //we also have the problem that accelration can be 0 at constant speed, but that just means that when we detect the slow down
            //we are just declerating.
        }else {
            long testTime=uptimeMillis();
            long timerCng = testTime - timer; /// 1000l;
            double timerCngDble= (double)timerCng/1000L;
            //increasing or decreasing accelration triangular graph shapes+the rectuagle beneath them are area of change in velocity
            System.out.println("timerCng in else statement here: "+ timerCngDble);
            double chngVelocity = 0.0;
            double chngVelocityX = 0.0;
            double chngVelocityZ = 0.0;
            if (yAxisMonitor < sensorEvent.values[1]) {
                //acceleration
                //3 situations: a1<0 & a2 <0 and a1<a2,   a1<0 & a2 >0 and a1<a2 , a1>0 & a2 >0 and a1<a2
                if ((yAxisMonitor<0) && (sensorEvent.values[1]<0)){
                    //triangle area
                     chngVelocity=0.5 * (sensorEvent.values[1] - yAxisMonitor) * timerCngDble;
                     chngVelocity+= sensorEvent.values[1] * timerCngDble;//rectangle above triangle flush with t axis

                }else if ((yAxisMonitor<0) && (sensorEvent.values[1]>0)){

                    //triangle area cosX=height/hypo
                    //better way to find the two trianles: use slope for whle line, slopeA=(a2-a1)/T1-T2 = 0-a1/x-0 Solve for X!
                    //(0-a1)/(x-0)=slopeA
                    //0-a1=slopeA*(x-0)
                    //0-a1/slopeA=x-0
                    float slopeA= (sensorEvent.values[1] - yAxisMonitor)/((float)(timerCngDble));
                    float T2=-yAxisMonitor/slopeA;

                    chngVelocity=0.5 * (yAxisMonitor) * T2;
                    chngVelocity+=0.5 * (sensorEvent.values[1]) * (timerCngDble-T2) ;

                    // chngVelocity=0.5 * Math.abs((sensorEvent.values[1] - yAxisMonitor)) * timerCng;
                    //we migh have a problem here, the negative value signifies change
                }else{
                    //non negative values
                    //rect+triangle

                    chngVelocity = 0.5 * (sensorEvent.values[1] - yAxisMonitor) * timerCngDble + (yAxisMonitor * timerCngDble);
                }
            } else if (yAxisMonitor > sensorEvent.values[1]) {
                //deceleration

                //3 situations: a1<0 & a2 <0 and a1>a2,   a1<0 & a2 >0 and a1>a2 , a1>0 & a2 >0 and a1>a2
                if ((yAxisMonitor<0) && (sensorEvent.values[1]<0)){
                        //triangle + rect

                    chngVelocity = 0.5 * Math.abs((sensorEvent.values[1] - yAxisMonitor)) * timerCngDble + (yAxisMonitor * timerCngDble);

                    chngVelocity=-(chngVelocity);
                    //we migh have a problem here, the negative value signifies change

                }else if ( (yAxisMonitor>0) && (sensorEvent.values[1]<0)){

                    //triangle area cosX=height/hypo
                    //better way to find the two trianles: use slope for whle line, slopeA=(a2-a1)/T1-T2 = 0-a1/x-0 Solve for X!
                    //(0-a1)/(x-0)=slopeA
                    //0-a1=slopeA*(x-0)
                    //0-a1/slopeA=x-0
                    float slopeA= (sensorEvent.values[1] - yAxisMonitor)/((float)(timerCngDble));
                    float T2=-yAxisMonitor/slopeA;

                    chngVelocity=0.5 * (yAxisMonitor) * T2;
                    chngVelocity+=0.5 * (sensorEvent.values[1]) * (timerCngDble-T2) ;

                }else{
                    //non negative values
                    chngVelocity = 0.5 * (yAxisMonitor - sensorEvent.values[1]) * timerCngDble + (sensorEvent.values[1] * timerCngDble);
                }

            } else {
                //it seems this else statement should never happen this is a on sensor change method. so acceleration hasnt changed, and this else doesnt happen
                //this means acceleration hasnt changed. acceleration * change in time. a*deltaT
                //yaxis==currentsensorvalue
                //this is a on sensor change method, if accelration hasnt changed, then this shouldnt be called.
                //chngVelocity = yAxisMonitor * timerCng;
            }


            //zAxisCalib=sensorEvent.values[2];
            //xAxisCalib=sensorEvent.values[0];
            //ALTER THIS TO XAXIS VARIABLE! THEN DO SAME FOR Z
            if (xAxisCalib < sensorEvent.values[0]) {
                //acceleration
                //3 situations: a1<0 & a2 <0 and a1<a2,   a1<0 & a2 >0 and a1<a2 , a1>0 & a2 >0 and a1<a2
                if ((xAxisCalib<0) && (sensorEvent.values[0]<0)){
                    //triangle area
                    chngVelocityX=0.5 * (sensorEvent.values[0] - xAxisCalib) * timerCngDble;
                    chngVelocityX+= sensorEvent.values[1] * timerCngDble;//rectangle above triangle flush with t axis

                }else if ((xAxisCalib<0) && (sensorEvent.values[0]>0)){

                    //triangle area cosX=height/hypo
                    //better way to find the two trianles: use slope for whle line, slopeA=(a2-a1)/T1-T2 = 0-a1/x-0 Solve for X!
                    //(0-a1)/(x-0)=slopeA
                    //0-a1=slopeA*(x-0)
                    //0-a1/slopeA=x-0
                    float slopeA= (sensorEvent.values[0] - xAxisCalib)/((float)(timerCngDble));
                    float T2=-xAxisCalib/slopeA;

                    chngVelocity=0.5 * (xAxisCalib) * T2;
                    chngVelocity+=0.5 * (sensorEvent.values[0]) * (timerCngDble-T2) ;

                    // chngVelocity=0.5 * Math.abs((sensorEvent.values[1] - yAxisMonitor)) * timerCng;
                    //we migh have a problem here, the negative value signifies change
                }else{
                    //non negative values
                    //rect+triangle

                    chngVelocityX = 0.5 * (sensorEvent.values[0] - xAxisCalib) * timerCngDble + (xAxisCalib * timerCngDble);
                }
            } else if (xAxisCalib > sensorEvent.values[0]) {
                //deceleration

                //3 situations: a1<0 & a2 <0 and a1>a2,   a1<0 & a2 >0 and a1>a2 , a1>0 & a2 >0 and a1>a2
                if ((xAxisCalib<0) && (sensorEvent.values[0]<0)){
                    //triangle + rect

                    chngVelocityX = 0.5 * Math.abs((sensorEvent.values[0] - xAxisCalib)) * timerCngDble + (xAxisCalib * timerCngDble);

                    chngVelocityX=-(chngVelocityX);
                    //we migh have a problem here, the negative value signifies change

                }else if ( (xAxisCalib>0) && (sensorEvent.values[0]<0)){

                    //triangle area cosX=height/hypo
                    //better way to find the two trianles: use slope for whle line, slopeA=(a2-a1)/T1-T2 = 0-a1/x-0 Solve for X!
                    //(0-a1)/(x-0)=slopeA
                    //0-a1=slopeA*(x-0)
                    //0-a1/slopeA=x-0
                    float slopeA= (sensorEvent.values[0] - xAxisCalib)/((float)(timerCngDble));
                    float T2=-xAxisCalib/slopeA;

                    chngVelocityX=0.5 * (xAxisCalib) * T2;
                    chngVelocityX+=0.5 * (sensorEvent.values[0]) * (timerCngDble-T2) ;

                }else{
                    //non negative values
                    chngVelocityX = 0.5 * (xAxisCalib - sensorEvent.values[0]) * timerCngDble + (sensorEvent.values[0] * timerCngDble);
                }

            } else {
                //it seems this else statement should never happen this is a on sensor change method. so acceleration hasnt changed, and this else doesnt happen
                //this means acceleration hasnt changed. acceleration * change in time. a*deltaT
                //yaxis==currentsensorvalue
                //how is this even possible? this is a on sensor change method, if accelration hasnt changed, then this shouldnt be called.
                //chngVelocity = yAxisMonitor * timerCng;
            }




            if (zAxisCalib < sensorEvent.values[2]) {
                //acceleration
                //3 situations: a1<0 & a2 <0 and a1<a2,   a1<0 & a2 >0 and a1<a2 , a1>0 & a2 >0 and a1<a2
                if ((zAxisCalib<0) && (sensorEvent.values[2]<0)){
                    //triangle area
                    chngVelocityZ=0.5 * (sensorEvent.values[2] - xAxisCalib) * timerCngDble;
                    chngVelocityZ+= sensorEvent.values[2] * timerCngDble;//rectangle above triangle flush with t axis

                }else if ((zAxisCalib<0) && (sensorEvent.values[2]>0)){

                    //triangle area cosX=height/hypo
                    //better way to find the two trianles: use slope for whle line, slopeA=(a2-a1)/T1-T2 = 0-a1/x-0 Solve for X!
                    //(0-a1)/(x-0)=slopeA
                    //0-a1=slopeA*(x-0)
                    //0-a1/slopeA=x-0
                    float slopeA= (sensorEvent.values[2] - zAxisCalib)/((float)(timerCngDble));
                    float T2=-zAxisCalib/slopeA;

                    chngVelocityZ=0.5 * (zAxisCalib) * T2;
                    chngVelocityZ+=0.5 * (sensorEvent.values[2]) * (timerCngDble-T2) ;

                    // chngVelocity=0.5 * Math.abs((sensorEvent.values[1] - yAxisMonitor)) * timerCng;
                    //we migh have a problem here, the negative value signifies change
                }else{
                    //non negative values
                    //rect+triangle

                    chngVelocityZ = 0.5 * (sensorEvent.values[2] - zAxisCalib) * timerCngDble + (zAxisCalib * timerCngDble);
                }
            } else if (zAxisCalib > sensorEvent.values[2]) {
                //deceleration

                //3 situations: a1<0 & a2 <0 and a1>a2,   a1<0 & a2 >0 and a1>a2 , a1>0 & a2 >0 and a1>a2
                if ((zAxisCalib<0) && (sensorEvent.values[2]<0)){
                    //triangle + rect

                    chngVelocityZ = 0.5 * Math.abs((sensorEvent.values[2] - zAxisCalib)) * timerCngDble + (zAxisCalib * timerCngDble);

                    chngVelocityZ=-(chngVelocityZ);
                    //we migh have a problem here, the negative value signifies change

                }else if ( (zAxisCalib>0) && (sensorEvent.values[2]<0)){

                    //triangle area cosX=height/hypo
                    //better way to find the two trianles: use slope for whle line, slopeA=(a2-a1)/T1-T2 = 0-a1/x-0 Solve for X!
                    //(0-a1)/(x-0)=slopeA
                    //0-a1=slopeA*(x-0)
                    //0-a1/slopeA=x-0
                    float slopeA= (sensorEvent.values[2] - zAxisCalib)/((float)(timerCngDble));
                    float T2=-zAxisCalib/slopeA;

                    chngVelocityZ=0.5 * (zAxisCalib) * T2;
                    chngVelocityZ+=0.5 * (sensorEvent.values[2]) * (timerCngDble-T2) ;

                }else{
                    //non negative values
                    chngVelocityZ = 0.5 * (zAxisCalib - sensorEvent.values[2]) * timerCngDble + (sensorEvent.values[2] * timerCngDble);
                }

            } else {
                //it seems this else statement should never happen this is a on sensor change method. so acceleration hasnt changed, and this else doesnt happen
                //this means acceleration hasnt changed. acceleration * change in time. a*deltaT
                //yaxis==currentsensorvalue
                // if accelration hasnt changed, then this shouldnt be called.
                //chngVelocity = yAxisMonitor * timerCng;
            }
            //change in time= in miliseconds. converted to seconds
            //double timerCng=(uptimeMillis()-timer)/1000;

            //change in velocity =a*change in time
            //note that yaxis  monitor is actually the previous reading of accleration. on the next accelarion change
            //double chngVelocity= yAxisMonitor*timerCng;// m/s
            velocity = velocity + chngVelocity;// m/s
            velocityX = velocityX + chngVelocityX;// m/s
            velocityZ = velocityZ + chngVelocityZ;// m/s
            double distanceSns = velocity * timerCngDble; // should be meters
            double distanceSnsX = velocityX * timerCngDble; // should be meters
            double distanceSnsZ = velocityZ * timerCngDble; // should be meters
            System.out.println(distanceSns / 100l + " distance in cm or meters?");
            System.out.println(distanceSnsX / 100l + " distance in cm or meters?");
            System.out.println(distanceSnsZ / 100l + " distance in cm or meters?");
            //total distance up until now:
            distance += distanceSns;//cm total or meters total?
            distanceX += distanceSnsX;//cm total or meters total?
            distanceZ += distanceSnsZ;//cm total or meters total?
            System.out.println("change in acceleration direction has been detected!");
            System.out.println("**************************************************************************************************");
            System.out.println("**************************************************************************************************");
            System.out.println("**************************************************************************************************");
            System.out.println("**************************************************************************************************");

            //update time to be used as  next perivous time. timer is being used as pervious time
            timer = uptimeMillis();
            System.out.println("sensorEvent.val: " + sensorEvent.values[1]);
            System.out.println("yAxisMonitor: " + yAxisMonitor);
            System.out.println("velocity: " + velocity);
            System.out.println("timer: " + timer / 1000l);
            //timerTotal = (uptimeMillis() - timerTotal) / 1000l;//i dont know why i did this it is just change in time as above. with incorrect data on subsequenct calculations current time - previous diffrenct bw start of activity and end.
            System.out.println("TIMERTOTAL: " + timerTotal);
            System.out.println("total distance: " + distance);
            System.out.println("total distanceZ: " + distanceZ);
            System.out.println("total distanceX: " + distanceX);
            System.out.println("timerCngDble: " + timerCngDble);
            System.out.println("chngVelocity: " + chngVelocity);
            System.out.println("distance in current sensor event: " + distanceSns);


            readingsService.add((float)distanceSns);
            readingsService.add((float)distanceSnsZ);
            readingsService.add((float)distanceSnsX);
            for (int i=0; i<=readingsService.size()-1;i++){
            /*    //WHY IS THIS HAPPENING?? WHY ADD SAME DISTANCE TO ENTIRE ARRAY?
                readingsService.add((float)distanceSns);
                readingsService.add((float)distanceSnsZ);
                readingsService.add((float)distanceSnsX);
                //should see here a list that is emptied every 5 seconds
*/
                System.out.println(""+i+": ");
                System.out.println("readingsService filtered entries REAL INTERACTION: "+readingsService.get(i));
            }
            yAxisMonitor = sensorEvent.values[1];
            xAxisCalib = sensorEvent.values[0];
            zAxisCalib = sensorEvent.values[2];
        }
    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public boolean getCaliberateEnd(){
        //true means end in caliberteActivity
        return caliBend;
    }

    @Override
    protected void onPause() {
        super.onPause();

        cSensorManager.unregisterListener(this);
        cSensorManager.unregisterListener(this);
        cSensorManager.unregisterListener(this);
        cSensorManager.unregisterListener(this);

        caliberationComplete=false;

        //reinitilalize variables for next test:

        yAxisMonitor=0;
        //timer
        timer=0;//make it zero at first. timer starts as soon as we start moving, at first movement, and ends at last movement
        timerTotal=0;//make it zero at first. timer starts as soon as we start moving, at first movement, and ends at last movement
        //velocity initial
        velocity=0;
        lowerRange=0;
        upperRange=0;
        startTime=uptimeMillis();
        zAxisCalib=0;
        yAxisCalib=0;
        xAxisCalib=0;
    }

    public void onClickEndTest(View view) {
        System.out.println("testing 123... you have clicked the end button!");
        //go to android monitor alt-6 to see this message when clicking test button
         caliberationComplete=true;
        //I think it makes sense to have a count down here.
        Intent intent= new Intent(this, MainActivity.class);
        cSensorManager.unregisterListener(this);

        startActivity(intent);
        //setContentView(R.layout.activity_rotation_testing);
        //in addition to the new layout being loaded, we should launch the accelormeter testing class now. should we just call a new activity?
    }
    //this defines a function that the activity can call because activity uses onBind connection method. returning AccelermeterServiceBInder obejct, then use getAccel method
    //this is an inner class. rotation testing will communicate with this service and communitcate with it using ServiceConnection object
    //this binder object will deliver through the servicecONNECTION  object to roataion testing this service object.
    //this is how we will end caliberation. after 5 seconds we will activate an activityStarter method
    public void onEndCaliberation() {
        //System.out.println("lowerRange " + lowerRange);
        //System.out.println("upperRange " + upperRange);
         System.out.println(sensorReadings.size()+" this is the final size of sensor array ");
        Intent intent= new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_ROTATE_INFO,Double.toString(distance));
        intent.putExtra(MainActivity.EXTRA_ROTATE_INFO_X,Double.toString(distanceX));
        intent.putExtra(MainActivity.EXTRA_ROTATE_INFO_Z,Double.toString(distanceZ));

        intent.putExtra("distanceY",Double.toString(distance));
        intent.putExtra("distanceX",Double.toString(distanceX));
        intent.putExtra("distanceZ",Double.toString(distanceZ));
        //intent.putExtra(rotation_testing.EXTRA_UPPER_INFO,upperRange);
        // cSensorManager.unregisterListener(this);
        for (int i=0;i<=readings.size()-1;i++){
            System.out.println("readings entries: "+readings.get(i));
        }
        //get mean, then get standard deviation. then use standard deviation to see if reading is out of distribution one would expect given stdv +/- mean
        float total=0f;
        for (int i=0;i<=readings.size()-1;i++){
            total+=readings.get(i);
        }
        float mean= total/readings.size();
        System.out.println(mean);
        total=0;
        for (int i=0;i<=readings.size()-1;i++){
            total+=(readings.get(i)-mean)*(readings.get(i)-mean);
        }
        float std=(float)Math.sqrt((double)(total/(readings.size()-1)));
        System.out.println(std+" - standard deviation");
        setContentView(begin_testing);
        caliberating=false;

        for (int i=0;i<=sensorReadings.size()-1;i++){
            //if("0,1,2,3,4,5,6,7,8,9")
            if((i%3==0)||(i==0))
                System.out.println(sensorReadings.get(i)+" sensorReadings X #"+i);
            else if(i%2==0)
                System.out.println(sensorReadings.get(i)+" sensorReadings Y #"+i);
            else
                System.out.println(sensorReadings.get(i)+" sensorReadings Z #"+i);
        }
        for (int i=0;i<=timeStamps.size()-1;i++){
            System.out.println(timeStamps.get(i)+" timStamps #"+i);
        }

        System.out.println((uptimeMillis()-startTime)/1000.0+" -total time ");

        //empty the arrays:
        timeStamps.clear();
        sensorReadings.clear();
        readingsService.clear();
        caliberationComplete=false;

        //reinitilalize variables for next test:

        yAxisMonitor=0;
        //timer
        timer=0;//make it zero at first. timer starts as soon as we start moving, at first movement, and ends at last movement
        timerTotal=0;//make it zero at first. timer starts as soon as we start moving, at first movement, and ends at last movement
        //velocity initial
        velocity=0;
        lowerRange=0;
        upperRange=0;
        startTime=uptimeMillis();
        zAxisCalib=0;
        yAxisCalib=0;
        xAxisCalib=0;
        cSensorManager.unregisterListener(this);
        startActivity(intent);
        //in addition to the new layout being loaded, we should launch the accelormeter testing class now. should we just call a new activity?
    }
}
/*

package com.example.g.testaccelerometer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import static android.os.SystemClock.uptimeMillis;
import static com.example.g.testaccelerometer.CaliberateService.lowerRange;
import static com.example.g.testaccelerometer.CaliberateService.upperRange;

public class CaliberateActivity extends Activity {
    //creating accelerrmotere service binding variables
    private CaliberateService CaliberateMeter;
    boolean caliberationComplete=false;
    private RotationService rotationMeter;
    private GyroscopeService gyroMeter;
    static long timerCaliberate=uptimeMillis();
    private boolean bound=false;
    private ServiceConnection connectionC = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            CaliberateService.CaliberationServiceBinder  CaliberationServiceBinder = (CaliberateService.CaliberationServiceBinder) iBinder;
            CaliberateMeter = CaliberationServiceBinder.getCaliberateData();
            ComponentName ender = componentName;
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

            CaliberateMeter=null;
            bound=false;

        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent= new Intent(this, CaliberateService.class);
        bindService(intent, connectionC, Context.BIND_AUTO_CREATE);
        watchCaliberate();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (bound){
            unbindService(connectionC);
            bound=false;
        }
    }
    //AT THIS POINT we need to ask the service for the data!
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caliberate);
    }


    String rangeData="rangeData data string placeholder";

    //save rotation data incase of activity destroy, BUT WE WILL ASK USER TO LOCK ORIENTATION PROBABLY!
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(rotation_testing.EXTRA_RANGE_INFO,rangeData);
    }
    private void watchCaliberate(){
       timerCaliberate=uptimeMillis();
        do {
            if (CaliberateMeter!=null){
                caliberationComplete=CaliberateMeter.getCaliberateEnd();
            }
            if (caliberationComplete) {
                System.out.println("onEndCalib triggered");
                onEndCaliberation();  //end activity
            }

        }while(!caliberationComplete);
    //}while((uptimeMillis()-timerCaliberate)/1000>=5);
        final Handler handler = new Handler();
        handler.post(new Runnable() {
           @Override
            public void run(){
               System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^run not ending..^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
               if (CaliberateMeter!=null){
                caliberationComplete=CaliberateMeter.getCaliberateEnd();
               }
               if (caliberationComplete){
                   handler.removeCallbacks(this);
                   handler.removeCallbacksAndMessages(this);
                     onEndCaliberation();  //end activity
               }else {
                   handler.postDelayed(this,5000);
               }
           }
        });
    }

    //this is how we will end caliberation. after 5 seconds we will activate an activityStarter method
    public void onEndCaliberation() {

 //       System.out.println("caliberation ender function activated !");
        //go to android monitor alt-6 to see this message when clicking test button
        //I think it makes sense to have a count down here.
   //     Intent intent= new Intent(this, MainActivity.class);
    //    intent.putExtra(rotation_testing.EXTRA_RANGE_INFO,rangeData);
   //     startActivity(intent);

//        System.out.println("caliberation ender function activated !");
        //go to android monitor alt-6 to see this message when clicking test button
        //I think it makes sense to have a count down here.
        //we have to destroy the bind in order to stop the service

        System.out.println("lowerRange " + lowerRange);
        System.out.println("upperRange " + upperRange);
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
*/

        /*

        //previous version with incomplete filter,  lacks cutoff , aka Fc value, neccsrssary for getting RC aka time constant

        else {
            //because we cannot use static in an else statement, just use modulus 5 seconds to empty up until x[1] the growing arrays
            //+5 on timeCliberate because 5 sec of caliberation
            if ((uptimeMillis() - (timerCaliberate+5)) % 5 ==0) {
                for (int i =1; i<=readingsService.size()-1;i--) {
                    readingsService.remove(i);
                    priorGravity.remove(i);
                }
            }
            //NON CALIBERATION PART ACTUAL HUMAN INTERACTION PART
            float gravity[]={0,0,0};
            float linear_acceleration[]={0,0,0};
            //t is analoges to RC in a circuit. it is the latency the filter adds to the sensor event.. the time constant
            //alpha here is just a place holder! replace it with t/t+dt     dt=sensor's event delivery rate. =time between this and last event? looks like 1event/delay
            //t=RC=1/(2*pi*delay)         //delay is the frequency of sampling by sensor
            //alpha=t/t+dt
            //not sure about dt=1/delay //
            //dt=1/delay
            //high pass filter α := RC / (RC + dt)  vs.  Low pass filter α := dt / (RC + dt)
            float dt=1f/ cAccelerometer.getMinDelay();//TODO: is this really 1/sample rate??

            //low pass filer implementation
            final float RC=(float)(1f/(2f*Math.PI*cAccelerometer.getMinDelay())); //TODO: is minDelay really the frequency?
            //SEEMS LIKE CUTOFF FREQUENCY IS JUST THE FREQUENCY OF AUTOMATED SAMPLING BY THE SENSOR.

            //TODO: after 5 seconds of running the real application we should clear the array
            //is alpha the low threashold freqeuency??? that threashold is suppose to filter out the low freq.

            final float alpha=dt/(RC+dt);//not sure about time-uptimeMillies=dt ???

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
            final float dTgyroEx=(sensorEvent.timestamp-timer)*(1f/cAccelerometer.getMinDelay());
            //this might be a better way to get the dt value,
            // Remove the gravity contribution with the high-pass filter.
            //linear_acceleration[0] = sensorEvent.values[0] - gravity[0];
            // linear_acceleration[1] = sensorEvent.values[1] - gravity[1];
            // linear_acceleration[2] = sensorEvent.values[2] - gravity[2];
            int readServpriorIndex=readingsService.size()-1;
            priorGravity.add(sensorEvent.values[1]);
            int gravPreviousIndex=priorGravity.size()-2;
            readingsService.add(alpha*readingsService.get(readServpriorIndex)+alpha*(gravity[1]-priorGravity.get(gravPreviousIndex)));
            //readingsService is the exprected filtered results! sds
            for (int i=0; i<=readingsService.size()-1;i++)
                System.out.println("readingsService filtered entries REAL INTERACTION: "+readingsService.get(i));

            if ((sensorEvent.values[1] < CaliberateActivity.lowerRange) || (sensorEvent.values[1] > CaliberateActivity.upperRange)) {
                //if ((sensorEvent.values[1]> Float.parseFloat(rotation_testing.EXTRA_LOWER_INFO))&&(sensorEvent.values[1]<Float.parseFloat(rotation_testing.EXTRA_RANGE_INFO))){
                //this is the time from timer uptime difference
                //take previous time reading, and current time reading, get the difference = time for this distance increment

                //begining of movement, trigger timer. first movement detected
                //change in time= in miliseconds. converted to seconds
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
                }else {
                    double timerCng = (uptimeMillis() - timer) / 1000l;
                    //increasing or decreasing accelration triangular graph shapes+the rectuagle beneath them are area of change in velocity
                    double chngVelocity = 0.0;
                    if (yAxisMonitor < sensorEvent.values[1]) {
                        chngVelocity = 0.5 * (sensorEvent.values[1] - yAxisMonitor) * timerCng + (yAxisMonitor * timerCng);
                    } else if (yAxisMonitor > sensorEvent.values[1]) {
                        chngVelocity = 0.5 * (yAxisMonitor - sensorEvent.values[1]) * timerCng + (sensorEvent.values[1] * timerCng);
                    } else {
                        //it seems this else statement should never happen this is a on sensor change method. so acceleration hasnt changed, and this else doesnt happen
                        //this means acceleration hasnt changed. acceleration * change in time. a*deltaT
                        //yaxis==currentsensorvalue
                        //how is this even possible? this is a on sensor change method, if accelration hasnt changed, then this shouldnt be called.
                        //chngVelocity = yAxisMonitor * timerCng;
                    }


                    //change in time= in miliseconds. converted to seconds
                    //double timerCng=(uptimeMillis()-timer)/1000;

                    //change in velocity =a*change in time
                    //note that yaxis  monitor is actually the previous reading of accleration. on the next accelarion change
                    //double chngVelocity= yAxisMonitor*timerCng;// m/s
                    velocity = velocity + chngVelocity;// m/s
                    double distanceSns = velocity * timerCng; // should be meters
                    System.out.println(distanceSns / 100l + " distance in cm");
                    //total distance up until now:
                    distance += distanceSns;//meters
                    System.out.println("change in acceleration direction has been detected!");
                    System.out.println("**************************************************************************************************");

                    //update time to be used as  next perivous time. timer is being used as pervious time
                    timer = uptimeMillis();
                    System.out.println("sensorEvent.val: " + sensorEvent.values[1]);
                    System.out.println("yAxisMonitor: " + yAxisMonitor);
                    System.out.println("velocity: " + velocity);
                    System.out.println("timer: " + timer / 1000l);
                    timerTotal = (uptimeMillis() - timerTotal) / 1000l;
                    System.out.println("TIMERTOTAL: " + timerTotal);
                    System.out.println("distance: " + distance);
                    System.out.println("timerCng: " + timerCng);
                    System.out.println("chngVelocity: " + chngVelocity);
                    System.out.println("distanceSns: " + distanceSns);
                    yAxisMonitor = sensorEvent.values[1];
                }
            }
        }
        /*




         */
//zAxisMonitor=sensorEvent.values[1];
//no longer caliberating, this is testing phase.
