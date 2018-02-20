package es.antonborri.activityring.ring

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import es.antonborri.activityring.R


/**
 * Created by Anton on 18/02/2018.
 *
 * Displays a Progress Ring
 */
class ActivityRing(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val paint: Paint = Paint()
    private val arcPaint: Paint = Paint()
    private val shadowPaint: Paint = Paint()
    private val maskPaint: Paint = Paint()
    private val targetPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var translateX = 0
    private var translateY = 0
    private var radius = 0
    private var rectangle = RectF()

    private val iconOffset = 2

    private var shadowBitmap: Bitmap? = null
    private var ringBitmap: Bitmap? = null
    private var maskBitmap: Bitmap? = null
    private var resultBitmap: Bitmap? = null

    var color: Int = getThemeAccentColor()
        set(value) {
            field = value
            updatePaint()
            updateArcPaint()
            redrawRing()
        }
    var emptyOpacity = 0.15f
        set(value) {
            field = value
            updateArcPaint()
            redrawRing()
        }
    var strokeWidth = 32f * resources.displayMetrics.density
        set(value) {
            field = value
            updatePaint()
            updateMaskPaint()
            updateArcPaint()
            redrawRing()
        }
    var iconColor: Int = ContextCompat.getColor(context, android.R.color.black)
        set(value) {
            field = value
            invalidate()
        }
    var progress = 0f
        set(value) {
            field = value
            redrawRing()
        }

    var drawable: Drawable? = null
        set(value) {
            field = value
            invalidate()
        }


    init {

        if (attrs != null) {
            val a: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.ActivityRing)
            color = a.getColor(R.styleable.ActivityRing_color, color)
            emptyOpacity = a.getFloat(R.styleable.ActivityRing_emptyOpacity, 0.15f)
            strokeWidth = a.getDimension(R.styleable.ActivityRing_strokeWidth, strokeWidth)
            progress = a.getFloat(R.styleable.ActivityRing_progress, 0f)
            iconColor = a.getColor(R.styleable.ActivityRing_iconColor, iconColor)
            val drawableIdAttr = a.getResourceId(R.styleable.ActivityRing_icon, 0)
            a.recycle()
            if (drawableIdAttr != 0) {
                drawable = VectorDrawableCompat.create(resources, drawableIdAttr, null)
            }
        }


        updatePaint()
        updateMaskPaint()
        updateArcPaint()

        shadowPaint.apply {
            color = iconColor
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        targetPaint.apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        }

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = View.getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        val height = View.getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)
        val min = Math.min(width, height)

        translateX = (width * 0.5f - strokeWidth).toInt()
        translateY = (height * 0.5f - strokeWidth).toInt()

        val arcDiameter = min - paddingLeft - strokeWidth
        radius = (arcDiameter / 2).toInt()
        val top = height / 2 - arcDiameter / 2
        val left = width / 2 - arcDiameter / 2
        rectangle.set(left, top, left + arcDiameter, top + arcDiameter)

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)


        //Mask the Ring
        if (resultBitmap == null) {
            resultBitmap = createMaskedRing()
        }

        canvas.drawBitmap(resultBitmap, 0f, 0f, null)

        //Draw Icon as Last Step
        drawIcon(canvas)
    }

    /**
     * Returns Accent Color of Theme
     * @return Color value of themes Accent Color
     */
    private fun getThemeAccentColor(): Int {
        val colorAttr: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            android.R.attr.colorAccent
        } else {
            //Get colorAccent defined for AppCompat
            context.resources.getIdentifier("colorAccent", "attr", context.packageName)
        }
        val outValue = TypedValue()
        context.theme.resolveAttribute(colorAttr, outValue, true)
        return outValue.data
    }

    /**
     * Creates a black Circle as Shadow
     */
    private fun createShadow(): Bitmap {
        val bmp = Bitmap.createBitmap(2 * strokeWidth.toInt(), 2 * strokeWidth.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawCircle(strokeWidth, strokeWidth, strokeWidth / 2, shadowPaint)

        if (!isInEditMode) {
            blur(bmp)
        }

        return bmp
    }

    /**
     * Blurs a Bitmap
     * Used for creating the Shadow
     */
    private fun blur(bitmap: Bitmap) {
        val rs = RenderScript.create(context)
        val blur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
        val input = Allocation.createFromBitmap(rs, bitmap)
        val output = Allocation.createTyped(rs, input.type)

        blur.setRadius(25f)
        blur.setInput(input)
        blur.forEach(output)
        output.copyTo(bitmap)
        input.destroy()
        output.destroy()
    }

    /**
     * Draws a white Circle to Later Mask away Bleeding Shadows
     */
    private fun createMask(): Bitmap {
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawArc(rectangle, 0f, 360f, false, maskPaint)

        resultBitmap = null

        return bmp
    }

    /**
     * Draws the Ring with Shadow
     */
    private fun createRing(): Bitmap {
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val startAngle: Float
        var angle = progress * 360


        //Draw Semi-Transparent Ring in Background
        canvas.drawArc(rectangle, 0f, 360f, false, arcPaint)

        //If not a full Circle Draw a Shadow below start of the arc
        if (angle < 360) {
            //Shadow for Beginning of Arc
            if (shadowBitmap == null) {
                shadowBitmap = createShadow()
            }
            canvas.drawBitmap(shadowBitmap, width / 2 - strokeWidth, (paddingTop - strokeWidth) / 2, null)
        }

        //Draw Circle when the angle is 0
        if (angle == 0f) {
            paint.style = Paint.Style.FILL
            canvas.drawCircle((width / 2).toFloat(), (strokeWidth + paddingTop) / 2, strokeWidth / 2, paint)
            paint.style = Paint.Style.STROKE
        }

        if (angle > 0f) {

            //Draws Shadow under tip of Arc
            if(angle <= 270){
                drawEndShadow(angle, canvas)
            }

            //Draw Arc
            //-90f as start to start ring on Top
            canvas.drawArc(rectangle, -90f, angle, false, paint)


            //Draw a half Arc to Properly display the Shadow if the Angle is above 270
            if(angle > 270) {
                drawEndShadow(angle, canvas)
                angle %= 360
                startAngle = angle - 180 - 90
                angle = 180f
                canvas.drawArc(rectangle, startAngle, angle, false, paint)
            }

        }

        resultBitmap = null

        return bmp
    }

    /**
     * Draws Shadow Bitmap at Position of Circles End
     */
    private fun drawEndShadow(angle: Float, canvas: Canvas){
        //Draw Shadow
        val shadowAngle = angle + 90
        val shadowRad = Math.toRadians(shadowAngle.toDouble())
        if (shadowBitmap == null) {
            shadowBitmap = createShadow()
        }
        canvas.drawBitmap(shadowBitmap, translateX - radius * Math.cos(shadowRad).toFloat(), translateY - radius * Math.sin(shadowRad).toFloat(), null)
    }

    /**
     * Masks the Ring
     * (Removes the Bleeding Shadow)
     */
    private fun createMaskedRing(): Bitmap {
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        val target = ringBitmap ?: createRing()
        val mask = maskBitmap ?: createMask()

        canvas.drawBitmap(target, 0f, 0f, null)
        canvas.drawBitmap(mask, 0f, 0f, targetPaint)

        return bmp
    }

    /**
     * Draws the icon on the Canvas
     */
    private fun drawIcon(canvas: Canvas) {
        val size = strokeWidth.toInt() - iconOffset
        val top = ((strokeWidth + paddingTop - size) / 2).toInt()
        if (drawable != null) {
            DrawableCompat.setTint(drawable!!, shadowPaint.color)
            drawable!!.apply {
                setBounds((width - size) / 2, top, (width + size) / 2, top + size)
                draw(canvas)
            }
        }
    }

    private fun redrawRing() {
        if (width >0 && height > 0) {
            ringBitmap = createRing()
            invalidate()
        }
    }

    /**
     * Updates the Paint Object for Drawing the Main Part of the Ring
     */
    private fun updatePaint() {
        paint.apply {
            color = this@ActivityRing.color
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeWidth = this@ActivityRing.strokeWidth
        }
    }

    /**
     * Updates the Paint Object for Drawing the mask.
     * Called when updating the Stroke width
     */
    private fun updateMaskPaint() {
        maskPaint.apply {
            color = ContextCompat.getColor(context, android.R.color.white)
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeWidth = this@ActivityRing.strokeWidth
            isAntiAlias = true
        }
    }

    /**
     * Updates the Paint Object for Drawing the always Present Background Circle
     */
    private fun updateArcPaint() {
        arcPaint.apply {
            color = this@ActivityRing.color
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeWidth = this@ActivityRing.strokeWidth
            alpha = (255 * this@ActivityRing.emptyOpacity).toInt()
        }
    }
}