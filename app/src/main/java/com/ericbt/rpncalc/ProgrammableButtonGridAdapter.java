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

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import com.ericbt.rpncalc.javascript.ExecuteMethodTask;
import com.ericbt.rpncalc.javascript.ExecuteMethodTaskParameters;
import com.ericbt.rpncalc.javascript.MethodMetadata;
import com.ericbt.rpncalc.validators.NObjects;

public class ProgrammableButtonGridAdapter extends BaseAdapter {
	private final Context context;
	private final List<MethodMetadata> allMethods;
	private DisplayFragment displayFragment;

	public void setDisplayFragment(DisplayFragment displayFragment) {
		this.displayFragment = displayFragment;
	}

	public ProgrammableButtonGridAdapter(Context context, List<MethodMetadata> allMethods) {
		this.context = context;
		this.allMethods = allMethods;
	}

	public int getCount() {
		return allMethods.size();
	}

	public Object getItem(int index) {
		return allMethods.get(index);
	}

	public long getItemId(int index) {
		return 0;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		// If convertView is not null, we recycle it (but repopulate it). If it's null we inflate a new view and populate it.
		View gridView = (convertView == null) ? inflater.inflate(R.layout.programmable_button, null) : convertView;

		// set value into button.
		Button programmableButton = (Button) gridView.findViewById(R.id.ProgrammableButton);
		
		MethodMetadata methodMetadata = (MethodMetadata) getItem(position);
		
		String argumentList = (!methodMetadata.getArguments().isEmpty() && !methodMetadata.isButtonTextCustom()) ? String.format("(%s)", methodMetadata.getArgumentList()) : "";
		
		programmableButton.setText(String.format("%s%s", methodMetadata.getButtonText(), argumentList));
		programmableButton.setTag(methodMetadata);
		
		NObjects validator = new NObjects(methodMetadata.getArguments().size());
		programmableButton.setEnabled(validator.isValid(displayFragment.getStackData(), displayFragment.getText()));
		
		programmableButton.setOnClickListener(v -> {
			KeyFeedback.giveFeedback(v);
			displayFragment.push();

			new ExecuteMethodTask().execute(new ExecuteMethodTaskParameters((MethodMetadata) v.getTag(), displayFragment));
		});
		
		((Activity) context).registerForContextMenu(programmableButton);
		
		return gridView;
	}
	
}
