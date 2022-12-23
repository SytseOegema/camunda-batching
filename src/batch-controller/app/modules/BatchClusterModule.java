package modules;

import com.google.inject.AbstractModule;
import workers.BatchClusterActivatorWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchClusterModule extends AbstractModule {

  @Override
  protected void configure(){
    bind(BatchClusterActivatorWorker.class).asEagerSingleton();
  }
}
