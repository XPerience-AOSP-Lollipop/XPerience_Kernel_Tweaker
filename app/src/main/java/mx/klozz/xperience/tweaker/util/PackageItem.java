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

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import mx.klozz.xperience.tweaker.MainActivity;

/**
 * Created by klozz on 16.03.2015.
 */
public class PackageItem {
    private String packname, appname;

    public PackageItem(String p) {
        try {
            PackageInfo packageInfo = MainActivity.contexto.getPackageManager().getPackageInfo(p, 0);
            this.appname = MainActivity.contexto.getPackageManager().getApplicationLabel(packageInfo.applicationInfo).toString();
        } catch (PackageManager.NameNotFoundException e) {
            //e.printStackTrace();
            this.appname = "";
        }
        this.packname = p;
    }

    public String getPackName() {
        return packname;
    }

    public String getAppName() {
        return appname;
    }

}
