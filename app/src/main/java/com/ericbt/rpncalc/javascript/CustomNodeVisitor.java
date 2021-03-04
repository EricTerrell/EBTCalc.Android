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

import java.util.ArrayList;
import java.util.List;

import org.mozilla.javascript.ast.Assignment;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.PropertyGet;

import com.ericbt.rpncalc.StringLiterals;
import com.ericbt.rpncalc.javascript.MethodMetadata.MethodType;


public class CustomNodeVisitor implements NodeVisitor {
	private List<String> niladicConstructors = new ArrayList<>();
	public List<String> getNiladicConstructors() {
		return niladicConstructors;
	}

	private List<String> nonNiladicConstructors = new ArrayList<>();
	
	public List<String> getNonNiladicConstructors() {
		return nonNiladicConstructors;
	}
	
	public List<String> getConstructors() {
		List<String> allConstructors = new ArrayList<>(niladicConstructors.size() + nonNiladicConstructors.size());
		
		allConstructors.addAll(niladicConstructors);
		allConstructors.addAll(nonNiladicConstructors);
		
		return allConstructors;
	}
	
	private List<MethodMetadata> methods = new ArrayList<>();
	
	public List<MethodMetadata> getMethods() {
		return methods;
	}

	public boolean visit(AstNode node) {
		if (node instanceof FunctionNode) {
			FunctionNode functionNode = (FunctionNode) node;
			
			if (functionNode.getFunctionName() != null) {
				if (functionNode.getParamCount() == 0) {
					niladicConstructors.add(functionNode.getFunctionName().getString());
				}
				else {
					nonNiladicConstructors.add(functionNode.getFunctionName().getString());
				}
			}
		}
		else if (node instanceof Assignment) {
			MethodMetadata methodMetadata = getMethodInstance((Assignment) node);
			
			if (methodMetadata != null) {
				methods.add(methodMetadata);
			}
			else {
				methodMetadata = getMethodClass((Assignment) node);
				
				if (methodMetadata != null) {
					methods.add(methodMetadata);
				}
			}
		}

		return true;
	}
	
	private MethodMetadata getMethodInstance(Assignment assignment) {
		MethodMetadata methodMetadata = null;
		
		if (assignment.getRight() instanceof FunctionNode) {
			FunctionNode functionNode = (FunctionNode) assignment.getRight();

			if (assignment.getLeft() instanceof PropertyGet) {
				PropertyGet left = (PropertyGet) assignment.getLeft();
				
				if (left.getRight() instanceof Name) {
					Name methodName = (Name) left.getRight();
					
					if (!methodName.getIdentifier().equals("toString") && left.getLeft() instanceof PropertyGet) {
						PropertyGet leftleft = (PropertyGet) left.getLeft();
						
						if (leftleft.getRight() instanceof Name) {
							Name prototypeName = (Name) leftleft.getRight();
							
							if (prototypeName.getString().equals("prototype")) {
								if (leftleft.getLeft() instanceof Name) {
									Name className = (Name) leftleft.getLeft();
									List<String> arguments = getArguments(functionNode);
									boolean hidden = isHidden(methodName);
									
									methodMetadata = new MethodMetadata(className.getIdentifier(), methodName.getIdentifier(), arguments, 
											                            MethodType.InstanceMethod, leftleft.getAbsolutePosition(), hidden);
								}
							}
						}
					}
				}
			}
		}
		
		return methodMetadata;
	}

	private MethodMetadata getMethodClass(Assignment assignment) {
		MethodMetadata methodMetadata = null;
		
		if (assignment.getRight() instanceof FunctionNode) {
			FunctionNode functionNode = (FunctionNode) assignment.getRight();

			if (assignment.getLeft() instanceof PropertyGet) {
				PropertyGet left = (PropertyGet) assignment.getLeft();
				
				if (left.getRight() instanceof Name) {
					Name methodName = (Name) left.getRight();
					
					if (left.getLeft() instanceof Name) {
						Name className = (Name) left.getLeft();
						List<String> arguments = getArguments(functionNode);
						boolean hidden = isHidden(methodName);
						
						methodMetadata = new MethodMetadata(className.getIdentifier(), methodName.getIdentifier(), arguments, 
								                            MethodType.ClassMethod, className.getAbsolutePosition(), hidden);
					}
				}
					
			}
		}
		
		return methodMetadata;
	}
	
	private boolean isHidden(Name methodName) {
		return methodName.getIdentifier().startsWith(StringLiterals.PrivatePrefix);		
	}
	
	private List<String> getArguments(FunctionNode functionNode) {
		List<String> arguments = new ArrayList<>();
		
		for (AstNode parameterNode : functionNode.getParams()) {
			arguments.add(parameterNode.getString());
		}
		
		return arguments;
	}
}
