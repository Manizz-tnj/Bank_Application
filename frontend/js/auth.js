// Authentication & Session Guard for Bank Security Application
const auth = {
    getUser() {
        const userJson = sessionStorage.getItem('bankUser');
        return userJson ? JSON.parse(userJson) : null;
    },

    setUser(user) {
        sessionStorage.setItem('bankUser', JSON.stringify(user));
    },

    logout() {
        sessionStorage.removeItem('bankUser');
        window.location.href = 'index.html';
    },

    requireAuth(allowedRoles = []) {
        const user = this.getUser();
        if (!user) {
            window.location.href = 'index.html';
            return null;
        }

        if (allowedRoles.length > 0 && !allowedRoles.includes(user.role)) {
            alert('Access Denied: You do not have permissions for this portal.');
            if (user.role === 'CUSTOMER') window.location.href = 'customer-dashboard.html';
            else if (user.role === 'EMPLOYEE') window.location.href = 'employee-dashboard.html';
            else if (user.role === 'ADMIN') window.location.href = 'admin-dashboard.html';
            return null;
        }

        return user;
    },

    showNotification(message, type = 'success') {
        let container = document.getElementById('alertContainer');
        if (!container) {
            container = document.createElement('div');
            container.id = 'alertContainer';
            document.body.appendChild(container);
        }

        const alertDiv = document.createElement('div');
        const alertClass = type === 'success' ? 'alert-success' : (type === 'danger' ? 'alert-danger' : 'alert-warning');
        alertDiv.className = `alert ${alertClass} alert-dismissible fade show shadow`;
        alertDiv.role = 'alert';
        alertDiv.innerHTML = `
            <strong>${type === 'success' ? 'Success!' : 'Notice:'}</strong> ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        `;

        container.appendChild(alertDiv);
        setTimeout(() => {
            alertDiv.classList.remove('show');
            setTimeout(() => alertDiv.remove(), 300);
        }, 4000);
    }
};
