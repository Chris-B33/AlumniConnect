import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { fetchEvents, selectEvents } from '../features/events/eventsSlice';
import { useDebounce } from '../hooks/useDebounce';
import NavBar from '../components/NavBar';
import Footer from '../components/Footer';
import styles from './ServicePage.module.css';

function EventsPage() {
  const dispatch = useDispatch();
  const { items, status, error } = useSelector(selectEvents);

  const [search, setSearch] = useState('');
  const debouncedSearch = useDebounce(search, 400);

  useEffect(() => {
    dispatch(fetchEvents(debouncedSearch));
  }, [dispatch, debouncedSearch]);

  return (
    <>
      <NavBar />
      <main className={styles.page}>
        <div className={styles.header}>
          <div>
            <h1>Events</h1>
            <p className={styles.subtitle}>Upcoming alumni and university events</p>
          </div>
          {status === 'rejected' && (
            <button className={styles.retryBtn} onClick={() => dispatch(fetchEvents(debouncedSearch))}>
              Retry
            </button>
          )}
        </div>

        <input
          className={styles.searchInput}
          type="search"
          placeholder="Search events…"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          aria-label="Search events"
        />

        {(status === 'idle' || status === 'pending') && (
          <div className={styles.loadingRows}>
            <div className={styles.skeletonRow} />
            <div className={styles.skeletonRow} />
            <div className={styles.skeletonRow} />
          </div>
        )}

        {status === 'rejected' && (
          <div className={styles.errorBox} role="alert">
            <strong>Could not load events.</strong>
            <span>{error}</span>
          </div>
        )}

        {status === 'fulfilled' && items.length === 0 && (
          <div className={styles.emptyState}>
            <span className={styles.emptyIcon}>📅</span>
            <p>{search ? `No events found for "${search}"` : 'No events found.'}</p>
            <span className={styles.emptyHint}>
              {search ? 'Try a different search term.' : 'Check back soon — events will appear here when published.'}
            </span>
          </div>
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
      <Footer />
    </>
  );
}

export default EventsPage;
