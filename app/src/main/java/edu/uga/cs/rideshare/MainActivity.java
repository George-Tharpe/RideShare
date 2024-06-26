package edu.uga.cs.rideshare;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Display the SplashFragment initially
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new SplashFragment())
                .commit();
    }
}
