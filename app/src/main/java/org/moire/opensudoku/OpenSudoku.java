package org.moire.opensudoku;

import android.arch.lifecycle.LifecycleOwner;
import android.util.Log;
import android.view.View;

import com.github.salomonbrys.kodein.KodeinAware;

import org.moire.opensudoku.gui.FolderListActivity;
import org.walleth.App;
import org.walleth.TyleSdk;

import timber.log.Timber;

/**
 * Created by sirisdev on 2/11/18.
 */

public class OpenSudoku extends App implements KodeinAware {

    public void setCurrentBalanceObserver(LifecycleOwner owner, View view) {
        tyleSdk.setCurrentBalanceObserver(owner, view);
    }

    public void installTransactionObservers(LifecycleOwner owner) {
        tyleSdk.installTransactionObservers(owner);
    }

    private static TyleSdk tyleSdk;

    @Override
    public void onCreate() {
        super.onCreate();
        tyleSdk = TyleSdk.newInstance(this,new FolderListActivity(), R.drawable.sudoku_icon, "Play Sudoku");
        Log.d("OpenSudoku", "onCreate");
        Timber.d("no Activity in onCreate");
        registerActivityLifecycleCallbacks(tyleSdk);
    }
}
