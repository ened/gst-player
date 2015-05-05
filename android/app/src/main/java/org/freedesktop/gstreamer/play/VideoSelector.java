package org.freedesktop.gstreamer.play;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;


public class VideoSelector extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private ListView videoList;
    VideoAdapter adapter;
    boolean sortByName = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_selector);

        videoList = (ListView) findViewById(R.id.videoList);
        adapter = new VideoAdapter(this);
        videoList.setAdapter(adapter);
        videoList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, final long id) {
                final String videoPath = adapter.getVideoPath(position);
                if (!TextUtils.isEmpty(videoPath)) {
                    Intent intent = new Intent(VideoSelector.this, Play.class);
                    intent.setData(Uri.parse("file://" + videoPath));
                    startActivity(intent);
                }
            }
        });

        getSupportLoaderManager().initLoader(1, null, this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_video_selector, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            return true;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_open_url) {
            final EditText input = new EditText(this);
            new AlertDialog.Builder(VideoSelector.this, R.style.GstPlayAppDialogTheme)
                    .setTitle(R.string.action_open_url)
                    .setMessage(getString(R.string.open_url_prompt))
                    .setView(input)
                    .setPositiveButton(R.string.ok,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            Editable value = input.getText();
                            try {
                                Intent intent = new Intent(VideoSelector.this, Play.class);
                                intent.setData(Uri.parse(value.toString()));
                                startActivity(intent);
                            } catch (Exception e) {
                                Toast.makeText(VideoSelector.this, R.string.invalid_url, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Do nothing.
                }
            }).show();

        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_sort) {
            sortByName = !sortByName;
            getSupportLoaderManager().restartLoader(1, null, this);
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (sortByName) {
            return new CursorLoader(this, MediaStore.Video.Media.getContentUri("external"), null, null, null,
                    "UPPER(" + MediaStore.Video.Media.DATA + ")");
        } else {
            return new CursorLoader(this, MediaStore.Video.Media.getContentUri("external"), null, null, null,
                    "UPPER(" + MediaStore.Video.Media.DISPLAY_NAME + ")");
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


    class VideoAdapter extends SimpleCursorAdapter {
        public VideoAdapter(Context context) {
            super(context, android.R.layout.simple_list_item_2, null,
                    new String[]{MediaStore.Video.Media.DISPLAY_NAME, MediaStore.Video.Media.DATA},
                    new int[]{android.R.id.text1, android.R.id.text2}, 0);
        }

        @Override
        public long getItemId(int position) {
            final Cursor cursor = getCursor();
            if (cursor.getCount() == 0 || position >= cursor.getCount()) {
                return 0;
            }
            cursor.moveToPosition(position);

            return cursor.getLong(0);
        }

        public String getVideoTitle(int position) {
            final Cursor cursor = getCursor();
            if (cursor.getCount() == 0) {
                return "";
            }
            cursor.moveToPosition(position);

            return cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));
        }

        public String getVideoPath(int position) {
            final Cursor cursor = getCursor();
            if (cursor.getCount() == 0) {
                return "";
            }
            cursor.moveToPosition(position);

            return cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
        }
    }
}
