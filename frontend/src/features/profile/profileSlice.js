import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import api from '../../services/api';

// ─── MARK: Profile API contract ───────────────────────────────────────────────
//
// This slice expects the following endpoints on the identity-service,
// routed through the gateway under /identity/**:
//
//   GET  /identity/api/profile
//        Headers: Authorization: Bearer <token>  (added automatically by api.js)
//        Response: {
//          email: string,
//          role: "STUDENT" | "ALUMNI",
//          firstName?: string,
//          lastName?: string,
//          bio?: string,
//          avatarUrl?: string   // full URL or gateway-relative path
//        }
//
//   PUT  /identity/api/profile
//        Headers: Authorization: Bearer <token>
//        Body (JSON): { firstName, lastName, bio }
//        Response: same shape as GET
//
//   POST /identity/api/profile/avatar
//        Headers: Authorization: Bearer <token>
//        Body: multipart/form-data, field name "avatar" (image file)
//        Response: { avatarUrl: string }
//
// Until GET /profile exists the page falls back to JWT claims (email + role).
// Once you've built the endpoints, flip VITE_FEATURE_PROFILE=true in .env.
// ─────────────────────────────────────────────────────────────────────────────

export const fetchProfile = createAsyncThunk(
  'profile/fetch',
  async (_, { rejectWithValue }) => {
    try {
      const response = await api.get('/identity/api/profile');
      return response.data;
    } catch (err) {
      return rejectWithValue(err.response?.data?.message ?? 'Failed to load profile');
    }
  }
);

export const updateProfile = createAsyncThunk(
  'profile/update',
  async (fields, { rejectWithValue }) => {
    try {
      const response = await api.put('/identity/api/profile', fields);
      return response.data;
    } catch (err) {
      return rejectWithValue(err.response?.data?.message ?? 'Failed to update profile');
    }
  }
);

export const uploadAvatar = createAsyncThunk(
  'profile/uploadAvatar',
  async ({ file, onProgress }, { rejectWithValue }) => {
    try {
      const formData = new FormData();
      formData.append('avatar', file);
      const response = await api.post('/identity/api/profile/avatar', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
        onUploadProgress: (e) => {
          if (onProgress && e.total) {
            onProgress(Math.round((e.loaded * 100) / e.total));
          }
        },
      });
      return response.data;
    } catch (err) {
      return rejectWithValue(err.response?.data?.message ?? 'Failed to upload avatar');
    }
  }
);

const profileSlice = createSlice({
  name: 'profile',
  initialState: {
    data: null,       // profile object from API
    fetchStatus: 'idle',
    saveStatus: 'idle',
    avatarStatus: 'idle',
    error: null,
    saveError: null,
    avatarError: null,
  },
  reducers: {
    clearSaveStatus: (state) => {
      state.saveStatus = 'idle';
      state.saveError = null;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchProfile.pending, (state) => {
        state.fetchStatus = 'pending';
        state.error = null;
      })
      .addCase(fetchProfile.fulfilled, (state, action) => {
        state.fetchStatus = 'fulfilled';
        state.data = action.payload;
      })
      .addCase(fetchProfile.rejected, (state, action) => {
        state.fetchStatus = 'rejected';
        state.error = action.payload;
      })
      .addCase(updateProfile.pending, (state) => {
        state.saveStatus = 'pending';
        state.saveError = null;
      })
      .addCase(updateProfile.fulfilled, (state, action) => {
        state.saveStatus = 'fulfilled';
        state.data = action.payload;
      })
      .addCase(updateProfile.rejected, (state, action) => {
        state.saveStatus = 'rejected';
        state.saveError = action.payload;
      })
      .addCase(uploadAvatar.pending, (state) => {
        state.avatarStatus = 'pending';
        state.avatarError = null;
      })
      .addCase(uploadAvatar.fulfilled, (state, action) => {
        state.avatarStatus = 'fulfilled';
        if (state.data) state.data.avatarUrl = action.payload.avatarUrl;
      })
      .addCase(uploadAvatar.rejected, (state, action) => {
        state.avatarStatus = 'rejected';
        state.avatarError = action.payload;
      });
  },
});

export const { clearSaveStatus } = profileSlice.actions;
export const selectProfile = (state) => state.profile;
export default profileSlice.reducer;
