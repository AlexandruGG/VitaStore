package com.example.android.vitastore.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class SupplementContract {

    private SupplementContract() {
    }

    public static final String CONTENT_AUTHORITY = "com.example.android.vitastore";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_SUPPLEMENTS = "supplements";

    public static final class SupplementEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_SUPPLEMENTS);

        /**
         * The MIME type of CONTENT_URI for a list of supplements
         */
        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SUPPLEMENTS;

        /**
         * The MIME type of the CONTENT_URI for a single supplement
         */
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SUPPLEMENTS;

        public final static String TABLE_NAME = "supplements";

        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_PRODUCT_NAME = "Name";
        public final static String COLUMN_PRODUCT_PRICE = "Price";
        public final static String COLUMN_PRODUCT_QUANTITY = "Quantity";
        public final static String COLUMN_SUPPLIER_NAME = "Supplier";
        public final static String COLUMN_SUPPLIER_TELEPHONE = "Telephone";

        public static final int MINIMUM_QUANTITY = 0;
        public static final int DEFAULT_PRICE = 1;
        public static final int MAXIMUM_QUANTITY = 10;


        public static boolean isValidQuantity(int quantity) {
            switch (quantity) {
                case 0:
                    return true;
                case 1:
                    return true;
                case 2:
                    return true;
                case 3:
                    return true;
                case 4:
                    return true;
                case 5:
                    return true;
                case 6:
                    return true;
                case 7:
                    return true;
                case 8:
                    return true;
                case 9:
                    return true;
                case 10:
                    return true;
                default:
                    return false;
            }
        }
    }
}
