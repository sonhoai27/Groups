package com.sonhoai.groups.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sonhoai.groups.DataModels.GroupUser;
import com.sonhoai.groups.R;

import java.util.List;

public class UserChatAdapter extends BaseAdapter{
    FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    DatabaseReference mReference;
    private Context context;
    private List<GroupUser> groupUsers;

    public UserChatAdapter(Context context, List<GroupUser> groupUsers) {
        this.context = context;
        this.groupUsers = groupUsers;
    }

    @Override
    public int getCount() {
        return groupUsers.size();
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
    public View getView(final int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_user_chat, viewGroup, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.txtName.setText(groupUsers.get(i).getName());
        holder.txtBad.setText("Xấu: "+groupUsers.get(i).getRatingBad());
        holder.txtGood.setText("Tốt: "+groupUsers.get(i).getRatingGood());
        holder.txtGood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mReference = mDatabase.getReference("memberGroup/"+groupUsers.get(i).getIdGroup()+"/"+groupUsers.get(i).getId());
                final String temp = String.valueOf(Integer.parseInt(groupUsers.get(i).getRatingGood())+1);
                GroupUser groupUser = new GroupUser(
                        groupUsers.get(i).getId(),
                        groupUsers.get(i).getName(),
                        temp,
                        groupUsers.get(i).getRatingBad(),
                        groupUsers.get(i).getIdGroup()
                );
                mReference.setValue(groupUser);
            }
        });
        holder.txtBad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mReference = mDatabase.getReference("memberGroup/" + groupUsers.get(i).getIdGroup() + "/" + groupUsers.get(i).getId());
                final String temp = String.valueOf(Integer.parseInt(groupUsers.get(i).getRatingBad()) + 1);
                GroupUser groupUser = new GroupUser(
                        groupUsers.get(i).getId(),
                        groupUsers.get(i).getName(),
                        groupUsers.get(i).getRatingGood(),
                        temp,
                        groupUsers.get(i).getIdGroup()
                );
                mReference.setValue(groupUser);
            }
        });
        return view;
    }

    private class ViewHolder{
        private TextView txtName, txtGood,txtBad;
        ViewHolder(View view){
            txtName = view.findViewById(R.id.txtUserName);
            txtGood = view.findViewById(R.id.txtGood);
            txtBad = view.findViewById(R.id.txtBad);
        }
    }
}
