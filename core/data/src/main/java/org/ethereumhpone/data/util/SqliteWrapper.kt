package org.ethereumhpone.data.util

import android.app.ActivityManager
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteException
import android.net.Uri
import android.widget.Toast


object SqliteWrapper {
    private const val SQLITE_EXCEPTION_DETAIL_MESSAGE = "unable to open database file"

    // FIXME: It looks like outInfo.lowMemory does not work well as we expected.
    // after run command: adb shell fillup -p 100, outInfo.lowMemory is still false.
    private fun isLowMemory(context: Context?): Boolean {
        if (null == context) {
            return false
        }
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val outInfo = ActivityManager.MemoryInfo()
        am.getMemoryInfo(outInfo)
        return outInfo.lowMemory
    }

    // FIXME: need to optimize this method.
    private fun isLowMemory(e: SQLiteException): Boolean {
        return e.message == SQLITE_EXCEPTION_DETAIL_MESSAGE
    }

    fun checkSQLiteException(context: Context?, e: SQLiteException) {
        if (isLowMemory(e)) {
            Toast.makeText(
                context, "Low Memory",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            throw e
        }
    }

    fun query(
        context: Context?,
        resolver: ContentResolver,
        uri: Uri?,
        projection: Array<String>,
        selection: String?,
        selectionArgs: Array<String?>?,
        sortOrder: String?
    ): Cursor? {
        return try {
            resolver.query(uri!!, projection, selection, selectionArgs, sortOrder)
        } catch (e: SQLiteException) {
            //Timber.e(e, "Catch a SQLiteException when query: ")
            checkSQLiteException(context, e)
            null
        }
    }

    fun requery(context: Context?, cursor: Cursor): Boolean {
        return try {
            cursor.requery()
        } catch (e: SQLiteException) {
            //Timber.e(e, "Catch a SQLiteException when requery: ")
            checkSQLiteException(context, e)
            false
        }
    }

    fun update(
        context: Context?, resolver: ContentResolver, uri: Uri?,
        values: ContentValues?, where: String?, selectionArgs: Array<String?>?
    ): Int {
        return try {
            resolver.update(uri!!, values, where, selectionArgs)
        } catch (e: SQLiteException) {
            //Timber.e(e, "Catch a SQLiteException when update: ")
            checkSQLiteException(context, e)
            -1
        }
    }

    fun delete(
        context: Context?, resolver: ContentResolver, uri: Uri?,
        where: String?, selectionArgs: Array<String?>?
    ): Int {
        return try {
            resolver.delete(uri!!, where, selectionArgs)
        } catch (e: SQLiteException) {
            //Timber.e(e, "Catch a SQLiteException when delete: ")
            checkSQLiteException(context, e)
            -1
        }
    }

    fun insert(
        context: Context?, resolver: ContentResolver,
        uri: Uri?, values: ContentValues?
    ): Uri? {
        return try {
            resolver.insert(uri!!, values)
        } catch (e: SQLiteException) {
            //Timber.e(e, "Catch a SQLiteException when insert: ")
            checkSQLiteException(context, e)
            null
        }
    }
}
