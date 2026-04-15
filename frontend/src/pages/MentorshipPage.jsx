import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import {
  fetchMentorships,
  requestMentorship,
  acceptMentorship,
  declineMentorship,
  selectMentorship,
} from '../features/mentorship/mentorshipSlice';
import { selectAuth } from '../features/auth/authSlice';
import { useDebounce } from '../hooks/useDebounce';
import NavBar from '../components/NavBar';
import Footer from '../components/Footer';
import styles from './ServicePage.module.css';

const API_BASE = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';

function resolveUrl(url) {
  if (!url) return null;
  return url.startsWith('/') ? `${API_BASE}${url}` : url;
}

function MentorCard({ m, onRequest, actionStatus }) {
  const avatarUrl = resolveUrl(m.mentorAvatarUrl);
  const initials = (m.mentorName ?? m.mentorEmail ?? '?').slice(0, 2).toUpperCase();
  const isPending = actionStatus === 'pending';

  return (
    <li className={styles.card}>
      <div className={styles.cardInner}>
        <div className={styles.cardAvatar}>
          {avatarUrl
            ? <img src={avatarUrl} alt={m.mentorName} className={styles.cardAvatarImg} />
            : <span className={styles.cardAvatarInitials}>{initials}</span>}
        </div>
        <div className={styles.cardContent}>
          <div className={styles.cardHeader}>
            <h2 className={styles.cardTitle}>{m.mentorName ?? m.mentorEmail}</h2>
            <StatusBadge status={m.status} />
          </div>
          {m.mentorBio && <p className={styles.cardBio}>{m.mentorBio}</p>}
          {m.status === 'AVAILABLE' && (
            <button
              className={styles.actionBtn}
              onClick={() => onRequest(m.mentorEmail)}
              disabled={isPending}
            >
              {isPending ? 'Requesting…' : 'Request Mentorship'}
            </button>
          )}
        </div>
      </div>
    </li>
  );
}

function RequestCard({ m, onAccept, onDecline, actionStatus }) {
  const isPending = actionStatus === 'pending';

  return (
    <li className={styles.card}>
      <div className={styles.cardInner}>
        <div className={styles.cardContent}>
          <div className={styles.cardHeader}>
            <h2 className={styles.cardTitle}>{m.studentName ?? m.studentEmail}</h2>
            <StatusBadge status={m.status} />
          </div>
          <p className={styles.cardMeta}>{m.studentEmail}</p>
          {m.areaOfExpertise && (
            <p className={styles.cardMeta}>Area: {m.areaOfExpertise}</p>
          )}
          {m.status === 'REQUESTED' && (
            <div className={styles.cardActions}>
              <button
                className={styles.acceptBtn}
                onClick={() => onAccept(m.id)}
                disabled={isPending}
              >
                Accept
              </button>
              <button
                className={styles.declineBtn}
                onClick={() => onDecline(m.id)}
                disabled={isPending}
              >
                Decline
              </button>
            </div>
          )}
        </div>
      </div>
    </li>
  );
}

function StatusBadge({ status }) {
  const map = {
    AVAILABLE: { label: 'Available', cls: styles.badgeAvailable },
    REQUESTED: { label: 'Pending', cls: styles.badgePending },
    ACCEPTED:  { label: 'Accepted', cls: styles.badgeAccepted },
    DECLINED:  { label: 'Declined', cls: styles.badgeDeclined },
  };
  const { label, cls } = map[status] ?? { label: status, cls: styles.badge };
  return <span className={`${styles.badge} ${cls}`}>{label}</span>;
}

function MentorshipPage() {
  const dispatch = useDispatch();
  const { items, status, error, actionStatus, actionError } = useSelector(selectMentorship);
  const { user } = useSelector(selectAuth);

  const [search, setSearch] = useState('');
  const debouncedSearch = useDebounce(search, 400);

  const isAlumni = user?.role === 'ALUMNI';

  useEffect(() => {
    dispatch(fetchMentorships({
      query: debouncedSearch,
      email: user?.email ?? '',
      role: user?.role ?? '',
    }));
  }, [dispatch, debouncedSearch, user]);

  const handleRequest = (mentorEmail) => {
    dispatch(requestMentorship({ mentorEmail, studentEmail: user?.email }));
  };

  const handleAccept = (id) => dispatch(acceptMentorship(id));
  const handleDecline = (id) => dispatch(declineMentorship(id));

  return (
    <>
      <NavBar />
      <main className={styles.page}>
        <div className={styles.header}>
          <div>
            <h1>Mentorship</h1>
            <p className={styles.subtitle}>
              {isAlumni ? 'Manage incoming mentorship requests' : 'Connect with an alumni mentor'}
            </p>
          </div>
          {status === 'rejected' && (
            <button
              className={styles.retryBtn}
              onClick={() => dispatch(fetchMentorships({ query: debouncedSearch, email: user?.email ?? '', role: user?.role ?? '' }))}
            >
              Retry
            </button>
          )}
        </div>

        <input
          className={styles.searchInput}
          type="search"
          placeholder={isAlumni ? 'Search students…' : 'Search mentors…'}
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          aria-label="Search"
        />

        {actionError && (
          <div className={styles.errorBox} role="alert">
            <strong>Action failed.</strong>
            <span>{actionError}</span>
          </div>
        )}

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
            <p>{search ? `No results for "${search}"` : isAlumni ? 'No requests yet.' : 'No mentors available.'}</p>
            <span className={styles.emptyHint}>
              {search ? 'Try a different search term.' : isAlumni ? 'Requests from students will appear here.' : 'Check back later.'}
            </span>
          </div>
        )}

        {status === 'fulfilled' && items.length > 0 && (
          <ul className={styles.list}>
            {isAlumni
              ? items.map((m) => (
                  <RequestCard
                    key={m.id}
                    m={m}
                    onAccept={handleAccept}
                    onDecline={handleDecline}
                    actionStatus={actionStatus}
                  />
                ))
              : items.map((m) => (
                  <MentorCard
                    key={m.id ?? m.mentorEmail}
                    m={m}
                    onRequest={handleRequest}
                    actionStatus={actionStatus}
                  />
                ))}
          </ul>
        )}
      </main>
      <Footer />
    </>
  );
}

export default MentorshipPage;
