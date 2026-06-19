package com.example.moblie_app.ui.diary;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moblie_app.R;

public class MealViewHolder extends RecyclerView.ViewHolder {

    TextView txtFoodName;
    TextView txtCalories;
    TextView txtMacro;
    ImageButton btnDelete;

    public MealViewHolder(@NonNull View itemView) {
        super(itemView);

        txtFoodName = itemView.findViewById(R.id.txtFoodName);
        txtCalories = itemView.findViewById(R.id.txtCalories);
        txtMacro = itemView.findViewById(R.id.txtMacro);
        btnDelete = itemView.findViewById(R.id.btnDelete);
    }
}