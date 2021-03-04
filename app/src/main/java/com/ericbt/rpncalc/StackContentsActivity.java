package com.ericbt.rpncalc;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class StackContentsActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.stack_contents);
		
		setTitle(String.format(getString(R.string.stack_contents_title), getString(R.string.app_name)));
		
		TableLayout tableLayout = (TableLayout) findViewById(R.id.Stack);
		
		String[] stackDataStringArray = (String[]) SerializeDeserialize.deserialize(getIntent().getExtras().getString(StringLiterals.StackDataStringArrayString));

		for (String item : stackDataStringArray) {
			TableRow tableRow = (TableRow) getLayoutInflater().inflate(R.layout.stack_contents_table_row, null);
			
			TextView textView = (TextView) tableRow.findViewById(R.id.Value);
			textView.setText(item);

			tableLayout.addView(tableRow);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean result = false;
		
		if (item.getItemId() == android.R.id.home) {
			finish();
			
			result = true;
		}
		
		return result;
	}
}
