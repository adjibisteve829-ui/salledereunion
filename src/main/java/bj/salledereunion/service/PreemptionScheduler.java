package bj.salledereunion.service;

import bj.salledereunion.util.PersistenceUtil;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
@WebListener
public class PreemptionScheduler implements ServletContextListener {
    private static final Logger LOGGER = Logger.getLogger(PreemptionScheduler.class.getName());
    private ScheduledExecutorService executor;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "preemption-echeances");
            thread.setDaemon(true);
            return thread;
        });
        executor.scheduleWithFixedDelay(() -> {
            try {
                new PreemptionService().traiterEcheances();
            } catch (RuntimeException exception) {
                LOGGER.log(Level.SEVERE, "Échec du traitement des échéances de préemption.", exception);
            }
        }, 0, 1, TimeUnit.MINUTES);
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        if (executor != null) {
            executor.shutdownNow();
        }
        PersistenceUtil.fermer();
    }
}
