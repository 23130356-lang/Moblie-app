package com.example.moblie_app.ui.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;

public class RingProgressView extends View {

    private final RectF arcRect = new RectF();
    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint centerValuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint centerUnitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private int ringColor = Color.parseColor("#10B981");
    private float progressFraction = 0f;
    private float animatedFraction = 0f;
    private String centerValueText = "0%";
    private String centerUnitText = "";

    private ValueAnimator animator;

    public RingProgressView(Context context) {
        super(context);
        init();
    }

    public RingProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        bgPaint.setStyle(Paint.Style.STROKE);
        bgPaint.setStrokeCap(Paint.Cap.ROUND);

        fgPaint.setStyle(Paint.Style.STROKE);
        fgPaint.setStrokeCap(Paint.Cap.ROUND);

        centerValuePaint.setColor(Color.parseColor("#0D1F1A"));
        centerValuePaint.setTextAlign(Paint.Align.CENTER);
        centerValuePaint.setFakeBoldText(true);

        centerUnitPaint.setColor(Color.parseColor("#526B60"));
        centerUnitPaint.setTextAlign(Paint.Align.CENTER);
    }

    /**
     * @param ringColorHex màu chủ đạo (ví dụ "#E67E22" cho calo)
     */
    public void setRingColor(String ringColorHex) {
        this.ringColor = Color.parseColor(ringColorHex);
        invalidate();
    }

    /**
     * Cập nhật tiến độ và animate mượt từ giá trị hiện tại.
     * @param current giá trị hiện tại
     * @param target  mục tiêu (nếu <= 0 sẽ coi như chưa có mục tiêu)
     * @param valueLabel chuỗi hiển thị giữa vòng tròn, ví dụ "1.480"
     * @param unitLabel  đơn vị nhỏ bên dưới, ví dụ "/2000 kcal"
     */
    public void setProgress(double current, double target, String valueLabel, String unitLabel) {
        float fraction = target > 0 ? (float) (current / target) : 0f;
        float clamped = Math.max(0f, Math.min(1f, fraction));

        this.centerValueText = valueLabel;
        this.centerUnitText = unitLabel;

        if (animator != null) animator.cancel();
        animator = ValueAnimator.ofFloat(animatedFraction, clamped);
        animator.setDuration(700);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(a -> {
            animatedFraction = (float) a.getAnimatedValue();
            progressFraction = clamped;
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float w = getWidth();
        float h = getHeight();
        float diameter = Math.min(w, h);
        float strokeWidth = diameter * 0.12f;

        bgPaint.setStrokeWidth(strokeWidth);
        fgPaint.setStrokeWidth(strokeWidth);

        // Vòng nền: cùng màu chủ đạo nhưng rất nhạt
        bgPaint.setColor(ColorUtils.setAlphaComponent(ringColor, 38));

        float inset = strokeWidth / 2f + 2f;
        arcRect.set(
                (w - diameter) / 2f + inset,
                (h - diameter) / 2f + inset,
                (w - diameter) / 2f + diameter - inset,
                (h - diameter) / 2f + diameter - inset
        );

        canvas.drawArc(arcRect, 0, 360, false, bgPaint);

        // Gradient đậm dần: từ màu nhạt -> màu chủ đạo theo chiều tiến độ ("đậm nhạt")
        int lightShade = ColorUtils.blendARGB(ringColor, Color.WHITE, 0.65f);
        SweepGradient gradient = new SweepGradient(
                arcRect.centerX(), arcRect.centerY(),
                new int[]{lightShade, ringColor, ringColor},
                new float[]{0f, 0.85f, 1f}
        );
        fgPaint.setShader(gradient);

        float sweep = 360f * animatedFraction;
        canvas.save();
        // Quay để gradient bắt đầu nhạt từ điểm xuất phát (12 giờ)
        canvas.rotate(-90, arcRect.centerX(), arcRect.centerY());
        canvas.drawArc(arcRect, 0, sweep, false, fgPaint);
        canvas.restore();

        // Text giữa vòng tròn
        centerValuePaint.setTextSize(diameter * 0.20f);
        centerUnitPaint.setTextSize(diameter * 0.105f);

        float centerX = arcRect.centerX();
        float centerY = arcRect.centerY();

        Paint.FontMetrics fm = centerValuePaint.getFontMetrics();
        float valueBaseline = centerY - (fm.ascent + fm.descent) / 2f - diameter * 0.06f;
        canvas.drawText(centerValueText, centerX, valueBaseline, centerValuePaint);

        if (centerUnitText != null && !centerUnitText.isEmpty()) {
            canvas.drawText(centerUnitText, centerX, valueBaseline + diameter * 0.16f, centerUnitPaint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int size = Math.min(getMeasuredWidth(), getMeasuredHeight());
        setMeasuredDimension(size, size);
    }
}