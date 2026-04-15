import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import api from '../../services/api';
import { parseJwt } from '../../utils/parseJwt';

function userFromToken(token) {
  if (!token) return null;
  const claims = parseJwt(token);
  if (!claims) return null;
  return {
    email: claims.sub,
    role: claims.role, // "STUDENT" | "ALUMNI"
  };
}

const storedToken = localStorage.getItem('token') ?? null;

export const loginUser = createAsyncThunk(
  'auth/login',
  async (credentials, { rejectWithValue }) => {
    try {
      const response = await api.post('/identity/api/auth/login', credentials);
      localStorage.setItem('token', response.data.accessToken);
      return response.data;
    } catch (err) {
      return rejectWithValue(err.response?.data?.message ?? 'Login failed');
    }
  }
);

export const registerUser = createAsyncThunk(
  'auth/register',
  async (credentials, { rejectWithValue }) => {
    try {
      const response = await api.post('/identity/api/auth/register', credentials);
      localStorage.setItem('token', response.data.accessToken);
      return response.data;
    } catch (err) {
      return rejectWithValue(err.response?.data?.message ?? 'Registration failed');
    }
  }
);

const authSlice = createSlice({
  name: 'auth',
  initialState: {
    user: userFromToken(storedToken),
    token: storedToken,
    status: 'idle', // idle | pending | fulfilled | rejected
    error: null,
  },
  reducers: {
    logout: (state) => {
      localStorage.removeItem('token');
      state.user = null;
      state.token = null;
      state.status = 'idle';
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(loginUser.pending, (state) => {
        state.status = 'pending';
        state.error = null;
      })
      .addCase(loginUser.fulfilled, (state, action) => {
        state.status = 'fulfilled';
        state.token = action.payload.accessToken;
        state.user = userFromToken(action.payload.accessToken);
      })
      .addCase(loginUser.rejected, (state, action) => {
        state.status = 'rejected';
        state.error = action.payload;
      })
      .addCase(registerUser.pending, (state) => {
        state.status = 'pending';
        state.error = null;
      })
      .addCase(registerUser.fulfilled, (state, action) => {
        state.status = 'fulfilled';
        state.token = action.payload.accessToken;
        state.user = userFromToken(action.payload.accessToken);
      })
      .addCase(registerUser.rejected, (state, action) => {
        state.status = 'rejected';
        state.error = action.payload;
      });
  },
});

export const { logout } = authSlice.actions;
export const selectAuth = (state) => state.auth;
export default authSlice.reducer;
