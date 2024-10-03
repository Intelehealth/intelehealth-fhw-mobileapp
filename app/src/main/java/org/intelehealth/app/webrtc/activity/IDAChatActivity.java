//package org.intelehealth.app.webrtc.activity
//
//import static org.intelehealth.klivekit.call.utils.CallConstants.MAX_INT;
//
//import android.app.PendingIntent;
//import android.content.Context;
//import android.content.Intent;
//
//import androidx.appcompat.widget.Toolbar;
//import androidx.recyclerview.widget.LinearLayoutManager;
//
//import org.intelehealth.app.R;
//import org.intelehealth.app.database.dao.PatientsDAO;
//import org.intelehealth.app.database.dao.ProviderDAO;
//import org.intelehealth.app.models.dto.ProviderDTO;
//import org.intelehealth.app.utilities.exception.DAOException;
//import org.intelehealth.fcm.utils.NotificationHandler;
//import org.intelehealth.klivekit.chat.ui.activity.ChatActivity;
//import org.intelehealth.core.socket.model.RtcArgs;
//
//import java.util.Random;
//
///**
// * Created by Vaghela Mithun R. on 25-08-2023 - 16:43.
// * Email : mithun@intelehealth.org
// * Mob   : +919727206702
// **/
//public class IDAChatActivity extends ChatActivity {
//    public static void startChatActivity(Context context, RtcArgs args) {
//        Intent chatIntent = new Intent(context, IDAChatActivity.class);
//        context.startActivity(buildExtra(chatIntent, args, context));
//    }
//
//    public static PendingIntent getPendingIntent(Context context, RtcArgs args) {
//        Intent chatIntent = new Intent(context, IDAChatActivity.class);
//        chatIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        int requestCode = new Random().nextInt(MAX_INT);
//        return PendingIntent.getActivity(context, requestCode, buildExtra(chatIntent, args, context),
//                NotificationHandler.getPendingIntentFlag());
//    }
//
//    private static Intent buildExtra(Intent chatIntent, RtcArgs args, Context context) {
//        try {
//            String nurseName = new ProviderDAO().getProviderName(args.getNurseId(), ProviderDTO.Columns.PROVIDER_UUID.value);
//            chatIntent.putExtra("patientName", args.getPatientName());
//            chatIntent.putExtra("visitUuid", args.getVisitId());
//            chatIntent.putExtra("patientUuid", args.getPatientId());
//            chatIntent.putExtra("fromUuid", args.getNurseId()); // provider uuid
//            chatIntent.putExtra("isForVideo", false);
//            chatIntent.putExtra("toUuid", args.getDoctorUuid());
//            chatIntent.putExtra("hwName", nurseName);
//            chatIntent.putExtra("openMrsId", new PatientsDAO().getOpenmrsId(args.getPatientId()));
//        } catch (DAOException e) {
//            throw new RuntimeException(e);
//        }
//
//        return chatIntent;
//    }
//
//    @Override
//    protected int getContentResourceId() {
//        return R.layout.activity_chat_ekal;
//    }
//
//    @Override
//    protected void setupActionBar() {
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        super.setupActionBar();
//        toolbar.setNavigationOnClickListener(v -> finishAfterTransition());
//    }
//
//    @Override
//    protected void initiateView() {
//        mEmptyTextView = findViewById(R.id.empty_tv);
//        mMessageEditText = findViewById(R.id.etMessageInput);
//        mLoadingLinearLayout = findViewById(R.id.loading_layout);
//        mEmptyLinearLayout = findViewById(R.id.empty_view);
//        mRecyclerView = findViewById(R.id.rvConversation);
//        mLayoutManager = new LinearLayoutManager(IDAChatActivity.this, LinearLayoutManager.VERTICAL, true);
//        mRecyclerView.setLayoutManager(mLayoutManager);
//    }
//}
