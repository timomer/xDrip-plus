package com.eveningoutpost.dexdrip;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import com.eveningoutpost.dexdrip.Models.BgReading;
import com.eveningoutpost.dexdrip.Models.Calibration;
import com.eveningoutpost.dexdrip.Models.Sensor;
import com.eveningoutpost.dexdrip.Models.Treatments;
import com.eveningoutpost.dexdrip.Tables.BgReadingTable;
import com.eveningoutpost.dexdrip.Tables.CalibrationDataTable;
import com.eveningoutpost.dexdrip.UtilityModels.CollectionServiceStarter;
import com.eveningoutpost.dexdrip.UtilityModels.Experience;
import com.eveningoutpost.dexdrip.stats.StatsActivity;
import com.eveningoutpost.dexdrip.utils.DexCollectionType;
import com.eveningoutpost.dexdrip.utils.Preferences;
import com.eveningoutpost.dexdrip.utils.Preferences_Light;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Emma Black on 11/5/14.
 */
public class NavDrawerBuilder {
    private List<Calibration> last_two_calibrations = Calibration.latestValid(2);
    private final List<BgReading> last_two_bgReadings = BgReading.latestUnCalculated(2);
    private final List<BgReading> bGreadings_in_last_30_mins = BgReading.last30Minutes();
    private boolean is_active_sensor = Sensor.isActive();
    private double time_now = new Date().getTime();
    public final List<Intent> nav_drawer_intents = new ArrayList<>();
    public final List<String> nav_drawer_options = new ArrayList<>();

    private static boolean use_note_search = false;

    public NavDrawerBuilder(Context context) {

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean IUnderstand = prefs.getBoolean("I_understand", false);
        if (!IUnderstand) {
            this.nav_drawer_options.add(context.getString(R.string.settings));
            //this.nav_drawer_intents.add(new Intent(context, Preferences.class));
            this.nav_drawer_intents.add(new Intent(context, Preferences_Light.class));
            return;
        }

        this.nav_drawer_options.add(Home.menu_name);
        this.nav_drawer_intents.add(new Intent(context, Home.class));

        if ((is_active_sensor) && (last_two_calibrations != null) && (last_two_calibrations.size() > 0)) {
            this.nav_drawer_options.add(context.getString(R.string.calibration_graph));
            this.nav_drawer_intents.add(new Intent(context, CalibrationGraph.class));
        }

        if (prefs.getBoolean("show_data_tables", false)) {
            this.nav_drawer_options.add(context.getString(R.string.bg_data_table));
            this.nav_drawer_intents.add(new Intent(context, BgReadingTable.class));
            this.nav_drawer_options.add(context.getString(R.string.calibration_data_table));
            this.nav_drawer_intents.add(new Intent(context, CalibrationDataTable.class));
        }

        if ((prefs.getString("dex_collection_method", "").equals("Follower"))) {
            this.nav_drawer_options.add(context.getString(R.string.add_calibration));
            this.nav_drawer_intents.add(new Intent(context, AddCalibration.class));
        } else {

            if (is_active_sensor) {
                if (!CollectionServiceStarter.isBTShare(context)) {
                    if (last_two_bgReadings.size() > 1) {
                        if (last_two_calibrations.size() > 1) {
                            if (bGreadings_in_last_30_mins.size() >= 2) {
                                if (time_now - last_two_calibrations.get(0).timestamp < (1000 * 60 * 60)) { //Put steps in place to discourage over calibration
                                    this.nav_drawer_options.add(CalibrationOverride.menu_name);
                                    this.nav_drawer_intents.add(new Intent(context, CalibrationOverride.class));
                                } else {
                                    this.nav_drawer_options.add(context.getString(R.string.add_calibration));
                                    this.nav_drawer_intents.add(new Intent(context, AddCalibration.class));
                                }
                            } else {
                                this.nav_drawer_options.add(context.getString(R.string.cannot_calibrate_right_now));
                                this.nav_drawer_intents.add(new Intent(context, Home.class));
                            }
                        } else {
                            this.nav_drawer_options.add(DoubleCalibrationActivity.menu_name);
                            this.nav_drawer_intents.add(new Intent(context, DoubleCalibrationActivity.class));
                        }
                    }
                }
                this.nav_drawer_options.add(StopSensor.menu_name);
                this.nav_drawer_intents.add(new Intent(context, StopSensor.class));
            } else {
                this.nav_drawer_options.add(context.getString(R.string.start_sensor));
                this.nav_drawer_intents.add(new Intent(context, StartNewSensor.class));
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (DexCollectionType.hasBluetooth() && (DexCollectionType.getDexCollectionType() != DexCollectionType.DexcomG5)) {
                // if (CollectionServiceStarter.isBTWixel(context) ||
                //       CollectionServiceStarter.isBTShare(context) ||
                //     CollectionServiceStarter.isWifiandBTWixel(context) ||
                //   CollectionServiceStarter.isDexBridgeOrWifiandDexBridge()) {
                this.nav_drawer_options.add(BluetoothScan.menu_name);
                this.nav_drawer_intents.add(new Intent(context, BluetoothScan.class));
            }
        }

        //if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
        this.nav_drawer_options.add(context.getString(R.string.system_status));
        this.nav_drawer_intents.add(new Intent(context, MegaStatus.class));
        //}

        boolean bg_alerts = prefs.getBoolean("bg_alerts_from_main_menu", false);
        if (bg_alerts) {
            this.nav_drawer_options.add(AlertList.menu_name);
            this.nav_drawer_intents.add(new Intent(context, AlertList.class));
        }

        if (Experience.gotData()) {
            this.nav_drawer_options.add(context.getString(R.string.snooze_alert));
            this.nav_drawer_intents.add(new Intent(context, SnoozeActivity.class));
        }

        //if (use_note_search || (Treatments.last() != null)) {
        //    this.nav_drawer_options.add(NoteSearch.menu_name);
        //    this.nav_drawer_intents.add(new Intent(context, NoteSearch.class));
        //    use_note_search = true; // cache
        //}

        if (Experience.gotData()) {
            this.nav_drawer_options.add(context.getString(R.string.statistics));
            this.nav_drawer_intents.add(new Intent(context, StatsActivity.class));

            //this.nav_drawer_options.add(context.getString(R.string.history));
            //this.nav_drawer_intents.add(new Intent(context, BGHistory.class));
        }

        this.nav_drawer_options.add(context.getString(R.string.settings));
        //this.nav_drawer_intents.add(new Intent(context, Preferences.class));
        this.nav_drawer_intents.add(new Intent(context, Preferences_Light.class));

    }
}
