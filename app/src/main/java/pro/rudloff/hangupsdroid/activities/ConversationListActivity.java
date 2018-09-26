package pro.rudloff.hangupsdroid.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.chaquo.python.PyObject;
import com.stfalcon.chatkit.dialogs.DialogsList;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;
import pro.rudloff.hangupsdroid.App;
import pro.rudloff.hangupsdroid.AvatarLoader;
import pro.rudloff.hangupsdroid.Conversation;
import pro.rudloff.hangupsdroid.MessageDateFormatter;
import pro.rudloff.hangupsdroid.R;
import pro.rudloff.hangupsdroid.listeners.ConversationClickListener;
import pro.rudloff.hangupsdroid.runnables.AddConversationRunnable;
import pro.rudloff.hangupsdroid.runnables.ProgressDialogRunnable;

public class ConversationListActivity extends AppCompatActivity {

    private DialogsListAdapter<Conversation> conversationAdapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conversation_list);
        setTitle("HangupsDroid");

        App app = (App) getApplicationContext();

        DialogsList dialogsListView = findViewById(R.id.conversationList);
        conversationAdapter = new DialogsListAdapter<Conversation>(new AvatarLoader());
        conversationAdapter.setDatesFormatter(new MessageDateFormatter());
        conversationAdapter.setOnDialogClickListener(new ConversationClickListener());
        dialogsListView.setAdapter(conversationAdapter);

        runOnUiThread(new ProgressDialogRunnable(this, getString(R.string.conversations_dialog)));

        app.pythonApp.callAttr("addConversations", this);
    }

    public void addConversations(PyObject conversationList) {
        App app = (App) getApplicationContext();

        runOnUiThread(new AddConversationRunnable(conversationAdapter, conversationList));
        app.progressDialog.dismiss();
    }
}