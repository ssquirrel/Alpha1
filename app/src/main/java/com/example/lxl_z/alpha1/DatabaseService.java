package com.example.lxl_z.alpha1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

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
    static final String CITY_TABLE = "CITY_TABLE";
    static final String CITY_COL = "CITY_COL";
    static final String OWM_ID_COL = "OWM_ID_COL";

    private static DatabaseService instance = null;

    private SQLiteDatabase db;

    private Map<String, String> map = new HashMap<>();

    String getID(String city) {
        return map.get(city);
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

    private void addID(String city, String id) {
        map.put(city, id);
    }

    private static DatabaseService getInstance(Context context) {
        if (instance == null)
            instance = new DatabaseService(context.getApplicationContext());

        return instance;
    }


    static class QueryService {
        private List<String> city = new ArrayList<>();
        private List<String> id = new ArrayList<>();

        private List<String> result = new ArrayList<>();

        private DatabaseService dbService;

        QueryService(Context context) {
            dbService = DatabaseService.getInstance(context);
        }

        List<String> query(String text) {
            if (text.isEmpty()) {
                city.clear();
                id.clear();
                return city;
            } else if (city.isEmpty()) {
                QUERY_SLOW_PATH(text);
                return city;
            } else {
                QUERY_FAST_PATH(text);
                return result;
            }

        }

        void confirm(String c) {
            int idx = city.indexOf(c);
            dbService.addID(city.get(idx), id.get(idx));
        }

        private void QUERY_FAST_PATH(String text) {
            result.clear();

            for (String str : city) {
                if (str.indexOf(text) == 0)
                    result.add(str);

            }
        }

        private void QUERY_SLOW_PATH(String text) {
            SQLiteDatabase db = dbService.db;

            try (Cursor cr = db.query(DatabaseService.CITY_TABLE,
                    new String[]{DatabaseService.CITY_COL},
                    DatabaseService.CITY_COL + " MATCH ?",
                    new String[]{text + "*"},
                    null, null, null)) {

                if (cr.moveToFirst()) {

                    while (!cr.isAfterLast()) {
                        city.add(cr.getString(cr.getColumnIndex(DatabaseService.CITY_COL)));
                        id.add(cr.getString(cr.getColumnIndex(DatabaseService.OWM_ID_COL)));

                        cr.moveToNext();
                    }
                }
            }

        }


    }

}
