package workers;

import akka.actor.ActorSystem;
import play.libs.concurrent.CustomExecutionContext;

import javax.inject.Inject;

/**
 * Custom execution context wired to "kafka.processInstance.consumer" thread pool
 */
public class BatchClusterActivatorContext extends CustomExecutionContext {
    @Inject
    public BatchClusterActivatorContext(ActorSystem actorSystem) {
        super(actorSystem, "kafka.processInstance.consumer");
    }
}
