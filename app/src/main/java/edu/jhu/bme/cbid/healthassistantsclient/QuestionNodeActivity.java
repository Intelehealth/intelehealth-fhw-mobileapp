package edu.jhu.bme.cbid.healthassistantsclient;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.NumberPicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import edu.jhu.bme.cbid.healthassistantsclient.objects.Knowledge;
import edu.jhu.bme.cbid.healthassistantsclient.objects.Node;


public class QuestionNodeActivity extends AppCompatActivity {

    final String LOG_TAG = "Question Node Activity";

    Integer patientID = null;
    Knowledge mKnowledge;
    ExpandableListView questionListView;
    String mFileName = "generic.json";
    int complaintNumber = 0;
    HashMap<String, String> complaintDetails;
    ArrayList<String> complaints;
    List<Node> complaintsNodes;
    Node currentNode;
    NodeAdapter adapter;

    String specialString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        Bundle bundle = getIntent().getExtras();
//        patientID = bundle.getInt("patientID");
//        complaints = bundle.getStringArrayList("complaints");

        complaints = new ArrayList<>();
        complaints.add("Difficulty in Breathing");

        mKnowledge = new Knowledge(HelperMethods.encodeJSON(this, mFileName));
        complaintsNodes = new ArrayList<>();
        for (int i = 0; i < complaints.size(); i++) {
            complaintsNodes.add(mKnowledge.getComplaint(complaints.get(i)));
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_node);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        questionListView = (ExpandableListView) findViewById(R.id.complaint_question_expandable_list_view);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i = 0; i < adapter.getGroupCount(); i++) {
                    if (!currentNode.getOption(i).isSelected()) {
                        questionsMissing();
                        questionListView.expandGroup(i);
                        break;
                    }
                }
                if (complaintNumber < complaints.size() - 1) {

                    //TODO: Build the string for this specific complaint before moving onto the next one
                    complaintNumber++;
                    setupQuestions(complaintNumber);
                }
            }
        });

        setupQuestions(complaintNumber);

        questionListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Log.d(LOG_TAG, currentNode.getOption(groupPosition).getOption(childPosition).language());
                currentNode.getOption(groupPosition).getOption(childPosition).toggleSelected();
                if (currentNode.getOption(groupPosition).anySubSelected()) {
                    currentNode.getOption(groupPosition).setSelected();
                } else {
                    currentNode.getOption(groupPosition).setUnselected();
                }
                adapter.notifyDataSetChanged();

                Node question = currentNode.getOption(groupPosition).getOption(childPosition);
                if (question.type() != null) {
                    handleQuestion(question);
                    adapter.notifyDataSetChanged();
                }
                if (!question.isTerminal()) {
                    displayNextLevel(question);
                }

                //nextQuestion(groupPosition);
                return false;

            }
        });


    }

    private void setupQuestions(int complaintIndex) {
        currentNode = mKnowledge.getComplaint(complaints.get(complaintIndex));
        adapter = new NodeAdapter(this, currentNode, this.getClass().getSimpleName());
        questionListView.setAdapter(adapter);
        questionListView.setChoiceMode(ExpandableListView.CHOICE_MODE_MULTIPLE);
        questionListView.expandGroup(0);
        setTitle(currentNode.text());
    }

    public void questionsMissing() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(R.string.question_answer_all);
        alertDialogBuilder.setNeutralButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }

    private void nextQuestion(int groupPosition) {
        if (groupPosition < adapter.getGroupCount() - 1) {
            questionListView.collapseGroup(groupPosition);
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            questionListView.expandGroup(groupPosition + 1);
        }
    }

    private void handleQuestion(Node questionNode) {
        String type = questionNode.type();
        switch (type) {
            case "text":
                askText(questionNode);
                break;
            case "date":
                askDate(questionNode);
                break;
            case "location":
                askLocation(questionNode);
                break;
            case "number":
                askNumber(questionNode);
                break;
            case "area":
                askArea(questionNode);
                break;
            case "duration":
                askDuration(questionNode);
                break;
            case "range":
                askRange(questionNode);
                break;
            case "frequency":
                askFrequency(questionNode);
                break;
        }
    }

    private void displayNextLevel(Node node) {

    }

    private void askText(final Node node) {
        final AlertDialog.Builder textInput = new AlertDialog.Builder(QuestionNodeActivity.this);
        textInput.setTitle(R.string.question_text_input);
        final EditText dialogEditText = new EditText(this);
        dialogEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        textInput.setView(dialogEditText);
        textInput.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                node.addLanguage(dialogEditText.getText().toString());
                node.changeText(node.language());
            }
        });
        textInput.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        textInput.show();
    }

    private void askDate(final Node node) {

        final AlertDialog.Builder textInput = new AlertDialog.Builder(QuestionNodeActivity.this);
        textInput.setTitle(R.string.question_date_picker);
        //DatePicker datePicker = new DatePicker()


        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(QuestionNodeActivity.this,
                android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(0);
                        cal.set(year, monthOfYear, dayOfMonth);
                        Date date = cal.getTime();
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MMM/yyyy", getResources().getConfiguration().locale);
                        String dateString = simpleDateFormat.format(date);
                        node.addLanguage(dateString);
                        node.changeText(node.language());
                        //TODO:: Check if the language is actually what is intended to be displayed
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
    }

    private void askLocation(final Node node) {

        final AlertDialog.Builder textInput = new AlertDialog.Builder(QuestionNodeActivity.this);
        textInput.setTitle(R.string.question_text_input);
    }

    private void askNumber(final Node node) {

        final AlertDialog.Builder textInput = new AlertDialog.Builder(QuestionNodeActivity.this);
        textInput.setTitle(R.string.question_text_input);
    }

    private void askArea(final Node node) {

        final AlertDialog.Builder textInput = new AlertDialog.Builder(QuestionNodeActivity.this);
        textInput.setTitle(R.string.question_text_input);
    }

    private void askDuration(final Node node) {
        final AlertDialog.Builder durationPicker = new AlertDialog.Builder(QuestionNodeActivity.this);
        durationPicker.setTitle(R.string.question_duration_picker);
        final LayoutInflater inflater = getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_duration_picker, null);
        durationPicker.setView(convertView);
        final NumberPicker quantityPicker = (NumberPicker) convertView.findViewById(R.id.dialog_duration_quantity);
        final NumberPicker unitPicker = (NumberPicker) convertView.findViewById(R.id.dialog_duration_unit);
        final String[] units = new String[]{"Hours", "Days", "Weeks", "Months", "Years"};
        unitPicker.setDisplayedValues(units);
        quantityPicker.setMinValue(0);
        quantityPicker.setMaxValue(24);
        unitPicker.setMinValue(0);
        unitPicker.setMaxValue(4);
        durationPicker.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                quantityPicker.setValue(quantityPicker.getValue());
                unitPicker.setValue(unitPicker.getValue());
                String durationString = String.valueOf(quantityPicker.getValue()) + " " + units[unitPicker.getValue()];
                node.addLanguage(" " + durationString);
                node.changeText(durationString);
                node.setSelected();
                dialog.dismiss();
            }
        });
        durationPicker.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        durationPicker.show();
    }

    private void askRange(final Node node) {

        final AlertDialog.Builder textInput = new AlertDialog.Builder(QuestionNodeActivity.this);
        textInput.setTitle(R.string.question_text_input);
    }

    private void askFrequency(final Node node) {

        final AlertDialog.Builder textInput = new AlertDialog.Builder(QuestionNodeActivity.this);
        textInput.setTitle(R.string.question_text_input);
    }

}
