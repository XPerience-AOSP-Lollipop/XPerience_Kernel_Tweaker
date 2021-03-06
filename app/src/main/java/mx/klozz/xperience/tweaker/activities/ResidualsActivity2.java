/*
 * XPerience Kernel Tweaker - An Android CPU Control application
 * Copyright (C) 2011-2015 Carlos "Klozz" Jesus <TeamMEX@XDA-Developers>
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package mx.klozz.xperience.tweaker.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import mx.klozz.xperience.tweaker.R;
import mx.klozz.xperience.tweaker.util.ActivityThemeChangeInterface;
import mx.klozz.xperience.tweaker.util.CMDProcessor;
import mx.klozz.xperience.tweaker.util.Constants;
import mx.klozz.xperience.tweaker.util.FileArrayAdapter;
import mx.klozz.xperience.tweaker.util.Item;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by klozz on 16.03.2015.
 */
public class ResidualsActivity2 extends Activity implements Constants, AdapterView.OnItemClickListener, ActivityThemeChangeInterface {
    SharedPreferences mPreferences;
    private boolean mIsLightTheme;
    private FileArrayAdapter adapter;
    private String rpath;
    private int ndel = 0;
    Resources res;
    private Context context = this;
    private ListView packList;
    private LinearLayout linlaHeaderProgress;
    private LinearLayout nofiles;
    private LinearLayout tools;
    private Button applyBtn;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        res = getResources();
        setTheme();
        setContentView(R.layout.residual_list);

        Intent intent1 = getIntent();
        rpath = intent1.getStringExtra("dir");

        packList = (ListView) findViewById(R.id.applist);
        packList.setOnItemClickListener(this);
        linlaHeaderProgress = (LinearLayout) findViewById(R.id.linlaHeaderProgress);
        nofiles = (LinearLayout) findViewById(R.id.nofiles);
        tools = (LinearLayout) findViewById(R.id.tools);
        applyBtn = (Button) findViewById(R.id.applyBtn);
        applyBtn.setText(getString(R.string.delallbtn));
        applyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(getString(R.string.residual_files_title))
                        .setMessage(getString(R.string.clean_files_msg, rpath))
                        .setNegativeButton(getString(R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                })
                        .setPositiveButton(getString(R.string.yes),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                //alertDialog.setCancelable(false);
                Button theButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                theButton.setOnClickListener(new CleanAllListener(alertDialog));

            }
        });
        new LongOperation().execute();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        packList.setAdapter(adapter);
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean isThemeChanged() {
        final boolean is_light_theme = mPreferences.getBoolean(PREF_USE_LIGHT_THEME, false);
        return is_light_theme != mIsLightTheme;
    }

    @Override
    public void setTheme() {
        final boolean is_light_theme = mPreferences.getBoolean(PREF_USE_LIGHT_THEME, false);
        mIsLightTheme = is_light_theme;
        setTheme(is_light_theme ? R.style.Theme_Light : R.style.Theme_Dark);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long row) {
        final Item o = adapter.getItem(position);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(getString(R.string.residual_files_title))
                .setMessage(getString(R.string.del_file_msg, o.getName()))
                .setNegativeButton(getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                .setPositiveButton(getString(R.string.yes),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        //alertDialog.setCancelable(false);
        Button theButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        theButton.setOnClickListener(new DelFileListener(alertDialog, o));

    }

    class CleanAllListener implements View.OnClickListener {
        private final Dialog dialog;

        public CleanAllListener(Dialog dialog) {
            this.dialog = dialog;
        }

        @Override
        public void onClick(View v) {
            ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
            ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(false);

            CMDProcessor.CommandResult cr = new CMDProcessor().su.runWaitFor("busybox rm -f " + rpath + "/*");
            if (cr.success()) {
                ndel += adapter.getCount();
                adapter.clear();
                linlaHeaderProgress.setVisibility(View.VISIBLE);
                tools.setVisibility(View.GONE);
            }
            Intent returnIntent = new Intent();
            returnIntent.putExtra("result", ndel);
            setResult(RESULT_OK, returnIntent);
            dialog.dismiss();
            finish();
        }
    }

    class DelFileListener implements View.OnClickListener {
        private final Dialog dialog;
        private final Item o;

        public DelFileListener(Dialog dialog, Item o) {
            this.dialog = dialog;
            this.o = o;
        }

        @Override
        public void onClick(View v) {
            ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
            ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(false);

            CMDProcessor.CommandResult cr = new CMDProcessor().su.runWaitFor("busybox rm -f " + rpath + "/" + o.getName());
            if (cr.success()) {
                adapter.remove(o);
                adapter.notifyDataSetChanged();
                ndel++;
            }

            Intent returnIntent = new Intent();
            returnIntent.putExtra("result", ndel);
            setResult(RESULT_OK, returnIntent);
            dialog.dismiss();
            if (adapter.isEmpty())
                finish();

        }
    }


    private class LongOperation extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            CMDProcessor.CommandResult cr = new CMDProcessor().su.runWaitFor("busybox find " + rpath + " -type f -name \"*\" -print0");
            if (cr.success()) {
                return cr.stdout;
            } else {
                Log.d(TAG, "residual files err: " + cr.stderr);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            final List<Item> dir = new ArrayList<Item>();
            if (result != null) {
                final String fls[] = result.split("\0");
                for (String fl : fls) {
                    final File f = new File(fl);
                    dir.add(new Item(f.getName(), f.getParent(), null, null, "file"));
                }
            }
            linlaHeaderProgress.setVisibility(View.GONE);
            if (dir.isEmpty()) {
                nofiles.setVisibility(View.VISIBLE);
                tools.setVisibility(View.GONE);
            } else {
                nofiles.setVisibility(View.GONE);
                tools.setVisibility(View.VISIBLE);
                adapter = new FileArrayAdapter(ResidualsActivity2.this, R.layout.file_item, dir);
                packList.setAdapter(adapter);
            }

        }

        @Override
        protected void onPreExecute() {
            linlaHeaderProgress.setVisibility(View.VISIBLE);
            nofiles.setVisibility(View.GONE);
            tools.setVisibility(View.GONE);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }


}
