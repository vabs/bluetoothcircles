package hackthon.bluetoothcircles;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class Messages extends AppCompatActivity {

    ListView messagesList;
    ArrayList<String> messages;
    ArrayAdapter<String> messagesListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            messages = new ArrayList<String>();
            messages = extras.getStringArrayList("messages");
        }
        init();

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
    }

    public void init() {
        messagesList = (ListView)findViewById(R.id.messagesList);
        messagesListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, 0);
        messagesList.setAdapter(messagesListAdapter);
        if(messages != null)
            messagesListAdapter.addAll(messages);
    }

}
