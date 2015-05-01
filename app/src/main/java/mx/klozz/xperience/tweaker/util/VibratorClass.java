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

package mx.klozz.xperience.tweaker.util;

import android.util.Log;

import java.io.File;

import mx.klozz.xperience.tweaker.helpers.Helpers;

/**
 * Created by klozz on 16.03.2015.
 */
public class VibratorClass implements Constants {
    private int max = 0;
    private int min = 0;
    private String path = null;

    public int get_min() {
        return min;
    }

    public int get_max() {
        return max;
    }

    public String get_val(String p) {
        return getOnlyNumerics(Helpers.LeerUnaLinea(p));
    }

    public String get_path() {
        if (new File("/sys/class/vibetonz/immDuty/pwmvalue_intensity").exists()) {
            this.min = 0;
            this.max = 127;
            this.path = "/sys/class/vibetonz/immDuty/pwmvalue_intensity";
            Log.d(TAG, "vibe path detected: " + this.path);
        } else if (new File("/sys/vibrator/pwmvalue").exists()) {
            this.min = 0;
            this.max = 127;
            this.path = "/sys/vibrator/pwmvalue";
            Log.d(TAG, "vibe path detected: " + this.path);
        } else if (new File("/sys/class/misc/vibratorcontrol/vibrator_strength").exists()) {
            this.min = 1000;
            this.max = 1600;
            this.path = "/sys/class/misc/vibratorcontrol/vibrator_strength";
            Log.d(TAG, "vibe path detected: " + this.path);
        } else if (new File("/sys/vibe/pwmduty").exists()) {
            this.min = 1000;
            this.max = 1450;
            this.path = "/sys/vibe/pwmduty";
            Log.d(TAG, "vibe path detected: " + this.path);
        } else if (new File("/sys/class/timed_output/vibrator/amp").exists()) {
            this.min = 0;
            this.max = 100;
            this.path = "/sys/class/timed_output/vibrator/amp";
            Log.d(TAG, "vibe path detected: " + this.path);
        } else if (new File("/sys/class/misc/pwm_duty/pwm_duty").exists()) {
            this.min = 0;
            this.max = 100;
            this.path = "/sys/class/misc/pwm_duty/pwm_duty";
            Log.d(TAG, "vibe path detected: " + this.path);
        } else if (new File("/sys/devices/virtual/timed_output/vibrator/voltage_level").exists()) {
            this.min = 1200;
            this.max = 3100;
            this.path = "/sys/devices/virtual/timed_output/vibrator/voltage_level";
            Log.d(TAG, "vibe path detected: " + this.path);
        } else {
            this.path = null;
            Log.d(TAG, "vibe path not detected");
        }
        return this.path;
    }

    private String getOnlyNumerics(String str) {
        if (str == null) {
            Log.e(TAG, "vibe value read error");
            return "0";
        }
        StringBuffer strBuff = new StringBuffer();
        char c;
        for (int i = 0; i < str.length(); i++) {
            c = str.charAt(i);
            if (Character.isDigit(c)) {
                strBuff.append(c);
            }
        }
        return strBuff.toString();
    }


}
