package com.example.recipes_project.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipes_project.R;
import com.example.recipes_project.models.UploadedImage;

import java.util.ArrayList;

public class UploadedImagesAdapter extends RecyclerView.Adapter<UploadedImagesAdapter.ViewHolder> {
    private Context context;
    private ArrayList<UploadedImage> imageList;
    private OnImageClickListener listener;

    public UploadedImagesAdapter(Context context, ArrayList<UploadedImage> imageList, OnImageClickListener listener) {
        this.context = context;
        this.imageList = imageList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_uploaded_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UploadedImage uploadedImage = imageList.get(position);

        // Decode the Base64 string to Bitmap
        if (uploadedImage.getImageBase64() != null) {
            byte[] decodedString = Base64.decode(uploadedImage.getImageBase64(), Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            holder.imageView.setImageBitmap(decodedByte);
        }

        holder.itemView.setOnClickListener(v -> {
            // Show the image in a dialog when clicked
            showImageInDialog(uploadedImage);
        });

        // Handle the delete button
        holder.deleteButton.setOnClickListener(v -> listener.onDeleteClick(uploadedImage));
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    // Function to show image in a dialog
    private void showImageInDialog(UploadedImage uploadedImage) {
        // Decode the Base64 string to Bitmap
        byte[] decodedString = Base64.decode(uploadedImage.getImageBase64(), Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        // Create an AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_full_image, null);
        ImageView imageView = view.findViewById(R.id.dialogImageView);

        // Set the image in the dialog
        imageView.setImageBitmap(decodedByte);

        // Create the dialog and show it
        builder.setView(view)
                .setCancelable(true)  // Allow dismissing by tapping outside
                .create()
                .show();
    }

    // ViewHolder class
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageButton deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }

    // Interface for delete button click
    public interface OnImageClickListener {
        void onDeleteClick(UploadedImage image);
    }
}
