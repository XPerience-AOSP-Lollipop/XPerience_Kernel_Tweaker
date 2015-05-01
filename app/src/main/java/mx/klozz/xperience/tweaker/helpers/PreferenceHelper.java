/*
 * Copyright (C) 2014 Carlos Jesús <TeamMEX@XDA-Developers>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */

package mx.klozz.xperience.tweaker.helpers;

import android.app.Activity;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;

public class PreferenceHelper {

    public PreferenceCategory setPreferenceCategory(String title,
                                                    Activity activity) {
        PreferenceCategory mCategory = new PreferenceCategory(activity);
        if (title != null) mCategory.setTitle(title);
        return mCategory;
    }

    public Preference setPreference(String title, String summary,
                                    Activity activity) {
        Preference mPref = new Preference(activity);
        if (title != null) mPref.setTitle(title);
        if (summary != null) mPref.setSummary(summary);
        return mPref;
    }

    public CheckBoxPreference setCheckBoxPreference(boolean checked,
                                                    String title, String summary, Activity activity) {
        CheckBoxPreference mBox = new CheckBoxPreference(activity);
        mBox.setChecked(checked);
        if (title != null) mBox.setTitle(title);
        if (summary != null) mBox.setSummary(summary);
        return mBox;
    }
}
