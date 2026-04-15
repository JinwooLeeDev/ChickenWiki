import React, { createContext, useContext, useEffect, useState } from "react";
import {
  clearStoredAuth,
  getStoredAuth,
  loginRequest,
  meRequest,
  saveStoredAuth,
  signupRequest,
} from "../services/auth";

const AuthContext = createContext(null);

function toAuthState(response) {
  return {
    accessToken: response.accessToken,
    user: {
      id: response.userId,
      username: response.username,
      role: response.role,
    },
  };
}

export function AuthProvider({ children }) {
  const [auth, setAuth] = useState(() => getStoredAuth());
  const [isReady, setIsReady] = useState(false);

  useEffect(() => {
    const saved = getStoredAuth();
    if (!saved?.accessToken) {
      setIsReady(true);
      return;
    }

    let mounted = true;
    meRequest(saved.accessToken)
      .then((user) => {
        if (!mounted) return;
        setAuth({
          accessToken: saved.accessToken,
          user: {
            id: user.userId,
            username: user.username,
            role: user.role,
          },
        });
      })
      .catch(() => {
        if (!mounted) return;
        clearStoredAuth();
        setAuth(null);
      })
      .finally(() => {
        if (mounted) {
          setIsReady(true);
        }
      });

    return () => {
      mounted = false;
    };
  }, []);

  const login = async ({ username, password, remember }) => {
    const response = await loginRequest({ username, password });
    const nextAuth = toAuthState(response);
    saveStoredAuth(nextAuth, remember);
    setAuth(nextAuth);
    return nextAuth;
  };

  const signup = async ({ username, password, remember }) => {
    const response = await signupRequest({ username, password });
    const nextAuth = toAuthState(response);
    saveStoredAuth(nextAuth, remember);
    setAuth(nextAuth);
    return nextAuth;
  };

  const logout = () => {
    clearStoredAuth();
    setAuth(null);
  };

  return (
    <AuthContext.Provider
      value={{
        accessToken: auth?.accessToken || null,
        user: auth?.user || null,
        isLoggedIn: Boolean(auth?.accessToken),
        isReady,
        login,
        signup,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used inside AuthProvider");
  }
  return context;
}
