package com.bob.attendancemanager;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;

import java.util.ArrayList;

public class EditSessionStudents extends AppCompatActivity {

    private Database database;
    private int classID;
    private int sessionID;
    private ArrayList<Student> classStudents;
    private ArrayList<Integer> sessionStudents;
    private ArrayList<Integer> insertStudents;
    private ArrayList<Integer> deleteStudents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_edit_session_students);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Button submit = findViewById(R.id.submit);
        ListView listview = findViewById(R.id.listview);

        // Initialize the database
        database = new Database(this);

        classID = getIntent().getIntExtra("Class ID", 0);
        sessionID = getIntent().getIntExtra("Session ID", 0);

        // Get the list of students for the class
        classStudents = database.getStudents(classID);

        // Get the list of students for the session
        Session session = database.getSession(classID, sessionID);
        ArrayList<Student> sessionStudents1 = session.getStudents();

        // Extract student IDs for session students
        sessionStudents = new ArrayList<>();
        for (Student st : sessionStudents1) {
            sessionStudents.add(st.getID());
        }

        insertStudents = new ArrayList<>();
        deleteStudents = new ArrayList<>();
//        deleteStudents.add(1);

        listview.setAdapter(new ListAdapter());

        // Set the submit button action
        submit.setOnClickListener(v -> {
            try {
                // Add new students to the session
                database.addStudentsToSession(classID, sessionID, insertStudents);
                // Remove students from the session
                database.removeStudentsFromSession(classID, sessionID, deleteStudents);
                Log.d("removed", String.valueOf(deleteStudents.get(0)));
            } catch (Exception e) {
                e.printStackTrace();
            }
            finish();
        });
    }

    private class ListAdapter extends BaseAdapter {

        public ListAdapter() {}

        @Override
        public int getCount() {
            return classStudents.size();
        }

        @Override
        public Student getItem(int i) {
            return classStudents.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            LayoutInflater inflater = getLayoutInflater();
            @SuppressLint({"InflateParams", "ViewHolder"})
            View v = inflater.inflate(R.layout.list_item_checkbox, null);

            CheckBox text = v.findViewById(R.id.text);
            Student student = classStudents.get(i);
            int studentID = student.getID();
            text.setText(student.getFirstName() + " " + student.getLastName());
            text.setChecked(sessionStudents.contains(studentID));

            text.setOnCheckedChangeListener((compoundButton, checked) -> {
                if (checked && !sessionStudents.contains(studentID)) {
                    addStudent(studentID);
                } else if (!checked && sessionStudents.contains(studentID)) {
                    removeStudent(studentID);
                } else if (checked && sessionStudents.contains(studentID)) {
                    if (deleteStudents.contains(studentID) && deleteStudents.size() != 1) {
                        deleteStudents.remove((Integer) studentID);
                    } else {
                        deleteStudents = new ArrayList<>();
                    }
                }
            });

            return v;
        }
    }

    private void addStudent(int id) {
        if (!insertStudents.contains(id)) insertStudents.add(id);
        if (deleteStudents.contains(id)) deleteStudents.remove((Integer) id);
    }

    private void removeStudent(int id) {
        if (insertStudents.contains(id)) insertStudents.remove((Integer) id);
        if (!deleteStudents.contains(id)) deleteStudents.add(id);
    }

}
