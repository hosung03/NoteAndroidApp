package com.hosung.note.noteandapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.ImageView;

import java.io.File;

/**
 * Created by mac on 2016. 12. 10..
 */

class LoadImageTask extends AsyncTask<Object, Void, Bitmap> {

    private ImageView imv = null;
    private String path = null;
    private Bitmap bitmap = null;

    public Bitmap getBitmap() { return bitmap; }

    public LoadImageTask(ImageView imv, String filename) {
        this.imv = imv;
        this.path = Environment.getExternalStorageDirectory().getAbsolutePath().toString() + "/NoteAndApp/" + filename;
    }

    @Override
    protected Bitmap doInBackground(Object... params) {
        Bitmap result = null;
        if(new File(path).exists()){
            result = BitmapFactory.decodeFile(path);
        }
        return result;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        if(result != null && imv != null){
            bitmap = result;
            imv.setImageBitmap(result);
        }
    }

}