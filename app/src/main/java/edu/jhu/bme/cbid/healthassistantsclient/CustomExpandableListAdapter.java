package edu.jhu.bme.cbid.healthassistantsclient;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import edu.jhu.bme.cbid.healthassistantsclient.objects.Node;

/**
 * Created by Amal Afroz Alam on 23, April, 2016.
 * Contact me: contact@amal.io
 */
public class CustomExpandableListAdapter extends BaseExpandableListAdapter {

    private String LOG_TAG = "Node Adapter";
    private Context mContext;
    private Node mNode;
    private LayoutInflater mInflater;
    private String callingClass;

    public CustomExpandableListAdapter(Context context, Node node, String caller) {
        this.mContext = context;
        this.mNode = node;
        this.mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.callingClass = caller;
    }

    @Override
    public int getGroupCount() {
        return mNode.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if(mNode.getOption(groupPosition).isTerminal()){
            return 0;
        }
        return mNode.getOption(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mNode.getOption(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mNode.getOption(groupPosition).getOption(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition * 1024;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return ((groupPosition * 1024) + childPosition);
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_expandable_group, null);
        }

        Node node = (Node) getGroup(groupPosition);

        TextView textView = (TextView) convertView.findViewById(R.id.expandable_list_group);
        ImageView imageView = (ImageView) convertView.findViewById(R.id.expandable_list_group_image);

        textView.setText(node.getText());
        textView.setTypeface(Typeface.DEFAULT_BOLD);

        switch (callingClass) {
            case "ComplaintNodeActivity":
                imageView.setVisibility(View.GONE);
                break;
            default:
                if (node.isSelected() | node.anySubSelected()) {
                    imageView.setImageResource(R.drawable.checkbox);
                    //textView.setBackgroundResource(R.color.colorAccent);
                } else {
                    imageView.setImageResource(R.drawable.blank_checkbox);
                    //textView.setBackgroundResource(0);
                }
                break;
        }

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_expandable_item, null);
        }
        final Node node = (Node) getChild(groupPosition, childPosition);
        final TextView textView = (TextView) convertView.findViewById(R.id.expandable_list_item);
        textView.setText(node.getText());

        ImageView imageView = (ImageView) convertView.findViewById(R.id.expandable_list_item_image);

        switch (callingClass) {

            case "ComplaintNodeActivity":
                if (node.isSelected()) {
                    imageView.setImageResource(R.drawable.checkbox);
                } else {
                    imageView.setImageResource(R.drawable.blank_checkbox);
                }
                break;
            default:
                if (node.isSelected()) {
                    imageView.setImageResource(R.drawable.checkbox);
                    //textView.setBackgroundResource(R.color.colorAccent);
                } else {
                    //textView.setBackgroundResource(0);
                    imageView.setImageResource(R.drawable.blank_checkbox);
                }
                break;
        }

        return convertView;

        //If the child is a complaint, then add it to the group list, otherwise dont
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    public void updateNodeList(Node newNode){
        mNode = null;
        mNode = newNode;
        this.notifyDataSetChanged();
    }
}
