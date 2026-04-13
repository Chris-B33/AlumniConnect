import { useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Link, useNavigate } from 'react-router-dom';
import { registerUser, selectAuth } from '../features/auth/authSlice';
import styles from './LoginPage.module.css';

function RegisterPage() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { status, error } = useSelector(selectAuth);

  const [form, setForm] = useState({ email: '', password: '', role: 'Student' });

  const handleChange = (e) => {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const result = await dispatch(registerUser(form));
    if (registerUser.fulfilled.match(result)) {
      navigate('/');
    }
  };

  return (
    <main className={styles.page}>
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
              placeholder="Min. 8 characters"
              minLength={8}
              required
            />
          </div>

          <div className={styles.field}>
            <label htmlFor="role">I am a</label>
            <select
              id="role"
              name="role"
              value={form.role}
              onChange={handleChange}
            >
              <option value="Student">Student</option>
              <option value="Alumni">Alumni</option>
            </select>
          </div>

          <button className={styles.submit} type="submit" disabled={status === 'pending'}>
            {status === 'pending' ? 'Creating account…' : 'Create Account'}
          </button>

          {error && <p className={styles.error} role="alert">{error}</p>}
        </form>

        <p className={styles.switchLink}>
          Already have an account? <Link to="/login">Sign in</Link>
        </p>
      </div>
    </main>
  );
}

export default RegisterPage;
