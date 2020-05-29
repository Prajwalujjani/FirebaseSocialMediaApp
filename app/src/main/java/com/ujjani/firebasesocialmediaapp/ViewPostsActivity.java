package com.ujjani.firebasesocialmediaapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ViewPostsActivity extends AppCompatActivity implements AdapterView.OnItemClickListener , AdapterView.OnItemLongClickListener {

    private ListView postsListView;
    private  ArrayList<String> userNames;
    private ArrayAdapter adapter;

    private FirebaseAuth firebaseAuth;

    private ImageView sentPostsImageView;
    private TextView txtDescription;

    private ArrayList<DataSnapshot> dataSnapshots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_posts);

        sentPostsImageView = findViewById(R.id.sentPostImageView);
        txtDescription = findViewById(R.id.txtDescription);

        firebaseAuth = FirebaseAuth.getInstance();

        postsListView = findViewById(R.id.postsListView);
        userNames = new ArrayList<>();
        adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,userNames);
        postsListView.setAdapter(adapter);

        dataSnapshots = new ArrayList<>();

        postsListView.setOnItemClickListener(this);
        postsListView.setOnItemLongClickListener(this);


        FirebaseDatabase.getInstance().getReference().child("my_users").child(firebaseAuth.getCurrentUser().getUid()).child("received_posts").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                //this array list is used to store and populate data in the listView of the user that sent you a post ex: fromWhom, Description etc
                dataSnapshots.add(dataSnapshot);

                String fromWhomUserName  = (String) dataSnapshot.child("fromWhom").getValue();

                userNames.add(fromWhomUserName);
                adapter.notifyDataSetChanged();


            }


           //when the child or object is deleted this method will be called
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {


            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                int i=0;
                for(DataSnapshot snapshot : dataSnapshots){
                    if(snapshot.getKey().equals(dataSnapshot.getKey())){
                        dataSnapshots.remove(i);
                        userNames.remove(i);

                    }
                    i++;
                }
                adapter.notifyDataSetChanged();
                sentPostsImageView.setImageResource(R.drawable.placeholder);
                txtDescription.setText("");

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


        DataSnapshot myDataSnapShot = dataSnapshots.get(position);

        String downloadLink = (String) myDataSnapShot.child("imageLink").getValue();


        Picasso.get().load(downloadLink).into(sentPostsImageView);

        txtDescription.setText((String) myDataSnapShot.child("des").getValue());

    }


    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {


        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle("Delete entry")
                .setMessage("Are you sure you want to delete this entry?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        //It is used to delete Images in the FireBaseStorage
                        FirebaseStorage.getInstance().getReference()
                                .child("my_images").child((String) dataSnapshots
                                .get(position).child("imageIdentifier").getValue())
                                .delete();

                        //It is used to remove Snapshots(information of the image)
                        FirebaseDatabase.getInstance().getReference()
                                .child("my_users").child(firebaseAuth.getCurrentUser()
                                .getUid()).child("received_posts")
                                .child(dataSnapshots.get(position).getKey()).removeValue();

                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

        return false;

                    }
}

