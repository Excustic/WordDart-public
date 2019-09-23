package com.example.worddart;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserPreviewAdapter extends RecyclerView.Adapter<UserPreviewAdapter.UserViewHolder> {

    private Context mContext;
    private ArrayList<UserPreview> userList;

    public UserPreviewAdapter(Context mCtx, ArrayList<UserPreview> productList)
    {
        this.mContext = mCtx;
        this.userList = productList;
    }
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.lobby_list, null);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        //getting the product of the specified position
        UserPreview user = userList.get(position);
        //binding the data with the viewholder views
        holder.profName.setText(user.getName());
        holder.circleImageView.setImageBitmap(user.getProfImage());
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
    private CircleImageView circleImageView;
    private TextView profName;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            circleImageView=itemView.findViewById(R.id.lobby_list_prof);
            profName=itemView.findViewById(R.id.lobby_list_name);
        }
    }
}
