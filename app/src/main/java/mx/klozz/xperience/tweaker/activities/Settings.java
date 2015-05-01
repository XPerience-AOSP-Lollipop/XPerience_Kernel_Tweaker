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

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.*;
import android.preference.Preference.OnPreferenceChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import mx.klozz.xperience.tweaker.R;
import mx.klozz.xperience.tweaker.tabs.HideTabs;
import mx.klozz.xperience.tweaker.util.ActivityThemeChangeInterface;
import mx.klozz.xperience.tweaker.util.BootClass;
import mx.klozz.xperience.tweaker.util.Constants;
import mx.klozz.xperience.tweaker.helpers.Helpers;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

import java.io.File;
import java.util.List;

public class Settings extends PreferenceActivity implements Constants, ActivityThemeChangeInterface, OnPreferenceChangeListener {

    SharedPreferences mPreferences;
    private SwitchPreference mLightThemePref, mInitd;
    private ColorPickerPreference mWidgetBgColorPref;
    private ColorPickerPreference mWidgetTextColorPref;
    private Preference mVersion, mIntSD, mExtSD;
    private Context c = this;
    private String ver = "";
    private String det = "";

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        addPreferencesFromResource(R.xml.xkt_settings);
        setTheme();

        mLightThemePref = (SwitchPreference) findPreference("use_light_theme");

        mWidgetBgColorPref = (ColorPickerPreference) findPreference("widget_bg_color");
        mWidgetBgColorPref.setOnPreferenceChangeListener(this);
        mWidgetTextColorPref = (ColorPickerPreference) findPreference("widget_text_color");
        mWidgetTextColorPref.setOnPreferenceChangeListener(this);

        mVersion = findPreference("version_info");
        mVersion.setTitle(getString(R.string.ver) + VERSION_NUM);

        mIntSD = findPreference("int_sd");
        mExtSD = findPreference("ext_sd");
        mExtSD.setSummary(mPreferences.getString("ext_sd_path", Helpers.extSD()));
        mIntSD.setSummary(mPreferences.getString("int_sd_path", Environment.getExternalStorageDirectory().getAbsolutePath()));

        final File initd = new File(INIT_D);
        mInitd = (SwitchPreference) findPreference("boot_mode");
        if (initd.exists() && initd.isDirectory()) {
            mInitd.setSummary(INIT_D + mPreferences.getString("script_name", "99XKT"));
        } else {
            getPreferenceScreen().removePreference(mInitd);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        String key = preference.getKey();
        if (key.equals("use_light_theme")) {
            mPreferences.edit().putBoolean("theme_changed", true).commit();
            finish();
            return true;
        } else if (key.equals("licensia")) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(licensia)));
        } else if (key.equals("visible_tabs")) {
            startActivity(new Intent(this, HideTabs.class));
            return true;
        } else if (key.equals("boot_mode")) {
            if (mInitd.isChecked()) {
                LayoutInflater factory = LayoutInflater.from(this);
                final View editDialog = factory.inflate(R.layout.prop_edit_dialog, null);
                final EditText tv = (EditText) editDialog.findViewById(R.id.vprop);
                final TextView tn = (TextView) editDialog.findViewById(R.id.nprop);
                tv.setText(mPreferences.getString("script_name", "99XKT"));
                tn.setText("");
                tn.setVisibility(TextView.GONE);
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.script_name))
                        .setView(editDialog)
                        .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String s = tv.getText().toString();
                                if ((s != null) && (s.length() > 0)) {
                                    mPreferences.edit().putString("script_name", s).apply();
                                }
                                mInitd.setSummary(INIT_D + mPreferences.getString("script_name", "99XKT"));
                                new BootClass(c, mPreferences).writeScript();
                            }
                        }).create().show();
            } else {
                final StringBuilder sb = new StringBuilder();
                sb.append("mount -o rw,remount /system;\n");
                sb.append("busybox rm ").append(INIT_D).append(mPreferences.getString("script_name", "99XKT")).append(";\n");
                sb.append("mount -o ro,remount /system;\n");
                Helpers.shExec(sb, c, true);
            }
            return true;
        } else if (key.equals("int_sd")) {
            LayoutInflater factory = LayoutInflater.from(this);
            final View editDialog = factory.inflate(R.layout.prop_edit_dialog, null);
            final EditText tv = (EditText) editDialog.findViewById(R.id.vprop);
            final TextView tn = (TextView) editDialog.findViewById(R.id.nprop);
            tv.setText("");
            tn.setText(getString(R.string.info_auto_sd));

            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.pt_int_sd))
                    .setView(editDialog)
                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String s = tv.getText().toString();
                            if ((s != null) && (s.length() > 0)) {
                                if (s.endsWith("/")) {
                                    s = s.substring(0, s.length() - 1);
                                }
                                if (!s.startsWith("/")) {
                                    s = "/" + s;
                                }
                                final File dir = new File(s);
                                if (dir.exists() && dir.isDirectory() && dir.canRead() && dir.canWrite())
                                    mPreferences.edit().putString("int_sd_path", s).apply();
                            } else {
                                mPreferences.edit().remove("int_sd_path").apply();
                            }
                            mIntSD.setSummary(mPreferences.getString("int_sd_path", Environment.getExternalStorageDirectory().getAbsolutePath()));
                        }
                    }).create().show();
        } else if (key.equals("ext_sd")) {
            LayoutInflater factory = LayoutInflater.from(this);
            final View editDialog = factory.inflate(R.layout.prop_edit_dialog, null);
            final EditText tv = (EditText) editDialog.findViewById(R.id.vprop);
            final TextView tn = (TextView) editDialog.findViewById(R.id.nprop);
            tv.setText("");
            tn.setText(getString(R.string.info_auto_sd));

            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.pt_ext_sd))
                    .setView(editDialog)
                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String s = tv.getText().toString();
                            if ((s != null) && (s.length() > 0)) {
                                if (s.endsWith("/")) {
                                    s = s.substring(0, s.length() - 1);
                                }
                                if (!s.startsWith("/")) {
                                    s = "/" + s;
                                }
                                final File dir = new File(s);
                                if (dir.exists() && dir.isDirectory() && dir.canRead() && dir.canWrite())
                                    mPreferences.edit().putString("ext_sd_path", s).apply();
                            } else {
                                mPreferences.edit().remove("ext_sd_path").apply();
                            }
                            mExtSD.setSummary(mPreferences.getString("ext_sd_path", Helpers.extSD()));
                        }
                    }).create().show();
        } else if (key.equals("br_op")) {
            startActivity(new Intent(this, BackupRestore.class));
        } else if (key.equals("version_info")) {
        } else if (key.equals("pref_donate")) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(PAYPAL)));
        }
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mWidgetBgColorPref) {
            String hex = ColorPickerPreference.convertToARGB(Integer.parseInt(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            final SharedPreferences.Editor editor = mPreferences.edit();
            editor.putInt(PREF_WIDGET_BG_COLOR, intHex);
            editor.commit();
            Helpers.updateAppWidget(this);
            return true;
        } else if (preference == mWidgetTextColorPref) {
            String hex = ColorPickerPreference.convertToARGB(Integer.parseInt(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            final SharedPreferences.Editor editor = mPreferences.edit();
            editor.putInt(PREF_WIDGET_TEXT_COLOR, intHex);
            editor.commit();
            Helpers.updateAppWidget(this);
            return true;
        }
        return false;
    }

    @Override
    public boolean isThemeChanged() {
        final boolean is_light_theme = mPreferences.getBoolean(PREF_USE_LIGHT_THEME, false);
        return is_light_theme != mLightThemePref.isChecked();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void setTheme() {
        final boolean is_light_theme = mPreferences.getBoolean(PREF_USE_LIGHT_THEME, false);
        setTheme(is_light_theme ? R.style.Theme_Light : R.style.Theme_Dark);
       
    }

}
