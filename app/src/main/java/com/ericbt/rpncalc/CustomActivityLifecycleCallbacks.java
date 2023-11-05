/*
  EBTCalc
  (C) Copyright 2023, Eric Bergman-Terrell

  This file is part of EBTCalc.

    EBTCalc is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    EBTCalc is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with EBTCalc.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.ericbt.rpncalc;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

public class CustomActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {
    private Activity currentActivity;

    public Activity getCurrentActivity() { return currentActivity; }

    private void statusMessage(String state) {
        final String activityText = currentActivity != null ? currentActivity.toString() : "null";

        Log.i(StringLiterals.LogTag,
                String.format("CustomActivityLifecycleCallbacks: state: %s currentActivity: %s",
                        state, activityText));
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        currentActivity = activity;

        statusMessage("onActivityCreated");
    }

    @Override
    public void onActivityStarted(Activity activity) {
        currentActivity = activity;

        statusMessage("onActivityStarted");
    }

    @Override
    public void onActivityResumed(Activity activity) {
        currentActivity = activity;

        statusMessage("onActivityResumed");
    }

    @Override
    public void onActivityPaused(Activity activity) {
        statusMessage("onActivityPaused");
    }

    @Override
    public void onActivityStopped(Activity activity) {
        statusMessage("onActivityPaused");
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        statusMessage("onActivitySaveInstanceState");
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        statusMessage("onActivityDestroyed");
    }
}
