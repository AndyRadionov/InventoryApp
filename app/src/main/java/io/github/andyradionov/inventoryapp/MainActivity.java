package io.github.andyradionov.inventoryapp;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import io.github.andyradionov.inventoryapp.data.Product;
import io.github.andyradionov.inventoryapp.data.ProductContract;

import static io.github.andyradionov.inventoryapp.data.ProductContract.ProductEntry;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        ProductAdapter.OnProductItemClickListener {

    private final String TAG = MainActivity.class.getSimpleName();

    /*
     * The columns of data that we are interested in displaying within our MainActivity's list of
     * products data.
     */
    public static final String[] MAIN_PRODUCT_PROJECTION = {
            ProductEntry._ID,
            ProductEntry.COLUMN_PRODUCT_NAME,
            ProductEntry.COLUMN_PRODUCT_QUANTITY
    };

    public static final int INDEX_PRODUCT_ID = 0;
    public static final int INDEX_PRODUCT_NAME = 1;
    public static final int INDEX_PRODUCT_QUANTITY = 2;

    private static final int ID_PRODUCT_LOADER = 42;

    private RecyclerView mProductsContainer;
    private ProductAdapter mProductAdapter;
    private int mPosition = RecyclerView.NO_POSITION;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProductsContainer = findViewById(R.id.rv_container_products);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mProductsContainer.setLayoutManager(layoutManager);

        mProductAdapter = new ProductAdapter(this, this);
        mProductsContainer.setAdapter(mProductAdapter);

        getSupportLoaderManager().initLoader(ID_PRODUCT_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add_product) {
            Intent startEditor = new Intent(MainActivity.this, EditorActivity.class);
            startActivityForResult(startEditor, EditorActivity.REQUEST_CODE_CREATE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onProductSaleClick(Product product) {
        ContentValues values = new ContentValues();
        int newQuantity = product.getQuantity() - 1;

        if (newQuantity < 0) {
            return;
        }

        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, newQuantity);

        int rowsUpdated = getContentResolver().update(ProductContract.ProductEntry.CONTENT_URI, values,
                "_id = ?", new String[]{String.valueOf(product.getId())});

        if (rowsUpdated == 1) {
            getSupportLoaderManager().restartLoader(ID_PRODUCT_LOADER, null, this);
        }
    }

    @Override
    public void onProductEditClick(Product product) {
        Intent startEditProduct = new Intent(MainActivity.this, EditorActivity.class);
        startEditProduct.putExtra(EditorActivity.EXTRA_PRODUCT_ID, product.getId());
        startEditProduct.putExtra(EditorActivity.EXTRA_PRODUCT_NAME, product.getName());
        startEditProduct.putExtra(EditorActivity.EXTRA_PRODUCT_QUANTITY, product.getQuantity());

        startActivityForResult(startEditProduct, EditorActivity.REQUEST_CODE_EDIT);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        intent.putExtra(EditorActivity.EXTRA_REQUEST_CODE, requestCode);
        super.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!isCorrectResult(requestCode, resultCode)) {
            return;
        }

        int productId = data.getIntExtra(EditorActivity.EXTRA_PRODUCT_ID, -1);

        if (resultCode == EditorActivity.RESPONSE_CODE_DEL) {
            deleteProduct(productId);
        }

        String productName = data.getStringExtra(EditorActivity.EXTRA_PRODUCT_NAME);
        int productQuantity = data.getIntExtra(EditorActivity.EXTRA_PRODUCT_QUANTITY, -1);

        if (!isValidProduct(productName, productQuantity)) {
            return;
        }

        if (requestCode == EditorActivity.REQUEST_CODE_CREATE) {
            insertProduct(productName, productQuantity);
        } else {
            updateProduct(productId, productName, productQuantity);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        if (loaderId != ID_PRODUCT_LOADER) {
            throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }

        String sortOrder = ProductEntry._ID + " ASC";

        return new CursorLoader(this,
                ProductEntry.CONTENT_URI,
                MAIN_PRODUCT_PROJECTION,
                null, null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mProductAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mProductAdapter.swapCursor(null);
    }

    private boolean isValidProduct(String name, int quantity) {
        return !TextUtils.isEmpty(name) && quantity != -1;
    }

    private boolean isCorrectResult(int requestCode, int responseCode) {
        return (requestCode == EditorActivity.REQUEST_CODE_CREATE
                || requestCode == EditorActivity.REQUEST_CODE_EDIT)
                && (responseCode == EditorActivity.RESPONSE_CODE_TRUE
                || responseCode == EditorActivity.RESPONSE_CODE_DEL);
    }

    private void insertProduct(String name, int quantity) {
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, name);
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);

        Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);

        if (newUri != null) {
            getSupportLoaderManager().restartLoader(ID_PRODUCT_LOADER, null, this);
        }
    }

    private void updateProduct(int id, String name, int quantity) {
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, name);
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);

        int rowsUpdated = getContentResolver().update(ProductEntry.CONTENT_URI, values,
                "_id = ?", new String[]{String.valueOf(id)});

        if (rowsUpdated == 1) {
            getSupportLoaderManager().restartLoader(ID_PRODUCT_LOADER, null, this);
        }
    }

    private void deleteProduct(int id) {
        int rowsUpdated = getContentResolver().delete(ProductEntry.CONTENT_URI,
                "_id = ?", new String[]{String.valueOf(id)});

        if (rowsUpdated == 1) {
            getSupportLoaderManager().restartLoader(ID_PRODUCT_LOADER, null, this);
        }
    }
}
