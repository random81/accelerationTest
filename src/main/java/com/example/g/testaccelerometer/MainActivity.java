package com.example.g.testaccelerometer;

//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.content.Intent;
import android.widget.TextView;
public class MainActivity extends Activity {
    private String stringMessg = "";
    private String stringMessgZ = "";
    private String stringMessgX= "";
    private boolean dataToSave=false;
    public static final String EXTRA_ROTATE_INFO="";
    public static final String EXTRA_ROTATE_INFO_Z="";
    public static final String EXTRA_ROTATE_INFO_X="";
    //public static final String EXTRA_ROTATE_INFO="";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Intent intent=getIntent();
        //we will use intent object to retreive data


        if (intent.getStringExtra(EXTRA_ROTATE_INFO)!=null){
            //this should be null when we are starting off
            stringMessg = intent.getStringExtra("distanceY");
            stringMessgZ = intent.getStringExtra("distanceZ");
            stringMessgX= intent.getStringExtra("distanceX");
            System.out.println(stringMessg+" THIS IS A DISTANCE INTENT MESSAGE CONTENTS AT MAIN.");
            dataToSave=true;
            //grabbing the views for population with information
            TextView accelerationInfo=(TextView) findViewById(R.id.distanceMainY);
            TextView accelerationInfoX=(TextView) findViewById(R.id.distanceMainX);
            TextView accelerationInfoZ=(TextView) findViewById(R.id.distanceMainZ);
            //population of views with information
            accelerationInfo.setText(stringMessg);
            accelerationInfoZ.setText(stringMessgZ);
            accelerationInfoX.setText(stringMessgX);
        }
        else{

            System.out.println(" EXTRA ROTATE INFO IS NULL! prob. suppose to be NULL :)  test of intent message contents at main.");
        }

    }

    @Override
    public void onResume()
    {
        //Since on create only happens once, this should make things work when we come back for another test
        super.onResume();

        ///stringMessg = "";
        //dataToSave=false;
        //String EXTRA_ROTATE_INFO="";

        setContentView(R.layout.activity_main);
        Intent intent=getIntent();

        if (intent.getStringExtra(EXTRA_ROTATE_INFO)!=null){

            /*

            intent.putExtra("distanceY",Double.toString(distance));
            intent.putExtra("distanceX",Double.toString(distanceX));
            intent.putExtra("distanceZ",Double.toString(distanceZ));
             */

            stringMessg = intent.getStringExtra("distanceY");
            stringMessgZ = intent.getStringExtra("distanceZ");
            stringMessgX= intent.getStringExtra("distanceX");
            System.out.println(stringMessg+" THIS IS A DISTANCE INTENT MESSAGE CONTENTS AT MAIN.");
            dataToSave=true;
            TextView accelerationInfo=(TextView) findViewById(R.id.distanceMainY);
            TextView accelerationInfoX=(TextView) findViewById(R.id.distanceMainX);
            TextView accelerationInfoZ=(TextView) findViewById(R.id.distanceMainZ);
            accelerationInfo.setText(stringMessg);
            accelerationInfoZ.setText(stringMessgZ);
            accelerationInfoX.setText(stringMessgX);
        }
        else{

            System.out.println(" EXTRA ROTATE INFO IS NULL! prob. suppose to be NULL :)  test of intent message contents at main.");
        }
        //TriggerEventListener mListener = new TriggerEventListener();
        //thinking about using the array to initilze and register devices. need sensor.getType method to do that in a for loop.

        /*

        for (int i = 0 ; i<deviceSensors.size();i++){
            System.out.println(deviceSensors.get(i));
            System.out.println("this is a get of sensor from List");

        }
         */


    }

    /*
    it appears as if this is unneccessary being that it is a static variable. static is not destroyed by activity destruction.  the code works however.

    public static final String EXTRA_ROTATE_INFO="";

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        if(dataToSave){

            savedInstanceState.putString(EXTRA_ROTATE_INFO, stringMessg);
        }
    }
     */



    public void onClickBeginTest(View view) {
        System.out.println("testing 123... YOU HAVE CLICKED THE START BUTTON!");
        //go to android monitor alt-6 to see this message when clicking test button
        //I think it makes sense to have a count down here.
        Intent intent= new Intent(this,CaliberateActivity.class);
        startActivity(intent);
        //setContentView(R.layout.activity_rotation_testing);
        //in addition to the new layout being loaded, we should launch the accelormeter testing class now. should we just call a new activity?
    }
}
