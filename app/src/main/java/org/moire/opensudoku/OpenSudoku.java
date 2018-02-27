package org.moire.opensudoku;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import com.github.salomonbrys.kodein.Kodein;
import com.github.salomonbrys.kodein.KodeinAware;
import com.github.salomonbrys.kodein.TypeToken;

import org.moire.opensudoku.gui.FolderListActivity;
import org.walleth.App;
import org.walleth.activities.SdkMainActivity;
import org.walleth.data.networks.CurrentAddressProvider;
import org.walleth.data.networks.NetworkDefinitionProvider;
import org.walleth.data.tokens.CurrentTokenProvider;

import timber.log.Timber;

/**
 * Created by sirisdev on 2/11/18.
 */

public class OpenSudoku extends App implements Application.ActivityLifecycleCallbacks, KodeinAware {

    private static NetworkDefinitionProvider networkDefinitionProvider;
    private static CurrentAddressProvider currentAddressProvider;
    private static CurrentTokenProvider currentTokenProvider;

    public NetworkDefinitionProvider getAppNetworkDefinitionProvider() {
        if (networkDefinitionProvider == null) {
            networkDefinitionProvider = ((App)this).getNetworkDefinitionProvider();
        }
        return networkDefinitionProvider;
    }

    public CurrentAddressProvider getAppCurrentAddressProvider() {
        if (currentAddressProvider == null) {
            currentAddressProvider = ((App)this).getCurrentAddressProvider();
        }
        return currentAddressProvider;
    }

    public CurrentTokenProvider getAppCurrentTokenProvider() {
        if (currentTokenProvider == null) {
            currentTokenProvider = ((App)this).getCurrentTokenProvider();
        }
        return currentTokenProvider;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("OpenSudoku", "onCreate");
        Timber.d("no Activity in onCreate");
        registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
        Log.d("OpenSudoku", "onActivityCreated");
        Timber.d("Activity[%s] created", activity.getClass().getSimpleName());
    }

    @Override
    public void onActivityStarted(Activity activity) {
        Log.d("OpenSudoku", "onActivityStarted");
        Timber.d("Activity[%s] started", activity.getClass().getSimpleName());
        if (activity instanceof SdkMainActivity) {
            ((SdkMainActivity)activity).setGameInfo(new FolderListActivity(), R.drawable.sudoku_icon, "Play Sudoku");
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
        Log.d("OpenSudoku", "onActivityResumed");
        Timber.d("Activity[%s] started", activity.getClass().getSimpleName());
    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
