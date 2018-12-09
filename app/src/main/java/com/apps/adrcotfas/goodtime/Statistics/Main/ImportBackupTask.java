package com.apps.adrcotfas.goodtime.Statistics.Main;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.OpenableColumns;
import android.widget.Toast;

import com.apps.adrcotfas.goodtime.Util.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

public class ImportBackupTask extends AsyncTask<Uri, Void, Boolean> {

    private WeakReference<Context> mContext;

    ImportBackupTask(Context context) {
        mContext = new WeakReference<>(context);
    }

    @Override
    protected Boolean doInBackground(Uri... uris) {
        try {
            InputStream tmpStream = mContext.get().getContentResolver().openInputStream(uris[0]);
            File tmpPath = new File(mContext.get().getFilesDir(), "tmp");
            File tempFile = File.createTempFile("import", null, tmpPath);

            String fileName = null;
            tempFile.deleteOnExit();
            FileUtils.copy(tmpStream, tempFile);

            Cursor cursor = null;
            try {
                cursor = mContext.get().getContentResolver().query(uris[0], null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }

            // Walking on thin ice but this should suffice for now
            if ((fileName != null && !fileName.contains("Goodtime")) || !FileUtils.isSQLite3File(tempFile)) {
                return false;
            }

            FileInputStream inStream = new FileInputStream(tempFile);
            File destinationPath = mContext.get().getDatabasePath("goodtime-db");
            FileUtils.copy(inStream, destinationPath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    //TODO: extract strings
    @Override
    protected void onPostExecute(Boolean result) {
        Toast.makeText(mContext.get(), result ? "Import successful" : "Import failed", Toast.LENGTH_SHORT).show();
    }
}