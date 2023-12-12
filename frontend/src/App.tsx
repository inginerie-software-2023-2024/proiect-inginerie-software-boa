import { createContext } from "react";
import { BrowserRouter } from "react-router-dom";
import { RouterComponent } from "./RouterComponent";
import {
  ContextInterface,
  useAuthContext,
} from "./components/utils/useAuthContext";

export const UserContext = createContext<ContextInterface>({
  user: null,
  login: () => {},
  logout: () => {},
});

function App() {
  const createUser = useAuthContext();

  return (
    <UserContext.Provider value={createUser}>
      <BrowserRouter>
        <RouterComponent />
      </BrowserRouter>
    </UserContext.Provider>
  );
}

export default App;
