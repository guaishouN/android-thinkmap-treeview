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
import com.gyso.treeview.adapter.DrawInfo;
import com.gyso.treeview.adapter.TreeViewAdapter;
import com.gyso.treeview.adapter.TreeViewHolder;
import com.gyso.treeview.line.Baseline;
import com.gyso.treeview.line.SimpleSmoothLine;
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
public class AnimalTreeViewAdapter extends TreeViewAdapter<Animal> {
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
}
