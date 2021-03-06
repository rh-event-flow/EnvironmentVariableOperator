package io.streamzi.ev;

import io.streamzi.ev.operator.ConfigMapOperator;
import io.streamzi.ev.operator.DeploymentConfigOperator;
import io.streamzi.ev.operator.DeploymentOperator;
import io.streamzi.ev.watcher.ConfigMapWatcher;
import io.streamzi.ev.watcher.DeploymentConfigWatcher;
import io.streamzi.ev.watcher.DeploymentWatcher;
import org.apache.logging.log4j.LogManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Start the ConfigMap Watcher
 */
public class Manager {

    private final static org.apache.logging.log4j.Logger logger = LogManager.getLogger(Manager.class);

    private static final String CM_PREDICATE = "streamzi.io/kind=ev";

    public Manager() {
    }

    public static void main(String[] args) {

        logger.info("\uD83D\uDEE0 Starting Environment Variable Operator \uD83D\uDEE0");

        DeploymentConfigWatcher dcw = new DeploymentConfigWatcher(new DeploymentConfigOperator());
        DeploymentWatcher dw = new DeploymentWatcher(new DeploymentOperator());
        ConfigMapWatcher cmw = new ConfigMapWatcher(new ConfigMapOperator(), CM_PREDICATE);

        final ExecutorService executor = Executors.newFixedThreadPool(3);
        executor.submit(dcw);
        executor.submit(dw);
        executor.submit(cmw);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down");
            executor.shutdown();
            try {
                executor.awaitTermination(5000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ie) {
                logger.fatal("Error on close", ie);
            }
        }));


    }

}
