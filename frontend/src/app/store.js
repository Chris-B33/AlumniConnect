import { configureStore } from '@reduxjs/toolkit';
import authReducer from '../features/auth/authSlice';
import eventsReducer from '../features/events/eventsSlice';
import mentorshipReducer from '../features/mentorship/mentorshipSlice';
import profileReducer from '../features/profile/profileSlice';
import chatReducer from '../features/chat/chatSlice';

const store = configureStore({
  reducer: {
    auth: authReducer,
    events: eventsReducer,
    mentorship: mentorshipReducer,
    profile: profileReducer,
    chat: chatReducer,
  },
});

export default store;
