// Admin Dashboard Interactive Logic
let currentAdmin = null;

document.addEventListener('DOMContentLoaded', async () => {
    currentAdmin = auth.requireAuth(['ADMIN']);
    if (!currentAdmin) return;

    document.getElementById('userNameDisplay').textContent = currentAdmin.name;
    document.getElementById('userRoleDisplay').textContent = `System Administrator (${currentAdmin.userId})`;

    await loadDashboardStats();
    await loadStaffList();
    setupAdminListeners();
});

async function loadDashboardStats() {
    try {
        const stats = await api.admin.getStats();

        document.getElementById('statTotalCustomers').textContent = stats.totalCustomers;
        document.getElementById('statTotalAccounts').textContent = stats.totalAccounts;
        document.getElementById('statActiveAccounts').textContent = stats.activeAccounts;
        document.getElementById('statLockedAccounts').textContent = stats.lockedAccounts;
        document.getElementById('statTotalLiquidity').textContent = `$${stats.totalBankBalance.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
        document.getElementById('statTotalTxns').textContent = stats.totalTransactions;
        document.getElementById('statDepositVol').textContent = `$${stats.totalDepositVolume.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
        document.getElementById('statTransferVol').textContent = `$${stats.totalTransferVolume.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
        document.getElementById('statHighAlerts').textContent = stats.highAlerts;
        document.getElementById('statCriticalAlerts').textContent = stats.criticalAlerts;
    } catch (err) {
        auth.showNotification(`Error loading system metrics: ${err.message}`, 'danger');
    }
}

async function loadStaffList() {
    const tableBody = document.getElementById('staffTableBody');
    tableBody.innerHTML = '<tr><td colspan="7" class="text-center py-3"><div class="spinner-border spinner-border-sm text-primary"></div> Loading personnel...</td></tr>';

    try {
        const staff = await api.admin.getEmployees();
        tableBody.innerHTML = staff.map(s => `
            <tr>
                <td class="fw-bold">${s.employeeId}</td>
                <td>${s.name}</td>
                <td>${s.email}</td>
                <td>${s.department}</td>
                <td>${s.designation}</td>
                <td><span class="badge ${s.role === 'ADMIN' ? 'bg-danger' : 'bg-primary'}">${s.role}</span></td>
                <td><span class="badge ${s.locked ? 'badge-locked' : 'badge-active'}">${s.locked ? 'LOCKED' : 'ACTIVE'}</span></td>
            </tr>
        `).join('');
    } catch (err) {
        tableBody.innerHTML = `<tr><td colspan="7" class="text-danger text-center py-3">Error loading staff: ${err.message}</td></tr>`;
    }
}

function setupAdminListeners() {
    document.getElementById('createStaffForm')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const payload = {
            name: document.getElementById('staffName').value.trim(),
            email: document.getElementById('staffEmail').value.trim(),
            phone: document.getElementById('staffPhone').value.trim(),
            department: document.getElementById('staffDepartment').value.trim(),
            designation: document.getElementById('staffDesignation').value.trim(),
            role: document.getElementById('staffRole').value,
            password: document.getElementById('staffPassword').value.trim()
        };

        try {
            const created = await api.admin.createEmployee(payload);
            auth.showNotification(`Staff member created successfully! Assigned ID: ${created.employeeId}`, 'success');
            bootstrap.Modal.getInstance(document.getElementById('createStaffModal')).hide();
            document.getElementById('createStaffForm').reset();
            await loadStaffList();
        } catch (err) {
            auth.showNotification(err.message, 'danger');
        }
    });
}
