package edu.uga.cs.rideshare;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RiderFragment extends Fragment {
    private User currentUser;
    private List<User> userList;
    private DatabaseReference mDatabase;
    private List<Ride> rideList;
    private String dateTimeString;

    public RiderFragment(User currentUser, List<User> userList, List<Ride> rideList) {
        // Required empty public constructor
        this.currentUser = currentUser;
        this.userList = userList;
        this.rideList = rideList;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_rider, container, false);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Find the start button
        Button homeButton = view.findViewById(R.id.home_button);
        Button dateButton = view.findViewById(R.id.date_time_button);
        Button postRequestButton = view.findViewById(R.id.post_button);

        // Set OnClickListener to go to DriverFragment when driverButton is clicked
        homeButton.setOnClickListener((View.OnClickListener) v -> {
            // Replace the HomeScreenFragment with the DriverFragment
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeScreenFragment(currentUser, userList, rideList))
                    .commit();
        });

        LinearLayout rLayout = view.findViewById(R.id.a_rides_layout);

        // Populate rList with rides that meet the criteria
        List<Ride> rList = getAvailableRides();

        // Display available rides
        for (Ride ride : rList) {
            // Create a TextView to display ride information
            TextView textView = new TextView(getContext());
            textView.setTextAppearance(getContext(), R.style.MyTextViewStyle);
            textView.setText("\n\nRide Date: " + ride.date + "\n" + "To: " + ride.goingTo + ", From: " + ride.from);

            Button acceptButton = new Button(getContext());
            acceptButton.setTextAppearance(getContext(), R.style.MyButtonStyle);
            acceptButton.setText("Accept Ride");

            acceptButton.setOnClickListener(v -> {
                // Update ride when accept button is clicked
                ride.riderAccepted = true;
                ride.rider = currentUser;
                // Update UI
                rLayout.removeView(acceptButton);
                rLayout.removeView(textView);
                Log.d("rideRef:, ", "here");
                // Update ride in the database
                DatabaseReference rideRef = mDatabase.child("rides").child(ride.getKey());
                Log.d("rideRef:, ", rideRef.toString());
                rideRef.child("riderAccepted").setValue(true);
                rideRef.child("rider").setValue(currentUser);

                // Show toast message
                Toast.makeText(getContext(), "Ride Accepted", Toast.LENGTH_SHORT).show();
            });

            // Add the TextView and Button to the layout
            rLayout.addView(textView);
            rLayout.addView(acceptButton);
        }

        // Date and Time Selection
        dateButton.setOnClickListener((View.OnClickListener) v -> {
            // Get current date
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            // Create date picker dialog
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view1, year1, monthOfYear, dayOfMonth) -> {
                // Get selected date
                Calendar timeCalendar = Calendar.getInstance();
                int hour = timeCalendar.get(Calendar.HOUR_OF_DAY);
                int minute = timeCalendar.get(Calendar.MINUTE);

                // Create time picker dialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), (view2, hourOfDay, minute1) -> {
                    // Handle selected date and time
                    Calendar selectedDateTime = Calendar.getInstance();
                    selectedDateTime.set(year1, monthOfYear, dayOfMonth, hourOfDay, minute1);

                    // Format the selected date and time
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.US);
                    dateTimeString = dateFormat.format(selectedDateTime.getTime());

                    // Display the selected date and time
                    Toast.makeText(getContext(), "Selected Date and Time: " + dateTimeString, Toast.LENGTH_LONG).show();
                }, hour, minute, false);

                // Show time picker dialog
                timePickerDialog.show();
            }, year, month, day);

            // Show date picker dialog
            datePickerDialog.show();
        });

        EditText startLocationInput = view.findViewById(R.id.from_text);
        EditText destinationInput = view.findViewById(R.id.to_text);

        // Post ride request
        postRequestButton.setOnClickListener((View.OnClickListener) v -> {
            String startLocation = startLocationInput.getText().toString();
            String destination = destinationInput.getText().toString();
            postRideRequest(dateTimeString, startLocation, destination);
        });

        return view;
    }

    private void postRideRequest(String date, String startLocation, String destination) {
        String key = mDatabase.child("rides").push().getKey();
        Log.d("key:", key);
        Ride ride = new Ride(key, date, destination, startLocation, null, currentUser);
        if (ride == null) {
            Log.e("Ride object", "Ride object is null");
            return;
        }

        DatabaseReference rideRef = mDatabase.child("rides").child(ride.getKey());
        rideRef.child("date").setValue(date);
        rideRef.child("destination").setValue(destination);
        rideRef.child("startLocation").setValue(startLocation);
        rideRef.child("rider").setValue(currentUser);
        rideRef.child("riderAccepted").setValue(true)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Ride Accepted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to accept ride: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private List<Ride> getAvailableRides() {
        List<Ride> availableRides = new ArrayList<>();
        for (Ride ride : rideList) {
            if (ride.driverAccepted && !ride.riderAccepted) {
                availableRides.add(ride);
            }
        }
        return availableRides;
    }
}
