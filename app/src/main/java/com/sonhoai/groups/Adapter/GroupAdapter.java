package com.sonhoai.groups.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sonhoai.groups.DataModels.Group;
import com.sonhoai.groups.R;

import java.util.List;

public class GroupAdapter extends BaseAdapter {
    private List<Group> groups;
    private Context context;

    public GroupAdapter(List<Group> groups, Context context) {
        this.groups = groups;
        this.context = context;
    }

    @Override
    public int getCount() {
        return groups.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
       ViewHolder holder;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_group, viewGroup, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.txtName.setText(groups.get(i).getName());
        holder.txtContent.setText(groups.get(i).getContent());
        holder.txtDate.setText(groups.get(i).getDate());
        return view;
    }

    private class ViewHolder{
        private TextView txtName,txtContent,txtDate;
        ViewHolder(View view){
            txtName = view.findViewById(R.id.txtGroupName);
            txtContent = view.findViewById(R.id.txtGroupContent);
            txtDate = view.findViewById(R.id.txtGroupDate);
        }
    }
}
