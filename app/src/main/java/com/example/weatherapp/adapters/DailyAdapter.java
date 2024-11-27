package com.example.weatherapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.weatherapp.R;
import com.example.weatherapp.domains.models.Daily;

import java.util.ArrayList;

public class DailyAdapter extends RecyclerView.Adapter<DailyAdapter.viewHolder> {
    ArrayList<Daily> arrayList;

    public DailyAdapter(ArrayList<Daily> arrayList) {
        this.arrayList = arrayList;
    }

    Context context;

    @NonNull
    @Override
    public DailyAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_daily, parent, false);
        context = parent.getContext();
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DailyAdapter.viewHolder holder, int position) {
        holder.day.setText(arrayList.get(position).getDay());

        int resID = holder.itemView.getResources().getIdentifier(
                arrayList.get(position).getIcon(),
                "drawable",
                holder.itemView.getContext().getPackageName()
        );
        Glide.with(context).load(resID).into(holder.weatherIcon);

        holder.description.setText(arrayList.get(position).getDescription());
        holder.maxTemp.setText(arrayList.get(position).getMaxTemp() + "°/");
        holder.minTemp.setText(arrayList.get(position).getMinTemp() + "°");
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder {
        TextView day;
        ImageView weatherIcon;
        TextView description;
        TextView maxTemp;
        TextView minTemp;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            day = itemView.findViewById(R.id.day);
            weatherIcon = itemView.findViewById(R.id.weather_icon);
            description = itemView.findViewById(R.id.weather_description);
            maxTemp = itemView.findViewById(R.id.max_temp);
            minTemp = itemView.findViewById(R.id.min_temp);
        }
    }
}
