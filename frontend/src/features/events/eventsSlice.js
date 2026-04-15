import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import api from '../../services/api';

// SEARCH CONTRACT: query string is sent as ?q=<term>
// Confirm param name with whoever owns the event-service search endpoint.
// e.g. GET /event/api/events?q=networking
export const fetchEvents = createAsyncThunk(
  'events/fetchAll',
  async (query = '', { rejectWithValue }) => {
    try {
      const params = query ? { q: query } : {};
      const response = await api.get('/event/api/events', { params });
      return response.data;
    } catch (err) {
      return rejectWithValue(err.response?.data?.message ?? 'Failed to fetch events');
    }
  }
);

const eventsSlice = createSlice({
  name: 'events',
  initialState: {
    items: [],
    status: 'idle', // idle | pending | fulfilled | rejected
    error: null,
  },
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchEvents.pending, (state) => {
        state.status = 'pending';
        state.error = null;
      })
      .addCase(fetchEvents.fulfilled, (state, action) => {
        state.status = 'fulfilled';
        state.items = Array.isArray(action.payload) ? action.payload : [];
      })
      .addCase(fetchEvents.rejected, (state, action) => {
        state.status = 'rejected';
        state.error = action.payload;
      });
  },
});

export const selectEvents = (state) => state.events;
export default eventsSlice.reducer;
