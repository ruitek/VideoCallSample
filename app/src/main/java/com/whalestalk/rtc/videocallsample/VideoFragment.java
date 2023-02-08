package com.whalestalk.rtc.videocallsample;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.whalestalk.rtc.sdk.SurfaceViewRenderer;

import java.util.concurrent.ConcurrentHashMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VideoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VideoFragment extends Fragment {

    private static final String TAG = "[MF] Video";

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mRoomName;
    private String mUserName;

    private Handler mUIHandler;

    // <peerId, mediaId>
    // simplified version, actually a peer can have more than 1 video
    private ConcurrentHashMap<String, String> mPeerVideoMap;

    private String mLocalVideoId;

    private boolean mIsAudioMuted;
    private boolean mIsVideoMuted;

    private int mRoomJoinState;
    private static final String BUNDLE_AUDIO_MUTED = "audioMuted";
    private static final String BUNDLE_VIDEO_MUTED = "videoMuted";

    // UI Controls
    private LinearLayout mLLVideoCall;

    private ImageButton mBtnMuteAudio;
    private ImageButton mBtnMuteVideo;
    private ImageButton mBtnLeaveRoom;

    public VideoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param roomName Room Name.
     * @param userName User Name.
     * @return A new instance of fragment VideoFragment.
     */
    public static VideoFragment newInstance(String roomName, String userName) {
        VideoFragment fragment = new VideoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, roomName);
        args.putString(ARG_PARAM2, userName);
        fragment.setArguments(args);
        return fragment;
    }

    //==================================================
    // Fragment life cycle
    //==================================================
    @Override
    public void onAttach(@NonNull Context context) {
        Log.d(TAG, "onAttach");

        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mRoomName = getArguments().getString(ARG_PARAM1);
            mUserName = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        mRoomJoinState = 1; // start with authenticating

        // init an UI handler
        if(mUIHandler == null) {
            mUIHandler = new Handler(Looper.getMainLooper());
        }

        // init locally stored peer to video map
        if(mPeerVideoMap == null) {
            mPeerVideoMap = new ConcurrentHashMap<String, String>();
        }

        // prepare UI
        View videoView = inflater.inflate(R.layout.fragment_video, container, false);
        mLLVideoCall = (LinearLayout) videoView.findViewById(R.id.ll_video_call);
        mBtnMuteAudio = (ImageButton) videoView.findViewById(R.id.btn_mute_audio);
        mBtnMuteVideo = (ImageButton) videoView.findViewById(R.id.btn_mute_video);
        mBtnLeaveRoom = (ImageButton) videoView.findViewById(R.id.btn_leave_room);

        if (savedInstanceState != null) {
            mIsAudioMuted = savedInstanceState.getBoolean(BUNDLE_AUDIO_MUTED);
            mIsVideoMuted = savedInstanceState.getBoolean(BUNDLE_VIDEO_MUTED);
        }

        updateUIComponent(mRoomJoinState);
        setMuteAudioBtnLabel();
        setMuteVideoBtnLabel();

        mBtnMuteAudio.setOnClickListener(v -> {
            // toggle local audio mute status
            mIsAudioMuted = !mIsAudioMuted;
            ((MainActivity)requireActivity()).muteLocalAudio(mIsAudioMuted);

            // Set UI and Toast.
            mUIHandler.post(this::setMuteAudioBtnLabel);
        });

        mBtnMuteVideo.setOnClickListener(v -> {
            // toggle local video mute status
            mIsVideoMuted = !mIsVideoMuted;
            ((MainActivity)requireActivity()).muteLocalVideo(mIsVideoMuted);

            // Set UI and Toast.
            mUIHandler.post(this::setMuteVideoBtnLabel);
        });

        mBtnLeaveRoom.setOnClickListener(v -> ((MainActivity)requireActivity()).leaveRoom());

        return videoView;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated");
        super.onViewCreated(view, savedInstanceState);

        ((MainActivity)requireActivity()).startJoinRoom();
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        Log.d(TAG, "onViewStateRestored");

        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart");

        super.onStart();
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");

        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");

        super.onPause();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop");

        super.onStop();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        Log.d(TAG, "onSaveInstanceState");

        super.onSaveInstanceState(outState);

        outState.putBoolean(BUNDLE_AUDIO_MUTED, mIsAudioMuted);
        outState.putBoolean(BUNDLE_VIDEO_MUTED, mIsVideoMuted);
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView");

        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");

        super.onDestroy();
    }

    @Override
    public void onDetach() {
        Log.d(TAG, "onDetach");

        super.onDetach();
    }


    //==================================================
    // UI helper
    //==================================================
    private void setMuteAudioBtnLabel() {
        if(mIsAudioMuted) {
            mBtnMuteAudio.setImageDrawable(getResources().getDrawable(R.drawable.baseline_mic_off_24));
        } else {
            mBtnMuteAudio.setImageDrawable(getResources().getDrawable(R.drawable.baseline_mic_none_24));
        }
    }

    private void setMuteVideoBtnLabel() {
        if(mIsVideoMuted) {
            mBtnMuteVideo.setImageDrawable(getResources().getDrawable(R.drawable.baseline_videocam_off_24));
        } else {
            mBtnMuteVideo.setImageDrawable(getResources().getDrawable(R.drawable.baseline_videocam_24));
        }
    }

    private void onConnectedUIUpdate() {
        mBtnMuteAudio.setEnabled(true);
        mBtnMuteVideo.setEnabled(true);
        mBtnLeaveRoom.setEnabled(true);
    }

    private void onConnectingUIUpdate() {
        mBtnMuteAudio.setEnabled(true);
        mBtnMuteVideo.setEnabled(true);
        mBtnLeaveRoom.setEnabled(false);
    }

    private void onDisconnectedUIUpdate() {
        mBtnMuteAudio.setEnabled(false);
        mBtnMuteVideo.setEnabled(false);
        mBtnLeaveRoom.setEnabled(true);
    }

    public void updateUIComponent(int roomJoinState) {
        mRoomJoinState = roomJoinState;

        if(roomJoinState == 0 || roomJoinState >= 5) {
            mUIHandler.post(this::onDisconnectedUIUpdate);
        } else if(roomJoinState == 4) {
            mUIHandler.post(this::onConnectedUIUpdate);
        } else {
            mUIHandler.post(this::onConnectingUIUpdate);
        }
    }

    //=================================================
    // work flow functions
    //=================================================

    public void peerLeft(String peerId) {
        String mediaId = mPeerVideoMap.get(peerId);
        if(mediaId != null) {
            mPeerVideoMap.remove(peerId);

            mUIHandler.post(() -> removeVideoView(mediaId));
        }
    }

    public void newVideoViewReady(String peerId, String mediaId, boolean isLocal, SurfaceViewRenderer video) {
        if(!isLocal && !peerId.isEmpty() && !mediaId.isEmpty()) {
            mPeerVideoMap.put(peerId, mediaId);
        } else if (isLocal && !mediaId.isEmpty()) {
            mLocalVideoId = mediaId;
        }

        mUIHandler.post(() -> {
            addVideoView(video);
        });
    }

    private void addVideoView(SurfaceViewRenderer videoView) {
        if(videoView == null) {
            Log.w(TAG, "try to add null video view?");
            return;
        }

        View tmpView = mLLVideoCall.findViewWithTag(videoView.getTag());
        if(tmpView != null) {
            Log.w(TAG, "video view already added?");
            return;
        }

        // add view
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT, 1f);

        videoView.setLayoutParams(params);

        mLLVideoCall.addView(videoView);
    }

    private void removeVideoView(String mediaId) {
        View peerView = mLLVideoCall.findViewWithTag(mediaId);
        if(peerView != null) {
            mUIHandler.post(()->mLLVideoCall.removeView(peerView));
        }

        ((MainActivity)requireActivity()).removeVideoMedia(mediaId);
    }

    //==================================================
    // Other utilities
    //==================================================
    public void clean() {
        // remove remote video view
        for(String peerId : mPeerVideoMap.keySet()) {
            removeVideoView(mPeerVideoMap.get(peerId));
        }

        // remove local video view
        removeVideoView(mLocalVideoId);
    }

}