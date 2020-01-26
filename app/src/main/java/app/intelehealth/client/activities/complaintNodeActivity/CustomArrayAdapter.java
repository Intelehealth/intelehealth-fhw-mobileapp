package app.intelehealth.client.activities.complaintNodeActivity;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Locale;

import app.intelehealth.client.R;
import app.intelehealth.client.knowledgeEngine.Node;


/**
 * Created by Amal Afroz Alam on 27, April, 2016.
 * Contact me: contact@amal.io
 */
public class CustomArrayAdapter extends ArrayAdapter<Node> {

    private Context mContext;
    private int layoutResourceID;
    private ImmutableList<Node> mNodes;
    private List<Node> mNodesFilter;

    private static final String TAG = CustomArrayAdapter.class.getSimpleName();

    /**
     * Constructor
     *
     * @param context  The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     * @param nodes  The objects to represent in the ListView.
     */

    /**
     * The Array Adapter is primarily used for sub-questions that come off a knowledgeEngine.
     * For this reason, some of the nodes used with the array adapter might actually have a job aid with it that must be displayed.
     * Currently, images are the only ones supported.
     */
    public CustomArrayAdapter(Context context, int resource, List<Node> nodes) {
        super(context, resource, nodes);
        mContext = context;
        layoutResourceID = resource;
        mNodesFilter = nodes;
        mNodes= ImmutableList.copyOf(mNodesFilter);
    }

    //Each view is a knowledgeEngine itself
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            convertView = inflater.inflate(layoutResourceID, parent, false);
        }

        final Node thisNode = mNodesFilter.get(position);

        TextView textViewItem = convertView.findViewById(R.id.subquestion_text_view);
        textViewItem.setText(thisNode.findDisplay());

        ImageView imageView = convertView.findViewById(R.id.subquestion_image_view);

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

    // Filter Class
    public void filter(String charText) {
        Log.i(TAG, "filter: Entered Filter");
        Log.i(TAG, "filter: "+ mNodes.size());
        Log.i(TAG, "filter: "+ mNodesFilter.size());
        mNodesFilter.clear();
        Log.i(TAG, "filter: "+ mNodes.size());
        Log.i(TAG, "filter: "+ mNodesFilter.size());
        charText = charText.toLowerCase(Locale.getDefault());
        Log.i(TAG, "filter: "+charText);
        if (!charText.trim().isEmpty()) {
            Log.i(TAG, "filter: Not Empty" );
            for (Node node : mNodes) {
                Log.i(TAG, "filter: " + node.getText());
                Log.i(TAG, "filter: " + node.findDisplay());
                if (!node.findDisplay().isEmpty()) {
                    if (node.findDisplay().toLowerCase(Locale.getDefault())
                            .contains(charText)) {
                        mNodesFilter.add(node);
                        Log.i(TAG, "filter: Node Matched");
                    }
                }
            }
        } else {
            mNodesFilter.addAll(mNodes);
        }
        notifyDataSetChanged();
    }

    public ImmutableList<Node> getmNodes() {
        return mNodes;
    }

}
