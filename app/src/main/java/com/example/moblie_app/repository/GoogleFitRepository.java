package com.example.moblie_app.repository;

import android.content.Context;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;

public class GoogleFitRepository {

    public interface StepCallback {
        void onSuccess(int steps);
        void onError(String message);
    }

    public void readTodaySteps(Context context, GoogleSignInAccount account, StepCallback callback) {
        Fitness.getHistoryClient(context, account)
                .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(dataSet -> callback.onSuccess(extractSteps(dataSet)))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    private int extractSteps(DataSet dataSet) {
        int steps = 0;
        for (DataPoint point : dataSet.getDataPoints()) {
            if (point.getDataType().getFields().contains(Field.FIELD_STEPS)) {
                steps += point.getValue(Field.FIELD_STEPS).asInt();
            }
        }
        return steps;
    }
}
