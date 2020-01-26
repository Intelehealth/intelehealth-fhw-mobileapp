package app.intelehealth.client.activities.physcialExamActivity;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import app.intelehealth.client.R;
import app.intelehealth.client.activities.questionNodeActivity.QuestionNodeActivity;
import app.intelehealth.client.knowledgeEngine.Node;


/**
 * Creates list of complaints.
 * <p>
 * Created by Amal Afroz Alam on 23, April, 2016.
 * Contact me: contact@amal.io
 */
public class CustomExpandableListAdapter extends BaseExpandableListAdapter {

    private String TAG = CustomExpandableListAdapter.class.getSimpleName();
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
        if (mNode.getOption(groupPosition).isTerminal()) {
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

        TextView textView = convertView.findViewById(R.id.expandable_list_group);
        ImageView imageView = convertView.findViewById(R.id.expandable_list_group_image);

        textView.setText(node.findDisplay());
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

        Node nodeGroup = (Node) getGroup(groupPosition);
        if (nodeGroup.getText().equalsIgnoreCase("Associated symptoms")) {
            convertView = mInflater.inflate(R.layout.list_expandable_item_radio, null);

            final Node node = (Node) getChild(groupPosition, childPosition);

            final TextView textView = convertView.findViewById(R.id.expandable_list_item);
            final RadioButton rbYes = convertView.findViewById(R.id.radio_yes);
            final RadioButton rbNo = convertView.findViewById(R.id.radio_no);

            textView.setText(node.findDisplay());

            View finalConvertView = convertView;
            rbYes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    node.setNoSelected(false);
                    ((QuestionNodeActivity) mContext).onListClicked(finalConvertView, groupPosition, childPosition);
                }
            });

            rbNo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    node.setNoSelected(true);
                    node.setUnselected();
                }
            });

            switch (callingClass) {

                case "ComplaintNodeActivity":
                    if (node.isSelected()) {
                        rbYes.setChecked(true);
                    } else {
                        rbNo.setChecked(true);
                    }
                    break;
                default:
                    if (node.isSelected()) {
                        rbYes.setChecked(true);
                    } else {
                        if (node.isNoSelected()) {
                            rbNo.setChecked(true);
                        } else {
                            rbNo.setChecked(false);
                        }
                    }
                    break;
            }

        } else {

//            if (convertView == null) {
//                convertView = mInflater.inflate(R.layout.list_expandable_item, null);
//            }
            convertView = mInflater.inflate(R.layout.list_expandable_item, null);


            final Node node = (Node) getChild(groupPosition, childPosition);
            final TextView textView = convertView.findViewById(R.id.expandable_list_item);
            textView.setText(node.findDisplay());

            ImageView imageView = convertView.findViewById(R.id.expandable_list_item_image);

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

    public void updateNodeList(Node newNode) {
        mNode = null;
        mNode = newNode;
        this.notifyDataSetChanged();
    }
}
