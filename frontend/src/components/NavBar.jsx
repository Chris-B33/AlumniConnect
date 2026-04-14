import { useDispatch, useSelector } from 'react-redux';
import { NavLink } from 'react-router-dom';
import { logout, selectAuth } from '../features/auth/authSlice';
import styles from './NavBar.module.css';

function NavBar() {
  const dispatch = useDispatch();
  const { user } = useSelector(selectAuth);

  const roleLabel = user?.role === 'ALUMNI' ? 'Alumni' : user?.role === 'STUDENT' ? 'Student' : null;

  return (
    <nav className={styles.nav}>
      <div className={styles.left}>
        <img src="/ul-logo.jpg" alt="University of Limerick" className={styles.logo} />
        <span className={styles.brand}>AlumniConnect</span>
      </div>

      <ul className={styles.links}>
        <li>
          <NavLink to="/" end className={({ isActive }) => isActive ? styles.active : undefined}>
            Dashboard
          </NavLink>
        </li>
        <li>
          <NavLink to="/events" className={({ isActive }) => isActive ? styles.active : undefined}>
            Events
          </NavLink>
        </li>
        <li>
          <NavLink to="/mentorship" className={({ isActive }) => isActive ? styles.active : undefined}>
            Mentorship
          </NavLink>
        </li>
      </ul>

      <div className={styles.right}>
        {user && (
          <div className={styles.userInfo}>
            {roleLabel && <span className={styles.roleBadge}>{roleLabel}</span>}
            <span className={styles.email}>{user.email}</span>
          </div>
        )}
        <button className={styles.signOut} onClick={() => dispatch(logout())}>
          Sign Out
        </button>
      </div>
    </nav>
  );
}

export default NavBar;
