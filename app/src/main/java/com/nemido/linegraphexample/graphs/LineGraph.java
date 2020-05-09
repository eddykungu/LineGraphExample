package com.nemido.linegraphexample.graphs;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.nemido.linegraphexample.R;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LineGraph extends View {
    private float height, width;
    private final float paddingTop = getPaddingTop();
    private final float paddingBottom = getPaddingBottom();
    private final float paddingStart = getPaddingStart();
    private final float paddingEnd = getPaddingEnd();
    private Paint xAxisPaint, guidesPaint, textPaint, linePaint, circlePaint, activeCirclePaint;
    private List<Paint> circlePaints, linePaints;
    private Context context;
    private AttributeSet attrs;
    private int defStyleAttr;
    List<List<GraphPoints>> all;
    List<String> labels;

    public LineGraph(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public LineGraph(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.attrs = attrs;
        init();
    }

    public LineGraph(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        this.attrs = attrs;
        this.defStyleAttr = defStyleAttr;
        init();
    }

    /**
     * Initializes constant values
     */
    private void init() {
        String[] colors = {"#0000FF", "#FF0000", "#00FF00", "#800080", "#80604D", "#F05E23", "#FFFF00", "#00FFFF", "#FF00FF", "#FFCBA4"};
        int capacity = 10;
        circlePaints = new ArrayList<>(capacity);
        linePaints = new ArrayList<>(capacity);
        for (int i = 0; i < capacity; ++i){
            Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            linePaint.setColor(Color.parseColor(colors[i]));
            linePaint.setStrokeWidth(3.5f);
            linePaints.add(linePaint);
            circlePaint.setColor(Color.parseColor(colors[i]));
            circlePaints.add(circlePaint);
        }

        guidesPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        guidesPaint.setColor(Color.LTGRAY);
        guidesPaint.setStrokeWidth(1.5f);

        xAxisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        xAxisPaint.setColor(Color.BLACK);
        xAxisPaint.setStrokeWidth(3.5f);

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.BLUE);
        linePaint.setStrokeWidth(2.5f);

        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(Color.BLUE);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(Color.LTGRAY);
        textPaint.setTextSize(20f);

        all = new ArrayList<>();
        labels = new ArrayList<>();
        invalidate();
    }

    public void setAll(List<List<GraphPoints>> all) {
        this.all = all;
        invalidate();
    }

    /**
     * adds a list of graph points to the list of lists of graph points
     * @param data list to be added
     */
    public void add(List<GraphPoints> data) {
        all.add(data);
        invalidate();
    }

    /**
     * Adds two arrays into a list of lists of graph points
     * @param x x-axis values
     * @param y y-axis values
     * @param label legend label
     */
    public void add(float[] x, float[] y, String label) {
        List<GraphPoints> data = new ArrayList<>();
        for (int i = 0; i < x.length; ++i) {
            data.add(new GraphPoints(x[i], y[i]));
        }
        all.add(data);
        labels.add(label);
        invalidate();
    }

    /**
     * sorts an array with respect to x axis values
     * @param data array to be sorted
     */
    private void sort(List<GraphPoints> data) {
        for (int i = 0; i < data.size(); i++) {
            GraphPoints temp;
            for (int j = 0; j < data.size() - 1; j++) {
                if (data.get(j).getX() > data.get(j + 1).getX()) {
                    temp = data.get(j);
                    data.set(j, data.get(j + 1));
                    data.set(j + 1, temp);
                }
            }
        }
    }

    /**
     * Compiles the list provided into an array of type float that'll be used to plot the line
     * @param data a list of plots
     * @return an array of float points with the corresponding values
     */
    private float[] compileArray(List<GraphPoints> data) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            data.sort(Comparator.comparing(GraphPoints::getX));
        else sort(data);
        float[] a = new float[data.size() * 4];
        for (int i = 0; i < data.size() - 1; ++i) {
            a[(i * 4)] = data.get(i).getX();
            a[(i * 4) + 1] = data.get(i).getY();
            a[(i * 4) + 2] = data.get(i + 1).getX();
            a[(i * 4) + 3] = data.get(i + 1).getY();
        }
        return a;
    }

    private float maxX(List<GraphPoints> data) {
        float max = 0;
        for (GraphPoints p : data) {
            max = Math.max(max, p.getX());
        }
        return max;
    }

    private float maxY(List<GraphPoints> data) {
        float max = 0;
        for (GraphPoints points : data) {
            max = Math.max(max, points.getY());
        }

        if (max <= 100) {
            max = Math.round(max / 10) * 10;
        } else if (max <= 1000) {
            max = Math.round(max / 100) * 100;
        } else if (max <= 10000) {
            max = Math.round(max / 1000) * 1000;
        } else if (max <= 100000) {
            max = Math.round(max / 10000) * 10000;
        } else if (max <= 1000000) {
            max = Math.round(max / 100000) * 100000;
        }
        return max;
    }

    /**
     * Method operates on a x-axis value so as to return a corresponding value for drawing on the chart
     * @param x the actual value
     * @param max max x value in the list
     * @return a point x equivalent on the graph
     */
    private float pointX(float x, final float max) {
        float w = width - paddingStart - paddingEnd - 20;
        float interval = w / max;
        float start = 10 + paddingStart;
        return ((x * interval) + start);
    }

    /**
     * Method operates on a y-axis value so as to return a corresponding value for drawing on the chart
     * @param y the actual value
     * @param max max y value in the list
     * @return a point y equivalent on the graph
     */
    private float pointY(float y, final float max) {
        float interval = (height - 20 - 50 - paddingTop - paddingBottom) / max;
        float h = height - 10 - paddingBottom;
        return h - ((y * interval)) - 20;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = widthMeasureSpec;
        height = heightMeasureSpec;

        setMeasuredDimension((int) width, (int) height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) throws NullPointerException {
        super.onDraw(canvas);
        float start = getPaddingStart() + 10;
        float end = width - paddingEnd - 10;
        float h = height - paddingBottom - paddingTop - 10;
        float ht = paddingTop + 1.5f;
        float w = width - paddingEnd - paddingStart - 10;

        float maxX = 0;
        float maxY = 0;
        for (List<GraphPoints> pts : all) {
            maxX = Math.max(maxX(pts), maxX);
            maxY = Math.max(maxY(pts), maxY);
        }
        //Draw Legend
        legend(canvas, start, ht, all.size(), labels);

        //Draw the x axis
        canvas.drawLine(start, h, end, h, xAxisPaint);
        //Draw The Dividers
        drawLines(start, end, h, canvas, maxY);
        //Draw Graph line and plots
        int j = 0;
        for (List<GraphPoints> pt:all){
            List<GraphPoints> a = getData(pt, maxX, maxY);
            float[] pts = compileArray(a);

            canvas.drawLines(pts, linePaints.get(j));
            drawPlots(canvas, a, j++);
        }
    }

    /**
     * draws the legend of the chart
     * @param c canvas
     * @param x starting point x
     * @param y starting point y
     * @param count number of graph line
     * @param labels labels of the graph line
     */
    private void legend(Canvas c,float x, float y, final int count, List<String> labels) {
        final float l = 150f;
        float w = 10f;
        float radius = 3.5f;
        List<GraphPoints> points = new ArrayList<>();
        for (int i = 0; i < count; ++i){
            c.drawLine(x, y, x+w, y, linePaints.get(i));
            c.drawCircle(x, y, radius, circlePaints.get(i));
            c.drawCircle(x+w, y, radius, circlePaints.get(i));
            c.drawText(labels.get(i), x+w+50, y+5, textPaint);
            x += l;
        }
    }

    /**
     * Draws the horizontal dividers of the graph
     *
     * @param x0 horizontal starting point of the graph
     * @param x1 horizontal end point of the graph
     * @param h  The height of the graph View
     * @param c  Canvas on which the View will be drawn on
     */
    void drawLines(final float x0, final float x1, float h, Canvas c, float maxY) {
        float y = h-50;
        int count = 5;
        int size = count * 4;
        float interval = y / count;
        y = h;
        float[] points = new float[size];

        float interval1 = maxY / 5;
        int j = 0;

        c.drawText(String.format(Locale.getDefault(), "%,.0f", 0f), x0-10, y+15, textPaint);
        for (int i = 0; i < size; i += 4) {
            if (y < 0)
                break;
            y -= interval;
            c.drawText(String.format(Locale.getDefault(), "%,.0f", ++j * interval1), x0, y + 15, textPaint);
            points[i] = x0;
            points[i + 1] = y;
            points[i + 2] = x1;
            points[i + 3] = y;
        }
        c.drawLines(points, guidesPaint);
    }

    /**
     * Draws the plotting points of the graph as circles
     */
    private void drawPlots(Canvas c, List<GraphPoints> pts, int i) {
        for (GraphPoints pt : pts) {
            c.drawCircle(pt.getX(), pt.getY(), 3.5f, circlePaints.get(i));
        }
    }

    List<GraphPoints> getData(List<GraphPoints> data, float maxX, float maxY) {
        float cx, cy;
        List<GraphPoints> pts = new ArrayList<>();
        for (GraphPoints point : data) {
            cx = pointX(point.getX(), maxX);
            cy = pointY(point.getY(), maxY);
            pts.add(new GraphPoints(cx, cy));
        }
        return pts;
    }

    static class DrawLines extends View {
        private float[] pts;
        private Paint linePaint;

        public DrawLines(Context context) {
            super(context);
            init();
        }

        public DrawLines(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        public DrawLines(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            init();
        }

        private void init() {
            linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            linePaint.setColor(Color.BLUE);
            linePaint.setStrokeWidth(3.5f);
        }

        public void setPts(float[] pts) {
            this.pts = pts;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawLines(pts, linePaint);
        }
    }

    /**
     * Draws a circe object used as plotting points on the graph
     */
    static class Circles extends View {
        private Paint circlePaint;
        private List<GraphPoints> data;
        private float radius;

        public Circles(Context context) {
            super(context);
            init();
        }

        public Circles(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        public Circles(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            init();
        }

        private void init() {
            circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            circlePaint.setColor(Color.BLUE);
            radius = 2f;
        }

        public void setData(List<GraphPoints> data) {
            this.data = data;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            for (GraphPoints pt : data) {
                canvas.drawCircle(pt.getX(), pt.getY(), radius, circlePaint);
            }
        }
    }

    static class Mask{
        private List<GraphPoints> pts;
        private String label;
        private List<Date> dates;

        public Mask(List<GraphPoints> pts, String label, List<Date> dates) {
            this.pts = pts;
            this.label = label;
            this.dates = dates;
        }

        public List<GraphPoints> getPts() {
            return pts;
        }

        public String getLabel() {
            return label;
        }

        public List<Date> getDates() {
            return dates;
        }
    }
}