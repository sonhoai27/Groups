package com.sonhoai.groups.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sonhoai.groups.DataModels.FileShare;
import com.sonhoai.groups.R;

import java.util.List;
import java.util.regex.Pattern;

public class FileAdapter extends BaseAdapter {
    private Context context;
    private List<FileShare> fileShareList;

    public FileAdapter(Context context, List<FileShare> fileShareList) {
        this.context = context;
        this.fileShareList = fileShareList;
    }

    @Override
    public int getCount() {
        return fileShareList.size();
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
            view = LayoutInflater.from(context).inflate(R.layout.item_file, viewGroup, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.txtName.setText(fileShareList.get(i).getNameFile().split(Pattern.quote(":"))[1]);
        holder.txtDate.setText(fileShareList.get(i).getDate());
        return view;
    }

    private class ViewHolder{
        private TextView txtName, txtDate;
        ViewHolder(View view){
            txtName = view.findViewById(R.id.txtFileName);
            txtDate = view.findViewById(R.id.txtFileTimeUpload);
        }
    }
}
