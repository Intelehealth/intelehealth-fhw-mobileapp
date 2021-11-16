package org.intelehealth.msfarogyabharat.utilities.multipleSelectionSpinner;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

import org.intelehealth.msfarogyabharat.R;

import java.util.ArrayList;
import java.util.Arrays;

public class MultiSelectionSpinner extends androidx.appcompat.widget.AppCompatSpinner implements
        DialogInterface.OnMultiChoiceClickListener {

        ArrayList<Item> items = null;
        boolean[] selection = null;
        ArrayAdapter adapter;


public MultiSelectionSpinner(Context context) {
        super(context);

        adapter = new ArrayAdapter(context,
        android.R.layout.simple_spinner_item);
        super.setAdapter(adapter);
        }

public MultiSelectionSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);

        adapter = new ArrayAdapter(context,
        android.R.layout.simple_spinner_item);
        super.setAdapter(adapter);
        }

@Override
public void onClick(DialogInterface dialog, int which, boolean isChecked) {
        if (selection != null && which < selection.length) {
        selection[which] = isChecked;

        adapter.clear();
//        adapter.add(buildSelectedItemString());

                if(buildSelectedItemString().isEmpty()){
                        adapter.add(getResources().getString(R.string.textViewHintFacility));

                }
                else{
                        adapter.add(buildSelectedItemString());

                }
        } else {
        throw new IllegalArgumentException(
        "Argument 'which' is out of bounds.");
        }
        }

@Override
public boolean performClick() {
final AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AlertDialogStyle);

        String[] itemNames = new String[items.size()];

        for (int i = 0; i < items.size(); i++) {
        itemNames[i] = items.get(i).getName();
        }

        builder.setMultiChoiceItems(itemNames, selection, this);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
@Override
public void onClick(DialogInterface arg0, int arg1)
        {
        // Do nothing

    }
        });


        builder.show();

        return true;
        }

@Override
public void setAdapter(SpinnerAdapter adapter) {
        throw new RuntimeException(
        "setAdapter is not supported by MultiSelectSpinner.");
        }

public void setItems(ArrayList<Item> items) {
        this.items = items;
        selection = new boolean[this.items.size()];
        adapter.clear();
        adapter.add(getResources().getString(R.string.textViewHintFacility));
        Arrays.fill(selection, false);
}

        public void setSelection(ArrayList<Item> selection) {
                for (int i = 0; i < this.selection.length; i++) {
                        this.selection[i] = false;
                }
//        adapter.add("");

        for (Item sel : selection) {
        for (int j = 0; j < items.size(); ++j) {
        if (items.get(j).getValue().equals(sel.getValue())) {
        this.selection[j] = true;
        }
        }
        }

        adapter.clear();
        if(!buildSelectedItemString().isEmpty()){
        adapter.add(buildSelectedItemString());}
        else{
                adapter.add(getResources().getString(R.string.textViewHintFacility));
        }
        }

private String buildSelectedItemString() {
        StringBuilder sb = new StringBuilder();
        boolean foundOne = false;

        for (int i = 0; i < items.size(); ++i) {
        if (selection[i]) {
        if (foundOne) {
        sb.append(", ");
        }

        foundOne = true;

        sb.append(items.get(i).getName());
        }
        }

        return sb.toString();
        }

public ArrayList<Item> getSelectedItems() {
        ArrayList<Item> selectedItems = new ArrayList<>();

        for (int i = 0; i < items.size(); ++i) {
        if (selection[i]) {
        selectedItems.add(items.get(i));
        }
        }

        return selectedItems;
        }

        public String getSelectedItemsAsString() {
                StringBuilder sb = new StringBuilder();
                boolean foundOne = false;

                for (int i = 0; i < items.size(); ++i) {
                        if (selection[i]) {
                                if (foundOne) {
                                        sb.append(", ");
                                }
                                foundOne = true;
                                sb.append(items.get(i).getName());
                        }
                }
                return sb.toString();
        }
        }