package com.ejvindh.tododiary;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.android.Auth;
import com.dropbox.core.v2.DbxClientV2;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import static com.prolificinteractive.materialcalendarview.MaterialCalendarView.SELECTION_MODE_NONE;
import static com.prolificinteractive.materialcalendarview.MaterialCalendarView.SELECTION_MODE_SINGLE;


public class TodoDiary extends AppCompatActivity implements OnClickListener, OnDateSelectedListener {

    final static private String db_name = "db";
    final static private String jumbleFileName = "jumble.txt";
    final static private String local_backupfiles_endfolder = "TodoDiary";
    final static private String date_flag = "||||";
    final static private String todo_flag_sign = "++++";
    final static private String endline = "\r\n";
    final static private String encoding = "UTF-16";
    private boolean savedInstanceStateFlag;
    private String todo_flag;
    private boolean enable_todo;
    private boolean local_backup;
    private String local_backupfiles_path;
    private int activity_requestCode_jumble=1;
    private int activity_requestCode_browsefiles=2;
    private int activity_requestCode_raw_db=3;
    private DbxClientV2 dbxClient;
    private String accessToken;
    private Button save_button;
    private Button goto_today;
    private Button markdone_button;
    private Button prevtodo_button;
    private Button nexttodo_button;
    private MaterialCalendarView date_picked;
    private TextView todocount;
    private EditText entrytext;
    private String prev_entrytext = "";
    private String prev_marked_done_by_button_entrytext = "";
    private String date_marker;
    private int count_date_position_in_text;
    private String filePath;
    private String db_name_full;
    private boolean authenticated;
    private int undone_count = -1;
    private boolean marked_todo_in_file = false;
    private boolean entrytext_changed = false;
    private boolean marked_done_by_button = false;
    private boolean entrytext_changed_after_marked_done_by_button = false;
    private boolean changes = false;
    private int current_db_readonly = 0;
    private boolean jumble_readonly = false;
    private ArrayList<Integer> loadedToLocal_years = new ArrayList<>();
    private ArrayList<Integer> changed_years = new ArrayList<>();
    private ArrayList<Integer> todo_day_of_years = new ArrayList<>();
    private int thisYear;
    @SuppressWarnings("FieldCanBeLocal")
    private int thisMonth;
    private int thisDayOfYear;
    @SuppressWarnings("FieldCanBeLocal")
    private int thisDayOfMonth;
    private int selectedDayOfMonth;
    private int selectedDayOfYear;
    private int selectedMonth;
    private int selectedYear;
    private boolean loadingFlag;
    private boolean endingFlag;
    private int externalReadWritePermissionCheckFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tododiary);
        thisYear = Calendar.getInstance().get(Calendar.YEAR);
        thisMonth = Calendar.getInstance().get(Calendar.MONTH);
        thisDayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        thisDayOfMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

        //Hent gemte settings, og hvis de ikke findes, så loade default
        SharedPreferences settings = getSharedPreferences("DBTodo_settings", 0);
        enable_todo = settings.getBoolean("enable_todo", true);
        local_backup = settings.getBoolean("local_backup", false);
        local_backupfiles_path = settings.getString("local_backupfiles_path",
                Environment.getExternalStorageDirectory().toString());
        if (enable_todo) todo_flag = todo_flag_sign; else todo_flag = "";

        save_button = (Button) findViewById(R.id.save_button);
        save_button.setOnClickListener(this);
        goto_today = (Button) findViewById(R.id.goto_today);
        goto_today.setOnClickListener(this);
        markdone_button = (Button) findViewById(R.id.markdone_button);
        markdone_button.setOnClickListener(this);
        prevtodo_button = (Button) findViewById(R.id.prevtodo_button);
        prevtodo_button.setOnClickListener(this);
        nexttodo_button = (Button) findViewById(R.id.nexttodo_button);
        nexttodo_button.setOnClickListener(this);
        todocount = (TextView) findViewById(R.id.todocount);
        date_picked = (MaterialCalendarView) findViewById(R.id.calendarView);
        date_picked.setOnDateChangedListener(this);
        entrytext = (EditText) findViewById(R.id.editText1);
        entrytext.setBackgroundResource(R.drawable.read_only);
        entrytext.getBackground().setAlpha(0);

        if (savedInstanceState != null) {
            //Hvis telefonen er blevet drejet portrait/horisontal el. lign, læses variable ind igen
            savedInstanceStateFlag = true;
            authenticated = savedInstanceState.getBoolean("authenticated");
            String saved_entrytext = savedInstanceState.getString("entrytext");
            entrytext.setText(saved_entrytext);
            prev_entrytext = savedInstanceState.getString("prev_entrytext");
            prev_marked_done_by_button_entrytext =
                    savedInstanceState.getString("prev_marked_done_by_button_entrytext");
            selectedDayOfYear = savedInstanceState.getInt("selecteddayofyear");
            selectedDayOfMonth = savedInstanceState.getInt("selectedday");
            selectedMonth = savedInstanceState.getInt("selectedmonth");
            selectedYear = savedInstanceState.getInt("selectedyear");
            date_marker = create_date_marker(selectedYear, selectedMonth, selectedDayOfMonth);
            count_date_position_in_text = savedInstanceState.getInt("count_date_position_in_text");
            marked_todo_in_file = savedInstanceState.getBoolean("marked_todo_in_file");
            entrytext_changed = savedInstanceState.getBoolean("entrytext_changed");
            marked_done_by_button = savedInstanceState.getBoolean("marked_done_by_button");
            entrytext_changed_after_marked_done_by_button = savedInstanceState.getBoolean(
                    "entrytext_changed_after_marked_done_by_button");
            changes = savedInstanceState.getBoolean("changes");
            loadingFlag = savedInstanceState.getBoolean("loadingFlag");
            endingFlag = savedInstanceState.getBoolean("endingFlag");
            externalReadWritePermissionCheckFlag = savedInstanceState.getInt("externalReadWritePermissionCheckFlag");
            jumble_readonly = savedInstanceState.getBoolean("jumble_readonly");
            current_db_readonly = savedInstanceState.getInt("current_db_readonly");
            String temp_changed_years = savedInstanceState.getString("changed_years");
            changed_years.clear();
            assert temp_changed_years != null;
            if (!temp_changed_years.equals("_")) {
                String[] opsplittet = temp_changed_years.split("_");
                for (String anOpsplittet : opsplittet) {
                    changed_years.add(Integer.parseInt(anOpsplittet));
                }
            }
        } else {
            //Ved første kørsel af onCreate
            savedInstanceStateFlag = false;
            selectedYear = thisYear;
            selectedMonth = thisMonth;
            selectedDayOfMonth = thisDayOfMonth;
            Calendar tmpSelectedDay = Calendar.getInstance();
            tmpSelectedDay.set(selectedYear, selectedMonth, selectedDayOfMonth);
            selectedDayOfYear = tmpSelectedDay.get(Calendar.DAY_OF_YEAR);
            if (!permissionCheck()) local_backup = false;
        }
        filePath = getFilesDir().getAbsolutePath();//returns current directory.
        setDBNames(selectedYear);//hent databasefilernes navn udfra årstal
        getAccessToken();

        entrytext.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                // Auto-generated method stub
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Auto-generated method stub
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Hvis teksten er blevet ændret, så sæt de relevante ændringsflag, og opdater kna
                entrytext_changed = entrytext.getText().toString().compareTo(prev_entrytext) != 0;
                entrytext_changed_after_marked_done_by_button =
                        entrytext.getText().toString().compareTo(prev_marked_done_by_button_entrytext) != 0;
                undone_count = countUndone(selectedYear);
                ButtonTexts();
            }
        });
    }

    @Override
    public void onDateSelected(@NonNull MaterialCalendarView date_picked, @NonNull CalendarDay date, boolean selected) {
        //selected is no value on logcat
        if (selected) {
            makeChangesOnDateChange(date);
        }

    }

    public void makeChangesOnDateChange(CalendarDay date) {
        if (!authenticated) {
            entrytext.setText(getResources().getString(R.string.failed_db_load));
            entrytext.setEnabled(false);
        }
        if (authenticated) {
            int oldSelectedYear = selectedYear;
            selectedDayOfMonth = date.getDay();
            selectedMonth = date.getMonth();
            selectedYear = date.getYear();
            Calendar tmpSelectedDay = Calendar.getInstance();
            tmpSelectedDay.setTime(date.getDate());
            selectedDayOfYear = tmpSelectedDay.get(Calendar.DAY_OF_YEAR);
            //Når en ny dato er valgt
            entrytext.setEnabled(true);
            save_changed_entrytext();//Gem teksten, hvis den er blevet ændret
            if (oldSelectedYear != selectedYear) {
                //Har der været årsskift i Datepicker?
                if (current_db_readonly != 0) {
                    //Var det forrige årstal hentet som read_only?
                    delete_readonly_db();
                    entrytext.getBackground().setAlpha(0);//fjern "Read Only" skiltet
                }
                change_year(selectedYear);
                //Downloads dbtolocal if necessary
            }
            undone_count = countUndone(selectedYear);
            ButtonTexts();
            populateText(selectedYear, selectedMonth, selectedDayOfMonth);//Sæt indhold ind i edittext for den valgte dato
        }
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //Settings-menu -- sæt de rigtige flueben, og vis/skjul knappen til at vælge mappe
        boolean visibilityFlag = !loadingFlag;
        MenuItem itemLoading = menu.findItem(R.id.loadingMessage);
        MenuItem itemJumble = menu.findItem(R.id.goto_jumble);
        MenuItem itemRaw = menu.findItem(R.id.raw_db);
        MenuItem itemLocal = menu.findItem(R.id.keeplocalcopy);
        MenuItem itemFindDict = menu.findItem(R.id.find_dict);
        MenuItem itemTodo = menu.findItem(R.id.enabletodo);

        itemLoading.setVisible(!visibilityFlag);
        itemJumble.setVisible(visibilityFlag);
        itemRaw.setVisible(visibilityFlag);
        itemLocal.setVisible(visibilityFlag);
        itemLocal.setChecked(local_backup);
        itemTodo.setVisible(visibilityFlag);
        itemTodo.setChecked(enable_todo);
        if (loadingFlag) {
            itemFindDict.setVisible(visibilityFlag);
        } else {
            itemFindDict.setVisible(local_backup);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Settings-menu -- hvad gør de enkelte valgmuligheder
        if (item.getItemId()== R.id.goto_jumble) {
            //Jumble-knappen. Start en ny aktivitet til at håndtere rode-filen
            delete_readonly_db(); //hvis jumble tidligere har været hentet ind som readonly, så slet den
            File jumblefile = new File(filePath, jumbleFileName);
            if (!jumblefile.exists()) {
                DownloadDBtoLocal(0, jumbleFileName);
            } else {
                openJumbleActivity(jumblefile);
            }
        }
        if (item.getItemId()== R.id.raw_db) {
            //Vis den rå db-fil
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
                    StringBuilder raw_db_txt = new StringBuilder();
                    String thisLine;
                    while ((thisLine = raw_db_br.readLine()) != null) {
                        raw_db_txt.append(thisLine).append("\n");
                    }
                    raw_db_br.close();
                    raw_db_text = raw_db_txt.toString();
                } catch (IOException e) {
                    // Auto-generated catch block
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
            externalReadWritePermissionCheckFlag = 1; // Toogle localCopy
            if (permissionCheck()) {
                item.setChecked(!item.isChecked());
                local_backup = item.isChecked();
            }
        }
        if (item.getItemId()== R.id.find_dict) {
            //Vælge mappe til lokal backup
            externalReadWritePermissionCheckFlag = 2; // Change localCopyFolder
            if (permissionCheck()) {
                //Only opens if permissions are given. If permissions have been revoked, they can be given again
                //...however will not open the intent the first time anyway.
                openChangeLocalCopyFolderIntent();
            }
        }
        if (item.getItemId()== R.id.enabletodo) {
            //Skal Todofunktionaliteten være tændt?
            item.setChecked(!item.isChecked());
            enable_todo = item.isChecked();
            if (enable_todo) todo_flag = todo_flag_sign; else todo_flag = "";
            undone_count = countUndone(selectedYear);
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
        outState.putInt("selectedday", selectedDayOfMonth);
        outState.putInt("selecteddayofyear", selectedDayOfYear);
        outState.putInt("selectedmonth", selectedMonth);
        outState.putInt("selectedyear", selectedYear);
        outState.putBoolean("marked_todo_in_file", marked_todo_in_file);
        outState.putBoolean("entrytext_changed", entrytext_changed);
        outState.putBoolean("marked_done_by_button", marked_done_by_button);
        outState.putBoolean("entrytext_changed_after_marked_done_by_button", entrytext_changed_after_marked_done_by_button);
        outState.putBoolean("changes", changes);
        outState.putBoolean("loadingFlag", loadingFlag);
        outState.putBoolean("endingFlag", endingFlag);
        outState.putInt("externalReadWritePermissionCheckFlag", externalReadWritePermissionCheckFlag);
        outState.putInt("current_db_readonly", current_db_readonly);
        outState.putBoolean("jumble_readonly", jumble_readonly);
        StringBuilder changed_y = new StringBuilder();
        if (changed_years.size()>0) {
            for(int i=0;i<changed_years.size();i++) {
                changed_y.append(Integer.toString(changed_years.get(i))).append("_");
            }
        } else {
            changed_y = new StringBuilder("_");
        }
        outState.putString("changed_years", changed_y.toString());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!savedInstanceStateFlag) {
            if (accessToken == null) {
                accessToken = Auth.getOAuth2Token();
            }
            if (accessToken != null) {
                storeToken(accessToken);
                DropboxClientFactory();
                DownloadDBtoLocal(selectedYear, db_name_full);
                savedInstanceStateFlag = true;
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
        editor.apply();
        if (endingFlag) {
            System.exit(1);
        }
    }

    public void onClick(View v) {
        // Hvordan opfører knapperne sig
        if (v.getId() == R.id.save_button) {
            //Save-knappen
            save_changed_entrytext();
            save_local_to_DB();
            populateText(selectedYear, selectedMonth, selectedDayOfMonth);
            undone_count = countUndone(selectedYear);
            ButtonTexts();
        } else if (v.getId() == R.id.goto_today) {
            //Find dagen-i-dag
            save_changed_entrytext();//Gem teksten, hvis den er blevet ændret
            setPickedDayOfYear(thisYear, thisDayOfYear);
        } else if (v.getId() == R.id.markdone_button) {
            // Mark-done button
            marked_done_by_button = true;
            entrytext_changed_after_marked_done_by_button = false;
            prev_marked_done_by_button_entrytext = entrytext.getText().toString();
            ButtonTexts();
        } else if (v.getId() == R.id.prevtodo_button) {
            //prevtodo-button
            int prev_day=0;
            for (int parsed_days : todo_day_of_years) {
                if (parsed_days<selectedDayOfYear) prev_day=parsed_days;
            }
            if (prev_day !=0) {
                setPickedDayOfYear(selectedYear, prev_day);
            }
            ButtonTexts();
        } else if (v.getId() == R.id.nexttodo_button) {
            //nexttodo-button
            int next_day=0;
            for (int parsed_days : todo_day_of_years) {
                if (next_day==0 && parsed_days>selectedDayOfYear) next_day=parsed_days;
            }
            if (next_day !=0) {
                setPickedDayOfYear(selectedYear, next_day);
            }
            ButtonTexts();
        }
    }

    @Override
    public void onBackPressed() {
        // onPause aktiveres af mange andre ting end egentlig nedlukning af appen
        // denne onBackPressed samler op, når onPause er aktiveret for at stoppe programmet helt
        if (!loadingFlag) {
            if (authenticated) {
                save_changed_entrytext();
                delete_readonly_db();
                endingFlag = true;
                if (changes) {
                    save_local_to_DB();
                } else {
                    deletelocalfiles();
                    super.onBackPressed();
                }
            } else {
                super.onBackPressed();
            }
        }
    }

    public void openChangeLocalCopyFolderIntent() {
        Intent intent = new Intent(this, BrowseFiles.class);
        intent.putExtra("local_backupfiles_path", local_backupfiles_path);
        startActivityForResult(intent, activity_requestCode_browsefiles);
    }


    protected void confirm_pause() {
        // Hvis der har været problemer med at gemme database filen, så skal brugeren bekræfte,
        // at der skal lukkes
        Resources res = getResources();
        @SuppressWarnings("unused")
        AlertDialog alertbox = new AlertDialog.Builder(this)
                .setTitle(res.getString(R.string.upload_error_heading))
                .setMessage(res.getString(R.string.upload_error_intro))
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
        // (dvs. JumbleActivity, RawView og BrowseFiles)
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
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
                boolean changed_before = parseChangedYears();
                if (!changed_before) changed_years.add(selectedYear);
                String returneret_result=data.getStringExtra("returningresult");
                //Resultatet gemmes lokalt
                File raw_db_write = new File(filePath, db_name_full);
                try {
                    BufferedWriter bw = new BufferedWriter
                            (new OutputStreamWriter(new FileOutputStream(raw_db_write),encoding));
                    bw.write(returneret_result.replaceAll("\n", endline));
                    bw.close();
                } catch (IOException e) {
                    // Auto-generated catch block
                    e.printStackTrace();
                }
            }
            populateText(selectedYear, selectedMonth, selectedDayOfMonth);
            ButtonTexts();
        }
    }

    private void save_local_to_DB() {
        // Uploade de database-filer, der er blevet ændret i til Dropbox
        if (changed_years.size()>0) {
            for(int i=0;i<changed_years.size();i++) {
                setDBNames(changed_years.get(i));
                String tmpFileName = db_name_full;
                if (changed_years.get(i)==0) {
                    //Gemme jumble-file
                    tmpFileName = jumbleFileName;
                }
                UploadLocaltoDB(tmpFileName, changed_years.get(i));
            }
        }
        setDBNames(selectedYear);
    }

    private void UploadLocaltoDB(String db_name_full, final int yearToSave) {
        // Selve forbindelsesinterfacet til Dropbox (kalder DBUpload.java)
        if (accessToken != null) {
            File file = new File(filePath, db_name_full);
            final ProgressDialog dialog = new ProgressDialog(this);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setCancelable(false);
            dialog.setMessage(getString(R.string.saving));
            dialog.show();
            DBUpload dbfile = new DBUpload(dbxClient, file, new DBUpload.Callback() {
                @Override
                public void onUploadComplete(Integer result) {
                    dialog.dismiss();
                    showToast("Saved");
                    for (int j = 0; j < changed_years.size(); j++) {
                        if (changed_years.get(j) == yearToSave) {
                            changed_years.remove(j);
                        }
                    }
                    if (changed_years.size() == 0) {
                        changes = false;
                    }
                    ButtonTexts();

                    if ((endingFlag) && (!changes)) {
                        deletelocalfiles();
                        onPause();
                    }
                }

                @Override
                public void onError(Exception e) {
                    dialog.dismiss();
                    if (!endingFlag) {
                        showToast("An error has occurred in the Upload");
                    } else {
                        confirm_pause();
                    }
                }
            });
            dbfile.execute();
        } else {
            //Not authenticated => never try to upload
            showToast("Cannot upload, because not authenticated.");
        }
    }

    private void DownloadDBtoLocal(final int year, final String filename) {
        // Hente en databasefil fra Dropbox til det lokale medie (kalder DBDownload.java)
        loadingFlag = true;
        lockScreenOrientation();
        date_picked.setSelectionMode(SELECTION_MODE_NONE);
        if (accessToken != null) {
            final File file = new File(filePath, filename);
            final ProgressDialog dialog = new ProgressDialog(this);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setCancelable(false);
            dialog.setMessage(getString(R.string.loading));
            dialog.show();
            if (dbxClient != null) {
                new DBDownload(dbxClient, file, new DBDownload.Callback() {
                    @Override
                    public void onDownloadComplete(File result) {
                        dialog.dismiss();
                        loadedToLocal_years.add(year);
                        authenticated = true;
                        if (year != 0) {
                            undone_count = countUndone(year);//Hvor mange undone's er der i år?
                            populateText(selectedYear, selectedMonth, selectedDayOfMonth);
                            ButtonTexts();
                        }
                        date_picked.setSelectionMode(SELECTION_MODE_SINGLE);
                        date_picked.setSelectedDate(CalendarDay.from(selectedYear, selectedMonth, selectedDayOfMonth));
                        loadingFlag = false;
                        unlockScreenOrientation();
                        if (year == 0) {
                            openJumbleActivity(file);
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        dialog.dismiss();
                        String[] eParts = e.toString().split("--");
                        if (Integer.parseInt(eParts[1]) == 1) {
                            //FilenotFound -- create
                            if (year != 0) {
                                //Not jumblefile
                                int writeDBResult = CreateDB.WriteDB(year, filename, date_flag, endline, encoding, filePath);
                                if (writeDBResult == 0) {
                                    loadedToLocal_years.add(year);
                                    date_marker = create_date_marker(selectedYear, selectedMonth, selectedDayOfMonth);
                                    authenticated = true;
                                    changes = true;
                                    changed_years.add(year);
                                    undone_count = countUndone(year);//Hvor mange undone's er der i år?
                                    populateText(selectedYear, selectedMonth, selectedDayOfMonth);
                                    ButtonTexts();
                                    loadingFlag = false;
                                    unlockScreenOrientation();
                                    date_picked.setSelectionMode(SELECTION_MODE_SINGLE);
                                    date_picked.setSelectedDate(CalendarDay.from(selectedYear, selectedMonth, selectedDayOfMonth));
                                } else {
                                    //Error creating new DB => you have serious trouble.
                                    showToast("An error has occurred:\n Couldn't create new database.");
                                    authenticated = false;
                                }
                            } else {
                                //Create jumblefile
                                File jumblefile = new File(filePath, jumbleFileName);
                                openJumbleActivity(jumblefile);
                                loadedToLocal_years.add(year);
                            }
                        } else if (Integer.parseInt(eParts[1]) == 4) {
                            //Serious filetroubles
                            showToast("An error has occurred:\n" + eParts[2]);
                            authenticated = false;
                        } else {
                            //Bøvl med netværksforbindelse/serveradgang
                            //Check om der ligger en lokal kopi
                            showToast("Bad network connection, or Dropbox server down:\n" + eParts[2]);
                            authenticated = false;
                            externalReadWritePermissionCheckFlag = 4; //loading from External path
                            if (permissionCheck()) {
                                String localcopyfoldername = local_backupfiles_path + "/" + local_backupfiles_endfolder;
                                File keeplocalcopy_file = new File(localcopyfoldername, filename);
                                if (keeplocalcopy_file.exists()) {
                                    copyFile(keeplocalcopy_file, file);
                                    current_db_readonly = selectedYear;
                                    authenticated = true;
                                    showToast("Showing latest copy from local storage (readonly)");
                                    entrytext.getBackground().setAlpha(51);//vis "Read Only" skiltet i edittext
                                    populateText(selectedYear, selectedMonth, selectedDayOfMonth);
                                }
                                if (filename.equals(jumbleFileName)) {
                                    jumble_readonly = true;
                                }
                            }
                        }
                        loadingFlag = false;
                        unlockScreenOrientation();
                    }
                }).execute();
            }
        } else {
            showToast("Not authenticated with a Dropbox account. Cannot download.");
            authenticated = false;
        }
    }

    private void openJumbleActivity(File jumblefile) {
        Intent jumble = new Intent(this, JumbleActivity.class);
        String jumbletext="";
        try {
            BufferedReader jumble_br = new BufferedReader
                    (new InputStreamReader(new FileInputStream(jumblefile), encoding));
            StringBuilder jumbletxt = new StringBuilder();
            String thisLine;
            while ((thisLine = jumble_br.readLine()) != null) {
                jumbletxt.append(thisLine).append("\n");
            }
            jumble_br.close();
            jumbletext = jumbletxt.toString();
        } catch (IOException e) {
            // Auto-generated catch block
            e.printStackTrace();
        }
        jumble.putExtra("jumbletext", jumbletext);
        jumble.putExtra("jumble_ro", jumble_readonly);
        startActivityForResult(jumble, activity_requestCode_jumble);
    }

private void deletelocalfiles() {
        // Slet alle lokale filer. Hvis local_backup=true, så gemmes også en backupfil lokalt
        String localcopyfoldername = local_backupfiles_path + "/" + local_backupfiles_endfolder;
        File keeplocalcopy_folder = new File(localcopyfoldername);
        if (!keeplocalcopy_folder.exists()) {
            boolean mkdirResult = keeplocalcopy_folder.mkdir();
            if (!mkdirResult) showToast("Trouble creating folder");
        }
        File deletefolder = new File(filePath);
        if (deletefolder.isDirectory()) {
            String[] children = deletefolder.list();
            for (String aChildren : children) {
                externalReadWritePermissionCheckFlag = 3;
                if (local_backup && permissionCheck()) {
                    copyFile(new File(deletefolder, aChildren), new File(keeplocalcopy_folder, aChildren));
                }
                boolean deleteResult = new File(deletefolder, aChildren).delete();
                if (!deleteResult) showToast("Trouble deleting folder");
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
                boolean deleteResult = delete_ro_db_file.delete();
                if (!deleteResult) showToast("Trouble deleting file");
            }
            current_db_readonly = 0;
            if (jumble_readonly) {
                File delete_jumble_file = new File(filePath, jumbleFileName);
                if (delete_jumble_file.exists()) {
                    boolean deleteResult = delete_jumble_file.delete();
                    if (!deleteResult) showToast("Trouble deleting file");
                }
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
            inChannel.close();
            outChannel.close();
        } catch (IOException e) {
            // Auto-generated catch block
            e.printStackTrace();
        }
    }

    private boolean permissionCheck() {
        int PERMISSION_EXTERNAL_READ_WRITE = 1;
        boolean permissionSuccess = false;
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            permissionSuccess = true;
        } else {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE,
                                  Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_EXTERNAL_READ_WRITE
            );
        }
        return permissionSuccess;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        final int PERMISSION_EXTERNAL_READ_WRITE = 1;
        switch (requestCode) {
            case PERMISSION_EXTERNAL_READ_WRITE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (externalReadWritePermissionCheckFlag == 1) { // Toogle localCopy
                        local_backup = !local_backup;
                    } else if (externalReadWritePermissionCheckFlag == 2) { // Change localCopyFolder
                        openChangeLocalCopyFolderIntent();
                    } else if (externalReadWritePermissionCheckFlag == 3) { // Saving local Copy
                        showToast("Could not save locally due to lacking permissions!");
                    } else if (externalReadWritePermissionCheckFlag == 4) { // Loading local Copy
                        showToast("Could not load local copy due to lacking permissions!");
                    }
                } else {
                    local_backup = false;
                    if (externalReadWritePermissionCheckFlag == 3) { // Saving local Copy
                        showToast("Could not save locally due to lacking permissions!");
                    } else if (externalReadWritePermissionCheckFlag == 4) { // Loading local Copy
                        showToast("Could not load local copy due to lacking permissions!");
                    }
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }















    private void ButtonTexts() {
        //Opdatere udseendet af de forskellige knapper o.lign.
        //*************** SaveButton
        save_button.setEnabled(changes || entrytext_changed || marked_done_by_button);

        //*************** TodayButton
        //Grey hvis vi allerede er på idag
        goto_today.setEnabled(!(selectedDayOfYear == thisDayOfYear && selectedYear == thisYear));

        //*************** Entrytext: Readonly-mode?
        if (current_db_readonly != 0|| !authenticated) {
            entrytext.setFocusable(false);
        } else {
            entrytext.setFocusableInTouchMode(true);
        }
        if (enable_todo) {
            //For hele Todoknapperiet: Skal kun vises, hvis enable_todo
            //*************** MarkDoneButton
            markdone_button.setVisibility(View.VISIBLE);
            if ((marked_todo_in_file && !marked_done_by_button) || entrytext_changed_after_marked_done_by_button) {
                markdone_button.setText(R.string.mark_done);
                markdone_button.setTextColor(Color.rgb(128,0,0));
                markdone_button.setEnabled(authenticated && current_db_readonly==0); //tændt hvis authenticated og visningen ikke er RO
            } else if (marked_done_by_button && marked_todo_in_file || marked_done_by_button && entrytext_changed) {
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
            if (todo_day_of_years.isEmpty()) {
                prevtodo_button.setEnabled(false);
                nexttodo_button.setEnabled(false);
            } else {
                if (selectedDayOfYear > todo_day_of_years.get(0)) {
                    prevtodo_button.setEnabled(true);
                } else {
                    prevtodo_button.setEnabled(false);
                }
                if (selectedDayOfYear < todo_day_of_years.get(todo_day_of_years.size()-1)) {
                    nexttodo_button.setEnabled(true);
                } else {
                    nexttodo_button.setEnabled(false);
                }
            }

            //*************** Todocounter
            todocount.setVisibility(View.VISIBLE);
            if (undone_count!=-1) {
                if (selectedYear <= thisYear) {
                    todocount.setEnabled(true);
                    int temp_counter = undone_count;
                    if ((selectedYear < thisYear) || (selectedDayOfYear < thisDayOfYear)) {
                        if (marked_todo_in_file && marked_done_by_button) {
                            temp_counter = temp_counter - 1;
                        }
                    }
                    String tmpString = getString(R.string.leftovers) + " "+Integer.toString(selectedYear)+
                            ":\n"+Integer.toString(temp_counter)+" "+ getString(R.string.entries);
                    todocount.setText(tmpString);
                } else {
                    todocount.setEnabled(false);
                    String tmpString = getString(R.string.leftovers) + " "+Integer.toString(selectedYear)+
                            ":\n" + getString(R.string.emptyTodoCounter);
                    todocount.setText(tmpString);
                }
            }
        } else {
            //Todoknapperne skjules, hvis todofunktionaliteten er slået fra
            markdone_button.setVisibility(View.INVISIBLE);
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
        return date_flag + " " +
                parseDayName + " " +
                parseDays.get(Calendar.YEAR) + "-" +
                String.format(Locale.US, "%02d", (parseDays.get(Calendar.MONTH) + 1)) + "-" +
                String.format(Locale.US, "%02d", parseDays.get(Calendar.DAY_OF_MONTH));
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
            StringBuilder date_text = new StringBuilder();
            String thisLine;
            while ((thisLine = br.readLine()) != null) {
                if (!date_position_in_text_found) {
                    count_date_position_in_text = count_date_position_in_text + thisLine.length() + 1;
                }
                if (inside_date) {
                    if (thisLine.contains(date_flag)) {
                        inside_date = false;
                    } else {
                        date_text.append(thisLine).append("\n");
                    }
                }
                if(thisLine.contains(date_marker)) {
                    date_position_in_text_found = true;
                    marked_todo_in_file = thisLine.contains(todo_flag_sign);
                    inside_date = true;
                }
            }
            br.close();
            prev_entrytext = date_text.toString();
            prev_marked_done_by_button_entrytext = date_text.toString();
            entrytext.setText(date_text.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void change_year(int new_year) {
        setDBNames(new_year);
        undone_count = countUndone(new_year);
        ButtonTexts();
        boolean loaded_before = parseLoadedYears(new_year);
        if (!loaded_before) {
            DownloadDBtoLocal(new_year, db_name_full);
        }
    }

    private boolean parseLoadedYears(int year) {
        // Løb igennem de ændrede database-filer, for at se om det nuværende årstal har været ændret før
        boolean loaded_before = false;
        if (loadedToLocal_years.size()>0) {
            for(int i=0;i<loadedToLocal_years.size();i++) {
                if (loadedToLocal_years.get(i).equals(year)) {
                    loaded_before = true;
                }
            }
        }
        return loaded_before;
    }

    private boolean parseChangedYears() {
        // Løb igennem de ændrede database-filer, for at se om det nuværende årstal har været ændret før
        boolean changed_before = false;
        if (changed_years.size()>0) {
            for(int i=0;i<changed_years.size();i++) {
                if (changed_years.get(i).equals(selectedYear)) {
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
            if (currentyear < thisYear) {
                while ((thisLine = br.readLine()) != null) {
                    if (thisLine.contains(todo_flag_sign) && thisLine.contains(date_flag)) {
                        todo_day_of_years.add(convertDateMarkerToDayOfYear(thisLine));
                        counting++;
                    }
                }
            } else if (currentyear == thisYear) {
                int currentday;
                while ((thisLine = br.readLine()) != null) {
                    if (thisLine.contains(todo_flag_sign) && thisLine.contains(date_flag)) {
                        currentday = convertDateMarkerToDayOfYear(thisLine);
                        todo_day_of_years.add(currentday);
                        if (currentday <= thisDayOfYear) {
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        return counting;
    }

    private int convertDateMarkerToDayOfYear(String thisLine) {
        // Udregne hvilken dag-på-året som den pågældende date-marker repræsenterer
        int parsedmonth;
        int parsedday;
        Calendar parseDays = Calendar.getInstance();
        parseDays.set(Calendar.YEAR, selectedYear);

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

    private void setPickedDayOfYear(int yy, int dd) {
        // Ændre datepicker til at være den valgte day-of-year
        Calendar tmpSelectedDay = Calendar.getInstance();
        tmpSelectedDay.set(Calendar.YEAR, yy);
        tmpSelectedDay.set(Calendar.DAY_OF_YEAR, dd);
        date_picked.setSelectedDate(tmpSelectedDay);
        date_picked.setCurrentDate(tmpSelectedDay);
        makeChangesOnDateChange(date_picked.getSelectedDate());
    }

    private void setDBNames(int year) {
        //lave en streng med database-filnavnet ud fra det valgte årstal
        db_name_full = db_name+year+".txt";
    }

    private void save_changed_entrytext() {
        // Gem den tekst, der ligger i entrytext til den lokale databasefil (ikke dropbox endnu)
        if (current_db_readonly == 0) {
            String current_entrytext = entrytext.getText().toString();
            if (!current_entrytext.endsWith("\n")) {
                current_entrytext = current_entrytext + "\n\n";
            }
            if (!current_entrytext.equals(prev_entrytext) || marked_done_by_button){
                //Hvis teksten er blevet ændret, skal ændringer gemmes
                changes = true;
                //Har dette årstal været ændret før? Ellers marker som ugemt
                boolean changed_before = parseChangedYears();
                if (!changed_before) changed_years.add(selectedYear);
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
                                current_entrytext = current_entrytext.split("\\n+$", 2)[0];
                                bw.write(current_entrytext.replaceAll("\n", endline).split("\\n+$", 2)[0] + endline + endline);
                                //Replace all newlines with the endline-sequence defined +
                                //...make certain that the entry ends with only one extra newline
                                // (removing all the last endlines + inserting two new ones
                            } else {
                                bw.write(thisLine + endline);
                            }
                        }
                    }
                    br.close();
                    bw.close();
                    marked_done_by_button = false;
                    if (!file_read.delete()) {
                        showToast("Problemer med adgang til det lokale drev. Prøv at genstarte enheden!");
                    }
                    if (!file_write.renameTo(file_read)) {
                        showToast("Problemer med adgang til det lokale drev. Prøv at genstarte enheden!");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @SuppressWarnings("unused")
    //Bruges ikke lige nu, men hvis engang jeg vil håndtere dårlig authentication, så.....
    private void logOut() {
        clearToken();
    }

    public void showToast(String msg) {
        //Vise kortvarige advarsler
        Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        error.show();
    }

    private void getAccessToken() {
        accessToken = getSavedTokenFromPrefs();
        if (accessToken == null) {
            Auth.startOAuth2Authentication(TodoDiary.this, getString(R.string.APP_KEY));
        } else {
            DropboxClientFactory();
        }
    }

    private void DropboxClientFactory() {
        if (dbxClient == null) {
            DbxRequestConfig requestConfig = DbxRequestConfig.newBuilder("TodoDiary").build();
            dbxClient = new DbxClientV2(requestConfig, accessToken);
        }
    }

    private String getSavedTokenFromPrefs() {
        // Hente Dropbox-nøgle fra sharedpref, hvis der tidligere har været autenticiteret
        SharedPreferences prefs = getSharedPreferences("prefs", 0);
        String key = prefs.getString("TOKEN_KEYS", null);
        if (key != null) {
            return key;
        } else {
            return null;
        }
    }

    private void storeToken(String token) {
        // Save the access key for later
        SharedPreferences prefs = getSharedPreferences("prefs", 0);
        Editor edit = prefs.edit();
        edit.putString("TOKEN_KEYS", token);
        edit.apply();
    }

    private void clearToken() {
        // Slet autentification fra sharedpref
        SharedPreferences prefs = getSharedPreferences("prefs", 0);
        Editor edit = prefs.edit();
        edit.clear();
        edit.apply();
    }

    private void lockScreenOrientation() {
        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    private void unlockScreenOrientation() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }
}
