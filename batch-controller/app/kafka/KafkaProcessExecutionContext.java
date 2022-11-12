package kafka;

import akka.actor.ActorSystem;
import play.libs.concurrent.CustomExecutionContext;

import javax.inject.Inject;

/**
 * Custom execution context wired to "database.dispatcher" thread pool
 */
public class KafkaProcessExecutionContext extends CustomExecutionContext {
    @Inject
    public KafkaProcessExecutionContext(ActorSystem actorSystem) {
        super(actorSystem, "kafka.process.consumer");
    }
}
