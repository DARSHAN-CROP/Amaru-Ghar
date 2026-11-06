const C="ag-v3";
self.addEventListener("install",e=>{e.waitUntil(caches.open(C).then(c=>c.addAll(["./","index.html","manifest.json","icon.png","icon_dark.png","firebase-config.js","sync.js","lang.js"])).then(()=>self.skipWaiting()))});
self.addEventListener("activate",e=>e.waitUntil(self.clients.claim()));
self.addEventListener("fetch",e=>{if(e.request.method!=="GET"||new URL(e.request.url).origin!==self.location.origin)return;e.respondWith(fetch(e.request).then(r=>{const x=r.clone();caches.open(C).then(c=>c.put(e.request,x)).catch(()=>{});return r}).catch(()=>caches.match(e.request)))});
