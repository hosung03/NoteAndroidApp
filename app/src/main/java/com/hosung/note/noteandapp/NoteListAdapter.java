package com.hosung.note.noteandapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.util.List;

/**
 * Created by Hosung, Lee on 2016. 12. 7..
 */

public class NoteListAdapter extends ArrayAdapter<NoteInfo> {
    public List<NoteInfo> arrNoteList = null;
    public Context mContext = null;
    public NoteListDB mDB = null;
    public Boolean isEdit = false;

    public NoteListAdapter(Context context, int resource, List<NoteInfo> arrNoteList) {
        super(context, resource, arrNoteList);
        this.arrNoteList = arrNoteList;
        mContext = context;
        mDB = new NoteListDB(context);
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.notelist_item, parent, false);
        }

        ImageView ivThumbnail = (ImageView) convertView.findViewById(R.id.ivThumbnail);
        TextView lbNote = (TextView) convertView.findViewById(R.id.lbNote);
        TextView lbLocation = (TextView) convertView.findViewById(R.id.lbLocation);
        Button btnDeleteItem = (Button) convertView.findViewById(R.id.btnDeleteItem);
        RelativeLayout rlDeleteButton = (RelativeLayout) convertView.findViewById(R.id.rlDeleteButton);

        NoteInfo noteInfo = arrNoteList.get(position);
        if (!noteInfo.getPhotofile().equals("")) {
            new LoadImageTask(ivThumbnail,noteInfo.getPhotofile()).execute();
        } else {
            ivThumbnail.setImageBitmap(null);
        }

        lbNote.setText(noteInfo.getNote());
        lbLocation.setText(noteInfo.getAddress());

        btnDeleteItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDB.deleteNoteInfo(arrNoteList.get(position)) > 0) {
                    arrNoteList.remove(position);
                    notifyDataSetChanged();
                }
            }
        });

        if(isEdit){
            rlDeleteButton.setVisibility(View.VISIBLE);
        } else {
            rlDeleteButton.setVisibility(View.GONE);
        }

        return convertView;
    }
}
