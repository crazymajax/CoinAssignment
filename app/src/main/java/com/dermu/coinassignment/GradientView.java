package com.dermu.coinassignment;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

/**
 * A custom view that displays a nice gradient in the background
 * This could also be done with shape xml drawable but I wanted to show my canvas capabilities.
 * Created by Francois on 9/5/2015.
 */
public class GradientView extends View {

    Paint blueGradientPaint = new Paint();
    float viewWidth = 100f;
    float viewHeight = 100f;
    LinearGradient fadeToBlackGradient;

    public GradientView(Context context) {
        super(context);
    }

    public GradientView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GradientView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld){
        super.onSizeChanged(xNew, yNew, xOld, yOld);

        viewWidth = xNew;
        viewHeight = yNew;
        int darkBlue = Color.parseColor("#242c3e"); //1a2044 //1a202d
        int lightBlue = Color.parseColor("#384050"); //333d4f //384050 //2c3444 //272f41 //3c3e4f //424558 //262b4d //262b33
        float oneThird = viewHeight * (1f/3f);
        float twoThirds = oneThird * 2f;
        fadeToBlackGradient = new LinearGradient(
                0, 0,
                0, twoThirds,
                darkBlue, lightBlue, Shader.TileMode.MIRROR);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = canvas.getWidth();
        int height = canvas.getHeight();
        blueGradientPaint.setShader(fadeToBlackGradient);
        canvas.drawRect(0, 0,
                width, height,
                blueGradientPaint);
    }
}
