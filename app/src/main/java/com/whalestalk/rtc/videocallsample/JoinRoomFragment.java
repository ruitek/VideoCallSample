package com.whalestalk.rtc.videocallsample;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

public class JoinRoomFragment extends Fragment {

    private static final String TAG = "[MF] Join";

    private EditText m_etRoomName;
    private EditText m_etUserName;

    public JoinRoomFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View joinRoomView = inflater.inflate(R.layout.fragment_join_room, container, false);

        m_etRoomName = (EditText) joinRoomView.findViewById(R.id.et_room_name);
        m_etUserName = (EditText) joinRoomView.findViewById(R.id.et_user_name);
        Button joinRoomBT = (Button) joinRoomView.findViewById(R.id.btn_join_room);

        joinRoomBT.setOnClickListener(v -> joinRoom());

        return joinRoomView;
    }

    public void joinRoom() {
        String roomName = m_etRoomName.getText().toString();
        String userName = m_etUserName.getText().toString();

        if ("".equals(roomName)) {
            roomName = "demo888";
        }

        if ("".equals(userName)) {
            userName = "Bob";
        }

        Log.d(TAG, "room name: " + roomName + "; user name: " + userName);

        ((MainActivity) requireActivity()).joinRoom(roomName, userName);
    }

}