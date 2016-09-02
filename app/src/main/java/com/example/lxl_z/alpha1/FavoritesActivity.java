package com.example.lxl_z.alpha1;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.lxl_z.alpha1.Weather.AQI;
import com.example.lxl_z.alpha1.Weather.AsyncWeatherService;
import com.example.lxl_z.alpha1.Weather.Response;
import com.example.lxl_z.alpha1.Weather.Weather;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity
        implements AsyncWeatherService.OnLoadDoneCallback {
    private static final int PICK_FAVORITES_REQUEST = 0;
    private static final String FAVORITES_LIST = "FAVORITES_LIST.txt";

    private RecyclerView.Adapter favoritesAdapter;
    private ItemTouchHelper itemTouchHelper;

    List<String> cities = new ArrayList<>();
    List<AQI> aqi = new ArrayList<>();
    List<Weather> weather = new ArrayList<>();

    AsyncWeatherService.AsyncWeatherTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        favoritesAdapter = new FavoritesAdapter();

        RecyclerView favorites = (RecyclerView) findViewById(R.id.favorites_list);
        favorites.setLayoutManager(new LinearLayoutManager(this));
        favorites.setAdapter(favoritesAdapter);

        itemTouchHelper = new ItemTouchHelper(new DragAndSwipe());
        itemTouchHelper.attachToRecyclerView(favorites);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.favorites_add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FavoritesActivity.this, SearchActivity.class);
                startActivityForResult(intent, PICK_FAVORITES_REQUEST);
            }
        });

        task = AsyncWeatherService.getInstance(this).newCurrentWeatherTask(this);

        if (Arrays.asList(fileList()).contains(FAVORITES_LIST)) {
            try (
                    FileInputStream fis = openFileInput(FAVORITES_LIST);
                    BufferedReader br = new BufferedReader(new InputStreamReader(fis))
            ) {
                String city;

                while ((city = br.readLine()) != null) {
                    cities.add(city);
                    aqi.add(null);
                    weather.add(null);
                }

                if (cities.size() > 0) {
                    favoritesAdapter.notifyDataSetChanged();
                }

            } catch (IOException e) {
                Log.d("Selection:onCreate", e.getMessage());
            }
        }

    }


    @Override
    protected void onResume() {
        super.onResume();

        if (cities.size() > 0)
            task.execute(cities);

    }

    @Override
    protected void onPause() {
        super.onPause();

        try (FileOutputStream fos = openFileOutput(FAVORITES_LIST, Context.MODE_PRIVATE);
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(fos));
        ) {

            for (String city : cities) {
                out.write(city);
                out.newLine();
            }

        } catch (Exception e) {
            Log.d("Selection:onPause", e.getMessage());
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (requestCode == PICK_FAVORITES_REQUEST && resultCode == RESULT_OK) {
            String city = intent.getStringExtra(SearchActivity.EXTRA_CITY);
            if (cities.contains(city))
                return;

            cities.add(city);
            aqi.add(null);
            weather.add(null);
        }
    }

    @Override
    public void onLoadDone(Response response) {
        int idx = cities.indexOf(response.city);

        if (idx == -1)
            return;

        weather.set(idx, response.weather);
        //not every city has aqi
        if (response.aqi != null)
            aqi.set(idx, response.aqi.get(0));

        favoritesAdapter.notifyItemChanged(idx);
    }

    private class SearchSuggestionView extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnTouchListener {
        TextView city;
        TextView temp;
        TextView aqi;
        View handle;

        SearchSuggestionView(View v) {
            super(v);

            city = (TextView) v.findViewById(R.id.city);
            temp = (TextView) v.findViewById(R.id.temp);
            aqi = (TextView) v.findViewById(R.id.aqi);
            handle = v.findViewById(R.id.handle);
            handle.setOnTouchListener(this);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

        }

        public boolean onTouch(View v, MotionEvent event) {
            if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                itemTouchHelper.startDrag(this);
            }


            return true;
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
            holder.city.setText(cities.get(position));

            if (aqi.get(position) != null)
                holder.aqi.setText(String.valueOf(aqi.get(position).aqi));
            else
                holder.aqi.setText("");

            if (weather.get(position) != null)
                holder.temp.setText(String.valueOf(weather.get(position).temp));
            else
                holder.temp.setText("");
        }

        @Override
        public int getItemCount() {
            return cities.size();
        }
    }

    private class DragAndSwipe extends ItemTouchHelper.SimpleCallback {
        DragAndSwipe() {
            super(ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                    ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder,
                             int direction) {
            int target = viewHolder.getAdapterPosition();

            cities.remove(target);
            aqi.remove(target);
            weather.remove(target);

            favoritesAdapter.notifyItemRemoved(target);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView,
                              RecyclerView.ViewHolder dragged,
                              RecyclerView.ViewHolder target) {

            int from = dragged.getAdapterPosition();
            int to = target.getAdapterPosition();

            Collections.swap(cities, from, to);
            Collections.swap(aqi, from, to);
            Collections.swap(weather, from, to);

            favoritesAdapter.notifyItemMoved(from, to);

            return true;
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return false;
        }
    }
}
