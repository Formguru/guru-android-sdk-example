package com.example.guruandroidsdkexample

class FpsGauge {

    companion object {
        const val CAPACITY = 25
    }
    private val dq: ArrayDeque<Long> = ArrayDeque(CAPACITY)

    fun onFrameFinish() {
        dq.addLast(System.currentTimeMillis())
        while (dq.size > CAPACITY) {
            dq.removeFirst()
        }
    }

    fun currentFps(): Float? {
        if (dq.size < 2) {
            return null
        }
        val durationMs = dq.last() - dq.first()
        val n = dq.size - 1
        return n / (durationMs / 1000.0f)
    }
}