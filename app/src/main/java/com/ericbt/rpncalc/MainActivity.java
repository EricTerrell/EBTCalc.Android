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

import java.util.Stack;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.ericbt.rpncalc.javascript.ExecuteMethodTask;
import com.ericbt.rpncalc.javascript.MethodMetadata;
import com.ericbt.rpncalc.javascript.PromptActivity;
import com.ericbt.rpncalc.javascript.SourceCode;
import com.ericbt.rpncalc.javascript.SourceCodeParseListener;

public class MainActivity extends Activity implements MethodExecutionListener, SourceCodeParseListener {
    private final static int ACCEPT_LICENSE_TERMS = 1001;

    private Menu optionsMenu;
    private ProgrammableKeypadFragment programmableKeypadFragment;
    private DisplayFragment displayFragment;
    private LinearLayout enclosingView;
    private boolean created;

    private static final int ENTER_STRING = 1;
    private static final int RUN_METHODS = 2;
    private static final int SETTINGS = 3;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(StringLiterals.LogTag, "MainActivity.onCreate begin");

        super.onCreate(savedInstanceState);

        Preferences.setDefaultValues();

        if (getString(R.string.main_activity_force_portrait).equals(StringLiterals.True)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        Globals.setDigitsPastDecimalPoint(Preferences.getDigitsPastDecimalPoint());

        if (getIntent().getBooleanExtra("EXIT", false)) {
            finish();

            ExitApplication.exit();
        } else {
            setContentView(R.layout.main);

            enclosingView = findViewById(R.id.EnclosingView);

            setTitle(String.format(getString(R.string.main_title), getString(R.string.app_name)));

            // Hide the right programmable keypad view if there is insufficient width.
            if (!wideEnoughForTwoColumnDisplay()) {
                LinearLayout programmableKeypadView = findViewById(R.id.ProgrammableKeypadRight);

                programmableKeypadView.setVisibility(View.GONE);
                enclosingView.setWeightSum(0.5f);
            } else {
                LinearLayout leftProgrammableKeypadView = findViewById(R.id.LeftProgrammableKeyboard);
                leftProgrammableKeypadView.setVisibility(View.GONE);
            }

            getDisplayFragment().restoreData();

            // Prompt user to accept license terms if they have not been previously accepted.
            if (!Preferences.getUserAcceptedTerms()) {
                final Intent licenseTermsIntent = new Intent(this, LicenseTermsActivity.class);
                licenseTermsIntent.putExtra(StringLiterals.AllowCancel, false);

                startActivityForResult(licenseTermsIntent, ACCEPT_LICENSE_TERMS);
            } else {
                loadSourceCode();
            }
        }

        // import source code from another app
        // Get intent, action and MIME type
        final Intent intent = getIntent();
        final String action = intent.getAction();
        final String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                SourceCode.setUserCode(intent.getStringExtra(Intent.EXTRA_TEXT), this);
            }
        }

        created = true;

        Log.i(StringLiterals.LogTag, "MainActivity.onCreate end");
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.i(StringLiterals.LogTag, "MainActivity.onResume");

        if (created) {
            created = false;

            MiscUtils.logViewDimensions(enclosingView, MainActivity.this.getClass().getName());

            if (Globals.getInitialLaunch()) {
                Globals.setInitialLaunch(false);
            }
        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        Log.i(StringLiterals.LogTag, "MainActivity.onAttachFragment");

        if (fragment instanceof ProgrammableKeypadFragment) {
            programmableKeypadFragment = (ProgrammableKeypadFragment) fragment;
        } else if (fragment instanceof DisplayFragment) {
            displayFragment = (DisplayFragment) fragment;
        }

        if (programmableKeypadFragment != null && displayFragment != null) {
            programmableKeypadFragment.setDisplayFragment(displayFragment);
        }

        super.onAttachFragment(fragment);
    }

    private void loadSourceCode() {
        // Can now load the source code since we have permission to read
        Log.i(StringLiterals.LogTag, "Starting parse");

        SourceCode.loadBuiltInCode(MainActivity.this);
        SourceCode.loadUserCode(MainActivity.this);

        SourceCode.setUserCode(SourceCode.getUserCode(), this);
    }

    @Override
    protected void onStop() {
        Log.i(StringLiterals.LogTag, "MainActivity.onStop");

        if (!PromptActivity.isActive() && !LicenseTermsActivity.isActive()) {
            // Ensure that any pending source code parses or method executions do not happen after a rotation.
            BackgroundTasks.cancel(false);
            enableOptionsMenuItems(true);
        }

        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);

        optionsMenu = menu;

        enableOptionsMenuItems(false);

        ExecuteMethodTask.listen(this);
        SourceCode.listen(this);

        return true;
    }

    public DisplayFragment getDisplayFragment() {
        return displayFragment;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result = false;

        if (item.getItemId() == R.id.SettingsMenuItem) {
            startActivityForResult(new Intent(this, SettingsActivity.class), SETTINGS);
            result = true;
        } else if (item.getItemId() == R.id.EditMenuItem) {
            Intent intent = new Intent(MainActivity.this, EditMethodsActivity.class);
            startActivity(intent);
            result = true;
        } else if (item.getItemId() == R.id.AboutMenuItem) {
            Intent intent = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(intent);
            result = true;
        } else if (item.getItemId() == R.id.HelpMenuItem) {
            String url = getString(R.string.online_help_url);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } else if (item.getItemId() == R.id.AboutEBTDesktopMenuItem) {
            String url = getString(R.string.ebtcalc_desktop_url);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } else if (item.getItemId() == R.id.share) {
            shareCode();

            result = true;
        }

        return result;
    }

    private void shareCode() {
        final Intent sharingIntent = new Intent(Intent.ACTION_SEND);

        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.code));
        sharingIntent.putExtra(Intent.EXTRA_TEXT, SourceCode.getUserCode());
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_via)));
    }

    public void onEnterString() {
        startActivityForResult(new Intent(MainActivity.this, EnterStringActivity.class), ENTER_STRING);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case ACCEPT_LICENSE_TERMS: {
                loadSourceCode();
            }
            break;

            case ENTER_STRING: {
                if (resultCode == RESULT_OK) {
                    String stringText = data.getExtras().getString(StringLiterals.StringText);
                    displayFragment.pushValue(new ResultWrapper(stringText));
                }
            }
            break;

            case RUN_METHODS: {
                if (resultCode == RESULT_OK) {
                    String stackDataString = data.getExtras().getString(StringLiterals.StackData);

                    Object obj = SerializeDeserialize.deserialize(stackDataString);

                    if (obj instanceof Stack<?>) {
                        @SuppressWarnings("unchecked")
                        Stack<ResultWrapper> stackData = (Stack<ResultWrapper>) obj;

                        displayFragment.setStackData(stackData);
                        displayFragment.updateStack();
                    }
                }
            }
            break;

            case SETTINGS: {
                if (resultCode == SettingsActivity.CHANGED_COLUMN_MODE) {
                    this.recreate();
                }
            }
            break;
        }
    }

    public void onRunProgrammableMethods() {
        getDisplayFragment().push();

        Intent intent = new Intent(MainActivity.this, RunMethodsActivity.class);
        intent.putExtra(StringLiterals.StackData, SerializeDeserialize.serialize(displayFragment.getStackData()));
        startActivityForResult(intent, RUN_METHODS);
    }

    public boolean wideEnoughForTwoColumnDisplay() {
        Point screenSize = new Point();
        getWindowManager().getDefaultDisplay().getSize(screenSize);

        int minWidthForTwoColumnDisplay = Integer.parseInt(getString(R.string.min_width_for_two_column_display));

        boolean wideEnough = (screenSize.x >= minWidthForTwoColumnDisplay);

        // Force one column mode if we're in portrait and the user wants one column mode.
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT && Preferences.getForce1ColumnValue()) {
            wideEnough = false;
        }

        return wideEnough;
    }

    private void enableOptionsMenuItems(boolean enabled) {
        if (optionsMenu != null) {
            for (int i = 0; i < optionsMenu.size(); i++) {
                optionsMenu.getItem(i).setEnabled(enabled);
            }
        }
    }

    @Override
    public void methodExecutionStatus(ExecutionStatus executionStatus) {
        enableOptionsMenuItems(executionStatus == ExecutionStatus.Completed);
    }

    @Override
    public void sourceCodeChanged(SourceCodeStatus sourceCodeStatus) {
        if (sourceCodeStatus == SourceCodeStatus.ParsingCompleted) {
            enableOptionsMenuItems(true);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        final Button button = (Button) view;

        final Intent editMethods = new Intent(this, EditMethodsActivity.class);
        editMethods.putExtra(StringLiterals.SourcePosition, ((MethodMetadata) button.getTag()).getPosition());
        startActivity(editMethods);

        super.onCreateContextMenu(menu, view, menuInfo);
    }
}