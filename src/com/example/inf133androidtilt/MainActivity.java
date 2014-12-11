package com.example.inf133androidtilt;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.OrientationEventListener;
import android.widget.TextView;

public class MainActivity extends Activity {
	//setup the variables
	//Sensors
	private SensorManager mSensorManager;
	private SensorEventListener mEventListenerTilt;
	
	//Orientation
	private Sensor accelerometer;
	private Sensor magnetometer;
	private float[] mGravity;
	private float[] mGeomagnetic;
	private int notPlayed = 0;
	
	//Text displaying the angles
	private TextView mTextViewA;
	private TextView mTextViewP;
	private TextView mTextViewR;
	private float azimuth, pitch, roll;
	
	//Sound
	private AudioAttributes attributes;
	private SoundPool sounds;
	private int sfx_left, sfx_right, sfx_upright, sfx_upsidedown, sfx_flatup, sfx_flatdown;
	
	//Updates the text and checks if the device tilts to the correct angle then plays the sound
	private void updateUI() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mTextViewA.setText("Azimuth: "+azimuth);
				mTextViewP.setText("Pitch: "+pitch);
				mTextViewR.setText("Roll: "+roll);
				
				//flatup
				if ((pitch > -3 && pitch < 3) && (roll > -3 && roll < 3) && notPlayed != 1) {
					sounds.play(sfx_flatup, 1.0f, 1.0f, 0, 0, 1.0f);
					notPlayed = 1;
				}
				//flatdown
				else if ((pitch > -3 && pitch < 3) && (Math.abs(roll) > 170 && Math.abs(roll) < 180) && notPlayed != 2) {
					sounds.play(sfx_flatdown, 1.0f, 1.0f, 0, 0, 1.0f);
					notPlayed = 2;
				}
				//right
				else if (roll > 85 && roll < 90 && notPlayed != 3) {
					sounds.play(sfx_right, 1.0f, 1.0f, 0, 0, 1.0f);
					notPlayed = 3;
				}
				//left
				else if (roll > -90 && roll < -85 && notPlayed != 4) {
					sounds.play(sfx_left, 1.0f, 1.0f, 0, 0, 1.0f);
					notPlayed = 4;
				}
				//upright
				else if ((pitch > -87 && pitch < -80) && (roll > -3 && roll < 3) && notPlayed != 5) {
					sounds.play(sfx_upright, 1.0f, 1.0f, 0, 0, 1.0f);
					notPlayed = 5;
				}
				//upsidedown
				else if ((pitch > 80 && pitch < 87) && (roll > -3 && roll < 3) && notPlayed != 6) {
					sounds.play(sfx_upsidedown, 1.0f, 1.0f, 0, 0, 1.0f);
					notPlayed = 6;
				}
			}
		});
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Start up the sound files
		startupSFX();
		
		//Make the activity_main.xml the app homepage
		setContentView(R.layout.activity_main);
		
		//Locate the texts' ID
		mTextViewA = (TextView) findViewById(R.id.textView1);
		mTextViewP = (TextView) findViewById(R.id.textView2);
		mTextViewR = (TextView) findViewById(R.id.textView3);
		
		//Specify which sensors to use
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		
		//Senses the orientation change and records it
		mEventListenerTilt = new SensorEventListener() {
			@Override
			public void onSensorChanged(SensorEvent event) {
				if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
					mGravity = event.values;
				if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
					mGeomagnetic = event.values;
				if (mGravity != null && mGeomagnetic != null) {
					float R[] = new float[9];
					float I[] = new float[9];
					boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
					if (success) {
						float values[] = new float[3];
						SensorManager.getOrientation(R, values);
						
						//record the three angles to their variables
						azimuth = (int) Math.toDegrees(values[0]);
						pitch = (int) Math.toDegrees(values[1]);
						roll = (int) Math.toDegrees(values[2]);
					}
				}
				updateUI();
			}
			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy){}
		};
		//Register the accelerometer and magnetometer to listen
		setListeners();
	}
	
	protected void startupSFX() {
		attributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
		sounds = new SoundPool.Builder().setAudioAttributes(attributes).build();
		
		//Set each sound to their correct orientation
		sfx_left = sounds.load(this, R.raw.game_start, 1);
		sfx_right = sounds.load(this, R.raw.link_start, 1);
		sfx_upright = sounds.load(this, R.raw.nyanpasu, 1);
		sfx_upsidedown = sounds.load(this, R.raw.space_dandy, 1);
		sfx_flatup = sounds.load(this, R.raw.kohina_desu, 1);
		sfx_flatdown = sounds.load(this, R.raw.fma_song, 1);
	}
	
	protected void setListeners() {
		mSensorManager.registerListener(mEventListenerTilt, accelerometer, SensorManager.SENSOR_DELAY_UI);
	    mSensorManager.registerListener(mEventListenerTilt, magnetometer, SensorManager.SENSOR_DELAY_UI);
	}

	@Override
    public void onResume() {
        super.onResume();
        setListeners();
    }
	
	protected void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(mEventListenerTilt);
		sounds.autoPause();
	}

	@Override
	public void onStop() {
		mSensorManager.unregisterListener(mEventListenerTilt);
		sounds.autoPause();
		super.onStop();
	}
}
