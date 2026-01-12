import React, { useState, useRef } from 'react';
import 'bootstrap/dist/css/bootstrap.min.css';
import ReCAPTCHA from "react-google-recaptcha";
import './App.css';

function LoginPage() {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const recaptchaRef = useRef();

    const handleLogin = async (guest = false) => {
        // Get token from React reCAPTCHA component
        const gRecaptchaResponse = recaptchaRef.current?.getValue();

        if (!gRecaptchaResponse && !guest) {
            setError("Please complete the reCAPTCHA.");
            return;
        }

        try {
            const params = new URLSearchParams();
            params.append('email', guest ? '' : email);
            params.append('password', guest ? '' : password);
            params.append('guest', guest.toString());
            params.append('g-recaptcha-response', gRecaptchaResponse);

            const response = await fetch('/api/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: params.toString()
            });

            const result = await response.json();

            if (result.status === "success") {
                sessionStorage.setItem("firstName", result.username);
                window.location.href = "/main";
            } else {
                setError(result.message);
            }
        } catch (err) {
            console.error('Login error:', err);
            setError("Server error. Try again later.");
        }

        // Reset reCAPTCHA
        recaptchaRef.current?.reset();
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        handleLogin(false);
    };

    return (
        <div id="login-header">
            <header id="movie-page-header">
                <h1 id="site-name">FABFLIX</h1>
            </header>

            <div className="login-card-box">
                <div className="card">
                    <h3 className="login-title">Login</h3>
                    <form onSubmit={handleSubmit}>
                        <input
                            id="email"
                            type="email"
                            className="form-control mb-2"
                            placeholder="Email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                        />
                        <input
                            id="password"
                            type="password"
                            className="form-control mb-3"
                            placeholder="Password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                        />
                        <ReCAPTCHA
                            ref={recaptchaRef}
                            sitekey="6LdL4fkrAAAAACy2g32scZf-ZxLxewh1fUatlSZd"
                            // sitekey="6LfeKi4sAAAAAHhdSvRCccKxxz4I4UKbejUCNBxP"
                            size="normal"
                        />

                        <p className="guest-login">
                            <a href="#" id="guest-login-link" onClick={(e) => {
                                e.preventDefault();
                                handleLogin(true);
                            }}>
                                Sign in as Guest
                            </a>
                        </p>
                        <button type="submit" className="custom-btn">Login</button>
                    </form>
                    <div id="error-msg">{error}</div>
                </div>
            </div>

            <p className="text-center mt-3">
                Enjoy discovering, watching, purchasing movies — please log in to continue.
            </p>
        </div>
    );
}

export default LoginPage;