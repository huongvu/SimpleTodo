package com.example.huongvu.simpletodo.activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.support.v7.widget.Toolbar;

import com.example.huongvu.simpletodo.R;
import com.example.huongvu.simpletodo.adapters.ItemCursorAdapter;
import com.example.huongvu.simpletodo.models.ItemsTodo;
import com.example.huongvu.simpletodo.utils.ItemTodoDBHelper;


import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    ArrayList<String> items;
    ArrayList<Integer> itemsid;
    //ArrayAdapter<String> itemsAdapter;

    ListView lvItems;
    String editText = "";
    String editPrio = "";

    int currItem_pos;
    Cursor todoCursor = null;

    private final int REQUEST_CODE = 20;
    private final int REQUEST_NEW_ID = 300;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //Get Adapter Custom Cursor


        lvItems = (ListView)findViewById(R.id.lvItems);

        //readDbItem();

        //itemsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        //lvItems.setAdapter(itemsAdapter);
        getAdapter();

        setupListViewListener();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

    }

    private void setupListViewListener(){

        lvItems.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> adapter, View item, int pos, long id) {
                        // first parameter is the context, second is the class of the activity to launch
                        Intent i = new Intent(MainActivity.this, EditItemActivity.class);
                        i.putExtra("item", todoCursor.getString(todoCursor.getColumnIndexOrThrow("name")));
                        i.putExtra("prio",todoCursor.getString(todoCursor.getColumnIndexOrThrow("priority")));
                        //currItem_pos = pos;
                        currItem_pos = todoCursor.getInt(todoCursor.getColumnIndexOrThrow("id"));
                        startActivityForResult(i, REQUEST_CODE);

                    }

                }
        );

        lvItems.setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> adapter, View item, int pos, long id) {

                        deleteDbItem(todoCursor.getString(todoCursor.getColumnIndexOrThrow("name")));
                        //itemsAdapter.notifyDataSetChanged();
                        getAdapter();
                        return true;
                    }
                }
        );
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // REQUEST_CODE is defined above
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            // Extract name value from result extras
            editText = data.getExtras().getString("itemUpdate");
            editPrio = data.getExtras().getString("PriorityUpdate");
            //items.set(currItem_pos, editText);
            writeDbItem(currItem_pos, editText, editPrio);
            getAdapter();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onAddItem(View view) {
        EditText etNewItem = (EditText)findViewById(R.id.etNewItem);
        String itemText = etNewItem.getText().toString().trim();
        //itemsAdapter.add(itemText);
        if(itemText.equals("")) {
            //donothing
        }
        else{
            writeDbItem(REQUEST_NEW_ID, itemText, "LOW");
            getAdapter();
            etNewItem.setText("");
        }


    }

    private void readDbItem() {
        // Get singleton instance of database
        ItemTodoDBHelper databaseHelper = ItemTodoDBHelper.getInstance(this);

        // Get all items from database
        List<ItemsTodo> dbItems = databaseHelper.getAllItems();

        items = new ArrayList<String>();
        itemsid = new ArrayList<Integer>();
        try {
            for (ItemsTodo dbItemInfor : dbItems) {
                // Add Item Name to itmes
                items.add(dbItemInfor.itemName);
                itemsid.add(dbItemInfor.itemId);
            }
        }catch (Exception e) {
            //items.add("Error");
        }
    }

    private long writeDbItem(int itemId, String itemName, String Priority) {
        long returnItemId;
        // Get singleton instance of database
        ItemTodoDBHelper databaseHelper = ItemTodoDBHelper.getInstance(this);

        ItemsTodo updateItem = new ItemsTodo();

        updateItem.itemId = itemId;
        updateItem.itemName = itemName;
        updateItem.itemPriority = Priority;
        // Add or update item to the database

        returnItemId = databaseHelper.addOrUpdateItem(updateItem);

        return returnItemId;
        //databaseHelper.deleteAllItems();
    }

    private void deleteDbItem(String itemName) {
        // Get singleton instance of database
        ItemTodoDBHelper databaseHelper = ItemTodoDBHelper.getInstance(this);

        // Delete item from database
        databaseHelper.deleteItems(itemName);
    }

    private void readItem() {
        File filesDir = getFilesDir();
        File todoFile = new File(filesDir, "todo.txt");
        try{
            items = new ArrayList<String>(FileUtils.readLines(todoFile));
        }catch (IOException e){
            items = new ArrayList<String>();
        }
    }

    private void writeItem() {
        File filesDir = getFilesDir();
        File todoFile = new File(filesDir, "todo.txt");
        try{
            FileUtils.writeLines(todoFile, items);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void getAdapter() {
        // Get singleton instance of database
        ItemTodoDBHelper databaseHelper = ItemTodoDBHelper.getInstance(this);
        // Delete item from database
        todoCursor = databaseHelper.getCustomCursor();

        // Setup cursor adapter using cursor from last step
        ItemCursorAdapter todoAdapter = new ItemCursorAdapter(this, todoCursor, 0);

        // Attach cursor adapter to the ListView
        lvItems.setAdapter(todoAdapter);

    }

    //Get update cursor



}
