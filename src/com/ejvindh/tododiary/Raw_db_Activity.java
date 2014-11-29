package com.ejvindh.tododiary;

import com.ejvindh.tododiary.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

public class Raw_db_Activity extends Activity {
	private String raw_db_content;
	private boolean raw_db_ro;
	private int raw_db_position;
	private EditText raw_db_text;
	private Intent intent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_raw_db);
		// Show the Up button in the action bar.
		setupActionBar();
		raw_db_text = (EditText)findViewById(R.id.raw_db_text);
		intent = getIntent();
		raw_db_content = intent.getExtras().getString("raw_db_text");
		raw_db_ro = intent.getExtras().getBoolean("raw_db_ro");
		raw_db_position = intent.getExtras().getInt("raw_db_position");
		raw_db_text.setText(raw_db_content);
		raw_db_text.setSelection(raw_db_position);
		raw_db_text.setFocusable(!raw_db_ro);
		if (raw_db_ro) {
			raw_db_text.setBackgroundDrawable(getResources().getDrawable(R.drawable.read_only));
			raw_db_text.getBackground().setAlpha(51);
		}
	}
	
	
	@Override
	public void onBackPressed() {
		returncontent();
    }

	private void returncontent() {
    	String new_raw_db_content = raw_db_text.getText().toString();
    	if (new_raw_db_content.compareTo(raw_db_content) == 0) {
    		Intent returnIntent = new Intent();
    		setResult(RESULT_CANCELED, returnIntent);
        	finish();
    	} else {
    		Intent returnIntent = new Intent();
            returnIntent.putExtra("returningresult", new_raw_db_content);
            setResult(RESULT_OK,returnIntent);
            finish();
    	}
	}


	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.jumble, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			returncontent();
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
