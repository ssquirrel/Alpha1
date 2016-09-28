package com.example.lxl_z.alpha1.Weather;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.lxl_z.alpha1.R;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by LXL_z on 8/26/2016.
 */
public class DatabaseService {
    private static final String CACHE_TABLE = "CACHE_TABLE";
    private static final String CITY_COL = "CITY_COL";
    private static final String CACHE_COL = "CACHE_COL";

    private static final String CITY_TABLE = "CITY_TABLE";
    private static final String OWM_ID_COL = "OWM_ID_COL";
    private static final String HW_ID_COL = "HW_ID_COL";

    private static final int CITY_INDEX = 0;
    private static final int OWM_INDEX = 1;
    private static final int HW_INDEX = 2;

    private static DatabaseService instance = null;

    private SQLiteDatabase db;

    private Map<String, CityID> map = new HashMap<>();

    private Gson gson = new Gson();

    void putCache(Response response) {
        ContentValues cv = new ContentValues(2);
        cv.put(CITY_COL, response.city);
        cv.put(CACHE_COL, gson.toJson(response));

        db.replace(CACHE_TABLE, null, cv);
    }

    Response getCached(String city) {
        try (Cursor cr = db.query(DatabaseService.CACHE_TABLE,
                new String[]{DatabaseService.CACHE_COL},
                DatabaseService.CITY_COL + "=?",
                new String[]{city},
                null, null, null)) {

            if (cr.moveToFirst()) {
                String json = cr.getString(cr.getColumnIndex(DatabaseService.CACHE_COL));
                return gson.fromJson(json, Response.class);
            }
        }

        return null;
    }

    CityID getID(String city) {
        CityID cityID = map.get(city);

        if (cityID == null) {
            throw new RuntimeException("No CityID record for " + city);
        }

        return cityID;
    }

    private DatabaseService(Context context) {
        db = context.openOrCreateDatabase("data.db", Context.MODE_PRIVATE, null);

        if (db.getVersion() == 0) {
            db.execSQL("CREATE TABLE " + CACHE_TABLE + " (" +
                    CITY_COL + " TEXT UNIQUE," +
                    CACHE_COL + " TEXT)");

            db.execSQL("CREATE VIRTUAL TABLE " + CITY_TABLE +
                    " USING fts3(" +
                    CITY_COL + "," +
                    OWM_ID_COL + "," +
                    HW_ID_COL + ")");

            db.beginTransaction();
            try (InputStream is = context.getResources().openRawResource(R.raw.list);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

                String line;

                while ((line = reader.readLine()) != null) {
                    String[] tokens = line.split("\\s");

                    ContentValues cv = new ContentValues(3);
                    cv.put(CITY_COL, tokens[CITY_INDEX]);
                    cv.put(OWM_ID_COL, tokens[OWM_INDEX]);
                    cv.put(HW_ID_COL, tokens[HW_INDEX]);

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

    private void putID(String city, CityID id) {
        map.put(city, id);
    }

    public static DatabaseService getInstance(Context context) {
        if (instance == null)
            instance = new DatabaseService(context.getApplicationContext());

        return instance;
    }

    public static class PersistenceService {
        private static final String FAVORITES_LIST = "FAVORITES_LIST.txt";

        private DatabaseService dbService;
        private Context context;

        public PersistenceService(Context ctx) {
            dbService = DatabaseService.getInstance(ctx);
            context = ctx;
        }

        public List<String> restore() {
            if (!Arrays.asList(context.fileList()).contains(FAVORITES_LIST)) {
                return new ArrayList<>();
            }

            try (FileInputStream fin = context.openFileInput(FAVORITES_LIST);
                 BufferedReader br = new BufferedReader(new InputStreamReader(fin))) {

                List<String> cities = new ArrayList<>();

                String line;
                while ((line = br.readLine()) != null) {
                    String tokens[] = line.split("\\s");

                    cities.add(tokens[CITY_INDEX]);

                    CityID id = new CityID();
                    id.owmID = tokens[OWM_INDEX];
                    id.hwID = tokens[HW_INDEX];

                    dbService.putID(tokens[CITY_INDEX], id);
                }

                return cities;

            } catch (IOException e) {
                throw new RuntimeException("PersistenceService unable to restore");
            }
        }

        public void save(List<String> cities) {
            try (FileOutputStream fos =
                         context.openFileOutput(FAVORITES_LIST, Context.MODE_PRIVATE);
                 BufferedWriter out = new BufferedWriter(new OutputStreamWriter(fos))) {


                for (String city : cities) {
                    CityID id = dbService.getID(city);

                    String tokens[] = new String[3];
                    tokens[CITY_INDEX] = city;
                    tokens[OWM_INDEX] = id.owmID;
                    tokens[HW_INDEX] = id.hwID;

                    out.write(tokens[0] + " " + tokens[1] + " " + tokens[2]);

                    out.newLine();
                }

            } catch (IOException e) {
                throw new RuntimeException("PersistenceService unable to save");
            }
        }


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
            dbService.putID(city.get(idx), id.get(idx));
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
                        cityID.hwID = cr.getString(cr.getColumnIndex(DatabaseService.HW_ID_COL));
                        cityID.owmID = cr.getString(cr.getColumnIndex(DatabaseService.OWM_ID_COL));

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
