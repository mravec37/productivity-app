const baseUrl = 'http://localhost:8080/auth';
const apiBaseUrl = 'http://localhost:8080';

const signupForm = document.getElementById('signup-form');
const verifySection = document.getElementById('verify-section');
const verifyBtn = document.getElementById('verify-btn');
const resendBtn = document.getElementById('resend-btn');
const loginForm = document.getElementById('login-form');
const messageDiv = document.getElementById('message');

let tempEmail = ''; // Dočasný email pre overenie

signupForm.addEventListener('submit', async (e) => {
    e.preventDefault();

    const username = document.getElementById('signup-username').value;
    const email = document.getElementById('signup-email').value;
    const password = document.getElementById('signup-password').value;

    const response = await fetch(`${baseUrl}/signup`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password, username })
    });

    if (response.ok) {
        messageDiv.textContent = 'Registrácia prebehla úspešne. Skontrolujte si e-mail pre overovací kód.';
        verifySection.style.display = 'block';
        tempEmail = email;
    } else {
        const errorText = await response.text();
        messageDiv.textContent = `Registrácia zlyhala: ${errorText}`;
    }
});

verifyBtn.addEventListener('click', async () => {
    const verificationCode = document.getElementById('verify-code').value;

    const response = await fetch(`${baseUrl}/verify`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: tempEmail, verificationCode })
    });

    if (response.ok) {
        messageDiv.textContent = 'Účet bol úspešne overený. Prihlasujeme vás...';

        const password = document.getElementById('signup-password').value;

        const loginResponse = await fetch(`${baseUrl}/authenticate`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email: tempEmail, password })
        });

        if (loginResponse.ok) {
            const data = await loginResponse.json();
            localStorage.setItem('jwtToken', data.token);
            window.location.href = '/task_manager/index.html';
        } else {
            const loginError = await loginResponse.text();
            messageDiv.textContent = `Overenie prebehlo, ale prihlásenie zlyhalo: ${loginError}`;
        }

    } else {
        const errorText = await response.text();
        messageDiv.textContent = `Overenie zlyhalo: ${errorText}`;
    }
});

resendBtn.addEventListener('click', async () => {
    const response = await fetch(`${baseUrl}/resend?email=${encodeURIComponent(tempEmail)}`, {
        method: 'POST'
    });

    if (response.ok) {
        messageDiv.textContent = 'Overovací kód bol odoslaný znova. Skontrolujte si e-mail.';
    } else {
        const errorText = await response.text();
        messageDiv.textContent = `Znovu odoslanie kódu zlyhalo: ${errorText}`;
    }
});

loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();

    const email = document.getElementById('login-email').value;
    const password = document.getElementById('login-password').value;

    const response = await fetch(`${baseUrl}/authenticate`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password })
    });

    if (response.ok) {
        const data = await response.json();
        messageDiv.textContent = 'Prihlásenie bolo úspešné!';
        localStorage.setItem('jwtToken', data.token);
        window.location.href = '/task_manager/index.html';
    } else {
        const errorText = await response.text();
        messageDiv.textContent = `Prihlásenie zlyhalo: ${errorText}`;
    }
});
