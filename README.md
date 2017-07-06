# component-reload-issue

## Context
I'm trying to combine `component` with `lein-ring` to support various workflows.

I'm not sure whether I have any option other than to introduce a global `system-map` to work-around the reloading issues I'm experiencing.

## TL;DR

- Execute `lein ring server-headless`
- Visit `http://localhost:8000/info` and observe JSON body
- Modify `demo-api-routes` changing JSON body
- Revisit `http://localhost:8000/info` and observe JSON body is __unchanged__

## Reproduction

To reproduce the issue, I've created a system consisting of two components

- a `demo-server` component
- a `demo-routes` component, which is intended to encapsulate my `compojure` routes and related handler code.

The `demo-server` is only started from under `lein run` where the system map is defined by:

```clojure
(component/system-map
     :demo-routes (demo-routes)
     :demo-server (component/using (demo-server) [:demo-routes]))
```

I'm not interested in reloading in this context. This would be my production deploy route, via an `uberjar`.

When running the project using `lein ring server-headless` the system map *only* includes the `demo-routes`. I don't need a `demo-server` in this case, as a Jetty server is started by `lein-ring`:

```clojure
(component/system-map
     :demo-routes (component/using (demo-routes) []))
```

Everything is working as expected under `lein run`:

```
➜  component-reload-issue git:(master) ✗ lein run
Start: routes
Start: server
-main routes: #object[component_ring_issue.core$wrap_with_deps$fn__6196 0x7b55fc83 component_ring_issue.core$wrap_with_deps$fn__6196@7b55fc83]
```

I have some routes in the system map and `http://localhost:8000/info` returns:

```json
{
  "status": "OK"
}
```

Similary, things appears to be working fine under `lein ring server-headless`:

```
➜  component-reload-issue git:(master) ✗ lein ring server-headless
2017-07-05 11:13:49.889:INFO::main: Logging initialized @777ms
Start: routes
lein-ring routes: #object[component_ring_issue.core$wrap_with_deps$fn__5010 0x5a9a187b component_ring_issue.core$wrap_with_deps$fn__5010@5a9a187b]
2017-07-05 11:13:53.366:INFO:oejs.Server:main: jetty-9.2.10.v20150310
2017-07-05 11:13:53.387:INFO:oejs.ServerConnector:main: Started ServerConnector@324e62e2{HTTP/1.1}{0.0.0.0:8000}
2017-07-05 11:13:53.390:INFO:oejs.Server:main: Started @4277ms
Started server on port 8000
```

The routes are initialised:

```
lein-ring routes: #object[component_ring_issue.core$wrap_with_deps$fn__5010 0x5a9a187b component_ring_issue.core$wrap_with_deps$fn__5010@5a9a187b]
```

And the `/info` returns the expected JSON containing the status `"OK"`

## Issue

Under `lein ring`, when I modify `demo-api-routes` e.g. changing the string `"OK"` to `"NOT OK"`, the change is not reflected in subsequent requests to `http://localhost:8000/info`

This is not unexpected given the approach I'm taking. I am struggling to find a way forward that doesn't involve introducing global state. My intent is to have `component` inject dependencies into my routes via the standard practice of introducing a middleware that associates those dependencies with each incoming request.

When I modify `core.clj` and the `/info` route request comes in, my understanding is that `wrap-reload` is invoked.

The reload will *redefine* my `demo-api-routes` (desirable) but the `component` system map will still refer to my old var, which still serves the `"OK"` response.

The way I'm joining up `lein-ring` and `component` is via the `:init` facility:

```clojure
:ring {:handler component-reload-issue.core/ring-handler
       :init component-reload-issue.core/ring-init
       :port 8000
       :auto-reload? true}
```

When `lein ring server-headless` is invoked, the `:init` function is invoked which first calls `(component/start)` on the system (injecting dependencies etc.) and then defines the var `ring-handler` which is referenced by `:handler`.

## Question
Is there a way for me to achieve the reloading behaviour I want with `lein-ring`, whilst also maintaining the encapsulation and testing advantages that `component` brings *without* introducing a global `system-map` and adandoning the componentisation and DI for `demo-routes`?
