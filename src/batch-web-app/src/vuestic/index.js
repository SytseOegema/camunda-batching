import "vuestic-ui/dist/vuestic-ui.css";
import "./vuestic-overrides.css";
import VaIcon from "./components/va-icon";
import VaButton from "./components/va-button";
import iconsConfig from "./icons-config/icons-config";
import { createVuestic } from "vuestic-ui";

const themeColors = {
  variables: {
    primary: '#B456C0',
    custom: '#A35600'
  }
}

function useVuestic(app) {
  app.use(
    createVuestic({
      config: {
        components: {
          VaIcon,
          VaButton,
        },
        colors: themeColors,
        icons: iconsConfig,
      },
    })
  );
}

export { useVuestic };
