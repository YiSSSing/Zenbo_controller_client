package com.example.zenbo_phonecontroller;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class RockerView extends View {
    private static final String TAG = "RockerView" ;

    private static final int DEFAULT_SIZE = 400 ,
                             DEFAULT_ROCKER_SIZE = DEFAULT_SIZE >> 3 ;

    private Paint rockerBackgroundPaint , rockerPaint ;
    private Point centerPoint , rockerPosition ;
    private int shakeableAreaRadius , rockerRadius ;

    private static final int ROCKER_MODE_PIC = 0 ,
                             ROCKER_MODE_COLOR  = 1 ,
                             ROCKER_MODE_XML = 2 ,
                             ROCKER_MODE_DEFAULT = 3 ;
    private int rockerMode = ROCKER_MODE_DEFAULT ;
    private Bitmap rockerBitmap ;
    private int rockerColor ;

    private static final int ROCKER_BACKGROUND_MODE_PIC = 4 ,
                             ROCKER_BACKGROUND_MODE_COLOR = 5 ,
                             ROCKER_BACKGROUND_MODE_XML = 6 ,
                             ROCKER_BACKGROUND_MODE_DEFAULT = 7 ;
    private int rockerBackgroundMode = ROCKER_BACKGROUND_MODE_DEFAULT ;
    private Bitmap rockerBackgroundBitmap ;
    private int rockerBackgroundColor ;

    private OnShakeListener userOnShake ;

    public RockerView (Context context , AttributeSet attrs) {
        super(context , attrs) ;
        initAttribute(context,attrs) ;

        rockerPaint = new Paint() ;
        rockerPaint.setAntiAlias(true);

        rockerBackgroundPaint = new Paint() ;
        rockerBackgroundPaint.setAntiAlias(true);

        centerPoint = new Point() ;
        rockerPosition = new Point() ;
    }

    private void initAttribute(Context context , AttributeSet attr) {
        TypedArray ta = context.obtainStyledAttributes(attr,R.styleable.RockerView) ;

        //draw center rocker
        Drawable rockerStyle = ta.getDrawable(R.styleable.RockerView_rockerStyle) ;
        if ( rockerStyle != null ) {
            if ( rockerStyle instanceof BitmapDrawable ) {
                rockerBitmap = ((BitmapDrawable) rockerStyle).getBitmap() ;
                rockerMode = ROCKER_MODE_PIC ;
            }else if ( rockerStyle instanceof GradientDrawable ) {
                rockerBitmap = drawableToBitmap(rockerStyle) ;
                rockerMode = ROCKER_MODE_XML ;
            }else if ( rockerStyle instanceof ColorDrawable) {
                rockerColor = ((ColorDrawable) rockerStyle).getColor() ;
                rockerMode = ROCKER_MODE_COLOR ;
            }else rockerMode = ROCKER_MODE_DEFAULT ;
        }else rockerMode = ROCKER_MODE_DEFAULT ;

        //draw rocker view background
        Drawable viewBackground = ta.getDrawable(R.styleable.RockerView_viewBackground) ;
        if ( viewBackground != null ) {
            if ( viewBackground instanceof BitmapDrawable ) {
                rockerBackgroundBitmap = ((BitmapDrawable) viewBackground).getBitmap() ;
                rockerBackgroundMode = ROCKER_BACKGROUND_MODE_PIC ;
            }else if ( viewBackground instanceof GradientDrawable) {
                rockerBackgroundBitmap = drawableToBitmap(viewBackground) ;
                rockerBackgroundMode = ROCKER_BACKGROUND_MODE_XML ;
            }else if ( viewBackground instanceof ColorDrawable ) {
                rockerBackgroundColor = ((ColorDrawable) viewBackground).getColor() ;
                rockerBackgroundMode = ROCKER_BACKGROUND_MODE_COLOR ;
            }else rockerBackgroundMode = ROCKER_BACKGROUND_MODE_DEFAULT ;
        }else rockerBackgroundMode = ROCKER_BACKGROUND_MODE_DEFAULT ;

        shakeableAreaRadius = ta.getDimensionPixelOffset(R.styleable.RockerView_viewRadius, DEFAULT_SIZE) ;
        rockerRadius = ta.getDimensionPixelOffset(R.styleable.RockerView_rockerRadius, DEFAULT_ROCKER_SIZE) ;
        ta.recycle(); ;

    }

    private Bitmap drawableToBitmap ( Drawable d ) {
        int width = d.getIntrinsicWidth() , height = d.getIntrinsicHeight() ;
        Bitmap.Config con = d.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565 ;
        Bitmap bitmap = Bitmap.createBitmap(width,height,con) ;
        Canvas c = new Canvas(bitmap) ;
        d.setBounds(0,0,width,height);
        d.draw(c) ;
        return bitmap ;
    }

    @Override
    protected void onMeasure( int widthSrc , int heightSrc ) {
        //EXACTLY = match parent or a real value
        int width = MeasureSpec.getMode(widthSrc) == MeasureSpec.EXACTLY ? MeasureSpec.getSize(widthSrc) : DEFAULT_SIZE ;
        int height = MeasureSpec.getMode(heightSrc) == MeasureSpec.EXACTLY ? MeasureSpec.getSize(heightSrc) : DEFAULT_SIZE ;
        setMeasuredDimension(width,height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getMeasuredWidth() , height = getMeasuredHeight() ;
        int cx = width >> 1 , cy = height >> 1 ;

        centerPoint.set(cx,cy) ;
        //if rocker view is not a circle , only support shaking on the short radius circle
        shakeableAreaRadius = width < height ? cx : cy ;
        if ( rockerPosition.x == 0 || rockerPosition.y == 0 ) rockerPosition.set(centerPoint.x,centerPoint.y) ;

        Rect src , dst ;
        //shakeable area
        switch ( rockerBackgroundMode ) {
            case ROCKER_BACKGROUND_MODE_PIC :
                src = new Rect(0,0,rockerBackgroundBitmap.getWidth(),rockerBackgroundBitmap.getHeight()) ;
                dst = new Rect(centerPoint.x - shakeableAreaRadius , centerPoint.y - shakeableAreaRadius ,
                               centerPoint.x + shakeableAreaRadius , centerPoint.y + shakeableAreaRadius) ;
                canvas.drawBitmap(rockerBackgroundBitmap,src,dst,rockerBackgroundPaint) ;
                break ;
            case ROCKER_BACKGROUND_MODE_XML :
                src = new Rect(0,0,rockerBackgroundBitmap.getWidth(),rockerBackgroundBitmap.getHeight()) ;
                dst = new Rect(centerPoint.x - shakeableAreaRadius , centerPoint.y - shakeableAreaRadius ,
                        centerPoint.x + shakeableAreaRadius , centerPoint.y + shakeableAreaRadius) ;
                canvas.drawBitmap(rockerBackgroundBitmap,src,dst,rockerBackgroundPaint) ;
                break ;
            case ROCKER_BACKGROUND_MODE_COLOR :
                rockerBackgroundPaint.setColor(rockerBackgroundColor) ;
                canvas.drawCircle(centerPoint.x,centerPoint.y,shakeableAreaRadius,rockerBackgroundPaint) ;
                break ;
            default :
                rockerBackgroundPaint.setColor(Color.LTGRAY) ;
                canvas.drawCircle(centerPoint.x,centerPoint.y,shakeableAreaRadius,rockerBackgroundPaint) ;
                break ;
        }

        //draw rocker
        switch ( rockerMode ) {
            case ROCKER_MODE_PIC :
                src = new Rect(0,0,rockerBitmap.getWidth(),rockerBitmap.getHeight()) ;
                dst = new Rect(rockerPosition.x - rockerRadius , rockerPosition.y - rockerRadius ,
                               rockerPosition.x + rockerRadius , rockerPosition.y + rockerRadius) ;
                canvas.drawBitmap(rockerBitmap , src , dst , rockerPaint );
                break ;
            case ROCKER_MODE_XML :
                src = new Rect(0,0,rockerBitmap.getWidth(),rockerBitmap.getHeight()) ;
                dst = new Rect(rockerPosition.x - rockerRadius , rockerPosition.y - rockerRadius ,
                        rockerPosition.x + rockerRadius , rockerPosition.y + rockerRadius) ;
                canvas.drawBitmap(rockerBitmap , src , dst , rockerPaint );
                break ;
            case ROCKER_MODE_COLOR :
                rockerPaint.setColor(rockerColor) ;
                canvas.drawCircle(rockerPosition.x,rockerPosition.y,rockerRadius,rockerPaint);
                break ;
            default :
                rockerPaint.setColor(Color.BLACK) ;
                canvas.drawCircle(rockerPosition.x,rockerPosition.y,rockerRadius,rockerPaint);
                break ;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch ( e.getAction() ) {
            case MotionEvent.ACTION_DOWN :             //click down
                callBackStart() ;
                break ;
            case MotionEvent.ACTION_MOVE :             //shaking
                float x = e.getX() , y = e.getY() ;
                rockerPosition = getRockerPosition(centerPoint , new Point((int)x,(int)y),shakeableAreaRadius,rockerRadius) ;
                moveRocker(rockerPosition.x,rockerPosition.y) ;
                break ;
            case MotionEvent.ACTION_UP :                //hand off the rocker
                moveRocker(centerPoint.x,centerPoint.y) ;
                callBackFinish() ;
                break ;
            case MotionEvent.ACTION_CANCEL :
                moveRocker(centerPoint.x,centerPoint.y) ;
                callBackFinish() ;
                break ;
            default :
                Log.e("RockerView ERROR :: " , "Undefined motion event detected") ;
                return false ;
        }
        return true ;
    }

    private Point getRockerPosition(Point centerPoint , Point touching , double shakeableRadius , double rockerRadius) {
        double x = touching.x - centerPoint.x , y = touching.y - centerPoint.y ;
        double distance = Math.sqrt( x*x + y*y ) ;

        double rad = Math.acos(x/distance)  * (touching.y <= centerPoint.y ? 1 : -1 ) ;
        double angle = Math.round(rad / Math.PI * 180 ) ;
        angle = ( angle >= 0 ? angle : 360 + angle ) ;
        callBack(angle,distance) ;

        if ( distance + rockerRadius <= shakeableRadius ) return touching ;
        else return new Point( (int)(centerPoint.x + (shakeableRadius - rockerRadius)*Math.cos(rad)) , (int)(centerPoint.y + (shakeableRadius - rockerRadius)*Math.sin(rad*(-1))) ) ;
    }

    private void moveRocker(double x , double y) {
        rockerPosition.set( (int)x , (int)y ) ;
        invalidate() ;
    }

    private void callBackStart() { userOnShake.onStart() ; }

    private void callBack(double angle , double distance) { userOnShake.onShake(angle,distance) ; }

    private void callBackFinish() { userOnShake.onFinish() ; }

    public void setOnShakeListener(OnShakeListener function) { userOnShake = function ; }

    public interface OnShakeListener {
        void onStart() ;

        void onShake(double angle , double distance) ;

        void onFinish() ;
    }

}
