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

package mx.klozz.xperience.tweaker.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;

import mx.klozz.xperience.tweaker.R;
import mx.klozz.xperience.tweaker.activities.GobernorActivity;
import mx.klozz.xperience.tweaker.activities.IOSchedActivity;
import mx.klozz.xperience.tweaker.MainActivity;
import mx.klozz.xperience.tweaker.activities.MemUsageActivity;
import mx.klozz.xperience.tweaker.activities.Settings;
import mx.klozz.xperience.tweaker.helpers.PreferenceHelper;
import mx.klozz.xperience.tweaker.util.Constants;
import mx.klozz.xperience.tweaker.helpers.Helpers;

import java.io.File;
import java.util.Arrays;

public class CPUSettings extends Fragment implements SeekBar.OnSeekBarChangeListener, Constants {

    private LinearLayout lCurrentCPU;
    private SeekBar mMaxSlider;
    private SeekBar mMinSlider;
    private Spinner mGovernor;
    private Spinner mIo;
    private TextView mCurFreq;
    private TextView mMaxSpeedText;
    private TextView mMinSpeedText;
    private CurrentCPUThread mCurrentCPUThread;
    SharedPreferences mPreferences;
    private boolean mIsTegra3 = false;
    private boolean mIsDynFreq = false;
    private Context context;
    private final String supported[] = {"ondemand", "ondemandplus", "lulzactive", "lulzactiveW", "interactive", "hyper", "conservative", "lionheart", "adaptive", "intellidemand,intellimm"};
    private int nCpus = Helpers.getNumOfCPUS();
    private SwitchPreference mThermal;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        if (savedInstanceState != null) {
            MainActivity.CurrentCPU = savedInstanceState.getInt("CurrentCPU");
            MainActivity.mMaximunFreqSetting.set(MainActivity.CurrentCPU, savedInstanceState.getString("maxfreq"));
            MainActivity.mMinimunFreqSetting.set(MainActivity.CurrentCPU, savedInstanceState.getString("minfreq"));
            MainActivity.mCurrentGovernor.set(MainActivity.CurrentCPU, savedInstanceState.getString("governor"));
            MainActivity.mCurrentIOSched.set(MainActivity.CurrentCPU, savedInstanceState.getString("io"));
            MainActivity.mCPUOn.set(MainActivity.CurrentCPU, savedInstanceState.getString("cpuon"));
        } else {
            if (MainActivity.mMinimunFreqSetting.isEmpty() || MainActivity.mMaximunFreqSetting.isEmpty())
                MainActivity.getCPUvalues();
        }

        setHasOptionsMenu(true);
    }

    @Override
    public void onSaveInstanceState(Bundle saveState) {
        super.onSaveInstanceState(saveState);
        saveState.putInt("CurrentCPU", MainActivity.CurrentCPU);
        saveState.putString("maxfreq", MainActivity.mMaximunFreqSetting.get(MainActivity.CurrentCPU));
        saveState.putString("minfreq", MainActivity.mMinimunFreqSetting.get(MainActivity.CurrentCPU));
        saveState.putString("governor", MainActivity.mCurrentGovernor.get(MainActivity.CurrentCPU));
        saveState.putString("io", MainActivity.mCurrentIOSched.get(MainActivity.CurrentCPU));
        saveState.putString("cpuon", MainActivity.mCPUOn.get(MainActivity.CurrentCPU));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.cpu_settings, root, false);

        mIsTegra3 = new File(TEGRA_MAX_FREQ_PATH).exists();
        mIsDynFreq = new File(DYN_MAX_FREQ_PATH).exists() && new File(DYN_MIN_FREQ_PATH).exists();

        lCurrentCPU = (LinearLayout) view.findViewById(R.id.lCurCPU);

        mCurFreq = (TextView) view.findViewById(R.id.current_speed);
        mCurFreq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (nCpus == 1) return;
                if (++MainActivity.CurrentCPU > (nCpus - 1)) MainActivity.CurrentCPU = 0;
                setCPUval(MainActivity.CurrentCPU);
            }
        });

        mCurFreq.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (new File(CPU_ON_PATH.replace("cpu0", "cpu" + MainActivity.CurrentCPU)).exists() && MainActivity.CurrentCPU > 0) {
                    final StringBuilder sb = new StringBuilder();
                    if (MainActivity.mCPUOn.get(MainActivity.CurrentCPU).equals("1")) {
                        sb.append("set_val \"").append(CPU_ON_PATH.replace("cpu0", "cpu" + MainActivity.CurrentCPU)).append("\" \"0\";\n");
                        MainActivity.mCPUOn.set(MainActivity.CurrentCPU, "0");
                    } else {
                        sb.append("set_val \"").append(CPU_ON_PATH.replace("cpu0", "cpu" + MainActivity.CurrentCPU)).append("\" \"1\";\n");
                        MainActivity.mCPUOn.set(MainActivity.CurrentCPU, "1");
                    }
                    Helpers.shExec(sb, context, true);

                    setCPUval(MainActivity.CurrentCPU);
                }

                return true;
            }
        });

        final int mFrequenciesNum = MainActivity.mAvailableFrequencies.length - 1;

        mMaxSlider = (SeekBar) view.findViewById(R.id.max_slider);
        mMaxSlider.setMax(mFrequenciesNum);
        mMaxSlider.setOnSeekBarChangeListener(this);
        mMaxSpeedText = (TextView) view.findViewById(R.id.max_speed_text);

        mMinSlider = (SeekBar) view.findViewById(R.id.min_slider);
        mMinSlider.setMax(mFrequenciesNum);
        mMinSlider.setOnSeekBarChangeListener(this);
        mMinSpeedText = (TextView) view.findViewById(R.id.min_speed_text);

        mGovernor = (Spinner) view.findViewById(R.id.pref_governor);
        String[] mAvailableGovernors = Helpers.LeerUnaLinea(GOVERNORS_LIST_PATH).split(" ");
        ArrayAdapter<CharSequence> governorAdapter = new ArrayAdapter<CharSequence>(context, android.R.layout.simple_spinner_item);
        governorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for (String mAvailableGovernor : mAvailableGovernors) {
            governorAdapter.add(mAvailableGovernor.trim());
        }
        mGovernor.setAdapter(governorAdapter);
        mGovernor.setSelection(Arrays.asList(mAvailableGovernors).indexOf(MainActivity.mCurrentGovernor.get(MainActivity.CurrentCPU)));
        mGovernor.post(new Runnable() {
            public void run() {
                mGovernor.setOnItemSelectedListener(new GovListener());
            }
        });


        mIo = (Spinner) view.findViewById(R.id.pref_io);
        String[] mAvailableIo = Helpers.getAvailableIOSchedulers(IO_SCHEDULER_PATH);

        ArrayAdapter<CharSequence> ioAdapter = new ArrayAdapter<CharSequence>(context, android.R.layout.simple_spinner_item);
        ioAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for (String aMAvailableIo : mAvailableIo) {
            ioAdapter.add(aMAvailableIo);
        }
        mIo.setAdapter(ioAdapter);
        mIo.setSelection(Arrays.asList(mAvailableIo).indexOf(MainActivity.mCurrentIOSched.get(MainActivity.CurrentCPU)));
        mIo.post(new Runnable() {
            public void run() {
                mIo.setOnItemSelectedListener(new IOListener());
            }
        });

        Switch mSetOnBoot = (Switch) view.findViewById(R.id.cpu_sob);
        mSetOnBoot.setChecked(mPreferences.getBoolean(CPU_SOB, false));
        mSetOnBoot.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton v, boolean checked) {
                final SharedPreferences.Editor editor = mPreferences.edit();
                editor.putBoolean(CPU_SOB, checked);
                if (checked) {
                    for (int i = 0; i < nCpus; i++) {
                        editor.putString(PREF_MIN_CPU + i, MainActivity.mMinimunFreqSetting.get(i));
                        editor.putString(PREF_MAX_CPU + i, MainActivity.mMaximunFreqSetting.get(i));
                        editor.putString(PREF_GOV, MainActivity.mCurrentGovernor.get(i));
                        editor.putString(PREF_IO, MainActivity.mCurrentIOSched.get(i));
                        editor.putString("cpuon" + i, MainActivity.mCPUOn.get(i));
                    }
                }
                editor.apply();
            }
        });

        //if(nCpus>1){
        LinearLayout vcpus[] = new LinearLayout[nCpus];
        for (int i = 0; i < nCpus; i++) {
            vcpus[i] = (LinearLayout) inflater.inflate(R.layout.cpu_view, root, false);
            vcpus[i].setId(i);
            TextView nc = (TextView) vcpus[i].findViewById(R.id.ncpu);
            nc.setText(Integer.toString(i + 1));
            if (i != MainActivity.CurrentCPU) nc.setText(" ");
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, (float) 0.1);
            lCurrentCPU.addView(vcpus[i], params);
        }
        //}

        setCPUval(MainActivity.CurrentCPU);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.cpu_setting, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.tablist:
                Helpers.getTabList(getString(R.string.menu_tab), (ViewPager) getView().getParent(), getActivity());
                break;
            case R.id.app_settings:
                Intent intent = new Intent(context, Settings.class);
                startActivity(intent);
                break;
            case R.id.gov_settings:
                for (String aSupported : supported) {
                    if (aSupported.equals(MainActivity.mCurrentGovernor.get(MainActivity.CurrentCPU))) {
                        if (new File(GOV_SETTINGS_PATH + MainActivity.mCurrentGovernor.get(MainActivity.CurrentCPU)).exists()) {
                            intent = new Intent(context, GobernorActivity.class);
                            intent.putExtra("curgov", MainActivity.mCurrentGovernor.get(MainActivity.CurrentCPU));
                            startActivity(intent);
                        }
                        break;
                    }
                }
                break;
            case R.id.io_settings:
                if (new File(IO_TUNABLE_PATH).exists()) {
                    intent = new Intent(context, IOSchedActivity.class);
                    intent.putExtra("curio", MainActivity.mCurrentIOSched.get(MainActivity.CurrentCPU));
                    startActivity(intent);
                }
                break;
            case R.id.cpu_info:
                intent = new Intent(getActivity(), MemUsageActivity.class);
                intent.putExtra("tip", "cpu");
                startActivity(intent);
                break;
        }
        return true;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            switch (seekBar.getId()) {
                case R.id.max_slider:
                    setMaxSpeed(progress);
                    break;
                case R.id.min_slider:
                    setMinSpeed(progress);
                    break;
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        final StringBuilder sb = new StringBuilder();
        int oc = -1000;
        if (new File(OC_VALUE_PATH).exists()) {
            oc = Integer.parseInt(Helpers.LeerUnaLinea(OC_VALUE_PATH));
            sb.append("busybox echo 100 > ").append(OC_VALUE_PATH).append(";\n");
        }
        switch (seekBar.getId()) {
            case R.id.max_slider:
                updateSharedPrefs(PREF_MAX_CPU + MainActivity.CurrentCPU, MainActivity.mMaximunFreqSetting.get(MainActivity.CurrentCPU));
                sb.append("set_val \"").append(MAX_FREQ_PATH.replace("cpu0", "cpu" + MainActivity.CurrentCPU)).append("\" \"").append(MainActivity.mMaximunFreqSetting.get(MainActivity.CurrentCPU)).append("\";\n");
                if (mIsTegra3) {
                    sb.append("busybox echo ").append(MainActivity.mMaximunFreqSetting.get(MainActivity.CurrentCPU)).append(" > ").append(TEGRA_MAX_FREQ_PATH).append(";\n");
                }
                if (mIsDynFreq) {
                    sb.append("busybox echo ").append(MainActivity.mMaximunFreqSetting.get(MainActivity.CurrentCPU)).append(" > ").append(DYN_MAX_FREQ_PATH).append(";\n");
                }
                if (new File(HARD_LIMIT_PATH).exists()) {
                    sb.append("busybox echo ").append(MainActivity.mMaximunFreqSetting.get(MainActivity.CurrentCPU)).append(" > ").append(HARD_LIMIT_PATH).append(";\n");
                }
                break;
            case R.id.min_slider:
                updateSharedPrefs(PREF_MIN_CPU + MainActivity.CurrentCPU, MainActivity.mMinimunFreqSetting.get(MainActivity.CurrentCPU));
                sb.append("set_val \"").append(MIN_FREQ_PATH.replace("cpu0", "cpu" + MainActivity.CurrentCPU)).append("\" \"").append(MainActivity.mMinimunFreqSetting.get(MainActivity.CurrentCPU)).append("\";\n");
                if (mIsDynFreq) {
                    sb.append("busybox echo ").append(MainActivity.mMinimunFreqSetting.get(MainActivity.CurrentCPU)).append(" > ").append(DYN_MIN_FREQ_PATH).append(";\n");
                }
                break;
        }
        if (new File(OC_VALUE_PATH).exists() && oc != 100 && oc > -1000) {
            sb.append("busybox echo ").append(Integer.toString(oc)).append(" > ").append(OC_VALUE_PATH).append(";\n");
        }
        Helpers.shExec(sb, context, true);
    }

    public class GovListener implements OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            String selected = parent.getItemAtPosition(pos).toString();
            if (MainActivity.mCurrentGovernor.get(MainActivity.CurrentCPU).equals(selected)) return;
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < nCpus; i++) {
                sb.append("set_val \"").append(GOVERNOR_PATH.replace("cpu0", "cpu" + i)).append("\" \"").append(selected).append("\";\n");
            }
            //restore gov tunable
            final String s = mPreferences.getString(selected.replace(" ", "_"), "");
            if (!s.equals("")) {
                sb.append("if busybox [ -d ").append(GOV_SETTINGS_PATH).append(selected).append(" ]; then\n");
                String p[] = s.split(";");
                for (String aP : p) {
                    if (aP != null && aP.contains(":")) {
                        final String pn[] = aP.split(":");
                        sb.append("busybox echo ").append(pn[1]).append(" > ").append(GOV_SETTINGS_PATH).append(selected).append("/").append(pn[0]).append(";\n");
                    }
                }
                sb.append("fi;\n");
            }
            Helpers.shExec(sb, context, true);
            MainActivity.mCurrentGovernor.set(MainActivity.CurrentCPU, selected);
            updateSharedPrefs(PREF_GOV, selected);
        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing.
        }
    }

    public class IOListener implements OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            String selected = parent.getItemAtPosition(pos).toString();
            if (MainActivity.mCurrentIOSched.get(MainActivity.CurrentCPU).equals(selected)) return;
            final StringBuilder sb = new StringBuilder();
            for (byte i = 0; i < 2; i++) {
                if (new File(IO_SCHEDULER_PATH.replace("mmcblk0", "mmcblk" + i)).exists())
                    sb.append("busybox echo ").append(selected).append(" > ").append(IO_SCHEDULER_PATH.replace("mmcblk0", "mmcblk" + i)).append(";\n");
            }
            //restore io tunable
            final String s = mPreferences.getString(selected.replace(" ", "_"), "");
            if (!s.equals("")) {
                String p[] = s.split(";");
                for (byte i = 0; i < 2; i++) {
                    if (new File(IO_TUNABLE_PATH.replace("mmcblk0", "mmcblk" + i)).exists()) {
                        for (String aP : p) {
                            if (aP != null && aP.contains(":")) {
                                final String pn[] = aP.split(":");
                                sb.append("busybox echo ").append(pn[1]).append(" > ").append(IO_TUNABLE_PATH.replace("mmcblk0", "mmcblk" + i)).append("/").append(pn[0]).append(";\n");
                            }
                        }
                    }
                }
            }
            Helpers.shExec(sb, context, true);
            MainActivity.mCurrentIOSched.set(MainActivity.CurrentCPU, selected);
            updateSharedPrefs(PREF_IO, selected);
        }

        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCurrentCPUThread == null) {
            mCurrentCPUThread = new CurrentCPUThread();
            mCurrentCPUThread.start();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mCurrentCPUThread != null) {
            if (mCurrentCPUThread.isAlive()) {
                mCurrentCPUThread.interrupt();
                try {
                    mCurrentCPUThread.join();
                } catch (InterruptedException e) {
                    Log.d(TAG, "CPU thread error " + e);
                }
            }
        }
        super.onDestroy();
    }

    public void setMaxSpeed(int progress) {
        final String current = MainActivity.mAvailableFrequencies[progress];
        int minSliderProgress = mMinSlider.getProgress();
        if (progress <= minSliderProgress) {
            mMinSlider.setProgress(progress);
            mMinSpeedText.setText(Helpers.toMHz(current));
            MainActivity.mMinimunFreqSetting.set(MainActivity.CurrentCPU, current);
        }
        mMaxSpeedText.setText(Helpers.toMHz(current));
        MainActivity.mMaximunFreqSetting.set(MainActivity.CurrentCPU, current);
    }

    public void setMinSpeed(int progress) {
        final String current = MainActivity.mAvailableFrequencies[progress];
        int maxSliderProgress = mMaxSlider.getProgress();
        if (progress >= maxSliderProgress) {
            mMaxSlider.setProgress(progress);
            mMaxSpeedText.setText(Helpers.toMHz(current));
            MainActivity.mMaximunFreqSetting.set(MainActivity.CurrentCPU, current);
        }
        mMinSpeedText.setText(Helpers.toMHz(current));
        MainActivity.mMinimunFreqSetting.set(MainActivity.CurrentCPU, current);
    }

    private void setCpuView(int i) {
        //if(nCpus<=1) return;
        for (int k = 0; k < nCpus; k++) {
            setCpuOnOff(k);
            setCpuNo(k, i);
        }
    }

    private void setCpuNo(int i, int k) {
        final LinearLayout l = (LinearLayout) lCurrentCPU.getChildAt(i);
        final TextView nc = (TextView) l.findViewById(R.id.ncpu);
        if (i == k) {
            nc.setText(Integer.toString(i + 1));
        } else {
            nc.setText(" ");
        }
    }

    private void setCpuOnOff(int i) {
        final LinearLayout l = (LinearLayout) lCurrentCPU.getChildAt(i);
        final View vc = (View) l.findViewById(R.id.vcpu);
        if (MainActivity.mCPUOn.get(i).equals("0")) {
            vc.setBackgroundColor(getResources().getColor(R.color.light_gray));
        } else {
            vc.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
        }
    }

    public void setCPUval(int i) {
        setCpuView(i);

        mMaxSpeedText.setText(Helpers.toMHz(MainActivity.mMaximunFreqSetting.get(i)));
        mMaxSlider.setProgress(Arrays.asList(MainActivity.mAvailableFrequencies).indexOf(MainActivity.mMaximunFreqSetting.get(i)));

        mMinSpeedText.setText(Helpers.toMHz(MainActivity.mMinimunFreqSetting.get(i)));
        mMinSlider.setProgress(Arrays.asList(MainActivity.mAvailableFrequencies).indexOf(MainActivity.mMinimunFreqSetting.get(i)));

        if (i > 0 && (mIsDynFreq || new File(HARD_LIMIT_PATH).exists())) {
            mMaxSlider.setEnabled(false);
            mMinSlider.setEnabled(false);
        } else {
            mMaxSlider.setEnabled(true);
            mMinSlider.setEnabled(true);
        }

        if (MainActivity.mCPUOn.get(i).equals("0")) {
            mMaxSlider.setEnabled(false);
            mMinSlider.setEnabled(false);
        }
        mPreferences.edit().putString("cpuon" + i, MainActivity.mCPUOn.get(i)).apply();
    }

    protected class CurrentCPUThread extends Thread {
        private boolean mInterrupt = false;
        private String onlist = "";

        public void interrupt() {
            mInterrupt = true;
        }

        @Override
        public void run() {
            try {
                while (!mInterrupt) {
                    onlist = "";
                    for (int i = 0; i < nCpus; i++) {
                        if (new File(CPU_ON_PATH.replace("cpu0", "cpu" + i)).exists()) {
                            final String on = Helpers.LeerUnaLinea(CPU_ON_PATH.replace("cpu0", "cpu" + i));
                            if ((on != null) && (on.length() > 0)) {
                                onlist += on.trim() + ":";
                            } else {
                                onlist += "0:";
                            }
                        } else {
                            onlist += "1:";
                        }
                    }
                    if (new File(CUR_CPU_PATH.replace("cpu0", "cpu" + MainActivity.CurrentCPU)).exists()) {
                        final String curfreq = Helpers.LeerUnaLinea(CUR_CPU_PATH.replace("cpu0", "cpu" + MainActivity.CurrentCPU));
                        if ((curfreq != null) && (curfreq.length() > 0)) {
                            mCurrentCPUHandler.sendMessage(mCurrentCPUHandler.obtainMessage(0, curfreq + ":" + onlist));
                        } else {
                            mCurrentCPUHandler.sendMessage(mCurrentCPUHandler.obtainMessage(0, "0:" + onlist));
                        }
                    } else {
                        mCurrentCPUHandler.sendMessage(mCurrentCPUHandler.obtainMessage(0, "0:" + onlist));
                    }
                    sleep(600);
                }
            } catch (InterruptedException e) {
                Log.d(TAG, "CPU thread error " + e);
            }
        }
    }

    protected Handler mCurrentCPUHandler = new Handler() {
        public void handleMessage(Message msg) {
            final String r = (String) msg.obj;
            //Log.d(TAG, "CPU onoff: "+r);
            mCurFreq.setText(Helpers.toMHz(r.split(":")[0]));
            for (int i = 0; i < nCpus; i++) {
                MainActivity.mCPUOn.set(i, r.split(":")[i + 1]);
                try {
                    setCpuOnOff(i);
                } catch (Exception e) {
                }
            }
        }
    };


    private void updateSharedPrefs(String var, String value) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(var, value).apply();
        Intent intent = new Intent(INTENT_PP);
        intent.putExtra("from", getString(R.string.app_name));
        context.sendBroadcast(intent);

    }

}

