import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import api from '../../services/api';

// ─── MICHAEL: Chat REST contract ─────────────────────────────────────────────
//
// This slice expects the following endpoints routed through the gateway
// under /chat/** (StripPrefix=1 → chat-service):
//
//   GET /chat/api/threads
//       Headers: Authorization: Bearer <token>
//       Response: [{ id: string, participantEmail: string, lastMessage?: string, updatedAt: string }]
//
//   GET /chat/api/threads/:threadId/messages
//       Headers: Authorization: Bearer <token>
//       Response: [{ id: string, senderEmail: string, content: string, sentAt: string }]
//
//   POST /chat/api/threads/:threadId/messages
//       Headers: Authorization: Bearer <token>
//       Body: { content: string }
//       Response: { id, senderEmail, content, sentAt }
//       (Used as fallback when WebSocket is unavailable)
//
// Real-time delivery is handled by chatSocket.js (WebSocket).
// See VITE_CHAT_WS_URL and VITE_CHAT_POLL_INTERVAL_MS in .env.example.
// ─────────────────────────────────────────────────────────────────────────────

export const fetchThreads = createAsyncThunk(
  'chat/fetchThreads',
  async (_, { rejectWithValue }) => {
    try {
      const response = await api.get('/chat/api/threads');
      return Array.isArray(response.data) ? response.data : [];
    } catch (err) {
      return rejectWithValue(err.response?.data?.message ?? 'Failed to load conversations');
    }
  }
);

export const fetchMessages = createAsyncThunk(
  'chat/fetchMessages',
  async (threadId, { rejectWithValue }) => {
    try {
      const response = await api.get(`/chat/api/threads/${threadId}/messages`);
      return { threadId, messages: Array.isArray(response.data) ? response.data : [] };
    } catch (err) {
      return rejectWithValue(err.response?.data?.message ?? 'Failed to load messages');
    }
  }
);

export const sendMessageHttp = createAsyncThunk(
  'chat/sendMessage',
  async ({ threadId, content }, { rejectWithValue }) => {
    try {
      const response = await api.post(`/chat/api/threads/${threadId}/messages`, { content });
      return { threadId, message: response.data };
    } catch (err) {
      return rejectWithValue(err.response?.data?.message ?? 'Failed to send message');
    }
  }
);

const chatSlice = createSlice({
  name: 'chat',
  initialState: {
    threads: [],
    threadStatus: 'idle',
    activeThreadId: null,
    messages: {},        // keyed by threadId
    messageStatus: 'idle',
    sendStatus: 'idle',
    wsStatus: 'unavailable', // 'connecting' | 'open' | 'closed' | 'error' | 'unavailable'
    error: null,
  },
  reducers: {
    setActiveThread: (state, action) => {
      state.activeThreadId = action.payload;
    },
    setWsStatus: (state, action) => {
      state.wsStatus = action.payload;
    },
    receiveMessage: (state, action) => {
      const { threadId, ...msg } = action.payload;
      if (!state.messages[threadId]) state.messages[threadId] = [];
      state.messages[threadId].push(msg);
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchThreads.pending, (state) => {
        state.threadStatus = 'pending';
        state.error = null;
      })
      .addCase(fetchThreads.fulfilled, (state, action) => {
        state.threadStatus = 'fulfilled';
        state.threads = action.payload;
      })
      .addCase(fetchThreads.rejected, (state, action) => {
        state.threadStatus = 'rejected';
        state.error = action.payload;
      })
      .addCase(fetchMessages.pending, (state) => {
        state.messageStatus = 'pending';
      })
      .addCase(fetchMessages.fulfilled, (state, action) => {
        state.messageStatus = 'fulfilled';
        state.messages[action.payload.threadId] = action.payload.messages;
      })
      .addCase(fetchMessages.rejected, (state) => {
        state.messageStatus = 'rejected';
      })
      .addCase(sendMessageHttp.pending, (state) => {
        state.sendStatus = 'pending';
      })
      .addCase(sendMessageHttp.fulfilled, (state, action) => {
        state.sendStatus = 'idle';
        const { threadId, message } = action.payload;
        if (!state.messages[threadId]) state.messages[threadId] = [];
        state.messages[threadId].push(message);
      })
      .addCase(sendMessageHttp.rejected, (state) => {
        state.sendStatus = 'rejected';
      });
  },
});

export const { setActiveThread, setWsStatus, receiveMessage } = chatSlice.actions;
export const selectChat = (state) => state.chat;
export default chatSlice.reducer;
