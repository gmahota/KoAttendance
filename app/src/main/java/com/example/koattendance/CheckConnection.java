package com.example.koattendance;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

public class CheckConnection {

    Snackbar snackbar;
    byte network=0;

    public void run(Context context, View layout) {

        ConnectivityManager manager = (ConnectivityManager)
                context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = manager.getActiveNetworkInfo();

        if (null != activeNetwork) {

            if (network != 1){
                if (network != 0){
                    snackbar = Snackbar.make(layout, "A conexão foi recuperada", Snackbar.LENGTH_LONG);
                    snackbar.show();

                    network=1;
                }
            }

        }
        else {
            if (network != 2) {
                snackbar = Snackbar.make(layout, "OFFLINE", Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction("Ignorar", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snackbar.dismiss();
                    }
                })
                        .setActionTextColor(context.getResources().getColor(R.color.design_default_color_primary))
                        .show();

                network=2;
            }
        }
    }

}
