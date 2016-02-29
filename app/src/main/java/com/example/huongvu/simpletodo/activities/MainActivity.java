package com.example.huongvu.simpletodo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.example.huongvu.simpletodo.R;
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
    ArrayAdapter<String> itemsAdapter;

    ListView lvItems;
    String editText = "";
    int currItem_pos;

    private final int REQUEST_CODE = 20;
    private final int REQUEST_NEW_ID = 300;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lvItems = (ListView)findViewById(R.id.lvItems);

        readDbItem();

        itemsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        lvItems.setAdapter(itemsAdapter);

        setupListViewListener();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private void setupListViewListener(){

        lvItems.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> adapter, View item, int pos, long id) {
                        // first parameter is the context, second is the class of the activity to launch
                        Intent i = new Intent(MainActivity.this, EditItemActivity.class);
                        i.putExtra("item", itemsAdapter.getItem(pos));
                        currItem_pos = pos;
                        startActivityForResult(i, REQUEST_CODE);

                    }

                }
        );

        lvItems.setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> adapter, View item, int pos, long id) {
                        deleteDbItem(items.get(pos));
                        items.remove(pos);
                        itemsid.remove(pos);
                        itemsAdapter.notifyDataSetChanged();
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
            items.set(currItem_pos, editText);
            itemsAdapter.notifyDataSetChanged();
            writeDbItem(itemsid.get(currItem_pos),editText);
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
        long updateItemId;
        EditText etNewItem = (EditText)findViewById(R.id.etNewItem);
        String itemText = etNewItem.getText().toString();
        itemsAdapter.add(itemText);
        updateItemId = writeDbItem(REQUEST_NEW_ID, itemText);
        itemsid.add((int)updateItemId);
        etNewItem.setText("");

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

    private long writeDbItem(int itemId, String itemName) {
        long returnItemId;
        // Get singleton instance of database
        ItemTodoDBHelper databaseHelper = ItemTodoDBHelper.getInstance(this);

        ItemsTodo updateItem = new ItemsTodo();

        updateItem.itemId = itemId;
        updateItem.itemName = itemName;
        // Add or update item to the database

        returnItemId = databaseHelper.addOrUpdateItem(updateItem);

        return returnItemId;
        //databaseHelper.deleteAllItems();
    }

    private void deleteDbItem(String itemName) {
        // Get singleton instance of database
        ItemTodoDBHelper databaseHelper = ItemTodoDBHelper.getInstance(this);

        // Get all items from database
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


}
