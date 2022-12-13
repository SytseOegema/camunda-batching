docker run \
  -e ZEEBE_CLIENT_BROKER_GATEWAY-ADDRESS=zeebe:26500 \
  -e ZEEBE_CLIENT_SECURITY_PLAINTEXT=true \
  --network camunda-platform_camunda-platform \
  sytse/faas-connector
