package com.example.easyvote;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class CustomWaitingDialogBox {

    private AlertDialog dialog;
    private Context context;

    public CustomWaitingDialogBox(Context context) {
        this.context = context;
    }

    public void showDialog(String massage, boolean cancellable) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.custom_waiting_dialog_box, null);
        builder.setView(dialogView);
        builder.setCancelable(cancellable); // Prevent dialog from being canceled

        // Move this line here to set the background color after creating the dialog instance
        dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView messageTextView = dialogView.findViewById(R.id.message);
        messageTextView.setText(massage);
        ProgressBar progressBar = dialogView.findViewById(R.id.progressBar);

        dialog.show();
    }

    public void hideDialog() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }
}
