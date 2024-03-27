package org.intelehealth.ekalarogya.builder;

import android.app.Activity;
import android.content.ContentValues;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import org.intelehealth.ekalarogya.R;
import org.intelehealth.ekalarogya.databinding.LayoutPrescriptionBinding;
import org.intelehealth.ekalarogya.knowledgeEngine.Node;
import org.intelehealth.ekalarogya.models.ClsDoctorDetails;
import org.intelehealth.ekalarogya.models.Patient;
import org.intelehealth.ekalarogya.models.VitalsObject;
import org.intelehealth.ekalarogya.utilities.DateAndTimeUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class PrescriptionBuilder {

    private final Activity context;
    private final LayoutPrescriptionBinding binding;

    public PrescriptionBuilder(Activity context) {
        this.context = context;
        binding = LayoutPrescriptionBinding.inflate(LayoutInflater.from(context));
    }

    public void setPatientData(Patient patient, String visitDate) {
        String name = patient.getFirst_name().concat(" ").concat(patient.getLast_name());
        String age = context.getString(R.string.prescription_age, String.valueOf(DateAndTimeUtils.getAge(patient.getDate_of_birth(), context)));
        String gender = context.getString(R.string.prescription_gender, patient.getGender());

        String addressLine1 = checkValueAndReturnNA(patient.getAddress1());
        String addressLine2 = checkValueAndReturnNA(patient.getAddress2());
        String postalCode = checkValueAndReturnNA(patient.getPostal_code());

        String address = context.getString(R.string.prescription_address, addressLine1.concat(",").concat(addressLine2).concat(", ").concat(postalCode));
        String openMrsId = context.getString(R.string.prescription_patient_id, patient.getOpenmrs_id());
        String dateOfVisit = context.getString(R.string.prescription_date_of_visit, visitDate);

        binding.tvPatientName.setText(name);
        binding.tvPatientAge.setText(age);
        binding.tvPatientGender.setText(gender);
        binding.tvPatientAddress.setText(address);
        binding.tvPatientId.setText(openMrsId);
        binding.tvPatientDateOfVisit.setText(dateOfVisit);
    }

    public void setVitals(VitalsObject vitalsObject) {
        String height = context.getString(R.string.prescription_height, checkValueAndReturnNA(vitalsObject.getHeight()));
        String weight = context.getString(R.string.prescription_weight, checkValueAndReturnNA(vitalsObject.getWeight()));
        String bmi = context.getString(R.string.prescription_bmi, checkValueAndReturnNA(vitalsObject.getBmi()));
        String bpSys = checkValueAndReturnNA(vitalsObject.getBpsys());
        String bpDia = checkValueAndReturnNA(vitalsObject.getBpdia());
        String bloodPressure = context.getString(R.string.prescription_bp, bpSys.concat(" / ").concat(bpDia));
        String pulse = context.getString(R.string.prescription_pulse, checkValueAndReturnNA(vitalsObject.getPulse()));
        String temperature = context.getString(R.string.prescription_temperature, checkValueAndReturnNA(vitalsObject.getTemperature()));
        String respiratoryRate = context.getString(R.string.prescription_respiratory_rate, checkValueAndReturnNA(vitalsObject.getResp()));
        String spO2 = context.getString(R.string.prescription_spo2, checkValueAndReturnNA(vitalsObject.getSpo2()));
        String haemoglobin = context.getString(R.string.prescription_haemoglobin, checkValueAndReturnNA(vitalsObject.getHsb()));
        String bloodGroup = context.getString(R.string.prescription_blood_group, checkValueAndReturnNA(vitalsObject.getBlood()));
        String sugarFasting = context.getString(R.string.prescription_sugar_fasting, checkValueAndReturnNA(vitalsObject.getSugarfasting()));
        String sugarRandom = context.getString(R.string.prescription_sugar_random, checkValueAndReturnNA(vitalsObject.getSugarrandom()));

        binding.tvVitalsHeight.setText(height);
        binding.tvVitalsWeight.setText(weight);
        binding.tvVitalsBmi.setText(bmi);
        binding.tvVitalsBp.setText(bloodPressure);
        binding.tvVitalsPulse.setText(pulse);
        binding.tvVitalsTemperature.setText(temperature);
        binding.tvVitalsRespiratoryRate.setText(respiratoryRate);
        binding.tvVitalsSpo2.setText(spO2);
        binding.tvVitalsHaemoglobin.setText(haemoglobin);
        binding.tvVitalsBloodGroup.setText(bloodGroup);
        binding.tvVitalsSugarFasting.setText(sugarFasting);
        binding.tvVitalsSugarRandom.setText(sugarRandom);
    }

    public void setComplaintData(String complaints) {
        checkDataValidOrHideViews(binding.tvPresentingComplaints, binding.tvPresentingComplaintsData, complaints);
    }

    public void setDiagnosis(String diagnosis) {
        checkDataValidOrHideViews(binding.tvDiagnosis, binding.tvDiagnosisData, diagnosis);
    }

    public void setMedication(String medication) {
        if (medication.contains("\n")) {
            String[] medicationData = medication.split("\n");
            medication = "";

            for (String data : medicationData) {
                medication = medication.concat(Node.big_bullet).concat(" ").concat(data).concat("\n");
            }
        }
        checkDataValidOrHideViews(binding.tvMedication, binding.tvMedicationData, medication);
    }

    public void setTests(String tests) {
        tests = tests.replaceAll("\n\n", "\n");
        checkDataValidOrHideViews(binding.tvTests, binding.tvTestsData, tests);
    }

    public void setAdvice(String advice) {
        if (advice.contains("\n")) {
            String[] adviceData = advice.split("\n");
            advice = "";

            for (String data : adviceData) {
                if (!data.isEmpty()) {
                    advice = advice
                            .concat(Node.big_bullet)
                            .concat(" ")
                            .concat(data)
                            .concat("\n");
                }
            }
        }
        checkDataValidOrHideViews(binding.tvGeneralAdvice, binding.tvGeneralAdviceData, advice);
    }

    public void setFollowUp(String followUp) {
        checkDataValidOrHideViews(binding.tvFollowUp, binding.tvFollowUpData, followUp);
    }

    public void setDoctorData(ClsDoctorDetails clsDoctorDetails) {
        if (clsDoctorDetails == null)
            return;

        binding.tvDrSignature.setText(clsDoctorDetails.getTextOfSign());
        binding.tvDrSignature.setTypeface(getSignatureTypeface(clsDoctorDetails.getFontOfSign()));
        binding.tvDrName.setText(checkValueAndReturnNA(clsDoctorDetails.getName()));

        String degreeSpecialization = "";
        if (clsDoctorDetails.getQualification() != null) {
            degreeSpecialization = clsDoctorDetails.getQualification();
        }

        if (clsDoctorDetails.getSpecialization() != null) {
            if (degreeSpecialization.isEmpty() || degreeSpecialization.isBlank()) {
                degreeSpecialization = degreeSpecialization.concat(", ").concat(clsDoctorDetails.getSpecialization());
            } else {
                degreeSpecialization = clsDoctorDetails.getSpecialization();
            }
        }

        binding.tvDrDegreeSpecialization.setText(degreeSpecialization);

        binding.tvDrEmail.setText(context.getString(R.string.prescription_dr_email, checkValueAndReturnNA(clsDoctorDetails.getEmailId())));
        binding.tvDrRegistration.setText(context.getString(R.string.prescription_dr_registration, checkValueAndReturnNA(clsDoctorDetails.getRegistrationNumber())));
    }

    private Typeface getSignatureTypeface(String font) {
        String directory = "";

        if (font.equalsIgnoreCase("youthness")) {
            directory = "fonts/Youthness.ttf";
        } else if (font.equalsIgnoreCase("asem")) {
            directory = "fonts/Asem.otf";
        } else if (font.equalsIgnoreCase("arty")) {
            directory = "fonts/Arty.otf";
        } else if (font.equalsIgnoreCase("almondita")) {
            directory = "fonts/almondita.ttf";
        }

        return Typeface.createFromAsset(context.getAssets(), directory);
    }

    public void build(String fileName) {
        DisplayMetrics metrics = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Display display = context.getDisplay();
            if (display != null) {
                display.getRealMetrics(metrics);
            }
        } else {
            WindowManager windowManager = context.getWindowManager();
            windowManager.getDefaultDisplay().getMetrics(metrics);
        }

        // Measure the view at the exact width and unspecified height to determine the total height needed
        binding.getRoot().measure(View.MeasureSpec.makeMeasureSpec(metrics.widthPixels, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        binding.getRoot().layout(0, 0, metrics.widthPixels, binding.getRoot().getMeasuredHeight());

        int viewHeight = binding.getRoot().getMeasuredHeight();
        int viewWidth = metrics.widthPixels;

        // Create a PDF document with a single page that matches the content height
        PdfDocument pdfDocument = new PdfDocument();

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(viewWidth, viewHeight, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        binding.getRoot().draw(canvas);

        pdfDocument.finishPage(page);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

            Uri uri = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                try (OutputStream out = context.getContentResolver().openOutputStream(uri)) {
                    pdfDocument.writeTo(out);
                    pdfDocument.close();
                } catch (IOException e) {
                    throw new RuntimeException("Error saving PDF", e);
                }
            }
        } else {
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File filePath = new File(downloadsDir, fileName);

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                pdfDocument.writeTo(fos);
                pdfDocument.close();
                fos.close();
            } catch (IOException e) {
                throw new RuntimeException("Error saving PDF", e);
            }
        }
    }

    private String checkValueAndReturnNA(String value) {
        if (value == null || value.isEmpty() || value.isBlank()) {
            return "NA";
        } else {
            return value;
        }
    }

    private void checkDataValidOrHideViews(TextView title, TextView subtitle, String data) {
        if (data == null || data.isBlank() || data.isEmpty()) {
            title.setVisibility(View.GONE);
            subtitle.setVisibility(View.GONE);
        } else {
            subtitle.setText(data);
        }
    }
}