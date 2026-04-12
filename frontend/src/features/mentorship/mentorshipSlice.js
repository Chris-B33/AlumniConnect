import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import api from '../../services/api';

export const fetchMentorships = createAsyncThunk(
  'mentorship/fetchAll',
  async (_, { rejectWithValue }) => {
    try {
      const response = await api.get('/mentorships');
      return response.data;
    } catch (err) {
      return rejectWithValue(err.response?.data?.message ?? 'Failed to fetch mentorships');
    }
  }
);

const mentorshipSlice = createSlice({
  name: 'mentorship',
  initialState: {
    items: [],
    status: 'idle', // idle | pending | fulfilled | rejected
    error: null,
  },
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchMentorships.pending, (state) => {
        state.status = 'pending';
        state.error = null;
      })
      .addCase(fetchMentorships.fulfilled, (state, action) => {
        state.status = 'fulfilled';
        state.items = action.payload;
      })
      .addCase(fetchMentorships.rejected, (state, action) => {
        state.status = 'rejected';
        state.error = action.payload;
      });
  },
});

export const selectMentorship = (state) => state.mentorship;
export default mentorshipSlice.reducer;
