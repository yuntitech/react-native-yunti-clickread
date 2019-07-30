
package com.yunti.clickread;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;

public class RNYtClickreadModule extends ReactContextBaseJavaModule {

  private final ReactApplicationContext reactContext;

  public RNYtClickreadModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @Override
  public String getName() {
    return "RNYtClickread";
  }
}