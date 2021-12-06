package org.intelehealth.msfarogyabharat.activities.visitSummaryActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.intelehealth.msfarogyabharat.R;
import org.intelehealth.msfarogyabharat.activities.complaintNodeActivity.ComplaintNodeActivity;
import org.intelehealth.msfarogyabharat.activities.missedCallResponseActivity.RecordingsAdapter;
import org.intelehealth.msfarogyabharat.activities.physcialExamActivity.PhysicalExamActivity;
import org.intelehealth.msfarogyabharat.app.AppConstants;
import org.intelehealth.msfarogyabharat.app.IntelehealthApplication;
import org.intelehealth.msfarogyabharat.database.dao.EncounterDAO;
import org.intelehealth.msfarogyabharat.database.dao.ImagesDAO;
import org.intelehealth.msfarogyabharat.database.dao.ObsDAO;
import org.intelehealth.msfarogyabharat.models.Add_Doc_Adapter_DataModel;
import org.intelehealth.msfarogyabharat.models.dto.ObsDTO;
import org.intelehealth.msfarogyabharat.utilities.SessionManager;
import org.intelehealth.msfarogyabharat.utilities.UuidDictionary;
import org.intelehealth.msfarogyabharat.utilities.exception.DAOException;

import java.io.File;
import java.util.List;

/**
 * Created By: Prajwal Waingankar on 22-Oct-21
 * Github: prajwalmw
 * Email: prajwalwaingankar@gmail.com
 */

public class VisitSummaryAdapter extends RecyclerView.Adapter<VisitSummaryAdapter.VisitSummaryViewHolder> {
    Context context, visitsumContext;
    List<String> complaintList;
    List<String> visitUuidList, visitStartDatesList;
    List<String> physexamList;
    boolean allVisitsEnded = false;
    String currentvisituuid;
    String complaint, physexam, visitid;
    Add_Doc_Adapter_DataModel model;
    File obsImgdir = new File(AppConstants.IMAGE_PATH);
    VisitSummaryActivity summaryActivity = new VisitSummaryActivity();
    SessionManager sessionManager;
OnVisitItemClickListner onClickingIteListner;

    public VisitSummaryAdapter(Context context, Context visitsumContext, List<String> visitUuidList, List<String> visitStartDatesList,
                               List<String> complaintList, List<String> physexamList,
                               boolean allVisitsEnded, String currentvisituuid, Add_Doc_Adapter_DataModel model,OnVisitItemClickListner listner) {
        this.context = context;
        this.visitsumContext = visitsumContext;
        this.visitUuidList = visitUuidList;
        this.visitStartDatesList = visitStartDatesList;
        this.complaintList = complaintList;
        this.physexamList = physexamList;
        this.allVisitsEnded = allVisitsEnded;
        this.currentvisituuid = currentvisituuid;
        this.model = model;
        onClickingIteListner=listner;
        sessionManager = new SessionManager(context);
        Log.v("main","allvisitsended: "+ this.allVisitsEnded);
    }

    @Override
    public VisitSummaryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_visitsummary, parent, false);

        return new VisitSummaryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(VisitSummaryAdapter.VisitSummaryViewHolder holder, int position) {
        if(complaintList.size() > 0 && position != complaintList.size())
            complaint = complaintList.get(position);
        else
            complaint = visitsumContext.getString(R.string.something_went_wrong);

        if(physexamList.size() > 0 && position != physexamList.size())
            physexam = physexamList.get(position);
        else
            physexam = visitsumContext.getString(R.string.something_went_wrong);

        if(visitStartDatesList.size() > 0 && position != visitStartDatesList.size())
            holder.textView_caseTitle.setText(visitsumContext.getString(R.string.case_visitsummary) + (position + 1)
                    + " : " + visitStartDatesList.get(position));
        else
            holder.textView_caseTitle.setText(visitsumContext.getString(R.string.case_visitsummary) + (position + 1));

        if(sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
         if(complaint.contains(", Pregnancy Week: ")) {
             complaint = complaint.replace("Pregnancy Week", "गर्भावस्था सप्ताह");
         }
         if(complaint.contains(" weeks")) {
             complaint = complaint.replace("weeks", "हफ़्ते");
         }
            holder.textView_content_complaint.setText(complaint);
        }
        else {
            holder.textView_content_complaint.setText(complaint);
        }

        holder.textView_content_complaint.setText(complaint);
        holder.textView_content_physexam.setText(physexam);
    }

    @Override
    public int getItemCount() {
        return visitUuidList.size();
    }

    public class VisitSummaryViewHolder extends RecyclerView.ViewHolder {
        TextView textView_caseTitle, textView_content_complaint, textView_content_physexam;
        ImageButton imagebutton_edit_complaint, imagebutton_edit_physexam;
        LinearLayout linearlayout_body;

        public VisitSummaryViewHolder(View itemView) {
            super(itemView);
            textView_caseTitle = itemView.findViewById(R.id.textView_caseTitle);
            textView_content_complaint = itemView.findViewById(R.id.textView_content_complaint);
            textView_content_physexam = itemView.findViewById(R.id.textView_content_physexam);
            imagebutton_edit_complaint = itemView.findViewById(R.id.imagebutton_edit_complaint);
            imagebutton_edit_physexam = itemView.findViewById(R.id.imagebutton_edit_physexam);
            linearlayout_body = itemView.findViewById(R.id.linearlayout_body);

            Animation slide_down = AnimationUtils.loadAnimation(context, R.anim.slide_down);
            Animation slide_up = AnimationUtils.loadAnimation(context, R.anim.slide_up);

            textView_caseTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickingIteListner.mItemClicking(getAdapterPosition());
                    if(linearlayout_body.getVisibility() == View.VISIBLE) {
                        linearlayout_body.startAnimation(slide_up);
                        linearlayout_body.setVisibility(View.GONE);
                    }
                    else {
                        linearlayout_body.startAnimation(slide_down);
                        linearlayout_body.setVisibility(View.VISIBLE);
                    }

                    //edit icons visibility handled.
                    //is all visits are Ended for a patient then hide the edit pencil icon too.
                    if(allVisitsEnded) {
                        imagebutton_edit_complaint.setVisibility(View.GONE);
                        imagebutton_edit_physexam.setVisibility(View.GONE);
                    }
                    else {
                        //any one visit is ended i.e. the latest visit is Active so show edit for only that visit ie. the last visit.
                        visitid = visitUuidList.get(getAdapterPosition());
                        if(visitid.equalsIgnoreCase(currentvisituuid) && !allVisitsEnded) {
                            Log.v("main", "position: "+ getAdapterPosition() + ":");
                            // Toast.makeText(context, "good", Toast.LENGTH_SHORT).show();
                            imagebutton_edit_complaint.setVisibility(View.VISIBLE);
                            imagebutton_edit_physexam.setVisibility(View.VISIBLE);
                        }
                        else {
                            // Toast.makeText(context, "bad", Toast.LENGTH_SHORT).show();
                            imagebutton_edit_complaint.setVisibility(View.GONE);
                            imagebutton_edit_physexam.setVisibility(View.GONE);
                        }
                    }
                }
            });

            // Edit of Complaints
            imagebutton_edit_complaint.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final MaterialAlertDialogBuilder complaintDialog = new MaterialAlertDialogBuilder(visitsumContext);
                    complaintDialog.setTitle(visitsumContext.getResources().getString(R.string.visit_summary_complaint));
                  //  final LayoutInflater inflater = getLayoutInflater();
                    final LayoutInflater inflater = LayoutInflater.from(visitsumContext);
                    View convertView = inflater.inflate(R.layout.dialog_edit_entry, null);
                    complaintDialog.setView(convertView);

                    final TextView complaintText = convertView.findViewById(R.id.textView_entry);
                    if (complaintList.size() > 0 && getAdapterPosition() != complaintList.size() && complaintList != null) {

                        String complaintData = "";
                        if(sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
                            if(complaintList.get(getAdapterPosition()).contains(", Pregnancy Week: ")) {
                                complaintData = complaintList.get(getAdapterPosition()).replace("Pregnancy Week", "गर्भावस्था सप्ताह");
                            }
                            if(complaintData.contains(" weeks")) {
                                complaintData = complaintData.replace("weeks", "हफ़्ते");
                            }
                            complaintText.setText(Html.fromHtml(complaintData));
                        }
                        else {
                            complaintText.setText(Html.fromHtml(complaintList.get(getAdapterPosition())));
                        }

                    }
                    else {
                        complaintText.setText(visitsumContext.getString(R.string.something_went_wrong));
                    }

                    complaintText.setEnabled(false);

                    complaintDialog.setPositiveButton(visitsumContext.getResources().getString(R.string.generic_manual_entry),
                            new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            final MaterialAlertDialogBuilder textInput = new MaterialAlertDialogBuilder(visitsumContext);
                            textInput.setTitle(visitsumContext.getResources().getString(R.string.question_text_input));
                            final EditText dialogEditText = new EditText(visitsumContext);

                            if (complaintList != null && getAdapterPosition() != complaintList.size()) {
                                if(complaintList.size() > 0)
                                    dialogEditText.setText(Html.fromHtml(complaintList.get(getAdapterPosition())));
                                else
                                    dialogEditText.setText(visitsumContext.getString(R.string.something_went_wrong));

                            } else {
                                dialogEditText.setText("");
                            }
                            textInput.setView(dialogEditText);
                            textInput.setPositiveButton(visitsumContext.getResources().getString(R.string.generic_ok),
                                    new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String input = dialogEditText.getText().toString();
                                    input = summaryActivity.applyBoldTag(input);
                                   // complaint.setValue(input.replace("\n", "<br>"));
                                    complaintList.add(getAdapterPosition(), input.replace("\n", "<br>"));
//                                complaint.setValue(dialogEditText.getText().toString().replace("\n", "<br>"));
                                    if (complaintList.get(getAdapterPosition()) != null) {
                                        complaintText.setText(Html.fromHtml(complaintList.get(getAdapterPosition())));
                                        textView_content_complaint.setText(Html.fromHtml(complaintList.get(getAdapterPosition())));
                                    }
                                    updateDatabase(complaintList.get(getAdapterPosition()), UuidDictionary.CURRENT_COMPLAINT);
                                    dialog.dismiss();
                                }
                            });
                            textInput.setNeutralButton(visitsumContext.getResources().getString(R.string.generic_cancel),
                                    new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            AlertDialog alertDialog = textInput.show();
                            IntelehealthApplication.setAlertDialogCustomTheme(visitsumContext, alertDialog);
                            dialogInterface.dismiss();
                        }
                    });

                    complaintDialog.setNegativeButton(visitsumContext.getResources().getString(R.string.generic_erase_redo),
                            new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //Deleting the old image in physcial examination
                            if (obsImgdir.exists()) {
                                ImagesDAO imagesDAO = new ImagesDAO();

                                try {
                                    List<String> imageList = imagesDAO.getImages(model.getEncounteruid(), UuidDictionary.COMPLEX_IMAGE_PE);
                                    for (String obsImageUuid : imageList) {
                                        String imageName = obsImageUuid + ".jpg";
                                        new File(obsImgdir, imageName).deleteOnExit();
                                    }
                                    imagesDAO.deleteConceptImages(model.getEncounteruid(), UuidDictionary.COMPLEX_IMAGE_PE);
                                } catch (DAOException e1) {
                                    FirebaseCrashlytics.getInstance().recordException(e1);
                                }
                            }

                            Intent intent1 = new Intent(visitsumContext, ComplaintNodeActivity.class);
                            intent1.putExtra("patientUuid", model.getPatientuuid());
                            intent1.putExtra("visitUuid", model.getVisituuid());
                            intent1.putExtra("encounterUuidAdultIntial", model.getEncounteruid());
                            intent1.putExtra("name", model.getPatientname());
                            intent1.putExtra("float_ageYear_Month", model.getFloat_ageYear_Month());
                            intent1.putExtra("tag", "edit");
                            visitsumContext.startActivity(intent1);
                            dialogInterface.dismiss();
                        }
                    });

                    complaintDialog.setNeutralButton(visitsumContext.getResources().getString(R.string.generic_cancel),
                            new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });

                    //complaintDialog.show();
                    AlertDialog alertDialog = complaintDialog.create();
                    alertDialog.show();
                    Button pb = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    pb.setTextColor(visitsumContext.getResources().getColor((R.color.colorPrimary)));
                    pb.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

                    Button nb = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                    nb.setTextColor(visitsumContext.getResources().getColor((R.color.colorPrimary)));
                    nb.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

                    Button neutralb = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                    neutralb.setTextColor(visitsumContext.getResources().getColor((R.color.colorPrimary)));
                    neutralb.setTypeface(ResourcesCompat.getFont(visitsumContext, R.font.lato_bold));

                    IntelehealthApplication.setAlertDialogCustomTheme(visitsumContext, alertDialog);
                }
            });
            //complaint - end

            //Edit Physical - Start
            imagebutton_edit_physexam.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final MaterialAlertDialogBuilder physicalDialog = new MaterialAlertDialogBuilder(visitsumContext);
                    physicalDialog.setTitle(visitsumContext.getResources().getString(R.string.visit_summary_on_examination));
                    final LayoutInflater inflater = LayoutInflater.from(visitsumContext);
                    View convertView = inflater.inflate(R.layout.dialog_edit_entry, null);
                    physicalDialog.setView(convertView);

                    final TextView physicalText = convertView.findViewById(R.id.textView_entry);
                    if (physexamList != null)
                        if(physexamList.size() > 0 && getAdapterPosition() != physexamList.size())
                            physicalText.setText(Html.fromHtml(physexamList.get(getAdapterPosition())));
                        else
                            physicalText.setText(visitsumContext.getString(R.string.something_went_wrong));

                    physicalText.setEnabled(false);

                    physicalDialog.setPositiveButton(visitsumContext.getResources().getString(R.string.generic_manual_entry),
                            new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            final MaterialAlertDialogBuilder textInput = new MaterialAlertDialogBuilder(visitsumContext);
                            textInput.setTitle(visitsumContext.getResources().getString(R.string.question_text_input));
                            final EditText dialogEditText = new EditText(visitsumContext);
                            if (physexamList != null && getAdapterPosition() != physexamList.size())
                                if(physexamList.size() > 0)
                                    dialogEditText.setText(Html.fromHtml(physexamList.get(getAdapterPosition())));
                                else
                                    dialogEditText.setText(visitsumContext.getString(R.string.something_went_wrong));

                            else
                                dialogEditText.setText("");
                            textInput.setView(dialogEditText);
                            textInput.setPositiveButton(visitsumContext.getResources().getString(R.string.generic_ok),
                                    new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    physexamList.add(getAdapterPosition(), dialogEditText.getText().toString()
                                            .replace("\n", "<br>"));

                                    if (physexamList.get(getAdapterPosition()) != null) {
                                        physicalText.setText(Html.fromHtml(physexamList.get(getAdapterPosition())));
                                        textView_content_physexam.setText(Html.fromHtml(physexamList.get(getAdapterPosition())));
                                    }
                                    updateDatabase(physexamList.get(getAdapterPosition()), UuidDictionary.PHYSICAL_EXAMINATION);
                                    dialog.dismiss();
                                }
                            });
                            textInput.setNegativeButton(visitsumContext.getResources().getString(R.string.generic_cancel),
                                    new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            AlertDialog dialog = textInput.show();
                            IntelehealthApplication.setAlertDialogCustomTheme(visitsumContext, dialog);
                            dialogInterface.dismiss();
                        }
                    });

                    physicalDialog.setNegativeButton(visitsumContext.getString(R.string.generic_erase_redo),
                            new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (obsImgdir.exists()) {
                                ImagesDAO imagesDAO = new ImagesDAO();

                                try {
                                    List<String> imageList = imagesDAO.getImages(model.getEncounteruid(), UuidDictionary.COMPLEX_IMAGE_PE);
                                    for (String obsImageUuid : imageList) {
                                        String imageName = obsImageUuid + ".jpg";
                                        new File(obsImgdir, imageName).deleteOnExit();
                                    }
                                    imagesDAO.deleteConceptImages(model.getEncounteruid(), UuidDictionary.COMPLEX_IMAGE_PE);
                                } catch (DAOException e1) {
                                    FirebaseCrashlytics.getInstance().recordException(e1);
                                }
                            }
                            Intent intent1 = new Intent(visitsumContext, PhysicalExamActivity.class);
                            intent1.putExtra("patientUuid", model.getPatientuuid());
                            intent1.putExtra("visitUuid", model.getVisituuid());
                            intent1.putExtra("encounterUuidAdultIntial", model.getEncounteruid());
                            intent1.putExtra("name", model.getPatientname());
                            intent1.putExtra("float_ageYear_Month", model.getFloat_ageYear_Month());
                            intent1.putExtra("tag", "edit");

                            visitsumContext.startActivity(intent1);
                            dialogInterface.dismiss();
                        }
                    });

                    physicalDialog.setNeutralButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });

                    AlertDialog alertDialog = physicalDialog.create();
                    alertDialog.show();
                    Button pb = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    pb.setTextColor(visitsumContext.getResources().getColor((R.color.colorPrimary)));
                    pb.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

                    Button nb = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                    nb.setTextColor(visitsumContext.getResources().getColor((R.color.colorPrimary)));
                    nb.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

                    Button neutralb = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                    neutralb.setTextColor(visitsumContext.getResources().getColor((R.color.colorPrimary)));
                    neutralb.setTypeface(ResourcesCompat.getFont(visitsumContext, R.font.lato_bold));

                    IntelehealthApplication.setAlertDialogCustomTheme(visitsumContext, alertDialog);
                }
            });
            //Edit Physical - End
        }
    }

    public void updateDatabase(String string, String conceptID) {
      //  sessionManager = new SessionManager(context);
        ObsDTO obsDTO = new ObsDTO();
        ObsDAO obsDAO = new ObsDAO();
        try {
            obsDTO.setConceptuuid(String.valueOf(conceptID));
//          obsDTO.setEncounteruuid(encounterUuidAdultIntial);
            obsDTO.setEncounteruuid(model.getEncounteruid()); //latest visit encounter.
            obsDTO.setCreator(sessionManager.getCreatorID());
            obsDTO.setValue(string);
            obsDTO.setUuid(obsDAO.getObsuuid(model.getEncounteruid(), String.valueOf(conceptID)));

            obsDAO.updateObs(obsDTO);


        } catch (DAOException dao) {
            FirebaseCrashlytics.getInstance().recordException(dao);
        }

        EncounterDAO encounterDAO = new EncounterDAO();
        try {
            encounterDAO.updateEncounterSync("false", model.getEncounteruid());
            encounterDAO.updateEncounterModifiedDate(model.getEncounteruid());
        } catch (DAOException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }


   interface OnVisitItemClickListner{

    void mItemClicking(int pos);
}
}
