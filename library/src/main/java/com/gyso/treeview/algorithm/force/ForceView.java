package com.gyso.treeview.algorithm.force;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * Force layout from D3.js
 * <p>
 * Created by Z.Pan on 2016/10/8.
 */
public class ForceView extends View implements ForceListener {

    private static final int[] colors = {
            Color.parseColor("#f09d24"),
            Color.parseColor("#d95c8a"),
            Color.parseColor("#13a1e1"),
            Color.parseColor("#8bc34a"),
            Color.parseColor("#8d6e63")
    };

    public interface OnNodeClickListener {
        void onNodeClick(FNode node);
    }

    private OnNodeClickListener onNodeClickListener;

    public OnNodeClickListener getOnNodeClickListener() {
        return onNodeClickListener;
    }

    public void setOnNodeClickListener(OnNodeClickListener onNodeClickListener) {
        this.onNodeClickListener = onNodeClickListener;
    }

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint linkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint linkTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Path arrowPath = new Path();
    private int strokeColor;

    private Force force;
    private float textBaseline;
    private float textHeight;
    private float linkTextBaseline;

    private float touchSlop;
    private float downX, downY;
    private float translateX, translateY;
    private float scale = 1f;
    private float x0, y0;
    private float padding = 5f;
    private FNode node;
    private List<FLink> targetLinks = new ArrayList<>();
    private List<FLink> sourceLinks = new ArrayList<>();
    private List<FNode> selectedNodes = new ArrayList<>();
    private ScaleGestureDetector scaleDetector;

    public ForceView(Context context) {
        this(context, null);
    }

    public ForceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ForceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint.setColor(Color.LTGRAY);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);

        strokePaint.setAntiAlias(true);
        strokePaint.setColor(Color.BLUE);
        strokePaint.setStrokeWidth(5f);
        strokePaint.setStyle(Paint.Style.STROKE);

        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(dp2px(13));
        textPaint.setColor(Color.BLUE);

        linkPaint.setAntiAlias(true);

        linkTextPaint.setAntiAlias(true);
        linkTextPaint.setTextAlign(Paint.Align.CENTER);
        linkTextPaint.setTextSize(dp2px(13));

        scaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        textBaseline = (fontMetrics.bottom + fontMetrics.top) * 0.5f;
        textHeight = fontMetrics.bottom - fontMetrics.top;
        fontMetrics = linkTextPaint.getFontMetrics();
        linkTextBaseline = (fontMetrics.bottom + fontMetrics.top) * 0.5f;

        touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        force = new Force(ForceView.this);

        post(new Runnable() {
            @Override
            public void run() {
                int w = getWidth();
                int h = getHeight();
                force.setSize(w, h)
                        .setStrength(0.7f)
                        .setFriction(0.8f)
                        .setDistance(150)
                        .setCharge(-320f)
                        .setGravity(0.1f)
                        .setTheta(0.8f)
                        .setAlpha(0.2f)
                        .start();
            }
        });
    }

    public void setData(ArrayList<FNode> nodes, ArrayList<FLink> links) {
        force.setNodes(nodes)
                .setLinks(links)
                .start();
    }

    private void resetCanvasState() {
        translateX = 0;
        translateY = 0;
        scale = 1;
    }

    public void setCurrentLevel(int level) {
        if (force.getCurrentLevel() == level) {
            return;
        }
        resetCanvasState();
        force.setCurrentLevel(level).start();
    }

    private void drawLinks(Canvas canvas, List<FLink> links) {
        if (links == null) {
            return;
        }

        linkPaint.setColor(Color.GRAY);
        linkTextPaint.setColor(Color.GRAY);
        for (int i = 0; i < links.size(); i++) {
            FLink link = links.get(i);
            drawLink(canvas, link);
        }
    }

    private void drawLink(Canvas canvas, FLink link) {
        if (link == null) {
            return;
        }
        float startX = link.source.x;
        float startY = link.source.y;
        float stopX = link.target.x;
        float stopY = link.target.y;

        int color = Color.GRAY;
        if (node != null && node == link.source || node == link.target) {
            color = getColor(link.source.getLevel());
        }

        linkPaint.setColor(color);
        linkTextPaint.setColor(color);

        canvas.drawLine(startX, startY, stopX, stopY, linkPaint);

        // draw arrow
        double distance = link.getNodeDistance();
        float ratio = (float) ((distance - link.target.getRadius()) / distance);
        float ax1 = (stopX - startX) * ratio + startX;
        float ay1 = (stopY - startY) * ratio + startY;

        ratio = (float) ((distance - link.target.getRadius() - dp2px(10)) / distance);
        float ax3 = (stopX - startX) * ratio + startX;
        float ay3 = (stopY - startY) * ratio + startY;

        ratio = (float) ((distance - link.target.getRadius() - dp2px(12)) / distance);
        float ax24 = (stopX - startX) * ratio + startX;
        float ay24 = (stopY - startY) * ratio + startY;

        float l = dp2px(5);
        float dx = (stopY - startY) / (float) distance * l;
        float dy = (startX - stopX) / (float) distance * l;

        float ax2 = ax24 - dx;
        float ay2 = ay24 - dy;
        float ax4 = ax24 + dx;
        float ay4 = ay24 + dy;

        arrowPath.reset();
        arrowPath.moveTo(ax1, ay1);
        arrowPath.lineTo(ax2, ay2);
        arrowPath.lineTo(ax3, ay3);
        arrowPath.lineTo(ax4, ay4);
        arrowPath.close();

        canvas.drawPath(arrowPath, linkPaint);

        // draw link text
        String linkText = link.getText();
        if (linkText != null && linkText.trim().length() > 0) {
            ratio = (float) ((distance - link.target.getRadius() - distance / 5f) / distance);
            float textX = (stopX - startX) * ratio + startX;
            float textY = (stopY - startY) * ratio + startY;
            canvas.drawText(linkText, textX, textY - linkTextBaseline, linkTextPaint);
        }
    }

    private void drawNodes(Canvas canvas, List<FNode> nodes, boolean drawStroke) {
        if (nodes == null) {
            return;
        }

        for (int i = 0; i < nodes.size(); i++) {
            FNode node = nodes.get(i);
            drawNode(canvas, node, drawStroke);
        }
    }

    private void drawNode(Canvas canvas, FNode node, boolean drawStroke) {
        if (node == null) {
            return;
        }

        float cx = node.x;
        float cy = node.y;
        resetPaintColor(node.getLevel());
        canvas.drawCircle(cx, cy, node.getRadius(), paint);

        String text = node.getText();
        double w = Math.sqrt(4 * node.getRadius() * node.getRadius() - textHeight * textHeight) - padding * 2;
        float textWidth = textPaint.measureText(text);
        float n;
        if (w >= textWidth) {
            canvas.drawText(text, cx, cy - textBaseline, textPaint);
        } else {
            float th = textHeight * 2;
            w = Math.sqrt(4 * node.getRadius() * node.getRadius() - th * th) - padding * 2;

            n = (float) w / textWidth;
            int end = (int) (text.length() * n);
            if (end < text.length() - 1) {
                canvas.drawText(text.substring(0, end), cx, cy - textHeight * 0.5f - textBaseline, textPaint);
                String t;
                if (textWidth > 2 * w) {
                    t = text.substring(end, 2 * end - 1) + "...";
                } else {
                    t = text.substring(end);
                }
                canvas.drawText(t, cx, cy + textHeight * 0.5f - textBaseline, textPaint);
            } else {
                canvas.drawText(text, cx, cy - textBaseline, textPaint);
            }
        }
        if (drawStroke) {
            strokePaint.setColor(strokeColor);
            canvas.drawCircle(cx, cy, node.getRadius() + 5, strokePaint);
        }
    }

    private void resetPaintColor(int level) {
        int color = getColor(level);
        paint.setColor(color);
        textPaint.setColor(Color.WHITE);
//        textPaint.setAlpha(192);
        strokePaint.setColor(color);
    }

    private int getColor(int level) {
        return colors[level % colors.length];
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        System.out.println(2);

        ArrayList<FNode> nodes = force.nodes;
        ArrayList<FLink> links = force.links;

        canvas.save();

        canvas.translate(translateX, translateY);
        canvas.scale(scale, scale);

        drawLinks(canvas, links);

        drawNodes(canvas, nodes, false);

        if (!targetLinks.isEmpty()) {
            drawLinks(canvas, targetLinks);
        }

        if (!sourceLinks.isEmpty()) {
            drawLinks(canvas, sourceLinks);
        }

        drawNodes(canvas, selectedNodes, false);

        drawNode(canvas, node, true);

        canvas.restore();

    }

    private int activePointerId = -1;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);

        float x;
        float y;
        int pointerIndex;

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                activePointerId = event.getPointerId(0);
                x0 = downX = x = event.getX();
                y0 = downY = y = event.getY();
                node = force.getNode(
                        x - translateX,
                        y - translateY,
                        scale);
                if (node != null) {
                    strokeColor = getColor(node.getLevel());
                    strokeColor = Color.argb(128, Color.red(strokeColor), Color.green(strokeColor), Color.blue(strokeColor));
                    ArrayList<FLink> links = force.links;
                    for (int i = 0, size = links.size(); i < size; i++) {
                        FLink link = links.get(i);
                        if (link.source == node) {
                            selectedNodes.add(link.target);
                            targetLinks.add(link);
                        } else if (link.target == node) {
                            selectedNodes.add(link.source);
                            sourceLinks.add(link);
                        }
                    }
                    node.setDragState(FNode.DRAG_START);
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                pointerIndex = event.findPointerIndex(activePointerId);
                x = event.getX(pointerIndex);
                y = event.getY(pointerIndex);

                if (Math.abs((x - x0) * (x - y0)) > touchSlop * touchSlop) {
                    if (node != null) {
                        node.px = (x - translateX) / scale;
                        node.py = (y - translateY) / scale;
                        force.resume();
                    } else {
                        if (!scaleDetector.isInProgress()) {
                            translateX += x - downX;
                            translateY += y - downY;
                            invalidate();
                        }
                    }
                }
                downX = x;
                downY = y;
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                activePointerId = -1;
                if (node != null) {
                    node.setDragState(FNode.DRAG_END);
                    invalidate();
                    x = event.getX();
                    y = event.getY();
                    if (Math.abs((x - x0) * (x - y0)) < touchSlop * touchSlop) {
                        if (onNodeClickListener != null) {
                            onNodeClickListener.onNodeClick(node);
                        }
                    }
                    node = null;
                }
                targetLinks.clear();
                sourceLinks.clear();
                selectedNodes.clear();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                node = null;
                targetLinks.clear();
                sourceLinks.clear();
                break;

            case MotionEvent.ACTION_POINTER_UP:
                pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK)
                        >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                int pointerId = event.getPointerId(pointerIndex);
                if (pointerId == activePointerId) {
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    downX = event.getX(newPointerIndex);
                    downY = event.getY(newPointerIndex);
                    activePointerId = event.getPointerId(newPointerIndex);
                }
                break;
        }
        return true;
    }

    @Override
    public void refresh() {
        invalidate();
    }

    @Override
    protected void onDetachedFromWindow() {
        force.endTickTask();
        super.onDetachedFromWindow();
    }

    private int dp2px(int dp) {
        float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (detector.isInProgress()) {
                float factor = detector.getScaleFactor();

                float pScale = scale;

                scale *= factor;
                scale = Math.max(0.1f, Math.min(scale, 5.0f));

                if (!((pScale == 0.1 && scale == 0.1) || (pScale == 5 && scale == 5))) {
                    float focusX = detector.getFocusX();
                    float focusY = detector.getFocusY();
                    translateX += (focusX - translateX) * (1 - factor);
                    translateY += (focusY - translateY) * (1 - factor);
                }

                invalidate();
            }

            return true;
        }
    }

}
