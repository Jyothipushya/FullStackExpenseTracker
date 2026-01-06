// static/js/auth.js - SIMPLIFIED VERSION
/*document.addEventListener('DOMContentLoaded', function() {
    // Password toggle functionality
    document.querySelectorAll('.toggle-password').forEach(button => {
        button.addEventListener('click', function() {
            const input = this.parentElement.querySelector('input');
            const icon = this.querySelector('i');
            if (input.type === 'password') {
                input.type = 'text';
                icon.classList.remove('fa-eye');
                icon.classList.add('fa-eye-slash');
            } else {
                input.type = 'password';
                icon.classList.remove('fa-eye-slash');
                icon.classList.add('fa-eye');
            }
        });
    });

    // Remove any JavaScript form validation that blocks submission
    // Let Spring Security handle all validation
    const forms = document.querySelectorAll('form');
    forms.forEach(form => {
        // Remove any existing onsubmit handlers
        form.onsubmit = null;

        // Add a simple submit handler that allows submission
        form.addEventListener('submit', function(e) {
            console.log('Form submitting...');

            // Only do minimal client-side validation
            if (form.id === 'registerForm') {
                const password = document.getElementById('password')?.value;
                const confirmPassword = document.getElementById('confirmPassword')?.value;

                // Check password match only
                if (password && confirmPassword && password !== confirmPassword) {
                    e.preventDefault();
                    alert('Passwords do not match!');
                    return false;
                }
            }

            return true; // Allow Spring Security to handle everything else
        });
    });

    // Show flash messages from URL parameters
    const urlParams = new URLSearchParams(window.location.search);
    const error = urlParams.get('error');
    const success = urlParams.get('success');
    const logout = urlParams.get('logout');

    if (error) {
        console.log('Error parameter found:', error);
    }
    if (success) {
        console.log('Success parameter found:', success);
    }
    if (logout) {
        console.log('Logout parameter found:', logout);
    }
});*/
// static/js/auth.js - CORRECTED VERSION
document.addEventListener('DOMContentLoaded', function() {
    console.log("=== AUTH.JS LOADED ===");

    // Only run on login/register pages
    const currentPath = window.location.pathname;
    const isAuthPage = currentPath.includes('/login') ||
                       currentPath.includes('/register') ||
                       currentPath === '/' ||
                       currentPath === '/login' ||
                       currentPath === '/register';

    if (!isAuthPage) {
        console.log("Not on auth page, skipping auth.js");
        return; // EXIT - don't run on budget pages
    }

    console.log("On auth page, running auth.js");

    // Password toggle functionality - ONLY for password fields
    document.querySelectorAll('.toggle-password').forEach(button => {
        button.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();

            const input = this.parentElement.querySelector('input[type="password"], input[type="text"]');
            if (!input) return;

            const icon = this.querySelector('i');
            if (input.type === 'password') {
                input.type = 'text';
                if (icon) {
                    icon.classList.remove('fa-eye');
                    icon.classList.add('fa-eye-slash');
                }
            } else {
                input.type = 'password';
                if (icon) {
                    icon.classList.remove('fa-eye-slash');
                    icon.classList.add('fa-eye');
                }
            }
        });
    });

    // ONLY apply to login and register forms
    const authForms = document.querySelectorAll('#loginForm, #registerForm');
    authForms.forEach(form => {
        console.log("Setting up auth form:", form.id);

        // Remove any existing onsubmit handlers
        form.onsubmit = null;

        // Add submit handler ONLY for auth forms
        form.addEventListener('submit', function(e) {
            console.log('Auth form submitting:', form.id);

            // Only check password match for register form
            if (form.id === 'registerForm') {
                const password = document.getElementById('password')?.value;
                const confirmPassword = document.getElementById('confirmPassword')?.value;

                // Check password match only
                if (password && confirmPassword && password !== confirmPassword) {
                    e.preventDefault();
                    alert('Passwords do not match!');
                    return false;
                }
            }

            // Allow Spring Security to handle everything else
            console.log('Allowing form submission');
            return true;
        });
    });

    console.log("=== AUTH.JS SETUP COMPLETE ===");
});