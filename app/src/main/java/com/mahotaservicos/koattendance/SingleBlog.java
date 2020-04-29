package com.mahotaservicos.koattendance;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class SingleBlog extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
