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

package com.ericbt.rpncalc.javascript;

import java.text.Collator;
import java.util.Comparator;

import com.ericbt.rpncalc.StringLiterals;

public class MethodMetadataComparitor implements Comparator<MethodMetadata> {
	private Collator collator = Collator.getInstance();

	@Override
	public int compare(MethodMetadata methodMetadata1, MethodMetadata methodMetadata2) {
		String name1 = methodMetadata1.getMethodName();
		String name2 = methodMetadata2.getMethodName();

		if (name1.startsWith(StringLiterals.PrivatePrefix)) {
			name1 = name1.substring(StringLiterals.PrivatePrefix.length());
		}
		
		if (name2.startsWith(StringLiterals.PrivatePrefix)) {
			name2 = name2.substring(StringLiterals.PrivatePrefix.length());
		}
		
		return collator.compare(name1, name2);
	}
}

