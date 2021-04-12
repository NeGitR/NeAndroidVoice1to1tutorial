package com.ne.ne_android_voice_1to1_tutorial;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.Bundle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.netease.lava.nertc.sdk.NERtc;

import com.netease.lava.nertc.sdk.NERtcCallback;
import com.netease.lava.nertc.sdk.NERtcConstants;
import com.netease.lava.nertc.sdk.NERtcEx;
import com.netease.lava.nertc.sdk.NERtcParameters;
import com.netease.lava.nertc.sdk.audio.NERtcCreateAudioMixingOption;
import com.netease.lava.nertc.sdk.video.NERtcRemoteVideoStreamType;



import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements NERtcCallback  {
    private static String LOG_TAG = "NeTRC-VDemo";
    private static final int PERMISSION_REQUEST_CODE = 100;
    private EditText mUserIDEt;
    private EditText mRoomIDEt;
    private Button mJoinBtn;

    private NERtc mRtcEngine; // Tutorial Step 1
    private NERtcEx mRtcEngineX; // Tutorial Step 1

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        requestPermissionsIfNeeded();
        initializeNeRtcEngine();
        mUserIDEt.setText(generateRandomUserID());
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mUserIDEt = findViewById(R.id.et_user_id);
        mRoomIDEt = findViewById(R.id.et_room_id);
        mJoinBtn = findViewById(R.id.btn_join);
        mJoinBtn.setOnClickListener(v -> {
            String userIDText = mUserIDEt.getText() != null ? mUserIDEt.getText().toString() : "";
            if (TextUtils.isEmpty(userIDText)) {
                Toast.makeText(this, "Please input userID", Toast.LENGTH_SHORT).show();
                return;
            }
            String roomID = mRoomIDEt.getText() != null ? mRoomIDEt.getText().toString() : "";
            if (TextUtils.isEmpty(roomID)) {
                Toast.makeText(this, "lease input roomID", Toast.LENGTH_SHORT).show();
                return;
            }

            joinChannel();               // Tutorial Step 2
            hideSoftKeyboard();
        });
    }

    private void requestPermissionsIfNeeded() {
        final List<String> missedPermissions = NERtc.checkPermission(this);
        if (missedPermissions.size() > 0) {
            ActivityCompat.requestPermissions(this, missedPermissions.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        }
    }

    private void initializeNeRtcEngine() { // step 1
        try {
            NERtcParameters parameters = new NERtcParameters();
            NERtc.getInstance().setParameters(parameters); //先设置参数，后初始化
            NERtc.getInstance().init(getApplicationContext(), getString(R.string.app_key), this, null);
            mRtcEngine = NERtc.getInstance();
            mRtcEngineX = NERtcEx.getInstance();
        } catch (Exception e) {
            Toast.makeText(this, "SDK Init Failed !", Toast.LENGTH_LONG).show();
            Log.e(LOG_TAG, Log.getStackTraceString(e));
            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
    }
    // Tutorial Step 2
    private void joinChannel() {

        // Sets the channel profile of the Agora RtcEngine.
        // CHANNEL_PROFILE_COMMUNICATION(0): (Default) The Communication profile. Use this profile in one-on-one calls or group calls, where all users can talk freely.
        // CHANNEL_PROFILE_LIVE_BROADCASTING(1): The Live-Broadcast profile. Users in a live-broadcast channel have a role as either broadcaster or audience. A broadcaster can both send and receive streams; an audience can only receive streams.

        NERtcEx.getInstance().setChannelProfile(NERtcConstants.RTCChannelProfile.LIVE_BROADCASTING);
        NERtc.getInstance().setAudioProfile(NERtcConstants.AudioProfile.STANDARD,NERtcConstants.AudioScenario.CHATROOM);
        NERtcEx.getInstance().setClientRole(NERtcConstants.UserRole.CLIENT_ROLE_BROADCASTER);

//      mRtcEngine.setParameters("{\"che.audio.start_debug_recording\":\"NoName\"}");

        mRtcEngine.joinChannel(null, mRoomIDEt.getText().toString(), Long.valueOf(mUserIDEt.getText().toString())); // if you do not specify the uid, we will generate the uid for you

    }

    // Tutorial Step 7
    public void onLocalAudioMuteClicked(View view) {
        ImageView iv = (ImageView) view;
        if (iv.isSelected()) {
            iv.setSelected(false);
            iv.clearColorFilter();
        } else {
            iv.setSelected(true);
            iv.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        }

        // Stops/Resumes sending the local audio stream.
        mRtcEngineX.muteLocalAudioStream(iv.isSelected());
    }

    // Tutorial Step 5
    public void onSwitchSpeakerphoneClicked(View view) {
        ImageView iv = (ImageView) view;
        if (iv.isSelected()) {
            iv.setSelected(false);
            iv.clearColorFilter();
        } else {
            iv.setSelected(true);
            iv.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        }

        // Enables/Disables the audio playback route to the speakerphone.
        //
        // This method sets whether the audio is routed to the speakerphone or earpiece. After calling this method, the SDK returns the onAudioRouteChanged callback to indicate the changes.
        mRtcEngineX.setSpeakerphoneOn(view.isSelected());
    }

    public void onEncCallClicked(View view) {
        mRtcEngine.leaveChannel();
        NERtcEx.getInstance().release();
        finish();
    }

    public void doStartAudioPcmDump(View view) {
        mRtcEngineX.startAudioDump();
    }

    public void doStopAudioPcmDump(View view) {
        mRtcEngineX.stopAudioDump();
    }

    // Tutorial Step 3
        private void leaveChannel() {
        mRtcEngine.leaveChannel();
    }

   private void hideSoftKeyboard () {
            if (getCurrentFocus() == null) return;
            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            if (imm == null) return;
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }

        private String generateRandomUserID () {
            return String.valueOf(new Random().nextInt(100000));
        }


        @Override
        public void onUserJoined ( long userID){
//        for (NERtcVideoView videoView : mRemoteUserVvList) {
//            if (videoView.getTag() == null) {
//                videoView.setZOrderMediaOverlay(true);
//                videoView.setScalingType(NERtcConstants.VideoScalingType.SCALE_ASPECT_BALANCED);
//                NERtc.getInstance().setupRemoteVideoCanvas(videoView, userID);
//                videoView.setTag(userID);
//                break;
//            }
//        }
        }

        @Override
        public void onUserLeave ( long userID, int i){
//        NERtcVideoView userView = mContainer.findViewWithTag(userID);
//        if (userView != null) {
//            userView.setTag(null);
//        }
        }

        @Override
        public void onClientRoleChange ( int var1, int var2){

        }

        @Override
        public void onUserVideoStart ( long userID, int profile){
//        NERtc.getInstance().subscribeRemoteVideoStream(userID, NERtcRemoteVideoStreamType.kNERtcRemoteVideoStreamTypeHigh, true);
        }

        @Override
        public void onJoinChannel ( int i, long l, long l1){

        }

        @Override
        public void onLeaveChannel ( int i){
            NERtc.getInstance().release();
            finish();
        }

        @Override
        public void onUserAudioStart ( long l){

        }

        @Override
        public void onUserAudioStop ( long l){

        }

        @Override
        public void onUserVideoStop ( long l){

        }

        @Override
        public void onDisconnect ( int i){

        }
}

//
//        @Override
//        protected void onDestroy() {
//        super.onDestroy();
//
//        leaveChannel();
//        RtcEngine.destroy();
//        mRtcEngine = null;
//    }
//
//        public boolean checkSelfPermission(String[] permissions, int requestCode) {
//        for (int i = 0; i < permissions.length - 1; i++) {
//            if (ContextCompat.checkSelfPermission(this,
//                    permissions[i])
//                    != PackageManager.PERMISSION_GRANTED) {
//
//                ActivityCompat.requestPermissions(this,
//                        permissions,
//                        requestCode + i);
//                return false;
//            }
//        }
//        return true;
//    }
//
//
//        // Tutorial Step 7
//        public void onLocalAudioMuteClicked(View view) {
//        ImageView iv = (ImageView) view;
//        if (iv.isSelected()) {
//            iv.setSelected(false);
//            iv.clearColorFilter();
//        } else {
//            iv.setSelected(true);
//            iv.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
//        }
//
//        // Stops/Resumes sending the local audio stream.
//        mRtcEngine.muteLocalAudioStream(iv.isSelected());
//    }
//
//        // Tutorial Step 5
//        public void onSwitchSpeakerphoneClicked(View view) {
//        ImageView iv = (ImageView) view;
//        if (iv.isSelected()) {
//            iv.setSelected(false);
//            iv.clearColorFilter();
//        } else {
//            iv.setSelected(true);
//            iv.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
//        }
//
//        // Enables/Disables the audio playback route to the speakerphone.
//        //
//        // This method sets whether the audio is routed to the speakerphone or earpiece. After calling this method, the SDK returns the onAudioRouteChanged callback to indicate the changes.
//        mRtcEngine.setEnableSpeakerphone(view.isSelected());
//    }
//
//        // Tutorial Step 3
//        public void onEncCallClicked(View view) {
//        finish();
//    }
//

//
//
//        mRtcEngine.setParameters("{\"rtc.log_filter\":65535}");
//        mRtcEngine.setParameters("{\"che.audio.\":\"all\"}");
//        // Sets the channel profile of the Agora RtcEngine.
//        // CHANNEL_PROFILE_COMMUNICATION(0): (Default) The Communication profile. Use this profile in one-on-one calls or group calls, where all users can talk freely.
//        // CHANNEL_PROFILE_LIVE_BROADCASTING(1): The Live-Broadcast profile. Users in a live-broadcast channel have a role as either broadcaster or audience. A broadcaster can both send and receive streams; an audience can only receive streams.
//        mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
//        mRtcEngine.setClientRole(1);
//        mRtcEngine.setAudioProfile(Constants.AUDIO_PROFILE_MUSIC_HIGH_QUALITY,Constants.AUDIO_SCENARIO_GAME_STREAMING);
//        mRtcEngine.setParameters("{\"che.audio.start_debug_recording\":\"NoName\"}");
//
//
//        // Allows a user to join a channel.
//        mRtcEngine.joinChannel(accessToken, "hzt002", "Extra Optional Data", 0); // if you do not specify the uid, we will generate the uid for you
//    }
//

//

//

//    }
