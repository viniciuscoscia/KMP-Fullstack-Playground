package com.viniciuscoscia.kmpfullstackplayground.osarchitecture

import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.thread

/**
 * Android Internals #2 — a hand-rolled miniature of [android.os.Looper] + `MessageQueue`.
 *
 * A plain [Thread] dies the moment its `run()` returns, which would make the main thread useless:
 * it has to stay alive for the whole lifetime of the process so it can keep drawing frames and
 * dispatching touch/key events. A **looper** is what keeps it alive — it parks the thread on a
 * queue and wakes up only when there is work to do.
 *
 * The real thing is `Looper.prepare()` (attach a `MessageQueue` to the current thread),
 * `Looper.loop()` (the infinite dequeue-and-dispatch loop) and a `Handler` (the front door you
 * `post()` work through). This class collapses all three into one type and carries [Runnable]s
 * instead of `Message`s, but the shape is the same:
 *
 * - the queue is **FIFO** — work is dispatched in the order it was posted;
 * - taking from the queue **blocks** rather than spins, so an idle thread costs ~0% CPU (the real
 *   `MessageQueue.next()` blocks in native `epoll` for exactly this reason);
 * - the loop only ends when something explicitly quits it.
 *
 */
class MyLooper {
    private var thread: Thread? = null
    private val queue = LinkedBlockingQueue<Runnable>()

    fun enqueue(block: Runnable) {
        if (thread == null) {
            createLooperThread()
        }
        queue.offer(block)
    }

    private fun createLooperThread() {
        thread = thread ?: thread {
            try {
                while (true) {
                    val block = queue.take()
                    block.run()
                }
            } catch (e: InterruptedException) {
                return@thread
            }
        }
    }

    fun quit() {
        thread?.interrupt()
        thread = null
    }
}
