/*document.addEventListener('DOMContentLoaded', function() {
    initSidebarToggle();
    initExpenseChart();
    initModal();
    updateCurrentDate();
});

function initSidebarToggle() {
    const menuToggle = document.getElementById('menuToggle');
    const sidebar = document.querySelector('.sidebar');
    if (menuToggle && sidebar) {
        menuToggle.addEventListener('click', function() {
            sidebar.classList.toggle('active');
        });
    }
}

function initExpenseChart() {
    const ctx = document.getElementById('expenseChart');
    if (!ctx) return;

    const expenseChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: ['Food & Dining', 'Shopping', 'Transportation', 'Entertainment', 'Bills & Utilities', 'Others'],
            datasets: [{
                data: [520, 320, 180, 150, 450, 220],
                backgroundColor: ['#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF', '#FF9F40'],
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { position: 'right' }
            }
        }
    });
}

function initModal() {
    const form = document.getElementById('addExpenseForm');
    if (form) {
        form.addEventListener('submit', function(e) {
            e.preventDefault();
            addNewExpense();
        });
    }

    const today = new Date().toISOString().split('T')[0];
    const dateInput = document.getElementById('expenseDate');
    if (dateInput) {
        dateInput.value = today;
        dateInput.max = today;
    }
}

function showAddExpenseModal() {
    const modal = document.getElementById('addExpenseModal');
    if (modal) {
        modal.style.display = 'flex';
        document.body.style.overflow = 'hidden';
    }
}

function closeModal() {
    const modal = document.getElementById('addExpenseModal');
    if (modal) {
        modal.style.display = 'none';
        document.body.style.overflow = 'auto';
        const form = document.getElementById('addExpenseForm');
        if (form) form.reset();
    }
}

function addNewExpense() {
    const description = document.getElementById('expenseDescription').value;
    const amount = document.getElementById('expenseAmount').value;
    const category = document.getElementById('expenseCategory').value;

    if (!description || !amount || !category) {
        alert('Please fill all fields');
        return;
    }

    alert('Expense added: ' + description + ' - $' + amount);
    closeModal();
}

function updateCurrentDate() {
    const dateElement = document.getElementById('currentDate');
    if (dateElement) {
        dateElement.textContent = new Date().toLocaleDateString('en-US', {
            weekday: 'long',
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        });
    }
}

window.addEventListener('click', function(event) {
    const modal = document.getElementById('addExpenseModal');
    if (event.target === modal) {
        closeModal();
    }
});*/
// static/js/dashboard.js - CORRECTED VERSION
document.addEventListener('DOMContentLoaded', function() {
    console.log("=== DASHBOARD.JS LOADED ===");

    // Only run on dashboard pages
    const currentPath = window.location.pathname;
    const isDashboardPage = currentPath.includes('/dashboard') ||
                           currentPath === '/' ||
                           currentPath === '/dashboard';

    if (!isDashboardPage) {
        console.log("Not on dashboard page, skipping dashboard.js");
        return; // EXIT - don't run on budget pages
    }

    console.log("On dashboard page, running dashboard.js");

    initSidebarToggle();
    initExpenseChart();
    initModal();
    updateCurrentDate();
});

function initSidebarToggle() {
    const menuToggle = document.getElementById('menuToggle');
    const sidebar = document.querySelector('.sidebar');
    if (menuToggle && sidebar) {
        menuToggle.addEventListener('click', function(e) {
            e.stopPropagation();
            sidebar.classList.toggle('active');
        });
    }
}

function initExpenseChart() {
    const ctx = document.getElementById('expenseChart');
    if (!ctx) return;

    const expenseChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: ['Food & Dining', 'Shopping', 'Transportation', 'Entertainment', 'Bills & Utilities', 'Others'],
            datasets: [{
                data: [520, 320, 180, 150, 450, 220],
                backgroundColor: ['#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF', '#FF9F40'],
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { position: 'right' }
            }
        }
    });
}

function initModal() {
    // ONLY set up modal if we're on dashboard
    const modal = document.getElementById('addExpenseModal');
    if (!modal) return;

    const form = document.getElementById('addExpenseForm');
    if (form) {
        form.addEventListener('submit', function(e) {
            e.preventDefault();
            addNewExpense();
        });
    }

    const today = new Date().toISOString().split('T')[0];
    const dateInput = document.getElementById('expenseDate');
    if (dateInput) {
        dateInput.value = today;
        dateInput.max = today;
    }
}

function showAddExpenseModal() {
    const modal = document.getElementById('addExpenseModal');
    if (modal) {
        modal.style.display = 'flex';
        document.body.style.overflow = 'hidden';
    }
}

function closeModal() {
    const modal = document.getElementById('addExpenseModal');
    if (modal) {
        modal.style.display = 'none';
        document.body.style.overflow = 'auto';
        const form = document.getElementById('addExpenseForm');
        if (form) form.reset();
    }
}

function addNewExpense() {
    const description = document.getElementById('expenseDescription').value;
    const amount = document.getElementById('expenseAmount').value;
    const category = document.getElementById('expenseCategory').value;

    if (!description || !amount || !category) {
        alert('Please fill all fields');
        return;
    }

    alert('Expense added: ' + description + ' - $' + amount);
    closeModal();
}

function updateCurrentDate() {
    const dateElement = document.getElementById('currentDate');
    if (dateElement) {
        dateElement.textContent = new Date().toLocaleDateString('en-US', {
            weekday: 'long',
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        });
    }
}

// Make sure this doesn't interfere with other clicks
window.addEventListener('click', function(event) {
    const modal = document.getElementById('addExpenseModal');
    if (event.target === modal) {
        closeModal();
    }
});

// DON'T add global click handlers that might interfere
// Remove this if it exists in your file:
// document.addEventListener('click', function(e) { ... });