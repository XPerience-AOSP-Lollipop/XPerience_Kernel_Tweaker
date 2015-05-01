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

/**
 * Created by klozz on 16.03.2015.
 */
public class Prop implements Comparable<Prop> {

    private String name;
    private String data;

    public Prop(String n, String d) {
        name = n;
        data = d;
    }

    public String getName() {
        return name;
    }

    public void setName(String d) {
        this.name = d;

    }

    public String getVal() {
        return data;
    }

    public void setVal(String d) {
        this.data = d;
    }

    public int compareTo(Prop o) {
        if (this.name != null)
            return this.name.toLowerCase().compareTo(o.getName().toLowerCase());
        else
            throw new IllegalArgumentException();
    }
}
