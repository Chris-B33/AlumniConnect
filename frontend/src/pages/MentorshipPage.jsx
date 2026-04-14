import { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { fetchMentorships, selectMentorship } from '../features/mentorship/mentorshipSlice';
import NavBar from '../components/NavBar';
import Footer from '../components/Footer';
import styles from './ServicePage.module.css';

function MentorshipPage() {
  const dispatch = useDispatch();
  const { items, status, error } = useSelector(selectMentorship);

  useEffect(() => {
    dispatch(fetchMentorships());
  }, [dispatch]);

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
            <button className={styles.retryBtn} onClick={() => dispatch(fetchMentorships())}>
              Retry
            </button>
          )}
        </div>

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
            <p>No mentorships found.</p>
            <span className={styles.emptyHint}>Mentorship connections will appear here once available.</span>
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
                  <p className={styles.cardMeta}>
                    Mentee: {m.menteeName ?? m.mentee}
                  </p>
                )}
                {m.status && (
                  <span className={styles.badge}>{m.status}</span>
                )}
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
