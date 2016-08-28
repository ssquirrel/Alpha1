package com.example.lxl_z.alpha1;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FavoritesActivity extends AppCompatActivity {
    private static final int PICK_FAVORITES_REQUEST = 0;

    private RecyclerView.Adapter favoritesAdapter = new FavoritesAdapter();


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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (requestCode == PICK_FAVORITES_REQUEST && resultCode == RESULT_OK) {
            String city = intent.getStringExtra(SearchActivity.EXTRA_CITY);
            String owmID = intent.getStringExtra(SearchActivity.EXTRA_OWM_ID);
        }
    }

    private class SearchHintView extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        SearchHintView(View v) {
            super(v);

        }

        @Override
        public void onClick(View v) {

        }
    }

    private class FavoritesAdapter extends RecyclerView.Adapter<SearchHintView> {
        public SearchHintView onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.favorites_item, parent, false);

            return new SearchHintView(v);
        }

        @Override
        public void onBindViewHolder(SearchHintView holder, int position) {


        }

        @Override
        public int getItemCount() {
            return 0;
        }
    }

}
