/*
  EBTCalc
  (C) Copyright 2022, Eric Bergman-Terrell
  
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

import java.util.Stack;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.ericbt.rpncalc.javascript.ExecuteMethodTask;
import com.ericbt.rpncalc.javascript.SourceCode;
import com.ericbt.rpncalc.javascript.SourceCodeParseListener;
import com.ericbt.rpncalc.validators.CanFix;
import com.ericbt.rpncalc.validators.CanPush;
import com.ericbt.rpncalc.validators.Factorial;
import com.ericbt.rpncalc.validators.HasText;
import com.ericbt.rpncalc.validators.NObjects;
import com.ericbt.rpncalc.validators.NoDecimalPoint;
import com.ericbt.rpncalc.validators.OneDouble;
import com.ericbt.rpncalc.validators.ProgrammableMethods;
import com.ericbt.rpncalc.validators.StackNToArray;
import com.ericbt.rpncalc.validators.TextOrStackItem;
import com.ericbt.rpncalc.validators.TopItemIs1DArray;
import com.ericbt.rpncalc.validators.TwoDoubles;
import com.ericbt.rpncalc.validators.UnconditionallyValid;
import com.ericbt.rpncalc.validators.Validator;

public class MainKeypadFragment extends Fragment implements DisplayChangeListener, MethodExecutionListener, SourceCodeParseListener {
	private LinearLayout enclosingLayout;
	private Button programmableMethods;
	private View view;
	private boolean created;
	private MainActivity mainActivity;
	
	@Override
	public void onStart() {
		Log.i(StringLiterals.LogTag, "MainKeyboardFragment.onStart");
		
		mainActivity = (MainActivity) getActivity();
		
		ExecuteMethodTask.listen(this);
		DisplayFragment.listen(this);
		SourceCode.listen(this);
		
		mainActivity.getDisplayFragment().broadcastDisplayChange(this);

		super.onStart();
	}

	@Override
	public void onStop() {
		Log.i(StringLiterals.LogTag, "MainKeyboardFragment.onStop");
		
		ExecuteMethodTask.unListen(this);
		DisplayFragment.unListen(this);
		SourceCode.unListen(this);

		super.onStop();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.main_keypad, container, false);
	
		enclosingLayout = view.findViewById(R.id.EnclosingLayout);

		setupDecimalPointButton(view);
		setupDigitButtons(view);
		
		final Button enter = view.findViewById(R.id.Enter);
		
		enter.setText(Html.fromHtml("E<br>n<br>t<br>e<br>r"));
		
		enter.setOnClickListener(v -> {
			SoundEffect.playKeyClick();
			mainActivity.getDisplayFragment().enter();
		});

		enter.setTag(new NObjects(1));
		
		final Button drop = view.findViewById(R.id.Drop);
		
		drop.setOnClickListener(v -> {
			SoundEffect.playKeyClick();
			mainActivity.getDisplayFragment().drop();
		});

		drop.setTag(new NObjects(1));
		
		final Button pi = view.findViewById(R.id.Pi);
		pi.setTag(new CanPush());
		
		pi.setOnClickListener(v -> {
			SoundEffect.playKeyClick();
			mainActivity.getDisplayFragment().pi();
		});

		final Button swap = view.findViewById(R.id.Swap);
		
		swap.setOnClickListener(v -> {
			SoundEffect.playKeyClick();
			mainActivity.getDisplayFragment().swap();
		});

		swap.setTag(new NObjects(2));
		
		final Button clearEntry = view.findViewById(R.id.ClearEntry);
		
		clearEntry.setOnClickListener(v -> {
			SoundEffect.playKeyClick();
			mainActivity.getDisplayFragment().clearEntry();
		});
		
		clearEntry.setTag(new HasText());

		final Button clearAll = view.findViewById(R.id.ClearAll);
		
		clearAll.setOnClickListener(v -> {
			SoundEffect.playKeyClick();
			mainActivity.getDisplayFragment().clearAll();
		});

		clearAll.setTag(new TextOrStackItem());
		
		final Button changeSign = view.findViewById(R.id.ChangeSign);
		
		changeSign.setOnClickListener(v -> {
			SoundEffect.playKeyClick();
			mainActivity.getDisplayFragment().changeSign();
		});
		
		changeSign.setTag(new OneDouble());

		final Button recip = view.findViewById(R.id.Recip);
		
		recip.setOnClickListener(v -> {
			SoundEffect.playKeyClick();
			mainActivity.getDisplayFragment().reciprocal();
		});

		recip.setTag(new OneDouble());

		final Button sqrt = view.findViewById(R.id.Sqrt);
		
		sqrt.setOnClickListener(v -> {
			SoundEffect.playKeyClick();
			mainActivity.getDisplayFragment().sqrt();
		});

		sqrt.setTag(new OneDouble());

		final Button square = view.findViewById(R.id.Square);
		
		square.setOnClickListener(v -> {
			SoundEffect.playKeyClick();
			mainActivity.getDisplayFragment().square();
		});

		square.setTag(new OneDouble());

		final Button backspace = view.findViewById(R.id.Backspace);
		
		backspace.setOnClickListener(v -> {
			SoundEffect.playKeyClick();
			mainActivity.getDisplayFragment().backspace();
		});

		backspace.setTag(new HasText());
		
		programmableMethods = view.findViewById(R.id.ProgrammableMethods);
		programmableMethods.setTag(new ProgrammableMethods(mainActivity));
		
		programmableMethods.setOnClickListener(v -> {
			SoundEffect.playKeyClick();
			mainActivity.onRunProgrammableMethods();
		});
		
		final Button enterString = view.findViewById(R.id.EnterString);
		enterString.setTag(new CanPush());
		
		enterString.setOnClickListener(v -> {
			SoundEffect.playKeyClick();
			mainActivity.onEnterString();
		});
		
		final Button raise = view.findViewById(R.id.Raise);
		
		raise.setOnClickListener(v -> {
			SoundEffect.playKeyClick();
			mainActivity.getDisplayFragment().raise();
		});
		
		raise.setTag(new TwoDoubles());
		
		final Button sciNot = view.findViewById(R.id.SciNot);
		
		sciNot.setOnClickListener(v -> {
			SoundEffect.playKeyClick();
			mainActivity.getDisplayFragment().scientificNotation();
		});
		
		sciNot.setTag(new TwoDoubles());
		
		final Button percent = view.findViewById(R.id.Percent);
		
		percent.setOnClickListener(v -> {
			SoundEffect.playKeyClick();
			mainActivity.getDisplayFragment().percent();
		});
		
		percent.setTag(new OneDouble());
		
		final Button fixedPoint = view.findViewById(R.id.Fix);
		
		fixedPoint.setOnClickListener(v -> {
			SoundEffect.playKeyClick();
			mainActivity.getDisplayFragment().fixedPoint();
		});
		
		fixedPoint.setTag(new CanFix());
		
		final Button floatingPoint = view.findViewById(R.id.Float);
		floatingPoint.setTag(new CanPush());
			
		floatingPoint.setOnClickListener(v -> {
			SoundEffect.playKeyClick();
			mainActivity.getDisplayFragment().floatingPoint();
		});
		
		final Button factorial = view.findViewById(R.id.Factorial);
		factorial.setTag(new Factorial());
		
		factorial.setOnClickListener(v -> {
			SoundEffect.playKeyClick();
			mainActivity.getDisplayFragment().factorial();
		});
		
		final Button decimalPoint = (Button) view.findViewById(R.id.DecimalPoint);
		decimalPoint.setTag(new NoDecimalPoint());
		
		setupFourFunctionButtons(view);
		setupStackArrayButtons(view);
		
		created = true;
		
		return view;
	}

	private void setupDecimalPointButton(View view) {
		Button decimalPointButton = (Button) view.findViewById(R.id.DecimalPoint);
		
		decimalPointButton.setText(String.valueOf(Preferences.getDecimalPointCharacter()));
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		if (created) {
			created = false;
			
			MiscUtils.logViewDimensions(view, MainKeypadFragment.this.getClass().getName());
		}
	}

	private void setupDigitButtons(View view) {
		int[] buttonIDs = new int[] { R.id.Digit0, R.id.Digit1, R.id.Digit2, R.id.Digit3, R.id.Digit4, R.id.Digit5, R.id.Digit6, R.id.Digit7, R.id.Digit8, R.id.Digit9, R.id.DecimalPoint };

        for (int buttonID : buttonIDs) {
            Button button = (Button) view.findViewById(buttonID);

            button.setOnClickListener(button1 -> {
				SoundEffect.playKeyClick();
				mainActivity.getDisplayFragment().digitDecimalOrBackspace(((Button) button1).getText().charAt(0));
			});

            if (buttonID != R.id.DecimalPoint) {
                button.setTag(new UnconditionallyValid());
            }
        }
	}
	
	private void setupFourFunctionButtons(View view) {
		final Button add = (Button) view.findViewById(R.id.Add);
		
		add.setOnClickListener(v -> {
			SoundEffect.playKeyClick();
			mainActivity.getDisplayFragment().add();
		});

		add.setTag(new TwoDoubles());
		
		final Button subtract = (Button) view.findViewById(R.id.Subtract);
		
		subtract.setOnClickListener(v -> {
			SoundEffect.playKeyClick();
			mainActivity.getDisplayFragment().subtract();
		});

		subtract.setTag(new TwoDoubles());

		final Button multiply = (Button) view.findViewById(R.id.Multiply);
		
		multiply.setOnClickListener(v -> {
			SoundEffect.playKeyClick();
			mainActivity.getDisplayFragment().multiply();
		});

		multiply.setTag(new TwoDoubles());

		final Button divide = (Button) view.findViewById(R.id.Divide);
		
		divide.setOnClickListener(v -> {
			SoundEffect.playKeyClick();
			mainActivity.getDisplayFragment().divide();
		});
		
		divide.setTag(new TwoDoubles());
	}

	private void setupStackArrayButtons(View view) {
		Button stackToArray = (Button) view.findViewById(R.id.StackToArray);
		stackToArray.setTag(new UnconditionallyValid());
		
		stackToArray.setOnClickListener(v -> {
			SoundEffect.playKeyClick();
			mainActivity.getDisplayFragment().stackToArray();
		});
		
		Button arrayToStack = (Button) view.findViewById(R.id.ArrayToStack);
		arrayToStack.setTag(new TopItemIs1DArray());
		
		arrayToStack.setOnClickListener(v -> {
			SoundEffect.playKeyClick();
			mainActivity.getDisplayFragment().arrayToStack();
		});
		
		Button stackNToArray = (Button) view.findViewById(R.id.StackNToArray);
		stackNToArray.setTag(new StackNToArray());
		
		stackNToArray.setOnClickListener(v -> {
			SoundEffect.playKeyClick();
			mainActivity.getDisplayFragment().stackNToArray();
		});
	}
	
	public void displayChanged(Stack<ResultWrapper> stackData, CharSequence text) {
		visitButtons((ViewGroup) getView(), stackData, text, new EnableDisableButton());
	}
	
	private void visitButtons(ViewGroup viewGroup, Stack<ResultWrapper> stackData, CharSequence text, ButtonVisitor buttonVisitor) {
		if (viewGroup != null) {
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
	}

	private class EnableDisableButton implements ButtonVisitor {
		public void Visit(Stack<ResultWrapper> stackData, CharSequence text, Button button) {
			if (button.getTag() instanceof Validator) {
				Validator validator = (Validator) button.getTag();
				
				if (button.getId() == R.id.ProgrammableMethods) {
					button.setEnabled(!mainActivity.wideEnoughForTwoColumnDisplay());
				}
				else {
					button.setEnabled(validator.isValid(stackData, text));
				}
			}
		}
	}

	@Override
	public void methodExecutionStatus(ExecutionStatus executionStatus) {
		if (executionStatus == ExecutionStatus.Started) {
			MiscUtils.enableDisableView(enclosingLayout, false);
		}
		else {
			visitButtons((ViewGroup) getView(), mainActivity.getDisplayFragment().getStackData(), mainActivity.getDisplayFragment().getText(), new EnableDisableButton());
		}
	}
	
	@Override
	public void sourceCodeChanged(SourceCodeStatus sourceCodeStatus) {
		if (programmableMethods != null) {
			programmableMethods.setEnabled(sourceCodeStatus == SourceCodeStatus.ParsingCompleted && !mainActivity.wideEnoughForTwoColumnDisplay());
		}
		
		if (sourceCodeStatus == SourceCodeStatus.ParsingStarted) {
			MiscUtils.enableDisableView(enclosingLayout, false);
		}
		else {
			visitButtons((ViewGroup) getView(), mainActivity.getDisplayFragment().getStackData(), mainActivity.getDisplayFragment().getText(), new EnableDisableButton());
		}
	}
	
}
