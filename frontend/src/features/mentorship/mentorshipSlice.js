import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import api from '../../services/api';

export const fetchMentorships = createAsyncThunk(
  'mentorship/fetchAll',
  async ({ query = '', email = '', role = '' } = {}, { rejectWithValue }) => {
    try {
      const params = {};
      if (query) params.q = query;
      if (role === 'STUDENT' && email) params.student = email;
      if (role === 'ALUMNI' && email) params.mentor = email;
      const response = await api.get('/mentorship/api/mentorships', { params });
      return response.data;
    } catch (err) {
      return rejectWithValue(err.response?.data?.message ?? 'Failed to fetch mentorships');
    }
  }
);

export const requestMentorship = createAsyncThunk(
  'mentorship/request',
  async ({ mentorEmail, studentEmail }, { rejectWithValue }) => {
    try {
      const response = await api.post('/mentorship/api/mentorships', { mentorEmail, studentEmail });
      return response.data;
    } catch (err) {
      return rejectWithValue(err.response?.data?.message ?? 'Failed to request mentorship');
    }
  }
);

export const acceptMentorship = createAsyncThunk(
  'mentorship/accept',
  async (id, { rejectWithValue }) => {
    try {
      const response = await api.patch(`/mentorship/api/mentorships/${id}/accept`);
      return response.data;
    } catch (err) {
      return rejectWithValue(err.response?.data?.message ?? 'Failed to accept mentorship');
    }
  }
);

export const declineMentorship = createAsyncThunk(
  'mentorship/decline',
  async (id, { rejectWithValue }) => {
    try {
      const response = await api.patch(`/mentorship/api/mentorships/${id}/decline`);
      return response.data;
    } catch (err) {
      return rejectWithValue(err.response?.data?.message ?? 'Failed to decline mentorship');
    }
  }
);

const upsertById = (items, updated) => {
  const idx = items.findIndex((m) => m.id === updated.id);
  if (idx >= 0) {
    items[idx] = updated;
  } else {
    items.push(updated);
  }
};

const mentorshipSlice = createSlice({
  name: 'mentorship',
  initialState: {
    items: [],
    status: 'idle',
    error: null,
    actionStatus: 'idle',
    actionError: null,
  },
  reducers: {
    clearActionStatus: (state) => {
      state.actionStatus = 'idle';
      state.actionError = null;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchMentorships.pending, (state) => {
        state.status = 'pending';
        state.error = null;
      })
      .addCase(fetchMentorships.fulfilled, (state, action) => {
        state.status = 'fulfilled';
        state.items = Array.isArray(action.payload) ? action.payload : [];
      })
      .addCase(fetchMentorships.rejected, (state, action) => {
        state.status = 'rejected';
        state.error = action.payload;
      })
      .addCase(requestMentorship.pending, (state) => {
        state.actionStatus = 'pending';
        state.actionError = null;
      })
      .addCase(requestMentorship.fulfilled, (state, action) => {
        state.actionStatus = 'fulfilled';
        const updated = action.payload;
        const idx = state.items.findIndex((m) => m.mentorEmail === updated.mentorEmail);
        if (idx >= 0) state.items[idx] = updated;
        else state.items.push(updated);
      })
      .addCase(requestMentorship.rejected, (state, action) => {
        state.actionStatus = 'rejected';
        state.actionError = action.payload;
      })
      .addCase(acceptMentorship.fulfilled, (state, action) => {
        upsertById(state.items, action.payload);
      })
      .addCase(declineMentorship.fulfilled, (state, action) => {
        upsertById(state.items, action.payload);
      });
  },
});

export const { clearActionStatus } = mentorshipSlice.actions;
export const selectMentorship = (state) => state.mentorship;
export default mentorshipSlice.reducer;
