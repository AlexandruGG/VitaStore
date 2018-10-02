package com.example.android.vitastore;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.vitastore.data.SupplementContract.SupplementEntry;

/**
 * The adapter helps with displaying product data in the CatalogActivity and implements a functionality to sell products (reduce quantity)
 */
public class SupplementCursorAdapter extends CursorAdapter {

    SupplementCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        TextView nameTextView = (TextView) view.findViewById(R.id.text_product_name);
        TextView priceTextView = (TextView) view.findViewById(R.id.text_product_price);
        final TextView quantityTextView = (TextView) view.findViewById(R.id.text_product_quantity);
        Button sellButton = (Button) view.findViewById(R.id.button_sell);
        final int productQuantity = cursor.getInt(cursor.getColumnIndex(SupplementEntry.COLUMN_PRODUCT_QUANTITY));
        final Uri updateUri = ContentUris.withAppendedId(SupplementEntry.CONTENT_URI, cursor.getInt(cursor.getColumnIndex(SupplementEntry._ID)));

        nameTextView.setText(cursor.getString(cursor.getColumnIndex(SupplementEntry.COLUMN_PRODUCT_NAME)));
        priceTextView.setText(Double.toString(cursor.getDouble(cursor.getColumnIndex(SupplementEntry.COLUMN_PRODUCT_PRICE))));
        quantityTextView.setText(Integer.toString(cursor.getInt(cursor.getColumnIndex(SupplementEntry.COLUMN_PRODUCT_QUANTITY))));

        int pos = cursor.getPosition();
        sellButton.setTag(pos);

        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Integer cursorPosition = (Integer) view.getTag();
                cursor.moveToPosition(cursorPosition);

                if (productQuantity > 0) {
                    ContentValues values = new ContentValues();
                    int newQuantity = productQuantity - 1;

                    values.put(SupplementEntry.COLUMN_PRODUCT_QUANTITY, newQuantity);
                    context.getContentResolver().update(updateUri, values, null, null);

                    quantityTextView.setText(String.valueOf(newQuantity));

                    if (newQuantity == 0)
                        Toast.makeText(context, "No " + cursor.getString(cursor.getColumnIndex
                                (SupplementEntry.COLUMN_PRODUCT_NAME)) + " left in stock", Toast
                                .LENGTH_SHORT).show();
                    else
                        Toast.makeText(context, cursor.getString(cursor.getColumnIndex
                                (SupplementEntry.COLUMN_PRODUCT_NAME)) + " sold!", Toast
                                .LENGTH_SHORT).show();
                } else
                    Toast.makeText(context, "No " + cursor.getString(cursor.getColumnIndex
                            (SupplementEntry.COLUMN_PRODUCT_NAME)) + " left in stock", Toast
                            .LENGTH_SHORT).show();
            }
        });
    }
}