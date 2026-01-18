// src/components/VideoCall.jsx
import React, { useEffect, useRef } from "react";
import { ZegoUIKitPrebuilt } from "@zegocloud/zego-uikit-prebuilt";

const APP_ID = 1881138890;
const SERVER_SECRET = "88ed2503b274cc1ef595e09f5381c5a8";

// NHẬN PROPS RỜI: userId, userName (Thay vì object user)
export default function VideoCall({ roomId, userId, userName, onLeave }) {
  const containerRef = useRef(null);
  const zpRef = useRef(null);

  useEffect(() => {
    const startCall = async () => {
      // Tạo Token
      const kitToken = ZegoUIKitPrebuilt.generateKitTokenForTest(
        APP_ID,
        SERVER_SECRET,
        roomId,
        String(userId), // Đảm bảo là String
        userName || "User"
      );

      // Tạo instance
      zpRef.current = ZegoUIKitPrebuilt.create(kitToken);

      // Join phòng
      zpRef.current.joinRoom({
        container: containerRef.current,
        sharedLinks: [
          {
            name: "Link tham gia",
            url:
              window.location.protocol + "//" + window.location.host + window.location.pathname + "?roomID=" + roomId,
          },
        ],
        scenario: {
          mode: ZegoUIKitPrebuilt.VideoConference,
        },

        // --- TẮT MÀN HÌNH CHỜ & TỰ BẬT CAM/MIC ---
        showPreJoinView: false,
        turnOnMicrophoneWhenJoining: true,
        turnOnCameraWhenJoining: true,
        showMyCameraToggleButton: true,
        showMyMicrophoneToggleButton: true,
        showAudioVideoSettingsButton: true,

        onLeaveRoom: () => {
          onLeave();
        },
      });
    };

    if (containerRef.current) {
      startCall();
    }

    // --- CLEANUP: Hủy instance khi tắt component ---
    return () => {
      if (zpRef.current) {
        zpRef.current.destroy(); // <--- DÒNG NÀY SỬA LỖI REPEAT
        zpRef.current = null;
      }
    };

    // Chỉ chạy lại khi roomId hoặc userId thay đổi
  }, [roomId, userId, userName]);

  return (
    <div
      style={{
        position: "fixed",
        top: 0,
        left: 0,
        width: "100vw",
        height: "100vh",
        zIndex: 9999,
        backgroundColor: "#fff",
      }}
    >
      <div ref={containerRef} style={{ width: "100%", height: "100%" }} />
    </div>
  );
}
