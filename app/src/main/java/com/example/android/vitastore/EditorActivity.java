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

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.vitastore.data.SupplementContract.SupplementEntry;

/**
 * Activity used for editing or adding new supplements
 */

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int CURRENT_SUPPLEMENT_LOADER = 0;
    private Uri currentSupplementUri;
    private EditText productNameText;
    private EditText productPriceText;
    private Spinner quantitySpinner;
    private int productQuantity = SupplementEntry.MINIMUM_QUANTITY;
    private EditText supplierNameText;
    private EditText supplierTelephoneText;
    private boolean productHasChanged = false;

    private View.OnTouchListener productTouchListener = new View.OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            productHasChanged = true;
            return false;
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        currentSupplementUri = intent.getData();

        if (currentSupplementUri == null) {
            setTitle(R.string.editor_activity_title_new_product);
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_product));
            getSupportLoaderManager().initLoader(CURRENT_SUPPLEMENT_LOADER, null, this);
        }

        productNameText = (EditText) findViewById(R.id.edit_product_name);
        productPriceText = (EditText) findViewById(R.id.edit_product_price);
        quantitySpinner = (Spinner) findViewById(R.id.spinner_quantity);
        supplierNameText = (EditText) findViewById(R.id.edit_supplier_name);
        supplierTelephoneText = (EditText) findViewById(R.id.edit_supplier_telephone);
        Button callSupplier = (Button) findViewById(R.id.button_contact);
        ImageButton decreaseQuantity = (ImageButton) findViewById(R.id.button_minus);
        ImageButton increaseQuantity = (ImageButton) findViewById(R.id.button_plus);

        productNameText.setOnTouchListener(productTouchListener);
        productPriceText.setOnTouchListener(productTouchListener);
        quantitySpinner.setOnTouchListener(productTouchListener);
        supplierNameText.setOnTouchListener(productTouchListener);
        supplierTelephoneText.setOnTouchListener(productTouchListener);
        callSupplier.setOnTouchListener(productTouchListener);
        decreaseQuantity.setOnTouchListener(productTouchListener);
        increaseQuantity.setOnTouchListener(productTouchListener);

        setupSpinner();

        callSupplier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent phoneIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + supplierTelephoneText.getText().toString().trim()));
                startActivity(phoneIntent);
            }
        });

        decreaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (quantitySpinner.getSelectedItemPosition() < quantitySpinner.getAdapter().getCount() &&
                        quantitySpinner.getSelectedItemPosition() > SupplementEntry.MINIMUM_QUANTITY) {
                    quantitySpinner.setSelection(quantitySpinner.getSelectedItemPosition() - 1);
                    productQuantity -= 1;
                } else
                    Toast.makeText(getApplicationContext(), getString(R.string.editor_min_quantity_reached), Toast.LENGTH_SHORT).show();

            }
        });

        increaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (quantitySpinner.getSelectedItemPosition() < quantitySpinner.getAdapter().getCount() &&
                        quantitySpinner.getSelectedItemPosition() < SupplementEntry.MAXIMUM_QUANTITY) {
                    quantitySpinner.setSelection(quantitySpinner.getSelectedItemPosition() + 1);
                    productQuantity += 1;
                } else
                    Toast.makeText(getApplicationContext(), getString(R.string.editor_max_quantity_reached), Toast.LENGTH_SHORT).show();

            }
        });
    }

    /**
     * Helper method used to set up the Spinner which allows the user to select supplement quantity
     */
    private void setupSpinner() {
        ArrayAdapter quantitySpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.array_quantities,
                android.R.layout.simple_spinner_item);
        quantitySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        quantitySpinner.setAdapter(quantitySpinnerAdapter);

        quantitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection))
                    productQuantity = position;
                else
                    productQuantity = SupplementEntry.MINIMUM_QUANTITY;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                productQuantity = SupplementEntry.MINIMUM_QUANTITY;
            }
        });
    }

    private void saveProduct() {
        String productNameString = productNameText.getText().toString().trim();
        String productPriceString = productPriceText.getText().toString().trim();
        String supplierNameString = supplierNameText.getText().toString().trim();
        String supplierTelephoneString = supplierTelephoneText.getText().toString().trim();

        // If any text fields are missing prompt the user to input them
        if (TextUtils.isEmpty(productNameString) || TextUtils.isEmpty(supplierNameString) || TextUtils.isEmpty(supplierTelephoneString)) {
            Toast.makeText(this, getString(R.string.editor_full_details_needed), Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(SupplementEntry.COLUMN_PRODUCT_NAME, productNameString);
        values.put(SupplementEntry.COLUMN_SUPPLIER_NAME, supplierNameString);
        values.put(SupplementEntry.COLUMN_SUPPLIER_TELEPHONE, supplierTelephoneString);
        values.put(SupplementEntry.COLUMN_PRODUCT_QUANTITY, productQuantity);

        // If the price is not provided, use the default value
        double price = SupplementEntry.DEFAULT_PRICE;
        if (!TextUtils.isEmpty(productPriceString))
            price = Double.parseDouble(productPriceString);
        values.put(SupplementEntry.COLUMN_PRODUCT_PRICE, price);

        // If this is a new product, insert it into the database. If not, update its details
        if (currentSupplementUri == null) {
            Uri newUri = getContentResolver().insert(SupplementEntry.CONTENT_URI, values);
            if (newUri == null)
                Toast.makeText(this, getString(R.string.editor_insert_product_failed), Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, productNameString + " " + getString(R.string.editor_insert_product_successful),
                        Toast.LENGTH_SHORT).show();

        } else {
            int rowsAffected = getContentResolver().update(currentSupplementUri, values, null, null);
            if (rowsAffected == 0)
                Toast.makeText(this, getString(R.string.editor_update_product_failed) + " " + productNameString, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, productNameString + " " + getString(R.string.editor_update_product_successful),
                        Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new supplement, hide the "Delete" menu item
        if (currentSupplementUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    /**
     * Method which sets actions for the options menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveProduct();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!productHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        if (!productHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };

        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {SupplementEntry._ID, SupplementEntry.COLUMN_PRODUCT_NAME, SupplementEntry.COLUMN_PRODUCT_PRICE,
                SupplementEntry.COLUMN_PRODUCT_QUANTITY, SupplementEntry.COLUMN_SUPPLIER_NAME, SupplementEntry.COLUMN_SUPPLIER_TELEPHONE};

        return new CursorLoader(this, currentSupplementUri, projection, null, null, null);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Return early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1)
            return;

        if (cursor.moveToFirst()) {
            productNameText.setText(cursor.getString(cursor.getColumnIndex(SupplementEntry.COLUMN_PRODUCT_NAME)));
            productPriceText.setText(Double.toString(cursor.getDouble(cursor.getColumnIndex(SupplementEntry.COLUMN_PRODUCT_PRICE))));
            supplierNameText.setText(cursor.getString(cursor.getColumnIndex(SupplementEntry.COLUMN_SUPPLIER_NAME)));
            supplierTelephoneText.setText(cursor.getString(cursor.getColumnIndex(SupplementEntry.COLUMN_SUPPLIER_TELEPHONE)));
            quantitySpinner.setSelection(cursor.getInt(cursor.getColumnIndex(SupplementEntry.COLUMN_PRODUCT_QUANTITY)));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        productNameText.setText("");
        productPriceText.setText("");
        supplierNameText.setText("");
        supplierTelephoneText.setText("");
        quantitySpinner.setSelection(0);
    }

    /**
     * Show a dialog warning the user that changes will not be saved if they leave the editor
     *
     * @param discardButtonClickListener is the click listener for what to do when the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null)
                    dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null)
                    dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteProduct() {
        if (currentSupplementUri != null) {
            int rowsDeleted = getContentResolver().delete(currentSupplementUri, null, null);
            if (rowsDeleted == 0)
                Toast.makeText(this, getString(R.string.editor_delete_product_failed) + " " + productNameText.getText(),
                        Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, productNameText.getText() + " " + getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}
