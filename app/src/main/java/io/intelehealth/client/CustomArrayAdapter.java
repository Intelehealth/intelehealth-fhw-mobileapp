package io.intelehealth.client;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import io.intelehealth.client.objects.Node;

/**
 * Created by Amal Afroz Alam on 27, April, 2016.
 * Contact me: contact@amal.io
 */
public class CustomArrayAdapter extends ArrayAdapter<Node> {

    private Context mContext;
    private int layoutResourceID;
    private List<Node> mNodes;

    /**
     * Constructor
     *
     * @param context  The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     * @param nodes  The objects to represent in the ListView.
     */

    /**
     * The Array Adapter is primarily used for sub-questions that come off a node.
     * For this reason, some of the nodes used with the array adapter might actually have a job aid with it that must be displayed.
     * Currently, images are the only ones supported.
     */
    public CustomArrayAdapter(Context context, int resource, List<Node> nodes) {
        super(context, resource, nodes);
        mContext = context;
        layoutResourceID = resource;
        mNodes = nodes;
    }

    //Each view is a node itself
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            convertView = inflater.inflate(layoutResourceID, parent, false);
        }

        final Node thisNode = mNodes.get(position);

        TextView textViewItem = (TextView) convertView.findViewById(R.id.subquestion_text_view);
        textViewItem.setText(thisNode.getText());

        ImageView imageView = (ImageView) convertView.findViewById(R.id.subquestion_image_view);

        if (thisNode.isSelected()) {
            imageView.setImageResource(R.drawable.checkbox);
            //textView.setBackgroundResource(R.color.colorAccent);
        } else {
            imageView.setImageResource(R.drawable.blank_checkbox);
            //textView.setBackgroundResource(0);
        }

        return convertView;

    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    public void updateNodeList(List<Node> newNodes){
        mNodes.clear();
        mNodes.addAll(newNodes);
        this.notifyDataSetChanged();
    }
}
