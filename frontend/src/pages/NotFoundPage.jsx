import { Link } from 'react-router-dom';
import styles from './NotFoundPage.module.css';

function NotFoundPage() {
  return (
    <main className={styles.page}>
      <span className={styles.code}>404</span>
      <p className={styles.message}>Page not found</p>
      <p className={styles.sub}>That route doesn't exist in AlumniConnect.</p>
      <Link className={styles.link} to="/">Back to home</Link>
    </main>
  );
}

export default NotFoundPage;
