import { useEffect, useRef, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { selectAuth } from '../features/auth/authSlice';
import {
  fetchProfile,
  updateProfile,
  uploadAvatar,
  clearSaveStatus,
  selectProfile,
} from '../features/profile/profileSlice';
import NavBar from '../components/NavBar';
import Footer from '../components/Footer';
import styles from './ProfilePage.module.css';

function ProfilePage() {
  const dispatch = useDispatch();
  const { user } = useSelector(selectAuth);
  const { data, fetchStatus, saveStatus, avatarStatus, error, saveError, avatarError } =
    useSelector(selectProfile);

  const [editing, setEditing] = useState(false);
  const [form, setForm] = useState({ firstName: '', lastName: '', bio: '' });
  const [avatarProgress, setAvatarProgress] = useState(0);
  const fileRef = useRef();

  // Populate form when profile data arrives
  useEffect(() => {
    dispatch(fetchProfile());
  }, [dispatch]);

  useEffect(() => {
    if (data) {
      setForm({
        firstName: data.firstName ?? '',
        lastName:  data.lastName  ?? '',
        bio:       data.bio       ?? '',
      });
    }
  }, [data]);

  // Reset save status when leaving edit mode
  useEffect(() => {
    if (!editing) dispatch(clearSaveStatus());
  }, [editing, dispatch]);

  const handleSave = async (e) => {
    e.preventDefault();
    const result = await dispatch(updateProfile(form));
    if (updateProfile.fulfilled.match(result)) setEditing(false);
  };

  const handleAvatarChange = (e) => {
    const file = e.target.files?.[0];
    if (!file) return;
    setAvatarProgress(0);
    dispatch(uploadAvatar({ file, onProgress: setAvatarProgress }));
  };

  // Fall back to JWT claims if the API hasn't landed yet
  const email     = data?.email     ?? user?.email ?? '—';
  const roleLabel = (data?.role ?? user?.role) === 'ALUMNI' ? 'Alumni' : 'Student';
  const avatarUrl = data?.avatarUrl ?? null;
  const initials  = email.slice(0, 2).toUpperCase();

  return (
    <>
      <NavBar />
      <main className={styles.page}>
        <h1 className={styles.heading}>My Profile</h1>

        {fetchStatus === 'rejected' && (
          <div className={styles.notice}>
            Profile API not available yet — showing account details from your token.
          </div>
        )}

        <div className={styles.card}>
          {/* ── Avatar ── */}
          <div className={styles.avatarSection}>
            <div className={styles.avatar}>
              {avatarUrl
                ? <img src={avatarUrl} alt="Avatar" className={styles.avatarImg} />
                : <span className={styles.avatarInitials}>{initials}</span>
              }
            </div>
            <button
              className={styles.avatarBtn}
              onClick={() => fileRef.current?.click()}
              disabled={avatarStatus === 'pending'}
            >
              {avatarStatus === 'pending' ? `Uploading… ${avatarProgress}%` : 'Change photo'}
            </button>
            <input
              ref={fileRef}
              type="file"
              accept="image/*"
              style={{ display: 'none' }}
              onChange={handleAvatarChange}
            />
            {avatarStatus === 'fulfilled' && <p className={styles.successMsg}>Photo updated.</p>}
            {avatarError && <p className={styles.errorMsg}>{avatarError}</p>}
          </div>

          {/* ── Details ── */}
          {!editing ? (
            <div className={styles.details}>
              <div className={styles.row}>
                <span className={styles.label}>Email</span>
                <span>{email}</span>
              </div>
              <div className={styles.row}>
                <span className={styles.label}>Role</span>
                <span className={styles.badge}>{roleLabel}</span>
              </div>
              {data?.firstName && (
                <div className={styles.row}>
                  <span className={styles.label}>Name</span>
                  <span>{data.firstName} {data.lastName}</span>
                </div>
              )}
              {data?.bio && (
                <div className={styles.row}>
                  <span className={styles.label}>Bio</span>
                  <span>{data.bio}</span>
                </div>
              )}
              <button className={styles.editBtn} onClick={() => setEditing(true)}>
                Edit profile
              </button>
            </div>
          ) : (
            <form className={styles.form} onSubmit={handleSave}>
              <div className={styles.field}>
                <label htmlFor="firstName">First name</label>
                <input
                  id="firstName"
                  value={form.firstName}
                  onChange={(e) => setForm((p) => ({ ...p, firstName: e.target.value }))}
                  placeholder="First name"
                />
              </div>
              <div className={styles.field}>
                <label htmlFor="lastName">Last name</label>
                <input
                  id="lastName"
                  value={form.lastName}
                  onChange={(e) => setForm((p) => ({ ...p, lastName: e.target.value }))}
                  placeholder="Last name"
                />
              </div>
              <div className={styles.field}>
                <label htmlFor="bio">Bio</label>
                <textarea
                  id="bio"
                  rows={3}
                  value={form.bio}
                  onChange={(e) => setForm((p) => ({ ...p, bio: e.target.value }))}
                  placeholder="Tell us about yourself…"
                />
              </div>
              {saveError && <p className={styles.errorMsg}>{saveError}</p>}
              <div className={styles.formActions}>
                <button type="button" className={styles.cancelBtn} onClick={() => setEditing(false)}>
                  Cancel
                </button>
                <button type="submit" className={styles.saveBtn} disabled={saveStatus === 'pending'}>
                  {saveStatus === 'pending' ? 'Saving…' : 'Save changes'}
                </button>
              </div>
            </form>
          )}
        </div>
      </main>
      <Footer />
    </>
  );
}

export default ProfilePage;
