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

package com.ericbt.rpncalc.javascript;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ericbt.rpncalc.R;

public class ClassMetadataArrayAdapter extends ArrayAdapter<ClassMetadata> {
	private final List<ClassMetadata> classMetadata;
	private final Context context;
	private final boolean useDisplayClassName;
	
	public ClassMetadataArrayAdapter(Context context, int resource, int textViewResourceId, List<ClassMetadata> classMetadata, boolean useDisplayClassName) {
		super(context, resource, textViewResourceId, classMetadata);
		
		this.classMetadata = classMetadata;
		this.context = context;
		this.useDisplayClassName = useDisplayClassName;
	}

	public ClassMetadataArrayAdapter(Context context, int resource, int textViewResourceId, List<ClassMetadata> classMetadata) {
		this(context, resource, textViewResourceId, classMetadata, true);
	}

	@Override
	public ClassMetadata getItem(int position) {
		return classMetadata.get(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        final ClassMetadata item = getItem(position);

        LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        if (convertView == null) {
        	convertView = infalInflater.inflate(R.layout.class_spinner_item, null);
        }
        
        TextView className = convertView.findViewById(R.id.ClassName);
        className.setText(useDisplayClassName ? item.getDisplayClassName() : item.getClassName());
        
        return convertView;
	}

}
