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

package com.ericbt.rpncalc.javascript;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;

public class CustomContext extends Context {
    public final static int JAVASCRIPT_VERSION = Context.VERSION_ES6;

    public CustomContext(ContextFactory contextFactory) {
        super(contextFactory);

        setLanguageVersion(JAVASCRIPT_VERSION);
    }

    // One must disable Xml Secure Parsing in order to view stack contents.

    @Override
    public boolean hasFeature(int featureIndex) {
        if (featureIndex == Context.FEATURE_ENABLE_XML_SECURE_PARSING) {
            return false;
        }

        return super.hasFeature(featureIndex);
    }
}
