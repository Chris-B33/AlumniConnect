import { useDispatch, useSelector } from 'react-redux';
import { logout } from '../features/auth/authSlice';
import { selectEvents, fetchEvents } from '../features/events/eventsSlice';
import { useEffect } from 'react';

function HomePage() {
  const dispatch = useDispatch();
  const { items, status, error } = useSelector(selectEvents);

  useEffect(() => {
    dispatch(fetchEvents());
  }, [dispatch]);

  return (
    <main>
      <h1>Welcome to AlumniConnect</h1>
      <button onClick={() => dispatch(logout())}>Sign Out</button>

      <section>
        <h2>Events</h2>
        {status === 'pending' && <p>Loading events…</p>}
        {status === 'rejected' && <p role="alert">{error}</p>}
        {status === 'fulfilled' && items.length === 0 && <p>No events yet.</p>}
        <ul>
          {items.map((event) => (
            <li key={event.id}>{event.title}</li>
          ))}
        </ul>
      </section>
    </main>
  );
}

export default HomePage;
