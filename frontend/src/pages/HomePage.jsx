import { Link } from 'react-router-dom';
import { useSelector } from 'react-redux';
import { selectAuth } from '../features/auth/authSlice';
import NavBar from '../components/NavBar';
import Footer from '../components/Footer';
import styles from './HomePage.module.css';

function HomePage() {
  const { user } = useSelector(selectAuth);

  const greeting = user?.role === 'ALUMNI' ? 'Welcome back, Alumni' : user?.role === 'STUDENT' ? 'Welcome, Student' : 'Welcome';
  const subtext = user?.email ?? 'University of Limerick';

  return (
    <>
      <NavBar />
      <main className={styles.page}>
        <div className={styles.hero}>
          <p className={styles.heroLabel}>AlumniConnect</p>
          <h1>{greeting}</h1>
          <p className={styles.heroSub}>{subtext}</p>
        </div>

        <div className={styles.cards}>
          <Link to="/events" className={styles.card}>
            <div className={styles.cardIcon}>📅</div>
            <h2>Events</h2>
            <p>Browse upcoming alumni and university events</p>
          </Link>

          <Link to="/mentorship" className={styles.card}>
            <div className={styles.cardIcon}>🤝</div>
            <h2>Mentorship</h2>
            <p>Find a mentor or offer your experience as one</p>
          </Link>
        </div>
      </main>
      <Footer />
    </>
  );
}

export default HomePage;
