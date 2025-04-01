package edu.niu.android.globally;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {
    private final List<String> photoUrls;

    public PhotoAdapter(List<String> photoUrls) {
        this.photoUrls = photoUrls;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        // Make sure this matches your actual XML file name
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_photo_grid, parent, false); // Changed to match your XML
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        String url = photoUrls.get(position);
        Glide.with(holder.itemView.getContext())
                .load(url)
                .placeholder(android.R.drawable.ic_menu_gallery) // Default Android icon
                .error(android.R.drawable.ic_menu_report_image) // Default error icon
                .into(holder.imageView);
    }

    @Override
    public int getItemCount()
    {
        return photoUrls.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder
    {
        ImageView imageView;

        ViewHolder(@NonNull View itemView)
        {
            super(itemView);
            imageView = itemView.findViewById(R.id.photoImageView);
        }
    }
}