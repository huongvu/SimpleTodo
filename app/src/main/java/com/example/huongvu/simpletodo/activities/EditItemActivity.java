package com.example.huongvu.simpletodo.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import com.example.huongvu.simpletodo.R;

public class EditItemActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_item);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String itemText = getIntent().getStringExtra("item");
        String itemPrio = getIntent().getStringExtra("prio");

        EditText curItem = (EditText)findViewById(R.id.editText);
        EditText curPrio = (EditText)findViewById(R.id.editPriority);

        curItem.setText(itemText);
        curPrio.setText(itemPrio);


    }

    public void onSubmit(View v) {
        // closes the activity and returns to first screen
        this.finish();

    }

    public void onUpdateItem(View view) {
        Intent data = new Intent();
        EditText curItem = (EditText)findViewById(R.id.editText);
        EditText curPrio = (EditText)findViewById(R.id.editPriority);

        // Pass relevant data back as a result
        data.putExtra("itemUpdate", curItem.getText().toString());
        data.putExtra("PriorityUpdate", curPrio.getText().toString());

        String curItemUpdate = curItem.getText().toString().trim();
        if(curItemUpdate.equals(""))
        {
            //NOt OKIE
        }
        else{
            setResult(RESULT_OK, data);
            this.finish();
        }

    }
}
