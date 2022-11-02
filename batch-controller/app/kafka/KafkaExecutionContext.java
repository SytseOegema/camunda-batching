package kafka;

import akka.actor.ActorSystem;
import play.libs.concurrent.CustomExecutionContext;

import javax.inject.Inject;

/**
 * Custom execution context wired to "database.dispatcher" thread pool
 */
public class KafkaExecutionContext extends CustomExecutionContext {
    @Inject
    public KafkaExecutionContext(ActorSystem actorSystem) {
        super(actorSystem, "kafka.consumer");
    }
}
