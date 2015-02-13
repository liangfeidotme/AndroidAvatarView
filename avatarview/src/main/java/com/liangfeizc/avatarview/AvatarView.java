package com.liangfeizc.avatarview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by liangfei on 2/12/15.
 */
public class AvatarView extends ImageView {
    public static final String TAG = "AvatarView";

    private int mBorderWidth = 2;
    private int mShadowWidth = 2;

    private int mBorderColor = Color.WHITE;
    private int mShadowColor = Color.RED;

    private RectF mBorderRect;
    private RectF mBitmapRect;

    private float mBorderRadius;
    private float mBitmapRadius;

    private BitmapShader mBitmapShader;
    private Bitmap mBitmap;

    private Matrix mShaderMatrix;
    private Paint mBitmapPaint;
    private Paint mBorderPaint;

    public AvatarView(Context context) {
        super(context);
    }

    public AvatarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AvatarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AvatarView);
        try {
            mBorderWidth = a.getDimensionPixelSize(R.styleable.AvatarView_border_width, mBorderWidth);
            mBorderColor = a.getColor(R.styleable.AvatarView_border_color, mBorderColor);

            mShadowWidth = a.getDimensionPixelSize(R.styleable.AvatarView_shadow_width, mShadowWidth);
            mShadowColor = a.getColor(R.styleable.AvatarView_shadow_color, mShadowColor);
        } finally {
            a.recycle();
        }

        init();
    }

    private void init() {
        super.setScaleType(ScaleType.CENTER_CROP);
        mShaderMatrix = new Matrix();

        mBorderPaint = new Paint();
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setColor(mBorderColor);
        mBorderPaint.setStrokeWidth(mBorderWidth);
        mBorderPaint.setStyle(Paint.Style.STROKE);

        mBitmapPaint = new Paint();
        mBitmapPaint.setAntiAlias(true);

        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    public void onDraw(@NonNull Canvas canvas) {
        if (mBitmap == null) return;

        initBorder();
        initBitmap();

        int cx = getWidth() >> 1;
        int cy = getHeight() >> 1;

        canvas.drawCircle(cx, cy, mBorderRadius, mBorderPaint);
        canvas.drawCircle(cx, cy, mBitmapRadius, mBitmapPaint);
    }

    private void initBorder() {
        mBorderRect = new RectF(mShadowWidth, mShadowWidth, getWidth() - mShadowWidth, getHeight() - mShadowWidth);
        mBorderRadius = Math.min((mBorderRect.width() - mShadowWidth) / 2, (mBorderRect.height() - mShadowWidth) / 2);
        mBorderPaint.setShadowLayer(mShadowWidth, 0, 0, mShadowColor);
    }

    private void initBitmap() {
        mBitmapShader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        mBitmapPaint.setShader(mBitmapShader);

        int outerWidth = mBorderWidth + mShadowWidth;
        mBitmapRect = new RectF(outerWidth, outerWidth, getWidth() - outerWidth, getHeight()- outerWidth);
        mBitmapRadius = Math.min(mBitmapRect.width() / 2, mBitmapRect.height() / 2);
        updateShaderMatrix();
    }

    private Bitmap getBitmapFrom(Drawable drawable) {
        if (drawable == null) return null;
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        try {
            Bitmap bitmap;
            if (drawable instanceof ColorDrawable) {
                bitmap = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888);
            } else {
                bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            }

            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (OutOfMemoryError e) {
            return null;
        }
    }
    
    private void updateShaderMatrix() {
        float scale;
        float dx = 0;
        float dy = 0;

        mShaderMatrix.set(null);

        int bmpWidth = mBitmap.getWidth();
        int bmpHeight = mBitmap.getHeight();

        if (bmpWidth * mBitmapRect.height() > mBitmapRect.width() * bmpHeight) {
            scale = mBitmapRect.height() / (float) bmpHeight;
            dx = (mBitmapRect.width() - bmpWidth * scale) * 0.5f;
        } else {
            scale = mBitmapRect.width() / (float) bmpWidth;
            dy = (mBitmapRect.height() - bmpHeight * scale) * 0.5f;
        }

        mShaderMatrix.setScale(scale, scale);
        mShaderMatrix.postTranslate(
                (int) (dx + 0.5f) + mBorderWidth + mShadowWidth,
                (int) (dy + 0.5f) + mBorderWidth + mShadowWidth);

        mBitmapShader.setLocalMatrix(mShaderMatrix);
    }

    @Override
    public void setImageBitmap(Bitmap bitmap) {
        super.setImageBitmap(bitmap);
        mBitmap = bitmap;
        invalidate();
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        mBitmap = getBitmapFrom(drawable);
        invalidate();
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        mBitmap = getBitmapFrom(getDrawable());
        invalidate();
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        mBitmap = getBitmapFrom(getDrawable());
        invalidate();
    }
}
