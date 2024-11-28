package com.example.weatherapp.favourite;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.weatherapp.R;

import java.util.List;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ItemViewHolder> {
    private List<LocationData> locationDataList;
    private FavLocationInterface favLocationInterface;
    public LocationAdapter(List<LocationData> locationDataList, FavLocationInterface favLocationInterface) {
        this.locationDataList = locationDataList;
        this.favLocationInterface = favLocationInterface;
    }

    @NonNull
    @Override
    public LocationAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.location_favourite, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        LocationData locationData = locationDataList.get(position);
        holder.favCityName.setText(locationData.getCityName());
        holder.favCountryName.setText(locationData.getCountryName());
        holder.favTemp.setText(String.format("%d°C", locationData.getTemperature()));
        holder.favDescription.setText(locationData.getDescription());
        int resId = holder.itemView.getContext().getResources().getIdentifier(locationData.getIcon(), "drawable", holder.itemView.getContext().getPackageName());
        Glide.with(holder.itemView.getContext())
                .load(resId)
                .into(holder.favIcon);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                favLocationInterface.onItemClick(locationData);
            }
        });
    }

    @Override
    public int getItemCount() {
        return locationDataList.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView favCityName;
        TextView favCountryName;
        TextView favTemp;
        TextView favDescription;
        ImageView favIcon;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            favCityName = itemView.findViewById(R.id.fav_city_name);
            favCountryName = itemView.findViewById(R.id.fav_country_name);
            favTemp = itemView.findViewById(R.id.fav_temp);
            favDescription = itemView.findViewById(R.id.fav_weather_description);
            favIcon = itemView.findViewById(R.id.fav_weather_icon);
        }
    }
}

