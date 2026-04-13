import { Link } from 'react-router-dom';
import NavBar from '../components/NavBar';
import styles from './HomePage.module.css';

function HomePage() {
  return (
    <>
      <NavBar />
      <main className={styles.page}>
        <div className={styles.hero}>
          <h1>Welcome to AlumniConnect</h1>
          <p>University of Limerick — connecting students and alumni</p>
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
    </>
  );
}

export default HomePage;
