// Google sign-in + online sync (Firebase). Stored in the same per-user document, field "home".
const cfg = window.FIREBASE_CONFIG;
if (cfg) {
  const V = "10.12.2";
  try {
  const [{ initializeApp }, A, F] = await Promise.all([
    import(`https://www.gstatic.com/firebasejs/${V}/firebase-app.js`),
    import(`https://www.gstatic.com/firebasejs/${V}/firebase-auth.js`),
    import(`https://www.gstatic.com/firebasejs/${V}/firebase-firestore.js`),
  ]);
  const app = initializeApp(cfg);
  const auth = A.getAuth(app);
  const db = F.getFirestore(app);
  window.SYNC_READY = true;
  let unsub = null, timer = null;
  window.agSignIn = () => A.signInWithPopup(auth, new A.GoogleAuthProvider()).catch((e) => alert(e.message));
  window.agSignOut = () => A.signOut(auth);
  A.onAuthStateChanged(auth, (user) => {
    window.agUser(user);
    if (unsub) { unsub(); unsub = null; }
    window.agSetPush(() => {});
    if (!user) return;
    const ref = F.doc(db, "planner", user.uid);
    unsub = F.onSnapshot(ref, (snap) => {
      const mine = window.agGet();
      if (snap.exists() && snap.data().home) {
        const remote = JSON.parse(snap.data().home);
        if (remote.updated > mine.updated) window.agApply(remote);
        else if (remote.updated < mine.updated) F.setDoc(ref, { home: JSON.stringify(mine) }, { merge: true });
      } else {
        F.setDoc(ref, { home: JSON.stringify(mine) }, { merge: true });
      }
    });
    window.agSetPush(() => {
      clearTimeout(timer);
      timer = setTimeout(() => F.setDoc(ref, { home: JSON.stringify(window.agGet()) }, { merge: true }), 600);
    });
  });
  } catch (e) { /* offline: the app keeps working without sync */ }
}
