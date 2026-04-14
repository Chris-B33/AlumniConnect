import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { fetchMentorships, selectMentorship } from '../features/mentorship/mentorshipSlice';
import { useDebounce } from '../hooks/useDebounce';
import NavBar from '../components/NavBar';
import Footer from '../components/Footer';
import styles from './ServicePage.module.css';

function MentorshipPage() {
  const dispatch = useDispatch();
  const { items, status, error } = useSelector(selectMentorship);

  const [search, setSearch] = useState('');
  const debouncedSearch = useDebounce(search, 400);

  useEffect(() => {
    dispatch(fetchMentorships(debouncedSearch));
  }, [dispatch, debouncedSearch]);

  return (
    <>
      <NavBar />
      <main className={styles.page}>
        <div className={styles.header}>
          <div>
            <h1>Mentorship</h1>
            <p className={styles.subtitle}>Connect with alumni mentors and mentees</p>
          </div>
          {status === 'rejected' && (
            <button className={styles.retryBtn} onClick={() => dispatch(fetchMentorships(debouncedSearch))}>
              Retry
            </button>
          )}
        </div>

        <input
          className={styles.searchInput}
          type="search"
          placeholder="Search mentors…"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          aria-label="Search mentorships"
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
            <strong>Could not load mentorships.</strong>
            <span>{error}</span>
          </div>
        )}

        {status === 'fulfilled' && items.length === 0 && (
          <div className={styles.emptyState}>
            <span className={styles.emptyIcon}>🤝</span>
            <p>{search ? `No mentors found for "${search}"` : 'No mentorships found.'}</p>
            <span className={styles.emptyHint}>
              {search ? 'Try a different search term.' : 'Mentorship connections will appear here once available.'}
            </span>
          </div>
        )}

        {status === 'fulfilled' && items.length > 0 && (
          <ul className={styles.list}>
            {items.map((m) => (
              <li key={m.id} className={styles.card}>
                <h2 className={styles.cardTitle}>
                  {m.mentorName ?? m.mentor ?? 'Mentor'}
                </h2>
                {(m.menteeName ?? m.mentee) && (
                  <p className={styles.cardMeta}>Mentee: {m.menteeName ?? m.mentee}</p>
                )}
                {m.status && <span className={styles.badge}>{m.status}</span>}
              </li>
            ))}
          </ul>
        )}
      </main>
      <Footer />
    </>
  );
}

export default MentorshipPage;
