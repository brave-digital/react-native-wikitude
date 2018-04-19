package com.brave.wikitudebridge;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.wikitude.architect.ArchitectJavaScriptInterfaceListener;
import com.wikitude.architect.ArchitectStartupConfiguration;
import com.wikitude.architect.ArchitectView;
import com.wikitude.architect.services.camera.CameraLifecycleListener;
import com.wikitude.common.camera.CameraSettings;
import com.wikitude.common.permission.PermissionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class WikitudeActivity extends Activity {

    public static final String EXTRAS_KEY_AR_URL = "ar_url";
    public static final String EXTRAS_KEY_HAS_GEO = "ar_has_geo";
    public static final String EXTRAS_KEY_HAS_IR = "ar_has_ir";
    public static final String EXTRAS_KEY_HAS_INSTANT = "ar_has_instant";
    public static final String EXTRAS_KEY_SDK_KEY = "ar_sdk_key";

    public static final int CULLING_DISTANCE_DEFAULT_METERS = 50 * 1000;


    protected static final String TAG = "WikitudeActivity";
    protected String wikitudeSDKKey = "";

    /**
     * holds the Wikitude SDK AR-View, this is where camera, markers, compass, 3D models etc. are rendered
     */
    protected ArchitectView architectView;

    /**
     * JS interface listener handling e.g. 'AR.platform.sendJSONObject({foo:"bar", bar:123})' calls in JavaScript
     */
    protected ArchitectJavaScriptInterfaceListener mArchitectJavaScriptInterfaceListener;

    /**
     * worldLoadedListener receives calls when the AR world is finished loading or when it failed to laod.
     */
    protected ArchitectView.ArchitectWorldLoadedListener worldLoadedListener;
    /**
     * sensor accuracy listener in case you want to display calibration hints
     */

    protected ArchitectView.SensorAccuracyChangeListener sensorAccuracyListener;
    /**
     * location listener receives location updates and must forward them to the architectView
     */
    protected LocationListener 				locationListener;


    protected boolean hasGeolocation = false;
    protected boolean hasInstantTracking = false;
    protected boolean hasImageRecognition = true;
    protected String architectWorldURL = "";
    protected WikitudeActivity thisActivity;

    private int getFeatures() {
        int features = (this.hasGeolocation ? ArchitectStartupConfiguration.Features.Geo : 0) |
                (this.hasImageRecognition ? ArchitectStartupConfiguration.Features.ImageTracking : 0) |
                (this.hasInstantTracking ? ArchitectStartupConfiguration.Features.InstantTracking : 0) ;
        return features;
    }




    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

	      this.thisActivity = this;

	      this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);

        Bundle extras = getIntent().getExtras();

        this.architectWorldURL = extras.getString(EXTRAS_KEY_AR_URL);
        this.hasGeolocation = extras.getBoolean(EXTRAS_KEY_HAS_GEO, false);
        this.hasImageRecognition = extras.getBoolean(EXTRAS_KEY_HAS_IR, false);
        this.hasInstantTracking = extras.getBoolean(EXTRAS_KEY_HAS_INSTANT, false);
        this.wikitudeSDKKey = extras.getString(EXTRAS_KEY_SDK_KEY, "");

	      Log.i(TAG, "Wikitude Key is "+this.wikitudeSDKKey);

	      if (Objects.equals(this.wikitudeSDKKey, ""))
	      {
		      this.architectView = null;
		      Toast.makeText(getApplicationContext(), "Unable to start AR without an SDK Key", Toast.LENGTH_SHORT).show();
		      Log.e(this.getClass().getName(), "Wikitude SDK Key was empty. Please supply a SDK Key in the StartAR() function.");
	      	return;
	      }

        this.setVolumeControlStream( AudioManager.STREAM_MUSIC );
        setContentView(R.layout.activity_wikitude);
        this.setTitle("AR");

        // set AR-view for life-cycle notifications etc.
        this.architectView = (ArchitectView)this.findViewById( R.id.architectView );

		    // pass SDK key if you have one, this one is only valid for this package identifier and must not be used somewhere else
        final ArchitectStartupConfiguration config = new ArchitectStartupConfiguration();
        config.setLicenseKey(this.wikitudeSDKKey);
        config.setFeatures(this.getFeatures());
        config.setCameraPosition(CameraSettings.CameraPosition.DEFAULT);
        config.setCameraResolution(CameraSettings.CameraResolution.AUTO);
        config.setCamera2Enabled(false);

        this.architectView.setCameraLifecycleListener(getCameraLifecycleListener());
        try {
            // first mandatory life-cycle notification
            this.architectView.onCreate( config );
        } catch (RuntimeException rex) {
            this.architectView = null;
            Toast.makeText(getApplicationContext(), "can't create AR Architect View", Toast.LENGTH_SHORT).show();
            Log.e(this.getClass().getName(), "Exception in ArchitectView.onCreate()", rex);
        }

        // set world loaded listener if implemented
        this.worldLoadedListener = this.getWorldLoadedListener();

        // register valid world loaded listener in architectView, ensure this is set before content is loaded to not miss any event
        if (this.worldLoadedListener != null && this.architectView != null) {
            this.architectView.registerWorldLoadedListener(worldLoadedListener);
        }

        // set accuracy listener if implemented, you may e.g. show calibration prompt for compass using this listener
        this.sensorAccuracyListener = this.getSensorAccuracyListener();

        // set JS interface listener, any calls made in JS like 'AR.platform.sendJSONObject({foo:"bar", bar:123})' is forwarded to this listener, use this to interact between JS and native Android activity/fragment
        this.mArchitectJavaScriptInterfaceListener = this.getArchitectJavaScriptInterfaceListener();

        // set JS interface listener in architectView, ensure this is set before content is loaded to not miss any event
        if (this.mArchitectJavaScriptInterfaceListener != null && this.architectView != null) {
            this.architectView.addArchitectJavaScriptInterfaceListener(mArchitectJavaScriptInterfaceListener);
        }


        if (this.hasGeolocation) {

//
//            // listener passed over to locationProvider, any location update is handled here
//            this.locationListener = new LocationListener() {
//
//                @Override
//                public void onStatusChanged( String provider, int status, Bundle extras ) {
//                }
//
//                @Override
//                public void onProviderEnabled( String provider ) {
//                }
//
//                @Override
//                public void onProviderDisabled( String provider ) {
//                }
//
//                @Override
//                public void onLocationChanged( final Location location ) {
//                    // forward location updates fired by LocationProvider to architectView, you can set lat/lon from any location-strategy
//                    if (location!=null) {
//                        // sore last location as member, in case it is needed somewhere (in e.g. your adjusted project)
//                        AbstractArchitectCamActivity.this.lastKnownLocaton = location;
//                        if ( AbstractArchitectCamActivity.this.architectView != null ) {
//                            // check if location has altitude at certain accuracy level & call right architect method (the one with altitude information)
//                            if ( location.hasAltitude() && location.hasAccuracy() && location.getAccuracy()<7) {
//                                AbstractArchitectCamActivity.this.architectView.setLocation( location.getLatitude(), location.getLongitude(), location.getAltitude(), location.getAccuracy() );
//                            } else {
//                                AbstractArchitectCamActivity.this.architectView.setLocation( location.getLatitude(), location.getLongitude(), location.hasAccuracy() ? location.getAccuracy() : 1000 );
//                            }
//                        }
//                    }
//                }
//            };


            this.locationListener = null;

        } else {
            this.locationListener = null;
        }


    }




    public ArchitectView.SensorAccuracyChangeListener getSensorAccuracyListener() {
        return new ArchitectView.SensorAccuracyChangeListener() {
            @Override
            public void onCompassAccuracyChanged( int accuracy ) {
				/* UNRELIABLE = 0, LOW = 1, MEDIUM = 2, HIGH = 3 */
               /*
               Show the Compass Accuracy Low message if the sensor accuracy drops below medium.

                if ( accuracy < SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM && WikitudeActivity.this != null && !WikitudeActivity.this.isFinishing() && System.currentTimeMillis() - WikitudeActivity.this.lastCalibrationToastShownTimeMillis > 5 * 1000) {
                    Toast.makeText( SampleCamActivity.this, R.string.compass_accuracy_low, Toast.LENGTH_LONG ).show();
                    SampleCamActivity.this.lastCalibrationToastShownTimeMillis = System.currentTimeMillis();
                }
                */
            }
        };
    }


    public ArchitectJavaScriptInterfaceListener getArchitectJavaScriptInterfaceListener() {
        return new ArchitectJavaScriptInterfaceListener() {
            @Override
            public void onJSONObjectReceived(JSONObject jsonObject) {

                //Allows the app to react to JSON actions sent through the Wikitude Architect World.

                try {
                    /*
                    switch (jsonObject.getString("action")) {


                        case "capture_screen":
                            SampleCamActivity.this.architectView.captureScreen(ArchitectView.CaptureScreenCallback.CAPTURE_MODE_CAM_AND_WEBVIEW, new ArchitectView.CaptureScreenCallback() {
                                @Override
                                public void onScreenCaptured(final Bitmap screenCapture) {
                                    if ( ContextCompat.checkSelfPermission(SampleCamActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ) {
                                        SampleCamActivity.this.screenCapture = screenCapture;
                                        ActivityCompat.requestPermissions(SampleCamActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WIKITUDE_PERMISSIONS_REQUEST_EXTERNAL_STORAGE);
                                    } else {
                                        SampleCamActivity.this.saveScreenCaptureToExternalStorage(screenCapture);
                                    }
                                }
                            });
                            break;
                    }
                    */
                } catch (Exception e) {
                    Log.e(TAG, "onJSONObjectReceived: ", e);
                }
            }
        };
    }

    public ArchitectView.ArchitectWorldLoadedListener getWorldLoadedListener() {
        return new ArchitectView.ArchitectWorldLoadedListener() {
            @Override
            public void worldWasLoaded(String url) {
                Log.i(TAG, "worldWasLoaded: url: " + url);
            }

            @Override
            public void worldLoadFailed(int errorCode, String description, String failingUrl) {
                Log.e(TAG, "worldLoadFailed: url: " + failingUrl + " " + description);

	            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(thisActivity);
	            alertBuilder.setCancelable(false);
	            alertBuilder.setTitle("AR Error");
	            alertBuilder.setMessage("Unable to load the AR Experience:"+"\n"+description);
	            alertBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
		            @Override
		            public void onClick(DialogInterface dialog, int which) {
			            thisActivity.finish();
		            }
	            });

	            AlertDialog alert = alertBuilder.create();
	            alert.show();

            }
        };
    }

    /*
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {

        }
    }*/

    /**
     * call JacaScript in architectView
     * @param methodName
     * @param arguments
     */
    public void callJavaScript(final String methodName, final String[] arguments) {
        final StringBuilder argumentsString = new StringBuilder("");
        for (int i= 0; i<arguments.length; i++) {
            argumentsString.append(arguments[i]);
            if (i<arguments.length-1) {
                argumentsString.append(", ");
            }
        }

        if (this.architectView!=null) {
            final String js = ( methodName + "( " + argumentsString.toString() + " );" );
            this.architectView.callJavascript(js);
        }
    }

    protected CameraLifecycleListener getCameraLifecycleListener() {
        return null;
    }


    //Lifecycle Methods:

    @Override
    protected void onPostCreate( final Bundle savedInstanceState ) {
        super.onPostCreate( savedInstanceState );

        if ( this.architectView != null ) {

            try {
		            // call mandatory live-cycle method of architectView
		            this.architectView.onPostCreate();

                // load content via url in architectView, ensure '<script src="architect://architect.js"></script>' is part of this HTML file, have a look at wikitude.com's developer section for API references
                this.architectView.load(architectWorldURL);
                this.architectView.setCullingDistance(CULLING_DISTANCE_DEFAULT_METERS);

            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // call mandatory live-cycle method of architectView
        if ( this.architectView != null ) {
            this.architectView.onResume();

            // register accuracy listener in architectView, if set
            if (this.sensorAccuracyListener!=null) {
                this.architectView.registerSensorAccuracyChangeListener( this.sensorAccuracyListener );
            }
        }

        // tell locationProvider to resume, usually location is then (again) fetched, so the GPS indicator appears in status bar
        //if ( this.locationProvider != null ) {
        //    this.locationProvider.onResume();
        //}
    }

    @Override
    protected void onPause() {
        super.onPause();

        // call mandatory live-cycle method of architectView
        if ( this.architectView != null ) {
            this.architectView.onPause();

            // unregister accuracy listener in architectView, if set
            if ( this.sensorAccuracyListener != null ) {
                this.architectView.unregisterSensorAccuracyChangeListener( this.sensorAccuracyListener );
            }
        }

        // tell locationProvider to pause, usually location is then no longer fetched, so the GPS indicator disappears in status bar
        //if ( this.locationProvider != null ) {
        //    this.locationProvider.onPause();
        //}
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {

        // call mandatory live-cycle method of architectView
        if ( this.architectView != null ) {
            this.architectView.clearCache();
            this.architectView.onDestroy();
        }

	    super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if ( this.architectView != null ) {
            this.architectView.onLowMemory();
        }
    }


}
