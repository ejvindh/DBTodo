package com.ejvindh.tododiary;

import android.os.AsyncTask;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.GetMetadataErrorException;
import com.dropbox.core.v2.files.LookupError;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

class DBDownload extends AsyncTask<FileMetadata, Void, File> {
    private DbxClientV2 mDbxClient;
    private Exception mException;
    private final Callback mCallback;
    private File localFile;

    interface Callback {
        void onDownloadComplete(File result);
        void onError(Exception e);
    }

    DBDownload(DbxClientV2 dbxClient, File file, Callback callback) {
        mDbxClient = dbxClient;
        mCallback = callback;
        localFile = file;
    }

    @Override
    protected File doInBackground(FileMetadata... params) {
        if (localFile.exists()) {
            if (!localFile.delete()) {
                mException = new RuntimeException("No deleteAccess to : " + localFile);
            }
        }
        try {
            // Download the file.
            if (localFile.createNewFile()) {
                FileOutputStream fos = new FileOutputStream(localFile);
                mDbxClient.files().getMetadata("/" + localFile.getName());
                mDbxClient.files().download("/" + localFile.getName()).download(fos);
                return localFile;
            } else {
                mException = new RuntimeException("--4--Unable to create localfile: " + localFile.getName());
            }
        } catch (GetMetadataErrorException ex) {
            if (ex.errorValue.isPath()) {
                LookupError le = ex.errorValue.getPathValue();
                if (le.isNotFound()) {
                    mException = new RuntimeException("--1--File doesn't exist on Dropbox: " + localFile.getName());
                }
            }
        } catch (IOException e) {
            mException = new RuntimeException("--2--IoException: " + e.getClass());
        } catch (DbxException e) {
            mException = new RuntimeException("--3--DbxException: " + e.getClass());
        }
        return null;
    }

    @Override
    protected void onPostExecute(File result) {
        super.onPostExecute(result);
        if (mException != null) {
            mCallback.onError(mException);
        } else {
            mCallback.onDownloadComplete(result);
        }
    }
}
