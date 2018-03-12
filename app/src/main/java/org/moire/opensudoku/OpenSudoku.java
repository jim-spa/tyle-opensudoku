package org.moire.opensudoku;

import android.app.Activity;
import android.app.Application;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.github.salomonbrys.kodein.KodeinAware;

import org.kethereum.model.Address;
import org.kethereum.model.ChainDefinition;
import org.moire.opensudoku.gui.FolderListActivity;
import org.walleth.App;
import org.walleth.TyleSdk;
import org.walleth.activities.SdkMainActivity;
import org.walleth.data.balances.Balance;
import org.walleth.data.networks.CurrentAddressProvider;
import org.walleth.data.networks.NetworkDefinitionProvider;
import org.walleth.data.tokens.CurrentTokenProvider;
import org.walleth.data.transactions.TransactionEntity;
import org.walleth.ui.ValueView;

import java.util.List;

import timber.log.Timber;

import static java.math.BigDecimal.ZERO;
import static org.threeten.bp.zone.ZoneRulesProvider.refresh;

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
        tyleSdk = new TyleSdk();
        tyleSdk.init(this,new FolderListActivity(), R.drawable.sudoku_icon, "Play Sudoku");
        Log.d("OpenSudoku", "onCreate");
        Timber.d("no Activity in onCreate");
        registerActivityLifecycleCallbacks(tyleSdk);
    }
}
