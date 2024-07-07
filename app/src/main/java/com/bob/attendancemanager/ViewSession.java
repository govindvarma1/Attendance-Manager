package com.bob.attendancemanager;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class ViewSession extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_view_session);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        int classID = getIntent().getIntExtra("Class ID", 0);
        int sessionID = getIntent().getIntExtra("Session ID", 0);

        Database database = new Database(this); // Initialize with context

        Session session = database.getSession(classID, sessionID);
        ArrayList<Student> allStudents = database.getStudents(classID);
        ArrayList<Student> presentStudents = session.getStudents();
        ArrayList<Student> absentStudents = new ArrayList<>();

        // Identify absent students
        for (Student student : allStudents) {
            Log.d("a", String.valueOf(student.getID()));
            boolean present = false;
            for (Student presentStudent : presentStudents) {
                if (presentStudent.getID() == student.getID()) {
                    present = true;
                    break;
                }
            }
            if (!present) {
                absentStudents.add(student);
            }
        }



        ListView presentListView = findViewById(R.id.present);
        presentListView.setAdapter(new StudentListAdapter(presentStudents));

        ListView absentListView = findViewById(R.id.absent);
        absentListView.setAdapter(new StudentListAdapter(absentStudents));
    }

    private class StudentListAdapter extends BaseAdapter {
        private ArrayList<Student> data;

        public StudentListAdapter(ArrayList<Student> data) {
            this.data = data;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Student getItem(int i) {
            return data.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            LayoutInflater inflater = getLayoutInflater();
            View listItemView = inflater.inflate(R.layout.list_item, null);
            TextView textView = listItemView.findViewById(R.id.text);
            textView.setText(data.get(i).getFirstName() + " " + data.get(i).getLastName());
            return listItemView;
        }
    }
}
