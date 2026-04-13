import { useDispatch } from 'react-redux';
import { NavLink } from 'react-router-dom';
import { logout } from '../features/auth/authSlice';
import styles from './NavBar.module.css';

function NavBar() {
  const dispatch = useDispatch();

  return (
    <nav className={styles.nav}>
      <span className={styles.brand}>AlumniConnect</span>

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

      <button className={styles.signOut} onClick={() => dispatch(logout())}>
        Sign Out
      </button>
    </nav>
  );
}

export default NavBar;
