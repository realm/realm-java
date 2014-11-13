package io.realm.performancetest;

import android.content.Context;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dk.ilios.gopher.eventbus.AndroidBus;
import dk.ilios.gopher.model.BeaconTracker;
import dk.ilios.gopher.model.SimulatedBeaconTracker;
import dk.ilios.gopher.sensors.beacons.BeaconSensor;
import dk.ilios.gopher.sensors.steps.Pedometer;
import dk.ilios.gopher.sensors.steps.SoftwarePedometer;
import dk.ilios.gopher.ui.BeaconListFragment;
import dk.ilios.gopher.ui.FinderFragment;
import dk.ilios.gopher.util.PersistentState;
import dk.ilios.gopher.widget.FootStepView;
import dk.ilios.gopher.widget.FootStepViewV2;

//This annotation must list all activities that wish to inject
@Module(injects = {
            BaseActivity.class,
            MainActivity.class,
            BaseFragment.class,
            FinderFragment.class,
            BeaconListFragment.class,
            FootStepView.class,
            FootStepViewV2.class,
            SimulatedBeaconTracker.class,
            BeaconSensor.class
        },
        complete = true,
        library = false)
public class ApplicationModule {

    private final Context context;

    public ApplicationModule(Context context) {
        this.context = context;
    }

    @Provides
    @ApplicationContext
    Context provideContext() {
        return context;
    }

    @Provides
    @Singleton
    Bus provideBus() {
        return new AndroidBus(ThreadEnforcer.MAIN);
    }

    @Provides
    @Singleton
    PersistentState providesPersistentState(Context context) {
        return new PersistentState(context);
    }

    @Provides
    @Singleton
    BeaconTracker providesBeaconTracker() {
        return new BeaconTracker();
    }


    @Provides
    Pedometer providesPedometer(Context context) {
        return new SoftwarePedometer(context);
    }

}
