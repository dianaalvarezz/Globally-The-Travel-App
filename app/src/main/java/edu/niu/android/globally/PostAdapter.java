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
import android.widget.Button;
import android.util.Log;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private Context context;
    private List<Post> postList;
    private List<String> postKeys;
    private PostDeleteListener deleteListener;


    public interface PostDeleteListener
    {
        void onDelete(Post post, String firebaseKey);
    }




    public PostAdapter(Context context, List<Post> postList, List<String> postKeys, PostDeleteListener listener)
    {
        this.context = context;
        this.postList = postList;
        this.postKeys = postKeys;
        this.deleteListener = listener;
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

        holder.deleteButton.setOnClickListener(v ->
        {
            Log.d("DELETE_BTN", "Delete button clicked at position " + position);

            if (deleteListener != null)
            {
                Log.d("DELETE_BTN", "Calling deleteListener.onDelete()");
                deleteListener.onDelete(post, postKeys.get(position));
            }
            else
            {
                Log.e("DELETE_BTN", "deleteListener is null!");
            }
        });


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
        Button deleteButton;

        public PostViewHolder(View itemView)
        {
            super(itemView);
            imageView = itemView.findViewById(R.id.postImage);
            cityText = itemView.findViewById(R.id.postCity);
            countryText = itemView.findViewById(R.id.postCountry);
            captionText = itemView.findViewById(R.id.postCaption);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
