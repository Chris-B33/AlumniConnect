function Footer() {
  return (
    <footer style={styles.footer}>
      <div style={styles.inner}>

        <div style={styles.col}>
          <strong style={styles.colHeading}>AlumniConnect</strong>
          <p style={styles.colText}>
            Connecting University of Limerick graduates and students through mentorship, events, and community.
          </p>
        </div>

        <div style={styles.col}>
          <strong style={styles.colHeading}>University of Limerick</strong>
          <ul style={styles.list}>
            <li>Castletroy, Limerick, V94 T9PX</li>
            <li>Ireland</li>
            <li>Tel: +353 61 202700</li>
            <li>Email: info@ul.ie</li>
          </ul>
        </div>

        <div style={styles.col}>
          <strong style={styles.colHeading}>Quick Links</strong>
          <ul style={styles.list}>
            <li><a href="https://www.ul.ie" style={styles.link} target="_blank" rel="noreferrer">UL Homepage</a></li>
            <li><a href="https://www.ul.ie/about/contact-us" style={styles.link} target="_blank" rel="noreferrer">Contact Us</a></li>
          </ul>
        </div>

      </div>

      <div style={styles.bottom}>
        <span>© {new Date().getFullYear()} University of Limerick. All rights reserved.</span>
        <span style={styles.divider}>·</span>
        <span>AlumniConnect is an internal platform for UL students and alumni.</span>
      </div>
    </footer>
  );
}

const styles = {
  footer: {
    marginTop: 'auto',
    background: '#1a3a2a',
    color: 'rgba(255,255,255,0.8)',
    fontSize: '0.875rem',
  },
  inner: {
    maxWidth: '960px',
    margin: '0 auto',
    padding: '2.5rem 1.5rem',
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
    gap: '2rem',
  },
  col: {
    display: 'flex',
    flexDirection: 'column',
    gap: '0.75rem',
  },
  colHeading: {
    color: '#fff',
    fontSize: '0.9375rem',
    fontWeight: 700,
  },
  colText: {
    margin: 0,
    lineHeight: 1.6,
    color: 'rgba(255,255,255,0.65)',
  },
  list: {
    listStyle: 'none',
    margin: 0,
    padding: 0,
    display: 'flex',
    flexDirection: 'column',
    gap: '0.375rem',
    color: 'rgba(255,255,255,0.65)',
    lineHeight: 1.6,
  },
  link: {
    color: 'rgba(255,255,255,0.75)',
    textDecoration: 'none',
  },
  bottom: {
    borderTop: '1px solid rgba(255,255,255,0.1)',
    padding: '1rem 1.5rem',
    maxWidth: '960px',
    margin: '0 auto',
    display: 'flex',
    flexWrap: 'wrap',
    gap: '0.5rem',
    color: 'rgba(255,255,255,0.45)',
    fontSize: '0.8125rem',
  },
  divider: {
    color: 'rgba(255,255,255,0.25)',
  },
};

export default Footer;
