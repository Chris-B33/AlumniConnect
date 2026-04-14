/**
 * Decodes the payload of a JWT without verifying the signature.
 * Safe to use client-side for display purposes — never trust this for auth decisions.
 */
export function parseJwt(token) {
  try {
    const payload = token.split('.')[1];
    const json = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
    return JSON.parse(json);
  } catch {
    return null;
  }
}
