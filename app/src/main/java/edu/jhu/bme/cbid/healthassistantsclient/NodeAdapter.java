package edu.jhu.bme.cbid.healthassistantsclient;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import edu.jhu.bme.cbid.healthassistantsclient.objects.Knowledge;
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

    public NodeAdapter(Context context, Knowledge knowledge) {
        Log.d(LOG_TAG, "Created");
        this.mContext = context;
        this.mNode = knowledge;
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


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
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.group_complaint, null);
        }

        Node node = (Node) getGroup(groupPosition);

        TextView textView = (TextView) convertView.findViewById(R.id.complaint_group);
        textView.setText(node.text());

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_complaint, null);
        }


        Node node = (Node) getChild(groupPosition, childPosition);
        TextView textView = (TextView) convertView.findViewById(R.id.complaint_item);
        textView.setText(node.text());
        return convertView;

        //If the child is a complaint, then add it to the group list, otherwise dont
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
