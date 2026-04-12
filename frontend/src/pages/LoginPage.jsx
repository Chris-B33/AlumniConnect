import { useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { loginUser, selectAuth } from '../features/auth/authSlice';
import styles from './LoginPage.module.css';

function LoginPage() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { status, error } = useSelector(selectAuth);

  const [form, setForm] = useState({ email: '', password: '' });

  const handleChange = (e) => {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const result = await dispatch(loginUser(form));
    if (loginUser.fulfilled.match(result)) {
      navigate('/');
    }
  };

  return (
    <main className={styles.page}>

      {/* Crossfading campus background — drop images in frontend/public/ */}
      <div className={styles.slideshow} aria-hidden="true">
        <div className={styles.slide} />
        <div className={styles.slide} />
        <div className={styles.slide} />
      </div>

      <div className={styles.card}>
        <div className={styles.logo}>
          <h1>AlumniConnect</h1>
          <p>University of Limerick</p>
        </div>

        <form className={styles.form} onSubmit={handleSubmit}>
          <div className={styles.field}>
            <label htmlFor="email">Email</label>
            <input
              id="email"
              type="email"
              name="email"
              value={form.email}
              onChange={handleChange}
              placeholder="you@ul.ie"
              required
            />
          </div>

          <div className={styles.field}>
            <label htmlFor="password">Password</label>
            <input
              id="password"
              type="password"
              name="password"
              value={form.password}
              onChange={handleChange}
              placeholder="••••••••"
              required
            />
          </div>

          <button className={styles.submit} type="submit" disabled={status === 'pending'}>
            {status === 'pending' ? 'Signing in…' : 'Sign In'}
          </button>

          {error && <p className={styles.error} role="alert">{error}</p>}
        </form>
      </div>
    </main>
  );
}

export default LoginPage;
