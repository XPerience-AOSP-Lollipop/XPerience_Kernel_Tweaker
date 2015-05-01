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

package mx.klozz.xperience.tweaker.tabs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import mx.klozz.xperience.tweaker.R;

import java.util.ArrayList;

public class TabAdapter extends ArrayAdapter<Tab> {
    private ArrayList<Tab> TabList;
    private Context context;

    public TabAdapter(Context context, int textViewResourceId, ArrayList<Tab> TabList) {
        super(context, textViewResourceId, TabList);
        this.context = context;
        this.TabList = new ArrayList<Tab>();
        this.TabList.addAll(TabList);
    }


    public class ViewHolder {
        public TextView name;
        public ImageView status;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.tab_item, parent, false);

            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.label);
            holder.status = (ImageView) convertView.findViewById(R.id.status);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Tab t = getItem(position);
        holder.name.setText(t.getName());
        if (t.isSelected()) {
            holder.status.setImageDrawable(context.getResources().getDrawable(android.R.drawable.checkbox_on_background));
        } else {
            holder.status.setImageDrawable(context.getResources().getDrawable(android.R.drawable.checkbox_off_background));
        }

        return convertView;

    }

}