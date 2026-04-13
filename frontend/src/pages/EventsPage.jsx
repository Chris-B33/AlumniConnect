import { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { fetchEvents, selectEvents } from '../features/events/eventsSlice';
import NavBar from '../components/NavBar';
import styles from './ServicePage.module.css';

function EventsPage() {
  const dispatch = useDispatch();
  const { items, status, error } = useSelector(selectEvents);

  useEffect(() => {
    dispatch(fetchEvents());
  }, [dispatch]);

  return (
    <>
      <NavBar />
      <main className={styles.page}>
        <div className={styles.header}>
          <h1>Events</h1>
          <p className={styles.subtitle}>Upcoming alumni and university events</p>
        </div>

        {status === 'pending' && (
          <p className={styles.loading}>Loading events…</p>
        )}

        {status === 'rejected' && (
          <div className={styles.errorBox} role="alert">
            <strong>Could not load events.</strong>
            <span>{error}</span>
          </div>
        )}

        {status === 'fulfilled' && items.length === 0 && (
          <p className={styles.empty}>No events found.</p>
        )}

        {status === 'fulfilled' && items.length > 0 && (
          <ul className={styles.list}>
            {items.map((event) => (
              <li key={event.id} className={styles.card}>
                <h2 className={styles.cardTitle}>{event.title}</h2>
                {event.date && <p className={styles.cardMeta}>{event.date}</p>}
                {event.description && <p className={styles.cardBody}>{event.description}</p>}
              </li>
            ))}
          </ul>
        )}
      </main>
    </>
  );
}

export default EventsPage;
