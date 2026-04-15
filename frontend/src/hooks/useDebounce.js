import { useState, useEffect } from 'react';

/**
 * Returns a debounced copy of `value` that only updates after
 * `delay` ms of inactivity. Use this to avoid firing a search
 * API call on every keystroke.
 */
export function useDebounce(value, delay = 400) {
  const [debounced, setDebounced] = useState(value);

  useEffect(() => {
    const timer = setTimeout(() => setDebounced(value), delay);
    return () => clearTimeout(timer);
  }, [value, delay]);

  return debounced;
}
