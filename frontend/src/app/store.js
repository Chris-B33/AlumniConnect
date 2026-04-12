import { configureStore } from '@reduxjs/toolkit';
import authReducer from '../features/auth/authSlice';
import eventsReducer from '../features/events/eventsSlice';
import mentorshipReducer from '../features/mentorship/mentorshipSlice';

const store = configureStore({
  reducer: {
    auth: authReducer,
    events: eventsReducer,
    mentorship: mentorshipReducer,
  },
});

export default store;
