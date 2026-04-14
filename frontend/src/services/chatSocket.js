// ─── MICHAEL: WebSocket contract ─────────────────────────────────────────────
//
// This client connects to VITE_CHAT_WS_URL (default: ws://localhost:8080/ws/chat).
// The JWT token is passed as a query param: ?token=<accessToken>
//
// Expected message shapes (JSON over WebSocket):
//
//   Client → Server:
//     { type: "message", threadId: string, content: string }
//     { type: "ping" }
//
//   Server → Client:
//     { type: "message", threadId: string, id: string, senderEmail: string, content: string, sentAt: string }
//     { type: "pong" }
//     { type: "error", message: string }
//
// If WebSocket is not ready, set VITE_CHAT_WS_URL="" in .env and the chat page
// will fall back to polling GET /chat/api/threads/:id/messages every
// VITE_CHAT_POLL_INTERVAL_MS milliseconds.
// ─────────────────────────────────────────────────────────────────────────────

const WS_URL     = import.meta.env.VITE_CHAT_WS_URL ?? '';
const MAX_DELAY  = 30_000;
const BASE_DELAY = 1_000;

let socket       = null;
let reconnectTimer = null;
let attempt      = 0;
let onMessageCb  = null;
let onStatusCb   = null;

function notifyStatus(status) {
  onStatusCb?.(status); // 'connecting' | 'open' | 'closed' | 'error' | 'unavailable'
}

function scheduleReconnect(token) {
  const delay = Math.min(BASE_DELAY * 2 ** attempt, MAX_DELAY);
  attempt += 1;
  notifyStatus('closed');
  reconnectTimer = setTimeout(() => connect(token), delay);
}

export function connect(token) {
  if (!WS_URL || !token) {
    notifyStatus('unavailable');
    return;
  }
  if (socket && socket.readyState === WebSocket.OPEN) return;

  clearTimeout(reconnectTimer);
  notifyStatus('connecting');

  socket = new WebSocket(`${WS_URL}?token=${encodeURIComponent(token)}`);

  socket.onopen = () => {
    attempt = 0;
    notifyStatus('open');
  };

  socket.onmessage = (event) => {
    try {
      const msg = JSON.parse(event.data);
      if (msg.type !== 'pong') onMessageCb?.(msg);
    } catch {
      // ignore malformed frames
    }
  };

  socket.onerror = () => notifyStatus('error');

  socket.onclose = () => scheduleReconnect(token);
}

export function disconnect() {
  clearTimeout(reconnectTimer);
  socket?.close();
  socket = null;
  attempt = 0;
}

export function sendMessage(threadId, content) {
  if (socket?.readyState !== WebSocket.OPEN) return false;
  socket.send(JSON.stringify({ type: 'message', threadId, content }));
  return true;
}

export function onMessage(cb)  { onMessageCb = cb; }
export function onStatus(cb)   { onStatusCb  = cb; }
