package com.example.matwh.blehexpos.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.view.View;

/**
 * Class that represents a triangle. Filled with a colour and has a black outline.
 */
public class Triangle extends View {

    // Colour of triangle
    Paint paint;

    // Triangle path
    Path path;

    /**
     * Construct a triangle with three points and a colour.
     * @param context Application context.
     * @param points Three points of the triangle.
     * @param color Colour of the triangle.
     */
    public Triangle(Context context, Point[] points, int color) {
        super(context);
        init(points, color);
    }

    /* Initialise the paint and create the path for the triangle. */
    private void init(Point[] points, int color) {
        // Set paint
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);

        setPadding(50,50,50,50);
        setBackgroundColor(Color.TRANSPARENT);

        // Create path for triangle
        path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        // Draw triangle
        path.moveTo(points[0].x, points[0].y);
        path.lineTo(points[1].x,points[1].y);
        path.lineTo(points[2].x,points[2].y);
        path.lineTo(points[0].x,points[0].y);
        path.close();
    }

    /**
     * Draw the triangle with a black border.
     * @param canvas {@inheritDoc}
     */
    @Override
    protected void onDraw(Canvas canvas) {
        // Fill triangle
        paint.setStrokeWidth(2);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setAntiAlias(true);
        canvas.drawPath(path, paint);
        // Draw triangle outline
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLACK);
        canvas.drawPath(path, paint);
    }

    /**
     * Set the triangle colour.
     * @param c New colour for triangle.
     */
    public void setColor(int c) {
        paint.setColor(c);
        invalidate();
    }
}
