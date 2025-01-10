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

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.ericbt.rpncalc.BackgroundTasks;
import com.ericbt.rpncalc.Globals;
import com.ericbt.rpncalc.MethodExecutionListener;
import com.ericbt.rpncalc.MiscUtils;
import com.ericbt.rpncalc.Preferences;
import com.ericbt.rpncalc.ResultWrapper;
import com.ericbt.rpncalc.RuntimeErrorActivity;
import com.ericbt.rpncalc.SerializeDeserialize;
import com.ericbt.rpncalc.StringLiterals;
import com.ericbt.rpncalc.javascript.MethodMetadata.MethodType;

public class ExecuteMethodTask extends AsyncTask<ExecuteMethodTaskParameters, Void, ExecuteMethodTaskResult> {
    private ExecuteMethodTaskParameters parameters;

    private static boolean executing = false;

    public synchronized static boolean isExecuting() {
        return ExecuteMethodTask.executing;
    }

    private synchronized static void setExecuting(boolean executing) {
        ExecuteMethodTask.executing = executing;
    }

    private static final List<MethodExecutionListener> listeners = new ArrayList<>();

    public static void listen(MethodExecutionListener listener) {
        listeners.add(listener);
    }

    public static void unListen(MethodExecutionListener listener) {
        listeners.remove(listener);
    }

    private static void notifyListeners(MethodExecutionListener.ExecutionStatus executionStatus) {
        for (MethodExecutionListener listener : listeners) {
            listener.methodExecutionStatus(executionStatus);
        }
    }

    @Override
    protected void onPreExecute() {
        setExecuting(true);

        BackgroundTasks.add(this);

        notifyListeners(MethodExecutionListener.ExecutionStatus.Started);
    }

    @Override
    protected ExecuteMethodTaskResult doInBackground(ExecuteMethodTaskParameters... params) {
        parameters = params[0];

        parameters.setExecuteMethodTask(this);
        return executeMethod(params[0]);
    }

    @Override
    protected void onPostExecute(ExecuteMethodTaskResult result) {
        Log.i(StringLiterals.LogTag, "ExecuteMethodTask.onPostExecute");

        BackgroundTasks.remove(this);

        if (result.getException() == null) {
            // Pop the arguments since the method was successfully called.
            for (int i = 0; i < result.getMethodMetadata().getArguments().size(); i++) {
                parameters.getDisplayFragment().popValue();
            }

            if (!result.getResults().isEmpty()) {
                // Push the results.
                for (ResultWrapper resultWrapper : result.getResults()) {
                    parameters.getDisplayFragment().pushValue(resultWrapper);
                }

                parameters.getDisplayFragment().updateStack();
            }
        } else {
            String details = null, message;
            int sourcePosition = result.getMethodMetadata().getPosition();

            if (result.getException() instanceof EvaluatorException) {
                details = ((EvaluatorException) result.getException()).details();
                message = (result.getException()).getMessage();
                int lineNumber = ((EvaluatorException) result.getException()).lineNumber();
                int columnNumber = ((EvaluatorException) result.getException()).columnNumber();
                sourcePosition = MiscUtils.getLinePosition(lineNumber, SourceCode.getUserCode());

                if (columnNumber > 0) {
                    sourcePosition += columnNumber - 1;
                }
            } else if (result.getException() instanceof EcmaError) {
                details = ((EcmaError) result.getException()).details();
                message = (result.getException()).getMessage();
                int lineNumber = ((EcmaError) result.getException()).lineNumber();
                int columnNumber = ((EcmaError) result.getException()).columnNumber();
                sourcePosition = MiscUtils.getLinePosition(lineNumber, SourceCode.getUserCode());

                if (columnNumber > 0) {
                    sourcePosition += columnNumber - 1;
                }
            } else {
                message = result.getException().getMessage();
            }

            String text = String.format("Runtime error in method %s.%s:",
                    result.getMethodMetadata().getClassName(), result.getMethodMetadata().getMethodName());

            if (details != null && !details.isEmpty()) {
                text += StringLiterals.NewLine + StringLiterals.NewLine + details;
            }

            if (message != null && !message.isEmpty()) {
                text += StringLiterals.NewLine + StringLiterals.NewLine + message;
            }

            Intent intent = new Intent(parameters.getDisplayFragment().getActivity(), RuntimeErrorActivity.class);
            intent.putExtra(StringLiterals.Text, text);
            intent.putExtra(StringLiterals.SourcePosition, sourcePosition);

            parameters.getDisplayFragment().getActivity().startActivity(intent);
        }

        notifyListeners(MethodExecutionListener.ExecutionStatus.Completed);
        setExecuting(false);
    }

    @Override
    protected void onCancelled(ExecuteMethodTaskResult result) {
        Log.i(StringLiterals.LogTag, "ExecuteMethodTask.onCancelled");

        BackgroundTasks.remove(this);
        setExecuting(false);
    }

    @SuppressLint("DefaultLocale")
    private static ExecuteMethodTaskResult executeMethod(ExecuteMethodTaskParameters executeMethodTaskParameters) {
        long startTime = System.currentTimeMillis();

        ExecuteMethodTaskResult result = new ExecuteMethodTaskResult();

        result.setMethodMetadata(executeMethodTaskParameters.getMethodMetadata());

        try {
            final CustomContextFactory customContextFactory = new CustomContextFactory();

            final Context context = customContextFactory.enterContext();

            final Scriptable scope = context.initStandardObjects();

            deserializeGlobalObjectIfNecessary();

            scope.put("Globals", scope, Globals.getJavascriptGlobals());
            scope.put("GlobalsModified", scope, false);

            @SuppressWarnings("unchecked")
            Stack<ResultWrapper> copyOfStack = (Stack<ResultWrapper>) executeMethodTaskParameters.getDisplayFragment().getStackData().clone();

            String arguments = "";

            for (int i = executeMethodTaskParameters.getMethodMetadata().getArguments().size() - 1; i >= 0; i--) {
                String argName = String.format("arg%d", i + 1);

                scope.put(argName, scope, copyOfStack.pop().getResult());

                if (i != executeMethodTaskParameters.getMethodMetadata().getArguments().size() - 1) {
                    arguments = "," + arguments;
                }

                arguments = argName + arguments;
            }

            String methodCall;

            if (executeMethodTaskParameters.getMethodMetadata().getMethodType() == MethodType.GlobalFunction) {
                methodCall = String.format("; %s(%s)",
                        executeMethodTaskParameters.getMethodMetadata().getMethodName(),
                        arguments);
            } else {
                String classReference = executeMethodTaskParameters.getMethodMetadata().getMethodType() == MethodType.ClassMethod ?
                        String.format("%s", executeMethodTaskParameters.getMethodMetadata().getClassName()) :
                        String.format("new %s()", executeMethodTaskParameters.getMethodMetadata().getClassName());

                methodCall = String.format("; %s.%s(%s)",
                        classReference,
                        executeMethodTaskParameters.getMethodMetadata().getMethodName(),
                        arguments);
            }

            String fullSourceCode = String.format("%s%s", SourceCode.getMergedCode(), methodCall);

            customContextFactory.start(Preferences.getMethodTimeout(), executeMethodTaskParameters.getExecuteMethodTask());

            Object resultObj = context.evaluateString(scope, fullSourceCode, "<custom_methods>", 1, null);

            Log.i(StringLiterals.LogTag, Context.toString(resultObj));

            // Push the method result (if any).
            if (!(resultObj instanceof Undefined)) {
                result.getResults().push(ResultWrapper.createUsingContext(resultObj));
            }

            Globals.setJavascriptGlobals((NativeObject) scope.get("Globals", scope));

            serializeGlobalObjectIfNecessary(scope);
        } catch (EvaluatorException | EcmaError ex) {
            Log.e(StringLiterals.LogTag, String.format("%s %s Line %d Column: %d", ex.details(), ex.getMessage(), ex.lineNumber(), ex.columnNumber()));
            result.setException(ex);
        } catch (Throwable ex) {
            Log.e(StringLiterals.LogTag, String.format("Error: %s", ex.getMessage()));
            result.setException(ex);
        } finally {
            Context.exit();
        }

        long elapsedMilliseconds = System.currentTimeMillis() - startTime;
        Log.i(StringLiterals.LogTag, String.format("ExecuteMethodTask.executeMethod: %d ms", elapsedMilliseconds));

        return result;
    }

    private static void deserializeGlobalObjectIfNecessary() {
        if (Globals.getJavascriptGlobals() == null) {
            try {
                String javascriptGlobalsString = Preferences.getJavascriptGlobals();

                Object obj = SerializeDeserialize.deserialize(javascriptGlobalsString);
                Log.i(StringLiterals.LogTag, "ExecuteMethodTask.deserializeGlobalObjectIfNecessary: deserialized javascriptGlobalsString");

                if (obj instanceof NativeObject) {
                    Globals.setJavascriptGlobals((NativeObject) obj);
                } else {
                    Log.e(StringLiterals.LogTag, "ExecuteMethodTask.deserializeGlobalObjectIfNecessary: deserialized object is not a NativeObject");
                }
            } catch (Throwable ex) {
                Log.e(StringLiterals.LogTag, "ExecuteMethodTask.deserializeGlobalObjectIfNecessary - exception restoring javascript globals object", ex);
            }

            if (Globals.getJavascriptGlobals() == null) {
                Globals.setJavascriptGlobals(new NativeObject());
            }
        }
    }

    private static void serializeGlobalObjectIfNecessary(Scriptable scope) {
        Object globalsModified = scope.get("GlobalsModified", scope);

        if (globalsModified instanceof Boolean) {
            Boolean modified = (Boolean) globalsModified;

            if (modified) {
                try {
                    String javascriptGlobalsString = SerializeDeserialize.serialize(Globals.getJavascriptGlobals());

                    if (javascriptGlobalsString != null) {
                        Log.i(StringLiterals.LogTag, String.format("Serialized javascriptGlobals: %s %d", javascriptGlobalsString, javascriptGlobalsString.length()));

                        Preferences.putJavascriptGlobals(javascriptGlobalsString);
                        Log.i(StringLiterals.LogTag, "Saved javascriptGlobals");
                    } else {
                        Log.e(StringLiterals.LogTag, "MainActivity.onStop: cannot serialize javascriptGlobals");
                    }

                    Preferences.putDigitsPastDecimalPoint(Globals.getDigitsPastDecimalPoint());
                    Log.i(StringLiterals.LogTag, String.format("Saved digits past decimal point (%d)", Globals.getDigitsPastDecimalPoint()));
                } catch (Throwable ex) {
                    Log.e(StringLiterals.LogTag, "Exception saving javascript globals object", ex);
                }
            }
        }

    }
}
