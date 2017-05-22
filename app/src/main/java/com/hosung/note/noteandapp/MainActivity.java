package com.hosung.note.noteandapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TabHost;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getSimpleName();

    public static final int NOTE_LIST = 0;

    public static List<NoteInfo> arrNoteList = null;
    public static int currentIndex = -1;
    public static String BLANK_NOTE = "(New Note)";

    public static NoteListDB db = null;

    NoteListAdapter notelistAdapter = null;
    ListView noteListView = null;
    Button btnAddItem = null;
    Button btnEditItem = null;
    Boolean isEdit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);

        db = new NoteListDB(getApplicationContext());
        arrNoteList = db.getList();

        notelistAdapter = new NoteListAdapter(this, R.layout.notelist_item, arrNoteList);
        noteListView = (ListView) findViewById(R.id.noteListView);
        noteListView.setAdapter(notelistAdapter);
        noteListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                currentIndex = i;
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra("isEditNote",true);
                startActivityForResult(intent, NOTE_LIST);
            }
        });

        btnAddItem = (Button) findViewById(R.id.btnAddItem);
        btnAddItem.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                NoteInfo noteInfo = new NoteInfo();
                noteInfo.setNote(BLANK_NOTE);
                noteInfo.setAddress("");
                arrNoteList.add(0,noteInfo);
                currentIndex = 0;

                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra("isEditNote",false);
                startActivityForResult(intent, NOTE_LIST);

                notelistAdapter.notifyDataSetChanged();
            }
        });

        btnEditItem = (Button) findViewById(R.id.btnEditItem);
        btnEditItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isEdit) {
                    isEdit = false;
                    notelistAdapter.isEdit = false;
                    btnEditItem.setText("Edit");
                } else {
                    isEdit = true;
                    notelistAdapter.isEdit = true;
                    btnEditItem.setText("Done");
                }
                notelistAdapter.notifyDataSetChanged();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(notelistAdapter != null) notelistAdapter.notifyDataSetChanged();
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static void saveTODB(){
        if(db == null || currentIndex<-1) return;

        NoteInfo noteInfo = arrNoteList.get(currentIndex);
        if(noteInfo.getNoteno()>0){
            db.updateNoteInfo(noteInfo);
        } else {
            long noteno = db.insertNoteInfo(noteInfo);
            noteInfo.setNoteno(noteno);
        }
    }
}
