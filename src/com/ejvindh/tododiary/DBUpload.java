package com.ejvindh.tododiary;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import android.content.Context;
import android.os.AsyncTask;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.UploadRequest;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxFileSizeException;
import com.dropbox.client2.exception.DropboxIOException;
import com.dropbox.client2.exception.DropboxParseException;
import com.dropbox.client2.exception.DropboxPartialFileException;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.exception.DropboxUnlinkedException;

public class DBUpload extends AsyncTask<Void, Long, Integer> {
	
    private DropboxAPI<?> mApi;
    private String mPath;
    private File mFile;
    private UploadRequest mRequest;
    private String mErrorMsg;
    //private Context context;
    private int return_int=0;

    public DBUpload(Context context, DropboxAPI<?> api, String dropboxPath, File file) {
        //this.context = context.getApplicationContext();
        mApi = api;
        mPath = dropboxPath;
        mFile = file;
    }

    @Override
	protected void onPreExecute() {
		super.onPreExecute();
		//TODO: Det virker alt sammen, men det kommer for sent frem...
		//DBTodo.ejvind.show();
	    //Toast.makeText(context, "Gemmer saa meget",
	    //            Toast.LENGTH_LONG).show();
		//Log.d("ejvindh", "xxxx preexecute nu gemmer vi");
	}

	@Override
    protected Integer doInBackground(Void... params) {
        try {
        	FileInputStream fis = new FileInputStream(mFile);
            mRequest = mApi.putFileOverwriteRequest(mPath, fis, mFile.length(), null);
            if (mRequest != null) {
                mRequest.upload();
                //Succesfuld upload => return 0
                return return_int;
            }
        } catch (DropboxUnlinkedException e) {
            // This session wasn't authenticated properly or user unlinked
            mErrorMsg = "This app wasn't authenticated properly.";
            return_int=1;
        } catch (DropboxFileSizeException e) {
            // File size too big to upload via the API
            mErrorMsg = "This file is too big to upload";
            return_int=2;
        } catch (DropboxPartialFileException e) {
            // We canceled the operation
            mErrorMsg = "Upload canceled";
            return_int=3;
        } catch (DropboxServerException e) {
            // Server-side exception.  These are examples of what could happen,
            // but we don't do anything special with them here.
            if (e.error == DropboxServerException._401_UNAUTHORIZED) {
                // Unauthorized, so we should unlink them.  You may want to
                // automatically log the user out in this case.
                return_int=4;
            } else if (e.error == DropboxServerException._403_FORBIDDEN) {
                // Not allowed to access this
                return_int=5;
            } else if (e.error == DropboxServerException._404_NOT_FOUND) {
                // path not found (or if it was the thumbnail, can't be
                // thumbnailed)
                return_int=6;
            } else if (e.error == DropboxServerException._507_INSUFFICIENT_STORAGE) {
                // user is over quota
                return_int=7;
            } else {
                // Something else
                return_int=8;
            }
            // This gets the Dropbox error, translated into the user's language
            mErrorMsg = e.body.userError;
            if (mErrorMsg == null) {
                mErrorMsg = e.body.error;
            }
        } catch (DropboxIOException e) {
            // Happens all the time, probably want to retry automatically.
            mErrorMsg = "Network error.  Try again.";
            return_int=9;
        } catch (DropboxParseException e) {
            // Probably due to Dropbox server restarting, should retry
            mErrorMsg = "Dropbox error.  Try again.";
            return_int=10;
        } catch (DropboxException e) {
            // Unknown error
            mErrorMsg = "Unknown error.  Try again.";
            return_int=11;
        } catch (FileNotFoundException e) {
            return_int=12;
        } 
        return return_int;
    }

	
}
