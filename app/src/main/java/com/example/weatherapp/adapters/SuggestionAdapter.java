package com.example.weatherapp.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import androidx.recyclerview.widget.RecyclerView;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;
import com.example.weatherapp.R;
import com.example.weatherapp.domains.models.Suggestion;
import com.example.weatherapp.domains.RvLocationSuggestionInterface;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;


public class SuggestionAdapter extends RecyclerView.Adapter<SuggestionAdapter.viewHolder> {
    private final RvLocationSuggestionInterface rvLocationSuggestionInterface;
    private ArrayList<Suggestion> suggestionArrayList;
    private Context context;

    public SuggestionAdapter(ArrayList<Suggestion> suggestionArrayList, RvLocationSuggestionInterface rvLocationSuggestionInterface) {
        this.suggestionArrayList = suggestionArrayList;
        this.rvLocationSuggestionInterface = rvLocationSuggestionInterface;
    }

    @NonNull
    @Override
    public SuggestionAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.location_suggestion, parent, false);
        context = parent.getContext();
        return new viewHolder(view, rvLocationSuggestionInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        holder.cityName.setText(suggestionArrayList.get(position).getCityName());
        holder.displayName.setText(suggestionArrayList.get(position).getDisplayName());

        String imageUrl = String.format("https://open-meteo.com/images/country-flags/%s.svg",
                suggestionArrayList.get(position).getCountryCode());

        new Thread(() -> {
            try {
                InputStream inputStream = new URL(imageUrl).openStream();

                SVG svg = SVG.getFromInputStream(inputStream);

                Drawable drawable = new PictureDrawable(svg.renderToPicture());

                holder.countryFlag.post(() -> {
                    holder.countryFlag.setLayerType(View.LAYER_TYPE_SOFTWARE, null); // Disable hardware acceleration
                    holder.countryFlag.setImageDrawable(drawable);
                });
            } catch (IOException | SVGParseException e) {
                e.printStackTrace();
                holder.countryFlag.post(() -> holder.countryFlag.setImageResource(R.drawable.ic_launcher_background));
            }
        }).start();
    }

    @Override
    public int getItemCount() {
        return suggestionArrayList.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder {
        TextView cityName;
        TextView displayName;
        ImageView countryFlag;

        public viewHolder(@NonNull View itemView, RvLocationSuggestionInterface rvLocationSuggestionInterface) {
            super(itemView);
            cityName = itemView.findViewById(R.id.city_name);
            displayName = itemView.findViewById(R.id.display_name);
            countryFlag = itemView.findViewById(R.id.country_flag);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (rvLocationSuggestionInterface != null) {
                        int pos = getAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            rvLocationSuggestionInterface.onItemClick(pos);
                        }
                    }
                }
            });
        }
    }
}

