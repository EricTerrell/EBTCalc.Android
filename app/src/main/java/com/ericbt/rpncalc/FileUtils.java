/*
  EBTCalc
  (C) Copyright 2015, Eric Bergman-Terrell
  
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class FileUtils {
	public static String readFile(String filePath) throws Exception {
		StringBuilder text = new StringBuilder();
		
		FileInputStream fileInputStream = null;
		InputStreamReader inputStreamReader = null;
		BufferedReader bufferedReader = null;
		
		try {
			fileInputStream = new FileInputStream(filePath);
            inputStreamReader = new InputStreamReader(fileInputStream);
            bufferedReader = new BufferedReader(inputStreamReader);

            boolean finished = false;
            
            do {
            	String line = bufferedReader.readLine();
            	
            	if (line == null) {
            		finished = true;
            	}
            	else {
            		text.append(line);
            		text.append(StringLiterals.NewLine);
            	}
            } while (!finished);
		}
		finally {
			if (fileInputStream != null) {
				fileInputStream.close();
			}
				
			if (inputStreamReader != null) {
				inputStreamReader.close();
			}
				
			if (bufferedReader != null) {
				bufferedReader.close();
			}
		}

		return text.toString();
	}
	
	public static void writeFile(String filePath, String text) throws Exception {
		FileOutputStream fileOutputStream = null;
		OutputStreamWriter outputStreamWriter = null;
		BufferedWriter bufferedWriter = null;
		
		try {
			fileOutputStream = new FileOutputStream(filePath);
            outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            bufferedWriter = new BufferedWriter(outputStreamWriter);

        	bufferedWriter.write(text);
        	bufferedWriter.flush();
		}
		finally {
			if (bufferedWriter != null) {
				bufferedWriter.close();
			}

			if (outputStreamWriter != null) {
				outputStreamWriter.close();
			}
				
			if (fileOutputStream != null) {
				fileOutputStream.close();
			}
		}
	}
}
