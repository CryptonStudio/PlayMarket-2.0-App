package com.blockchain.store.playmarket.utilities;

import android.support.v7.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {
    public BaseActivity() {
        LocaleUtils.updateConfig(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        LocaleUtils.addActivity(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocaleUtils.removeActivity(this);
    }
}