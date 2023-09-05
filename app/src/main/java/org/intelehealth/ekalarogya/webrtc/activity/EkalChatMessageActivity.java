package org.intelehealth.ekalarogya.webrtc.activity;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;

import org.intelehealth.ekalarogya.R;
import org.intelehealth.ekalarogya.databinding.ActivityChatEkalBinding;
import org.intelehealth.ekalarogya.utilities.NotificationUtils;
import org.intelehealth.klivekit.chat.model.ChatMessage;
import org.intelehealth.klivekit.chat.model.MessageStatus;
import org.intelehealth.klivekit.chat.model.MessageType;
import org.intelehealth.klivekit.chat.ui.activity.ChatActivity;
import org.intelehealth.klivekit.chat.ui.activity.CoreChatActivity;
import org.intelehealth.klivekit.model.RtcArgs;

import java.util.List;

/**
 * Created by Vaghela Mithun R. on 25-08-2023 - 16:43.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public class EkalChatMessageActivity extends CoreChatActivity {
    private ActivityChatEkalBinding binding;

    public static void startEkalChatActivity(Context context, RtcArgs args) {
        Intent intent = new Intent(context, EkalChatMessageActivity.class);
        context.startActivity(CoreChatActivity.args(intent, args));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatEkalBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.chatContent.btnSendMessage.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        hideSoftKeyboard();
        if (args.getDoctorId().isEmpty()) {
            Toast.makeText(this, getString(org.intelehealth.klivekit.R.string.please_wait_for_doctor), Toast.LENGTH_SHORT).show();
            return;
        }
        String message = binding.chatContent.etMessageInput.getText().toString().trim();
        if (!message.isEmpty()) {
            postMessages(message);
        } else {
            Toast.makeText(this, getString(org.intelehealth.klivekit.R.string.empty_message_txt), Toast.LENGTH_SHORT).show();
        }
    }

    private void postMessages(String message) {
        if (args.getDoctorId() == null || TextUtils.isEmpty(args.getDoctorId())) {
            Toast.makeText(this, getString(org.intelehealth.klivekit.R.string.please_wait_for_doctor), Toast.LENGTH_LONG).show();
            return;
        }

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setMessage(message);
        chatMessage.setSenderId(args.getNurseId());
        chatMessage.setPatientId(args.getPatientId());
        chatMessage.setReceiverId(args.getDoctorId());
        chatMessage.setVisitId(args.getVisitId());
        chatMessage.setRoomName(args.getPatientName());
        chatMessage.setReceiverName(args.getDoctorName());
        chatMessage.setType(MessageType.TEXT.getValue());
        chatMessage.setMessageStatus(MessageStatus.SENDING.getValue());
        sendMessage(chatMessage);
    }

    @Override
    public void onMessagesLoad(@NonNull List<ChatMessage> messages) {

    }

    @Override
    public void onMessageListEmpty() {

    }

    public void hideSoftKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
