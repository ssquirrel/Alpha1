package com.example.lxl_z.alpha1;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.lxl_z.alpha1.Weather.AsyncWeatherService;
import com.example.lxl_z.alpha1.Weather.DatabaseService;
import com.example.lxl_z.alpha1.Weather.Response;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity
        implements AsyncWeatherService.OnLoadDoneListener {
    private static final int PICK_FAVORITES_REQUEST = 0;
    private static final String FAVORITES_LIST = "FAVORITES_LIST.txt";

    static final String EXTRA_FAVORITES = FAVORITES_LIST;
    static final String EXTRA_INDEX = "EXTRA_INDEX";

    private RecyclerView.Adapter favoritesAdapter;
    private ItemTouchHelper itemTouchHelper;

    List<String> cities;
    List<Integer> temps;
    List<Integer> iconResIds;

    AsyncWeatherService.AsyncWeather task;

    DatabaseService.PersistenceService pService;

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

        task = AsyncWeatherService.getInstance(this).getAsyncWeather(this);

        pService = new DatabaseService.PersistenceService(this);
        cities = pService.restore();

        List<Integer> initializer = Collections.nCopies(cities.size(), null);

        temps = new ArrayList<>(initializer);
        iconResIds = new ArrayList<>(initializer);
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (cities.size() > 0)
            task.fetch(cities);

    }

    @Override
    protected void onPause() {
        super.onPause();

        pService.save(cities);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (requestCode == PICK_FAVORITES_REQUEST && resultCode == RESULT_OK) {
            String city = intent.getStringExtra(SearchActivity.EXTRA_CITY);
            if (cities.contains(city))
                return;

            cities.add(city);
            temps.add(null);
            iconResIds.add(null);
        }
    }

    @Override
    public void onLoadDone(Response response) {
        int idx = cities.indexOf(response.city);

        if (idx == -1)
            return;

        if (!response.isValid) {
            temps.set(idx, null);
            iconResIds.set(idx, null);
            favoritesAdapter.notifyItemChanged(idx);
            return;
        }

        if (temps.get(idx) != null && temps.get(idx) == response.heWeather.temp)
            return;

        temps.set(idx, response.heWeather.temp);
        iconResIds.set(idx, response.heWeather.iconResId);
        favoritesAdapter.notifyItemChanged(idx);
    }

    private class SearchSuggestionView extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        TextView city;
        TextView temp;
        ImageView icon;

        SearchSuggestionView(View v) {
            super(v);

            city = (TextView) v.findViewById(R.id.city);
            temp = (TextView) v.findViewById(R.id.temp);
            icon = (ImageView) v.findViewById(R.id.icon);

            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(FavoritesActivity.this, DetailActivity.class);
            intent.putStringArrayListExtra(EXTRA_FAVORITES, (ArrayList<String>) cities);
            intent.putExtra(EXTRA_INDEX, getAdapterPosition());

            startActivity(intent);
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

            if (temps.get(position) != null)
                holder.temp.setText(getString(R.string.temp_string, temps.get(position)));
            else
                holder.temp.setText("");

            if (temps.get(position) != null)
                holder.icon.setImageResource(iconResIds.get(position));
            else
                holder.icon.setImageDrawable(null);
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
            temps.remove(target);
            iconResIds.remove(target);

            favoritesAdapter.notifyItemRemoved(target);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView,
                              RecyclerView.ViewHolder dragged,
                              RecyclerView.ViewHolder target) {

            int from = dragged.getAdapterPosition();
            int to = target.getAdapterPosition();

            Collections.swap(cities, from, to);
            Collections.swap(temps, from, to);
            Collections.swap(iconResIds, from, to);

            favoritesAdapter.notifyItemMoved(from, to);

            return true;
        }
    }
}
