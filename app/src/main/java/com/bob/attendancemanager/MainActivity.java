package com.bob.attendancemanager;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Database database;
    private ArrayList<Class> classes;
    private ListAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        ListView listview = findViewById(R.id.listview);
        Button new_class = findViewById(R.id.new_class);

        new_class.setOnClickListener(v-> {
            Intent intent = new Intent(MainActivity.this, ClassEditor.class);
            startActivity(intent);
        });

        // Initialize the database with context
        database = new Database(this);

        // Fetch the list of classes from the database
        classes = database.getAllClasses();

        // Initialize the list adapter and set it to the ListView
        listAdapter = new ListAdapter();
        listview.setAdapter(listAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the list of classes when the activity resumes
        classes = database.getAllClasses();
        listAdapter.notifyDataSetChanged();
    }

    public class ListAdapter extends BaseAdapter {

        public ListAdapter() {}

        @Override
        public int getCount() {
            return classes.size();
        }

        @Override
        public Class getItem(int i) {
            return classes.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            LayoutInflater inflater = getLayoutInflater();
            @SuppressLint({"InflateParams", "ViewHolder"})
            View v = inflater.inflate(R.layout.list_item, null);
            TextView text = v.findViewById(R.id.text);
            text.setText(classes.get(i).getClassName());

            text.setOnClickListener(v1-> {
                Intent intent = new Intent(MainActivity.this, ShowSessions.class);
                intent.putExtra("Class ID", classes.get(i).getID());
                intent.putExtra("Class Name", classes.get(i).getClassName());
                startActivity(intent);
            });

            text.setOnLongClickListener(v1-> {
                Intent intent = new Intent(MainActivity.this, ClassEditor.class);
                intent.putExtra("Class ID", classes.get(i).getID());
                intent.putExtra("Class Name", classes.get(i).getClassName());
                startActivity(intent);
                return true;
            });
            return v;
        }
    }

}
