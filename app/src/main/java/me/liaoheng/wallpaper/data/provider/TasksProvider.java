package me.liaoheng.wallpaper.data.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import me.liaoheng.wallpaper.data.db.DBHelper;

/**
 * @author liaoheng
 * @version 2018-01-16 15:44
 */
public class TasksProvider extends ContentProvider {

    private DBHelper mDbHelper;
    private UriMatcher mUriMatcher;

    @Override
    public boolean onCreate() {
        mDbHelper = new DBHelper(getContext());
        mUriMatcher = buildUriMatcher();
        return true;
    }

    private final int OK = 111;

    UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = TasksContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, TasksContract.PATH, OK);

        return matcher;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        if (mUriMatcher.match(uri) != OK) {
            throw new IllegalArgumentException("Error Uri: " + uri);
        }
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        return db.query(TasksContract.TaskEntry.TABLE_NAME, projection, selection, selectionArgs, sortOrder, null, null);
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        if (mUriMatcher.match(uri) != OK) {
            throw new IllegalArgumentException("Error Uri: " + uri);
        }
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long _id;
        _id = db.insert(TasksContract.TaskEntry.TABLE_NAME, null, values);
        Uri returnUri;
        if (_id > 0)
            returnUri = TasksContract.TaskEntry.buildUri(_id);
        else
            throw new SQLException("Failed to insert row into " + uri);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        if (mUriMatcher.match(uri) != OK) {
            throw new IllegalArgumentException("Error Uri: " + uri);
        }
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        return db.delete(TasksContract.TaskEntry.TABLE_NAME, selection, selectionArgs);
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        if (mUriMatcher.match(uri) != OK) {
            throw new IllegalArgumentException("Error Uri: " + uri);
        }
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        return db.update(TasksContract.TaskEntry.TABLE_NAME, values, selection, selectionArgs);
    }
}
