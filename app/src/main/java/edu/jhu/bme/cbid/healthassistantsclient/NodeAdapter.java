package edu.jhu.bme.cbid.healthassistantsclient;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import edu.jhu.bme.cbid.healthassistantsclient.objects.Node;

/**
 * Created by Amal Afroz Alam on 23, April, 2016.
 * Contact me: contact@amal.io
 */
public class NodeAdapter extends BaseExpandableListAdapter {

    private String LOG_TAG = "Node Adapter";
    private Context mContext;
    private Node mNode;
    private LayoutInflater mInflater;
    private String callingClass;

    // The default choice is the multiple one
    private int choiceMode = AbsListView.CHOICE_MODE_MULTIPLE;

    public NodeAdapter(Context context, Node node, String caller) {
        this.mContext = context;
        this.mNode = node;
        this.mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.callingClass = caller;
    }

//    public NodeAdapter(Context context, Knowledge knowledge) {
//        this.mContext = context;
//        this.mNode = knowledge;
//        this.mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//    }


    @Override
    public int getGroupCount() {
        return mNode.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
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

        final TextView textView = (TextView) convertView.findViewById(R.id.expandable_list_group);
        ImageView imageView = (ImageView) convertView.findViewById(R.id.expandable_list_group_image);

        textView.setText(node.text());
        textView.setTypeface(Typeface.DEFAULT_BOLD);

        switch (callingClass) {
            case "QuestionNodeActivity":
                if (node.isSelected() | node.anySubSelected()) {
                    imageView.setImageResource(R.drawable.green_check);
                    //textView.setBackgroundResource(R.color.colorAccent);
                } else {
                    imageView.setImageResource(R.drawable.grey_check);
                    //textView.setBackgroundResource(0);
                }
                break;
            default:
                imageView.setVisibility(View.GONE);
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
        textView.setText(node.text());

        ImageView imageView = (ImageView) convertView.findViewById(R.id.expandable_list_item_image);

        switch (callingClass) {

            case "ComplaintNodeActivity":
                if (node.isSelected()) {
                    imageView.setImageResource(R.drawable.green_check);
                } else {
                    imageView.setImageResource(R.drawable.grey_check);
                }
                break;
            case "QuestionNodeActivity":
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
}
