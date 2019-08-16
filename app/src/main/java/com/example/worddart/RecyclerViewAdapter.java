package com.example.worddart;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{
    private static String TAG="RecyclerViewAdapter";
    private ArrayList<String> profiles=new ArrayList<>();
    private ArrayList<String> names=new ArrayList<>();
    private Context context;
    public RecyclerViewAdapter(ArrayList<String> profiles, ArrayList<String> names, Context context) {
        this.profiles = profiles;
        this.names = names;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem,parent,false);
        ViewHolder holder=new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG,"OnBindViewHolder: called");
        Glide.with(context).asBitmap().load(profiles.get(position)).into(holder.profile);
        holder.name.setText(names.get(position));
    }

    @Override
    public int getItemCount() {
        return names.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profile;
        TextView name;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profile=itemView.findViewById(R.id.list_profile);
            name=itemView.findViewById(R.id.list_name);
        }
    }
}
