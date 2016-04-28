package edu.jhu.bme.cbid.healthassistantsclient;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;

import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import edu.jhu.bme.cbid.healthassistantsclient.objects.Node;

/**
 * Created by tusharjois on 3/22/16.
 */
public class HelperMethods {

    public static int getAge(String s) {
        if (s == null) return 0;

        String[] components = s.split("\\-");

        int year = Integer.parseInt(components[0]);
        int month = Integer.parseInt(components[1]);
        int day = Integer.parseInt(components[2]);

        LocalDate birthdate = new LocalDate(year, month, day);          //Birth date
        LocalDate now = new LocalDate();                    //Today's date
        Period period = new Period(birthdate, now, PeriodType.yearMonthDay());

        return period.getYears();
    }

    public static JSONObject encodeJSON(Context context, String fileName) {
        String raw_json = null;
        JSONObject encoded = null;
        try {
            InputStream is = context.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            raw_json = new String(buffer, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {

            encoded = new JSONObject(raw_json);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return encoded;

    }

    public static void subLevelQuestion(final Node node, final Activity context, final NodeAdapter callingAdapter){
        /*
        I could just grab the node, and then grab all of its options
        Then i can take all of the options, for loop for all its text, and then set an onclicklistener
        so when i get a click, i just check the node at that index, and then do the functions appropriate

        Functions though:
        I need to check the input method
        I need to check if its terminal
        I need to set it to selected in the first place
        Wait, but do I do all this shit on the select?
        When the option is clicked, I need to present a new screen, but what about when they press okay?
        What do I do then?
        So say I have a list of options, and then they are able to select multiple options
        When they click okay, I need to change all of them to selected, and then I need to grab that list of select indices
        Check each of the nodes at those indices, and then do what needs to be done
        But I need to check on click listeners on each item too, to make sure if it has an input type



        Okay, so item onclicks to check if its an input type
        When okay is clicked, I need to checked the selected ones, set their nodes as selected, and then
        I need to see if those nodes are terminal
        if they are not, i may need to display several dialogs
         */
        node.setSelected();
        List<Node> mNodes = node.getOptionsList();
        final subNodeAdapter adapter = new subNodeAdapter(context, R.layout.list_item_subquestion, mNodes);
        final AlertDialog.Builder subQuestion = new AlertDialog.Builder(context);
        subQuestion.setTitle(node.text());
        ListView listView = new ListView(context);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        listView.setClickable(true);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                node.getOption(position).toggleSelected();
                adapter.notifyDataSetChanged();
                if(node.getOption(position).type() != null){
                    HelperMethods.subHandleQuestion(node.getOption(position), context, adapter);
                }
            }
        });
        subQuestion.setView(listView);
        subQuestion.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                node.changeText(node.generateLanguage());
                callingAdapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        subQuestion.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                node.toggleSelected();
                callingAdapter.notifyDataSetChanged();
                dialog.cancel();
            }
        });
        subQuestion.show();


    }

    public static void handleQuestion(Node questionNode, final Activity context, final NodeAdapter adapter) {
        String type = questionNode.type();
        switch (type) {
            case "text":
                HelperMethods.askText(questionNode, context, adapter);
                break;
            case "date":
                HelperMethods.askDate(questionNode, context, adapter);
                break;
            case "location":
                HelperMethods.askLocation(questionNode, context, adapter);
                break;
            case "number":
                HelperMethods.askNumber(questionNode, context, adapter);
                break;
            case "area":
                HelperMethods.askArea(questionNode, context, adapter);
                break;
            case "duration":
                HelperMethods.askDuration(questionNode, context, adapter);
                break;
            case "range":
                HelperMethods.askRange(questionNode, context, adapter);
                break;
            case "frequency":
                HelperMethods.askFrequency(questionNode, context, adapter);
                break;
        }
    }

    public static void askText(final Node node, Activity context, final NodeAdapter adapter) {
        final AlertDialog.Builder textInput = new AlertDialog.Builder(context);
        textInput.setTitle(R.string.question_text_input);
        final EditText dialogEditText = new EditText(context);
        dialogEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        textInput.setView(dialogEditText);
        textInput.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                node.addLanguage(dialogEditText.getText().toString());
                node.changeText(node.language());
                node.setSelected();
                adapter.notifyDataSetChanged();
                dialog.dismiss();
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

    public static void askDate(final Node node, final Activity context, final NodeAdapter adapter) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(context,
                android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(0);
                        cal.set(year, monthOfYear, dayOfMonth);
                        Date date = cal.getTime();
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MMM/yyyy", context.getResources().getConfiguration().locale);
                        String dateString = simpleDateFormat.format(date);
                        node.addLanguage(dateString);
                        node.changeText(node.language());
                        node.setSelected();
                        adapter.notifyDataSetChanged();
                        //TODO:: Check if the language is actually what is intended to be displayed
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.setTitle(R.string.question_date_picker);
        datePickerDialog.show();
    }

    public static void askLocation(final Node node, Activity context, final NodeAdapter adapter) {

        final AlertDialog.Builder locationDialog = new AlertDialog.Builder(context);
        locationDialog.setTitle(R.string.question_location_picker);

        //TODO: Issue #51 on GitHub


    }

    public static void askNumber(final Node node, Activity context, final NodeAdapter adapter) {

        final AlertDialog.Builder numberDialog = new AlertDialog.Builder(context);
        numberDialog.setTitle(R.string.question_number_picker);
        final LayoutInflater inflater = context.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_1_number_picker, null);
        numberDialog.setView(convertView);
        final NumberPicker numberPicker = (NumberPicker) convertView.findViewById(R.id.dialog_1_number_picker);
        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(100);
        numberDialog.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                numberPicker.setValue(numberPicker.getValue());
                String value = String.valueOf(numberPicker.getValue());
                node.addLanguage(" " + value);
                node.changeText(value);
                node.setSelected();
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        numberDialog.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });
        numberDialog.show();

    }

    public static void askArea(final Node node, Activity context, final NodeAdapter adapter) {

        final AlertDialog.Builder areaDialog = new AlertDialog.Builder(context);
        areaDialog.setTitle(R.string.question_area_picker);
        final LayoutInflater inflater = context.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_2_numbers_picker, null);
        areaDialog.setView(convertView);
        final NumberPicker widthPicker = (NumberPicker) convertView.findViewById(R.id.dialog_2_numbers_quantity);
        final NumberPicker lengthPicker = (NumberPicker) convertView.findViewById(R.id.dialog_2_numbers_unit);
        final TextView middleText = (TextView) convertView.findViewById(R.id.dialog_2_numbers_text);
        middleText.setText("X");

        widthPicker.setMinValue(0);
        widthPicker.setMaxValue(100);
        lengthPicker.setMinValue(0);
        lengthPicker.setMaxValue(100);

        areaDialog.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                widthPicker.setValue(widthPicker.getValue());
                lengthPicker.setValue(lengthPicker.getValue());
                String durationString = String.valueOf(widthPicker.getValue()) + " X " + lengthPicker.getValue();
                node.addLanguage(" " + durationString);
                node.changeText(durationString);
                node.setSelected();
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        areaDialog.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        areaDialog.show();

    }

    public static void askRange(final Node node, Activity context, final NodeAdapter adapter) {

        final AlertDialog.Builder rangeDialog = new AlertDialog.Builder(context);
        rangeDialog.setTitle(R.string.question_range_picker);
        final LayoutInflater inflater = context.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_2_numbers_picker, null);
        rangeDialog.setView(convertView);
        final NumberPicker startPicker = (NumberPicker) convertView.findViewById(R.id.dialog_2_numbers_quantity);
        final NumberPicker endPicker = (NumberPicker) convertView.findViewById(R.id.dialog_2_numbers_unit);
        final TextView middleText = (TextView) convertView.findViewById(R.id.dialog_2_numbers_text);
        middleText.setText(" - ");

        startPicker.setMinValue(0);
        startPicker.setMaxValue(100);
        endPicker.setMinValue(0);
        endPicker.setMaxValue(100);
        rangeDialog.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startPicker.setValue(startPicker.getValue());
                endPicker.setValue(endPicker.getValue());
                String durationString = String.valueOf(startPicker.getValue()) + " to " + endPicker.getValue();
                //TODO gotta get the units of the range somehow. gotta see what they look like first
                node.addLanguage(" " + durationString);
                node.changeText(durationString);
                node.setSelected();
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        rangeDialog.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        rangeDialog.show();
    }

    public static void askFrequency(final Node node, Activity context, final NodeAdapter adapter) {

        final AlertDialog.Builder frequencyDialog = new AlertDialog.Builder(context);
        frequencyDialog.setTitle(R.string.question_frequency_picker);
        final LayoutInflater inflater = context.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_2_numbers_picker, null);
        frequencyDialog.setView(convertView);
        final NumberPicker quantityPicker = (NumberPicker) convertView.findViewById(R.id.dialog_2_numbers_quantity);
        final NumberPicker unitPicker = (NumberPicker) convertView.findViewById(R.id.dialog_2_numbers_unit);
        final TextView middleText = (TextView) convertView.findViewById(R.id.dialog_2_numbers_text);
        middleText.setVisibility(View.GONE);
        final String[] units = new String[]{"per Hour", "per Day", "Per Week", "per Month", "per Year"};
        final String[] doctorUnits = new String[]{"times per hour", "time per day", "times per week", "times per month", "times per year"};
        unitPicker.setDisplayedValues(units);
        quantityPicker.setMinValue(0);
        quantityPicker.setMaxValue(24);
        unitPicker.setMinValue(0);
        unitPicker.setMaxValue(4);
        frequencyDialog.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                quantityPicker.setValue(quantityPicker.getValue());
                unitPicker.setValue(unitPicker.getValue());
                String durationString = String.valueOf(quantityPicker.getValue()) + " " + doctorUnits[unitPicker.getValue()];
                node.addLanguage(" " + durationString);
                node.changeText(durationString);
                node.setSelected();
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        frequencyDialog.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        frequencyDialog.show();

    }

    public static void askDuration(final Node node, Activity context, final NodeAdapter adapter) {
        final AlertDialog.Builder durationDialog = new AlertDialog.Builder(context);
        durationDialog.setTitle(R.string.question_duration_picker);
        final LayoutInflater inflater = context.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_2_numbers_picker, null);
        durationDialog.setView(convertView);
        final NumberPicker quantityPicker = (NumberPicker) convertView.findViewById(R.id.dialog_2_numbers_quantity);
        final NumberPicker unitPicker = (NumberPicker) convertView.findViewById(R.id.dialog_2_numbers_unit);
        final TextView middleText = (TextView) convertView.findViewById(R.id.dialog_2_numbers_text);
        middleText.setVisibility(View.GONE);
        final String[] units = new String[]{"Hours", "Days", "Weeks", "Months", "Years"};
        unitPicker.setDisplayedValues(units);
        quantityPicker.setMinValue(0);
        quantityPicker.setMaxValue(24);
        unitPicker.setMinValue(0);
        unitPicker.setMaxValue(4);
        durationDialog.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                quantityPicker.setValue(quantityPicker.getValue());
                unitPicker.setValue(unitPicker.getValue());
                String durationString = String.valueOf(quantityPicker.getValue()) + " " + units[unitPicker.getValue()];
                node.addLanguage(" " + durationString);
                node.changeText(durationString);
                node.setSelected();
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        durationDialog.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        durationDialog.show();
    }

    public static void subHandleQuestion(Node questionNode, final Activity context, final subNodeAdapter adapter) {
        String type = questionNode.type();
        switch (type) {
            case "text":
                HelperMethods.subAskText(questionNode, context, adapter);
                break;
            case "date":
                HelperMethods.subAskDate(questionNode, context, adapter);
                break;
            case "location":
                HelperMethods.subAskLocation(questionNode, context, adapter);
                break;
            case "number":
                HelperMethods.subAskNumber(questionNode, context, adapter);
                break;
            case "area":
                HelperMethods.subAskArea(questionNode, context, adapter);
                break;
            case "duration":
                HelperMethods.subAskDuration(questionNode, context, adapter);
                break;
            case "range":
                HelperMethods.subAskRange(questionNode, context, adapter);
                break;
            case "frequency":
                HelperMethods.subAskFrequency(questionNode, context, adapter);
                break;
        }
    }

    public static void subAskText(final Node node, Activity context, final subNodeAdapter adapter) {
        final AlertDialog.Builder textInput = new AlertDialog.Builder(context);
        textInput.setTitle(R.string.question_text_input);
        final EditText dialogEditText = new EditText(context);
        dialogEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        textInput.setView(dialogEditText);
        textInput.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                node.addLanguage(dialogEditText.getText().toString());
                node.changeText(node.language());
                node.setSelected();
                adapter.notifyDataSetChanged();
                dialog.dismiss();
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

    public static void subAskDate(final Node node, final Activity context, final subNodeAdapter adapter) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(context,
                android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(0);
                        cal.set(year, monthOfYear, dayOfMonth);
                        Date date = cal.getTime();
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MMM/yyyy", context.getResources().getConfiguration().locale);
                        String dateString = simpleDateFormat.format(date);
                        node.addLanguage(dateString);
                        node.changeText(node.language());
                        node.setSelected();
                        adapter.notifyDataSetChanged();
                        //TODO:: Check if the language is actually what is intended to be displayed
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.setTitle(R.string.question_date_picker);
        datePickerDialog.show();
    }

    public static void subAskLocation(final Node node, Activity context, final subNodeAdapter adapter) {

        final AlertDialog.Builder locationDialog = new AlertDialog.Builder(context);
        locationDialog.setTitle(R.string.question_location_picker);

        //TODO: Issue #51 on GitHub


    }

    public static void subAskNumber(final Node node, Activity context, final subNodeAdapter adapter) {

        final AlertDialog.Builder numberDialog = new AlertDialog.Builder(context);
        numberDialog.setTitle(R.string.question_number_picker);
        final LayoutInflater inflater = context.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_1_number_picker, null);
        numberDialog.setView(convertView);
        final NumberPicker numberPicker = (NumberPicker) convertView.findViewById(R.id.dialog_1_number_picker);
        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(100);
        numberDialog.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                numberPicker.setValue(numberPicker.getValue());
                String value = String.valueOf(numberPicker.getValue());
                node.addLanguage(" " + value);
                node.changeText(value);
                node.setSelected();
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        numberDialog.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });
        numberDialog.show();

    }

    public static void subAskArea(final Node node, Activity context, final subNodeAdapter adapter) {

        final AlertDialog.Builder areaDialog = new AlertDialog.Builder(context);
        areaDialog.setTitle(R.string.question_area_picker);
        final LayoutInflater inflater = context.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_2_numbers_picker, null);
        areaDialog.setView(convertView);
        final NumberPicker widthPicker = (NumberPicker) convertView.findViewById(R.id.dialog_2_numbers_quantity);
        final NumberPicker lengthPicker = (NumberPicker) convertView.findViewById(R.id.dialog_2_numbers_unit);
        final TextView middleText = (TextView) convertView.findViewById(R.id.dialog_2_numbers_text);
        middleText.setText("X");

        widthPicker.setMinValue(0);
        widthPicker.setMaxValue(100);
        lengthPicker.setMinValue(0);
        lengthPicker.setMaxValue(100);

        areaDialog.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                widthPicker.setValue(widthPicker.getValue());
                lengthPicker.setValue(lengthPicker.getValue());
                String durationString = String.valueOf(widthPicker.getValue()) + " X " + lengthPicker.getValue();
                node.addLanguage(" " + durationString);
                node.changeText(durationString);
                node.setSelected();
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        areaDialog.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        areaDialog.show();

    }

    public static void subAskRange(final Node node, Activity context, final subNodeAdapter adapter) {

        final AlertDialog.Builder rangeDialog = new AlertDialog.Builder(context);
        rangeDialog.setTitle(R.string.question_range_picker);
        final LayoutInflater inflater = context.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_2_numbers_picker, null);
        rangeDialog.setView(convertView);
        final NumberPicker startPicker = (NumberPicker) convertView.findViewById(R.id.dialog_2_numbers_quantity);
        final NumberPicker endPicker = (NumberPicker) convertView.findViewById(R.id.dialog_2_numbers_unit);
        final TextView middleText = (TextView) convertView.findViewById(R.id.dialog_2_numbers_text);
        middleText.setText(" - ");

        startPicker.setMinValue(0);
        startPicker.setMaxValue(100);
        endPicker.setMinValue(0);
        endPicker.setMaxValue(100);
        rangeDialog.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startPicker.setValue(startPicker.getValue());
                endPicker.setValue(endPicker.getValue());
                String durationString = String.valueOf(startPicker.getValue()) + " to " + endPicker.getValue();
                //TODO gotta get the units of the range somehow. gotta see what they look like first
                node.addLanguage(" " + durationString);
                node.changeText(durationString);
                node.setSelected();
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        rangeDialog.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        rangeDialog.show();
    }

    public static void subAskFrequency(final Node node, Activity context, final subNodeAdapter adapter) {

        final AlertDialog.Builder frequencyDialog = new AlertDialog.Builder(context);
        frequencyDialog.setTitle(R.string.question_frequency_picker);
        final LayoutInflater inflater = context.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_2_numbers_picker, null);
        frequencyDialog.setView(convertView);
        final NumberPicker quantityPicker = (NumberPicker) convertView.findViewById(R.id.dialog_2_numbers_quantity);
        final NumberPicker unitPicker = (NumberPicker) convertView.findViewById(R.id.dialog_2_numbers_unit);
        final TextView middleText = (TextView) convertView.findViewById(R.id.dialog_2_numbers_text);
        middleText.setVisibility(View.GONE);
        final String[] units = new String[]{"per Hour", "per Day", "Per Week", "per Month", "per Year"};
        final String[] doctorUnits = new String[]{"times per hour", "time per day", "times per week", "times per month", "times per year"};
        unitPicker.setDisplayedValues(units);
        quantityPicker.setMinValue(0);
        quantityPicker.setMaxValue(24);
        unitPicker.setMinValue(0);
        unitPicker.setMaxValue(4);
        frequencyDialog.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                quantityPicker.setValue(quantityPicker.getValue());
                unitPicker.setValue(unitPicker.getValue());
                String durationString = String.valueOf(quantityPicker.getValue()) + " " + doctorUnits[unitPicker.getValue()];
                node.addLanguage(" " + durationString);
                node.changeText(durationString);
                node.setSelected();
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        frequencyDialog.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        frequencyDialog.show();

    }

    public static void subAskDuration(final Node node, Activity context, final subNodeAdapter adapter) {
        final AlertDialog.Builder durationDialog = new AlertDialog.Builder(context);
        durationDialog.setTitle(R.string.question_duration_picker);
        final LayoutInflater inflater = context.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_2_numbers_picker, null);
        durationDialog.setView(convertView);
        final NumberPicker quantityPicker = (NumberPicker) convertView.findViewById(R.id.dialog_2_numbers_quantity);
        final NumberPicker unitPicker = (NumberPicker) convertView.findViewById(R.id.dialog_2_numbers_unit);
        final TextView middleText = (TextView) convertView.findViewById(R.id.dialog_2_numbers_text);
        middleText.setVisibility(View.GONE);
        final String[] units = new String[]{"Hours", "Days", "Weeks", "Months", "Years"};
        unitPicker.setDisplayedValues(units);
        quantityPicker.setMinValue(0);
        quantityPicker.setMaxValue(24);
        unitPicker.setMinValue(0);
        unitPicker.setMaxValue(4);
        durationDialog.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                quantityPicker.setValue(quantityPicker.getValue());
                unitPicker.setValue(unitPicker.getValue());
                String durationString = String.valueOf(quantityPicker.getValue()) + " " + units[unitPicker.getValue()];
                node.addLanguage(" " + durationString);
                node.changeText(durationString);
                node.setSelected();
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        durationDialog.setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        durationDialog.show();
    }

}
