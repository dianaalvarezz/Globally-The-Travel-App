package edu.niu.android.globally;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import edu.niu.android.globally.Message;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private Context context;
    private List<Message> messages;

    // Constructor
    public MessageAdapter(Context context, List<Message> messages) {
        this.context = context;
        this.messages = messages;
    }

    // Create the ViewHolder for each item in the list
    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    // Bind data to the view
    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.senderTextView.setText(message.getSenderId());
        holder.messageTextView.setText(message.getMessageText());
        holder.timestampTextView.setText(String.valueOf(message.getTimestamp()));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    // ViewHolder to represent each message in the list
    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView senderTextView;
        TextView messageTextView;
        TextView timestampTextView;

        public MessageViewHolder(View itemView) {
            super(itemView);
            senderTextView = itemView.findViewById(R.id.senderTextView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
        }
    }
}
