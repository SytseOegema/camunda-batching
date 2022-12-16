echo "---------------------------------------------------------------------"
echo "CAMUNDA BATCH EXTENSION BUILDER SCRIPT"
echo "---------------------------------------------------------------------"
printf "\n\n"

echo "What do you want to build?"
echo " [1]: Batch controller docker image"
echo " [2]: Zeebe docker image"
echo " [3]: messaging package"
echo " [4]: Zeebe FaaS connector"
echo " [5]: Local Zeebe client package"
echo " [6]: Zeebe + local client package + messaging package + FaaS connector"

echo "NOTE: 1 & 2 depend on 3. Also different java versions are required for building the complete project."
printf "\n"
echo "If this is the first time. First use opti on [6] and then use option [1]"

read choice

if [[ "$choice" ==  "1" ]]; then
  cd build
  ./batchController.sh
  cd ..
fi

if [[ "$choice" ==  "2" ]]; then
  cd build
  ./zeebe.sh
  cd ..
fi

if [[ "$choice" ==  "3" ]]; then
  cd build
  ./messagingPackage.sh
  cd ..
fi

if [[ "$choice" ==  "4" ]]; then
  cd build
  ./faasConnector.sh
  cd ..
fi

if [[ "$choice" ==  "5" ]]; then
  cd build
  ./zeebeClient.sh
  cd ..
fi

if [[ "$choice" ==  "6" ]]; then
  echo "----------------------- Building Zeebe Client Package -----------------------"
  cd build
  ./zeebeClient.sh
  printf "\n\n"
  echo "----------------------- Building Messaging Packages -----------------------"
  printf "\n\n"
  ./messagingPackage.sh
  printf "\n\n"
  echo "----------------------- Building Zeebe Faas Connector -----------------------"
  printf "\n\n"
  ./faasConnector.sh
  printf "\n\n"
  echo "----------------------- Building Zeebe -----------------------"
  printf "\n\n"
  ./zeebe.sh
  cd ..
fi
