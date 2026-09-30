// Employee Dashboard Interactive Logic
let currentStaff = null;

document.addEventListener('DOMContentLoaded', async () => {
    currentStaff = auth.requireAuth(['EMPLOYEE', 'ADMIN']);
    if (!currentStaff) return;

    document.getElementById('userNameDisplay').textContent = currentStaff.name;
    document.getElementById('userRoleDisplay').textContent = `${currentStaff.role} (${currentStaff.userId})`;

    await loadAllData();
    setupListeners();
});

async function loadAllData() {
    await Promise.all([
        loadCustomers(),
        loadAccounts(),
        loadTransactions(),
        loadSecurityAlerts()
    ]);
}

async function loadCustomers(searchQuery = '') {
    const tableBody = document.getElementById('customersTableBody');
    tableBody.innerHTML = '<tr><td colspan="7" class="text-center py-3"><div class="spinner-border spinner-border-sm text-primary"></div> Loading customers...</td></tr>';

    try {
        const customers = await api.customers.getAll(searchQuery);
        if (customers.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="7" class="text-center py-3 text-muted">No customers found.</td></tr>';
            return;
        }

        tableBody.innerHTML = customers.map(c => `
            <tr>
                <td class="fw-bold">${c.customerId}</td>
                <td>${c.name}</td>
                <td>${c.email}</td>
                <td>${c.phone}</td>
                <td><span class="badge ${c.locked ? 'badge-locked' : 'badge-active'}">${c.locked ? 'LOCKED' : 'ACTIVE'}</span></td>
                <td>${c.accountIds ? c.accountIds.length : 0} accounts</td>
                <td>
                    ${c.locked 
                        ? `<button class="btn btn-sm btn-outline-success" onclick="unlockCustomer('${c.customerId}')">Unlock</button>`
                        : `<button class="btn btn-sm btn-outline-danger" onclick="lockCustomer('${c.customerId}')">Lock</button>`
                    }
                </td>
            </tr>
        `).join('');
    } catch (err) {
        tableBody.innerHTML = `<tr><td colspan="7" class="text-danger text-center py-3">Error: ${err.message}</td></tr>`;
    }
}

async function loadAccounts() {
    const tableBody = document.getElementById('accountsTableBody');
    tableBody.innerHTML = '<tr><td colspan="7" class="text-center py-3"><div class="spinner-border spinner-border-sm text-primary"></div> Loading accounts...</td></tr>';

    try {
        const accounts = await api.accounts.getAll();
        if (accounts.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="7" class="text-center py-3 text-muted">No accounts found.</td></tr>';
            return;
        }

        tableBody.innerHTML = accounts.map(a => `
            <tr>
                <td class="fw-bold">${a.accountId}</td>
                <td>${a.customerName} (${a.customerId})</td>
                <td><span class="badge bg-light text-dark border">${a.accountType}</span></td>
                <td class="fw-bold text-primary">$${a.balance.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</td>
                <td><span class="badge ${a.status === 'ACTIVE' ? 'badge-active' : (a.status === 'LOCKED' ? 'badge-locked' : 'badge-closed')}">${a.status}</span></td>
                <td class="small text-muted">${a.lockReason || '-'}</td>
                <td>
                    ${a.status === 'ACTIVE'
                        ? `<button class="btn btn-sm btn-outline-danger" onclick="lockAccount('${a.accountId}')">Lock</button>`
                        : (a.status === 'LOCKED' ? `<button class="btn btn-sm btn-outline-success" onclick="unlockAccount('${a.accountId}')">Unlock</button>` : '')
                    }
                </td>
            </tr>
        `).join('');
    } catch (err) {
        tableBody.innerHTML = `<tr><td colspan="7" class="text-danger text-center py-3">Error: ${err.message}</td></tr>`;
    }
}

async function loadTransactions() {
    const tableBody = document.getElementById('transactionsTableBody');
    tableBody.innerHTML = '<tr><td colspan="6" class="text-center py-3"><div class="spinner-border spinner-border-sm text-primary"></div> Loading transactions...</td></tr>';

    try {
        const txns = await api.transactions.getAll();
        if (txns.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="6" class="text-center py-3 text-muted">No transactions recorded yet.</td></tr>';
            return;
        }

        tableBody.innerHTML = txns.map(t => `
            <tr>
                <td class="fw-semibold small">${t.transactionId}</td>
                <td>${t.accountId}</td>
                <td><span class="badge bg-secondary">${t.transactionType}</span></td>
                <td class="fw-bold">$${t.amount.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</td>
                <td class="small text-muted">${new Date(t.timestamp).toLocaleString()}</td>
                <td class="small">${t.description}</td>
            </tr>
        `).join('');
    } catch (err) {
        tableBody.innerHTML = `<tr><td colspan="6" class="text-danger text-center py-3">Error: ${err.message}</td></tr>`;
    }
}

async function loadSecurityAlerts() {
    const tableBody = document.getElementById('alertsTableBody');
    tableBody.innerHTML = '<tr><td colspan="7" class="text-center py-3"><div class="spinner-border spinner-border-sm text-primary"></div> Loading security alerts...</td></tr>';

    try {
        const alerts = await api.security.getAlerts();
        if (alerts.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="7" class="text-center py-3 text-muted">No active security alerts. All systems secure.</td></tr>';
            return;
        }

        tableBody.innerHTML = alerts.map(a => {
            const sevBadge = a.severity === 'CRITICAL' ? 'bg-danger' : (a.severity === 'HIGH' ? 'bg-warning text-dark' : 'bg-info text-dark');
            const statusBadge = a.status === 'RESOLVED' ? 'bg-success' : 'bg-danger';

            return `
                <tr>
                    <td class="fw-bold">${a.alertId}</td>
                    <td><span class="badge ${sevBadge}">${a.severity}</span></td>
                    <td class="small fw-semibold">${a.ruleTriggered}</td>
                    <td class="small">${a.description}</td>
                    <td class="small text-muted">${new Date(a.timestamp).toLocaleString()}</td>
                    <td><span class="badge ${statusBadge}">${a.status}</span></td>
                    <td>
                        ${a.status !== 'RESOLVED' 
                            ? `<button class="btn btn-sm btn-outline-success" onclick="resolveAlertModal('${a.alertId}')">Resolve</button>`
                            : `<span class="small text-muted">${a.resolutionNotes || 'Resolved'}</span>`
                        }
                    </td>
                </tr>
            `;
        }).join('');
    } catch (err) {
        tableBody.innerHTML = `<tr><td colspan="7" class="text-danger text-center py-3">Error: ${err.message}</td></tr>`;
    }
}

async function lockAccount(accountId) {
    const reason = prompt(`Enter reason for locking account ${accountId}:`, 'Suspicious activity observed');
    if (!reason) return;
    try {
        await api.accounts.lock(accountId, reason);
        auth.showNotification(`Account ${accountId} locked successfully.`, 'warning');
        await loadAccounts();
    } catch (err) {
        auth.showNotification(err.message, 'danger');
    }
}

async function unlockAccount(accountId) {
    if (!confirm(`Are you sure you want to unlock account ${accountId}?`)) return;
    try {
        await api.accounts.unlock(accountId);
        auth.showNotification(`Account ${accountId} restored to active status.`, 'success');
        await loadAccounts();
    } catch (err) {
        auth.showNotification(err.message, 'danger');
    }
}

async function lockCustomer(customerId) {
    const reason = prompt(`Enter reason for locking customer ${customerId}:`, 'Administrative hold');
    if (!reason) return;
    try {
        await api.customers.lock(customerId, reason);
        auth.showNotification(`Customer ${customerId} profile locked.`, 'warning');
        await loadCustomers();
    } catch (err) {
        auth.showNotification(err.message, 'danger');
    }
}

async function unlockCustomer(customerId) {
    if (!confirm(`Are you sure you want to unlock customer ${customerId}?`)) return;
    try {
        await api.customers.unlock(customerId);
        auth.showNotification(`Customer ${customerId} profile restored.`, 'success');
        await loadCustomers();
    } catch (err) {
        auth.showNotification(err.message, 'danger');
    }
}

function resolveAlertModal(alertId) {
    const notes = prompt(`Resolution notes for alert ${alertId}:`, 'Investigated by staff, verified legitimate transaction');
    if (!notes) return;
    api.security.resolveAlert(alertId, notes)
        .then(() => {
            auth.showNotification(`Alert ${alertId} resolved.`, 'success');
            loadSecurityAlerts();
        })
        .catch(err => auth.showNotification(err.message, 'danger'));
}

function setupListeners() {
    document.getElementById('customerSearchForm')?.addEventListener('submit', (e) => {
        e.preventDefault();
        const q = document.getElementById('customerSearchInput').value.trim();
        loadCustomers(q);
    });
}
