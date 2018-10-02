package com.example.android.vitastore.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.android.vitastore.data.SupplementContract.SupplementEntry;


public class SupplementProvider extends ContentProvider {

    public static final String LOG_TAG = SupplementProvider.class.getSimpleName();

    private static final int SUPPLEMENTS = 100;
    private static final int SUPPLEMENT_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer, runs the first time anything from this class is called
    static {
        sUriMatcher.addURI(SupplementContract.CONTENT_AUTHORITY, SupplementContract.PATH_SUPPLEMENTS, SUPPLEMENTS);
        sUriMatcher.addURI(SupplementContract.CONTENT_AUTHORITY, SupplementContract.PATH_SUPPLEMENTS + "/#", SUPPLEMENT_ID);
    }

    private SupplementDbHelper suppDbHelper;

    @Override
    public boolean onCreate() {
        suppDbHelper = new SupplementDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase database = suppDbHelper.getReadableDatabase();
        Cursor cursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case SUPPLEMENTS:
                cursor = database.query(SupplementContract.SupplementEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case SUPPLEMENT_ID:
                selection = SupplementContract.SupplementEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(SupplementEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        if (getContext() != null)
            cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        if (contentValues.getAsString(SupplementEntry.COLUMN_PRODUCT_NAME) == null)
            throw new IllegalArgumentException("Product requires a name");

        Double price = contentValues.getAsDouble(SupplementEntry.COLUMN_PRODUCT_PRICE);
        if (price != null && price < 0)
            throw new IllegalArgumentException("Product requires a valid price");

        Integer quantity = contentValues.getAsInteger(SupplementEntry.COLUMN_PRODUCT_QUANTITY);
        if (quantity == null || !SupplementEntry.isValidQuantity(quantity))
            throw new IllegalArgumentException("Product requires valid quantity");

        if (contentValues.getAsString(SupplementEntry.COLUMN_SUPPLIER_NAME) == null)
            throw new IllegalArgumentException("Supplier requires a name");

        if (contentValues.getAsString(SupplementEntry.COLUMN_SUPPLIER_TELEPHONE) == null)
            throw new IllegalArgumentException("Supplier requires a telephone");

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case SUPPLEMENTS:
                return insertProduct(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion not supported for " + uri);
        }
    }

    /**
     * Helper method to insert a new product. Returns the new content URI for that specific row in the database.
     */
    private Uri insertProduct(Uri uri, ContentValues values) {
        SQLiteDatabase database = suppDbHelper.getWritableDatabase();

        long id = database.insert(SupplementContract.SupplementEntry.TABLE_NAME, null, values);

        // If the ID is -1 insertion failed. Log an error and return null
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        if (getContext() != null)
            getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case SUPPLEMENTS:
                return updateProduct(uri, contentValues, selection, selectionArgs);
            case SUPPLEMENT_ID:
                selection = SupplementEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Helper method to update existing products in the database. Returns the number of rows that were successfully updated.
     */
    private int updateProduct(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        if (contentValues.containsKey(SupplementEntry.COLUMN_PRODUCT_NAME))
            if (contentValues.getAsString(SupplementEntry.COLUMN_PRODUCT_NAME) == null)
                throw new IllegalArgumentException("Product requires a name");

        if (contentValues.containsKey(SupplementEntry.COLUMN_PRODUCT_PRICE)) {
            Double price = contentValues.getAsDouble(SupplementEntry.COLUMN_PRODUCT_PRICE);
            if (price != null && price < 0)
                throw new IllegalArgumentException("Product requires a valid price");
        }

        if (contentValues.containsKey(SupplementEntry.COLUMN_PRODUCT_QUANTITY)) {
            Integer quantity = contentValues.getAsInteger(SupplementEntry.COLUMN_PRODUCT_QUANTITY);
            if (quantity == null || !SupplementEntry.isValidQuantity(quantity))
                throw new IllegalArgumentException("Product requires valid quantity");
        }

        if (contentValues.containsKey(SupplementEntry.COLUMN_SUPPLIER_NAME))
            if (contentValues.getAsString(SupplementEntry.COLUMN_SUPPLIER_NAME) == null)
                throw new IllegalArgumentException("Supplier requires a name");

        if (contentValues.containsKey(SupplementEntry.COLUMN_SUPPLIER_TELEPHONE))
            if (contentValues.getAsString(SupplementEntry.COLUMN_SUPPLIER_TELEPHONE) == null)
                throw new IllegalArgumentException("Supplier requires a telephone");

        // If there are no values to update exit without changes
        if (contentValues.size() == 0)
            return 0;

        SQLiteDatabase database = suppDbHelper.getWritableDatabase();

        int rowsUpdated = database.update(SupplementEntry.TABLE_NAME, contentValues, selection, selectionArgs);

        // Notify listeners if any updates took place
        if (rowsUpdated != 0)
            if (getContext() != null)
                getContext().getContentResolver().notifyChange(uri, null);

        return rowsUpdated;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = suppDbHelper.getWritableDatabase();
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case SUPPLEMENTS:
                rowsDeleted = database.delete(SupplementContract.SupplementEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case SUPPLEMENT_ID:
                selection = SupplementEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(SupplementEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // Notify listeners if any deletion took place
        if (rowsDeleted != 0)
            if (getContext() != null)
                getContext().getContentResolver().notifyChange(uri, null);

        return rowsDeleted;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case SUPPLEMENTS:
                return SupplementEntry.CONTENT_LIST_TYPE;
            case SUPPLEMENT_ID:
                return SupplementEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}