package com.sonhoai.groups.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sonhoai.groups.DataModels.Class;
import com.sonhoai.groups.R;

import java.util.List;

public class ClassesAdapter extends BaseAdapter {

    private Context context;
    private List<Class> classes;

    public ClassesAdapter(Context context, List<Class> classes) {
        this.context = context;
        this.classes = classes;
    }

    @Override
    public int getCount() {
        return classes.size();
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
            view = LayoutInflater.from(context).inflate(R.layout.item_class, viewGroup, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.txtName.setText(classes.get(i).getName());
        holder.txtInfo.setText(classes.get(i).getInfo());
        holder.txtUser.setText(classes.get(i).getUser());
        return view;
    }

    private class ViewHolder{
        private TextView txtName,txtInfo,txtUser;
        ViewHolder(View view){
            txtName = view.findViewById(R.id.txtClassName);
            txtInfo = view.findViewById(R.id.txtClassInfo);
            txtUser = view.findViewById(R.id.txtClassPerson);
        }
    }
}
