package com.example.guruandroidsdkexample

import ai.getguru.androidsdk.*
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Size
import android.view.View
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.math.sqrt

class SkeletonOverlayView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    var keypoints: Keypoints? = null
        set(value) {
            field = value
            // trigger a redraw each time that we receive new keypoints
            this.invalidate()
        }
    var analysis: Analysis? = null
    var previewAspectRatio: Float = 1.0f
    var imageAspectRatio: Float = 1.0f
    private var currentFps: Float? = null

    public override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        assert(top == 0)
        assert(left == 0)
        previewAspectRatio = right.toFloat() / bottom
    }

    public override fun onDraw(canvas: Canvas) {
        if (keypoints == null) {
            return
        }

        val isPersonVisible = keypoints!!.maxOf { it.score } > 0.5
        if (!isPersonVisible) {
            return
        }

        val dimensions = calculateTargetDimensions()

        drawSkeleton(
            canvas,
            keypoints!!,
            dimensions,
            color = Color.RED,
        )
        drawFps(canvas)
        drawActivityAndReps(canvas)
        if (SHOULD_DRAW_BBOX) {
            drawBbox(canvas, dimensions)
        }
    }

    private fun translateX(x: Double, dimensions: Size): Float {
        return ((1.0f - x) * dimensions.width).toFloat()
    }

    private fun translateY(y: Double, dimensions: Size): Float {
        return (y * dimensions.height).toFloat()
    }

    private fun calculateTargetDimensions(): Size {
        return if (previewAspectRatio < imageAspectRatio) {
            Size(right, (right.toFloat() / imageAspectRatio).toInt())
        } else {
            Size(bottom, (bottom.toFloat() / imageAspectRatio).toInt())
        }
    }

    private fun drawSkeleton(
        canvas: Canvas,
        keypoints: Keypoints,
        dimensions: Size,
        shouldDrawBones: Boolean = true,
        color: Int = Color.RED,
    ) {
        val alreadyDrawn = mutableSetOf<Keypoint>()
        keypoints.getPairs().forEach { pair ->
            listOf(pair.first, pair.second).forEach {
                if (!alreadyDrawn.contains(it)) {
                    drawKeypoint(canvas, it, dimensions, color=color)
                    alreadyDrawn.add(it)
                }
            }
            if (shouldDrawBones) {
                drawLine(canvas, pair.first, pair.second, dimensions)
            }
        }
    }

    private fun drawActivityAndReps(canvas: Canvas) {
        if (analysis?.movement == null) {
            return
        }
        var text = analysis!!.movement!!
        if (analysis?.reps != null) {
            text += ": ${analysis!!.reps!!.size} reps"
        }
        paint.color = Color.RED
        paint.textSize = 48f
        val x = alignTextHorizontally(text, canvas)
        canvas.drawText(text, x, .9f * canvas.height, paint)
    }

    private fun alignTextHorizontally(text: String, canvas: Canvas): Float {
        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)
        val midpointX = canvas.width / 2
        return midpointX - bounds.width() / 2f
    }

    private fun drawBbox(canvas: Canvas, dimensions: Size) {
        if (keypoints == null) {
            return
        }

        // calculate the person's bounding box from the previous frame
        val bbox = BoundingBox.fromPreviousFrame(keypoints!!) ?: return

        paint.color = Color.RED
        paint.strokeWidth = getKeypointRadius().toFloat()
        paint.style = Paint.Style.STROKE

        canvas.drawRect(
            translateX(bbox.x1.toDouble(), dimensions),
            translateY(bbox.y1.toDouble(), dimensions),
            translateX(bbox.x2.toDouble(), dimensions),
            translateY(bbox.y2.toDouble(), dimensions),
            paint
        )
    }

    private fun drawFps(canvas: Canvas) {
        if (currentFps != null) {
            val text = "%.1f".format(currentFps) + "fps"
            paint.style = Paint.Style.FILL_AND_STROKE
            paint.strokeWidth = 1f
            paint.color = Color.RED
            paint.textSize = 48f
            canvas.drawText(text, .025f * canvas.width, .05f * canvas.height, paint)
        }
    }

    private fun drawKeypoint(
        canvas: Canvas,
        k: Keypoint,
        dimensions: Size,
        color: Int = Color.RED,
    ) {
        if (k.score < MIN_SCORE_THRESHOLD) {
            return
        }
        paint.color = color
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.alpha = (255 * .75).toInt()

        val radius = getKeypointRadius()
        val x = translateX(k.x, dimensions)
        val y = translateY(k.y, dimensions)
        canvas.drawCircle(x, y, radius.toFloat(), paint)
    }

    private fun getKeypointRadius(): Int {
        val w = this.width
        val h = this.height
        val diagonalLength = sqrt(w.toDouble().pow(2) + h.toDouble().pow(2))
        return ceil(diagonalLength * .005).toInt()
    }

    private fun drawLine(canvas: Canvas, p1: Keypoint, p2: Keypoint, dimensions: Size) {
        if (p1.score < MIN_SCORE_THRESHOLD || p2.score < MIN_SCORE_THRESHOLD) {
            return
        }
        paint.color = Color.WHITE
        paint.alpha = (255 * .75).toInt()
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = getKeypointRadius().toFloat()

        canvas.drawLine(
            translateX(p1.x, dimensions),
            translateY(p1.y, dimensions),
            translateX(p2.x, dimensions),
            translateY(p2.y, dimensions),
            paint
        )
    }

    fun setCurrentFps(currentFps: Float?) {
        this.currentFps = currentFps
    }

    companion object {
        private val paint = Paint()
        private const val MIN_SCORE_THRESHOLD = .2
        private const val SHOULD_DRAW_BBOX = false
    }
}