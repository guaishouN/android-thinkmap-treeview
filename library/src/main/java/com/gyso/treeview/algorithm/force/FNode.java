package com.gyso.treeview.algorithm.force;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 力导向图中显示的节点。
 *
 * Created by Z.Pan on 2016/10/8.
 */

public class FNode {

    /** 根节点级别 */
    public static final int ROOT_NODE_LEVEL = 0;

    static final short DRAG_START = 2;
    static final short DRAG = 4;
    static final short DRAG_END = 6;

    private String text;   // 节点显示的内容
    private Object obj;    // 用来携带其他数据，如：该节点对应的数据实体 Bean，或数据库中的 _id
    private int    level;  // 级别

    public float x, y;    // 当前坐标
    float px, py;  // 前一个状态的坐标
    int weight;    // 根据子节点自动计算，weight 越大，该节点越不容易被拖动

    private float radius = 50f; // 节点半径
    private short state;        // 节点状态，该状态决定了是否处于稳定状态

    public FNode(String text) {
        this(text, 50f, ROOT_NODE_LEVEL);
    }

    public FNode(String text, float radius, int level) {
        this.text = text;
        this.radius = radius;
        this.level = level;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }

    public String getText() {
        return text;
    }

    public Object getObj() {
        return obj;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    float getRadius() {
        return radius;
    }

    boolean isRootNode() {
        return level == ROOT_NODE_LEVEL;
    }

    /**
     * 给定一个坐标 (x, y)，判断该坐标是否在节点所在范围内。用来判断是否点击了该节点。
     * @param x x坐标
     * @param y y坐标
     * @param scale 缩放比例
     * @return true 表示 (x, y) 在该节点内部
     */
    boolean isInside(float x, float y, float scale) {
        float left = (this.x - radius) * scale;
        float top = (this.y - radius) * scale;
        float right = (this.x + radius) * scale;
        float bottom = (this.y + radius) * scale;
        return x >= left && x <= right && y >= top && y <= bottom;
    }

    boolean isStable() {
        return state != 0;
    }

    /**
     * 设置节点的状态是否正在被手指拖动。
     * @param state {@linkplain #DRAG_START} 开始拖动；{@linkplain #DRAG_END} 结束拖动。
     */
    void setDragState(@State short state) {
        switch (state) {
            case DRAG_START:
                this.state |= state;
                break;
            case DRAG_END:
                this.state &= ~state;
                break;
        }
    }

    @ShortDef({DRAG_START, DRAG, DRAG_END})
    @Retention(SOURCE)
    public @interface State {}

    @Retention(SOURCE)
    @Target({ANNOTATION_TYPE})
    public @interface ShortDef {
        short[] value() default {};
        boolean flag() default false;
    }

}
