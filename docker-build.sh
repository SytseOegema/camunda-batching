docker build \
  --tag sytse/zeebe:latest \
  --build-arg DISTBALL='dist/target/camunda-zeebe*.tar.gz' \
  --target app \
  .
