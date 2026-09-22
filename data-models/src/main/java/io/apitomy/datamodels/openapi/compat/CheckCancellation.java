package io.apitomy.datamodels.openapi.compat;

import java.util.ArrayList;
import java.util.List;

/**
 * Portable cooperative cancellation signal, shared by synchronous analysis and
 * asynchronous resource acquisition.
 * <p>
 * Cancellation is cooperative: {@link #cancel()} only records that cancellation
 * was requested and notifies listeners. Callers performing long-running work
 * (resource acquisition, proof search) must poll {@link #isCancelled()} at
 * reasonable points and stop promptly; nothing here interrupts a running thread.
 */
public final class CheckCancellation {

    private boolean cancelled = false;
    private final List<Runnable> listeners = new ArrayList<Runnable>();

    /**
     * Requests cancellation. Idempotent: a second call has no effect. Registered
     * listeners are notified synchronously, in registration order.
     */
    public void cancel() {
        if (cancelled) {
            return;
        }
        cancelled = true;
        List<Runnable> toNotify = new ArrayList<Runnable>(listeners);
        for (int i = 0; i < toNotify.size(); i++) {
            toNotify.get(i).run();
        }
    }

    /** True if {@link #cancel()} has been called. */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Registers a listener to be invoked when cancellation occurs. If cancellation
     * has already occurred, the listener is invoked immediately, synchronously.
     *
     * @param listener the listener to invoke on cancellation
     * @return a {@link Runnable} that unregisters this listener when run; running
     *         it more than once has no additional effect
     */
    public Runnable onCancel(Runnable listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener must not be null");
        }
        listeners.add(listener);
        if (cancelled) {
            listener.run();
        }
        return new Unregister(listeners, listener);
    }

    private static final class Unregister implements Runnable {
        private final List<Runnable> listeners;
        private final Runnable listener;

        Unregister(List<Runnable> listeners, Runnable listener) {
            this.listeners = listeners;
            this.listener = listener;
        }

        @Override
        public void run() {
            listeners.remove(listener);
        }
    }
}
