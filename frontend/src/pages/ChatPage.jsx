import { useEffect, useRef, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { selectAuth } from '../features/auth/authSlice';
import {
  fetchThreads,
  fetchMessages,
  sendMessageHttp,
  setActiveThread,
  setWsStatus,
  receiveMessage,
  selectChat,
} from '../features/chat/chatSlice';
import * as chatSocket from '../services/chatSocket';
import NavBar from '../components/NavBar';
import Footer from '../components/Footer';
import styles from './ChatPage.module.css';

const POLL_MS = Number(import.meta.env.VITE_CHAT_POLL_INTERVAL_MS) || 3000;
const WS_URL  = import.meta.env.VITE_CHAT_WS_URL ?? '';

function ChatPage() {
  const dispatch  = useDispatch();
  const { token } = useSelector(selectAuth);
  const {
    threads, threadStatus,
    activeThreadId, messages, messageStatus,
    sendStatus, wsStatus, error,
  } = useSelector(selectChat);

  const [draft, setDraft] = useState('');
  const bottomRef  = useRef();
  const pollRef    = useRef();

  // ── Connect WebSocket or start polling ──────────────────────────────────
  useEffect(() => {
    dispatch(fetchThreads());

    if (WS_URL && token) {
      chatSocket.onStatus((s) => dispatch(setWsStatus(s)));
      chatSocket.onMessage((msg) => {
        if (msg.type === 'message') dispatch(receiveMessage(msg));
      });
      chatSocket.connect(token);
      return () => chatSocket.disconnect();
    } else {
      dispatch(setWsStatus('unavailable'));
    }
  }, [dispatch, token]);

  // ── Fetch messages when active thread changes ───────────────────────────
  useEffect(() => {
    if (!activeThreadId) return;
    dispatch(fetchMessages(activeThreadId));

    // Poll if WS unavailable
    if (wsStatus === 'unavailable') {
      clearInterval(pollRef.current);
      pollRef.current = setInterval(() => {
        dispatch(fetchMessages(activeThreadId));
      }, POLL_MS);
    }
    return () => clearInterval(pollRef.current);
  }, [activeThreadId, wsStatus, dispatch]);

  // ── Scroll to latest message ────────────────────────────────────────────
  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, activeThreadId]);

  const handleSend = (e) => {
    e.preventDefault();
    const content = draft.trim();
    if (!content || !activeThreadId) return;

    const sent = WS_URL ? chatSocket.sendMessage(activeThreadId, content) : false;
    if (!sent) {
      dispatch(sendMessageHttp({ threadId: activeThreadId, content }));
    }
    setDraft('');
  };

  const activeMessages = activeThreadId ? (messages[activeThreadId] ?? []) : [];

  return (
    <>
      <NavBar />
      <main className={styles.page}>
        {/* ── Thread list ── */}
        <aside className={styles.sidebar}>
          <h2 className={styles.sidebarHeading}>Conversations</h2>

          {threadStatus === 'pending' && <p className={styles.sideNote}>Loading…</p>}
          {threadStatus === 'rejected' && <p className={styles.sideNote}>{error}</p>}
          {threadStatus === 'fulfilled' && threads.length === 0 && (
            <p className={styles.sideNote}>No conversations yet.</p>
          )}

          <ul className={styles.threadList}>
            {threads.map((t) => (
              <li
                key={t.id}
                className={`${styles.thread} ${t.id === activeThreadId ? styles.threadActive : ''}`}
                onClick={() => dispatch(setActiveThread(t.id))}
              >
                <span className={styles.threadEmail}>{t.participantEmail}</span>
                {t.lastMessage && (
                  <span className={styles.threadPreview}>{t.lastMessage}</span>
                )}
              </li>
            ))}
          </ul>
        </aside>

        {/* ── Message pane ── */}
        <section className={styles.pane}>
          {!activeThreadId ? (
            <div className={styles.emptyPane}>
              <span className={styles.emptyIcon}>💬</span>
              <p>Select a conversation to start messaging</p>
            </div>
          ) : (
            <>
              <div className={styles.wsStatus}>
                <span className={`${styles.dot} ${styles[wsStatus]}`} />
                <span>
                  {wsStatus === 'open'        && 'Live'}
                  {wsStatus === 'connecting'  && 'Connecting…'}
                  {wsStatus === 'closed'      && 'Reconnecting…'}
                  {wsStatus === 'error'       && 'Connection error'}
                  {wsStatus === 'unavailable' && `Polling every ${POLL_MS / 1000}s`}
                </span>
              </div>

              <div className={styles.messages}>
                {messageStatus === 'pending' && activeMessages.length === 0 && (
                  <p className={styles.loadingMsg}>Loading messages…</p>
                )}
                {activeMessages.map((msg) => (
                  <div key={msg.id} className={styles.bubble}>
                    <span className={styles.bubbleSender}>{msg.senderEmail}</span>
                    <p className={styles.bubbleContent}>{msg.content}</p>
                    {msg.sentAt && (
                      <span className={styles.bubbleTime}>
                        {new Date(msg.sentAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                      </span>
                    )}
                  </div>
                ))}
                <div ref={bottomRef} />
              </div>

              <form className={styles.sendBox} onSubmit={handleSend}>
                <input
                  className={styles.sendInput}
                  value={draft}
                  onChange={(e) => setDraft(e.target.value)}
                  placeholder="Type a message…"
                  disabled={sendStatus === 'pending'}
                />
                <button
                  className={styles.sendBtn}
                  type="submit"
                  disabled={!draft.trim() || sendStatus === 'pending'}
                >
                  Send
                </button>
              </form>
            </>
          )}
        </section>
      </main>
      <Footer />
    </>
  );
}

export default ChatPage;
