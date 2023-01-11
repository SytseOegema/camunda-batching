package workers;

import akka.actor.ActorSystem;
import play.libs.concurrent.CustomExecutionContext;

import javax.inject.Inject;

/**
 * Custom execution context wired to "worker.batchCluster.invoker" thread pool
 */
public class BatchClusterInvokerContext extends CustomExecutionContext {
    @Inject
    public BatchClusterInvokerContext(ActorSystem actorSystem) {
        super(actorSystem, "worker.batchCluster.invoker");
    }
}
