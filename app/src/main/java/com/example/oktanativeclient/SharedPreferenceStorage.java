package com.example.oktanativeclient;

import android.content.Context;
import android.content.SharedPreferences;

import com.okta.oidc.storage.OktaStorage;

public class SharedPreferenceStorage implements OktaStorage {
    
    private static final String PREFS_NAME = "OktaStorage";
    private final SharedPreferences prefs;
    
    public SharedPreferenceStorage(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    @Override
    public void save(String key, String value) {
        prefs.edit().putString(key, value).apply();
    }
    
    @Override
    public String get(String key) {
        return prefs.getString(key, null);
    }
    
    @Override
    public void delete(String key) {
        prefs.edit().remove(key).apply();
    }
}
