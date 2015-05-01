/*
 * XPerience Kernel Tweaker - An Android CPU Control application
 * Copyright (C) 2011-2015 Carlos "Klozz" Jesus <TeamMEX@XDA-Developers>
 *
 *      Copyright h0rn3t and AOKP Team
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

/**
 * Created by klozz on 27/02/2015.
 */
import android.app.AlertDialog;
import android.app.Fragment;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.view.*;
import android.widget.*;

import java.io.File;

import mx.klozz.xperience.tweaker.R;
import mx.klozz.xperience.tweaker.activities.Settings;
import mx.klozz.xperience.tweaker.util.CMDProcessor;
import mx.klozz.xperience.tweaker.util.Constants;
import mx.klozz.xperience.tweaker.helpers.Helpers;

public class BatteryInfo extends Fragment implements SeekBar.OnSeekBarChangeListener, Constants {


    TextView mBattery_Percent; //porcentaje de bateria
    TextView mBattery_volt; //Voltaje de bateria
    TextView mBattery_Status; //Estado de la bateria

    ImageView mBatteryIcon;

    Switch mFastChargeOnBoot; //activar en el inicio

    SharedPreferences mPreferences;

    private String mFastChargePath;//Directorio donde se encuentra lo relacionado al fastcharge
    private Context context;
    private BroadcastReceiver batteryinfoReveiver;
    private int level,plugged;
    private boolean no_settings = true;
    private final String BatteryStatFile = "/data/system/batterystats.bin";

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        context = getActivity();
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        batteryinfoReveiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED,0);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE,0);
                int leve = intent.getIntExtra(BatteryManager.EXTRA_LEVEL,0);
                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS,0);
                int Temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,0);

                level = leve * scale /100;

                mBattery_Percent.setText(level + "%");//Mostraremos el porcentaje de bateria

                //Para los ajustes de voltaje siempre y cuando el kernel soporte dicha funcion.
                if(new File(BAT_VOLT_PATH).exists()){
                    int volt = Integer.parseInt(Helpers.LeerUnaLinea(BAT_VOLT_PATH));
                    if(volt > 5000)
                        volt = (int) Math.round(volt/1000.0); //Microvolts :)
                    mBattery_volt.setText(volt + " mV");
                }

                switch ((int)Math.ceil(level/20.0)){
                    case 0:
                        mBatteryIcon.setImageResource(R.drawable.battery0);
                        break;
                    case 1:
                        mBatteryIcon.setImageResource(R.drawable.battery1);
                        break;
                    case 2:
                        mBatteryIcon.setImageResource(R.drawable.battery2);
                        break;
                    case 3:
                        mBatteryIcon.setImageResource(R.drawable.battery3);
                        break;
                    case 4:
                        mBatteryIcon.setImageResource(R.drawable.battery4);
                        break;
                    case 5:
                        mBatteryIcon.setImageResource(R.drawable.battery5);
                        break;
                }

                mBattery_Status.setText((Temp/10)+" °C "+ getResources().getStringArray(R.array.batt_status)[status]);
            }
        };
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    /*
    *
    * inflater para el menu :3 de opciones para la bateria.
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.battery_menu, menu);
}

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.tablist:
                Helpers.getTabList(getString(R.string.menu_tab), (ViewPager) getView().getParent(),getActivity());
                break;
            case R.id.app_settings:
                Intent intent = new Intent(context, Settings.class);
                startActivity(intent);
                break;
            case R.id.calibration:
                if(!new File(BatteryStatFile).exists()){
                    Toast.makeText(context, getString(R.string.no_file,BatteryStatFile), Toast.LENGTH_SHORT);
                    break;
                }
                if((level == 100)&& (plugged != 0)){

                    final LayoutInflater factory = LayoutInflater.from(context);
                    final View CalibDiag = factory.inflate(R.layout.calibration_dialog, null);
                    final CheckBox sw = (CheckBox) CalibDiag.findViewById(R.id.sw);//the checkbox to enable on reboot

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(getString(R.string.mt_calib))//MenuTab Calibration
                            .setView(CalibDiag)
                            .setMessage(getString(R.string.mt_calib_ok))//Dialog when calibrations finish
                            .setNegativeButton(getString(R.string.cancel),//Cancel button
                                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id){
                            dialog.cancel();
                        }
                    }
                            )
                            .setPositiveButton(getString(R.string.mt_calib),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id){
                                    StringBuilder sb = new StringBuilder();
                                    sb.append("busybox rm -f " + BatteryStatFile + ";\n");
                                    if(sw.isChecked()){
                                        mPreferences.edit().putBoolean("booting", true).commit();
                                        sb.append("reboot;\n");
                                    }
                                    Helpers.shExec(sb, context, true);
                                    dialog.cancel();
                                }
                            });

            AlertDialog alertDialog = builder.create();
                alertDialog.show();
        }else{
            new AlertDialog.Builder(context).setTitle(getString(R.string.mt_calib)).setMessage(getString(R.string.mt_calib_nok))
                    .setPositiveButton(getString(R.string.ok),
                    new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which){

                                    }
                                }).create().show();

        }
        break;
    }
    return true;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.battery_info, root, false);

        mBattery_Percent = (TextView) view.findViewById(R.id.battery_percent);
        mBattery_volt = (TextView) view.findViewById(R.id.battery_volt);
        mBattery_Status = (TextView) view.findViewById(R.id.battery_status);
        mBatteryIcon = (ImageView) view.findViewById(R.id.battery_icon);

        //Checamos si existe algo relacionado con la direccion de voltajes
        if (new File(BAT_VOLT_PATH).exists()){
            int volt = Integer.parseInt(Helpers.LeerUnaLinea(BAT_VOLT_PATH));//vemos si existe el directorio
            if(volt > 5000)
                volt = (int) Math.round(volt / 1000.0);//Convertimos a microvolts

            mBattery_volt.setText(volt + " mV");//Hacemos visible los datos
            mBatteryIcon.setVisibility(ImageView.GONE);
            mBattery_volt.setVisibility(TextView.VISIBLE);
            mBattery_volt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent powerUsageIntent = new Intent(Intent.ACTION_POWER_USAGE_SUMMARY);
                    if (powerUsageIntent.resolveActivity(context.getPackageManager()) != null)
                        startActivity(powerUsageIntent);
                }
            });
            mBattery_volt.setOnLongClickListener(new View.OnLongClickListener(){
                @Override
            public boolean onLongClick(View view){
                    mBatteryIcon.setVisibility(ImageView.VISIBLE);
                    mBattery_volt.setVisibility(TextView.GONE);
                    return true;
                }
            });
        }else{
            mBatteryIcon.setVisibility(ImageView.VISIBLE);
            mBattery_volt.setVisibility(TextView.GONE);
            mBatteryIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        Intent powerUsageIntent = new Intent(Intent.ACTION_POWER_USAGE_SUMMARY);
                        startActivity(powerUsageIntent);
                    } catch (Exception e) {
                    }
                }
            });
        }


        mFastChargePath = Helpers.fastcharge_path();
        if(mFastChargePath != null) {
            no_settings = false;
            mFastChargeOnBoot = (Switch) view.findViewById(R.id.fastcharge_sob);
            mFastChargeOnBoot.setChecked(mPreferences.getBoolean(PREF_FASTCHARGE, Helpers.LeerUnaLinea(mFastChargePath).equals("1")));
            mFastChargeOnBoot.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton v, boolean checked) {
                    mPreferences.edit().putBoolean(PREF_FASTCHARGE, checked).apply();
                    final NotificationManager NM = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    if (checked) {
                        new CMDProcessor().su.runWaitFor("busybox exho 1 > " + mFastChargePath);
                        Notification N = new Notification.Builder(context)
                                .setContentTitle(context.getText(R.string.app_name))
                                .setContentText(context.getText(R.string.fast_charge_notification_title))
                                .setTicker(context.getText(R.string.fast_charge_notification_title))
                                .setSmallIcon(R.drawable.ic_fastcharge)
                                .setWhen(System.currentTimeMillis()).getNotification();
                        NM.notify(1337, N);
                    } else {
                        new CMDProcessor().su.runWaitFor("busybox echo 0 > " + mFastChargePath);
                        NM.cancel(1337);

                    }
                }

            });
        }else{
            LinearLayout mPart = (LinearLayout) view.findViewById(R.id.fastcharge_layout);
            mPart.setVisibility(LinearLayout.GONE);
        }
        if(no_settings){
            LinearLayout NS = (LinearLayout) view.findViewById(R.id.no_settings);
            NS.setVisibility(LinearLayout.VISIBLE);
        }
        return view;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }


    @Override
    public  void onStop(){
        try{
            getActivity().unregisterReceiver(batteryinfoReveiver);

        }catch (Exception e){
            e.printStackTrace();
        }
        super.onStop();
    }

    @Override
    public void onResume(){
        super.onResume();
        try{
            getActivity().registerReceiver(batteryinfoReveiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}//Finish class Battery info
