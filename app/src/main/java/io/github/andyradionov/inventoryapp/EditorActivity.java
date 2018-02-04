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
package io.github.andyradionov.inventoryapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity {

    public static int REQUEST_CODE_CREATE = 10;
    public static int REQUEST_CODE_EDIT = 11;
    public static int RESPONSE_CODE_TRUE = 20;
    public static int RESPONSE_CODE_DEL = 21;
    public static String EXTRA_REQUEST_CODE = "extra_request_code";
    public static String EXTRA_PRODUCT_ID = "extra_product_id";
    public static String EXTRA_PRODUCT_NAME = "extra_product_name";
    public static String EXTRA_PRODUCT_QUANTITY = "extra_product_quantity";

    private EditText mProductNameEnter;
    private EditText mProductQuantityEnter;
    private int mRequestCode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        mProductNameEnter = findViewById(R.id.et_name_enter);
        mProductQuantityEnter = findViewById(R.id.et_quantity_enter);

        mRequestCode = getIntent().getIntExtra(EXTRA_REQUEST_CODE, -1);
        if (mRequestCode == REQUEST_CODE_EDIT) {
            setUpViews();
        }
    }

    public void onSaveClick(View view) {
        Intent resultIntent = new Intent();

        String name = mProductNameEnter.getText().toString();
        if (TextUtils.isEmpty(name)) {
            showErrorDialog(getString(R.string.name_error_msg));
            return;
        }
        int quantity;
        try {
            quantity = Integer.parseInt(mProductQuantityEnter.getText().toString());
        } catch (NumberFormatException e) {
            showErrorDialog(getString(R.string.quantity_error_msg));
            return;
        }

        resultIntent.putExtra(EXTRA_PRODUCT_NAME, name);
        resultIntent.putExtra(EXTRA_PRODUCT_QUANTITY, quantity);

        if (mRequestCode == REQUEST_CODE_EDIT) {
            int id = getIntent().getIntExtra(EXTRA_PRODUCT_ID, -1);
            resultIntent.putExtra(EXTRA_PRODUCT_ID, id);
        }

        setResult(RESPONSE_CODE_TRUE, resultIntent);
        finish();
    }

    public void onDeleteClick(View view) {
        Intent resultIntent = new Intent();

        if (mRequestCode == REQUEST_CODE_EDIT) {
            int id = getIntent().getIntExtra(EXTRA_PRODUCT_ID, -1);
            resultIntent.putExtra(EXTRA_PRODUCT_ID, id);
        }

        setResult(RESPONSE_CODE_DEL, resultIntent);
        finish();
    }

    private void setUpViews() {
        Intent startIntent = getIntent();
        String name = startIntent.getStringExtra(EXTRA_PRODUCT_NAME);
        int quantity = startIntent.getIntExtra(EXTRA_PRODUCT_QUANTITY, -1);

        mProductNameEnter.setText(name);
        mProductQuantityEnter.setText(String.valueOf(quantity));
    }

    private void showErrorDialog(String message) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setTitle(R.string.error_dialog_title)
                .setCancelable(false)
                .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create()
                .show();
    }
}
