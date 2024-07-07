package com.bob.attendancemanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class Database extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "attendancemanager.db";
    private static final int DATABASE_VERSION = 1;

    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createClassesTable = "CREATE TABLE classes (ID INTEGER PRIMARY KEY, ClassName TEXT);";
        db.execSQL(createClassesTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS classes");
        onCreate(db);
    }

    public ArrayList<Class> getAllClasses() {
        ArrayList<Class> classes = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT * FROM classes", null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Class c = new Class(cursor.getInt(cursor.getColumnIndexOrThrow("ID")),
                            cursor.getString(cursor.getColumnIndexOrThrow("ClassName")));
                    classes.add(c);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return classes;
    }

    public int getNextClassID() {
        int id = 0;

        ArrayList<Class> classes = getAllClasses();
        int size = classes.size();
        if (size != 0) {
            int last = size - 1;
            Class lastClass = classes.get(last);
            id = lastClass.getID() + 1;
        }
        return id;
    }

    public void addClass(Class c) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("ID", c.getID());
            values.put("ClassName", c.getClassName());
            db.insert("classes", null, values);
            String createSessionsTable = "CREATE TABLE IF NOT EXISTS \"" + c.getID() + " - Sessions\" (ID INTEGER, Subject TEXT, Date TEXT);";
            String createStudentsTable = "CREATE TABLE IF NOT EXISTS \"" + c.getID() + " - Students\" (ID INTEGER, FirstName TEXT, LastName TEXT, Email TEXT, Tel TEXT);";
            db.execSQL(createSessionsTable);
            db.execSQL(createStudentsTable);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public void deleteClass(int classID) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            // Delete the class entry from the "Classes" table
            db.delete("Classes", "ID = ?", new String[]{String.valueOf(classID)});

            // Delete the students associated with the class
            db.delete("Students", "Class_ID = ?", new String[]{String.valueOf(classID)});

            // Get sessions associated with the class
            ArrayList<Session> sessions = getSessions(classID);

            // Delete each session along with its students
            for (Session session : sessions) {
                deleteSession(classID, session.getID());
            }

            // Drop the sessions table associated with the class
            String dropSessionsTable = "DROP TABLE IF EXISTS \"" + classID + " - Sessions\";";
            db.execSQL(dropSessionsTable);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }


    public ArrayList<Student> getStudents(int id) {
        ArrayList<Student> students = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT * FROM \"" + id + " - Students\"", null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Student s = new Student();
                    s.setID(cursor.getInt(cursor.getColumnIndexOrThrow("ID")));
                    s.setFirstName(cursor.getString(cursor.getColumnIndexOrThrow("FirstName")));
                    s.setLastName(cursor.getString(cursor.getColumnIndexOrThrow("LastName")));
                    s.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("Email")));
                    s.setTel(cursor.getString(cursor.getColumnIndexOrThrow("Tel")));
                    students.add(s);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return students;
    }

    public int getNextStudentID(int classID) {
        int id = 0;
        ArrayList<Student> students = getStudents(classID);
        int size = students.size();
        if (size != 0) {
            int last = size - 1;
            Student lastStudent = students.get(last);
            id = lastStudent.getID() + 1;
        }
        return id;
    }

    public Student getStudent(int classID, int studentID) {
        Student student = new Student();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT ID, FirstName, LastName, Email, Tel FROM \"" + classID + " - Students\" WHERE ID = ?", new String[]{String.valueOf(studentID)});
            if (cursor != null && cursor.moveToFirst()) {
                student.setID(cursor.getInt(cursor.getColumnIndexOrThrow("ID")));
                student.setFirstName(cursor.getString(cursor.getColumnIndexOrThrow("FirstName")));
                student.setLastName(cursor.getString(cursor.getColumnIndexOrThrow("LastName")));
                student.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("Email")));
                student.setTel(cursor.getString(cursor.getColumnIndexOrThrow("Tel")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return student;
    }

    public void addStudent(int classID, Student s) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("ID", s.getID());
            values.put("FirstName", s.getFirstName());
            values.put("LastName", s.getLastName());
            values.put("Email", s.getEmail());
            values.put("Tel", s.getTel());
            db.insert("\"" + classID + " - Students\"", null, values);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public void updateStudent(int classID, Student s) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("FirstName", s.getFirstName());
            values.put("LastName", s.getLastName());
            values.put("Email", s.getEmail());
            values.put("Tel", s.getTel());
            db.update("\"" + classID + " - Students\"", values, "ID = ?", new String[]{String.valueOf(s.getID())});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public void deleteStudent(int classID, int studentID) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            db.delete("\"" + classID + " - Students\"", "ID = ?", new String[]{String.valueOf(studentID)});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public ArrayList<Session> getSessions(int classID) {
        ArrayList<Session> sessions = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT * FROM \"" + classID + " - Sessions\"", null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Session s = new Session();
                    s.setID(cursor.getInt(cursor.getColumnIndexOrThrow("ID")));
                    s.setSubject(cursor.getString(cursor.getColumnIndexOrThrow("Subject")));
                    s.setDate(cursor.getString(cursor.getColumnIndexOrThrow("Date")));
                    sessions.add(s);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return sessions;
    }

    public int getNextSessionID(int classID) {
        int id = 0;
        ArrayList<Session> sessions = getSessions(classID);
        int size = sessions.size();
        if (size != 0) {
            int last = size - 1;
            Session lastSession = sessions.get(last);
            id = lastSession.getID() + 1;
        }
        return id;
    }

    public void addSession(int classID, Session s) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("ID", s.getID());
            values.put("Subject", s.getSubject());
            values.put("Date", s.getDate());
            db.insert("\"" + classID + " - Sessions\"", null, values);
            String createSessionTable = "CREATE TABLE IF NOT EXISTS \"" + classID + " - " + s.getID() + "\" (ID INTEGER);";
            db.execSQL(createSessionTable);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public Session getSession(int classID, int sessionID) {
        Session s = new Session();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();
            cursor = db.rawQuery("SELECT ID, Subject, Date FROM \"" + classID + " - Sessions\" WHERE ID = ?", new String[]{String.valueOf(sessionID)});
            if (cursor != null && cursor.moveToFirst()) {
                s.setID(cursor.getInt(cursor.getColumnIndexOrThrow("ID")));
                s.setSubject(cursor.getString(cursor.getColumnIndexOrThrow("Subject")));
                s.setDate(cursor.getString(cursor.getColumnIndexOrThrow("Date")));

                Cursor cursor2 = db.rawQuery("SELECT * FROM \"" + classID + " - " + sessionID + "\"", null);
                ArrayList<Integer> ids = new ArrayList<>();
                if (cursor2 != null && cursor2.moveToFirst()) {
                    do {
                        ids.add(cursor2.getInt(cursor2.getColumnIndexOrThrow("ID")));
                    } while (cursor2.moveToNext());
                    cursor2.close();
                }

                ArrayList<Student> students = new ArrayList<>();
                for (int id : ids) {
                    students.add(getStudent(classID, id));
                }
                s.setStudents(students);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return s;
    }

    public void updateSessionData(int classID, Session s) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("Subject", s.getSubject());
            values.put("Date", s.getDate());
            db.update("\"" + classID + " - Sessions\"", values, "ID = ?", new String[]{String.valueOf(s.getID())});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public void deleteSession(int classID, int sessionID) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            db.delete("\"" + classID + " - Sessions\"", "ID = ?", new String[]{String.valueOf(sessionID)});
            String dropSessionTable = "DROP TABLE IF EXISTS \"" + classID + " - " + sessionID + "\";";
            db.execSQL(dropSessionTable);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public void addStudentsToSession(int classID, int sessionID, ArrayList<Integer> students) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            for (Integer i : students) {
                ContentValues values = new ContentValues();
                values.put("ID", i);
                db.insert("\"" + classID + " - " + sessionID + "\"", null, values);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public void removeStudentsFromSession(int classID, int sessionID, ArrayList<Integer> students) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            for (Integer i : students) {
                db.delete("\"" + classID + " - " + sessionID + "\"", "ID = ?", new String[]{String.valueOf(i)});
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }
}
