package com.brave.wikitudebridge;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.Toast;

import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.wikitude.architect.ArchitectView;
import com.wikitude.common.permission.PermissionManager;

import java.io.File;
import java.util.Arrays;

public class WikitudeModule extends ReactContextBaseJavaModule {

  public WikitudeModule(ReactApplicationContext reactContext) {
    super(reactContext);
  }

  @Override
  public String getName() {
    return "WikitudeModule";
  }

  /*@ReactMethod
  public void alert(String message) {
    Toast.makeText(getReactApplicationContext(), message, Toast.LENGTH_LONG).show();
  }*/

  @ReactMethod
  public void startAR(String architectWorldURL, boolean hasGeolocation, boolean hasImageRecognition, boolean hasInstantTracking)
  {
    final Activity currentActivity = getCurrentActivity();

	  final Intent intent = new Intent(currentActivity, WikitudePrecheck.class);

	  intent.putExtra(WikitudeActivity.EXTRAS_KEY_AR_URL, architectWorldURL);
	  intent.putExtra(WikitudeActivity.EXTRAS_KEY_HAS_GEO, hasGeolocation);
	  intent.putExtra(WikitudeActivity.EXTRAS_KEY_HAS_IR, hasImageRecognition);
	  intent.putExtra(WikitudeActivity.EXTRAS_KEY_HAS_INSTANT, hasInstantTracking);

	  //launch activity
	  currentActivity.startActivity(intent);
  }

}