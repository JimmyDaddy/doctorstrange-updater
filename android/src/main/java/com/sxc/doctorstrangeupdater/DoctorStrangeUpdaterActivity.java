package com.sxc.doctorstrangeupdater;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.sxc.doctorstrangeupdater.DoctorStrangeUpdater.DoctorStrangeUpdaterUpdateType;
import com.sxc.doctorstrangeupdater.DoctorStrangeUpdater.DoctorStrangeUpdaterFrequency;
import com.facebook.react.ReactActivity;

import javax.annotation.Nullable;


public abstract class DoctorStrangeUpdaterActivity extends ReactActivity
        implements DoctorStrangeUpdater.Interface {

    private DoctorStrangeUpdater updater;

    @Nullable
    @Override
    protected String getJSBundleFile() {
        updater = DoctorStrangeUpdater.getInstance(this.getApplicationContext());
        updater.setMetadataAssetName(this.getMetadataAssetName());
        return updater.getLatestJSCodeLocation();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        updater = DoctorStrangeUpdater.getInstance(this.getApplicationContext());
        updater.setUpdateMetadataUrl(this.getUpdateMetadataUrl())
                .setMetadataAssetName(this.getMetadataAssetName())
                .setUpdateFrequency(this.getUpdateFrequency())
                .setUpdateTypesToDownload(this.getAllowedUpdateType())
                .setHostnameForRelativeDownloadURLs(this.getHostnameForRelativeDownloadURLs())
                .showProgress(this.getShowProgress())
                .setParentActivity(this)
                .checkForUpdates();
    }

    protected abstract String getUpdateMetadataUrl();

    protected abstract String getMetadataAssetName();

    protected String getHostnameForRelativeDownloadURLs() {
        return null;
    }

    protected DoctorStrangeUpdaterUpdateType getAllowedUpdateType() {
        return DoctorStrangeUpdaterUpdateType.PATCH;
    }

    protected DoctorStrangeUpdaterFrequency getUpdateFrequency() {
        return DoctorStrangeUpdaterFrequency.EACH_TIME;
    }

    protected boolean getShowProgress() {
        return true;
    }

    public void updateFinished() {
        try {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle(R.string.auto_updater_downloaded_title);
            alertDialogBuilder
                    .setMessage(R.string.auto_updater_downloaded_message)
                    .setCancelable(false)
                    .setPositiveButton(
                            R.string.auto_updater_downloaded_now,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    DoctorStrangeUpdaterActivity.this.recreate();
                                }
                            }
                    )
                    .setNegativeButton(
                            R.string.auto_updater_downloaded_later,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            }
                    );

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
