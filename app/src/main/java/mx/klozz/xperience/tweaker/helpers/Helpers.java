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

package mx.klozz.xperience.tweaker.helpers;

/**
 * Created by klozz on 22/04/2014.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.provider.SyncStateContract;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;

import mx.klozz.xperience.tweaker.R;
import mx.klozz.xperience.tweaker.util.CMDProcessor;
import mx.klozz.xperience.tweaker.widget.Widget;
import mx.klozz.xperience.tweaker.util.Constants;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Helpers implements Constants {

    private static String mVoltagePath;
    public static Context contexto;

    public static boolean checkSu(){
        if(!new File("/system/bin/su").exists() && !new File("/system/xbin/su").exists()){
            Log.e(TAG, "SU Does not exist");
            return false;
        }
        try{
            if((new CMDProcessor().su.runWaitFor("ls /data/app-private")).success()){
                Log.d(TAG, "SU Exist and We Have Permissions");
                return true;
            }else {
                Log.i(TAG, "SU Exist but We Don't Have Permissions");
                return false;
            }
            }catch (final NullPointerException e){
            Log.e(TAG, e.getMessage());
            return false;
        }
    }

    public static String LeerUnaLinea(String fname){
        String linea = null;
        if (new File(fname).exists()) {
            BufferedReader br;
            try{
                br=new BufferedReader(new FileReader(fname),512);
                try{
                    linea = br.readLine();
                }finally {
                        br.close();
                    }
                }catch (Exception e){
                    Log.e(TAG, "IO Exception When I trying to Read the file ", e);
                    return readFileViashell(fname,true);
                }
            }
        return linea;
        }

    public static String readFileViashell(String filepath, boolean useSu){
        CMDProcessor.CommandResult CommandR = null;
        if(useSu){
            CommandR = new CMDProcessor().su.runWaitFor("cat "+filepath);
        }else{
            CommandR = new CMDProcessor().sh.runWaitFor("cat "+filepath);
        }
        if(CommandR.success())
            return CommandR.stdout;
        return null;
    }

    //obtener los IOSChedulers displonibles
    public static String[] getAvailableIOSchedulers(String sch){
        String[] schedulers = null;
        String[] aux = readStringArray(sch);

        if(aux != null){
            schedulers = new String[aux.length];
            for(byte i = 0; i < aux.length; i++){
                if(aux[i].charAt(0) == '[') {
                    schedulers[i] = aux[i].substring(1, aux[i].length() - 1);
                } else {
                    schedulers[i] = aux[i];
                }
            }
        }
        return schedulers;
    }

    //obtener los schedulers
    public static String getIOScheduler(String sch){
        String scheduler = null;
        String[] schedulers = readStringArray(sch);
        if(schedulers != null){
            for(String s: schedulers){
                if(s.charAt(0)== '['){
                    scheduler = s.substring(1, s.length() -1);
                    break;
                }
            }
        }
        return scheduler;
    }


    //Leer los Array's en strings
    private static String[] readStringArray(String fname){
        String linea = LeerUnaLinea(fname);
        if(linea != null){
            return linea.split(" ");
        }
        return null;
    }

    //ver si existen gobernadores
    public static Boolean GovernorExist(String gov){
        return LeerUnaLinea(GOVERNORS_LIST_PATH).contains(gov);
    }


    //obtener numero de cpu's
    public static int getNumOfCPUS(){
        int numerodecpu = 1;
        String numerodecpus = Helpers.LeerUnaLinea(NUM_OF_CPUS_PATH);
        String[] cpuCount = numerodecpus.split("-");
        if(cpuCount.length > 1){
            try {
                int CPUStart = Integer.parseInt(cpuCount[0]);
                int CPUEnd = Integer.parseInt(cpuCount[1]);
                numerodecpu = CPUEnd - CPUStart + 1;
                if (numerodecpu < 0) numerodecpu = 1;
            }catch (NumberFormatException ex){
                numerodecpu = 1;
            }
        }
        return numerodecpu;
    }

    public static String readCPU(Context context, int i) {
        Helpers.get_assetsScript("utils", context, "", "");
        new CMDProcessor().sh.runWaitFor("busybox chmod 750 " + context.getFilesDir() + "/utils");
        CMDProcessor.CommandResult CommandR = new CMDProcessor().su.runWaitFor(context.getFilesDir() + "/utils -getcpu " + i);
        if (CommandR.success()) return CommandR.stdout;
        else return null;
    }

    /*
     * Voltage settings
     */
    public static boolean voltageFileExists() {
        if (new File(VDD_PATH).exists()) {
            setVoltagePath(VDD_PATH);
            return true;
        } else if (new File(COMMON_VDD_PATH).exists()) {
            setVoltagePath(COMMON_VDD_PATH);
            return true;
        } else if (new File(UV_MV_PATH).exists()) {
            setVoltagePath(UV_MV_PATH);
            return true;
        } else if (new File(VDD_TABLE).exists()) {
            setVoltagePath(VDD_TABLE);
            return true;
        }
        return false;
    }


    public static void setVoltagePath(String voltageFile) {
        Log.d(TAG, "Voltage table path detected: " + voltageFile);
        mVoltagePath = voltageFile;
    }

    public static String getVoltagePath() {
        return mVoltagePath;
    }

    /*
     *finish voltage settings
     */

    public static String toMHz(String mhzString) {
        if ((mhzString == null) || (mhzString.length() <= 0)) return "";
        else return String.valueOf(Integer.parseInt(mhzString) / 1000) + " MHz";
    }

    public static void restartTW(final Activity activity) {
        if (activity == null) return;
        final int enter_anim = android.R.anim.fade_in;
        final int exit_anim = android.R.anim.fade_out;
        activity.overridePendingTransition(enter_anim, exit_anim);
        activity.finish();
        activity.overridePendingTransition(enter_anim, exit_anim);
        activity.startActivity(activity.getIntent());
    }

    /*
     * HElper para Widget
     */

    public static void updateAppWidget(Context context) {
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
        ComponentName widgetComponent = new ComponentName(context, Widget.class);
        int[] widgetIds = widgetManager.getAppWidgetIds(widgetComponent);
        Intent update = new Intent();
        update.setAction("mx.klozz.xperience.tweaker.ACTION_FREQS_CHANGED");
        update.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);
        context.sendBroadcast(update);
    }

    public static Bitmap getBackground(int bgcolor) {
        try {
            Bitmap.Config config = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = Bitmap.createBitmap(2, 2, config);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(bgcolor);
            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }
    //Finalizan los helpers para el widget

    public static String shExec(StringBuilder s, Context c, Boolean su) {
        get_assetsScript("run", c, "", s.toString());
        new CMDProcessor().sh.runWaitFor("busybox chmod 750 " + c.getFilesDir() + "/run");
        CMDProcessor.CommandResult CommandR = null;
        if (su) {
            CommandR = new CMDProcessor().su.runWaitFor(c.getFilesDir() + "/run");
        } else {
            CommandR = new CMDProcessor().sh.runWaitFor(c.getFilesDir() + "/run");
        }
        if (CommandR.success()) {
            return CommandR.stdout;
        } else {
            Log.d(TAG, "execute run error: " + CommandR.stderr);
            return "nok";
        }
    }

    /*
     *check if the binary exist
     */
    public static String BinExist(String b){
        CMDProcessor.CommandResult CommandR = new CMDProcessor().sh.runWaitFor("busybox which " + b);
        if(CommandR.success() && CommandR.stdout !=null && CommandR.stdout.contains(b));{
            if(new File(CommandR.stdout).isFile()){
                Log.d(TAG, b + "Detected on: "+ CommandR.stdout);
                return CommandR.stdout;
            }
        }
        Log.d(TAG,b+ " Detected on: "+ CommandR.stdout);
        return null;
    }

    //Check if the module is active or not
    public static Boolean ModuleisActive(String b){
        CMDProcessor.CommandResult CommandR;
        CommandR = new CMDProcessor().sh.runWaitFor("busybox echo `busybox ps | busybox grep " + b + " | busybox grep -v \"busybox grep " + b + "\" | busybox awk '{print $1}'`");
        Log.d(TAG, "Module: "+CommandR.stdout);
        return (CommandR.success() && CommandR.stdout !=null && CommandR.stdout.length() > 0);
    }

//Obtener estado de memoria :P
    public static long getMem(String tip) {
        long v = 0;
        CMDProcessor.CommandResult CommandR = new CMDProcessor().sh.runWaitFor("busybox echo `busybox grep " + tip + " /proc/meminfo | busybox grep -E -o '[[:digit:]]+'`");
        if (CommandR.success() && CommandR.stdout != null && CommandR.stdout.length() > 0) {
            try {
                v = (long) Integer.parseInt(CommandR.stdout);//kb
            } catch (NumberFormatException e) {
                Log.d(TAG, tip + " conversion err: " + e);
            }
        }
        return v;
    }

    public static long getSwap() {
        long v = 0;
        for (int i = 0; i < getNumOfCPUS(); i++) {
            CMDProcessor.CommandResult CommandR = new CMDProcessor().sh.runWaitFor("busybox echo `busybox grep zram" + i + " /proc/swaps`");
            if (CommandR.success() && CommandR.stdout != null && CommandR.stdout.contains("zram" + i)) {
                try {
                    v = v + (long) Integer.parseInt(CommandR.stdout.split(" ")[2]);//kb
                } catch (NumberFormatException e) {
                    Log.d(TAG, " swap conversion err: " + e);
                }
            }
        }
        return v;
    }

    public static boolean isZRAM() {
        CMDProcessor.CommandResult CommandR = new CMDProcessor().sh.runWaitFor(ISZRAM);
        if ((CommandR.success() && CommandR.stdout != null && CommandR.stdout.length() > 0) || (new File("/dev/block/zram0/").exists()) || (new File("/sys/block/zram0/").exists()))
            return true;
        return false;
    }//finaliza lo relacionado a memorias :D


    public static void get_assetsScript(String fn, Context c, String prefix, String postfix) {
        byte[] buffer;
        final AssetManager assetManager = c.getAssets();
        try {
            InputStream f = assetManager.open(fn);
            buffer = new byte[f.available()];
            f.read(buffer);
            f.close();
            final String s = new String(buffer);
            final StringBuilder sb = new StringBuilder(s);
            if (!postfix.equals("")) {
                sb.append("\n\n").append(postfix);
            }
            if (!prefix.equals("")) {
                sb.insert(0, prefix + "\n");
            }
            sb.insert(0, "#!" + Helpers.BinExist("sh") + "\n\n");
            try {
                FileOutputStream fos;
                fos = c.openFileOutput(fn, Context.MODE_PRIVATE);
                fos.write(sb.toString().getBytes());
                fos.close();

            } catch (IOException e) {
                Log.d(TAG, "error write " + fn + " file");
                e.printStackTrace();
            }
        } catch (IOException e) {
            Log.d(TAG, "error read " + fn + " file");
            e.printStackTrace();
        }
    }

    public synchronized static void get_assetsBinary(String fn, Context c) {
        byte[] buffer;
        final AssetManager assetManager = c.getAssets();
        try {
            InputStream f = assetManager.open(fn);
            buffer = new byte[f.available()];
            f.read(buffer);
            f.close();
            try {
                FileOutputStream fos;
                fos = c.openFileOutput(fn, Context.MODE_PRIVATE);
                fos.write(buffer);
                fos.close();
            } catch (IOException e) {
                Log.d(TAG, "error write " + fn + " file");
                e.printStackTrace();
            }
        } catch (IOException e) {
            Log.d(TAG, "error read " + fn + " file");
            e.printStackTrace();
        }
    }

    //Nuevos :D
    //CPU HELPERS
    public boolean isIntelliPlugEcoActive() {

        return mUtils.readFile(INTELLI_PROFILES).equals("1");
    }

    public boolean hasIntelliPlugEco() {
        return mUtils.existFile(INTELLI_PROFILES);
    }

    public int getIntelliProf() {
        if (mUtils.existFile(INTELLI_PROFILES)) {
            String value = mUtils.readFile(INTELLI_PROFILES);
            if (value != null) return Integer.parseInt(value);
        }
        return 0;
    }

    //Termal Engine
    public boolean hasThermalEngine(){//Check if exist
        return mUtils.existFile(THERMAL_ENABLED);
    }

    public boolean isThermalEngineActive(){ //Check if is enabled
        return mUtils.readFile(THERMAL_ENABLED).equals("1");
    }

/*PTSDSD
*
 */
public static String ReadableByteCount(long bytes) {
    if (bytes < 1024) return bytes + " B";
    int exp = (int) (Math.log(bytes) / Math.log(1024));
    String pre = String.valueOf("KMGTPE".charAt(exp - 1));
    return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
}

    public static void getTabList(String strTitle, final ViewPager vp, Activity activity) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
        alertDialogBuilder.setTitle(strTitle);

        List<String> listItems = new ArrayList<String>();
        for (byte i = 0; i < vp.getAdapter().getCount(); i++) {
            listItems.add(vp.getAdapter().getPageTitle(i).toString());
        }
        alertDialogBuilder.setItems(listItems.toArray(new CharSequence[listItems.size()]),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        vp.setCurrentItem(which);
                    }
                }
        ).show();
    }

    public static boolean is_Tab_available(int i) {
        boolean flag;
        switch (i) {
            case 4:
                flag = Helpers.voltageFileExists();
                break;
            default:
                flag = true;
                break;
        }
        return flag;
    }

    public static String bln_path() {
        if (new File("/sys/class/misc/backlightnotification/enabled").exists()) {
            return "/sys/class/misc/backlightnotification/enabled";
        } else if (new File("/sys/class/leds/button-backlight/blink_buttons").exists()) {
            return "/sys/class/leds/button-backlight/blink_buttons";
        } else {
            return null;
        }
    }

    public static String fastcharge_path() {
        if (new File("/sys/kernel/fast_charge/force_fast_charge").exists()) {
            return "/sys/kernel/fast_charge/force_fast_charge";
        } else if (new File("/sys/module/msm_otg/parameters/fast_charge").exists()) {
            return "/sys/module/msm_otg/parameters/fast_charge";
        } else if (new File("/sys/devices/platform/htc_battery/fast_charge").exists()) {
            return "/sys/devices/platform/htc_battery/fast_charge";
        } else {
            return null;
        }
    }

    /*//Work in progress fastcharge 2.0
    public static String fastcharge2_path(){
        ///
    }*/

    //Fsync
    public static String fsync_path() {
        if (new File("/sys/class/misc/fsynccontrol/fsync_enabled").exists()) {
            return "/sys/class/misc/fsynccontrol/fsync_enabled";
        } else if (new File("/sys/module/sync/parameters/fsync_enabled").exists()) {
            return "/sys/module/sync/parameters/fsync_enabled";
        } else {
            return null;
        }
    }

    public static String touch2wake_path() {
        if (new File("/sys/module/lge_touch_core/parameters/doubletap_to_wake").exists()) {
            return "/sys/module/lge_touch_core/parameters/doubletap_to_wake";
        } else if (new File("/sys/module/lge_touch_core/parameters/touch_to_wake").exists()) {
            return "/sys/module/lge_touch_core/parameters/touch_to_wake";
        } else {
            return null;
        }
    }

    public static String wifipm_path() {
        if (new File("/sys/module/bcmdhd/parameters/wifi_pm").exists()) {
            return "/sys/module/bcmdhd/parameters/wifi_pm";
        } else if (new File("/sys/module/bcmdhd/parameters/wifi_fast").exists()) {
            return "/sys/module/bcmdhd/parameters/wifi_fast";
        } else if (new File("/sys/module/dhd/parameters/wifi_pm").exists()) {
            return "/sys/module/dhd/parameters/wifi_pm";
        } else {
            return null;
        }
    }

    //Nexus 4
    public static String hotplug_path() {
        if (new File("/sys/devices/virtual/misc/mako_hotplug_control").exists()) {
            return "/sys/devices/virtual/misc/mako_hotplug_control";
        } else if (new File("/sys/class/misc/mako_hotplug_control").exists()) {
            return "/sys/class/misc/mako_hotplug_control";
        } else if (new File("/sys/module/auto_hotplug/parameters").exists()) {
            return "/sys/module/auto_hotplug/parameters";
        } else if (new File("/sys/module/dyn_hotplug/parameters").exists()) {
            return "/sys/module/dyn_hotplug/parameters";
        } else if (new File("/sys/class/misc/tegra_hotplug_control").exists()) {
            return "/sys/class/misc/tegra_hotplug_control";
        } else {
            return null;
        }
    }

    public static String extSD() {
        String externalsd = "";

        if (!TextUtils.isEmpty(System.getenv("SECONDARY_STORAGE"))) {
            final String externalstorage[] = System.getenv("SECONDARY_STORAGE").split(":");
            for (final String dirs : externalstorage) {
                final File dir = new File(dirs);
                if (dir.exists() && dir.isDirectory() && dir.canRead() && dir.canWrite()) {
                    externalsd = dirs;
                    break;
                }
            }
        } else {
            final String supported[] = {"/mnt/extSdCard", "/storage/sdcard1", "/mnt/external_sd"};
            for (final String dirs : supported) {
                final File dir = new File(dirs);
                if (dir.exists() && dir.isDirectory() && dir.canRead() && dir.canWrite()) {
                    externalsd = dirs;
                    break;
                }
            }
        }
        return externalsd;
    }

    /*
    *finish
     */
}//finaliza la clase Helpers
