/**
 * Feature flags read from Vite env vars.
 * Set these in your .env file (copy from .env.example).
 * All flags default to false so unreleased features stay hidden until
 * the backing API is confirmed ready.
 */
export const FEATURES = {
  profile: import.meta.env.VITE_FEATURE_PROFILE === 'true',
  chat:    import.meta.env.VITE_FEATURE_CHAT    === 'true',
};
