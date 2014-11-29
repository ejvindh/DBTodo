package com.ejvindh.tododiary;

import com.ejvindh.tododiary.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

public class JumbleActivity extends Activity {
	private String jumblecontent;
	private boolean jumble_ro;
	private EditText jumbletext;
	private Intent intent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_jumble);
		// Show the Up button in the action bar.
		setupActionBar();
		jumbletext = (EditText)findViewById(R.id.jumbletext);
		intent = getIntent();
		jumblecontent = intent.getExtras().getString("jumbletext");
		jumble_ro = intent.getExtras().getBoolean("jumble_ro");
		jumbletext.setText(jumblecontent);
		jumbletext.setFocusable(!jumble_ro);
		if (jumble_ro) {
			jumbletext.setBackgroundDrawable(getResources().getDrawable(R.drawable.read_only));
			jumbletext.getBackground().setAlpha(51);
		}
	}
	
	
	@Override
	public void onBackPressed() {
		returncontent();
    }

	private void returncontent() {
    	String newjumblecontent = jumbletext.getText().toString();
    	if (newjumblecontent.compareTo(jumblecontent) == 0) {
    		Intent returnIntent = new Intent();
    		setResult(RESULT_CANCELED, returnIntent);
        	finish();
    	} else {
    		Intent returnIntent = new Intent();
            returnIntent.putExtra("returningresult", newjumblecontent);
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
