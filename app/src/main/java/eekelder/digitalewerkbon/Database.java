package eekelder.digitalewerkbon;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by jur-e on 29-9-2018.
 */

public class Database extends SQLiteOpenHelper {

    private static Database sInstance;
    private final Context context;

    private static final String DATABASE_NAME = "Planning.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_EMPLOYEE = "Employee";
    private static final String TABLE_EMPLOYEE_COLUMN_FIRSTNAME = "firstName";
    private static final String TABLE_EMPLOYEE_COLUMN_SURNAME = "surName";
    private static final String TABLE_EMPLOYEE_COLUMN_ID = "employeeId";
    private static final String TABLE_EMPLOYEE_COLUMN_USERNAME = "userName";
    private static final String TABLE_EMPLOYEE_COLUMN_PASSWORD = "password";

    private static final String TABLE_CLIENT = "Client";
    private static final String TABLE_CLIENT_COLUMN_FIRSTNAME = "firstName";
    private static final String TABLE_CLIENT_COLUMN_SURNAME = "surName";
    private static final String TABLE_CLIENT_COLUMN_ID = "clientId";
    private static final String TABLE_CLIENT_COLUMN_PHONE = "phoneNumber";

    private static final String TABLE_EQUIPMENT = "Equipment";
    private static final String TABLE_EQUIPMENT_COLUMN_ID = "equipmentId";
    private static final String TABLE_EQUIPMENT_COLUMN_TYPE = "type";
    private static final String TABLE_EQUIPMENT_COLUMN_IDFIRE = "idFire";

    private static final String TABLE_PARCEL = "Parcel";
    private static final String TABLE_PARCEL_COLUMN_ID = "parcelId";
    private static final String TABLE_PARCEL_COLUMN_CLIENT_ID = "clientId";
    private static final String TABLE_PARCEL_COLUMN_NAME = "parcelName";
    private static final String TABLE_PARCEL_COLUMN_LONGITUDE = "longitude";
    private static final String TABLE_PARCEL_COLUMN_LATITUDE = "latitude";

    private static final String TABLE_DAYS = "Days";
    private static final String TABLE_DAYS_COLUMN_DAYNUMBERYEAR = "dayNumberYear";
    private static final String TABLE_DAYS_COLUMN_STARTTIME = "startTime";
    private static final String TABLE_DAYS_COLUMN_ENDTIME = "endTime";
    private static final String TABLE_DAYS_COLUMN_PAUSESTART = "pauseStart";
    private static final String TABLE_DAYS_COLUMN_PAUSETIME = "pauseTime";
    private static final String TABLE_DAYS_COLUMN_TRAVELSTART = "travelStart";
    private static final String TABLE_DAYS_COLUMN_TRAVELTIME = "travelTime";

    private static final String TABLE_TASK = "Task";
    private static final String TABLE_TASK_COLUMN_ID = "taskId";
    private static final String TABLE_TASK_COLUMN_CLIENT = "clientId";
    private static final String TABLE_TASK_COLUMN_DAYNUMBER = "dayNumber";
    private static final String TABLE_TASK_COLUMN_STARTTIME = "startTime";
    private static final String TABLE_TASK_COLUMN_ENDTIME = "endTime";
    private static final String TABLE_TASK_COLUMN_REMARKS = "remarks";
    private static final String TABLE_TASK_COLUMN_EMPLOYEES = "employees";
    private static final String TABLE_TASK_COLUMN_EQUIPMENT = "equipment";
    private static final String TABLE_TASK_COLUMN_DUTY = "duty";
    private static final String TABLE_TASK_COLUMN_DAYNUMBERYEAR = "dayNumberYear";
    private static final String TABLE_TASK_COLUMN_STATUS = "status";

    private static final String TABLE_EMPLOYEETIMES = "EmployeeTimes";
    private static final String TABLE_EMPLOYEETIMES_ID = "taskId";
    private static final String TABLE_EMPLOYEETIMES_STARTTIME = "startTime";
    private static final String TABLE_EMPLOYEETIMES_ENDTIME = "endTime";
    private static final String TABLE_EMPLOYEETIMES_PAUSETIME = "pauseTime";
    private static final String TABLE_EMPLOYEETIMES_PAUSESTART = "pauseStart";
    private static final String TABLE_EMPLOYEETIMES_REMARKS = "remarks";
    private static final String TABLE_EMPLOYEETIMES_OUTPUTS = "outputs";
    private static final String TABLE_EMPLOYEETIMES_DAYNUMBER = "EmployeeTimes";
    private static final String TABLE_EMPLOYEETIMES_EMPLOYEES = "employees";
    private static final String TABLE_EMPLOYEETIMES_EFFECTIVETIME = "workhours";


    private static final String clientCollection = "Clients";
    private static final String employeeCollection = "Employees";
    private static final String equipmentCollection = "Equipment";
    private static final String taskCollection = "Tasks";
    private static final String versionDocument = "version";
    private static final String versionDocumentEntry = "currentVersion";
    private static final String employeeTimesCollection = "employeeTimes";


    private FirebaseFirestore dbFire = FirebaseFirestore.getInstance();
    private CollectionReference clientCollectionReference = dbFire.collection(clientCollection);
    private CollectionReference employeeCollectionReference = dbFire.collection(employeeCollection);
    private CollectionReference equipmentCollectionReference = dbFire.collection(equipmentCollection);
    private CollectionReference taskCollectionReference = dbFire.collection(taskCollection);


    private DocumentReference clientVersionDocumentReference = clientCollectionReference.document(versionDocument);
    private DocumentReference employeeVersionDocumentReference = employeeCollectionReference.document(versionDocument);
    private DocumentReference equipmentVersionDocumentReference = equipmentCollectionReference.document(versionDocument);
    private DocumentReference taskVersionDocumentReference = taskCollectionReference.document(versionDocument);

    private static String stringArraySeparator = ",";

    private ExecuteStatus status = new ExecuteStatus();
    private int taskCounter;


    public static synchronized Database getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new Database(context.getApplicationContext());
        }

        return sInstance;
    }

    private Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("tag", "Creating database");

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor preferencesEditor = preferences.edit();
        preferencesEditor.putInt("clientVersion", 0).apply();
        preferencesEditor.putInt("equipmentVersion", 0).apply();
        preferencesEditor.putInt("employeeVersion", 0).apply();
        preferencesEditor.putInt("parcelVersion", 0).apply();
        preferencesEditor.putLong("taskVersionLong", 0).apply();


        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EQUIPMENT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PARCEL);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EMPLOYEE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLIENT);

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DAYS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASK);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EMPLOYEETIMES);


        db.execSQL("create table " + TABLE_EMPLOYEE + " (" + TABLE_EMPLOYEE_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " + TABLE_EMPLOYEE_COLUMN_FIRSTNAME + " TEXT, " + TABLE_EMPLOYEE_COLUMN_SURNAME + " TEXT, " + TABLE_EMPLOYEE_COLUMN_PASSWORD + " TEXT, " + TABLE_EMPLOYEE_COLUMN_USERNAME + " TEXT) ");
        db.execSQL("create table " + TABLE_EQUIPMENT + " (" + TABLE_EQUIPMENT_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " + TABLE_EQUIPMENT_COLUMN_TYPE + " TEXT, " + TABLE_EQUIPMENT_COLUMN_IDFIRE + " TEXT) ");
        db.execSQL("create table " + TABLE_CLIENT + " (" + TABLE_CLIENT_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " + TABLE_CLIENT_COLUMN_FIRSTNAME + " TEXT, " + TABLE_CLIENT_COLUMN_SURNAME + " TEXT, " + TABLE_CLIENT_COLUMN_PHONE + " TEXT) ");
        db.execSQL("create table " + TABLE_PARCEL + " (" + TABLE_PARCEL_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " + TABLE_PARCEL_COLUMN_NAME + " TEXT, " + TABLE_PARCEL_COLUMN_CLIENT_ID + " TEXT, " + TABLE_PARCEL_COLUMN_LONGITUDE + " REAL, " + TABLE_PARCEL_COLUMN_LATITUDE + " REAL) ");

        db.execSQL("create table " + TABLE_DAYS + " (" + TABLE_DAYS_COLUMN_DAYNUMBERYEAR + " INTEGER PRIMARY KEY, " + TABLE_DAYS_COLUMN_STARTTIME + " INTEGER, " + TABLE_DAYS_COLUMN_ENDTIME + " INTEGER, " + TABLE_DAYS_COLUMN_PAUSESTART + " INTEGER, " + TABLE_DAYS_COLUMN_PAUSETIME + " INTEGER, " + TABLE_DAYS_COLUMN_TRAVELSTART + " INTEGER, " + TABLE_DAYS_COLUMN_TRAVELTIME + " INTEGER)");
        db.execSQL("create table " + TABLE_TASK + " (" + TABLE_TASK_COLUMN_ID + " INTEGER PRIMARY KEY, " + TABLE_TASK_COLUMN_CLIENT + " TEXT, " + TABLE_TASK_COLUMN_DAYNUMBER + " INTEGER, " + TABLE_TASK_COLUMN_STARTTIME + " INTEGER, " + TABLE_TASK_COLUMN_ENDTIME + " INTEGER, " + TABLE_TASK_COLUMN_REMARKS + " TEXT, " + TABLE_TASK_COLUMN_DUTY + " TEXT, " + TABLE_TASK_COLUMN_STATUS + " INTEGER, " + TABLE_TASK_COLUMN_DAYNUMBERYEAR + " INTEGER, " + TABLE_TASK_COLUMN_EMPLOYEES  + " TEXT, " +  TABLE_TASK_COLUMN_EQUIPMENT + " TEXT)");
        db.execSQL("create table " + TABLE_EMPLOYEETIMES + " (" + TABLE_EMPLOYEETIMES_ID + " INTEGER PRIMARY KEY, " + TABLE_EMPLOYEETIMES_STARTTIME + " INTEGER, " + TABLE_EMPLOYEETIMES_ENDTIME + " INTEGER, " + TABLE_EMPLOYEETIMES_PAUSESTART + " INTEGER, " + TABLE_EMPLOYEETIMES_PAUSETIME + " INTEGER, " + TABLE_EMPLOYEETIMES_DAYNUMBER + " INTEGER, " + TABLE_EMPLOYEETIMES_EFFECTIVETIME + " INTEGER, " + TABLE_EMPLOYEETIMES_REMARKS + " TEXT, " + TABLE_EMPLOYEETIMES_OUTPUTS + " TEXT, " +  TABLE_EMPLOYEETIMES_EMPLOYEES + " TEXT)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("tag", "Upgrading database");
    }

    public void deleteMeFromTask(eekelder.digitalewerkbon.Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_EMPLOYEETIMES + " WHERE " + TABLE_EMPLOYEETIMES_ID + " = " + task.getDocumentName());
        insertTask(task);
        db.close();

    }

    public boolean setEmployeeTimes(EmployeeTimes employeeTimes){

        if (employeeTimes!=null) {

            SQLiteDatabase db = this.getWritableDatabase();
            db.execSQL("DELETE FROM " + TABLE_EMPLOYEETIMES + " WHERE " + TABLE_EMPLOYEETIMES_ID + " = " + employeeTimes.getTaskNumber());

            ContentValues values = new ContentValues();
            values.put(TABLE_EMPLOYEETIMES_ID, employeeTimes.getTaskNumber());
            values.put(TABLE_EMPLOYEETIMES_DAYNUMBER, employeeTimes.getDayNumber());
            values.put(TABLE_EMPLOYEETIMES_STARTTIME, employeeTimes.getStartTime());
            values.put(TABLE_EMPLOYEETIMES_ENDTIME, employeeTimes.getEndTime());
            values.put(TABLE_EMPLOYEETIMES_PAUSESTART, employeeTimes.getPauseStartTime());
            values.put(TABLE_EMPLOYEETIMES_PAUSETIME, employeeTimes.getPauseTime());
            values.put(TABLE_EMPLOYEETIMES_EMPLOYEES, employeeTimes.getEmployee());
            values.put(TABLE_EMPLOYEETIMES_REMARKS, employeeTimes.getRemarks());
            values.put(TABLE_EMPLOYEETIMES_OUTPUTS, employeeTimes.getOutputs());
            values.put(TABLE_EMPLOYEETIMES_EFFECTIVETIME, employeeTimes.getEffectiveWorkTime());

            long result = db.insert(TABLE_EMPLOYEETIMES, null, values);
            db.close();

            if (result == -1) {
                ExecuteStatus.setStatus(ExecuteStatus.getStatus()-1);

                return false;
            } else {
                ExecuteStatus.setStatus(ExecuteStatus.getStatus()-1);

                return true;

            }

        }

        ExecuteStatus.setStatus(ExecuteStatus.getStatus()-1);

        return false;
    }

    public EmployeeTimes getEmployeeTimes(long taskID){
        SQLiteDatabase db = this.getReadableDatabase();
        Log.d("tag", "Getting EmployeeTimes");
        Cursor results = db.rawQuery("SELECT * FROM " + TABLE_EMPLOYEETIMES + " WHERE " + TABLE_EMPLOYEETIMES_ID + " = " + taskID, null);
        EmployeeTimes employeeTimes = null;

        if (results != null) {
            if (results.moveToFirst()) {
                employeeTimes = new EmployeeTimes();
                int startTime = results.getInt(results.getColumnIndex(TABLE_EMPLOYEETIMES_STARTTIME));
                int endTime = results.getInt(results.getColumnIndex(TABLE_EMPLOYEETIMES_ENDTIME));
                int pauseTime = results.getInt(results.getColumnIndex(TABLE_EMPLOYEETIMES_PAUSETIME));
                int pauseStartTime = results.getInt(results.getColumnIndex(TABLE_EMPLOYEETIMES_PAUSESTART));
                String remarks = results.getString(results.getColumnIndex(TABLE_EMPLOYEETIMES_REMARKS));
                String outputs = results.getString(results.getColumnIndex(TABLE_EMPLOYEETIMES_OUTPUTS));
                String employees = results.getString(results.getColumnIndex(TABLE_EMPLOYEETIMES_EMPLOYEES));
                int dayNumber = results.getInt(results.getColumnIndex(TABLE_EMPLOYEETIMES_DAYNUMBER));
                int effectiveTime = results.getInt(results.getColumnIndex(TABLE_EMPLOYEETIMES_EFFECTIVETIME));
                employeeTimes.setStartTime(startTime);
                employeeTimes.setEndTime(endTime);
                employeeTimes.setPauseTime(pauseTime);
                employeeTimes.setPauseStartTime(pauseStartTime);
                employeeTimes.setTaskNumber(taskID);
                employeeTimes.setRemarks(remarks);
                employeeTimes.setOutputs(outputs);
                employeeTimes.setEmployee(employees);
                employeeTimes.setDayNumber(dayNumber);
                employeeTimes.setEffectiveWorkTime(effectiveTime);
            }
        }
        results.close();
        db.close();

        if (employeeTimes==null) {
            Log.d("tag", "NOT Returning EmployeeTimes ");
        } else {
            Log.d("tag", "Returning EmployeeTimes " + employeeTimes.getDocumentName());
        }

        ExecuteStatus.setStatus(ExecuteStatus.getStatus()-1);

        return employeeTimes;
    }


    public boolean startWorkDay() {
        Calendar calendar = Calendar.getInstance();
        int dayNumber = calendar.get(Calendar.DAY_OF_YEAR);
        int year = calendar.get(Calendar.YEAR);
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        int totalMinutes = hours * 60 + minutes;

        int dayNumberYear = dayNumber * 10000 + year;

        ContentValues values = new ContentValues();
        values.put(TABLE_DAYS_COLUMN_DAYNUMBERYEAR, dayNumberYear);
        values.put(TABLE_DAYS_COLUMN_ENDTIME, 0);
        values.put(TABLE_DAYS_COLUMN_STARTTIME, totalMinutes);
        values.put(TABLE_DAYS_COLUMN_PAUSETIME, 0);
        values.put(TABLE_DAYS_COLUMN_TRAVELTIME, 0);
        values.put(TABLE_DAYS_COLUMN_PAUSESTART, 0);
        values.put(TABLE_DAYS_COLUMN_TRAVELSTART, 0);

        Log.d("tag", "Starting Workday " + dayNumberYear + " Starttime " + totalMinutes);

        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.insert(TABLE_DAYS, null, values);
        db.close();

        if (result == -1) {
            return false;
        } else {
            return true;
        }


    }

    public boolean resetWorkday() {
        SQLiteDatabase db = this.getWritableDatabase();
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int dayNumber = calendar.get(Calendar.DAY_OF_YEAR);
        int dayNumberYearYear = dayNumber * 10000 + year;

        String sql = "DELETE FROM " + TABLE_DAYS + " WHERE " + TABLE_DAYS_COLUMN_DAYNUMBERYEAR + " = " + dayNumberYearYear;
        Log.d("tag", "Deleting Workday " + dayNumber);

        db.execSQL(sql);
        db.close();

        boolean result = startWorkDay();

        return result;
    }

    public boolean continueWorkday() {

        Calendar calendar = Calendar.getInstance();
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        int totalMinutes = hours * 60 + minutes;
        int year = calendar.get(Calendar.YEAR);
        int dayNumber = calendar.get(Calendar.DAY_OF_YEAR);
        int dayNumberYearYear = dayNumber * 10000 + year;
        SQLiteDatabase db = this.getWritableDatabase();

        String sql = "UPDATE " + TABLE_DAYS + " SET " + TABLE_DAYS_COLUMN_PAUSETIME + " = (" + totalMinutes + " - " + TABLE_DAYS_COLUMN_ENDTIME + ")  WHERE " + TABLE_DAYS_COLUMN_DAYNUMBERYEAR + " = " + dayNumberYearYear;
        db.execSQL(sql);
        String sql2 = "UPDATE " + TABLE_DAYS + " SET " + TABLE_DAYS_COLUMN_ENDTIME + " = " + 0 + "  WHERE " + TABLE_DAYS_COLUMN_DAYNUMBERYEAR + " = " + dayNumberYearYear;
        db.execSQL(sql2);
        Log.d("tag", "Continuing workday " + dayNumber);

        db.close();

        boolean result = true; /// add listener if change was correct

        return result;
    }

    public void startPauseOrTravel(String input) {
        Calendar calendar = Calendar.getInstance();
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        int totalMinutes = hours * 60 + minutes;
        SQLiteDatabase db = this.getWritableDatabase();

        if (input.contains("pause")) {
            String sql = "UPDATE " + TABLE_DAYS + " SET " + TABLE_DAYS_COLUMN_PAUSESTART + " = " + totalMinutes + " WHERE " + TABLE_DAYS_COLUMN_ENDTIME + " = " + 0;
            db.execSQL(sql);
        } else if (input.contains("travel")) {
            String sql = "UPDATE " + TABLE_DAYS + " SET " + TABLE_DAYS_COLUMN_TRAVELSTART + " = " + totalMinutes + " WHERE " + TABLE_DAYS_COLUMN_ENDTIME + " = " + 0;
            db.execSQL(sql);
        }
        db.close();

        Log.d("tag", "Starting Pause/Travel " + totalMinutes);


    }


    public int getPauseOrTravelTime(String input, int dayOfYear) {
        Cursor results = null;
        int output = 0;
        SQLiteDatabase db = this.getWritableDatabase();

        if (input.contains("pause")) {
            results = db.rawQuery("SELECT * FROM " + TABLE_DAYS + " WHERE " + TABLE_DAYS_COLUMN_DAYNUMBERYEAR + " = " + dayOfYear, null);
        } else if (input.contains("travel")) {
            results = db.rawQuery("SELECT * FROM " + TABLE_DAYS + " WHERE " + TABLE_DAYS_COLUMN_DAYNUMBERYEAR + " = " + dayOfYear, null);
        }
        db.close();

        if (results != null) {
            if (results.moveToFirst()) {
                if (input.contains("pause")) {
                    output = results.getInt(results.getColumnIndex(TABLE_DAYS_COLUMN_PAUSETIME));
                } else if (input.contains("travel")) {
                    output = results.getInt(results.getColumnIndex(TABLE_DAYS_COLUMN_TRAVELTIME));
                }
            }
        }

        results.close();

        return output;
    }

    public void endPauseOrTravel(String input) {
        Calendar calendar = Calendar.getInstance();
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        int totalMinutes = hours * 60 + minutes;
        SQLiteDatabase db = this.getWritableDatabase();

        if (input.contains("pause")) {
            String sql = "UPDATE " + TABLE_DAYS + " SET " + TABLE_DAYS_COLUMN_PAUSETIME + " = (" + totalMinutes + " - " + TABLE_DAYS_COLUMN_PAUSESTART + ")  WHERE " + TABLE_DAYS_COLUMN_ENDTIME + " = " + 0;
            db.execSQL(sql);
            String sql2 = "UPDATE " + TABLE_DAYS + " SET " + TABLE_DAYS_COLUMN_PAUSESTART + " = " + 0 + "  WHERE " + TABLE_DAYS_COLUMN_ENDTIME + " = " + 0;
            db.execSQL(sql2);
        } else if (input.contains("travel")) {
            String sql = "UPDATE " + TABLE_DAYS + " SET " + TABLE_DAYS_COLUMN_TRAVELTIME + " = (" + totalMinutes + " - " + TABLE_DAYS_COLUMN_TRAVELSTART + ")  WHERE " + TABLE_DAYS_COLUMN_ENDTIME + " = " + 0;
            db.execSQL(sql);
            String sql2 = "UPDATE " + TABLE_DAYS + " SET " + TABLE_DAYS_COLUMN_TRAVELSTART + " = " + 0 + "  WHERE " + TABLE_DAYS_COLUMN_ENDTIME + " = " + 0;
            db.execSQL(sql2);
        }
        db.close();

        Log.d("tag", "Ending Pause/Travel " + totalMinutes);

    }

    public WorkingHours endWorkDay() {

        WorkingHours workingHours = new WorkingHours();

        Calendar calendar = Calendar.getInstance();
        int dayNumber = calendar.get(Calendar.DAY_OF_YEAR); //Not used --> in case of finishing after midnight, than the db cannot find the line with the 'increased' dayNumber
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        int totalMinutes = hours * 60 + minutes;
        int year = calendar.get(Calendar.YEAR);
        int dayNumberYear = dayNumber * 10000 + year;
        SQLiteDatabase db = this.getWritableDatabase();

        String sql = "UPDATE " + TABLE_DAYS + " SET " + TABLE_DAYS_COLUMN_ENDTIME + " = " + totalMinutes + "  WHERE " + TABLE_DAYS_COLUMN_ENDTIME + " = " + 0;
        db.execSQL(sql);

        Cursor results = db.rawQuery("SELECT * FROM " + TABLE_DAYS + " WHERE " + TABLE_DAYS_COLUMN_DAYNUMBERYEAR + " = " + dayNumberYear, null);


        if (results != null) {
            if (results.moveToFirst()) {
                workingHours.setDayOfYearYear(dayNumberYear);
                workingHours.setStartTime(results.getInt(results.getColumnIndex(TABLE_DAYS_COLUMN_STARTTIME)));
                workingHours.setEndTime(results.getInt(results.getColumnIndex(TABLE_DAYS_COLUMN_ENDTIME)));
                workingHours.setPauseTime(results.getInt(results.getColumnIndex(TABLE_DAYS_COLUMN_PAUSETIME)));
                workingHours.setTravelTime(results.getInt(results.getColumnIndex(TABLE_DAYS_COLUMN_TRAVELTIME)));
                workingHours.setDayOfYear(dayNumber);

                Log.d("tag", "Ending workday: Day " + workingHours.getDayOfYear() + " startTime " + workingHours.getStartTime() + " endTime " + workingHours.getEndTime() + " pauseTime " + workingHours.getPauseTime() + " travelTime " + workingHours.getTravelTime());

                results.close();
                db.close();

                return workingHours;
            } else {
                results.close();
                db.close();

                return null;
            }
        } else {
            results.close();
            db.close();
            return null;
        }


    }

    public WorkingHours getWorkDay(int dayNumberYear, int dayOfYear) {

        WorkingHours workingHours = new WorkingHours();

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor results = db.rawQuery("SELECT * FROM " + TABLE_DAYS + " WHERE " + TABLE_DAYS_COLUMN_DAYNUMBERYEAR + " = " + dayNumberYear + " AND " + TABLE_DAYS_COLUMN_ENDTIME + " > " + 0, null);


        if (results != null) {
            if (results.moveToFirst()) {
                workingHours.setDayOfYearYear(dayNumberYear);
                workingHours.setStartTime(results.getInt(results.getColumnIndex(TABLE_DAYS_COLUMN_STARTTIME)));
                workingHours.setEndTime(results.getInt(results.getColumnIndex(TABLE_DAYS_COLUMN_ENDTIME)));
                workingHours.setPauseTime(results.getInt(results.getColumnIndex(TABLE_DAYS_COLUMN_PAUSETIME)));
                workingHours.setTravelTime(results.getInt(results.getColumnIndex(TABLE_DAYS_COLUMN_TRAVELTIME)));
                workingHours.setDayOfYear(dayOfYear);

                Log.d("tag", "Getting workday: Day " + workingHours.getDayOfYear() + " startTime " + workingHours.getStartTime() + " endTime " + workingHours.getEndTime() + " pauseTime " + workingHours.getPauseTime() + " travelTime " + workingHours.getTravelTime());

                results.close();
                db.close();

                return workingHours;
            } else {
                results.close();
                db.close();

                return null;
            }
        } else {
            results.close();
            db.close();
            return null;
        }


    }



    public boolean hasWorkDayStarted() {
        Calendar calendar = Calendar.getInstance();
        int dayNumber = calendar.get(Calendar.DAY_OF_YEAR);
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor results = db.rawQuery("SELECT * FROM " + TABLE_DAYS + " WHERE " + TABLE_DAYS_COLUMN_ENDTIME + " = " + 0, null);
        Cursor results2 = db.rawQuery("SELECT * FROM " + TABLE_DAYS + " WHERE " + TABLE_DAYS_COLUMN_DAYNUMBERYEAR + " = " + dayNumber, null);
        db.close();
        if (results.getCount() < 1 || results == null) {
            if (results2.getCount() < 1 || results2 == null) {
                results.close();
                results2.close();
                return false;

            } else {
                results.close();
                results2.close();
                return true;
            }

        } else {
            results.close();
            results2.close();
            return true;

        }


    }

    public void checkTaskData() {

        final long[] versionDB = {0L};

        taskVersionDocumentReference.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                versionDB[0] = document.getLong(versionDocumentEntry);
                                updateTaskData(versionDB[0]);
                            }
                        }
                    }
                });
    }

    public void updateTaskData(final long versionDB) {
        boolean update = false;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        long taskVersion = preferences.getLong("taskVersionLong", 0);

        Log.d("tag", "TaskAppVersion is " + taskVersion + " TaskFireVersion is " + versionDB);

        if (taskVersion != versionDB) {
            update = true;
        } else {
            ExecuteStatus.setStatus(ExecuteStatus.getStatus()-1);
        }

        if (update) {
            Log.d("tag", "Updating Task");
            final List<eekelder.digitalewerkbon.Task> taskList = new ArrayList<>();
            SQLiteDatabase db = this.getWritableDatabase();

            db.execSQL("DELETE FROM " + TABLE_TASK);
            db.close();

            Calendar calendar = Calendar.getInstance();
            int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR) - 7;
            int year = calendar.get(Calendar.YEAR);
            int dayOfYearYear = dayOfYear * 10000 + year;

            taskCollectionReference
                    .whereGreaterThan("dayNumberYear", dayOfYearYear)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                    if (!documentSnapshot.getId().equals("version")) {
                                        eekelder.digitalewerkbon.Task task2 = documentSnapshot.toObject(eekelder.digitalewerkbon.Task.class);
                                        taskList.add(task2);
                                        Log.d("tag", "Found Task " + task2.getDocumentName());

                                    }
                                }

                                insertTaskData(taskList, versionDB);
                            }
                        }
                    });


        } else {
            //checkEquipmentData();
        }

    }

    public void insertTaskData(List<eekelder.digitalewerkbon.Task> taskList, long versionDB){

        taskCounter = taskList.size();

        for (eekelder.digitalewerkbon.Task t : taskList){
            t.setStatus(0);
            insertTask(t);
        }

        if (taskCounter == 0) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor preferencesEditor = preferences.edit();
            preferencesEditor.putLong("taskVersionLong", versionDB).apply();
            Log.d("tag", "Setting AppTaskVersion to " + versionDB);
            ExecuteStatus.setStatus(ExecuteStatus.getStatus()-1);
        }
    }

    public boolean insertTask(eekelder.digitalewerkbon.Task task) {
        SQLiteDatabase db = this.getWritableDatabase();

        String employees = "";
        if (!task.getEmployees().isEmpty()) {
            for (String s : task.getEmployees()) {
                employees += s + stringArraySeparator;
            }
            employees = employees.substring(0, employees.length() - 1);
        }

        String equipment =  "";
        if (!task.getEquipment().isEmpty()){
            for (String s : task.getEquipment()){
                equipment += s + stringArraySeparator;
            }
            equipment = equipment.substring(0, equipment.length() - 1);
        }


        db.execSQL("DELETE FROM " + TABLE_TASK + " WHERE " + TABLE_TASK_COLUMN_ID + " = " + task.getDocumentName()); //update Firebase, since not relevant for other users.

        ContentValues contentValues = new ContentValues();
        contentValues.put(TABLE_TASK_COLUMN_CLIENT, task.getClient());
        contentValues.put(TABLE_TASK_COLUMN_DAYNUMBER, task.getDayNumber());
        contentValues.put(TABLE_TASK_COLUMN_DAYNUMBERYEAR, task.getDayNumberYear());
        contentValues.put(TABLE_TASK_COLUMN_STARTTIME, task.getStartTime());
        contentValues.put(TABLE_TASK_COLUMN_ENDTIME, task.getEndTime());
        contentValues.put(TABLE_TASK_COLUMN_DUTY, task.getDescription());
        contentValues.put(TABLE_TASK_COLUMN_REMARKS, task.getRemarks());
        contentValues.put(TABLE_TASK_COLUMN_EMPLOYEES, employees);
        contentValues.put(TABLE_TASK_COLUMN_EQUIPMENT, equipment);
        contentValues.put(TABLE_TASK_COLUMN_ID, task.getDocumentName());
        contentValues.put(TABLE_TASK_COLUMN_STATUS,task.getStatus());
        long result = db.insert(TABLE_TASK, null, contentValues);

        boolean succes = false;
        if (result != -1) {
            taskCounter--;
            Log.d("tag", "Inserting Task " + task.getClient() + " " + task.getDescription() + " taskCounter " + taskCounter);
            succes = true;

        } else {
            Log.d("tag", "ERROR: NOT Inserting Task " + task.getClient() + " " + task.getDescription() + " taskCounter " + taskCounter);
        }



        db.close();

        return succes;
    }

    public void checkClientData() {

        final int[] versionDB = {0};

        clientVersionDocumentReference.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                versionDB[0] = document.getLong(versionDocumentEntry).intValue();
                                updateClientData(versionDB[0]);
                            } else {
                                checkEquipmentData();
                                ExecuteStatus.setStatus(ExecuteStatus.getStatus()-1);
                            }
                        } else {
                            checkEquipmentData();
                            ExecuteStatus.setStatus(ExecuteStatus.getStatus()-1);
                        }
                    }
                });


    }

    public void updateClientData(final int versionDB) {
        boolean update = false;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        int clientVersion = preferences.getInt("clientVersion", 0);

        Log.d("tag", "ClientAppVersion is " + clientVersion + " ClientFireVersion is " + versionDB);

        if (clientVersion != versionDB) {
            update = true;
        } else {
            ExecuteStatus.setStatus(ExecuteStatus.getStatus()-1);
            checkEquipmentData();
        }

        if (update) {
            Log.d("tag", "Updating Client");
            final List<Client> clientList = new ArrayList<>();
            SQLiteDatabase db = this.getWritableDatabase();

            db.close();

            clientCollectionReference.get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                    if (!documentSnapshot.getId().equals("version")) {
                                        Client client = documentSnapshot.toObject(Client.class);
                                        clientList.add(client);
                                        Log.d("tag", "Found Client " + client.getDocumentName());

                                    }
                                }
                                insertClientData(clientList, versionDB);

                            } else {
                                checkEquipmentData();
                                ExecuteStatus.setStatus(ExecuteStatus.getStatus()-1);
                            }
                        }
                    });


        }

    }

    public void insertClientData(List<Client> clientList, int versionDB) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_CLIENT);

        int clientCounter = clientList.size();

        for (Client c : clientList) {

            ContentValues contentValues = new ContentValues();
            contentValues.put(TABLE_CLIENT_COLUMN_FIRSTNAME, c.getFirstName());
            contentValues.put(TABLE_CLIENT_COLUMN_SURNAME, c.getSurName());
            contentValues.put(TABLE_CLIENT_COLUMN_PHONE, c.getPhoneNumber());
            long result = db.insert(TABLE_CLIENT, null, contentValues);

            if (result != -1) {
                clientCounter--;
                Log.d("tag", "Inserting Client " + c.getDocumentName() + " clientCounter " + clientCounter);
            } else {
                Log.d("tag", "ERROR: NOT Inserting Client " + c.getDocumentName() + " clientCounter " + clientCounter);
            }

        }
        db.close();


        if (clientCounter == 0) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor preferencesEditor = preferences.edit();
            preferencesEditor.putInt("clientVersion", versionDB).apply();
            Log.d("tag", "Setting AppClientVersion to " + versionDB);
        }

        ExecuteStatus.setStatus(ExecuteStatus.getStatus()-1);
        checkEquipmentData();

    }

    public void checkParcelData(int version) {
        final int[] versionDB = {0};

        clientVersionDocumentReference.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            versionDB[0] = documentSnapshot.getLong(versionDocumentEntry).intValue();

                        }
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        updateParcelData(versionDB[0]);
                    }
                });


    }

    public void updateParcelData(int versionDB) {
        boolean update = false;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor preferencesEditor = preferences.edit();
        int parcelVersion = preferences.getInt("parcelVersion", 0);

        if (parcelVersion != versionDB) {
            update = true;
        } else {
            status.setStatus(status.getStatus() - 1);
        }

        if (update) {
            SQLiteDatabase db = this.getWritableDatabase();

            db.execSQL("DELETE FROM " + TABLE_PARCEL);

            db.close();
            clientCollectionReference.get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            List<DocumentSnapshot> documents = task.getResult().getDocuments();
                            for (DocumentSnapshot d : documents) {
                                Parcel parcel = d.toObject(Parcel.class);
                                insertParcelData(parcel);
                            }
                        }
                    });
            status.setStatus(status.getStatus() - 1);

            preferencesEditor.putInt("parcelVersion", versionDB);
        }
    }

    public void insertParcelData(Parcel parcel) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(TABLE_PARCEL_COLUMN_NAME, parcel.getName());
        contentValues.put(TABLE_PARCEL_COLUMN_CLIENT_ID, parcel.getClient());
        contentValues.put(TABLE_PARCEL_COLUMN_LONGITUDE, parcel.getLongitude());
        contentValues.put(TABLE_PARCEL_COLUMN_LATITUDE, parcel.getLatitude());
        db.insert(TABLE_PARCEL, null, contentValues);

        db.close();

    }

    public void checkEquipmentData() {
        final int[] versionDB = {0};

        equipmentVersionDocumentReference.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                versionDB[0] = document.getLong(versionDocumentEntry).intValue();
                                updateEquipmentData(versionDB[0]);
                            } else {
                                checkEmployeeData();
                                ExecuteStatus.setStatus(ExecuteStatus.getStatus()-1);
                            }
                        } else {
                            checkEmployeeData();
                            ExecuteStatus.setStatus(ExecuteStatus.getStatus()-1);

                        }
                    }
                });
    }

    public void updateEquipmentData(final int versionDB) {
        boolean update = false;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        int equipmentVersion = preferences.getInt("equipmentVersion", 0);

        Log.d("tag", "EquipmentAppVersion is " + equipmentVersion + " EquipmentFireVersion is " + versionDB);


        if (equipmentVersion != versionDB) {
            update = true;
        } else {
            ExecuteStatus.setStatus(ExecuteStatus.getStatus()-1);
            checkEmployeeData();
        }

        if (update) {
            Log.d("tag", "Updating Equipment");
            final List<Equipment> equipmentList = new ArrayList<>();

            equipmentCollectionReference.get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    if (!document.getId().equals("version")) {
                                        Equipment equipment = document.toObject(Equipment.class);
                                        equipmentList.add(equipment);
                                        Log.d("tag", "Found Equipment " + equipment.getType());
                                    }
                                }
                                insertEquipmentData(equipmentList, versionDB);

                            } else {
                                ExecuteStatus.setStatus(ExecuteStatus.getStatus()-1);
                                checkEmployeeData();
                            }

                        }
                    });


        }


    }

    public void insertEquipmentData(List<Equipment> equipmentList, int versionDB) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_EQUIPMENT);

        int equipmentCounter = equipmentList.size();

        for (Equipment e : equipmentList) {

            ContentValues contentValues = new ContentValues();
            contentValues.put(TABLE_EQUIPMENT_COLUMN_IDFIRE, e.getId());
            contentValues.put(TABLE_EQUIPMENT_COLUMN_TYPE, e.getType());

            long result = db.insert(TABLE_EQUIPMENT, null, contentValues);

            if (result != -1) {
                equipmentCounter--;
                Log.d("tag", "Inserting Equipment " + e.getType() + " equipmentCounter " + equipmentCounter);
            } else
                Log.d("tag", "ERROR: NOT Inserting Equipment " + e.getType() + " equipmentCounter " + equipmentCounter);


        }

        db.close();


        if (equipmentCounter == 0) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor preferencesEditor = preferences.edit();
            preferencesEditor.putInt("equipmentVersion", versionDB).apply();
            Log.d("tag", "Setting AppEquipmentVersion to " + versionDB);
        }

        ExecuteStatus.setStatus(ExecuteStatus.getStatus()-1);
        checkEmployeeData();


    }

    public void checkEmployeeData() {
        final int[] versionDB = {0};

        employeeVersionDocumentReference.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                versionDB[0] = document.getLong(versionDocumentEntry).intValue();
                                updateEmployeeData(versionDB[0]);
                            } else {
                                checkTaskData();
                                ExecuteStatus.setStatus(ExecuteStatus.getStatus()-1);
                            }
                        } else {
                            checkTaskData();
                            ExecuteStatus.setStatus(ExecuteStatus.getStatus()-1);
                        }
                    }
                });
    }

    public void updateEmployeeData(final int versionDB) {
        boolean update = false;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        int employeeVersion = preferences.getInt("employeeVersion", 0);

        Log.d("tag", "EmployeeAppVersion is " + employeeVersion + " EmployeeFireVersion is " + versionDB);


        if (employeeVersion != versionDB) {
            update = true;
        } else {
            ExecuteStatus.setStatus(ExecuteStatus.getStatus()-1);
            checkTaskData();
        }

        if (update) {
            Log.d("tag", "Updating Employee");
            final List<Employee> employeeList = new ArrayList<>();

            employeeCollectionReference.get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    if (!document.getId().equals("version")) {
                                        Employee employee = document.toObject(Employee.class);
                                        employeeList.add(employee);
                                        Log.d("tag", "Found Employee " + employee.getDocumentName());
                                    }
                                }

                                insertEmployeeData(employeeList, versionDB);
                            } else {
                                ExecuteStatus.setStatus(ExecuteStatus.getStatus()-1);
                                checkTaskData();
                            }
                        }
                    });


        }
    }


    public void insertEmployeeData(List<Employee> employeeList, int versionDB) {
        int employeeCounter = employeeList.size();

        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_EMPLOYEE);

        for (Employee e : employeeList) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(TABLE_EMPLOYEE_COLUMN_FIRSTNAME, e.getFirstName());
            contentValues.put(TABLE_EMPLOYEE_COLUMN_SURNAME, e.getSurName());
            contentValues.put(TABLE_EMPLOYEE_COLUMN_USERNAME, e.getDocumentName());
            contentValues.put(TABLE_EMPLOYEE_COLUMN_PASSWORD, e.getPassword());
            long result = db.insert(TABLE_EMPLOYEE, null, contentValues);

            if (result!=-1){
                employeeCounter--;
                Log.d("tag", "Inserting Employee " + e.getDocumentName() + " employeeCounter " + employeeCounter);
            } else {
                Log.d("tag", "ERROR: NOT Inserting Employee " + e.getDocumentName() + " employeeCounter " + employeeCounter);

            }



        }
        db.close();

        if (employeeCounter == 0) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor preferencesEditor = preferences.edit();
            preferencesEditor.putInt("employeeVersion", versionDB).apply();
            Log.d("tag", "Setting AppEmployeeVersion to " + versionDB);

        }

        ExecuteStatus.setStatus(ExecuteStatus.getStatus()-1);
        checkTaskData();

    }


    public List<Equipment> getEquipmentList() {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor results = db.rawQuery("SELECT * FROM " + TABLE_EQUIPMENT, null);
        List<Equipment> equipmentList = new ArrayList<Equipment>();

        if (results != null) {
            while (results.moveToNext()) {
                String equipmentId = results.getString(results.getColumnIndex(TABLE_EQUIPMENT_COLUMN_IDFIRE));
                String type = results.getString(results.getColumnIndex(TABLE_EQUIPMENT_COLUMN_TYPE));
                Equipment equipment = new Equipment();
                equipment.setId(equipmentId);
                equipment.setType(type);
                equipmentList.add(equipment);
            }
        }
        results.close();
        db.close();

        return equipmentList;
    }


    public Employee searchEmployeeByUserName(String userName) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor results = db.rawQuery("SELECT * FROM " + TABLE_EMPLOYEE + " WHERE " + TABLE_EMPLOYEE_COLUMN_USERNAME + " LIKE '%" + userName + "%'", null);
        Employee employee = null;

        if (results != null) {
            if (results.moveToFirst()) {
                String firstName = results.getString(results.getColumnIndex(TABLE_EMPLOYEE_COLUMN_FIRSTNAME));
                String surName = results.getString(results.getColumnIndex(TABLE_EMPLOYEE_COLUMN_SURNAME));
                String password = results.getString(results.getColumnIndex(TABLE_EMPLOYEE_COLUMN_PASSWORD));
                employee = new Employee();
                employee.setSurName(surName);
                employee.setFirstName(firstName);
                employee.setPassword(password);
            }
        }
        results.close();
        db.close();
        return employee;
    }

    public Client searchClientByFullName(String firstName, String surName) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor results = db.rawQuery("SELECT * FROM " + TABLE_CLIENT + " WHERE " + TABLE_CLIENT_COLUMN_SURNAME + " LIKE '%" + surName + "%' AND " + TABLE_CLIENT_COLUMN_FIRSTNAME + " LIKE '%" + firstName + "%'", null);
        Client client = null;

        if (results != null) {
            if (results.moveToFirst()) {
                String firstName2 = results.getString(results.getColumnIndex(TABLE_CLIENT_COLUMN_FIRSTNAME));
                String surName2 = results.getString(results.getColumnIndex(TABLE_CLIENT_COLUMN_SURNAME));
                String phoneNumber = results.getString(results.getColumnIndex(TABLE_CLIENT_COLUMN_PHONE));
                client = new Client();
                client.setFirstName(firstName2);
                client.setPhoneNumber(phoneNumber);
                client.setSurName(surName2);
            }
        }
        results.close();
        db.close();
        return client;
    }


    public List<Employee> getEmployeeList() {
        List<Employee> employeeList = new ArrayList<Employee>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor results = db.rawQuery("SELECT * FROM " + TABLE_EMPLOYEE, null);

        if (results != null) {
            while (results.moveToNext()) {
                String firstName = results.getString(results.getColumnIndex(TABLE_EMPLOYEE_COLUMN_FIRSTNAME));
                String surName = results.getString(results.getColumnIndex(TABLE_EMPLOYEE_COLUMN_SURNAME));
                String password = results.getString(results.getColumnIndex(TABLE_EMPLOYEE_COLUMN_PASSWORD));
                Employee employee = new Employee();
                employee.setSurName(surName);
                employee.setFirstName(firstName);
                employee.setPassword(password);
                employeeList.add(employee);
            }
        }
        results.close();
        db.close();
        return employeeList;
    }

    public List<Client> getClientList() {
        SQLiteDatabase db = this.getReadableDatabase();

        List<Client> clientList = new ArrayList<Client>();
        Cursor results = db.rawQuery("SELECT * FROM " + TABLE_CLIENT, null);

        if (results != null) {
            while (results.moveToNext()) {
                String firstName = results.getString(results.getColumnIndex(TABLE_CLIENT_COLUMN_FIRSTNAME));
                String surName = results.getString(results.getColumnIndex(TABLE_CLIENT_COLUMN_SURNAME));
                String phoneNumber = results.getString(results.getColumnIndex(TABLE_CLIENT_COLUMN_PHONE));
                Client client = new Client();
                client.setFirstName(firstName);
                client.setSurName(surName);
                client.setPhoneNumber(phoneNumber);
                clientList.add(client);
            }
        }
        results.close();
        db.close();
        return clientList;
    }

    public void checkData() {

        if (ExecuteStatus.getStatus()==0) { // twee database acties mogen niet door elkaar lopen.
            Log.d("tag", "Fixed Data check begonnen");
            ExecuteStatus.setStatus(4); //number depending on number of SQLite tables to check
            checkClientData();
        } else {
            Log.d("tag", "Fixed Data check nog bezig, kan niet nogmaals worden gestart!");

        }

    }

    public eekelder.digitalewerkbon.Task getTask(long taskNumber) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor results = db.rawQuery("SELECT * FROM " + TABLE_TASK + " WHERE " + TABLE_TASK_COLUMN_ID + " = " + taskNumber, null);

        eekelder.digitalewerkbon.Task task = null;

        if (results != null) {
            if (results.moveToFirst()) {
                String client = results.getString(results.getColumnIndex(TABLE_TASK_COLUMN_CLIENT));
                int startTime = results.getInt(results.getColumnIndex(TABLE_TASK_COLUMN_STARTTIME));
                int endTime = results.getInt(results.getColumnIndex(TABLE_TASK_COLUMN_ENDTIME));
                String employees = results.getString(results.getColumnIndex(TABLE_TASK_COLUMN_EMPLOYEES));
                String equipment = results.getString(results.getColumnIndex(TABLE_TASK_COLUMN_EQUIPMENT));
                String remarks = results.getString(results.getColumnIndex(TABLE_TASK_COLUMN_REMARKS));
                String description = results.getString(results.getColumnIndex(TABLE_TASK_COLUMN_DUTY));
                int dayNumber2 = results.getInt(results.getColumnIndex(TABLE_TASK_COLUMN_DAYNUMBER));
                int dayNumberYear2 = results.getInt(results.getColumnIndex(TABLE_TASK_COLUMN_DAYNUMBERYEAR));
                int status = results.getInt(results.getColumnIndex(TABLE_TASK_COLUMN_STATUS));
                long id = results.getLong(results.getColumnIndex(TABLE_TASK_COLUMN_ID));

                Log.d("tag", "Read Task " + dayNumberYear2);

                task = new eekelder.digitalewerkbon.Task();
                task.setClient(client);
                task.setStartTime(startTime);
                task.setEndTime(endTime);
                task.setEmployees(convertStringToArrayList(employees));
                task.setEquipment(convertStringToArrayList(equipment));
                task.setRemarks(remarks);
                task.setDescription(description);
                task.setDayNumber(dayNumber2);
                task.setDayNumberYear(dayNumberYear2);
                task.setStatus(status);
                task.setDocumentName(id);
            }
        }

        results.close();
        db.close();

        return task;

    }

    public List<eekelder.digitalewerkbon.Task> getTaskListDay(int dayNumber, boolean allTasks) {

        SQLiteDatabase db = this.getReadableDatabase();
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int dayNumberYear = dayNumber * 10000 + year;

        Log.d("tag", "Searching tasks for dayYear " + dayNumberYear);
        Cursor results = db.rawQuery("SELECT * FROM " + TABLE_TASK + " WHERE " + TABLE_TASK_COLUMN_DAYNUMBERYEAR + " = " + dayNumberYear, null);

        List<eekelder.digitalewerkbon.Task> taskList = new ArrayList<>();

        if (results != null) {
            while (results.moveToNext()) {
                String client = results.getString(results.getColumnIndex(TABLE_TASK_COLUMN_CLIENT));
                int startTime = results.getInt(results.getColumnIndex(TABLE_TASK_COLUMN_STARTTIME));
                int endTime = results.getInt(results.getColumnIndex(TABLE_TASK_COLUMN_ENDTIME));
                String employees = results.getString(results.getColumnIndex(TABLE_TASK_COLUMN_EMPLOYEES));
                String equipment = results.getString(results.getColumnIndex(TABLE_TASK_COLUMN_EQUIPMENT));
                String remarks = results.getString(results.getColumnIndex(TABLE_TASK_COLUMN_REMARKS));
                String description = results.getString(results.getColumnIndex(TABLE_TASK_COLUMN_DUTY));
                int dayNumber2 = results.getInt(results.getColumnIndex(TABLE_TASK_COLUMN_DAYNUMBER));
                int dayNumberYear2 = results.getInt(results.getColumnIndex(TABLE_TASK_COLUMN_DAYNUMBERYEAR));
                int status = results.getInt(results.getColumnIndex(TABLE_TASK_COLUMN_STATUS));
                long id = results.getLong(results.getColumnIndex(TABLE_TASK_COLUMN_ID));

                Log.d("tag", "Read Task " + dayNumberYear2);

                eekelder.digitalewerkbon.Task task = new eekelder.digitalewerkbon.Task();
                task.setClient(client);
                task.setStartTime(startTime);
                task.setEndTime(endTime);
                task.setEmployees(convertStringToArrayList(employees));
                task.setEquipment(convertStringToArrayList(equipment));
                task.setRemarks(remarks);
                task.setDescription(description);
                task.setDayNumber(dayNumber2);
                task.setDayNumberYear(dayNumberYear2);
                task.setStatus(status);
                task.setDocumentName(id);

                if (!allTasks){
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                    String userName = preferences.getString("employee","");
                    if (employees.contains(userName)){
                        taskList.add(task);
                        Log.d("tag", "Adding Task to daily task list");
                    }
                } else {
                    taskList.add(task);
                    Log.d("tag", "Adding Task to daily task list");
                }
            }

        }

        results.close();
        db.close();

        return taskList;
    }

    private ArrayList<String> convertStringToArrayList(String string){
        String[] array = string.split(stringArraySeparator);
        ArrayList<String> stringArrayList = new ArrayList<>();
        for (int i = 0; i<array.length; i++){
            stringArrayList.add(array[i]);
            Log.d("db", "Adding " + array[i] + " to stringArrayList");

        }
        return  stringArrayList;
    }



}
