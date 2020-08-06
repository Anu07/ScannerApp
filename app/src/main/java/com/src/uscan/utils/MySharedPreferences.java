package com.src.uscan.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;


public class MySharedPreferences {
    private static final String SETTINGS_NAME = "default_settings";
    private static MySharedPreferences sSharedPrefs;
    private SharedPreferences mPref;
    private SharedPreferences.Editor mEditor;
    private boolean mBulkUpdate = false;

    public void setArrayList(ArrayList<String> listOfURIs) {
        doEdit();
        LinkedHashSet<String> set = new LinkedHashSet<>();
        set.addAll(listOfURIs);
        mEditor.putStringSet("URIs", set);
        mEditor.commit();
        doCommit();
    }

    public ArrayList<String> getArrayList(){
        if(mPref.getStringSet("URIs", null)!=null && mPref.getStringSet("URIs", null).size()>0){
            return new ArrayList<>(mPref.getStringSet("URIs", null));
        }
       return new ArrayList<>();
    }


    /**
     * Enum representing your setting names or key for your setting.
     */
    public enum Key {
        USER_ID,
        FULL_NAME,
        EMAIL,
        CFTOKEN,
        LOCATION,
        WALLET_BALANCE,
        PAYMENT_DONE,
        USERNAME,
        PKGDATE,
        CONTACT,
        QUIZ_DONE,
        FIRE_TOKEN,LATLNG
    }

    private MySharedPreferences(Context context)
    {
        mPref = context.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE);
    }


    public static
    MySharedPreferences getInstance(Context context)
    {
        if (sSharedPrefs == null) {
            sSharedPrefs = new MySharedPreferences(context.getApplicationContext());
        }
        return sSharedPrefs;
    }

    public static
    MySharedPreferences getInstance()
    {
        if (sSharedPrefs != null) {
            return sSharedPrefs;
        }

        //Option 1:
        throw new IllegalArgumentException ("Should use getInstance(Context) at least once before using this method.");

        //Option 2:
        // Alternatively, you can create a new instance here
        // with something like this:
        // getInstance(MyCustomApplication.getAppContext());
    }

    public void put(Key key, String
    val )
    {
        doEdit();
        mEditor.putString(key.name(), val);
        doCommit();
    }

    public void put(Key key, int
    val )
    {
        doEdit();
        mEditor.putInt(key.name(), val);
        doCommit();
    }

    public void put(Key key, boolean
    val )
    {
        doEdit();
        mEditor.putBoolean(key.name(), val);
        doCommit();
    }

    public void put(Key key, float
    val )
    {
        doEdit();
        mEditor.putFloat(key.name(), val);
        doCommit();
    }

    /**
     * Convenience method for storing doubles.
     * <p/>
     * There may be instances where the accuracy of a double is desired.
     * SharedPreferences does not handle doubles so they have to
     * cast to and from String.
     *
     * @param key The enum of the preference to store.
     * @param val The new value for the preference.
     */
    public void put(Key key, double
    val )
    {
        doEdit();
        mEditor.putString(key.name(), String.valueOf(val));
        doCommit();
    }

    public void put(Key key, long
    val )
    {
        doEdit();
        mEditor.putLong(key.name(), val);
        doCommit();
    }

    public String getString(Key key, String defaultValue)
    {
        return mPref.getString(key.name(), defaultValue);
    }

    public String getString(Key key)
    {
        return mPref.getString(key.name(), null);
    }

    public int getInt(Key key)
    {
        return mPref.getInt(key.name(), 0);
    }

    public int getInt(Key key, int defaultValue)
    {
        return mPref.getInt(key.name(), defaultValue);
    }

    public long getLong(Key key)
    {
        return mPref.getLong(key.name(), 0);
    }

    public long getLong(Key key, long defaultValue)
    {
        return mPref.getLong(key.name(), defaultValue);
    }

    public float getFloat(Key key)
    {
        return mPref.getFloat(key.name(), 0);
    }

    public float getFloat(Key key, float defaultValue)
    {
        return mPref.getFloat(key.name(), defaultValue);
    }

    /**
     * Convenience method for retrieving doubles.
     * <p/>
     * There may be instances where the accuracy of a double is desired.
     * SharedPreferences does not handle doubles so they have to
     * cast to and from String.
     *
     * @param key The enum of the preference to fetch.
     */
    public double getDouble(Key key)
    {
        return getDouble(key, 0);
    }

    /**
     * Convenience method for retrieving doubles.
     * <p/>
     * There may be instances where the accuracy of a double is desired.
     * SharedPreferences does not handle doubles so they have to
     * cast to and from String.
     *
     * @param key The enum of the preference to fetch.
     */
    public double getDouble(Key key, double defaultValue)
    {
        try {
            return Double.valueOf(mPref.getString(key.name(), String.valueOf(defaultValue)));
        } catch (NumberFormatException nfe) {
            return defaultValue;
        }
    }

    public boolean getBoolean(Key key, boolean defaultValue)
    {
        return mPref.getBoolean(key.name(), defaultValue);
    }

    public boolean getBoolean(Key key)
    {
        return mPref.getBoolean(key.name(), false);
    }

    /**
     * Remove keys from SharedPreferences.
     *
     * @param keys The enum of the key(s) to be removed.
     */
    public void remove(Key... keys)
    {
        doEdit();
        for (Key key : keys) {
        mEditor.remove(key.name());
    }
        doCommit();
    }

    /**
     * Remove all keys from SharedPreferences.
     */
    public void clear()
    {
        doEdit();
        mEditor.clear();
        doCommit();
    }

    public void edit()
    {
        mBulkUpdate = true;
        mEditor = mPref.edit();
    }

    public void commit()
    {
        mBulkUpdate = false;
        mEditor.commit();
        mEditor = null;
    }

    private void doEdit()
    {
        if (!mBulkUpdate && mEditor == null) {
            mEditor = mPref.edit();
        }
    }

    private void doCommit()
    {
        if (!mBulkUpdate && mEditor != null) {
            mEditor.commit();
            mEditor = null;
        }
    }

}

