package com.ejvindh.tododiary;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;

public class JumbleActivity extends AppCompatActivity {
	private String jumblecontent;
	private EditText jumbletext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_jumble);
		// Show the Up button in the action bar.
		setupActionBar();
		jumbletext = (EditText)findViewById(R.id.jumbletext);
		Bundle bundle = getIntent().getExtras();
		boolean jumble_ro = true;
		if (bundle != null) {
			jumblecontent = bundle.getString("jumbletext");
			jumble_ro = bundle.getBoolean("jumble_ro");
		}
		jumbletext.setText(jumblecontent);
		jumbletext.setFocusable(!jumble_ro);
		if (jumble_ro) {
			jumbletext.setBackgroundResource(R.drawable.read_only);
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

	private void setupActionBar() {
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
	}
}
