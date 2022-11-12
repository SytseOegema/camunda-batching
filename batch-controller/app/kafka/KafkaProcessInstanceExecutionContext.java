package kafka;

import akka.actor.ActorSystem;
import play.libs.concurrent.CustomExecutionContext;

import javax.inject.Inject;

/**
 * Custom execution context wired to "database.dispatcher" thread pool
 */
public class KafkaProcessInstanceExecutionContext extends CustomExecutionContext {
    @Inject
    public KafkaProcessInstanceExecutionContext(ActorSystem actorSystem) {
        super(actorSystem, "kafka.processInstance.consumer");
    }
}
