package com.example.huongvu.simpletodo.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.huongvu.simpletodo.models.ItemsTodo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HUONGVU on 2/28/2016.
 */
public class ItemTodoDBHelper extends SQLiteOpenHelper {

    private static ItemTodoDBHelper sInstance;

    // Database Info
    private static final String DATABASE_NAME = "itemDatabase";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    private static final String TABLE_ITEMS = "items";

    // Post Table Columns
    private static final String KEY_ITEM_ID = "id";
    private static final String KEY_ITEM_NAME = "name";
    private static final String KEY_ITEM_PRI = "priority";

    public static synchronized ItemTodoDBHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new ItemTodoDBHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    public ItemTodoDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Called when the database connection is being configured.
    // Configure database settings for things like foreign key support, write-ahead logging, etc.
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // Called when the database is created for the FIRST time.
    // If a database already exists on disk with the same DATABASE_NAME, this method will NOT be called.
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_ITEMS_TABLE = "CREATE TABLE " + TABLE_ITEMS +
                "(" +
                KEY_ITEM_ID + " INTEGER PRIMARY KEY," +
                KEY_ITEM_NAME + " TEXT," +
                KEY_ITEM_PRI + " TEXT" +
                ")";


        db.execSQL(CREATE_ITEMS_TABLE);

    }

    // Called when the database needs to be upgraded.
    // This method will only be called if a database already exists on disk with the same DATABASE_NAME,
    // but the DATABASE_VERSION is different than the version of the database that exists on disk.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            // Simplest implementation is to drop all old tables and recreate them
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
            onCreate(db);
        }
    }

    // Insert a item into the database
    public void addItem(ItemsTodo item) {
        // Create and/or open the database for writing
        SQLiteDatabase db = getWritableDatabase();

        // It's a good idea to wrap our insert in a transaction. This helps with performance and ensures
        // consistency of the database.
        db.beginTransaction();
        try {
            // The item might already exist in the database.
            long itemId = addOrUpdateItem(item);

            ContentValues values = new ContentValues();

            values.put(KEY_ITEM_NAME, item.itemName);

            // Notice how we haven't specified the primary key. SQLite auto increments the primary key column.
            db.insertOrThrow(TABLE_ITEMS, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            //Log.d(TAG, "Error while trying to add post to database");
        } finally {
            db.endTransaction();
        }
    }

    // Insert or update an item in the database
    // Since SQLite doesn't support "upsert" we need to fall back on an attempt to UPDATE (in case the
    // item already exists) optionally followed by an INSERT (in case the item does not already exist).
    // Unfortunately, there is a bug with the insertOnConflict method
    // (https://code.google.com/p/android/issues/detail?id=13045) so we need to fall back to the more
    // verbose option of querying for the item's primary key if we did an update.
    public long addOrUpdateItem(ItemsTodo item) {
        // The database connection is cached so it's not expensive to call getWriteableDatabase() multiple times.
        SQLiteDatabase db = getWritableDatabase();
        long itemId = -1;

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_ITEM_NAME, item.itemName);
            values.put(KEY_ITEM_ID, item.itemId);
            values.put(KEY_ITEM_PRI, item.itemPriority);

            // First try to update the item in case the item already exists in the database
            // This assumes itemIds are unique
            int rows = db.update(TABLE_ITEMS, values, KEY_ITEM_ID + "= ?",new String[]{String.valueOf(item.itemId)});

            // Check if update succeeded
            if (rows == 1) {
                // Get the primary key of the item we just updated
                String ITEMS_SELECT_QUERY = String.format("SELECT %s FROM %s WHERE %s = ?",
                        KEY_ITEM_ID, TABLE_ITEMS, KEY_ITEM_NAME);
                Cursor cursor = db.rawQuery(ITEMS_SELECT_QUERY, new String[]{String.valueOf(item.itemName)});
                try {
                    if (cursor.moveToFirst()) {
                        itemId = cursor.getInt(0);
                        db.setTransactionSuccessful();
                    }
                } finally {
                    if (cursor != null && !cursor.isClosed()) {
                        cursor.close();
                    }
                }
            } else {
                // item with this itemName did not already exist, so insert new item
                ContentValues valuesItem = new ContentValues();
                valuesItem.put(KEY_ITEM_NAME, item.itemName);
                valuesItem.put(KEY_ITEM_PRI, item.itemPriority);
                itemId = db.insertOrThrow(TABLE_ITEMS, null, valuesItem);
                db.setTransactionSuccessful();

            }
        } catch (Exception e) {
            Log.d("database error", "Error while trying to add or update user");
            //itemId = 4;
        } finally {
            db.endTransaction();
        }
        return itemId;
    }

    // Get all items in the database
    public List<ItemsTodo> getAllItems() {
        List<ItemsTodo> items = new ArrayList<>();

        // SELECT * FROM ITEMS
        String ITEMS_SELECT_QUERY = String.format("SELECT *  FROM %s", TABLE_ITEMS);

        // "getReadableDatabase()" and "getWriteableDatabase()" return the same object (except under low
        // disk space scenarios)
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(ITEMS_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    ItemsTodo newItem = new ItemsTodo();
                    newItem.itemName = cursor.getString(cursor.getColumnIndex(KEY_ITEM_NAME));
                    newItem.itemId = cursor.getInt(cursor.getColumnIndex(KEY_ITEM_ID));
                    items.add(newItem);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d("database error", "Error while trying to get posts from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return items;
    }

    // Delete all items in the database
    public void deleteAllItems() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            // Order of deletions is important when foreign key relationships exist.
            db.delete(TABLE_ITEMS, null, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d("Database error", "Error while trying to delete all items");
        } finally {
            db.endTransaction();
        }
    }

    // Delete item in the database
    public void deleteItems(String itemName) {
        SQLiteDatabase db = getWritableDatabase();
        String ITEMS_SELECT_QUERY = KEY_ITEM_NAME + "= '"+itemName +"'";
        db.beginTransaction();
        try {
            db.delete(TABLE_ITEMS, ITEMS_SELECT_QUERY, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d("Database error", "Error while trying to delete all items");
        } finally {
            db.endTransaction();
        }
    }

    public Cursor getCustomCursor() {

        // SELECT * FROM ITEMS
        String ITEMS_SELECT_QUERY = "SELECT items.*,items.id as _id FROM items "; ;

        // "getReadableDatabase()" and "getWriteableDatabase()" return the same object (except under low
        // disk space scenarios)
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(ITEMS_SELECT_QUERY, null);

        return cursor;
    }
}
