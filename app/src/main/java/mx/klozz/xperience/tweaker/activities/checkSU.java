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
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import mx.klozz.xperience.tweaker.R;
import mx.klozz.xperience.tweaker.util.ActivityThemeChangeInterface;
import mx.klozz.xperience.tweaker.util.Constants;
import mx.klozz.xperience.tweaker.helpers.Helpers;

public class checkSU extends Activity implements Constants, ActivityThemeChangeInterface {
    private boolean mIsLightTheme;
    private ProgressBar wait;
    private TextView info;
    private ImageView attn;
    SharedPreferences mPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        setTheme();
        setContentView(R.layout.check_su);
        wait = (ProgressBar) findViewById(R.id.wait);
        info = (TextView) findViewById(R.id.info);
        attn = (ImageView) findViewById(R.id.attn);

        if (mPreferences.getBoolean("booting", false)) {
            info.setText(getString(R.string.boot_wait));
            wait.setVisibility(View.GONE);
            attn.setVisibility(View.VISIBLE);
        } else {

            new TestSU().execute();
        }
    }

    private class TestSU extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            SystemClock.sleep(1000);
            final Boolean canSu = Helpers.checkSu();
            final Boolean canBb = Helpers.BinExist("busybox") != null;
            if (canSu && canBb) return "ok";
            else return "nok";
        }

        @Override
        protected void onPostExecute(String result) {

            if (result.equals("nok")) {
                info.setText(getString(R.string.su_failed_su_or_busybox));
                wait.setVisibility(View.GONE);
                attn.setVisibility(View.VISIBLE);
            } else {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("r", result);
                setResult(RESULT_OK, returnIntent);
                finish();
            }

        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
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

}
