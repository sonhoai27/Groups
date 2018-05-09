package com.sonhoai.groups.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sonhoai.groups.DataModels.Message;
import com.sonhoai.groups.R;
import com.sonhoai.groups.Uti.HandleFBAuth;

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
        if(messages.get(i).getIdUser().equals(HandleFBAuth.firebaseAuth.getUid())){
            holder.txtName.setText(messages.get(i).getNameUser());
            holder.txtContent.setText(Html.fromHtml(messages.get(i).getContent()));
            holder.txtDate.setText(messages.get(i).getDate());

            holder.layoutItemChat.setGravity(Gravity.RIGHT);
            holder.txtContent.setBackgroundResource(R.drawable.bg_message_2);

            holder.txtContent.setTextColor(Color.WHITE);
        }else if(!messages.get(i).getIdUser().equals(HandleFBAuth.firebaseAuth.getUid())){
            holder.txtName.setText(messages.get(i).getNameUser());
            holder.txtContent.setText(Html.fromHtml(messages.get(i).getContent()));
            holder.txtDate.setText(messages.get(i).getDate());

            holder.layoutItemChat.setGravity(Gravity.LEFT);

            holder.txtContent.setBackgroundResource(R.drawable.bg_message);

            holder.txtContent.setTextColor(Color.GRAY);
        }
        try {
            Log.i("AAAAA", messages.get(i).getContent().substring(0, 2));
            if(messages.get(i).getContent().substring(0, 2).equals("<f")){
                holder.txtContent.setBackgroundResource(R.drawable.bg_gradient_message);
                holder.txtContent.setTextColor(Color.WHITE);
            }
        }catch (Exception e){

        }


        return view;
    }

    private class ViewHolder{
        private TextView txtName,txtContent,txtDate;
        private LinearLayout layoutItemChat;
        ViewHolder(View view){
            layoutItemChat = view.findViewById(R.id.layoutItemChat);
            txtName = view.findViewById(R.id.txtNameChat);
            txtContent = view.findViewById(R.id.txtContentChat);
            txtDate = view.findViewById(R.id.txtDateChat);
        }
    }
}
