package com.instabug.library;

import android.content.Context;

public class Instabug {
  public static class Builder {
    public Builder(Context context, String apiKey) {

    }

    public Builder setInvocationEvent(int event) {
      return this;
    }

    public Builder setInAppMessagingState(int state) {
      return this;
    }

    public void build() {

    }
  }
}
