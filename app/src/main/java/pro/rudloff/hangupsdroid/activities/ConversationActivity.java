package pro.rudloff.hangupsdroid.activities;

import android.os.Bundle;
import com.chaquo.python.PyObject;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessageInput.InputListener;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;
import com.stfalcon.chatkit.messages.MessagesListAdapter.OnLoadMoreListener;
import pro.rudloff.hangupsdroid.App;
import pro.rudloff.hangupsdroid.AvatarLoader;
import pro.rudloff.hangupsdroid.Conversation;
import pro.rudloff.hangupsdroid.Message;
import pro.rudloff.hangupsdroid.R;
import pro.rudloff.hangupsdroid.User;
import pro.rudloff.hangupsdroid.runnables.AddMessageRunnable;

/** Activity that lists messages in a specific conversation. */
public class ConversationActivity extends Activity implements OnLoadMoreListener, InputListener {

    /** ChatKit adapter used to inject the messages in the view. */
    private MessagesListAdapter<Message> messageAdapter;

    /** Current conversation. */
    private Conversation conversation;

    /**
     * Called when the activity is created.
     *
     * @param savedInstanceState Saved state of the activity if it has been previously killed by the
     *     OS.
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conversation);
        getSupportActionBar().setHomeButtonEnabled(true);

        App app = (App) getApplicationContext();

        conversation =
                new Conversation(
                        app.pythonApp.callAttr(
                                "get_conversation", getIntent().getStringExtra("conversationId")));
        setTitle(conversation.getDialogName());

        MessagesList messagesListView = findViewById(R.id.messagesList);
        messageAdapter =
                new MessagesListAdapter<Message>(
                        conversation.getSelfUser().getId(), new AvatarLoader());
        messagesListView.setAdapter(messageAdapter);
        messageAdapter.setLoadMoreListener(this);
        messageAdapter.addToEnd(conversation.getMessages(this), true);
        app.pythonApp.callAttr("add_conversation_observer", this, conversation.getId());
        app.pythonApp.callAttr(
                "add_messages", this, conversation.getId(), conversation.getFirstMessage().getId());

        MessageInput inputView = findViewById(R.id.input);
        inputView.setInputListener(this);
    }

    /**
     * Called by our Python module when new messages are available.
     *
     * @param messageList Python list of hangups ConversationEvent objects.
     */
    public void onNewMessages(PyObject messageList) {
        App app = (App) getApplicationContext();

        runOnUiThread(new AddMessageRunnable(this, messageAdapter, messageList));
        app.progressDialog.dismiss();
        app.pythonApp.callAttr("set_read", conversation.getId());
    }

    /**
     * Called when the user scrolls to the top of the view.
     *
     * @param page Next page to load
     * @param totalItemsCount Current messages counter
     */
    public void onLoadMore(int page, int totalItemsCount) {
        App app = (App) getApplicationContext();

        app.pythonApp.callAttr(
                "add_messages", this, conversation.getId(), conversation.getFirstMessage().getId());
    }

    /**
     * Called by our Python module when a new hangups ChatMessageEvent is received.
     *
     * @param event hangups ConversationEvent object
     */
    public void onChatMessageEvent(PyObject event) {
        App app = (App) getApplicationContext();

        runOnUiThread(
                new AddMessageRunnable(
                        this,
                        messageAdapter,
                        new Message(
                                event,
                                new User(
                                        app.pythonApp.callAttr(
                                                "get_user", event.get("user_id"))))));
        if (hasWindowFocus()) {
            app.pythonApp.callAttr("set_read", conversation.getId());
        }
    }

    /**
     * Called when the user submits a new message.
     *
     * @param text Message content
     * @return Is the message valid?
     */
    public boolean onSubmit(CharSequence text) {
        App app = (App) getApplicationContext();

        app.pythonApp.callAttr("send_message", conversation.getId(), text.toString());

        return true;
    }
}
