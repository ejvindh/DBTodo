package com.ejvindh.tododiary;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import static com.ejvindh.tododiary.R.*;


public class BrowseFiles extends ListActivity {
	private List<String> path = null;
	private TextView myPath;
	private Button choose_button;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(layout.activity_browse_files);
		// Show the Up button in the action bar.
		Bundle bundle = getIntent().getExtras();
		String directory = "";
		if (bundle != null) {
			directory = bundle.getString("local_backupfiles_path");
		}
		choose_button = findViewById(id.button1);


		myPath = findViewById(id.path);
		getDir(directory);
	}

	private void getDir(String dirPath) {
		myPath.setText(String.format("%s%s", getString(string.path), dirPath));
		List<String> item = new ArrayList<>();
		path = new ArrayList<>();
		File f = new File(dirPath);
		final String f_path = f.getPath();
		File[] files = f.listFiles();
		String root = "/";
		if(!dirPath.equals(root)) {
			item.add(root);
			path.add(root);
			item.add("../");
			path.add(f.getParent());
		}
		for (File file : files) {
			path.add(file.getPath());
			if (file.isDirectory()) item.add(file.getName() + "/");
			else item.add(file.getName());
		}
		ArrayAdapter<String> fileList =
				new ArrayAdapter<>(this, layout.row, item);
		setListAdapter(fileList);
		choose_button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent=new Intent();
				intent.putExtra("RESULT_STRING", f_path);
				setResult(RESULT_OK, intent);
				finish();
			}
		});

	}

	protected void onListItemClick(ListView l, View v, int position, long id) {
		File file = new File(path.get(position));
		if (file.isDirectory()) {
			if(file.canRead()) getDir(path.get(position));
			else {
				new AlertDialog.Builder(this)
						.setIcon(mipmap.ic_launcher)
						.setTitle("Ingen adgang til [" + file.getName() + "]")
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									@Override

									public void onClick(DialogInterface dialog, int which) {
										// Auto-generated method stub
									}
								}).show();
			}
		}
	}

}