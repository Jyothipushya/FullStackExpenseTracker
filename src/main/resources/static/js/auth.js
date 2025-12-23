// static/js/auth.js - SIMPLIFIED VERSION
document.addEventListener('DOMContentLoaded', function() {
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
});