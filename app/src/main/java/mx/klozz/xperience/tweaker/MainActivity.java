package mx.klozz.xperience.tweaker;
/*
 * XPerience Kernel Tweaker - An Android CPU Control application
 * Copyright (C) 2011-2015 Carlos "Klozz" Jesus <TeamMEX@XDA-Developers>
 *
 *     Copyright h0rn3t and AOKP Team
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

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.Toast;

import mx.klozz.xperience.tweaker.R;
import mx.klozz.xperience.tweaker.fragments.*;
import mx.klozz.xperience.tweaker.util.ActivityThemeChangeInterface;
import mx.klozz.xperience.tweaker.util.BootClass;
import mx.klozz.xperience.tweaker.util.Constants;
import mx.klozz.xperience.tweaker.activities.checkSU;
import mx.klozz.xperience.tweaker.helpers.Helpers;

import java.util.ArrayList;
import java.util.List;


/*
 *
 * Common notes: This app is based on Performance Control from AOKP with modifications
 * So initial credits to AOKP team
 * and Horn3t coz I taked some code from him
 *
 * Added my license to make a code licensed on GPL3 like Performance Control from AOKP
 * but Credits are added Here!
 *
 * I added some other things and maked a more clean code
 *
 * some comments are on spanish coz I used to learn about some coding and some friends learn from my code explanations.
 */

public class MainActivity extends Activity implements Constants, ActivityThemeChangeInterface {
    public static Context contexto;
    public static Boolean TabHide = false;
    public static ArrayList<String> mCurrentGovernor = new ArrayList<String>();//Tomar la info del gobernador establecido
    public static ArrayList<String> mCurrentIOSched = new ArrayList<String>();//Tomar la info del IO/Scheduler
    public static ArrayList<String> mMaximunFreqSetting = new ArrayList<String>();//Maxima frecuencia de CPU
    public static ArrayList<String> mMinimunFreqSetting = new ArrayList<String>();//Minima frecuencia de cpu
    public static ArrayList<String> mCPUOn = new ArrayList<String>();//Saber que core esta en uso
    public static String[] mAvailableFrequencies = new String[0];//Frecuencias sacadas de la tabla de frecuencias del kernel
    public static int CurrentCPU = 0;// CPU en uso
    public static boolean is_restored = false;
    private SharedPreferences mPreferences; //Preferencias de compartir
    private ViewPager mViewPager;//Ver la pagina
    private boolean mIsLightTheme;//Tema claro //Gracias a H0rn3t Este codigo de cambio de temas fue tomado de su codifo fuente
    private boolean pref_changed = false; //Preferencias de configuracion han cambiado?
    private PreferenceChangeListener mPreferenceListener;//para ver si las preferencias han cambiado
    private TitleAdapter titleAdapter;//Adaptador de titulo


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contexto = this;
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);//pasar el valor de preference manager a mPreferences
        setTheme();
        setContentView(R.layout.activity_main);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);//Encontrar en el XML De Activity main por ID

        PagerTabStrip mPagerTabStrip = (PagerTabStrip) findViewById(R.id.pagerTabStrip);//Para crear pestañas

        mPagerTabStrip.setTabIndicatorColor(getResources().getColor(android.R.color.holo_red_dark));//color del indicador de la pestaña

        mPagerTabStrip.setDrawFullUnderline(true);
        titleAdapter = new TitleAdapter(getFragmentManager());
        if (savedInstanceState == null) {
            checkForSu();
        } else {

            mViewPager.setAdapter(titleAdapter);
            mViewPager.setCurrentItem(0);
        }
        mPreferenceListener = new PreferenceChangeListener();
        mPreferences.registerOnSharedPreferenceChangeListener(mPreferenceListener);
    }

    @Override
    public void onSaveInstanceState(Bundle saveState) {
        super.onSaveInstanceState(saveState);
    }

    /*
     * Obtener la lista de titulos para el TabStrip a mostrar dependiendo de si se
     * @return String[] conteniendo titulos
     */
    private String[] getTitles() {
        List<String> titleslist = new ArrayList<String>();
        String def_ids = "";//Definimos los id vacios por ahora ya que obtendran valores de los array
        for (int i = 0; i < getResources().getStringArray(R.array.tabs).length; i++)
            def_ids += i + ":";
        final String TabIDs = mPreferences.getString("tab_ids", def_ids);

        for (int i = 0; i < getResources().getStringArray(R.array.tabs).length; i++) {
            String TabID= TabIDs.split(":")[i];
            if ((TabID!= null) && (!TabID.equals(""))) {
                int id = Integer.valueOf(TabID);//el id lo sacamos de TabID
                final String sTab = getResources().getStringArray(R.array.tabs)[id];
                boolean isvisible = mPreferences.getBoolean(sTab, true);//saber si es una pestaña visible o no
                if (Helpers.is_Tab_available(id) && isvisible) titleslist.add(sTab);
            }
        }
        return titleslist.toArray(new String[titleslist.size()]);
    }

    class TitleAdapter extends FragmentPagerAdapter {
        String titles[] = getTitles();
        private Fragment frags[] = new Fragment[titles.length];//Frags = Fragmentos

        public TitleAdapter(FragmentManager fm) {
            super(fm);

            String def_ids = "";
            for (int i = 0; i < getResources().getStringArray(R.array.tabs).length; i++)
                def_ids += i + ":";
            final String TabIDs = mPreferences.getString("tab_ids", def_ids);

            int z = 0;
            for (int i = 0; i < getResources().getStringArray(R.array.tabs).length; i++) {
                String TabID= TabIDs.split(":")[i];
                if ((TabID!= null) && (!TabID.equals(""))) {
                    int id = Integer.valueOf(TabID);
                    boolean isvisible = mPreferences.getBoolean(getResources().getStringArray(R.array.tabs)[id], true);
                    if (Helpers.is_Tab_available(id) && isvisible) {
                        switch (id) {
                            case 0:
                                frags[z] = new TimeInState();
                                break;
                            case 1:
                                frags[z] = new CPUSettings();
                                break;
                            case 2:
                                frags[z] = new CPUAdvanced();
                                break;
                            case 3:
                                frags[z] = new MemSettings();
                                break;
                            case 4:
                                frags[z] = new VoltageControlSettings();
                                break;
                            case 5:
                                frags[z] = new BatteryInfo();
                                break;
                            case 6:
                                frags[z] = new Advanced();
                                break;
                            case 7:
                                frags[z] = new DiskInfo();
                                break;
                            case 8:
                                frags[z] = new Tools();
                                break;
                        }
                        z++;
                    }
                }
            }
        }

       /*
        * Obtener el titulo real de la posicion donde nos encontramos
        * para ello revisaremos el array
        */
        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
        /*
         * Retornamos la posicion dentro de los fragmentos que
         * definimos anteriormente que daba acceso a cada suceso
         * en cada clase
         */
        @Override
        public Fragment getItem(int position) {
            return frags[position];
        }

        @Override
        public int getCount() {
            return frags.length;
        }
    }//FInaliza TitleAdapter

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mPreferences.unregisterOnSharedPreferenceChangeListener(mPreferenceListener);
        super.onDestroy();
    }

    @Override
    public void onStop() {
        if (mPreferences.getBoolean("boot_mode", false) && pref_changed) {
            new Thread(new Runnable() {
                public void run() {
                    new BootClass(contexto, mPreferences).writeScript();
                }
            }).start();
            Toast.makeText(contexto, "init.d script updated", Toast.LENGTH_SHORT).show();
        }
        super.onStop();
    }//Finaliza on stop

    @Override
    public void onResume() {
        super.onResume();
        if (isThemeChanged() || TabHide || is_restored) {
            if (TabHide) TabHide = false;
            if (is_restored) is_restored = false;
            Helpers.restartTW(this);
        }

    }

    /*
     *  Creamos la funcion para verificar si el theme ha sido cambiado
     *  Creditos totales por esto a H0rn3t
     *  De ahi tome la idea
     */
    @Override
    public boolean isThemeChanged() {
        final boolean is_light_theme = mPreferences.getBoolean(PREF_USE_LIGHT_THEME, false);
        return is_light_theme != mIsLightTheme;
    }

    @Override
    public void setTheme() {
        final boolean is_light_theme = mPreferences.getBoolean(PREF_USE_LIGHT_THEME, false);
        mIsLightTheme = mPreferences.getBoolean(PREF_USE_LIGHT_THEME, false);
        setTheme(is_light_theme ? R.style.Theme_Light : R.style.Theme_Dark);
    }
    /*
     * Finaliza el codigo referente a cambiar el theme
     */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == 1) && (resultCode == RESULT_OK)) {
            String r = data.getStringExtra("r");
            if (r != null && r.equals("ok")) {
                mPreferences.edit().putString("rom", Build.DISPLAY).commit();
                getCPUvalues();
                mViewPager.setAdapter(titleAdapter);
                mViewPager.setCurrentItem(0);//El item que queremos que sea el primero en mostrar
                return;
            }
        }
        finish();
    }

   /*
    * Creamos una funcion para revisar si hay permisos SuperUsuario.
    * Tambien en ella revisaremos si existe Busybox y trataremos sobre los themes
    * Extraido de performanceControl app
    *
    */
    private void checkForSu() {
        if (mPreferences.getBoolean("theme_changed", false)) {
            mPreferences.edit().putBoolean("theme_changed", false).commit();
            getCPUvalues();//Obtendremos los valores de la CPU
            mViewPager.setAdapter(titleAdapter);
            mViewPager.setCurrentItem(0);
        } else {
            final String b = mPreferences.getString("rom", "");
            if (!b.equals(Build.DISPLAY)) {
                Log.d(TAG, "check for su & busybox");
                Intent intent = new Intent(MainActivity.this, checkSU.class);
                startActivityForResult(intent, 1);
            } else {
                if (Helpers.checkSu()) {
                    getCPUvalues();
                    mViewPager.setAdapter(titleAdapter);
                    mViewPager.setCurrentItem(0);
                } else {
                    Log.d(TAG, "check for su");
                    Intent intent = new Intent(MainActivity.this, checkSU.class);
                    startActivityForResult(intent, 1);
                }
            }
        }
    }

    /*
 * Obtendremos con la siguiente funcion los valores de la CPU
 *
 */
    public static void getCPUvalues() {
        final int nCpus = Helpers.getNumOfCPUS();
        final String r = Helpers.readCPU(contexto, nCpus);
        Log.d(TAG, "utils read: " + r);
        if (r.contains(":")) {//los valores obtenidos estan dela sig forma 890:1123:1345 etc
            mAvailableFrequencies = r.split(":")[nCpus * 5].split(" ");//Dividimos las frecuencias creandolas ahora 890 <br> 1123 etc
            mMaximunFreqSetting.clear();
            mMinimunFreqSetting.clear();
            mCurrentGovernor.clear();
            mCurrentIOSched.clear();
            mCPUOn.clear();
            // ahora nos moveremos por el array de valores
            for (int i = 0; i < nCpus; i++) {//el ciclo termina cuando el numero sea menor que numero de cpus ya que los cpu no inician de 1 sino de 0(los nucleos)
                if (Integer.parseInt(r.split(":")[i * 5]) < 0)
                    mMinimunFreqSetting.add(i, mAvailableFrequencies[0]);//¨Pasams los valores a minimun freq Setting
                else
                    mMinimunFreqSetting.add(i, r.split(":")[i * 5]);
            //Lo mismo que lo anterior pero ahora para el maximo
                if (Integer.parseInt(r.split(":")[i * 5 + 1]) < 0)
                    mMaximunFreqSetting.add(i, mAvailableFrequencies[mAvailableFrequencies.length - 1]);
                else
                    mMaximunFreqSetting.add(i, r.split(":")[i * 5 + 1]);

                mCurrentGovernor.add(i, r.split(":")[i * 5 + 2]);
                mCurrentIOSched.add(i, r.split(":")[i * 5 + 3]);
                mCPUOn.add(i, r.split(":")[i * 5 + 4]);
            }
        }
    }

    private class PreferenceChangeListener implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
            Toast.makeText(contexto, "Changed: " + key, Toast.LENGTH_LONG).show();
            pref_changed = true;
            Helpers.updateAppWidget(contexto);
        }
    }
}//Finaliza main

