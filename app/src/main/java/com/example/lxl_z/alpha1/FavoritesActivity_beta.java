package com.example.lxl_z.alpha1;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Created by Administrator on 9/9/2016.
 */
public class FavoritesActivity_beta extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites_beta);

        findViewById(R.id.city).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FavoritesActivity_beta.this, DetailActivity_beta.class);
                startActivity(intent);
            }
        });
    }
}
