package greyfox.rxnetwork2.internal.strategy.impl;

import greyfox.rxnetwork2.internal.strategy.NetworkObservingStrategy;
import io.reactivex.functions.Action;
import io.reactivex.functions.Cancellable;

/**
 * @author Radek Kozak
 */
abstract class BuiltInNetworkObservingStrategy implements NetworkObservingStrategy {

    abstract void dispose();

    final class OnDisposeAction implements Action {

        @Override
        public void run() throws Exception {
            dispose();
        }
    }

    final class StrategyCancellable implements Cancellable {

        @Override
        public void cancel() throws Exception {
            dispose();
        }
    }
}