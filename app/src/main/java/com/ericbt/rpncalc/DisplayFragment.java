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

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.mozilla.javascript.NativeArray;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ericbt.rpncalc.javascript.ExecuteMethodTask;
import com.ericbt.rpncalc.javascript.ExecuteMethodTaskParameters;
import com.ericbt.rpncalc.javascript.MethodMetadata;
import com.ericbt.rpncalc.javascript.MethodMetadata.MethodType;
import com.ericbt.rpncalc.javascript.SourceCode;
import com.ericbt.rpncalc.javascript.SourceCodeParseListener;
import com.ericbt.rpncalc.validators.NObjects;

public class DisplayFragment extends Fragment implements OnSharedPreferenceChangeListener, MethodExecutionListener, SourceCodeParseListener {
	private Context context;
	
	private String selectedItemText;
	
	private TextView topLine;
	
	public CharSequence getText() {
		return topLine.getText();
	}
	
	private ListView stack;
	
	private Stack<ResultWrapper> stackData = new Stack<>();
	
	private static final List<DisplayChangeListener> displayChangeListeners = new ArrayList<>();
	
	public Stack<ResultWrapper> getStackData() {
		return stackData;
	}
	
	public void setStackData(Stack<ResultWrapper> stackData) {
		this.stackData = stackData;
	}
	
	@SuppressWarnings("unchecked")
	public void restoreData() {
		try {
			String stackContents = Preferences.getStackContents();
			
			if (stackContents != null) {
				Object obj = SerializeDeserialize.deserialize(stackContents);
				
				if (obj instanceof Stack<?>) {
					stackData = (Stack<ResultWrapper>) obj;
				}

				topLine.setText(Preferences.getTopLine());
			}
		}
		catch (Throwable ex) {
			Log.e(StringLiterals.LogTag, "DisplayFragment.onCreate, exception deserializing stack data", ex);
		}

		updateStack();
	}
	
	private String[] getStackStringArray() {
		String[] stringArray = new String[stackData.size()];
		
		for (int i = 0; i < stackData.size(); i++) {
			stringArray[i] = ResultWrapper.getValueString(stackData.get(i));
		}
		
		return stringArray;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.display, container, false);
		
		topLine = view.findViewById(R.id.TopLine);
		
		topLine.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			
			public void afterTextChanged(Editable s) {
				broadcastDisplayChange();
			}
		});
		
		stack = view.findViewById(R.id.Stack);
		
		registerForContextMenu(stack);
		
		LinearLayout topLineLayout = view.findViewById(R.id.TopLineLayout);
		
		registerForContextMenu(topLineLayout);
		
		updateStack();
		
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);
		
		return view;
	}

	@Override
	public void onStart() {
		Log.i(StringLiterals.LogTag, "DisplayFragment.onStart");
		
		ExecuteMethodTask.listen(this);
		SourceCode.listen(this);
		
		topLine.setEnabled(true);
		stack.setEnabled(true);
		((StackArrayAdapter) stack.getAdapter()).setEnabled(true);

		super.onStart();
	}

	@Override
	public void onStop() {
		Log.i(StringLiterals.LogTag, "DisplayFragment.onStop");
		
		ExecuteMethodTask.unListen(this);
		SourceCode.unListen(this);
		
		Preferences.putTopLine(topLine.getText().toString());

		// Serialize the stack contents.
		String stackContents = SerializeDeserialize.serialize(stackData);
		Preferences.putStackContents(stackContents);
		
		// The call to updateStack corrects a race condition (observed on Moto G). There is code that updates the 
		// stack when the shared preferences change, but sometimes the notification is not sent.
		updateStack();
		
		super.onStop();
	}

	private String getClipboardText() {
		String clipboardText = "";
		
		ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
		
		if (clipboard.hasPrimaryClip()) {
			 ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);

			clipboardText = item.getText().toString();
		}
		
		return clipboardText;
	}
	
	private Double getClipboardDouble() {
		Double result = null;
		
		try {
			result = Double.parseDouble(getClipboardText().replace(String.valueOf(Preferences.getThousandsSeparatorCharacter()), "").replace(Preferences.getDecimalPointCharacter(), '.'));
		}
		catch (Throwable ex) {
		}
		
		return result;
	}
	
	private void pasteToClipboard(String text) {
		ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
		
		clipboard.setPrimaryClip(ClipData.newPlainText("simple text", text));
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
		if (view.getId() == R.id.TopLine || view.getId() == R.id.TopLineLayout) {
			getActivity().getMenuInflater().inflate(R.menu.top_line, menu);
			
			MenuItem pasteMenuItem = menu.findItem(R.id.PasteMenuItem);
			pasteMenuItem.setEnabled(getClipboardDouble() != null);
			
			MenuItem copyMenuItem = menu.findItem(R.id.CopyMenuItem);
			copyMenuItem.setEnabled(getText().length() > 0);
		}
		else {
			getActivity().getMenuInflater().inflate(R.menu.stack, menu);
	
			boolean atLeastOneObject = new NObjects(1).isValid(getStackData(), getText());
			
			MenuItem copyStackMenuItem = menu.findItem(R.id.CopyStackContentsMenuItem);
			copyStackMenuItem.setEnabled(atLeastOneObject);
			
			MenuItem displayMenuItem = menu.findItem(R.id.DisplayStackContentsMenuItem);
			displayMenuItem.setEnabled(atLeastOneObject);
			
			AdapterContextMenuInfo adapterContextMenuInfo = (AdapterContextMenuInfo) menuInfo;
			
			MenuItem copyStackItemMenuItem = menu.findItem(R.id.CopyStackItemMenuItem);
			copyStackItemMenuItem.setEnabled(adapterContextMenuInfo.position >= 0);
			
			if (adapterContextMenuInfo.position >= 0) {
				selectedItemText = getStackStringArray()[adapterContextMenuInfo.position];
			}
			else {
				selectedItemText = null;
			}
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		boolean result = false;
		
		if (item.getItemId() == R.id.DisplayStackContentsMenuItem) {
			push();
			
			String stackDataStringArrayString = SerializeDeserialize.serialize(getStackStringArray());
			
			if (stackDataStringArrayString != null) {
				Intent stackContents = new Intent(getActivity(), StackContentsActivity.class);
				stackContents.putExtra(StringLiterals.StackDataStringArrayString, stackDataStringArrayString);
				startActivity(stackContents);
			}
			else {
				Log.e(StringLiterals.LogTag, "DisplayFragment.onContextItemSelected: cannot serialize stack contents for display");
			}

			result = true;
		}
		else if (item.getItemId() == R.id.CopyStackItemMenuItem) {
			pasteToClipboard(selectedItemText);
		}
		else if (item.getItemId() == R.id.CopyStackContentsMenuItem) {
			copyStackContents();
		}
		else if (item.getItemId() == R.id.CopyMenuItem) {
			pasteToClipboard(getText().toString());
		}
		else if (item.getItemId() == R.id.PasteMenuItem) {
			topLine.setText(getClipboardText().replace(String.valueOf(Preferences.getThousandsSeparatorCharacter()), ""));
		}
		
		return result;
	}
	
	private void copyStackContents() {
		StringBuilder text = new StringBuilder();
		
		String[] stackStringArray = getStackStringArray();
		
		for (int i = 0; i < stackStringArray.length; i++) {
			text.append(stackStringArray[i]);
			
			if (i < stackStringArray.length - 1) {
				text.append(StringLiterals.NewLine);
			}
		}
		
		pasteToClipboard(text.toString());
	}

	public void push() throws NumberFormatException {
		String topLineText = topLine.getText().toString().replace(Preferences.getDecimalPointCharacter(), '.');
		
		if (!topLineText.isEmpty()) {
			try {
				Double value = Double.parseDouble(topLineText);
				topLine.setText("");
				
				stackData.push(new ResultWrapper(value));
		
				updateStack();
			}
			catch (NumberFormatException ex) {
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
				alertDialogBuilder.setTitle("Invalid Number");
				alertDialogBuilder.setMessage(String.format("Invalid Number: %s", topLine.getText()));
				alertDialogBuilder.setPositiveButton("OK", null);

				AlertDialog alertDialog = alertDialogBuilder.create();
				alertDialog.show();
				
				throw ex;
			}
		}
	}
	
	public void broadcastDisplayChange(DisplayChangeListener displayChangeListener) {
		displayChangeListener.displayChanged(stackData, topLine.getText().toString());
	}
	
	private void broadcastDisplayChange() {
		for (DisplayChangeListener displayChangeListener : displayChangeListeners) {
			broadcastDisplayChange(displayChangeListener);
		}
	}
	
	public void updateStack() {
		StackArrayAdapter stackArrayAdapter = new StackArrayAdapter(context, R.layout.stack_item, R.id.Value);
		stack.setAdapter(stackArrayAdapter);
		
		stackArrayAdapter.clear();
		
		List<ResultWrapper> dataInProperOrder = new ArrayList<>(stackData.size());
		
		for (int i = stackData.size() - 1; i >= 0; i--) {
			dataInProperOrder.add(stackData.elementAt(i));
		}
		
		if (!stackData.isEmpty()) {
			stack.setSelection(stackData.size() - 1);
		}
		
		stackArrayAdapter.addAll(dataInProperOrder);

		broadcastDisplayChange();
	}

	public void digitDecimalOrBackspace(char digitOrDecimalPoint) {
		if (digitOrDecimalPoint != '.' || topLine.getText().toString().indexOf('.') == -1) { 
			topLine.setText(String.format("%s%c", topLine.getText(), digitOrDecimalPoint));
		}
	}
	
	public void enter() {
		if (topLine.getText().length() > 0) {
			try {
				push();
			}
			catch (NumberFormatException ex) {
			}
		}
		else if (!stackData.isEmpty()) {
			stackData.push(stackData.peek());
			updateStack();
		}
	}
	
	public void add() {
		try {
			push();
			
			Double a = (Double) stackData.pop().getResult();
			Double b = (Double) stackData.pop().getResult();
			
			stackData.push(new ResultWrapper(b + a));
			
			updateStack();
			} catch (NumberFormatException ex) {
		}
	}
	
	public void subtract() {
		try {
			push();
			
			Double a = (Double) stackData.pop().getResult();
			Double b = (Double) stackData.pop().getResult();
			
			stackData.push(new ResultWrapper(b - a));
			
			updateStack();
		} catch (NumberFormatException ex) {
		}
	}
	
	public void multiply() {
		try {
			push();
			
			Double a = (Double) stackData.pop().getResult();
			Double b = (Double) stackData.pop().getResult();
			
			stackData.push(new ResultWrapper(b * a));
			
			updateStack();
		} catch (NumberFormatException ex) {
		}
	}
	
	public void divide() {
		try {
			push();
			
			Double a = (Double) stackData.pop().getResult();
			Double b = (Double) stackData.pop().getResult();
			
			Double result = b / a;
			
			if (result.isInfinite()) {
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
				alertDialogBuilder.setTitle("Division by Zero");
				alertDialogBuilder.setMessage("Cannot divide by zero.");
				alertDialogBuilder.setPositiveButton("OK", null);

				AlertDialog alertDialog = alertDialogBuilder.create();
				alertDialog.show();
				
				stackData.push(new ResultWrapper(b));
				stackData.push(new ResultWrapper(a));
			}
			else {
				stackData.push(new ResultWrapper(result));
			}
			
			updateStack();
		} catch (NumberFormatException ex) {
		}
}
	
	public void pi() {
		try {
			push();
			
			stackData.push(new ResultWrapper(Math.PI));
			updateStack();
		}
		catch (NumberFormatException ex) {
		}
	}
	
	public void drop() {
		try {
			push();
			
			stackData.pop();
			updateStack();
		}
		catch (NumberFormatException ex) {
		}
	}
	
	public void swap() {
		try {
			push();
			
			ResultWrapper a = stackData.pop();
			ResultWrapper b = stackData.pop();
			
			stackData.push(a);
			stackData.push(b);
			
			updateStack();
		}
		catch (NumberFormatException ex) {
		}
	}
	
	public void clearEntry() {
		topLine.setText("");
	}
	
	public void pushValue(ResultWrapper value) {
		push();
		
		stackData.push(value);
		
		updateStack();
	}
	
	public ResultWrapper popValue() {
		ResultWrapper result = stackData.pop();
		
		updateStack();
		
		return result;
	}
	
	public void clearAll() {
		topLine.setText("");
		stackData.clear();
		
		updateStack();
	}
	
	public void changeSign() {
		try {
			push();
			
			stackData.push(new ResultWrapper(-((Double) stackData.pop().getResult())));
			
			updateStack();
		}
		catch (NumberFormatException ex) {
		}
	}

	public void reciprocal() {
		try {
			push();
			
			stackData.push(new ResultWrapper(1.0 / (Double) stackData.pop().getResult()));
			
			updateStack();
		}
		catch (NumberFormatException ex) {
		}
	}

	public void sqrt() {
		try {
			push();
			
			stackData.push(new ResultWrapper(Math.sqrt((Double) stackData.pop().getResult())));
			
			updateStack();
		}
		catch (NumberFormatException ex) {
		}
	}

	public void square() {
		try {
			push();
			
			Double x = (Double) stackData.pop().getResult();
			
			stackData.push(new ResultWrapper(x * x));
			
			updateStack();
		}
		catch (NumberFormatException ex) {
		}
	}
	
	public void raise() {
		try {
			push();
			
			Double x = (Double) stackData.pop().getResult();
			Double y = (Double) stackData.pop().getResult();
			
			stackData.push(new ResultWrapper(Math.pow(y, x)));
			
			updateStack();
		}
		catch (NumberFormatException ex) {
		}
	}
	
	private double factorial(int n) {
		double factorial;
		
		if (n == 0 || n == 1) {
			factorial = 1.0;
		}
		else {
			factorial = n * factorial(n - 1);
		}
		
		return factorial;
	}
	
	public void factorial() {
		try {
			push();
			
			Double x = (Double) stackData.peek().getResult();
			
			stackData.pop();
			stackData.push(new ResultWrapper(factorial(x.intValue())));
			
			updateStack();
		}
		catch (NumberFormatException ex) {
		}
	}
	
	public void percent() {
		try {
			push();
			
			Double x = (Double) stackData.pop().getResult();
			
			stackData.push(new ResultWrapper(x / 100.0));
			
			updateStack();
		}
		catch (NumberFormatException ex) {
		}
	}
	
	public void scientificNotation() {
		try {
			push();
			
			Double x = (Double) stackData.pop().getResult();
			Double y = (Double) stackData.pop().getResult();
			
			stackData.push(new ResultWrapper(y * Math.pow(10.0, x)));
			
			updateStack();
		}
		catch (NumberFormatException ex) {
		}
	}
	
	public void fixedPoint() {
		try {
			push();
			
			Double x = (Double) stackData.pop().getResult();
			
			Globals.setDigitsPastDecimalPoint(x.intValue());
			
			updateStack();
		}
		catch (NumberFormatException ex) {
		}
	}
	
	public void floatingPoint() {
		try {
			push();
			
			Globals.setDigitsPastDecimalPoint(-1);
			
			updateStack();
		}
		catch (NumberFormatException ex) {
		}
	}
	
	private void stackToArray(int numberOfItems) {
		List<ResultWrapper> arrayItems = new ArrayList<>(numberOfItems);

		for (int i = numberOfItems - 1; i >= 0; i--) {
			arrayItems.add(0, stackData.pop());
		}

		StringBuilder arrayText = new StringBuilder();
		
		for (int i = 0; i < arrayItems.size(); i++) {
			String itemText = ResultWrapper.getValueString(arrayItems.get(i));

			if (i < arrayItems.size() - 1) {
				itemText += ", ";
			}

			arrayText.append(itemText);
		}
		
		Object[] objectArray = new Object[arrayItems.size()];
		
		for (int i = 0; i < objectArray.length; i++) {
			objectArray[i] = arrayItems.get(i).getResult();
		}
		
		NativeArray nativeArray = new NativeArray(objectArray);
		ResultWrapper value = new ResultWrapper(nativeArray);
		value.setResultString(String.format("[%s]", arrayText));
		
		pushValue(value);
		
		updateStack();

		fixupArray();
	}
	
	private void fixupArray() {
		// Now fix up the array to work in the context of the javascript.
		MethodMetadata methodMetadata = new MethodMetadata(null, "javascriptArray");
		methodMetadata.setMethodType(MethodType.GlobalFunction);

		List<String> arguments = new ArrayList<>();
		arguments.add("array");
		
		methodMetadata.setArguments(arguments);
		
		new ExecuteMethodTask().execute(new ExecuteMethodTaskParameters(methodMetadata, this));
	}
	
	public void stackToArray() {
		try {
			push();

			stackToArray(stackData.size());
		}
		catch (NumberFormatException ex) {
		}
	}
	
	public void stackNToArray() {
		try {
			push();

			int numberOfItems = (int) ((Double) stackData.pop().getResult()).doubleValue();

			stackToArray(numberOfItems);
		}
		catch (NumberFormatException ex) {
		}
	}
	
	public void arrayToStack() {
		NativeArray nativeArray = (NativeArray) stackData.pop().getResult();
		
		for (int i = 0; i < nativeArray.getLength(); i++) {
			pushValue(new ResultWrapper(nativeArray.get(i)));
		}
		
		updateStack();
	}
	
	public void backspace() {
		String text = topLine.getText().toString();
		
		if (!text.isEmpty()) {
			topLine.setText(text.substring(0, text.length() - 1));
		}
	}

	public static void listen(DisplayChangeListener listener) {
		displayChangeListeners.add(listener);
	}

	public static void unListen(DisplayChangeListener listener) {
		displayChangeListeners.remove(listener);
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Log.i(StringLiterals.LogTag, String.format("DisplayFragment.onSharedPreferenceChanged %s", key));
		
		updateStack();
	}

	private void enableDisableForStatus(boolean enabled) {
		topLine.setEnabled(enabled);

		stack.setEnabled(enabled);
		((StackArrayAdapter) stack.getAdapter()).setEnabled(enabled);
		
		if (enabled) {
			registerForContextMenu(stack);
		}
		else {
			unregisterForContextMenu(stack);
		}
	}

	@Override
	public void methodExecutionStatus(ExecutionStatus executionStatus) {
		enableDisableForStatus(executionStatus == ExecutionStatus.Completed);
	}

	@Override
	public void sourceCodeChanged(SourceCodeStatus sourceCodeStatus) {
		enableDisableForStatus(sourceCodeStatus == SourceCodeStatus.ParsingCompleted);
	}
	
	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		
		this.context = context;
	}

}
