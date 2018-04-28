package com.sonhoai.groups.Adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sonhoai.groups.DataModels.Message;
import com.sonhoai.groups.R;

import java.util.List;

public class MessageAdapter extends BaseAdapter {
    private List<Message> messages;
    private Context context;

    public MessageAdapter(List<Message> messages, Context context) {
        this.messages = messages;
        this.context = context;
    }

    @Override
    public int getCount() {
        return messages.size();
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
            view = LayoutInflater.from(context).inflate(R.layout.item_chat, viewGroup, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.txtName.setText(messages.get(i).getNameUser());
        holder.txtContent.setText(Html.fromHtml(messages.get(i).getContent()));
        holder.txtDate.setText(messages.get(i).getDate());
        return view;
    }

    private class ViewHolder{
        private TextView txtName,txtContent,txtDate;
        ViewHolder(View view){
            txtName = view.findViewById(R.id.txtNameChat);
            txtContent = view.findViewById(R.id.txtContentChat);
            txtDate = view.findViewById(R.id.txtDateChat);
        }
    }
}
