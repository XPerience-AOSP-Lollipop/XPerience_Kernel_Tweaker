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

import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mx.klozz.xperience.tweaker.helpers.Helpers;

public class PropUtil implements Constants {
    private static Set<String> exclude = new HashSet<String>();

    public static void add_exclude(String s) {
        exclude.add(s);
    }

    public static List<Prop> load_prop(String s) {
        List<Prop> props = new ArrayList<Prop>();
        props.clear();
        if (s == null) return props;
        final String p[] = s.split("\0");
        for (String aP : p) {
            try {
                if (aP != null) {
                    String pn = aP;
                    pn = pn.substring(pn.lastIndexOf("/") + 1, pn.length()).trim();
                    if (!exclude.contains(pn))
                        props.add(new Prop(pn, Helpers.LeerUnaLinea(aP).trim()));
                }
            } catch (Exception e) {
            }
        }
        return props;
    }

    public static void set_pref(String n, String v, String pref, SharedPreferences mPreferences) {
        final String s = mPreferences.getString(pref, "");
        final StringBuilder sb = new StringBuilder();
        if (!s.equals("")) {
            String p[] = s.split(";");
            for (String aP : p) {
                if (aP != null && aP.contains(":")) {
                    final String pn[] = aP.split(":");
                    if (!pn[0].equals(n)) sb.append(pn[0]).append(':').append(pn[1]).append(';');
                }
            }
        }
        sb.append(n).append(':').append(v).append(';');
        mPreferences.edit().putString(pref, sb.toString()).commit();
    }
}
