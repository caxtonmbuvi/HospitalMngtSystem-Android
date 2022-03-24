package com.example.hospitalmngtapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.example.hospitalmngtapp.Adapter.NotificationAdapter;
import com.example.hospitalmngtapp.Model.Notification;
import com.example.hospitalmngtapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private Toolbar notifications_toolbar;
    private RecyclerView notifications_recycler_view;
    private NotificationAdapter notificationAdapter;
    private List<Notification> notificationList;
    private ImageView search_error_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        search_error_image = findViewById(R.id.search_error_image);

        notifications_toolbar = findViewById(R.id.notifications_toolbar);
        setSupportActionBar(notifications_toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        notifications_recycler_view = findViewById(R.id.notifications_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        notifications_recycler_view.setHasFixedSize(true);
        notifications_recycler_view.setLayoutManager(layoutManager);

        notificationList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(NotificationsActivity.this, notificationList);
        notifications_recycler_view.setAdapter(notificationAdapter);

        readNotifications();
    }

    private void readNotifications() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("notifications").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                notificationList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Notification notification = snapshot.getValue(Notification.class);
                    notificationList.add(notification);

                }

                Collections.reverse(notificationList);
                notificationAdapter.notifyDataSetChanged();

                if (notificationList.isEmpty()){
                    search_error_image.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}