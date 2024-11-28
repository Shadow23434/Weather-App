package com.example.weatherapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapp.R;
import com.example.weatherapp.favourite.LocationAdapter;
import com.example.weatherapp.favourite.LocationData;
import com.example.weatherapp.startup.SignIn;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FavouriteActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LocationAdapter locationAdapter;
    private List<LocationData> locationDataList;
    private FirebaseAuth fAuth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite);

        recyclerView = findViewById(R.id.rv_location_favourite);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        locationDataList = new ArrayList<>();
        locationAdapter = new LocationAdapter(locationDataList, locationData -> {
            Intent intent = new Intent(FavouriteActivity.this, MainActivity.class);
            intent.putExtra("latitude", locationData.getLatitude());
            intent.putExtra("longitude", locationData.getLongitude());
            startActivity(intent);
        });
        recyclerView.setAdapter(locationAdapter);
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                deleteLocationData(position);
            }
        };
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerView);

        fAuth = FirebaseAuth.getInstance();
        user = fAuth.getCurrentUser();
        if(user == null){
            Intent intent = new Intent(FavouriteActivity.this, SignIn.class);
            startActivity(intent);
            finish();
        }
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("user").child(user.getUid()).child("favourite");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    LocationData locationData = dataSnapshot.getValue(LocationData.class);
                    if(locationData != null){
                        Log.d("Firebase", "Favourite location is show" + locationData);
                        locationDataList.add(locationData);
                    }
                }
                locationAdapter.notifyDataSetChanged();
                Log.d("Firebase", "Favourite location is show");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Favourite location failed show");
            }
        });

        onClickBackButton();
        onClickBottomAppBar();
        onClickFloatingButton();
    }

    private void deleteLocationData(int position) {
        LocationData locationData = locationDataList.get(position);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("user").child(user.getUid()).child("favourite").child(locationData.getKey());
        databaseReference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    locationDataList.remove(position);
                    locationAdapter.notifyItemRemoved(position);
                    Toast.makeText(FavouriteActivity.this, "Delete is successfully", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(FavouriteActivity.this, "Delete is failed", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void onClickBackButton() {
        ImageView backBtn = findViewById(R.id.fav_back_btn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FavouriteActivity.this, MainActivity.class));
                finish();
            }
        });
    }
    
    private void onClickBottomAppBar() {
        ImageView home_appBar_icon = findViewById(R.id.home_appBar_icon);
        ImageView search_appBar_icon = findViewById(R.id.search_appBar_icon);
        ImageView account_appBar_icon = findViewById(R.id.account_appBar_icon);

        home_appBar_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FavouriteActivity.this, MainActivity.class));
            }
        });

        search_appBar_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FavouriteActivity.this, SearchActivity.class));
            }
        });

        account_appBar_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FavouriteActivity.this, AccountActivity.class));
            }
        });
    }

    private void onClickFloatingButton() {
        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(FavouriteActivity.this, AssistantActivity.class));
            }
        });
    }
}