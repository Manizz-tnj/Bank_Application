// Centralized API Client for Bank Security & Account Management System
const API_BASE = 'http://localhost:8080/api';

const api = {
    getToken() {
        try {
            const userJson = sessionStorage.getItem('bankUser');
            if (userJson) {
                const user = JSON.parse(userJson);
                if (user && user.token) return user.token;
            }
        } catch (e) {}
        return localStorage.getItem('bank_token') || sessionStorage.getItem('bank_token');
    },

    setToken(token) {
        sessionStorage.setItem('bank_token', token);
    },

    clearToken() {
        localStorage.removeItem('bank_token');
        sessionStorage.removeItem('bank_token');
    },

    generateUUID() {
        if (typeof crypto !== 'undefined' && crypto.randomUUID) {
            return crypto.randomUUID();
        }
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
            const r = Math.random() * 16 | 0, v = c === 'x' ? r : (r & 0x3 | 0x8);
            return v.toString(16);
        });
    },

    async request(endpoint, options = {}) {
        const url = `${API_BASE}${endpoint}`;
        const headers = {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
            'X-Correlation-Id': api.generateUUID(),
            ...options.headers
        };

        const token = api.getToken();
        if (token && !headers['Authorization']) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        const config = {
            ...options,
            headers
        };

        try {
            const response = await fetch(url, config);
            const contentType = response.headers.get('content-type');
            let data = null;

            if (contentType && contentType.includes('application/json')) {
                data = await response.json();
            } else {
                data = await response.text();
            }

            if (!response.ok) {
                if (response.status === 401 && !endpoint.includes('/auth/login')) {
                    sessionStorage.removeItem('bankUser');
                    api.clearToken();
                    if (!window.location.pathname.endsWith('index.html')) {
                        window.location.href = 'index.html';
                    }
                }
                const errorMessage = (data && data.message) ? data.message : (data || `HTTP error ${response.status}`);
                throw new Error(errorMessage);
            }

            return data;
        } catch (error) {
            console.error(`API Error [${endpoint}]:`, error);
            throw error;
        }
    },

    // Authentication Endpoints
    auth: {
        login(username, password) {
            return api.request('/auth/login', {
                method: 'POST',
                body: JSON.stringify({ username, password })
            });
        },
        register(customerData) {
            return api.request('/auth/register', {
                method: 'POST',
                body: JSON.stringify(customerData)
            });
        }
    },

    // Customer Endpoints
    customers: {
        get(customerId) {
            return api.request(`/customers/${customerId}`);
        },
        getAll(name = '') {
            const query = name ? `?name=${encodeURIComponent(name)}` : '';
            return api.request(`/customers${query}`);
        },
        update(customerId, data) {
            return api.request(`/customers/${customerId}`, {
                method: 'PUT',
                body: JSON.stringify(data)
            });
        },
        changePin(customerId, oldPin, newPin) {
            return api.request(`/customers/${customerId}/pin`, {
                method: 'PUT',
                body: JSON.stringify({ oldPin, newPin })
            });
        },
        lock(customerId, reason) {
            return api.request(`/customers/${customerId}/lock`, {
                method: 'PUT',
                body: JSON.stringify({ reason })
            });
        },
        unlock(customerId) {
            return api.request(`/customers/${customerId}/unlock`, {
                method: 'PUT'
            });
        }
    },

    // Account Endpoints
    accounts: {
        create(accountData) {
            return api.request('/accounts', {
                method: 'POST',
                body: JSON.stringify(accountData)
            });
        },
        get(accountId) {
            return api.request(`/accounts/${accountId}`);
        },
        getByCustomer(customerId) {
            return api.request(`/accounts/customer/${customerId}`);
        },
        getAll() {
            return api.request('/accounts');
        },
        deposit(accountId, amount, description) {
            return api.request(`/accounts/${accountId}/deposit`, {
                method: 'POST',
                body: JSON.stringify({ amount, description })
            });
        },
        withdraw(accountId, amount, description) {
            return api.request(`/accounts/${accountId}/withdraw`, {
                method: 'POST',
                body: JSON.stringify({ amount, description })
            });
        },
        close(accountId) {
            return api.request(`/accounts/${accountId}/close`, {
                method: 'PUT'
            });
        },
        lock(accountId, reason) {
            return api.request(`/accounts/${accountId}/lock`, {
                method: 'PUT',
                body: JSON.stringify({ reason })
            });
        },
        unlock(accountId) {
            return api.request(`/accounts/${accountId}/unlock`, {
                method: 'PUT'
            });
        }
    },

    // Transaction Endpoints
    transactions: {
        transfer(fromAccountId, toAccountId, amount, description, idempotencyKey = null) {
            const key = idempotencyKey || api.generateUUID();
            return api.request('/transactions/transfer', {
                method: 'POST',
                headers: {
                    'Idempotency-Key': key
                },
                body: JSON.stringify({
                    fromAccountId,
                    toAccountId,
                    amount,
                    description,
                    idempotencyKey: key
                })
            });
        },
        get(transactionId) {
            return api.request(`/transactions/${transactionId}`);
        },
        getByAccount(accountId) {
            return api.request(`/transactions/account/${accountId}`);
        },
        getAll() {
            return api.request('/transactions');
        }
    },

    // Security Alert Endpoints
    security: {
        getAlerts(severity = null) {
            const query = severity ? `?severity=${severity}` : '';
            return api.request(`/security/alerts${query}`);
        },
        resolveAlert(alertId, notes) {
            return api.request(`/security/alerts/${alertId}/resolve`, {
                method: 'PUT',
                body: JSON.stringify({ notes })
            });
        }
    },

    // Admin & Analytics Endpoints
    admin: {
        getSetupStatus() {
            return api.request('/admin/setup-status');
        },
        initialSetup(adminData) {
            return api.request('/admin/initial-setup', {
                method: 'POST',
                body: JSON.stringify(adminData)
            });
        },
        getStats() {
            return api.request('/admin/stats');
        },
        getEmployees() {
            return api.request('/admin/employees');
        },
        createEmployee(employeeData) {
            return api.request('/admin/employees', {
                method: 'POST',
                body: JSON.stringify(employeeData)
            });
        }
    }
};
