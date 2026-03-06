import { Client } from "@stomp/stompjs";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

let stompClient = null;

export const connectToKitchenSocket = (onMessage) => {
  if (stompClient && stompClient.active) {
    console.log("⚠️ Socket already active, skipping connect");
    return;
  }

  const socketUrl = API_BASE_URL.replace(/^http/, "ws") + "/ws";

  console.log("🔌 Connecting to:", socketUrl);

  stompClient = new Client({
    brokerURL: socketUrl,
    reconnectDelay: 5000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,

    onConnect: () => {
      console.log(" Kitchen Socket Connected!");

      // Subscribe nhận đơn
      stompClient.subscribe("/topic/kitchen/orders", (message) => {
        try {
          const payload = JSON.parse(message.body);
          onMessage(payload);
        } catch (e) {
          console.error(" Parse error:", e);
        }
      });
    },

    onStompError: (frame) => {
      console.error("❌ Broker error:", frame.headers["message"]);
    },

    onWebSocketClose: () => {
      console.log("⚠️ WebSocket connection closed");
    },
  });

  stompClient.activate();
};

export const disconnectSocket = () => {
  if (stompClient) {
    stompClient.deactivate();
    stompClient = null;
    console.log(" Kitchen Socket Disconnected (User left page)");
  }
};
