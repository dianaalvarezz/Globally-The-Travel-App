package edu.niu.android.globally;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class FriendsPage extends Fragment
{
    private RecyclerView friendsRecyclerView; // Displays list of posts from friends
    private ArrayList<Post> friendsPosts = new ArrayList<>(); // Holds the posts from firebase
    private PostAdapter friendsAdapter; // Binds the data friends posts to the RecyclerView




    /*
    Public constructor
     */
    public FriendsPage()
    {
        // Required empty public constructor
    }



    /*
    Called when the fragments view is being created
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflated the fragment_friendspage.xml into rootView
        View rootView = inflater.inflate(R.layout.fragment_friendspage, container, false);

        // Finds RecyclerView defined in the layout by ID
        friendsRecyclerView = rootView.findViewById(R.id.friendsPostsRecyclerView);

        // Sets layout manager for recyclerview
        friendsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initializes the PostAdapter
        friendsAdapter = new PostAdapter(getContext(), friendsPosts, new ArrayList<>(), null);

        // Sets the FriendsAdapter as the adapter fro the RecyclerView
        friendsRecyclerView.setAdapter(friendsAdapter);

        //Calls loadfriendsposts that fetches posts from Firebase
        loadFriendsPosts();

        // Returns root view of the fragment
        return rootView;
    }



    /*
    Loads the posts from Firebase
     */
    private void loadFriendsPosts()
    {
        // References the posts node in Firebase's Realtime Database
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("posts");

        postsRef.get().addOnSuccessListener(dataSnapshot -> {
            // So no old data remains before loading new posts
            friendsPosts.clear();

            List<DataSnapshot> reversed = new ArrayList<>();

            // Loops through the children of the data snapshot
            // Gets each posts
            for (DataSnapshot snapshot : dataSnapshot.getChildren())
            {
                // Reverses order so newest posts appear on top of feed
                reversed.add(0, snapshot);
            }

            // Loops through the reversed list of posts and converts each DataSnapSHot into a Post object
            for (DataSnapshot snapshot : reversed)
            {
                Post post = snapshot.getValue(Post.class);

                if (post != null)
                {
                    friendsPosts.add(post);
                }

            }

            // Updates the RecyclerView
            friendsAdapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> {
            // Shows message incase fails to upload post
            Toast.makeText(getContext(), "Failed to load post", Toast.LENGTH_SHORT).show();
        });
    }
}
