package com.ejvindh.tododiary;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;

public class Raw_db_Activity extends AppCompatActivity {
	private String raw_db_content;
	private EditText raw_db_text;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_raw_db);
		// Show the Up button in the action bar.
		setupActionBar();
		raw_db_text = (EditText)findViewById(R.id.raw_db_text);
		Bundle bundle = getIntent().getExtras();
        boolean raw_db_ro = true;
        int raw_db_position = 1;
		if (bundle != null) {
            raw_db_content = bundle.getString("raw_db_text");
            raw_db_ro = bundle.getBoolean("raw_db_ro");
            raw_db_position = bundle.getInt("raw_db_position");
        }
		raw_db_text.setText(raw_db_content);
		raw_db_text.setSelection(raw_db_position);
		raw_db_text.setFocusable(!raw_db_ro);
		if (raw_db_ro) {
			raw_db_text.setBackgroundResource(R.drawable.read_only);
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
		if(getSupportActionBar() != null){
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}


}
