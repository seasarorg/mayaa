import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean

final cacheSize = 128  // Mayaaの実装と同じ
final threadsCount = 200
final trialCount = 100
def lock = new Object()
def foundNSEE = new AtomicBoolean(false)
def l = new LinkedList()

for (int trialId = 0; trialId < trialCount && !foundNSEE.get(); trialId++) {
    def latch = new CountDownLatch(threadsCount)
    threads = (1..threadsCount).collect { requestId ->
        def id = "[$trialId]$requestId"
        Thread.start {
            try {
                // 一斉に動いた方がぶつかりやすいので待ち合わせる
                latch.countDown()
                latch.await()

                synchronized (lock) {
                    l.addFirst("ITEM:$id") // addFirstは同期的にしか実行されないため、同じように同期化する
                }
                if (l.size() > cacheSize) {
                    l.removeLast() // これが複数同時に実行された場合、が問題
                }
            } catch (NoSuchElementException e) {
                println id
                e.printStackTrace()
                foundNSEE.set(true)
            }
        }
    }
    threads*.join()
}

if (!foundNSEE.get()) {
    println "Not reproduced (no error)."
    System.exit(0)
}

// (厳密には壊れ方にもよるが)1回でも発生すると、あとはなんどやってもエラーになる(場合がある)
def verifyTrialCount = 100
def nseeCount = 0
verifyTrialCount.times {
    l.addFirst("DUMMY")
    assert l.size() > 0
    try {
        l.removeLast()
    } catch (NoSuchElementException e) {
        nseeCount++
    }
}
if (nseeCount == verifyTrialCount) {
    println "Reproduced!"
} else {
    println "Not reproduced (error occurs but it works later)."
}
