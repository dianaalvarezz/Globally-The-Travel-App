package edu.niu.android.globally;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private Context context;
    private List<Post> postList;

    public PostAdapter(Context context, List<Post> postList)
    {
        this.context = context;
        this.postList = postList;
    }

    @Override
    public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PostViewHolder holder, int position)
    {
        Post post = postList.get(position);
        holder.cityText.setText(post.city);
        holder.countryText.setText(post.country);
        holder.captionText.setText(post.caption);
        Glide.with(context).load(post.imageUrl).into(holder.imageView);
    }

    @Override
    public int getItemCount()
    {
        return postList.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder
    {
        ImageView imageView;
        TextView cityText, countryText, captionText;

        public PostViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.postImage);
            cityText = itemView.findViewById(R.id.postCity);
            countryText = itemView.findViewById(R.id.postCountry);
            captionText = itemView.findViewById(R.id.postCaption);
        }
    }
}
