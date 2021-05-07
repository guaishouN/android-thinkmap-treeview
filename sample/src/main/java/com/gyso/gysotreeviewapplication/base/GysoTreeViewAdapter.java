package com.gyso.gysotreeviewapplication.base;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import com.gyso.gysotreeviewapplication.R;
import com.gyso.gysotreeviewapplication.databinding.NodeBaseLayoutBinding;
import com.gyso.treeview.adapter.TreeViewAdapter;
import com.gyso.treeview.adapter.TreeViewHolder;
import com.gyso.treeview.model.NodeModel;
import com.gyso.treeview.util.DensityUtils;


/**
 * @Author: 怪兽N
 * @Time: 2021/4/23  16:48
 * @Email: 674149099@qq.com
 * @WeChat: guaishouN
 * @Describe:
 * Tree View Adapter for node data to view
 */
public class GysoTreeViewAdapter extends TreeViewAdapter<Animal> {
    @Override
    public TreeViewHolder<Animal> onCreateViewHolder(@NonNull ViewGroup viewGroup, NodeModel<Animal> node) {
        NodeBaseLayoutBinding nodeBinding = NodeBaseLayoutBinding.inflate(LayoutInflater.from(viewGroup.getContext()),viewGroup,false);
        return new TreeViewHolder<>(nodeBinding.getRoot(),node);
    }

    @Override
    public void onBindViewHolder(@NonNull TreeViewHolder<Animal> holder) {
        //todo get view and node from holder, and then show by you
        View itemView = holder.getView();
        NodeModel<Animal> node = holder.getNode();
        TextView nameView = itemView.findViewById(R.id.name);
        ImageView headView = itemView.findViewById(R.id.portrait);
        Animal animal = node.value;
        nameView.setText(animal.name);
        headView.setImageResource(animal.headId);
    }

    @Override
    public void onDrawLine(Canvas canvas, TreeViewHolder<Animal> fromHolder, TreeViewHolder<Animal> toHolder, Paint mPaint, Path mPath) {
        //get view and node
        View fromView = fromHolder.getView();
        NodeModel<Animal> fromNode = fromHolder.getNode();
        View toView = toHolder.getView();
        NodeModel<Animal> toNode = toHolder.getNode();
        Context context = fromView.getContext();

        //set paint
        mPaint.reset();
        mPaint.setColor(ResourcesCompat.getColor(context.getResources(),R.color.textColor,null));
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(DensityUtils.dp2px(context,3));
        mPaint.setAntiAlias(true);

        //setPath
        mPath.reset();
        PointF startPoint = new PointF(fromView.getRight(),(fromView.getTop()+fromView.getBottom())/2f);
        PointF endPoint = new PointF(toView.getLeft(),(toView.getTop()+toView.getBottom())/2f);
        mPath.moveTo(startPoint.x,startPoint.y);
        mPath.cubicTo(
                startPoint.x+DensityUtils.dp2px(context,15),startPoint.y,
                startPoint.x,endPoint.y,
                endPoint.x,endPoint.y);

        //draw
        canvas.drawPath(mPath,mPaint);
    }
}
