package com.ejvindh.tododiary;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;
import com.ejvindh.tododiary.R;

public class TodoDiary extends Activity implements OnClickListener {
	
	private String dbk1;
	private String dbk2;
	private String dbs1;
	private String dbs2;
    final static private String ACCOUNT_PREFS_NAME = "prefs";
    final static private String db_name = "db";
    final static private String jumbleFileName = "jumble.txt";
    private boolean enable_todo;
    private boolean local_backup;
    private String local_backupfiles_path;
    private String local_backupfiles_endfolder = "TodoDiary";
    private int activity_requestCode_jumble=1;
    private int activity_requestCode_browsefiles=2;
    private int activity_requestCode_raw_db=3;
    private boolean mLoggedIn;
    DropboxAPI<AndroidAuthSession> mApi;
    private Button save_button;
    private Button goto_today;
    private Button markdone_button;
    private Button prevtodo_button;
    private Button nexttodo_button;
    private DatePicker date_picked;
    private TextView todocount;
    private EditText entrytext;
    private String prev_entrytext = "";
    private String prev_marked_done_by_button_entrytext = "";
    private String date_marker;
    private int count_date_position_in_text;
    private String filePath;
    private String db_name_full;
    private String date_flag = "||||";
    private String todo_flag_sign = "++++";
    private String todo_flag;
    private String endline = "\r\n";
    private String encoding = "UTF-16";
    private boolean authenticated;
    private int undone_count = -1;
    private boolean marked_todo_in_file = false;
    private boolean entrytext_changed = false;
    private boolean marked_done_by_button = false;
    private boolean entrytext_changed_after_marked_done_by_button = false;
    private boolean changes = false;
    private int current_db_readonly = 0;
    private boolean jumble_readonly = false;
    private ArrayList<Integer> changed_years = new ArrayList<Integer>();
    private ArrayList<Integer> todo_day_of_years = new ArrayList<Integer>();
    private int thisyear;
    private int thisday;
    private int currentyear;
    private String dbk3;
    private String dbk4;
    private String dbs3;
    private String dbs4;
    //public static Toast ejvind;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setDBAccess();
        AndroidAuthSession session = buildSession();
        mApi = new DropboxAPI<AndroidAuthSession>(session);
		 setContentView(R.layout.activity_tododiary);
		
		//Hent gemte settings, og hvis de ikke findes, så loade default
		SharedPreferences settings = getSharedPreferences("DBTodo_settings", 0);
		enable_todo = settings.getBoolean("enable_todo", true);
		local_backup = settings.getBoolean("local_backup", false);
		local_backupfiles_path = settings.getString("local_backupfiles_path", 
				Environment.getExternalStorageDirectory().toString());
		if (enable_todo) todo_flag = todo_flag_sign; else todo_flag = "";

		save_button = (Button)findViewById(R.id.save_button);
		save_button.setOnClickListener(this);
		goto_today = (Button)findViewById(R.id.goto_today);
		goto_today.setOnClickListener(this);
		markdone_button = (Button)findViewById(R.id.markdone_button);
		markdone_button.setOnClickListener(this);
		prevtodo_button = (Button)findViewById(R.id.prevtodo_button);
		prevtodo_button.setOnClickListener(this);
		nexttodo_button = (Button)findViewById(R.id.nexttodo_button);
		nexttodo_button.setOnClickListener(this);
		todocount = (TextView)findViewById(R.id.todocount);
		
		date_picked = (DatePicker)findViewById(R.id.datePicker1);
		date_picked.getCalendarView().setFirstDayOfWeek(Calendar.MONDAY);
		date_picked.setOnClickListener(this);
		date_picked.requestFocus();
		entrytext = (EditText)findViewById(R.id.editText1);
		entrytext.setBackgroundDrawable(getResources().getDrawable(R.drawable.read_only));
		entrytext.getBackground().setAlpha(0);
		
		thisday = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
		thisyear = Calendar.getInstance().get(Calendar.YEAR);
		currentyear = thisyear;
		
		Calendar selected_date = Calendar.getInstance();
		int selected_day = selected_date.get(Calendar.DAY_OF_MONTH);
		int selected_month = selected_date.get(Calendar.MONTH);
		int selected_year = selected_date.get(Calendar.YEAR);

        filePath = getFilesDir().getAbsolutePath();//returns current directory.

		if (savedInstanceState != null) {
			  //Hvis telefonen er blevet drejet portrait/horisontal el. lign, læses variable ind igen
		      authenticated = savedInstanceState.getBoolean("authenticated");
		      String saved_entrytext = savedInstanceState.getString("entrytext");
		      entrytext.setText(saved_entrytext);
		      prev_entrytext = savedInstanceState.getString("prev_entrytext");
		      prev_marked_done_by_button_entrytext = 
		    		  savedInstanceState.getString("prev_marked_done_by_button_entrytext");
		      int saved_selectedday = savedInstanceState.getInt("selectedday");
		      selected_day = saved_selectedday;
		      int saved_selectedmonth = savedInstanceState.getInt("selectedmonth");
		      selected_month = saved_selectedmonth;
		      int saved_selectedyear = savedInstanceState.getInt("selectedyear");
		      selected_year = saved_selectedyear;
		      currentyear = selected_year;
		      date_marker = create_date_marker(currentyear, selected_month, selected_day);
		      count_date_position_in_text = savedInstanceState.getInt("count_date_position_in_text");
		      marked_todo_in_file = savedInstanceState.getBoolean("marked_todo_in_file");
		      entrytext_changed = savedInstanceState.getBoolean("entrytext_changed");
		      marked_done_by_button = savedInstanceState.getBoolean("marked_done_by_button");
		      entrytext_changed_after_marked_done_by_button = savedInstanceState.getBoolean(
		    		  "entrytext_changed_after_marked_done_by_button");
		      changes = savedInstanceState.getBoolean("changes");
		      jumble_readonly = savedInstanceState.getBoolean("jumble_readonly");
		      current_db_readonly = savedInstanceState.getInt("current_db_readonly");
		      String temp_changed_years = savedInstanceState.getString("changed_years");
		      changed_years.clear();
		      if (!temp_changed_years.equals("_")) {
		    	  String[] opsplittet = temp_changed_years.split("\\_");
		    	  for(int i =0; i < opsplittet.length; i++) {
		    		  changed_years.add(Integer.parseInt(opsplittet[i]));
		    	  }
		      }
		}
		setDBNames(currentyear);//hent databasefilernes navn udfra årstal
		mLoggedIn = mApi.getSession().isLinked();
        if (!mLoggedIn) {
        	//Login til Dropbox
        	mApi.getSession().startOAuth2Authentication(TodoDiary.this);
        }

		if (savedInstanceState == null) {
			//Ved første kørsel af onCreate 
			DownloadDBtoLocal(currentyear, db_name_full);
			populateText(currentyear, 
					Calendar.getInstance().get(Calendar.MONTH), 
					Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
		}
		undone_count = countUndone(currentyear);//Hvor mange undone's er der i år?
		ButtonTexts();//opdater knapper o.lign
		
		date_picked.init(currentyear, selected_month, selected_day,  
			    new DatePicker.OnDateChangedListener(){

			   public void onDateChanged(DatePicker view, int yy, int mm, int dd){
				   //Når en ny dato er valgt
				   if (!authenticated) {
					   //Nogle gange når den ikke at blive ordentlig connected til DB, så prøv igen...
					   DownloadDBtoLocal(currentyear, db_name_full);
					   if (!authenticated) {
						   entrytext.setText(getResources().getString(R.string.failed_db_load));
						   entrytext.setEnabled(false);
					   }
				   }
				   if (authenticated) {
					   entrytext.setEnabled(true);
					   save_changed_entrytext();//Gem teksten, hvis den er blevet ændret
					   if (yy!=currentyear) {
						   //Har der været årsskift i Datepicker?
						   if (current_db_readonly !=0) {
							   //Var det forrige årstal hentet som read_only?
							   delete_readonly_db();
							   entrytext.getBackground().setAlpha(0);//fjern "Read Only" skiltet
						   }
						   change_year(yy);
					   }
					   undone_count = countUndone(currentyear);
					   ButtonTexts();
				       populateText(yy, mm, dd);//Sæt indhold ind i edittext for den valgte dato
				   }
			   }
			  }
			);
		entrytext.addTextChangedListener(new TextWatcher() {
		    @Override
		    public void afterTextChanged(Editable s) {
		        // Todo Auto-generated method stub
		    }

		    @Override
		    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		        // Todo Auto-generated method stub
		    }

		    @Override
		    public void onTextChanged(CharSequence s, int start, int before, int count) {
		    	//Hvis teksten er blevet ændret, så sæt de relevante ændringsflag, og opdater knap-tekster
				if (entrytext.getText().toString().compareTo(prev_entrytext) != 0) {
                    entrytext_changed = true;
                } else {
                	entrytext_changed = false;
                }
		        if (entrytext.getText().toString().compareTo(prev_marked_done_by_button_entrytext) != 0) {
                    entrytext_changed_after_marked_done_by_button = true;
                } else {
                	entrytext_changed_after_marked_done_by_button = false;
                }
		    	ButtonTexts();
		    }
		});
	}
	
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	//Settings-menu -- sæt de rigtige flueben, og vis/skjul knappen til at vælge mappe
    	menu.findItem(R.id.find_dict).setEnabled(local_backup);
    	menu.findItem(R.id.keeplocalcopy).setChecked(local_backup);
    	menu.findItem(R.id.enabletodo).setChecked(enable_todo);
        return true;
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Settings-menu - opsætning
		getMenuInflater().inflate(R.menu.settings_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		//Settings-menu -- hvad gør de enkelte valgmuligheder
		if (item.getItemId()== R.id.goto_jumble) {
			//Jumble-knappen. Start en ny aktivitet til at håndtere rode-filen
			delete_readonly_db(); //hvis jumble tidligere har været hentet ind som readonly, så slet den
			Intent jumble = new Intent(this, JumbleActivity.class);
  		  	String jumbletext="";
  		  	File jumblefile = new File(filePath, jumbleFileName);
  		  	if (!jumblefile.exists()) {
  		  		DownloadDBtoLocal(0, jumbleFileName);
  		  	}
  		  	try {
  		  		BufferedReader jumble_br = new BufferedReader 
						(new InputStreamReader(new FileInputStream(jumblefile), encoding));
				StringBuffer jumbletxt = new StringBuffer();
				String thisLine;
				while ((thisLine = jumble_br.readLine()) != null) { 
				    jumbletxt.append(thisLine + "\n");
				}
				jumble_br.close();
				jumbletext = jumbletxt.toString();
  		  	} catch (UnsupportedEncodingException e1) {
				// todo Auto-generated catch block
				e1.printStackTrace();
  		  	} catch (FileNotFoundException e1) {
				// todo Auto-generated catch block
				e1.printStackTrace();
  		  	} catch (IOException e) {
				// todo Auto-generated catch block
				e.printStackTrace();
  		  	}
  		  	jumble.putExtra("jumbletext", jumbletext);
  		  	jumble.putExtra("jumble_ro", jumble_readonly);
  		  	startActivityForResult(jumble, activity_requestCode_jumble);
		}
		if (item.getItemId()== R.id.raw_db) {
			//Vis den rå db-fil
			//TODO: Meget langsom scroll -- kan det fixes?
			save_changed_entrytext();
			Intent raw_db = new Intent(this, Raw_db_Activity.class);
			String raw_db_text="";
			File raw_db_file = new File(filePath, db_name_full);
			if (!raw_db_file.exists()) {
  		  		raw_db_text=getResources().getString(R.string.failed_db_load);
  		  	} else {
  	  		  	try {
  	  		  		BufferedReader raw_db_br = new BufferedReader 
  							(new InputStreamReader(new FileInputStream(raw_db_file), encoding));
  					StringBuffer raw_db_txt = new StringBuffer();
  					String thisLine;
  					while ((thisLine = raw_db_br.readLine()) != null) { 
  					    raw_db_txt.append(thisLine + "\n");
  					}
  					raw_db_br.close();
  					raw_db_text = raw_db_txt.toString();
  	  		  	} catch (UnsupportedEncodingException e1) {
  					// todo Auto-generated catch block
  					e1.printStackTrace();
  	  		  	} catch (FileNotFoundException e1) {
  					// todo Auto-generated catch block
  					e1.printStackTrace();
  	  		  	} catch (IOException e) {
  					// todo Auto-generated catch block
  					e.printStackTrace();
  	  		  	}
  		  	}
			raw_db.putExtra("raw_db_text", raw_db_text);
			raw_db.putExtra("raw_db_ro", (current_db_readonly!=0));
			raw_db.putExtra("raw_db_position", count_date_position_in_text);
  		  	startActivityForResult(raw_db, activity_requestCode_raw_db);
		}
		if (item.getItemId()== R.id.keeplocalcopy) {
			//Aktivere at der gemmes en lokal kopi
			item.setChecked(!item.isChecked());
			local_backup = item.isChecked();
		}
		if (item.getItemId()== R.id.find_dict) {
			//Vælge mappe til lokal backup
			Intent intent = new Intent(this, BrowseFiles.class);
			intent.putExtra("local_backupfiles_path", local_backupfiles_path);
			startActivityForResult(intent, activity_requestCode_browsefiles);
		}
		if (item.getItemId()== R.id.enabletodo) {
			//Skal Todo-funktionaliteten være tændt?
			item.setChecked(!item.isChecked());
			enable_todo = item.isChecked();
			if (enable_todo) todo_flag = todo_flag_sign; else todo_flag = "";
			undone_count = countUndone(currentyear);
			ButtonTexts();
		}
		return super.onOptionsItemSelected(item);
	}
	@Override
	protected void onSaveInstanceState (Bundle outState) {
		//B.la. hvis der skiftes fra landscape til portrait -- så skal alle variabler gemmes
		// (genindlæses i onCreate)
	    super.onSaveInstanceState(outState);
	    outState.putBoolean("authenticated", authenticated);
	    outState.putString("entrytext", entrytext.getText().toString());
	    outState.putInt("count_date_position_in_text", count_date_position_in_text);
	    outState.putString("prev_entrytext", prev_entrytext);
	    outState.putString("prev_marked_done_by_button_entrytext", prev_marked_done_by_button_entrytext);
	    outState.putInt("selectedday", date_picked.getDayOfMonth());
	    outState.putInt("selectedmonth", date_picked.getMonth());
	    outState.putInt("selectedyear", date_picked.getYear());
	    outState.putBoolean("marked_todo_in_file", marked_todo_in_file);
	    outState.putBoolean("entrytext_changed", entrytext_changed);
	    outState.putBoolean("marked_done_by_button", marked_done_by_button);
	    outState.putBoolean("entrytext_changed_after_marked_done_by_button", entrytext_changed_after_marked_done_by_button);
	    outState.putBoolean("changes", changes);
	    outState.putInt("current_db_readonly", current_db_readonly);
	    outState.putBoolean("jumble_readonly", jumble_readonly);
	    String changed_y = "";
		if (changed_years.size()>0) {
			for(int i=0;i<changed_years.size();i++) {  
				changed_y = changed_y + Integer.toString(changed_years.get(i)) + "_";
		    }
		} else {
			changed_y = "_";
		}
	    outState.putString("changed_years", changed_y);
	}
	
    @Override
    protected void onResume() {
        super.onResume();
        //Det følgende er nødvendigt til at samle op fra forsøget på at connecte til Dropbox
        AndroidAuthSession session = mApi.getSession();
        if (session.authenticationSuccessful()) {
            try {
                session.finishAuthentication();
                String token = session.getOAuth2AccessToken();
                storeKeys(token);
                mLoggedIn = true;
                DownloadDBtoLocal(currentyear, db_name_full);
                undone_count = 0;//Hvor mange undone's er der i år?
                ButtonTexts();
            } catch (IllegalStateException e) {
                showToast("Couldn't authenticate with Dropbox:" + e.getLocalizedMessage());
            }
        }
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	// Når app'en lukkes ned, gemmes de settings, der skal huskes på tværs af denne kørsel
		SharedPreferences settings = getSharedPreferences("DBTodo_settings", 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean("enable_todo", enable_todo);
		editor.putBoolean("local_backup", local_backup);
		editor.putString("local_backupfiles_path", local_backupfiles_path);
		editor.commit();
    }
    
	public void onClick(View v) {
		// Hvordan opfører knapperne sig
	      if (v.getId() == R.id.save_button) {
	    	  //Save-knappen
	    	  save_changed_entrytext();
	  		  save_local_to_DB();
	  		  populateText(currentyear, date_picked.getMonth(), date_picked.getDayOfMonth());
	  		  undone_count = countUndone(currentyear);
	  		  ButtonTexts();
	      } else if (v.getId() == R.id.goto_today) {
				//Find dagen-i-dag
				save_changed_entrytext();//Gem teksten, hvis den er blevet ændret
				if (currentyear != thisyear) {
					change_year(thisyear);
				}
				currentyear = thisyear;
				setPickedDayOfYear(thisday);
    	  } else if (v.getId() == R.id.markdone_button) {
    		  // Mark-done button
    		  marked_done_by_button = true;
    		  entrytext_changed_after_marked_done_by_button = false;
    		  prev_marked_done_by_button_entrytext = entrytext.getText().toString();
    		  ButtonTexts();
    	  } else if (v.getId() == R.id.prevtodo_button) {
    		  //prevtodo-button
    		  int selected_day_of_year = get_selected_day_of_year();
    		  int prev_day=0;
    		  for (int parsed_days : todo_day_of_years) {
    			  if (parsed_days<selected_day_of_year) prev_day=parsed_days;
    		  }
    		  if (prev_day !=0) {
    			  setPickedDayOfYear(prev_day);
    		  }
    		  ButtonTexts();
    	  } else if (v.getId() == R.id.nexttodo_button) {
    		  //nexttodo-button
    		  int selected_day_of_year = get_selected_day_of_year();
    		  int next_day=0;
    		  for (int parsed_days : todo_day_of_years) {
    			  if (next_day==0 && parsed_days>selected_day_of_year) next_day=parsed_days;
    		  }
    		  if (next_day !=0) {
    			  setPickedDayOfYear(next_day);
    		  }
    		  ButtonTexts();
    	  }
	}

	@Override
	public void onBackPressed() {
		// onPause aktiveres af mange andre ting end egentlig nedlukning af appen
		// denne onBackPressed samler op, når onPause er aktiveret for at stoppe programmet helt
		//TODO: Det kan vel egentlig give lidt noget bøvl, hvis programmet ikke lukkes ned af back-pressed
    	save_changed_entrytext();
		delete_readonly_db();
    	int DBUploadSucces=0;
		if (changes == true) {
			//TODO: Hvordan få den til at dukke op, før upload er færdig...
			DBUploadSucces = save_local_to_DB();
		}
		if (DBUploadSucces==0 || changes==false) {
			deletelocalfiles();
			super.onBackPressed();
		} else {
		    confirm_pause(DBUploadSucces);
		}
	}
    
	protected void confirm_pause(int error_code) {
		// Hvis der har været problemer med at gemme database filen, så skal brugeren bekræfte, 
		// at der skal lukkes
    	//Hvilken fejlmeddelelse fåes, hvis ingen netværk?? Network error == perfekt!!
    	Resources res = getResources();
    	String[] upload_errors = res.getStringArray(R.array.upload_errors);
        @SuppressWarnings("unused")
		AlertDialog alertbox = new AlertDialog.Builder(this)
        .setTitle(res.getString(R.string.upload_error_heading))
        .setMessage(upload_errors[error_code]  + "\n\n" + res.getString(R.string.upload_error_intro))
        .setPositiveButton(res.getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
    			deletelocalfiles();
                finish();
            }
        })
        .setNegativeButton(res.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                           }
        })
          .show();
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
        // Her samles op fra de aktiviteter, der skal give resultat tilbage
    	// (dvs. JumbleActivity og BrowseFiles)
    	// Check which request we're responding to
        if (requestCode == activity_requestCode_jumble) {
        	// JumbleActivity
        	if (resultCode == RESULT_OK) {
        		changes = true;
        		changed_years.add(0);
        		ButtonTexts();
                String returneret_result=data.getStringExtra("returningresult");
            	//Resultatet gemmes lokalt
                File jumblefile_write = new File(filePath, jumbleFileName);
                try {
            	BufferedWriter bw = new BufferedWriter
            		(new OutputStreamWriter(new FileOutputStream(jumblefile_write),encoding));
                bw.write(returneret_result.replaceAll("\n", endline));
            	bw.close();
                } catch (UnsupportedEncodingException e1) {
                	// todo Auto-generated catch block
            	e1.printStackTrace();
                } catch (FileNotFoundException e1) {
                	// todo Auto-generated catch block
            	e1.printStackTrace();
                } catch (IOException e) {
                	// todo Auto-generated catch block
            	e.printStackTrace();
                }
            }
            if (resultCode == RESULT_CANCELED) {    
            	//------Back from jumble -- no changes
            }
        }
		if (requestCode == activity_requestCode_browsefiles) {
			//BrowseFiles
		      if (resultCode == RESULT_OK) {
		    	  local_backupfiles_path = data.getStringExtra("RESULT_STRING");
		      }
		}
        if (requestCode == activity_requestCode_raw_db) {
        	//Return from Raw_db_view
        	if (resultCode == RESULT_OK) {
        		changes = true;
        		boolean changed_before = parseChangedYears(currentyear);
        		if (!changed_before) changed_years.add(currentyear);
                String returneret_result=data.getStringExtra("returningresult");
            	//Resultatet gemmes lokalt
                File raw_db_write = new File(filePath, db_name_full);
                try {
            	BufferedWriter bw = new BufferedWriter
            		(new OutputStreamWriter(new FileOutputStream(raw_db_write),encoding));
                bw.write(returneret_result.replaceAll("\n", endline));
            	bw.close();
                } catch (UnsupportedEncodingException e1) {
                	// todo Auto-generated catch block
            	e1.printStackTrace();
                } catch (FileNotFoundException e1) {
                	// todo Auto-generated catch block
            	e1.printStackTrace();
                } catch (IOException e) {
                	// todo Auto-generated catch block
            	e.printStackTrace();
                }
            }
            if (resultCode == RESULT_CANCELED) {    
            	//------Back from raw_db_view -- no changes
            }
            populateText(currentyear, date_picked.getMonth(), date_picked.getDayOfMonth());
    		ButtonTexts();
        }
    }
    
	private int save_local_to_DB() {
		// Uploade de database-filer, der er blevet ændret i til Dropbox
		int successflag = 0;
		if (changed_years.size()>0) {
			for(int i=0;i<changed_years.size();i++) {  
		        setDBNames(changed_years.get(i));
				if (changed_years.get(i)==0) {
					//Gemme jumble-file
			        int temp_success = UploadLocaltoDB(jumbleFileName);
			        if (temp_success != 0) successflag = temp_success;
				} else {
					//Gemme kalenderår
			        int temp_success = UploadLocaltoDB(db_name_full);
			        if (temp_success != 0) successflag = temp_success;
				}
		    }
		}
		setDBNames(currentyear);
		if (successflag==0) {
			changes = false;
			changed_years.clear();
		} else {
			showToast("Failed uploading to Dropbox");
		}
		return successflag;
	}

	private int UploadLocaltoDB(String db_name_full) {
		// Selve forbindelsesinterfacet til Dropbox (kalder DBUpload.java)
	    File file = new File(filePath, db_name_full);
	    DBUpload dbfile = new DBUpload(this, mApi, db_name_full, file);
	    dbfile.execute();
		int UploadSuccessflag = 0;
	    try {
	    	UploadSuccessflag = dbfile.get();
	    	if (UploadSuccessflag==0) {
				changes = false;
			}
		} catch (InterruptedException e) {
			// Todo Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// Todo Auto-generated catch block
			e.printStackTrace();
		}
	    return UploadSuccessflag;
	}

	private void DownloadDBtoLocal(int year, String filename) {
		// Hente en databasefil fra Dropbox til det lokale medie (kalder DBDownload.java)
		File file = new File(filePath, filename);
        DBDownload localfile = new DBDownload(this, mApi, filename, file);
        localfile.execute();
        try {
			if (localfile.get()==0) {
				authenticated = true;
			} else if (localfile.get()>=1 && localfile.get()<=3) {
				//------ikke authenticated"
				authenticated = false;
			} else if (localfile.get()==4) {
				CreateDB.WriteDB(this, year, filename, date_flag, endline, encoding, filePath);
				authenticated = true;
				changes = true;
				changed_years.add(year);
			} else if (localfile.get()>=6 && localfile.get()<=10) {
				//Bøvl med netværksforbindelse/serveradgang
				//Check om der ligger en lokal kopi
				authenticated = false;
		    	String localcopyfoldername = local_backupfiles_path + "/" + local_backupfiles_endfolder;
				File keeplocalcopy_file = new File(localcopyfoldername, filename);
		    	if (keeplocalcopy_file.exists()) {
		        	copyFile(keeplocalcopy_file, file);
		        	current_db_readonly = currentyear;
		        	authenticated = true;
		        	showToast("Showing latest copy from local storage (readonly)");
		    		entrytext.getBackground().setAlpha(51);//vis "Read Only" skiltet i edittext
		    	}
		    	if (filename.equals(jumbleFileName)) {
		    		jumble_readonly = true;
		    	}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	private void deletelocalfiles() {
    	// Slet alle lokale filer. Hvis local_backup=true, så gemmes også en backupfil lokalt
    	String localcopyfoldername = local_backupfiles_path + "/" + local_backupfiles_endfolder;
		File keeplocalcopy_folder = new File(localcopyfoldername);
    	if (!keeplocalcopy_folder.exists()) keeplocalcopy_folder.mkdir();
		File deletefolder = new File(filePath);
		if (deletefolder.isDirectory()) {
	        String[] children = deletefolder.list();
	        for (int i = 0; i < children.length; i++) {
	        	if (local_backup) copyFile(new File(deletefolder, children[i]), new File(keeplocalcopy_folder, children[i]));
		        new File(deletefolder, children[i]).delete();
	        }
	    }
	}
    
    private void delete_readonly_db() {
		if (current_db_readonly != 0|| jumble_readonly) {
			//Hvis den nuværende db-fil eller jumble-fil er hentet fra lokal backup (og derfor readonly), 
			// så slet den når der skiftes årstal
			//(ellers risikeres det at en nyere fil i Dropbox overskrives af en ældre)
			String readonly_filename = db_name+Integer.toString(current_db_readonly)+".txt";
			File delete_ro_db_file = new File(filePath, readonly_filename);
			if (delete_ro_db_file.exists()) {
				delete_ro_db_file.delete();
			}
			current_db_readonly = 0;
			if (jumble_readonly) {
				File delete_jumble_file = new File(filePath, jumbleFileName);
				if (delete_jumble_file.exists()) delete_jumble_file.delete();
				jumble_readonly = false;
			}
		}
	}

    public static void copyFile(File src, File dst) {
    	// Til at kopiere filer fra internal til external hukommelse
    	// (bruges i deletelocalfiles til at gemme en lokal backup-fil)
        FileChannel inChannel;
        FileChannel outChannel;
		try {
			inChannel = new FileInputStream(src).getChannel();
	        outChannel = new FileOutputStream(dst).getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
		} catch (FileNotFoundException e) {
			// Todo Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// Todo Auto-generated catch block
			e.printStackTrace();
        }
    }

	private void ButtonTexts() {
		//Opdatere udseendet af de forskellige knapper o.lign.
		//*************** SaveButton
		save_button.setEnabled(changes || entrytext_changed || marked_done_by_button);
		
		//*************** TodayButton
		//Grey hvis vi allerede er på idag
		int current_day_of_year = get_selected_day_of_year();
		goto_today.setEnabled(!(current_day_of_year == thisday && currentyear==thisyear));
		
		//*************** Entrytext: Readonly-mode?
		if (current_db_readonly != 0|| !authenticated) {
			entrytext.setFocusable(false);
		} else {
			entrytext.setFocusableInTouchMode(true);
		}
		if (enable_todo) {
			//For hele Todo-knapperiet: Skal kun vises, hvis enable_todo
			//*************** MarkDoneButton
			markdone_button.setVisibility(View.VISIBLE);
			if ((marked_todo_in_file && !marked_done_by_button) || entrytext_changed_after_marked_done_by_button) {
				markdone_button.setText(R.string.mark_done);
				markdone_button.setTextColor(Color.rgb(128,0,0));
				markdone_button.setEnabled(authenticated && current_db_readonly==0); //tændt hvis authenticated og visningen ikke er RO
			} else if (marked_done_by_button && !entrytext_changed_after_marked_done_by_button && marked_todo_in_file
					|| marked_done_by_button && !entrytext_changed_after_marked_done_by_button && entrytext_changed) {
				markdone_button.setText(R.string.done);
				markdone_button.setTextColor(Color.rgb(0,107,0));
				markdone_button.setEnabled(authenticated && current_db_readonly==0); //tændt hvis visningen ikke er RO
			} else {
				markdone_button.setText(R.string.allset);
				markdone_button.setTextColor(Color.rgb(179,179,179));
				markdone_button.setEnabled(false);
			}

			//*************** PrevButton + NextButton
			prevtodo_button.setVisibility(View.VISIBLE);
			nexttodo_button.setVisibility(View.VISIBLE);
			int selected_day_of_year = get_selected_day_of_year();
			if (todo_day_of_years.isEmpty()) {
				prevtodo_button.setEnabled(false);
				nexttodo_button.setEnabled(false);
			} else {
				if (selected_day_of_year > todo_day_of_years.get(0)) {
					prevtodo_button.setEnabled(true);
				} else {
					prevtodo_button.setEnabled(false);
				}
				if (selected_day_of_year < todo_day_of_years.get(todo_day_of_years.size()-1)) {
					nexttodo_button.setEnabled(true);
				} else {
					nexttodo_button.setEnabled(false);
				}
			}
			
			//*************** Todo-counter
			todocount.setVisibility(View.VISIBLE);
			if (undone_count!=-1) {
				if (currentyear<=thisyear) {
					todocount.setEnabled(true);
					int temp_counter = undone_count;
					int currentday = get_selected_day_of_year();
					if ((currentyear<thisyear) || (currentday < thisday)) {
						if (marked_todo_in_file && marked_done_by_button) {
							temp_counter = temp_counter - 1;
						}
					}
					todocount.setText(getString(R.string.leftovers) + " "+Integer.toString(currentyear)+
							":\n"+Integer.toString(temp_counter)+" "+ getString(R.string.entries));
				} else {
					todocount.setEnabled(false);
					todocount.setText(getString(R.string.leftovers) + " "+Integer.toString(currentyear)+
							":\n------");
				}
			}
		} else {
			//Todo-knapperne skjules, hvis todo-funktionaliteten er slået fra
			markdone_button.setVisibility(View.INVISIBLE);;
			prevtodo_button.setVisibility(View.INVISIBLE);
			nexttodo_button.setVisibility(View.INVISIBLE);
			todocount.setVisibility(View.INVISIBLE);
		}
	}
	
	private String create_date_marker(int yy, int mm, int dd) {
		// Lave en streng med dato-stemplet ud fra den valgte dato (som den findes i database-filerne)
	    String[] dayNames = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
	    String parseDayName;
	    Calendar parseDays = Calendar.getInstance();
	    parseDays.set(Calendar.YEAR, yy);
	    parseDays.set(Calendar.MONTH, mm);
	    parseDays.set(Calendar.DAY_OF_MONTH, dd);
	    parseDayName = dayNames[parseDays.get(Calendar.DAY_OF_WEEK)-1];
		String datemarkresult = date_flag + " " 
					   + parseDayName + " " 
		    		   + parseDays.get(Calendar.YEAR) + "-" 
		    		   + String.format("%02d", (parseDays.get(Calendar.MONTH)+1)) + "-" 
		    		   + String.format("%02d", parseDays.get(Calendar.DAY_OF_MONTH));
		return datemarkresult;
	}
	
	private void populateText(int yy, int mm, int dd) {
		// fyld indhold i entrytext
		count_date_position_in_text=0;
		boolean date_position_in_text_found = false;
		date_marker = create_date_marker(yy, mm, dd);
	    File file = new File(filePath, db_name_full);
	    try {
			BufferedReader br = new BufferedReader 
					(new InputStreamReader(new FileInputStream(file), encoding));
			boolean inside_date = false;
			String date_text = "";
			String thisLine;
			while ((thisLine = br.readLine()) != null) {
				if (!date_position_in_text_found) {
					count_date_position_in_text = count_date_position_in_text + thisLine.length() + 1;
				}
			    if (inside_date) {
			    	if (thisLine.contains(date_flag)) { 
			    		inside_date = false;
			    	} else {
			    		date_text = date_text + thisLine + "\n";
			    	}
			    }
			    if(thisLine.contains(date_marker)) { 
			    	date_position_in_text_found = true;
			    	if(thisLine.contains(todo_flag_sign)) marked_todo_in_file=true; else marked_todo_in_file = false;
			        inside_date = true;
			    }
			}
			br.close();
			prev_entrytext = date_text;
			prev_marked_done_by_button_entrytext = date_text;
			entrytext.setText(date_text);
		} catch (UnsupportedEncodingException e1) {
			// todo Auto-generated catch block
			e1.printStackTrace();
		} catch (FileNotFoundException e1) {
			// todo Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// todo Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void change_year(int new_year) {
		currentyear = new_year;
		setDBNames(currentyear);
		undone_count = countUndone(currentyear);
		ButtonTexts();
		boolean changed_before = parseChangedYears(currentyear);
		if (!changed_before) {
			DownloadDBtoLocal(currentyear, db_name_full);
		}
	}

	private boolean parseChangedYears(int year) {
		// Løb igennem de ændrede database-filer, for at se om det nuværende årstal har været ændret før
		boolean changed_before = false;
		if (changed_years.size()>0) {
			for(int i=0;i<changed_years.size();i++) {  
		        if (changed_years.get(i).equals(currentyear)) {
		        	changed_before = true;
		        }
		    }
		}
		return changed_before;
    }
    
	private int countUndone(int currentyear) {
		// Tæl hvor mange undone-markeringer der er i den valgte årgang.
		// Der tælles kun datoer, der ligger før "i dag"
		int counting = 0;
		todo_day_of_years.clear();
		File file = new File(filePath, db_name_full);
	    try {
			BufferedReader br = new BufferedReader 
					(new InputStreamReader(new FileInputStream(file), encoding));
			String thisLine;
			if (currentyear < thisyear) {
				while ((thisLine = br.readLine()) != null) { 
				    if (thisLine.contains(todo_flag_sign) && thisLine.contains(date_flag)) {
				    	todo_day_of_years.add(convertDateMarkerToDayOfYear(thisLine));
				    	counting++;
				    }
				}
			} else if (currentyear == thisyear) {
				int currentday;
				while ((thisLine = br.readLine()) != null) {
				    if (thisLine.contains(todo_flag_sign) && thisLine.contains(date_flag)) {
						currentday = convertDateMarkerToDayOfYear(thisLine);
				    	todo_day_of_years.add(currentday);
					    if (currentday <=thisday) {
					    	counting++;
					    }
					}
				}
			} else {
				while ((thisLine = br.readLine()) != null) {
				    if (thisLine.contains(todo_flag_sign) && thisLine.contains(date_flag)) {
				    	todo_day_of_years.add(convertDateMarkerToDayOfYear(thisLine));
					}
				}
			}
			br.close();
		} catch (UnsupportedEncodingException e1) {
			// todo Auto-generated catch block
			e1.printStackTrace();
		} catch (FileNotFoundException e1) {
			// todo Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// todo Auto-generated catch block
			e.printStackTrace();
		}
		return counting;
	}
    
	private int convertDateMarkerToDayOfYear(String thisLine) {
		// Udregne hvilken dag-på-året som den pågældende date-marker repræsenterer
		int parsedmonth;
		int parsedday;
		Calendar parseDays = Calendar.getInstance();
	    parseDays.set(Calendar.YEAR, currentyear);
		
		String pmonth = thisLine.substring(
				thisLine.length()-(todo_flag_sign.length()+5), 
				thisLine.length()-(todo_flag_sign.length()+3));
		try {
			parsedmonth = Integer.parseInt(pmonth)-1;
		} catch(NumberFormatException nfe) {
			parsedmonth = 0;
		}
		String pday = thisLine.substring(
				thisLine.length()-(todo_flag_sign.length()+2), 
				thisLine.length()-todo_flag_sign.length());
		try {
			parsedday = Integer.parseInt(pday);
		} catch(NumberFormatException nfe) {
			parsedday = 1;
		}
	    parseDays.set(Calendar.MONTH, parsedmonth);
	    parseDays.set(Calendar.DAY_OF_MONTH, parsedday);
	    return parseDays.get(Calendar.DAY_OF_YEAR);
	}

	private int get_selected_day_of_year() {
		// Udregne hvilken day-of-year den valgte dato udgør
		int cur_month = date_picked.getMonth();
		int cur_day = date_picked.getDayOfMonth();
		Calendar selected_day = Calendar.getInstance();
		selected_day.set(Calendar.YEAR, currentyear);
		selected_day.set(Calendar.MONTH, cur_month);
		selected_day.set(Calendar.DAY_OF_MONTH, cur_day);
		return selected_day.get(Calendar.DAY_OF_YEAR);
	}

	private void setPickedDayOfYear(int new_picked_day_of_year) {
		// Ændre datepicker til at være den valgte day-of-year
		Calendar selected_day = Calendar.getInstance();
		selected_day.set(Calendar.YEAR, currentyear);
		selected_day.set(Calendar.DAY_OF_YEAR, new_picked_day_of_year);
		date_picked.updateDate(currentyear, 
				selected_day.get(Calendar.MONTH), 
				selected_day.get(Calendar.DAY_OF_MONTH));
	}
	
	private void setDBAccess() {
		dbs1 = "b-g+j";
		dbk1 = "0+1*7";
		dbk2 = "d-zt+s";
		dbk3 = "k*3-t";
		dbs2 = "y*t-4";
		dbs3 = "d-pr+h*6";
		dbs4 = "6-p-t*i";
		dbk4 = "r+8+o-p*9";
	}

	private void setDBNames(int year) {
		//lave en streng med database-filnavnet ud fra det valgte årstal
		db_name_full = db_name+year+".txt";
	}
	
	private void save_changed_entrytext() {
		// Gem den tekst, der ligger in entrytext til den lokale databasefil (ikke dropbox endnu)
		if (current_db_readonly == 0) {
			String current_entrytext = entrytext.getText().toString();
	    	if (!current_entrytext.endsWith("\n")) {
	    		current_entrytext = current_entrytext + "\n\n";
	    	}
			if (!current_entrytext.equals(prev_entrytext) || marked_done_by_button){
				//Hvis teksten er blevet ændret, skal ændringer gemmes
				changes = true;
				//Har dette årstal været ændret før? Ellers marker som ugemt
				boolean changed_before = parseChangedYears(currentyear);
				if (!changed_before) changed_years.add(currentyear);
				ButtonTexts();
				File file_read = new File(filePath, db_name_full);
			    File file_write = new File(filePath, "_"+db_name_full);
			    try {
					BufferedReader br = new BufferedReader 
							(new InputStreamReader(new FileInputStream(file_read),encoding));
				    BufferedWriter bw = new BufferedWriter
				    		    (new OutputStreamWriter(new FileOutputStream(file_write),encoding));
					boolean inside_date = false;
					String thisLine;
					while ((thisLine = br.readLine()) != null) { 
					    if (inside_date) {
					    	if (thisLine.contains(date_flag)) {
					    		inside_date = false;
					    		bw.write(thisLine + endline);
					    	}
					    } else {
						    if(thisLine.contains(date_marker)) {
						    	String writeline;
						    	String temp_marked_done;
						    	if ((marked_todo_in_file && !marked_done_by_button) 
						    			|| entrytext_changed_after_marked_done_by_button) {
						    		temp_marked_done = todo_flag;
						    	} else {
						    		temp_marked_done = "";
						    	}
						    	writeline = thisLine.replace(todo_flag,  "") + temp_marked_done + endline;
						    	bw.write(writeline);
						    	inside_date = true;
						    	//TODO: Det kunne nu være rart, hvis jeg kunne lave det sådan, at der altid
						    	//er mindst én ekstra ekstra-linie mellem datoer...
						        bw.write(current_entrytext.replaceAll("\n", endline));
						    } else {
						    	bw.write(thisLine + endline);
						    }
					    }
					}
					br.close();
					bw.close();
					marked_done_by_button = false;
					file_read.delete();
					file_write.renameTo(file_read);
				} catch (UnsupportedEncodingException e1) {
					// todo Auto-generated catch block
					e1.printStackTrace();
				} catch (FileNotFoundException e1) {
					// todo Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e) {
					// todo Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
    @SuppressWarnings("unused")
    //Bruges ikke lige nu, men hvis engang jeg vil håndtere dårlig authentication, så.....
	private void logOut() {
        // Remove credentials from the session
        mApi.getSession().unlink();
        // Clear our stored keys
        clearKeys();
        // Change UI state to display logged out version
        mLoggedIn = false;
    }

    public void showToast(String msg) {
    	//Vise kortvarige advarsler
        Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        error.show();
    }

    private String getKeys() {
    	// Hente Dropbox-nøgle fra sharedpref, hvis der tidligere har været autenticiteret
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        String key = prefs.getString("TOKEN_KEYS", null);
        if (key != null) {
        	return key;
        } else {
        	return null;
        }
    }

    private void storeKeys(String token) {
        // Save the access key for later
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        Editor edit = prefs.edit();
        edit.putString("TOKEN_KEYS", token);
        edit.commit();
    }

    private void clearKeys() {
        // Slet autentification fra sharedpref
    	SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        Editor edit = prefs.edit();
        edit.clear();
        edit.commit();
    }

    private AndroidAuthSession buildSession() {
    	// Søg autentification til Dropbox
    	String polyk = dbk1 + dbk2 + dbk3 + dbk4;
    	String polyk1 = polyk.replaceAll("[\\+\\-\\*]", "");
    	String polys = dbs1 + dbs2 + dbs3 + dbs4;
    	String polys1 = polys.replaceAll("[\\+\\-\\*]", "");
        AppKeyPair appKeyPair = new AppKeyPair(polyk1, polys1);
        AndroidAuthSession session = new AndroidAuthSession(appKeyPair);
        String stored = getKeys();
        if (stored != null) {
        	session.setOAuth2AccessToken(stored);
        }
        return session;
    }
}
