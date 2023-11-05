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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class StackArrayAdapter extends ArrayAdapter<ResultWrapper> {
	private final Context context;
	private final int resource;
	private final int textViewResourceId;
	private boolean enabled = true;

	public StackArrayAdapter(Context context, int resource, int textViewResourceId) {
		super(context, textViewResourceId);
		
		this.context = context;
		this.resource = resource;
		this.textViewResourceId = textViewResourceId;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            
        	convertView = infalInflater.inflate(resource, null);
        }
        
        final ResultWrapper value = getItem(position);

        TextView textView = (TextView) convertView.findViewById(textViewResourceId);
        
        String valueString = ResultWrapper.getValueString(value);
    	textView.setText(valueString);
    	
    	textView.setEnabled(isEnabled(position));
    	
        return convertView;
	}

	@Override
	public boolean isEnabled(int position) {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		
		notifyDataSetChanged();
	}

	@Override
	public ResultWrapper getItem(int position) {
		// Reverse order of stack items for display.
		return super.getItem(super.getCount() - position - 1);
	}

}
