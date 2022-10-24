package modules;


import com.google.inject.AbstractModule;
import models.DatabaseInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBModule extends AbstractModule {

  @Override
  protected void configure(){
    final Logger accessLogger = LoggerFactory.getLogger("access");
    accessLogger.warn("DatabaseInitializer");
    bind(DatabaseInitializer.class).asEagerSingleton();
  }
}
