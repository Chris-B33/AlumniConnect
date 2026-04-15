import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import api from '../../services/api';

/** Prefer server JSON { message } (and Spring defaults) for Redux error text. */
function apiErrorMessage(err, fallback) {
  const d = err.response?.data;
  if (typeof d === 'string' && d.trim()) return d.trim();
  if (d && typeof d.message === 'string' && d.message.trim()) return d.message.trim();
  if (d && typeof d.error === 'string' && d.error.trim()) return d.error.trim();
  const status = err.response?.status;
  if (status === 502 || status === 503 || status === 504) {
    return 'A backend service is unavailable. Ensure Docker Compose is running and Eureka shows all services UP.';
  }
  if (status === 403) {
    return 'You are not allowed to perform this action.';
  }
  return fallback;
}

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

export const fetchMentorAvailability = createAsyncThunk(
  'mentorship/fetchAvailability',
  async (_, { rejectWithValue }) => {
    try {
      const response = await api.get('/mentorship/api/mentors/me/availability');
      return response.data;
    } catch (err) {
      return rejectWithValue(apiErrorMessage(err, 'Failed to load availability'));
    }
  }
);

export const updateMentorAvailability = createAsyncThunk(
  'mentorship/updateAvailability',
  async (available, { rejectWithValue }) => {
    try {
      const response = await api.put('/mentorship/api/mentors/me/availability', { available });
      return response.data;
    } catch (err) {
      return rejectWithValue(apiErrorMessage(err, 'Failed to update availability'));
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
    mentorAvailable: null,
    availabilityStatus: 'idle',
    availabilityError: null,
    availabilityActionStatus: 'idle',
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
      })
      .addCase(fetchMentorAvailability.pending, (state) => {
        state.availabilityStatus = 'pending';
        state.availabilityError = null;
      })
      .addCase(fetchMentorAvailability.fulfilled, (state, action) => {
        state.availabilityStatus = 'fulfilled';
        state.mentorAvailable = action.payload?.available ?? false;
      })
      .addCase(fetchMentorAvailability.rejected, (state, action) => {
        state.availabilityStatus = 'rejected';
        state.availabilityError = action.payload;
      })
      .addCase(updateMentorAvailability.pending, (state) => {
        state.availabilityActionStatus = 'pending';
        state.availabilityError = null;
      })
      .addCase(updateMentorAvailability.fulfilled, (state, action) => {
        state.availabilityActionStatus = 'fulfilled';
        state.mentorAvailable = action.payload?.available ?? false;
      })
      .addCase(updateMentorAvailability.rejected, (state, action) => {
        state.availabilityActionStatus = 'rejected';
        state.availabilityError = action.payload;
      });
  },
});

export const { clearActionStatus } = mentorshipSlice.actions;
export const selectMentorship = (state) => state.mentorship;
export default mentorshipSlice.reducer;
