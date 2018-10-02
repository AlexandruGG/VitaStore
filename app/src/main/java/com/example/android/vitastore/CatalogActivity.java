/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.vitastore;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.vitastore.data.SupplementContract.SupplementEntry;


public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int PRODUCT_LOADER = 0;
    SupplementCursorAdapter suppCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        ListView productListView = (ListView) findViewById(R.id.list);
        productListView.setEmptyView(findViewById(R.id.empty_view));

        suppCursorAdapter = new SupplementCursorAdapter(this, null);
        productListView.setAdapter(suppCursorAdapter);

        productListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                intent.setData(ContentUris.withAppendedId(SupplementEntry.CONTENT_URI, id));
                startActivity(intent);
            }
        });

        getLoaderManager().initLoader(PRODUCT_LOADER, null, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    /**
     * Method used to insert a dummy product into the database
     */
    private void insertProduct() {
        ContentValues values = new ContentValues();

        values.put(SupplementEntry.COLUMN_PRODUCT_NAME, "Vitamin C");
        values.put(SupplementEntry.COLUMN_PRODUCT_PRICE, 5.65);
        values.put(SupplementEntry.COLUMN_PRODUCT_QUANTITY, 3);
        values.put(SupplementEntry.COLUMN_SUPPLIER_NAME, "Boots UK");
        values.put(SupplementEntry.COLUMN_SUPPLIER_TELEPHONE, "02087678292");

        getContentResolver().insert(SupplementEntry.CONTENT_URI, values);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_insert_dummy_product:
                insertProduct();
                return true;
            case R.id.action_delete_all_products:
                deleteAllProducts();
                Toast.makeText(this, R.string.delete_all_products, Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {SupplementEntry._ID, SupplementEntry.COLUMN_PRODUCT_NAME, SupplementEntry.COLUMN_PRODUCT_PRICE, SupplementEntry
                .COLUMN_PRODUCT_QUANTITY};
        return new CursorLoader(this, SupplementEntry.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        suppCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        suppCursorAdapter.swapCursor(null);
    }

    /**
     * Helper method to delete all products from the database
     */
    private void deleteAllProducts() {
        int rowsDeleted = getContentResolver().delete(SupplementEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from supplement database");
    }
}
