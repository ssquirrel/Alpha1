package com.example.lxl_z.alpha1.Weather;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.lxl_z.alpha1.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by LXL_z on 8/26/2016.
 */
public class DatabaseService {
    public static final String CITY_TABLE = "CITY_TABLE";
    public static final String CITY_COL = "CITY_COL";
    public static final String OWM_ID_COL = "OWM_ID_COL";

    private static DatabaseService instance = null;

    private SQLiteDatabase db;

    private Map<String, CityID> map = new HashMap<>();

    CityID getID(String city) {
        CityID cityID = map.get(city);

        if (cityID != null)
            return cityID;

        cityID = new CityID();

        try (Cursor cr = db.query(DatabaseService.CITY_TABLE,
                null,
                DatabaseService.CITY_COL + " MATCH ?",
                new String[]{city},
                null, null, null)) {

            cr.moveToFirst();

            cityID.owmID = cr.getString(cr.getColumnIndex(DatabaseService.OWM_ID_COL));

            //only the following cities have state air id;
            switch (city) {
                case "Beijing":
                    cityID.stateAirID = "1";
                    break;
                case "Chengdu":
                    cityID.stateAirID = "2";
                    break;
                case "Guangzhou":
                    cityID.stateAirID = "3";
                    break;
                case "Shanghai":
                    cityID.stateAirID = "4";
                    break;
                case "Shenyang":
                    cityID.stateAirID = "5";
                    break;
            }
        }

        addID(city, cityID);

        return cityID;
    }

    private DatabaseService(Context context) {
        db = context.openOrCreateDatabase("database", Context.MODE_PRIVATE, null);

        if (db.getVersion() == 0) {
            db.execSQL("CREATE VIRTUAL TABLE " + CITY_TABLE +
                    " USING fts3(" +
                    CITY_COL + "," +
                    OWM_ID_COL + ")");

            db.beginTransaction();
            try (InputStream is = context.getResources().openRawResource(R.raw.list);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

                String line;

                while ((line = reader.readLine()) != null) {
                    String[] row = line.split("\\s");

                    ContentValues cv = new ContentValues(2);
                    cv.put(CITY_COL, row[0]);
                    cv.put(OWM_ID_COL, row[1]);

                    db.insert(CITY_TABLE, null, cv);
                }


                db.setTransactionSuccessful();
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage());
            } finally {
                db.endTransaction();
            }

            db.setVersion(1);
        }
    }

    private void addID(String city, CityID id) {
        map.put(city, id);
    }

    public static DatabaseService getInstance(Context context) {
        if (instance == null)
            instance = new DatabaseService(context.getApplicationContext());

        return instance;
    }

    public static class QueryService {
        private List<String> city = new ArrayList<>();
        private List<CityID> id = new ArrayList<>();

        private List<String> result = new ArrayList<>();

        private char lastSlowPath = 0;

        private DatabaseService dbService;

        public QueryService(Context context) {
            dbService = DatabaseService.getInstance(context);

        }

        public void query(String query) {
            if (query.isEmpty()) {
                result.clear();
                lastSlowPath = 0;
                return;
            }

            if (city.size() > 0 && lastSlowPath == query.charAt(0)) {
                QUERY_FAST_PATH(query);
            } else {
                QUERY_SLOW_PATH(query);
            }

            lastSlowPath = query.charAt(0);
        }

        public List<String> getResult() {
            return result;
        }

        public void confirm(String c) {
            int idx = city.indexOf(c);
            dbService.addID(city.get(idx), id.get(idx));
        }

        private boolean isPrefix(String string, String prefix) {
            return string.toLowerCase().indexOf(prefix.toLowerCase()) == 0;
        }

        private void QUERY_FAST_PATH(String query) {
            Long start = System.nanoTime();

            result.clear();

            for (String s : city) {
                if (isPrefix(s, query))
                    result.add(s);
            }

            Long end = System.nanoTime();
            Log.d("QUERY_FAST_PATH", String.valueOf(end - start));
        }

        private void QUERY_SLOW_PATH(String query) {
            Long start = System.nanoTime();

            city.clear();
            id.clear();
            result.clear();

            SQLiteDatabase db = dbService.db;

            try (Cursor cr = db.query(DatabaseService.CITY_TABLE,
                    null,
                    DatabaseService.CITY_COL + " MATCH ?",
                    new String[]{query + "*"},
                    null, null, null)) {

                if (cr.moveToFirst()) {

                    while (!cr.isAfterLast()) {
                        String c = cr.getString(cr.getColumnIndex(DatabaseService.CITY_COL));

                        city.add(c);
                        result.add(c);

                        CityID cityID = new CityID();
                        cityID.owmID = cr.getString(cr.getColumnIndex(DatabaseService.OWM_ID_COL));

                        //only the following cities have state air id;
                        switch (c) {
                            case "Beijing":
                                cityID.stateAirID = "1";
                                break;
                            case "Chengdu":
                                cityID.stateAirID = "2";
                                break;
                            case "Guangzhou":
                                cityID.stateAirID = "3";
                                break;
                            case "Shanghai":
                                cityID.stateAirID = "4";
                                break;
                            case "Shenyang":
                                cityID.stateAirID = "5";
                                break;
                        }

                        id.add(cityID);


                        cr.moveToNext();
                    }
                }

                Long end = System.nanoTime();
                Log.d("QUERY_SLOW_PATH", String.valueOf(end - start));
            }

        }


    }

}
