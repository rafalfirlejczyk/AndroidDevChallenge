/*
 * Copyright (c) 2016 Qualcomm Technologies, Inc.
 * All Rights Reserved.
 * Confidential and Proprietary - Qualcomm Technologies, Inc.
 */
package com.zebra.qti.snpe.IQclassifiers;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;


import com.qualcomm.qti.snpe.imageclassifiers.R;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            final FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.add(R.id.main_content, ModelCatalogueFragment.create());
            transaction.commit();
        }
    }

    public void displayModelOverview(final Model model) {
        final FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.main_content, ModelOverviewFragment.create(model));
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
