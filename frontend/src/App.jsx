import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { FEATURES } from './utils/features';
import ProtectedRoute from './components/ProtectedRoute';
import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import EventsPage from './pages/EventsPage';
import MentorshipPage from './pages/MentorshipPage';
import ProfilePage from './pages/ProfilePage';
import ChatPage from './pages/ChatPage';
import NotFoundPage from './pages/NotFoundPage';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login"    element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />

        {/* All routes inside here require a valid JWT */}
        <Route element={<ProtectedRoute />}>
          <Route path="/"           element={<HomePage />} />
          <Route path="/events"     element={<EventsPage />} />
          <Route path="/mentorship" element={<MentorshipPage />} />
          {FEATURES.profile && <Route path="/profile" element={<ProfilePage />} />}
          {FEATURES.chat    && <Route path="/chat"    element={<ChatPage />} />}
        </Route>

        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
