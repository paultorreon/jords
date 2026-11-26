package com.example.firejords;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText etField1, etField2;
    private Button btnSubmit, btnView;
    private ListView listEntries;

    private DatabaseReference entriesRef;
    private ArrayAdapter<String> adapter;
    private final ArrayList<String> rows = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etField1 = findViewById(R.id.etField1);
        etField2 = findViewById(R.id.etField2);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnView = findViewById(R.id.btnView);
        listEntries = findViewById(R.id.listEntries);

        entriesRef = FirebaseDatabase.getInstance().getReference("entries");

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, rows);
        listEntries.setAdapter(adapter);

        btnSubmit.setOnClickListener(v -> saveEntry());
        btnView.setOnClickListener(v -> loadEntries());
    }

    /** Validates both fields, then saves a new record to Firebase. */
    private void saveEntry() {
        String value1 = etField1.getText().toString().trim();
        String value2 = etField2.getText().toString().trim();

        if (TextUtils.isEmpty(value1)) {
            etField1.setError("Required");
            Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(value2)) {
            etField2.setError("Required");
            Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String key = entriesRef.push().getKey();
        if (key == null) {
            Toast.makeText(this, "Failed to save entry", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmit.setEnabled(false);

        entriesRef.child(key).setValue(new Entry(value1, value2))
                .addOnSuccessListener(unused -> {
                    btnSubmit.setEnabled(true);
                    etField1.setText("");
                    etField2.setText("");
                    etField1.requestFocus();
                    Toast.makeText(this, "Entry saved successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    btnSubmit.setEnabled(true);
                    Toast.makeText(this, "Failed to save: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    /** Reads all records once and shows each one as a row in the ListView. */
    private void loadEntries() {
        entriesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                rows.clear();

                for (DataSnapshot child : snapshot.getChildren()) {
                    Entry entry = child.getValue(Entry.class);
                    if (entry != null) {
                        rows.add(entry.getField1() + "  -  " + entry.getField2());
                    }
                }
                adapter.notifyDataSetChanged();

                if (rows.isEmpty()) {
                    Toast.makeText(MainActivity.this, "No records found",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this,
                        "Failed to load records: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
