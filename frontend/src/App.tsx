import {useState} from 'react';
import {Routes, Route, Navigate, useNavigate} from 'react-router-dom';
import Login from './components/Login';
import Register from './components/Register';
import type {User} from './types/types.ts';
import './App.css';
import Dashboard from "./components/Dashboard.tsx";


function App() {
    const [user, setUser] = useState<User | null>(() => {
        const savedUser = sessionStorage.getItem('user');
        return savedUser ? JSON.parse(savedUser) : null;
    });

    const navigate = useNavigate();

    const handleLogin = (loggedInUser: User) => {
        setUser(loggedInUser);
        sessionStorage.setItem('user', JSON.stringify(loggedInUser));
        navigate('/dashboard');
    };

    const handleLogout = () => {
        setUser(null);
        sessionStorage.removeItem('user');
        navigate('/login');
    };

    return (
        <Routes>
            <Route
                path="/login"
                element={
                    user ? (
                        <Navigate to="/dashboard" replace/>
                    ) : (
                        <div className="auth-container">
                            <Login onLogin={handleLogin}/>
                        </div>
                    )
                }
            />
            <Route
                path="/register"
                element={
                    user ? (
                        <Navigate to="/dashboard" replace/>
                    ) : (
                        <div className="auth-container">
                            <Register onRegister={handleLogin}/>
                        </div>
                    )
                }
            />
            <Route
                path="/dashboard"
                element={
                    user ? (
                        <Dashboard user={user} onLogout={handleLogout}/>
                    ) : (
                        <Navigate to="/login" replace/>
                    )
                }
            />
            <Route
                path="/"
                element={<Navigate to={user ? "/dashboard" : "/login"} replace/>}
            />
            <Route
                path="*"
                element={<Navigate to={user ? "/dashboard" : "/login"} replace/>}
            />
        </Routes>
    );
}

export default App;