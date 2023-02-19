package com.whalestalk.rtc.videocallsample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.whalestalk.rtc.sdk.SurfaceViewRenderer;
import com.whalestalk.rtc.sdk.WhalesTalkClient;
import com.whalestalk.rtc.sdk.WhalesTalkDefines;
import com.whalestalk.rtc.sdk.WhalesTalkObserver;

public class MainActivity extends AppCompatActivity implements WhalesTalkObserver {

    private static final String TAG = "[MF] Main";

    //==================================================
    // permissions
    //==================================================
    final String[] PERMISSIONS = {
            android.Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };

    private ActivityResultLauncher<String[]> multiplePermissionActivityResultLauncher;

    // Two fragments

    // simple join room, just let the fragment manager to take care of instance
    //JoinRoomFragment mJoinRoomFragment;
    VideoFragment mVideoFragment;


    //==================================================
    // WhalesTalk
    //==================================================

    private WhalesTalkClient mWhalesTalkClient;

    // APP KEY and Secret can be obtained from
    // support@whalestalk.com
    private static final String mAppKey = "";
    private static final String mSecret = "";

    private String mLocalAudioId;
    private String mLocalVideoId;

    private final WhalesTalkDefines.VideoDimension mLocalRequestDimension = WhalesTalkDefines.VideoDimension.HD720p;
    private final WhalesTalkDefines.VideoFrameRate mLocalRequestFPS = WhalesTalkDefines.VideoFrameRate.FPS24;

    private String mRoomName;

    private String mUserName;

    private String mRoomId;
    private String mUserId;

    private int mRoomJoinState;

    private int mCurrentCamIdx;

    private int mZoomLevel;

    private boolean mTorchOn;


    public MainActivity() {
        super(R.layout.activity_main);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // permission check
        ActivityResultContracts.RequestMultiplePermissions requestMultiplePermissionsContract = new ActivityResultContracts.RequestMultiplePermissions();
        multiplePermissionActivityResultLauncher = registerForActivityResult(requestMultiplePermissionsContract, isGranted -> {
            Log.d(TAG, "Launcher result: " + isGranted.toString());
            if (isGranted.containsValue(false)) {
                Log.d(TAG, "At least one of the permissions was not granted, launching again...");
                multiplePermissionActivityResultLauncher.launch(PERMISSIONS);
            }
        });

        askPermissions();

        if (savedInstanceState == null) {
            // init WhalesTalkClient
            if(mWhalesTalkClient == null) {
                mWhalesTalkClient = new WhalesTalkClient();

                int ret = mWhalesTalkClient.registerObserver(this);
                Log.d(TAG, "register observer ret: " + Integer.toString(ret));

                ret = mWhalesTalkClient.init(mAppKey, WhalesTalkDefines.PeerRole.PEER, getApplicationContext());
                Log.d(TAG, "init ret: " + Integer.toString(ret));

                mWhalesTalkClient.setPermissionGranted(true);

                mRoomJoinState = WhalesTalkDefines.RoomJoinState.NEW.getValue();
            }

            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.container, JoinRoomFragment.class, null)
                    .commitNow();
        }
    }

    @Override
    public void onDestroy() {

        if(mWhalesTalkClient != null) {
            mWhalesTalkClient.close();
            mWhalesTalkClient = null;
        }

        super.onDestroy();
    }

    //==================================================
    // call from JoinRoomFragment
    //==================================================
    public void joinRoom(String roomName, String userName) {

        mRoomName = roomName;
        mUserName = userName;

        mVideoFragment = VideoFragment.newInstance(roomName, userName);

        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.container, mVideoFragment, null)
                .commit();
    }

    //==================================================
    // call from VideoFragment
    //==================================================
    public void startJoinRoom() {
        // add local video and audio
        mLocalAudioId = mWhalesTalkClient.addAudio(0);
        Log.d(TAG, "local audio id: " + mLocalAudioId);

        // available cam list for this device
        String[] camList = mWhalesTalkClient.getCameraList();
        for(String cam : camList) {
            Log.d(TAG, "found cam: " + cam);
        }

        mLocalVideoId = mWhalesTalkClient.addVideo(0, mLocalRequestDimension, mLocalRequestFPS, false);
        Log.d(TAG, "local video id: " + mLocalVideoId);

        mZoomLevel = 1;

        mTorchOn = false;
        // join room
        mRoomJoinState = WhalesTalkDefines.RoomJoinState.NEW.getValue();
        int ret = mWhalesTalkClient.joinRoomWithSecret(mSecret, mRoomName, mUserName, "");
        Log.d(TAG, "join room ret: " + Integer.toString(ret));
    }

    public void muteLocalAudio(boolean mute) {
        mWhalesTalkClient.muteAudio(mLocalAudioId, mute);
    }

    public void muteLocalVideo(boolean mute) {
        mWhalesTalkClient.muteVideo(mLocalVideoId, mute);
    }

    public void removeVideoMedia(String mediaId) {
        mWhalesTalkClient.removeVideoMedia(mediaId);
    }

    public void zoomIn() {
        int level = mZoomLevel + 1;
        int ret = mWhalesTalkClient.setVideoZoomLevel(mLocalVideoId, level);
        if(ret == 0) {
            mZoomLevel = level;
        }
    }

    public void zoomOut() {
        int level = mZoomLevel - 1;
        int ret = mWhalesTalkClient.setVideoZoomLevel(mLocalVideoId, level);
        if(ret == 0) {
            mZoomLevel = level;
        }
    }

    public void toggleFlash() {
        boolean flag = !mTorchOn;

        int ret = mWhalesTalkClient.setFlashTorchOn(flag);
        if(ret == 0) {
            mTorchOn = flag;
        }
    }

    public boolean getFlashStatus() {
        return mTorchOn;
    }

    public void leaveRoom() {

        if(mVideoFragment != null) {
            mVideoFragment.clean();
        }

        mWhalesTalkClient.leaveRoom(mRoomId);

        mRoomJoinState = WhalesTalkDefines.RoomJoinState.NEW.getValue();

        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.container, JoinRoomFragment.class, null)
                .commitNow();
    }


    //==================================================
    // request for permissions
    //==================================================
    private void askPermissions() {
        if (!hasPermissions(PERMISSIONS)) {
            Log.d(TAG, "Launching multiple contract permission launcher for ALL required permissions");
            multiplePermissionActivityResultLauncher.launch(PERMISSIONS);
        } else {
            Log.d(TAG, "All permissions are already granted");
        }
    }

    private boolean hasPermissions(String[] permissions) {
        if (permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Permission is not granted: " + permission);
                    return false;
                }
                Log.d(TAG, "Permission already granted: " + permission);
            }
            return true;
        }
        return false;
    }

    //==================================================
    // WhalesTalkObserver
    //==================================================
    @Override
    public void onRoomJoinStateUpdated(int newState, String roomName, String roomId, String userId) {
        Log.d(TAG, "room: " + roomId + " has a new state: " + Integer.toString(newState));

        if(mVideoFragment == null) {
            return;
        }

        mRoomJoinState = newState;

        mVideoFragment.updateUIComponent(newState);

        if(WhalesTalkDefines.RoomJoinState.AUTHENTICATED.getValue() == newState) {
            mUserId = userId;
            mRoomId = roomId;
        } else if(WhalesTalkDefines.RoomJoinState.JOINED.getValue() == newState) {
            int ret;

            // add video to room, audio to room
            ret = mWhalesTalkClient.addAudioToRoom(mRoomId, mLocalAudioId);
            Log.d(TAG, "add audio to room ret: " + Integer.toString(ret));

            ret = mWhalesTalkClient.addVideoToRoom(mRoomId, mLocalVideoId);
            Log.d(TAG, "add video to room ret: " + Integer.toString(ret));

            // enter room
            ret = mWhalesTalkClient.enterRoom(mRoomId);
            Log.d(TAG, "enter room ret: " + Integer.toString(ret));
        }
    }

    @Override
    public void onRoomLockEvent(String roomId, String peerId, boolean locked) {

    }

    @Override
    public void onRoomPauseEvent(String roomId, boolean paused) {

    }

    @Override
    public void onPeerJoined(String roomId, String peerId, String peerName) {
        Log.d(TAG, "room: " + roomId + ", has a new joined peer: " + peerId + ", name: " + peerName);
    }

    @Override
    public void onPeerLeft(String roomId, String peerId) {
        Log.d(TAG, "room: " + roomId + ", has a left peer: " + peerId);

        if(mVideoFragment != null) {
            mVideoFragment.peerLeft(peerId);
        }
    }

    @Override
    public void onVideoViewReady(String roomId, String peerId, String mediaId, boolean isLocal) {
        Log.d(TAG, "a new ready to view video: " + mediaId);

        if(mVideoFragment == null) {
            return;
        }

        SurfaceViewRenderer videoView = mWhalesTalkClient.getVideoView(mediaId);
        mVideoFragment.newVideoViewReady(peerId, mediaId, isLocal, videoView);
    }

    @Override
    public void onMediaStateUpdated(String roomId, String peerId, String mediaId, int newState) {
        Log.d(TAG, "media " + mediaId + " state changed to: " + newState);
    }

    @Override
    public void onSigMessage(String roomId, String peerId, String message, boolean isPrivate) {

    }

    @Override
    public void onDataChannelMessage(String roomId, String peerId, String message) {

    }
}
