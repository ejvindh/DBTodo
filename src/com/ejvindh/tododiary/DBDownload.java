package com.ejvindh.tododiary;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.DropboxFileInfo;
import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxIOException;
import com.dropbox.client2.exception.DropboxParseException;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.exception.DropboxUnlinkedException;

public class DBDownload extends AsyncTask<Void, Long, Integer> {
	
	private DropboxAPI<?> mApi;
	private String mPath;
	private File mFile;
	private long mFileLen;
	private DropboxFileInfo mRequest;
	private Context mContext;
	private final ProgressDialog mDialog;
	private String mErrorMsg;
	
	public DBDownload(Context context, DropboxAPI<?> api, String dropboxPath,
	        File file) {
		mContext = context.getApplicationContext();
	    mFileLen = file.length();
	    mApi = api;
	    mPath = dropboxPath;
	    mFile = file;
	    mDialog = new ProgressDialog(context);
	    mDialog.setMax(100);
	    mDialog.setMessage("Downloading " + file.getName());
	    mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	    mDialog.setProgress(0);
	    mDialog.show();
	}
	
	@Override
	protected Integer doInBackground(Void... params) {
		int return_int = 0;
	    try {
	        FileOutputStream fis = new FileOutputStream(mFile);
	        mRequest = mApi.getFile(mPath, null, fis, new ProgressListener() {
	            @Override
	            public long progressInterval() {
	                // Update the progress bar every half-second or so
	                return 500;
	            }
	            @Override
	            public void onProgress(long bytes, long total) {
	                publishProgress(bytes);
	            }
	        });

	        if (mRequest != null) {
	            return return_int;
	        }

	    } catch (DropboxUnlinkedException e) {
	        // This session wasn't authenticated properly or user unlinked
	        mErrorMsg = "This app wasn't authenticated properly.";
	        return_int = 1;
	    } catch (DropboxServerException e) {
	        // Server-side exception.  These are examples of what could happen,
	        // but we don't do anything special with them here.
	        if (e.error == DropboxServerException._401_UNAUTHORIZED) {
		        return_int = 2;
	            // Unauthorized, so we should unlink them.  You may want to
	            // automatically log the user out in this case.
		        mErrorMsg = "Not authorized for Dropbox Access";
	        } else if (e.error == DropboxServerException._403_FORBIDDEN) {
		        return_int = 3;
	            // Not allowed to access this
		        mErrorMsg = "Access to Dropbox forbidden";
	        } else if (e.error == DropboxServerException._404_NOT_FOUND) {
	            // path not found (or if it was the thumbnail, can't be
	            // thumbnailed)
		        return_int = 4;
		        mErrorMsg = "File not found";
		    	Log.d("ejvindh", "-----notfound1");
	        } else {
		        mErrorMsg = "Dropbox server errror";
		        return_int = 6;
	            // Something else
	        }
	        // This gets the Dropbox error, translated into the user's language
	        mErrorMsg = e.body.userError;
	        if (mErrorMsg == null) {
	            mErrorMsg = e.body.error;
	        }
	    } catch (DropboxIOException e) {
	        // Happens all the time, probably want to retry automatically.
	        mErrorMsg = "Network error.  Try again.";
	        return_int = 7;
	    } catch (DropboxParseException e) {
	        // Probably due to Dropbox server restarting, should retry
	        mErrorMsg = "Dropbox error.  Try again.";
	        return_int = 8;
	    } catch (DropboxException e) {
	        // Unknown error
	        mErrorMsg = "Unknown error.  Try again.";
	        return_int = 9;
	    } catch (FileNotFoundException e) {
	        return_int = 10;
	    }
	    return return_int;
	}

	@Override
	protected void onProgressUpdate(Long... progress) {
	    int percent = (int)(100.0*(double)progress[0]/mFileLen + 0.5);
	    mDialog.setProgress(percent);
	}

	@Override
	protected void onPostExecute(Integer result) {
	    mDialog.dismiss();
	    if (result==0 || result == 4) {
	        //showToast("Database succesfully downloaded");
	        //showToast("File not found"); //men laves i så fald i CreateDB...
	    } else {
	        showToast(mErrorMsg);
	    }
	}

	private void showToast(String msg) {
	    Toast error = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
	    error.show();
	}
}
