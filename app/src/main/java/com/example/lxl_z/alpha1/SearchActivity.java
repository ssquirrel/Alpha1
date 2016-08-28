package com.example.lxl_z.alpha1;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    static final String EXTRA_CITY = DatabaseService.CITY_COL;
    static final String EXTRA_OWM_ID = DatabaseService.OWM_ID_COL;

    private RecyclerView.Adapter suggestionAdapter = new SuggestionAdapter();

    private TextView resultEmptyText;
    private RecyclerView suggestion;

    private DatabaseService.QueryService queryService;

    private List<String> data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity);

        resultEmptyText = (TextView) findViewById(R.id.result_empty_text);

        suggestion = (RecyclerView) findViewById(R.id.suggestion_list);
        suggestion.setLayoutManager(new LinearLayoutManager(this));
        suggestion.setAdapter(suggestionAdapter);

        queryService = new DatabaseService.QueryService(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        SearchView sv =
                (SearchView) menu.findItem(R.id.search).getActionView();

        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                data = queryService.query(newText);

                if (newText.isEmpty() || !data.isEmpty()) {
                    resultEmptyText.setVisibility(View.INVISIBLE);
                    suggestion.setVisibility(View.VISIBLE);

                    suggestionAdapter.notifyDataSetChanged();
                } else {
                    resultEmptyText.setVisibility(View.VISIBLE);
                    suggestion.setVisibility(View.INVISIBLE);
                }

                return true;
            }
        });

        return true;
    }

    private class SuggestionAdapter extends RecyclerView.Adapter<SearchHintView> {
        public SearchHintView onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.search_suggestion, parent, false);

            return new SearchHintView(v);
        }

        @Override
        public void onBindViewHolder(SearchHintView holder, int position) {
            holder.city.setText(data.get(position));

        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }

    private class SearchHintView extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        TextView city;

        SearchHintView(View v) {
            super(v);

            city = (TextView) v.findViewById(R.id.suggestion);
        }

        @Override
        public void onClick(View v) {
            int idx = this.getAdapterPosition();
            String city = data.get(idx);


            queryService.confirm(city);

            finish();
        }
    }
}
