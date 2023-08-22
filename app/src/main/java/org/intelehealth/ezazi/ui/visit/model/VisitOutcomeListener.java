package org.intelehealth.ezazi.ui.visit.model;

/**
 * Created by Vaghela Mithun R. on 21-08-2023 - 19:36.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public interface VisitOutcomeListener {
    void onOutcomeReceived(String outcome, String[] reason);
}
