package com.example.barapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "BarApp.db";
    private static final int DATABASE_VERSION = 4;

    // Table Names
    public static final String TABLE_BAR_ITEMS = "BAR_ITEMS";
    public static final String TABLE_SESSIONS = "SESSIONS";
    public static final String TABLE_INVENTORY_LOG = "INVENTORY_LOG";
    public static final String TABLE_SETTINGS = "SETTINGS";
    public static final String TABLE_USERS = "USERS";

    // Common Columns
    public static final String KEY_ID = "id";

    // BAR_ITEMS Columns
    public static final String KEY_BAR_NAME = "name";
    public static final String KEY_BAR_PRICE = "price";
    public static final String KEY_BAR_CATEGORY = "category";
    public static final String KEY_BAR_STOCK = "current_stock";

    // SESSIONS Columns
    public static final String KEY_SESSION_HALL_ID = "hall_id";
    public static final String KEY_SESSION_PEOPLE_COUNT = "people_count";
    public static final String KEY_SESSION_START_TIME = "start_time";
    public static final String KEY_SESSION_END_TIME = "end_time";
    public static final String KEY_SESSION_BAR_ITEMS_JSON = "bar_items_json";
    public static final String KEY_SESSION_TOTAL_AMOUNT = "total_amount";
    public static final String KEY_SESSION_STATUS = "status";
    public static final String KEY_SESSION_IS_SYNCED = "is_synced";

    // INVENTORY_LOG Columns
    public static final String KEY_LOG_ITEM_ID = "item_id";
    public static final String KEY_LOG_QUANTITY_CHANGE = "quantity_change";
    public static final String KEY_LOG_TIMESTAMP = "timestamp";
    public static final String KEY_LOG_TYPE = "type";

    // SETTINGS Columns
    public static final String KEY_SETTING_KEY = "key";
    public static final String KEY_SETTING_VALUE = "value";

    // USERS Columns
    public static final String KEY_USER_USERNAME = "username";
    public static final String KEY_USER_PASSWORD = "password";
    public static final String KEY_USER_ROLE = "role"; // "ADMIN" or "MANAGER"
    public static final String KEY_USER_FULL_NAME = "full_name";

    // Default Settings
    public static final String SETTING_RATE_PER_PERSON_HOUR = "RATE_PER_PERSON_HOUR";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create BAR_ITEMS table
        String CREATE_BAR_ITEMS_TABLE = "CREATE TABLE " + TABLE_BAR_ITEMS + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_BAR_NAME + " TEXT,"
                + KEY_BAR_PRICE + " REAL,"
                + KEY_BAR_CATEGORY + " TEXT,"
                + KEY_BAR_STOCK + " INTEGER" + ")";
        db.execSQL(CREATE_BAR_ITEMS_TABLE);

        // Create SESSIONS table
        String CREATE_SESSIONS_TABLE = "CREATE TABLE " + TABLE_SESSIONS + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_SESSION_HALL_ID + " INTEGER,"
                + KEY_SESSION_PEOPLE_COUNT + " INTEGER,"
                + KEY_SESSION_START_TIME + " INTEGER,"
                + KEY_SESSION_END_TIME + " INTEGER,"
                + KEY_SESSION_BAR_ITEMS_JSON + " TEXT,"
                + KEY_SESSION_TOTAL_AMOUNT + " REAL,"
                + KEY_SESSION_STATUS + " TEXT,"
                + KEY_SESSION_IS_SYNCED + " INTEGER DEFAULT 0" + ")";
        db.execSQL(CREATE_SESSIONS_TABLE);

        // Create INVENTORY_LOG table
        String CREATE_INVENTORY_LOG_TABLE = "CREATE TABLE " + TABLE_INVENTORY_LOG + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_LOG_ITEM_ID + " INTEGER,"
                + KEY_LOG_QUANTITY_CHANGE + " INTEGER,"
                + KEY_LOG_TIMESTAMP + " INTEGER,"
                + KEY_LOG_TYPE + " TEXT" + ")";
        db.execSQL(CREATE_INVENTORY_LOG_TABLE);

        // Create SETTINGS table
        String CREATE_SETTINGS_TABLE = "CREATE TABLE " + TABLE_SETTINGS + "("
                + KEY_SETTING_KEY + " TEXT PRIMARY KEY,"
                + KEY_SETTING_VALUE + " TEXT" + ")";
        db.execSQL(CREATE_SETTINGS_TABLE);

        // Create USERS table
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_USER_USERNAME + " TEXT UNIQUE,"
                + KEY_USER_PASSWORD + " TEXT,"
                + KEY_USER_ROLE + " TEXT,"
                + KEY_USER_FULL_NAME + " TEXT" + ")";
        db.execSQL(CREATE_USERS_TABLE);

        // Seed Data
        seedData(db);
        seedFakeSessions(db);
    }

    private void seedData(SQLiteDatabase db) {
        // Seed Settings
        ContentValues settingValues = new ContentValues();
        settingValues.put(KEY_SETTING_KEY, SETTING_RATE_PER_PERSON_HOUR);
        settingValues.put(KEY_SETTING_VALUE, "400.0");
        db.insert(TABLE_SETTINGS, null, settingValues);

        // Seed Default Users
        // Admin user: can access halls and bar sales
        ContentValues adminValues = new ContentValues();
        adminValues.put(KEY_USER_USERNAME, "admin");
        adminValues.put(KEY_USER_PASSWORD, "admin123"); // In production, use hashed passwords!
        adminValues.put(KEY_USER_ROLE, "ADMIN");
        adminValues.put(KEY_USER_FULL_NAME, "Администратор");
        db.insert(TABLE_USERS, null, adminValues);

        // Manager user: full access to everything
        ContentValues managerValues = new ContentValues();
        managerValues.put(KEY_USER_USERNAME, "manager");
        managerValues.put(KEY_USER_PASSWORD, "manager123"); // In production, use hashed passwords!
        managerValues.put(KEY_USER_ROLE, "MANAGER");
        managerValues.put(KEY_USER_FULL_NAME, "Руководитель");
        db.insert(TABLE_USERS, null, managerValues);

        // Seed Bar Items with Emoji Icons
        
        // 🍺 Пиво (Beer)
        insertBarItem(db, "🍺 Балтика 7", 250.0, "Beer", 30);
        insertBarItem(db, "🍺 Heineken 0.5", 350.0, "Beer", 25);
        insertBarItem(db, "🍺 Corona Extra", 400.0, "Beer", 20);
        insertBarItem(db, "🍺 Stella Artois", 380.0, "Beer", 20);
        insertBarItem(db, "🍺 Tuborg Green", 280.0, "Beer", 30);
        
        // 💨 Кальян (Hookah)
        insertBarItem(db, "💨 Кальян Классик", 800.0, "Hookah", 10);
        insertBarItem(db, "💨 Кальян Премиум", 1200.0, "Hookah", 8);
        insertBarItem(db, "💨 Кальян Фруктовый", 1000.0, "Hookah", 10);
        insertBarItem(db, "💨 Кальян Мятный", 900.0, "Hookah", 12);
        
        // 🥤 Напитки (Soft Drinks)
        insertBarItem(db, "🥤 Coca-Cola 0.5", 150.0, "Drinks", 50);
        insertBarItem(db, "🥤 Pepsi 0.5", 150.0, "Drinks", 45);
        insertBarItem(db, "🥤 Sprite 0.5", 150.0, "Drinks", 40);
        insertBarItem(db, "🥤 Fanta 0.5", 150.0, "Drinks", 40);
        insertBarItem(db, "💧 Вода 0.5", 100.0, "Drinks", 100);
        insertBarItem(db, "🧃 Сок апельсин", 200.0, "Drinks", 30);
        insertBarItem(db, "🧃 Сок яблоко", 200.0, "Drinks", 30);
        
        // ⚡ Энергетики (Energy Drinks)
        insertBarItem(db, "⚡ Red Bull", 300.0, "Energy", 25);
        insertBarItem(db, "⚡ Monster Energy", 320.0, "Energy", 20);
        insertBarItem(db, "⚡ Burn", 280.0, "Energy", 25);
        
        // 🍿 Снеки (Snacks)
        insertBarItem(db, "🍿 Попкорн большой", 250.0, "Snacks", 30);
        insertBarItem(db, "🍿 Попкорн средний", 180.0, "Snacks", 35);
        insertBarItem(db, "🥔 Чипсы Lays", 200.0, "Snacks", 40);
        insertBarItem(db, "🥔 Чипсы Pringles", 280.0, "Snacks", 25);
        insertBarItem(db, "🌮 Начос", 350.0, "Snacks", 15);
        insertBarItem(db, "🥜 Орешки", 220.0, "Snacks", 30);
        insertBarItem(db, "🍫 Шоколад", 180.0, "Snacks", 40);
        insertBarItem(db, "🍬 Жвачка", 50.0, "Snacks", 60);
        
        // ☕ Горячие напитки (Hot Drinks)
        insertBarItem(db, "☕ Кофе эспрессо", 200.0, "Coffee", 50);
        insertBarItem(db, "☕ Кофе латте", 280.0, "Coffee", 40);
        insertBarItem(db, "☕ Капучино", 260.0, "Coffee", 40);
        insertBarItem(db, "🍵 Чай черный", 150.0, "Tea", 50);
        insertBarItem(db, "🍵 Чай зеленый", 150.0, "Tea", 50);
    }

    private void seedFakeSessions(SQLiteDatabase db) {
        // Generate 30 fake closed sessions for the last 7 days
        long now = System.currentTimeMillis();
        long oneDayMillis = 24 * 60 * 60 * 1000L;
        java.util.Random random = new java.util.Random();

        for (int i = 0; i < 30; i++) {
            ContentValues values = new ContentValues();
            int hallId = random.nextInt(3) + 1; // 1, 2, or 3
            int people = random.nextInt(5) + 2; // 2 to 6 people

            // Random time in last 7 days
            long startTime = now - (long) (random.nextDouble() * 7 * oneDayMillis);
            long duration = (long) ((1 + random.nextDouble() * 2) * 60 * 60 * 1000); // 1 to 3 hours
            long endTime = startTime + duration;

            double durationHours = duration / (1000.0 * 60 * 60);
            double rate = 400.0;
            double timeCost = people * rate * durationHours;
            double barCost = random.nextInt(2000); // Random bar spend
            double total = timeCost + barCost;

            values.put(KEY_SESSION_HALL_ID, hallId);
            values.put(KEY_SESSION_PEOPLE_COUNT, people);
            values.put(KEY_SESSION_START_TIME, startTime);
            values.put(KEY_SESSION_END_TIME, endTime);
            values.put(KEY_SESSION_BAR_ITEMS_JSON, "[]"); // Dummy JSON
            values.put(KEY_SESSION_TOTAL_AMOUNT, total);
            values.put(KEY_SESSION_STATUS, "CLOSED");
            values.put(KEY_SESSION_IS_SYNCED, 0);

            db.insert(TABLE_SESSIONS, null, values);
        }
    }

    private void insertBarItem(SQLiteDatabase db, String name, double price, String category, int stock) {
        ContentValues values = new ContentValues();
        values.put(KEY_BAR_NAME, name);
        values.put(KEY_BAR_PRICE, price);
        values.put(KEY_BAR_CATEGORY, category);
        values.put(KEY_BAR_STOCK, stock);
        db.insert(TABLE_BAR_ITEMS, null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BAR_ITEMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SESSIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INVENTORY_LOG);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SETTINGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // --- Key Methods ---

    // 1. Settings
    public String getSetting(String key) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SETTINGS, new String[] { KEY_SETTING_VALUE },
                KEY_SETTING_KEY + "=?", new String[] { key }, null, null, null, null);
        String value = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                value = cursor.getString(0);
            }
            cursor.close();
        }
        return value;
    }

    public void setSetting(String key, String value) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_SETTING_KEY, key);
        values.put(KEY_SETTING_VALUE, value);
        // Insert or replace to handle updates
        db.insertWithOnConflict(TABLE_SETTINGS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    // 2. Bar Items
    public void updateBarItemPrice(int id, double newPrice) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_BAR_PRICE, newPrice);
        db.update(TABLE_BAR_ITEMS, values, KEY_ID + " = ?", new String[] { String.valueOf(id) });
    }

    public BarItemModel getBarItem(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_BAR_ITEMS, null, KEY_ID + "=?", new String[] { String.valueOf(id) }, null, null,
                null);
        if (cursor != null && cursor.moveToFirst()) {
            BarItemModel item = new BarItemModel(
                    cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_BAR_NAME)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_BAR_PRICE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_BAR_CATEGORY)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(KEY_BAR_STOCK)));
            cursor.close();
            return item;
        }
        return null;
    }

    public List<BarItemModel> getAllBarItems() {
        List<BarItemModel> itemList = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_BAR_ITEMS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                BarItemModel item = new BarItemModel(
                        cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_BAR_NAME)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_BAR_PRICE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_BAR_CATEGORY)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(KEY_BAR_STOCK)));
                itemList.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return itemList;
    }

    // 3. Inventory
    public void updateStock(int itemId, int change, String type) {
        SQLiteDatabase db = this.getWritableDatabase();

        int quantityDelta = change;
        if ("EXPENSE".equals(type)) {
            quantityDelta = -change;
        }

        db.execSQL("UPDATE " + TABLE_BAR_ITEMS + " SET " + KEY_BAR_STOCK + " = " + KEY_BAR_STOCK + " + " + quantityDelta
                + " WHERE " + KEY_ID + " = " + itemId);

        ContentValues logValues = new ContentValues();
        logValues.put(KEY_LOG_ITEM_ID, itemId);
        logValues.put(KEY_LOG_QUANTITY_CHANGE, change);
        logValues.put(KEY_LOG_TIMESTAMP, System.currentTimeMillis());
        logValues.put(KEY_LOG_TYPE, type);
        db.insert(TABLE_INVENTORY_LOG, null, logValues);
    }

    // 4. Sessions
    public long openNewSession(int hallId, int peopleCount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_SESSION_HALL_ID, hallId);
        values.put(KEY_SESSION_PEOPLE_COUNT, peopleCount);
        values.put(KEY_SESSION_START_TIME, System.currentTimeMillis());
        values.put(KEY_SESSION_STATUS, "OPEN");
        values.put(KEY_SESSION_BAR_ITEMS_JSON, "[]");
        values.put(KEY_SESSION_TOTAL_AMOUNT, 0.0);
        values.put(KEY_SESSION_IS_SYNCED, 0);
        return db.insert(TABLE_SESSIONS, null, values);
    }

    public void addBarItemToSession(int sessionId, int itemId, int quantity) {
        SessionModel session = getSession(sessionId);
        if (session == null)
            return;

        BarItemModel item = getBarItem(itemId);
        if (item == null)
            return;

        updateStock(itemId, quantity, "EXPENSE");

        try {
            JSONArray itemsArray = new JSONArray(session.getBarItemsJson());
            boolean itemFound = false;

            for (int i = 0; i < itemsArray.length(); i++) {
                JSONObject obj = itemsArray.getJSONObject(i);
                if (obj.getInt("id") == itemId) {
                    int currentQty = obj.getInt("quantity");
                    obj.put("quantity", currentQty + quantity);
                    itemFound = true;
                    break;
                }
            }

            if (!itemFound) {
                JSONObject newItem = new JSONObject();
                newItem.put("id", item.getId());
                newItem.put("name", item.getName());
                newItem.put("price", item.getPrice());
                newItem.put("quantity", quantity);
                itemsArray.put(newItem);
            }

            double addedCost = item.getPrice() * quantity;
            double newTotal = session.getTotalAmount() + addedCost;

            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(KEY_SESSION_BAR_ITEMS_JSON, itemsArray.toString());
            values.put(KEY_SESSION_TOTAL_AMOUNT, newTotal);

            db.update(TABLE_SESSIONS, values, KEY_ID + "=?", new String[] { String.valueOf(sessionId) });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void closeSession(int sessionId) {
        SessionModel session = getSession(sessionId);
        if (session == null || "CLOSED".equals(session.getStatus()))
            return;

        long endTime = System.currentTimeMillis();
        long durationMillis = endTime - session.getStartTime();
        double durationHours = durationMillis / (1000.0 * 60 * 60);

        String rateStr = getSetting(SETTING_RATE_PER_PERSON_HOUR);
        double ratePerPerson = (rateStr != null) ? Double.parseDouble(rateStr) : 0.0;

        double timeCost = durationHours * ratePerPerson * session.getPeopleCount();
        double finalTotal = session.getTotalAmount() + timeCost;

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_SESSION_END_TIME, endTime);
        values.put(KEY_SESSION_TOTAL_AMOUNT, finalTotal);
        values.put(KEY_SESSION_STATUS, "CLOSED");

        db.update(TABLE_SESSIONS, values, KEY_ID + "=?", new String[] { String.valueOf(sessionId) });
    }

    public SessionModel getSession(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SESSIONS, null, KEY_ID + "=?", new String[] { String.valueOf(id) }, null, null,
                null);
        if (cursor != null && cursor.moveToFirst()) {
            SessionModel session = new SessionModel(
                    cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SESSION_HALL_ID)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SESSION_PEOPLE_COUNT)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(KEY_SESSION_START_TIME)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(KEY_SESSION_END_TIME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_SESSION_BAR_ITEMS_JSON)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_SESSION_TOTAL_AMOUNT)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_SESSION_STATUS)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SESSION_IS_SYNCED)) == 1);
            cursor.close();
            return session;
        }
        return null;
    }

    // --- Analytics Methods ---

    public double getTotalRevenue() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + KEY_SESSION_TOTAL_AMOUNT + ") FROM " + TABLE_SESSIONS + " WHERE "
                + KEY_SESSION_STATUS + " = 'CLOSED'", null);
        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }

    public Map<String, Double> getDailyRevenue(int days) {
        Map<String, Double> data = new TreeMap<>();
        SQLiteDatabase db = this.getReadableDatabase();

        long now = System.currentTimeMillis();
        long startTime = now - (days * 24L * 60 * 60 * 1000);

        Cursor cursor = db.query(TABLE_SESSIONS,
                new String[] { KEY_SESSION_END_TIME, KEY_SESSION_TOTAL_AMOUNT },
                KEY_SESSION_STATUS + "='CLOSED' AND " + KEY_SESSION_END_TIME + " >= ?",
                new String[] { String.valueOf(startTime) }, null, null, KEY_SESSION_END_TIME + " ASC");

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MM-dd", java.util.Locale.US);

        if (cursor.moveToFirst()) {
            do {
                long time = cursor.getLong(0);
                double amount = cursor.getDouble(1);
                String day = sdf.format(new java.util.Date(time));

                data.put(day, data.getOrDefault(day, 0.0) + amount);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return data;
    }

    public List<SessionModel> getLastClosedSessions(int limit) {
        List<SessionModel> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SESSIONS, null, KEY_SESSION_STATUS + "='CLOSED'", null, null, null,
                KEY_SESSION_END_TIME + " DESC", String.valueOf(limit));

        if (cursor.moveToFirst()) {
            do {
                SessionModel session = new SessionModel(
                        cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SESSION_HALL_ID)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SESSION_PEOPLE_COUNT)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(KEY_SESSION_START_TIME)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(KEY_SESSION_END_TIME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_SESSION_BAR_ITEMS_JSON)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_SESSION_TOTAL_AMOUNT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_SESSION_STATUS)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SESSION_IS_SYNCED)) == 1);
                list.add(session);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    // Revenue Breakdown Methods for Analytics
    
    /**
     * Calculate total revenue from time-based charges (hourly rates)
     */
    public double getTimeBasedRevenue() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SESSIONS, 
                new String[] { KEY_SESSION_START_TIME, KEY_SESSION_END_TIME, 
                              KEY_SESSION_PEOPLE_COUNT, KEY_SESSION_TOTAL_AMOUNT, 
                              KEY_SESSION_BAR_ITEMS_JSON },
                KEY_SESSION_STATUS + "='CLOSED'", null, null, null, null);
        
        double timeRevenue = 0;
        String rateStr = getSetting(SETTING_RATE_PER_PERSON_HOUR);
        double rate = (rateStr != null) ? Double.parseDouble(rateStr) : 400.0;
        
        if (cursor.moveToFirst()) {
            do {
                long startTime = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_SESSION_START_TIME));
                long endTime = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_SESSION_END_TIME));
                int peopleCount = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SESSION_PEOPLE_COUNT));
                
                long durationMillis = endTime - startTime;
                double durationHours = durationMillis / (1000.0 * 60 * 60);
                double sessionTimeRevenue = durationHours * rate * peopleCount;
                
                timeRevenue += sessionTimeRevenue;
            } while (cursor.moveToNext());
        }
        cursor.close();
        return timeRevenue;
    }
    
    /**
     * Calculate total revenue from bar sales
     */
    public double getBarRevenue() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SESSIONS,
                new String[] { KEY_SESSION_BAR_ITEMS_JSON },
                KEY_SESSION_STATUS + "='CLOSED'", null, null, null, null);
        
        double barRevenue = 0;
        
        if (cursor.moveToFirst()) {
            do {
                String barItemsJson = cursor.getString(0);
                try {
                    JSONArray items = new JSONArray(barItemsJson);
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject item = items.getJSONObject(i);
                        double price = item.getDouble("price");
                        int quantity = item.getInt("quantity");
                        barRevenue += (price * quantity);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return barRevenue;
    }

    // User Authentication Methods
    
    /**
     * Authenticate user with username and password
     * @return UserModel if credentials are valid, null otherwise
     */
    public UserModel authenticateUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null,
                KEY_USER_USERNAME + "=? AND " + KEY_USER_PASSWORD + "=?",
                new String[] { username, password }, null, null, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            UserModel user = new UserModel(
                    cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_USERNAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_PASSWORD)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_ROLE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_FULL_NAME)));
            cursor.close();
            return user;
        }
        if (cursor != null) {
            cursor.close();
        }
        return null;
    }
}
