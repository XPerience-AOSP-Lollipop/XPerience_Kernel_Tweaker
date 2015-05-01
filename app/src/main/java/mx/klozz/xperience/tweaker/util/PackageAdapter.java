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


import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import mx.klozz.xperience.tweaker.R;

import java.util.ArrayList;

public class PackageAdapter extends ArrayAdapter<PackageItem> {

    private final ArrayList<PackageItem> list;
    private final Activity context;

    public PackageAdapter(Activity context, ArrayList<PackageItem> list) {
        super(context, R.layout.pack_item, list);
        this.context = context;
        this.list = list;
    }

    public void delItem(int p) {
        list.remove(p);
        notifyDataSetChanged();
    }

    public ArrayList<PackageItem> getList() {
        return list;
    }

    static class ViewHolder {
        public TextView app, pack;
        public ImageView image;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.pack_item, parent, false);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.app = (TextView) rowView.findViewById(R.id.packname);
            viewHolder.pack = (TextView) rowView.findViewById(R.id.packraw);
            viewHolder.image = (ImageView) rowView.findViewById(R.id.icon);
            rowView.setTag(viewHolder);
        }

        ViewHolder holder = (ViewHolder) rowView.getTag();
        final String npack = getItem(position).getPackName();
        holder.pack.setText(npack);
        holder.app.setText(getItem(position).getAppName());
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(npack, 0);
            holder.image.setImageDrawable(context.getPackageManager().getApplicationIcon(packageInfo.applicationInfo));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return rowView;
    }


}