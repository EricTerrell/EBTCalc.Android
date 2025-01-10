/*
  EBTCalc
  (C) Copyright 2025, Eric Bergman-Terrell
  
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

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Date;

import android.os.Environment;
import android.util.Log;

/**
 * @author Eric Bergman-Terrell
 *
 * Log uncaught exceptions to a log file if enabled.
 */
public class CustomUncaughtExceptionHandler implements UncaughtExceptionHandler {
	@Override
	public void uncaughtException(Thread thread, Throwable tr) {
		String logMessage = String.format("CustomUncaughtExceptionHandler.uncaughtException: Thread %d Message %s", thread.getId(), tr.getMessage());

		Log.e(StringLiterals.LogTag, logMessage);
		
		tr.printStackTrace();
		
		if (Preferences.isUncaughtExceptionLoggingEnabled()) {
			PrintWriter printWriter = null;

			FileOutputStream fileOutputStream = null;
			
			try {
				String logPath = String.format("%s/%s", Environment.getExternalStorageDirectory().getPath(), StringLiterals.LogFileName);
				printWriter = new PrintWriter(new FileWriter(logPath, true));

				logMessage = String.format("%s\r\n\r\nThread: %d\r\n\r\nMessage:\r\n\r\n%s\r\n\r\nStack Trace:\r\n\r\n%s",
						  				   new Date(), 
						  				   thread.getId(), 
						  				   tr.getMessage(), 
						  				   Log.getStackTraceString(tr));
				
				Log.e(StringLiterals.LogTag, logMessage);

				printWriter.print(logMessage);
				printWriter.print("\n\n---------------------------------------------------------------------------\n\n");
			}
			catch (Throwable tr2) {
				Log.e(StringLiterals.LogTag, "CustomUncaughtExceptionHandler.uncaughtException", tr2);
			}
			finally {
				if (printWriter != null) {
					printWriter.close();
				}
				
				if (fileOutputStream != null) {
					try {
						fileOutputStream.close();
					} catch(Exception ex) {
						Log.e(StringLiterals.LogTag, "CustomUncaughtExceptionHandler.uncaughtException", ex);
					}
				}
			}
		}
	}
}
