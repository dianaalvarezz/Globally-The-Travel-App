package edu.niu.android.globally;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;
import edu.niu.android.globally.PostPage.Pin;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import java.util.List;
public class PinAdapter extends RecyclerView.Adapter<PinAdapter.PinViewHolder> {

    private Context context;
    private List<Pin> pinList;
    private PostPage postPage; // PostPage reference passed directly

    // Constructor with context, pinList, and PostPage reference
    public PinAdapter(Context context, List<Pin> pinList, PostPage postPage) {
        this.context = context;
        this.pinList = pinList;
        this.postPage = postPage; // Initialize postPage directly
    }

    // Remove a pin from the list and SharedPreferences
    public void removePin(int position)
    {
        Pin pin = pinList.get(position);

        // Remove from SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences("GlobellyPins", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String pinKey = pin.getLatitude() + "," + pin.getLongitude() + "," + pin.getCountry();
        editor.remove(pinKey);  // Remove the pin from SharedPreferences
        editor.apply();

        // Remove from list and notify adapter
        pinList.remove(position);
        notifyItemRemoved(position);

        // Show a toast to inform the user
        Toast.makeText(context, "Pin removed!", Toast.LENGTH_SHORT).show();

        // Update the map in PostPage
        // Inside removePin():
        // Inside removePin():
        if (postPage != null) {
            postPage.updateMap();
            // Force reload HomePage
        }
    }

    @NonNull
    @Override
    public PinViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.pin_item, parent, false);
        return new PinViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PinViewHolder holder, int position) {
        Pin pin = pinList.get(position);
        holder.countryTextView.setText(pin.getCountry());
        holder.locationTextView.setText("Lat: " + pin.getLatitude() + " Lng: " + pin.getLongitude());

        // Remove button logic
        // Inside onBindViewHolder():
        holder.removeButton.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition(); // ✅ Dynamic position
            if (currentPosition != RecyclerView.NO_POSITION) {
                removePin(currentPosition);
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return pinList.size();
    }

    public static class PinViewHolder extends RecyclerView.ViewHolder {
        TextView countryTextView, locationTextView;
        Button removeButton;

        public PinViewHolder(View itemView) {
            super(itemView);
            countryTextView = itemView.findViewById(R.id.countryTextView);
            locationTextView = itemView.findViewById(R.id.locationTextView);
            removeButton = itemView.findViewById(R.id.removeButton); // reference to remove button
        }
    }
}

