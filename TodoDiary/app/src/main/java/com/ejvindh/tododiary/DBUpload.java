package com.ejvindh.tododiary;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import android.os.AsyncTask;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.WriteMode;

class DBUpload extends AsyncTask<Object, Object, Integer> {

    private final DbxClientV2 mDbxClient;
    private final Callback mCallback;
    private Exception mException;
    private File mFile;

    interface Callback {
        void onUploadComplete(Integer result);
        void onError(Exception e);
    }

    DBUpload(DbxClientV2 dbxClient, File file, Callback callback) {
        mDbxClient = dbxClient;
        mFile = file;
        mCallback = callback;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }


    @Override
    protected Integer doInBackground(Object... params) {
        int successvalue = 0;
        try {
            FileInputStream fis = new FileInputStream(mFile);
            mDbxClient.files().uploadBuilder("/" + mFile.getName())
                    .withMode(WriteMode.OVERWRITE)
                    .uploadAndFinish(fis);
            //Succesfuld upload => return 0
        } catch (DbxException | IOException e) {
            mException = e;
            successvalue = 1;
        }
        return successvalue;
    }

    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        if (mException != null) {
            mCallback.onError(mException);
        } else if (result == null) {
            mCallback.onError(null);
        } else {
            mCallback.onUploadComplete(result);
        }
    }
}
