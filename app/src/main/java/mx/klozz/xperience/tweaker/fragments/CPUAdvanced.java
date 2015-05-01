/*
 * XPerience Kernel Tweaker - An Android CPU Control application
 * Copyright (C) 2011-2015 Carlos "Klozz" Jesus <TeamMEX@XDA-Developers>
 *
 *          Copyright h0rn3t and AOKP Team
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

package mx.klozz.xperience.tweaker.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.SwitchPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import mx.klozz.xperience.tweaker.R;
import mx.klozz.xperience.tweaker.MainActivity;
import mx.klozz.xperience.tweaker.activities.ParamActivity;
import mx.klozz.xperience.tweaker.activities.Settings;
import mx.klozz.xperience.tweaker.util.CMDProcessor;
import mx.klozz.xperience.tweaker.util.Constants;
import mx.klozz.xperience.tweaker.util.GPUClass;
import mx.klozz.xperience.tweaker.helpers.Helpers;

import java.io.File;

public class CPUAdvanced extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener, Constants {

    SharedPreferences mPreferences;
    private SwitchPreference mMpdecision, mIntelliplug, mMsmHotplug, mEcomode, mGenHP, mKraitBoost,mIntellinrcores,mIntellitouchboost,mIntelliscreenoffreq;
    private Preference mHotplugset, mGpuGovset, mKraitHi, mKraitLo, mOCval;
    private ListPreference mSOmax, mSOmin, lmcps, lcpuq, lgpufmax, mKraitThres, mOClow, mOChigh,mIntelliprofiles;
    private Context context;
    private String hotpath = Helpers.hotplug_path();
    private final CharSequence[] vmcps = {"0", "1", "2"};
    private final CharSequence[] prof = {"0","1", "2", "3","4"};
    private GPUClass gpu;
    private String ps = "";
    private String ps_cpuquiet = "";
    private String app = "";
    private String ps_mc_ps = "";
    private String intelliprof="";
    private final int vstep = 12500;
    private final int vmin = 0;
    private final int nvsteps = 25;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        setHasOptionsMenu(true);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mPreferences.registerOnSharedPreferenceChangeListener(this);
        addPreferencesFromResource(R.layout.cpu_advanced);

        mSOmax = (ListPreference) findPreference("pref_so_max");
        mSOmin = (ListPreference) findPreference("pref_so_min");
        mOClow = (ListPreference) findPreference("pref_oc_low");
        mOChigh = (ListPreference) findPreference("pref_oc_high");
        mOCval = findPreference("pref_oc_val");

        mMpdecision = (SwitchPreference) findPreference("pref_mpdecision");
        mMsmHotplug = (SwitchPreference) findPreference("pref_msmhotplug");
        mIntelliplug = (SwitchPreference) findPreference("pref_intelliplug");
        mIntelliprofiles = (ListPreference) findPreference("pref_intelliprof");
        mEcomode = (SwitchPreference) findPreference("pref_ecomode");

        lmcps = (ListPreference) findPreference("pref_mcps");
        lcpuq = (ListPreference) findPreference("pref_cpuquiet");
        mHotplugset = findPreference("pref_hotplug");
        mGenHP = (SwitchPreference) findPreference("pref_hp");
        lgpufmax = (ListPreference) findPreference("pref_gpu_fmax");
        mGpuGovset = findPreference("pref_gpugov_set");

        mKraitThres = (ListPreference) findPreference("pref_krait_thres");
        mKraitBoost = (SwitchPreference) findPreference("pref_krait_boost");
        mKraitHi = findPreference("pref_krait_hi");
        mKraitLo = findPreference("pref_krait_lo");

        ps = getString(R.string.ps_so_minmax);
        ps_cpuquiet = getString(R.string.ps_cpuquiet);
        ps_mc_ps = getString(R.string.ps_mc_ps);
        intelliprof = getString(R.string.intelli_plugprof_sum);
        app = getString(R.string.app_name);

        final CharSequence[] entries = MainActivity.mAvailableFrequencies;

        if (!new File(SO_MAX_FREQ).exists() || !new File(SO_MIN_FREQ).exists()) {
            PreferenceCategory hideCat = (PreferenceCategory) findPreference("so_min_max");
            getPreferenceScreen().removePreference(hideCat);
        } else {
            mSOmax.setEntries(entries);
            mSOmax.setEntryValues(entries);
            mSOmin.setEntries(entries);
            mSOmin.setEntryValues(entries);
            final String readsomax = Helpers.LeerUnaLinea(SO_MAX_FREQ);
            final String readsomin = Helpers.LeerUnaLinea(SO_MIN_FREQ);
            mSOmax.setValue(readsomax);
            mSOmin.setValue(readsomin);
            mSOmax.setSummary(ps + readsomax + " kHz");
            mSOmin.setSummary(ps + readsomin + " kHz");
        }

        //live OC
        if (!new File(OC_VALUE_PATH).exists()) {
            PreferenceCategory hideCat = (PreferenceCategory) findPreference("oc_live");
            getPreferenceScreen().removePreference(hideCat);
        } else {
            final String voc = Helpers.LeerUnaLinea(OC_VALUE_PATH);
            mOCval.setSummary(ps + voc);

            if (!new File(OC_HIGH_PATH).exists()) {
                PreferenceCategory hideCat = (PreferenceCategory) findPreference("oc_live");
                hideCat.removePreference(mOChigh);
            } else {
                mOChigh.setEntries(entries);
                mOChigh.setEntryValues(entries);
                final String readsomax = Helpers.LeerUnaLinea(OC_HIGH_PATH);
                mOChigh.setValue(readsomax);
                mOChigh.setSummary(ps + readsomax + " kHz");

            }
            if (!new File(OC_LOW_PATH).exists()) {
                PreferenceCategory hideCat = (PreferenceCategory) findPreference("oc_live");
                hideCat.removePreference(mOClow);
            } else {
                mOClow.setEntries(entries);
                mOClow.setEntryValues(entries);
                final String readsomin = Helpers.LeerUnaLinea(OC_LOW_PATH);
                mOClow.setValue(readsomin);
                mOClow.setSummary(ps + readsomin + " kHz");
            }
        }
        //----

        if (Helpers.BinExist("mpdecision") == null) {
            PreferenceCategory hideCat = (PreferenceCategory) findPreference("mpdecision");
            getPreferenceScreen().removePreference(hideCat);
        } else {
            Boolean mpdon = Helpers.ModuleisActive("mpdecision");
            mMpdecision.setChecked(mpdon);
            mPreferences.edit().putBoolean("mpdecision", mpdon).apply();
        }
        if (!new File(MSM_HOTPLUG).exists()) {
            PreferenceCategory hideCat = (PreferenceCategory) findPreference("msmhotplug");
            getPreferenceScreen().removePreference(hideCat);
        } else {
            mMsmHotplug.setChecked(Helpers.LeerUnaLinea(MSM_HOTPLUG).equals("1"));
        }
        if (!new File(INTELLI_PLUG).exists()) {
            PreferenceCategory hideCat = (PreferenceCategory) findPreference("intelliplug");
            getPreferenceScreen().removePreference(hideCat);
        } else {
            mIntelliplug.setChecked(Helpers.LeerUnaLinea(INTELLI_PLUG).equals("1"));
        }
        if (!new File(INTELLI_PROFILES).exists()) {
            PreferenceCategory hideCat = (PreferenceCategory) findPreference("intelli_prof");
            getPreferenceScreen().removePreference(hideCat);
        } else {
            mIntelliprofiles.setEntries(getResources().getStringArray(R.array.intelli_profiles));
            mIntelliprofiles.setEntryValues(getResources().getStringArray(R.array.intelli_profiles_values));
            mIntelliprofiles.setValue(Helpers.LeerUnaLinea(INTELLI_PROFILES));
            mIntelliprofiles.setSummary(intelliprof + mIntelliprofiles.getEntry().toString());
        }
        if (hotpath == null) {
            PreferenceCategory hideCat = (PreferenceCategory) findPreference("hotplugset");
            getPreferenceScreen().removePreference(hideCat);
        }
        if (!new File(ECO_MODE).exists()) {
            PreferenceCategory hideCat = (PreferenceCategory) findPreference("ecomode");
            getPreferenceScreen().removePreference(hideCat);
        } else {
            mEcomode.setChecked(Helpers.LeerUnaLinea(ECO_MODE).equals("1"));
        }
        if (!new File(MC_PS).exists()) {
            PreferenceCategory hideCat = (PreferenceCategory) findPreference("mc_ps");
            getPreferenceScreen().removePreference(hideCat);
        } else {
            lmcps.setEntries(getResources().getStringArray(R.array.mc_ps_array));
            lmcps.setEntryValues(vmcps);
            lmcps.setValue(Helpers.LeerUnaLinea(MC_PS));
            lmcps.setSummary(ps_mc_ps + lmcps.getEntry().toString());
        }
        if (!new File(GEN_HP).exists()) {
            PreferenceCategory hideCat = (PreferenceCategory) findPreference("hp");
            getPreferenceScreen().removePreference(hideCat);
        } else {
            mGenHP.setChecked(Helpers.LeerUnaLinea(GEN_HP).equals("1"));
            mGenHP.setSummary(getString(R.string.ps_hp) + " | " + getString(R.string.ps_dsync));
        }
        if (!new File(CPU_QUIET_GOV).exists()) {
            PreferenceCategory hideCat = (PreferenceCategory) findPreference("cpuq");
            getPreferenceScreen().removePreference(hideCat);
        } else {
            final String cur_cpuq_gov = Helpers.LeerUnaLinea(CPU_QUIET_GOV);
            final String s = Helpers.LeerUnaLinea(CPU_QUIET_CUR);
            final CharSequence[] govs = cur_cpuq_gov.split(" ");
            lcpuq.setEntries(govs);
            lcpuq.setEntryValues(govs);
            lcpuq.setValue(s);
            lcpuq.setSummary(ps_cpuquiet + s);
        }
        gpu = new GPUClass();
        if (gpu.gpuclk_path() == null) {
            PreferenceCategory hideCat = (PreferenceCategory) findPreference("gpu_max_clk");
            getPreferenceScreen().removePreference(hideCat);
        } else {
            lgpufmax.setEntries(gpu.gpuclk_names());
            lgpufmax.setEntryValues(gpu.gpuclk_values());
            final String s = Helpers.LeerUnaLinea(gpu.gpuclk_path());
            lgpufmax.setValue(s);
            lgpufmax.setSummary(ps + lgpufmax.getEntry());
        }

        if (gpu.gpugovset_path() == null) {
            PreferenceCategory hideCat = (PreferenceCategory) findPreference("gpugovset");
            getPreferenceScreen().removePreference(hideCat);
        }

        PreferenceCategory Cat = (PreferenceCategory) findPreference("uv_krait");
        if (!new File(KRAIT_ON_PATH).exists()) {
            getPreferenceScreen().removePreference(Cat);
        } else {

            mKraitBoost.setChecked(Helpers.LeerUnaLinea(KRAIT_ON_PATH).equalsIgnoreCase("N"));

            if (!new File(KRAIT_THRES_PATH).exists()) {
                Cat.removePreference(mKraitThres);
            } else {
                mKraitThres.setEntries(entries);
                mKraitThres.setEntryValues(entries);
                final String readthres = Helpers.LeerUnaLinea(KRAIT_THRES_PATH);
                mKraitThres.setValue(readthres);
                mKraitThres.setSummary(ps + readthres + " kHz");
            }
            if (!new File(KRAIT_HIGH_PATH).exists()) {
                Cat.removePreference(mKraitHi);
            } else {
                final String s = Helpers.LeerUnaLinea(KRAIT_HIGH_PATH);
                mKraitHi.setSummary(getString(R.string.ps_krait_hi, s));
                mPreferences.edit().putString("pref_krait_hi", s).commit();
            }
            if (!new File(KRAIT_LOWER_PATH).exists()) {
                Cat.removePreference(mKraitLo);
            } else {
                final String s = Helpers.LeerUnaLinea(KRAIT_LOWER_PATH);
                mKraitLo.setSummary(getString(R.string.ps_krait_lo, s));
                mPreferences.edit().putString("pref_krait_lo", s).commit();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.tablist:
                Helpers.getTabList(getString(R.string.menu_tab), (ViewPager) getView().getParent(), getActivity());
                break;
            case R.id.app_settings:
                Intent intent = new Intent(getActivity(), Settings.class);
                startActivity(intent);
                break;
        }
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mOCval) {
            openDialog(getString(R.string.oc_value), 80, 150, preference, OC_VALUE_PATH, "pref_oc_val");
        } else if (preference == mMpdecision) {
            if (mMpdecision.isChecked()) {
                final StringBuilder sb = new StringBuilder();
                sb.append("mpdecisionstart;\n");
                Helpers.shExec(sb, context, true);
            } else {
                new CMDProcessor().su.runWaitFor("stop mpdecision");
            }
            return true;
        } else if (preference == mHotplugset) {
            Intent i = new Intent(getActivity(), ParamActivity.class);
            i.putExtra("path", hotpath);
            i.putExtra("sob", HOTPLUG_SOB);
            i.putExtra("pref", "hotplug");
            startActivity(i);
        } else if (preference == mMsmHotplug) {
            if (Helpers.LeerUnaLinea(MSM_HOTPLUG).equals("0")) {
                new CMDProcessor().su.runWaitFor("busybox echo 1 > " + MSM_HOTPLUG);
            } else {
                new CMDProcessor().su.runWaitFor("busybox echo 0 > " + MSM_HOTPLUG);
            }
            return true;
        } else if (preference == mIntelliplug) {
            if (Helpers.LeerUnaLinea(INTELLI_PLUG).equals("0")) {
                new CMDProcessor().su.runWaitFor("busybox echo 1 > " + INTELLI_PLUG);
            } else {
                new CMDProcessor().su.runWaitFor("busybox echo 0 > " + INTELLI_PLUG);
            }
            return true;
        } else if (preference == mEcomode) {
            if (Helpers.LeerUnaLinea(ECO_MODE).equals("0")) {
                new CMDProcessor().su.runWaitFor("busybox echo 1 > " + ECO_MODE);
            } else {
                new CMDProcessor().su.runWaitFor("busybox echo 0 > " + ECO_MODE);
            }
            return true;
        } else if (preference == mGenHP) {
            if (Helpers.LeerUnaLinea(GEN_HP).equals("0")) {
                new CMDProcessor().su.runWaitFor("busybox echo 1 > " + ECO_MODE);
            } else {
                new CMDProcessor().su.runWaitFor("busybox echo 0 > " + ECO_MODE);
            }
            return true;
        } else if (preference == mGpuGovset) {
            Intent i = new Intent(getActivity(), ParamActivity.class);
            i.putExtra("path", gpu.gpugovset_path());
            i.putExtra("sob", GPU_PARAM_SOB);
            i.putExtra("pref", "gpuparam");
            startActivity(i);
        } else if (preference == mKraitBoost) {
            if (Helpers.LeerUnaLinea(KRAIT_ON_PATH).equalsIgnoreCase("N")) {
                new CMDProcessor().su.runWaitFor("busybox echo y > " + KRAIT_ON_PATH);
            } else {
                new CMDProcessor().su.runWaitFor("busybox echo n > " + KRAIT_ON_PATH);
            }
            return true;
        } else if (preference == mKraitHi) {
            final int currentProgress = Integer.parseInt(Helpers.LeerUnaLinea(KRAIT_HIGH_PATH));
            showDialog(currentProgress, vmin, vstep, nvsteps, getString(R.string.pt_krait_hi), "pref_krait_hi");
            return true;
        } else if (preference == mKraitLo) {
            final int currentProgress = Integer.parseInt(Helpers.LeerUnaLinea(KRAIT_LOWER_PATH));
            showDialog(currentProgress, vmin, vstep, nvsteps, getString(R.string.pt_krait_lo), "pref_krait_lo");
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, String key) {
        if (key.equals("pref_so_max")) {
            setlistPref(mSOmax, SO_MAX_FREQ, ps + mSOmax.getValue() + " kHz");
        } else if (key.equals("pref_so_min")) {
            setlistPref(mSOmin, SO_MIN_FREQ, ps + mSOmin.getValue() + " kHz");
        } else if (key.equals("pref_oc_low")) {
            setlistPref(mOClow, OC_LOW_PATH, ps + mOClow.getValue() + " kHz");
        } else if (key.equals("pref_oc_high")) {
            setlistPref(mOChigh, OC_HIGH_PATH, ps + mOChigh.getValue() + " kHz");
        } else if (key.equals("pref_mcps")) {
            setlistPref(lmcps, MC_PS, ps_mc_ps + lmcps.getEntry());
        } else if (key.equals("pref_intelliprof")) {
            setlistPref(mIntelliprofiles, INTELLI_PROFILES, intelliprof + mIntelliprofiles.getEntry());
        } else if (key.equals("pref_cpuquiet")) {
            setlistPref(lcpuq, CPU_QUIET_CUR, ps_cpuquiet + lcpuq.getValue());
        } else if (key.equals("pref_gpu_fmax")) {
            setlistPref(lgpufmax, gpu.gpuclk_path(), ps + lgpufmax.getEntry());

            Intent intent = new Intent(INTENT_PP);
            intent.putExtra("from", app);
            context.sendBroadcast(intent);
        } else if (key.equals("pref_krait_thres")) {
            setlistPref(mKraitThres, KRAIT_THRES_PATH, ps + mKraitThres.getValue() + " kHz");
        }

    }

    private void setlistPref(ListPreference l, String p, String s) {
        final String v = l.getValue();
        if (!v.equals(Helpers.LeerUnaLinea(p))) {
            new CMDProcessor().su.runWaitFor("busybox echo " + v + " > " + p);
        }
        l.setSummary(s);
    }

    private static int getNearestStepIndex(final int value, final int min, final int step, final int total) {
        int index = 0;
        for (int k = 0; k < total; k++) {
            if (value > (k * step + min)) index++;
            else break;
        }
        return index;
    }

    protected void showDialog(final int curval, final int min, final int step, final int total, final String titlu, final String key) {
        final LayoutInflater factory = LayoutInflater.from(context);
        final View voltageDialog = factory.inflate(R.layout.voltage_dialog, null);

        final SeekBar voltageSeek = (SeekBar) voltageDialog.findViewById(R.id.voltageSeek);
        final TextView voltageMeter = (TextView) voltageDialog.findViewById(R.id.voltageMeter);

        voltageMeter.setText(String.valueOf(curval) + " μV");
        voltageSeek.setMax(total);
        voltageSeek.setProgress(getNearestStepIndex(curval, vmin, vstep, nvsteps));
        voltageSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (fromUser) {
                    final String volt = Integer.toString(progress * step + min);
                    voltageMeter.setText(volt + " μV");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        new AlertDialog.Builder(context)
                .setTitle(titlu)
                .setView(voltageDialog)
                .setPositiveButton(getResources().getString(R.string.ps_volt_save),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.cancel();
                                final String value = Integer.toString(voltageSeek.getProgress() * step + min);
                                SharedPreferences.Editor editor = mPreferences.edit();
                                editor.putString(key, value).commit();
                                if (key.equals("pref_krait_hi")) {
                                    mKraitHi.setSummary(getString(R.string.ps_krait_hi, value));
                                    new CMDProcessor().su.runWaitFor("busybox echo " + value + " > " + KRAIT_HIGH_PATH);
                                } else if (key.equals("pref_krait_lo")) {
                                    mKraitLo.setSummary(getString(R.string.ps_krait_lo, value));
                                    new CMDProcessor().su.runWaitFor("busybox echo " + value + " > " + KRAIT_LOWER_PATH);
                                }
                            }
                        }
                )
                .setNegativeButton(getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.cancel();
                            }
                        }
                ).create().show();

    }


    public void openDialog(String title, final int min, final int max, final Preference pref, final String path, final String key) {
        Resources res = context.getResources();
        String cancel = res.getString(R.string.cancel);
        String ok = res.getString(R.string.ok);
        final EditText settingText;
        LayoutInflater factory = LayoutInflater.from(context);
        final View alphaDialog = factory.inflate(R.layout.seekbar_dialog, null);

        final SeekBar seekbar = (SeekBar) alphaDialog.findViewById(R.id.seek_bar);
        int currentProgress = Integer.parseInt(Helpers.LeerUnaLinea(path));
        seekbar.setMax(max - min);
        if (currentProgress > max) currentProgress = max - min;
        else if (currentProgress < min) currentProgress = 0;
        else currentProgress = currentProgress - min;

        seekbar.setProgress(currentProgress);

        settingText = (EditText) alphaDialog.findViewById(R.id.setting_text);
        settingText.setText(Integer.toString(currentProgress + min));

        settingText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    int val = Integer.parseInt(settingText.getText().toString()) - min;
                    seekbar.setProgress(val);
                    return true;
                }
                return false;
            }
        });

        settingText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int val = Integer.parseInt(s.toString());
                    if (val > max) {
                        s.replace(0, s.length(), Integer.toString(max));
                        val = max;
                    }
                    seekbar.setProgress(val - min);
                } catch (NumberFormatException ex) {
                }
            }
        });

        SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekbar, int progress, boolean fromUser) {
                final int mSeekbarProgress = seekbar.getProgress();
                if (fromUser) {
                    settingText.setText(Integer.toString(mSeekbarProgress + min));
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekbar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekbar) {
            }
        };
        seekbar.setOnSeekBarChangeListener(seekBarChangeListener);

        new AlertDialog.Builder(context)
                .setTitle(title)
                .setView(alphaDialog)
                .setNegativeButton(cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // nothing
                            }
                        })
                .setPositiveButton(ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int val = min;
                        if (!settingText.getText().toString().equals(""))
                            val = Integer.parseInt(settingText.getText().toString());
                        if (val < min) val = min;
                        seekbar.setProgress(val - min);
                        int newProgress = seekbar.getProgress() + min;
                        new CMDProcessor().su.runWaitFor("busybox echo " + Integer.toString(newProgress) + " > " + path);
                        final String v = Helpers.LeerUnaLinea(path);
                        mPreferences.edit().putString(key, v).commit();
                        pref.setSummary(v);

                    }
                }).create().show();
    }
}

