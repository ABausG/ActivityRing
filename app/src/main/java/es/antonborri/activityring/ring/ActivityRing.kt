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
 */
class ActivityRing(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val paint: Paint
    private val arcPaint: Paint
    private val shadowPaint: Paint
    private val maskPaint: Paint
    private val targetPaint: Paint

    private var translateX = 0
    private var translateY = 0
    private var radius = 0
    private var rectangle = RectF()

    private val iconOffset = 2

    private var shadowBitmap: Bitmap? = null
    private var ringBitmap: Bitmap? = null
    private var maskBitmap: Bitmap? = null
    private var resultBitmap: Bitmap? = null

    var color: Int
    var strokeWidth = 24f
    var angle = 120f
    var drawable: Drawable? = null


    init {


        val density = resources.displayMetrics.density

        color = getThemeAccentColor()
        strokeWidth *= density

        if (attrs != null) {
            val a: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.ActivityRing)
            color = a.getColor(R.styleable.ActivityRing_color, color)
            strokeWidth = a.getDimension(R.styleable.ActivityRing_strokeWidth, strokeWidth)
            angle = a.getFloat(R.styleable.ActivityRing_angle, angle)
            val drawableIdAttr = a.getResourceId(R.styleable.ActivityRing_icon, 0)
            a.recycle()
            if (drawableIdAttr != 0) {
                drawable = VectorDrawableCompat.create(resources, drawableIdAttr, null)
            }
        }

        paint = Paint()
        paint.apply {
            color = this@ActivityRing.color
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeWidth = this@ActivityRing.strokeWidth
        }

        arcPaint = Paint()
        arcPaint.apply {
            color = this@ActivityRing.color
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeWidth = this@ActivityRing.strokeWidth
            alpha = 30
        }

        shadowPaint = Paint()
        shadowPaint.apply {
            color = ContextCompat.getColor(context, android.R.color.black)
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        maskPaint = Paint()
        maskPaint.apply {
            color = ContextCompat.getColor(context, android.R.color.white)
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeWidth = this@ActivityRing.strokeWidth
            isAntiAlias = true
        }

        targetPaint = Paint(Paint.ANTI_ALIAS_FLAG)
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
        val top = (height / 2 - arcDiameter / 2).toFloat()
        val left = (width / 2 - arcDiameter / 2).toFloat()
        rectangle.set(left, top, left + arcDiameter, top + arcDiameter)

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (resultBitmap == null) {
            resultBitmap = drawMaskedRing()
        }

        canvas.drawBitmap(resultBitmap, 0f, 0f, null)


        drawIcon(canvas)
    }


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

    private fun createShadow(): Bitmap {
        val bmp = Bitmap.createBitmap(2 * strokeWidth.toInt(), 2 * strokeWidth.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawCircle(strokeWidth, strokeWidth, strokeWidth / 2, shadowPaint)

        if (!isInEditMode) {
            blur(bmp)
        }

        return bmp
    }

    private fun createMask(): Bitmap {
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawArc(rectangle, 0f, 360f, false, maskPaint)
        return bmp
    }

    private fun drawRing(): Bitmap {
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        var startAngle = -90f

        //Draw Semi-Transparent Ring in Background
        canvas.drawArc(rectangle, -90f, 360f, false, arcPaint)


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

        //Draw Full Circle First if Angle is > 360
        if (angle >= 360f) {
            canvas.drawArc(rectangle, -90f, 360f, false, paint)
        }

        if (angle > 0f) {
            //Draw Shadow
            val shadowAngle = angle + 90
            val shadowRad = Math.toRadians(shadowAngle.toDouble())
            if (shadowBitmap == null) {
                shadowBitmap = createShadow()
            }
            canvas.drawBitmap(shadowBitmap, translateX - radius * Math.cos(shadowRad).toFloat(), translateY - radius * Math.sin(shadowRad).toFloat(), null)


            //Manipulate Angle to work Around Canvas drawing full Circles if angle > 360
            if (angle == 360f) {
                startAngle = 90f
                angle = 180f
            } else if (angle > 360f) {
                angle %= 360
                startAngle = angle - 180 - 90 //-180 for the drawn Offset and -90 for the general Offset
                angle = 180f //Set Sweep Angle to draw a half Circle to Cover up the whole shadow
            }


            //Draw Arc
            canvas.drawArc(rectangle, startAngle, angle, false, paint)
        }

        return bmp
    }

    private fun drawMaskedRing(): Bitmap {
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        val target = ringBitmap ?: drawRing()
        val mask = maskBitmap ?: createMask()

        canvas.drawBitmap(target, 0f, 0f, null)
        canvas.drawBitmap(mask, 0f, 0f, targetPaint)

        return bmp
    }

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
}