package com.example.lxl_z.alpha1;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.lxl_z.alpha1.Weather.AQI;
import com.example.lxl_z.alpha1.Weather.Request;
import com.example.lxl_z.alpha1.Weather.Response;
import com.example.lxl_z.alpha1.Weather.Weather;

import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity
        implements AsyncWeatherService.OnLoadDoneCallback {
    private static final int PICK_FAVORITES_REQUEST = 0;

    private RecyclerView.Adapter favoritesAdapter = new FavoritesAdapter();

    List<Request> data = new ArrayList<>();
    List<AQI> aqi = new ArrayList<>();
    List<Weather> current = new ArrayList<>();

    AsyncWeatherService.AsyncWeatherTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favorites_activity);


        RecyclerView favorites = (RecyclerView) findViewById(R.id.favorites_list);
        favorites.setLayoutManager(new LinearLayoutManager(this));
        favorites.setAdapter(favoritesAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.favorites_add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FavoritesActivity.this, SearchActivity.class);
                startActivityForResult(intent, PICK_FAVORITES_REQUEST);
            }
        });

        task = AsyncWeatherService.getInstance(this).newCurrentWeatherTask(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        task.execute(data);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (requestCode == PICK_FAVORITES_REQUEST && resultCode == RESULT_OK) {
            String city = intent.getStringExtra(SearchActivity.EXTRA_CITY);
            Request request = new Request(city);

            data.add(request);
            aqi.add(null);
            current.add(null);
        }
    }

    @Override
    public void onLoadDone(Response response) {
        int idx = -1;
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).city.equals(response.city)) {
                idx = i;
                break;
            }
        }

        if (idx == -1 || data.get(idx).rev == response.rev)
            return;


        current.set(idx, response.weather.get(0));
        //not every city has aqi
        if (response.aqi.size() > 0)
            aqi.set(idx, response.aqi.get(0));

        favoritesAdapter.notifyItemChanged(idx);
    }

    private class SearchSuggestionView extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        TextView city;
        TextView temp;
        TextView aqi;

        SearchSuggestionView(View v) {
            super(v);

            city = (TextView) v.findViewById(R.id.city);
            temp = (TextView) v.findViewById(R.id.temp);
            aqi = (TextView) v.findViewById(R.id.aqi);
        }

        @Override
        public void onClick(View v) {

        }
    }

    private class FavoritesAdapter extends RecyclerView.Adapter<SearchSuggestionView> {
        public SearchSuggestionView onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.favorites_item, parent, false);

            return new SearchSuggestionView(v);
        }

        @Override
        public void onBindViewHolder(SearchSuggestionView holder, int position) {
            holder.city.setText(data.get(position).city);


            if (aqi.get(position) != null)
                holder.aqi.setText(String.valueOf(aqi.get(position).aqi));
            else
                holder.aqi.setText("");

            if (current.get(position) != null)
                holder.temp.setText(String.valueOf(current.get(position).temp));
            else
                holder.temp.setText("");
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }

}
