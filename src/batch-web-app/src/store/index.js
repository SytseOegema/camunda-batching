import { createStore } from "vuex";
import state from "./state";
import actions from "./actions";
import getters from "./getters";
import mutations from "./mutations";

export const store = createStore({
  actions,
  getters,
  mutations,
  state,
});
