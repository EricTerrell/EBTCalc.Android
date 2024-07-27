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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.ericbt.rpncalc.javascript.CategoryClassMetadata;
import com.ericbt.rpncalc.javascript.CategoryClassMetadataArrayAdapter;
import com.ericbt.rpncalc.javascript.CategoryClassMetadataComparitor;
import com.ericbt.rpncalc.javascript.ClassMetadata;
import com.ericbt.rpncalc.javascript.ExecuteMethodTask;
import com.ericbt.rpncalc.javascript.MethodMetadata;
import com.ericbt.rpncalc.javascript.ParseSourceCodeTask;
import com.ericbt.rpncalc.javascript.SourceCode;
import com.ericbt.rpncalc.javascript.SourceCodeParseListener;
import com.ericbt.rpncalc.validators.NObjects;
import com.ericbt.rpncalc.validators.Validator;
	
public class ProgrammableKeypadFragment extends Fragment implements SourceCodeParseListener, DisplayChangeListener, MethodExecutionListener {
	private GridView buttonGrid;
	private Spinner selectCategory;
	private LinearLayout enclosingLayout;
	private DisplayFragment displayFragment;
	private static CategoryClassMetadata selectedCategoryClass;
	private int sourceCodeVersion = -1;
	
	public void setDisplayFragment(DisplayFragment displayFragment) {
		this.displayFragment = displayFragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.programmable_keypad, container, false);
		
		enclosingLayout = (LinearLayout) view.findViewById(R.id.EnclosingLayout);
		buttonGrid = (GridView) view.findViewById(R.id.ButtonGrid);
		selectCategory = (Spinner) view.findViewById(R.id.SelectClass);
		
		return view;
	}
	
	@Override
	public void onStart() {
		Log.i(StringLiterals.LogTag, "ProgrammableKeypadFragment.onStart begin");
		
		DisplayFragment.listen(this);
		SourceCode.listen(this);
		ExecuteMethodTask.listen(this);

		if (ParseSourceCodeTask.isSourceCodeChanging()) {
			visitButtons(buttonGrid, displayFragment.getStackData(), displayFragment.getText(), new DisableButtons());
		}
		else if (sourceCodeVersion != ParseSourceCodeTask.getSourceCodeVersion()) {
			sourceCodeChanged(SourceCodeStatus.ParsingCompleted);
		}

		// When a lengthy calculation is running, and the user goes to another app, and returns to this app,
		// need to enable the UI.
		MiscUtils.enableDisableView(enclosingLayout, true);
		selectCategory.setEnabled(true);
		
		super.onStart();

		Log.i(StringLiterals.LogTag, "ProgrammableKeypadFragment.onStart end");
	}

	@Override
	public void onStop() {
		Log.i(StringLiterals.LogTag, "ProgrammableKeypadFragment.onStop");

		DisplayFragment.unListen(this);
		SourceCode.unListen(this);
		ExecuteMethodTask.unListen(this);
		
		super.onStop();
	}

	private void updateUI(List<ClassMetadata> allClassMetadata) {
		updateButtons();
		updateSpinner(allClassMetadata);
	}
	
	public void updateUI() {
		updateUI(SourceCode.filterClassMetadata(false));
		
		sourceCodeVersion = ParseSourceCodeTask.getSourceCodeVersion();
	}

	private void updateSpinner(List<ClassMetadata> allClassMetadata) {
		CategoryClassMetadata currentCategoryClassMetadata = (CategoryClassMetadata) selectCategory.getSelectedItem();
		
		if (currentCategoryClassMetadata == null) {
			currentCategoryClassMetadata = selectedCategoryClass;
		}
		
		List<CategoryClassMetadata> categoryClassMetadata = getCategoryClassMetadata(allClassMetadata);
		
		CategoryClassMetadata[] sortedCategoryClassMetadata = categoryClassMetadata.toArray(new CategoryClassMetadata[categoryClassMetadata.size()]);
		Arrays.sort(sortedCategoryClassMetadata, new CategoryClassMetadataComparitor());
		
		CategoryClassMetadataArrayAdapter spinnerAdapter = new CategoryClassMetadataArrayAdapter(getActivity(), R.layout.class_spinner_item, R.id.ClassName, sortedCategoryClassMetadata);
		spinnerAdapter.setDropDownViewResource(R.layout.class_spinner_dropdown_item);
		selectCategory.setAdapter(spinnerAdapter);
		
		selectCategory.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				selectedCategoryClass = (CategoryClassMetadata) parent.getItemAtPosition(position);
				
				updateButtons();
			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		
		CategoryClassMetadata selectedCategoryClassMetadata = (CategoryClassMetadata) selectCategory.getSelectedItem();

		if (currentCategoryClassMetadata != null && selectedCategoryClassMetadata != null && !currentCategoryClassMetadata.getCategoryName().equals(selectedCategoryClassMetadata.getCategoryName())) {
			for (int position = 0; position < spinnerAdapter.getCount(); position++) {
				CategoryClassMetadata ccm = spinnerAdapter.getItem(position);
				
				if (ccm.getCategoryName().equals(currentCategoryClassMetadata.getCategoryName())) {
					selectCategory.setSelection(position);
					break;
				}
			}
		}
	}

	private List<CategoryClassMetadata> getCategoryClassMetadata(List<ClassMetadata> allClassMetadata) {
		List<CategoryClassMetadata> categoryMetadataList = new ArrayList<>();
		
		Map<String, List<ClassMetadata>> classMetadataForCategory = new HashMap<>();
		
		// Go through all the class metadata and organize it by the proper name.
		for (ClassMetadata classMetadata : allClassMetadata) {
			String key = classMetadata.toString();
			
			List<ClassMetadata> metadataList = classMetadataForCategory.get(key);
			
			if (metadataList == null) {
				metadataList = new ArrayList<>();
				classMetadataForCategory.put(key, metadataList);
			}
			
			metadataList.add(classMetadata);
		}
		
		for (String categoryName : classMetadataForCategory.keySet()) {
			categoryMetadataList.add(new CategoryClassMetadata(categoryName, classMetadataForCategory.get(categoryName)));
		}
		
		return categoryMetadataList;
	}

	private void updateButtons() {
		CategoryClassMetadata selectedCategory = (CategoryClassMetadata) selectCategory.getSelectedItem();
		
		if (selectedCategory != null) {
			if (!selectedCategory.getClassMetadata().isEmpty()) {
				List<MethodMetadata> methodMetadata = new ArrayList<>();
				
				for (ClassMetadata selectedClassMetadata : selectedCategory.getClassMetadata()) {
					methodMetadata.addAll(selectedClassMetadata.filterMethodMetadata(false));
				}
				
				ProgrammableButtonGridAdapter programmableButtonGridAdapter = new ProgrammableButtonGridAdapter(getActivity(), methodMetadata);
				programmableButtonGridAdapter.setDisplayFragment(displayFragment);
				
				buttonGrid.setAdapter(programmableButtonGridAdapter);
			}
			else {
				// There was a syntax error, javascript could not be parsed, need to clear the current button grid.
				ProgrammableButtonGridAdapter programmableButtonGridAdapter = new ProgrammableButtonGridAdapter(getActivity(), new ArrayList<MethodMetadata>());
				programmableButtonGridAdapter.setDisplayFragment(displayFragment);
				
				buttonGrid.setAdapter(programmableButtonGridAdapter);
			}
		}
	}
	
	public void sourceCodeChanged(SourceCodeStatus sourceCodeStatus) {
		if (sourceCodeStatus == SourceCodeStatus.ParsingCompleted) {
			updateUI(SourceCode.filterClassMetadata(false));
		}
	}

	public void displayChanged(Stack<ResultWrapper> stackData, CharSequence text) {
		visitButtons(buttonGrid, stackData, text, new EnableDisableButtons());
	}

	private void visitButtons(ViewGroup viewGroup, Stack<ResultWrapper> stackData, CharSequence text, ButtonVisitor buttonVisitor) {
		for (int i = 0; i < viewGroup.getChildCount(); i++) {
			View childView = viewGroup.getChildAt(i);
			
			if (childView instanceof Button) {
				buttonVisitor.Visit(stackData, text, (Button) childView);
			}
			
			if (childView instanceof ViewGroup) {
				visitButtons((ViewGroup) childView, stackData, text, buttonVisitor);
			}
		}
	}
	
	private static class EnableDisableButtons implements ButtonVisitor {
		public void Visit(Stack<ResultWrapper> stackData, CharSequence text, Button button) {
			if (button.getTag() instanceof MethodMetadata) {
				Validator validator = new NObjects(((MethodMetadata) button.getTag()).getArguments().size());
				
				button.setEnabled(validator.isValid(stackData, text));
			}
		}
	}
	
	private static class DisableButtons implements ButtonVisitor {
		public void Visit(Stack<ResultWrapper> stackData, CharSequence text, Button button) {
			if (button.getTag() instanceof MethodMetadata) {
				button.setEnabled(false);
			}
		}
	}
	
	@Override
	public void methodExecutionStatus(ExecutionStatus executionStatus) {
		MiscUtils.enableDisableView(enclosingLayout, executionStatus == ExecutionStatus.Completed);

		if (executionStatus == ExecutionStatus.Completed) {
			visitButtons(buttonGrid, displayFragment.getStackData(), displayFragment.getText(), new EnableDisableButtons());
		}
	}
}
