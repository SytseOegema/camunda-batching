package workers;

import akka.actor.ActorSystem;
import play.libs.concurrent.CustomExecutionContext;

import javax.inject.Inject;

/**
 * Custom execution context wired to "database.dispatcher" thread pool
 */
public class BatchClusterInvokerExecutionContext extends CustomExecutionContext {
    @Inject
    public BatchClusterInvokerExecutionContext(ActorSystem actorSystem) {
        super(actorSystem, "kafka.processInstance.consumer");
    }
}
