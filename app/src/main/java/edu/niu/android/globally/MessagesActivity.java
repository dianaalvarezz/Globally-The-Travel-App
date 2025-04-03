package edu.niu.android.globally;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class MessagesActivity extends AppCompatActivity {
    private RecyclerView messagesRecyclerView;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;
    private EditText messageEditText;
    private Button sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        // Enable back button in the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize RecyclerView and other views
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);

        // Set up RecyclerView with a vertical layout
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messageList);
        messagesRecyclerView.setAdapter(messageAdapter);

        // Set up send button listener
        sendButton.setOnClickListener(v -> sendMessage());

        // Load messages (we'll implement this part shortly)
        loadMessages();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();  // Go back to the home page
        return true;
    }

    private void loadMessages() {
        // For now, we'll simulate loading messages with hardcoded data
        messageList.add(new Message("User1", "User2", "Hello, how are you?", System.currentTimeMillis()));
        messageList.add(new Message("User2", "User1", "I'm good, thanks!", System.currentTimeMillis()));
        messageAdapter.notifyDataSetChanged();
    }

    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();

        if (messageText.isEmpty()) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
            return;
        }

        // You can replace these with real user IDs or data from your app
        String senderId = "User1";  // This would come from your authentication
        String receiverId = "User2";  // Same for receiver

        // Create the message object
        Message message = new Message(senderId, receiverId, messageText, System.currentTimeMillis());

        // Save the message to Firebase
        DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference("messages");
        String messageId = messagesRef.push().getKey();

        if (messageId != null) {
            messagesRef.child(messageId).setValue(message).addOnSuccessListener(aVoid -> {
                messageEditText.setText("");  // Clear the input field
                Toast.makeText(MessagesActivity.this, "Message sent", Toast.LENGTH_SHORT).show();
                messageList.add(message);
                messageAdapter.notifyDataSetChanged();
            }).addOnFailureListener(e -> {
                Toast.makeText(MessagesActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
            });
        }
    }
}
