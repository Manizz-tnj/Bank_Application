// Customer Dashboard Interactive Logic for Apex National Bank
let currentUser = null;
let customerProfile = null;
let customerAccounts = [];
let selectedAccountId = null;
let currentAccountTransactions = [];

document.addEventListener('DOMContentLoaded', async () => {
    currentUser = auth.requireAuth(['CUSTOMER']);
    if (!currentUser) return;

    // Display basic header info
    document.getElementById('userNameDisplay').textContent = currentUser.name;
    document.getElementById('userRoleDisplay').textContent = `CIF: ${currentUser.userId}`;

    // Load initial data
    await loadCustomerData();

    // Event Listeners for Forms
    setupFormListeners();
});

async function loadCustomerData() {
    try {
        customerProfile = await api.customers.get(currentUser.userId);
        customerAccounts = await api.accounts.getByCustomer(currentUser.userId);

        renderProfileDetails(customerProfile);
        renderAccountCards(customerAccounts);
        updateSummaryStats(customerAccounts);
        populateAccountDropdowns(customerAccounts);

        // Load transactions for the first active account if available
        if (customerAccounts.length > 0) {
            await loadAccountTransactions(customerAccounts[0].accountId);
        } else {
            document.getElementById('transactionsTableBody').innerHTML = 
                '<tr><td colspan="6" class="text-center py-4 text-muted">No operational accounts found. Open an account to start banking.</td></tr>';
        }
    } catch (err) {
        auth.showNotification(err.message, 'danger');
    }
}

function renderProfileDetails(profile) {
    if (!profile) return;

    document.getElementById('profileCustomerId').textContent = profile.customerId;
    document.getElementById('profileName').textContent = profile.name;
    document.getElementById('profileDob').textContent = profile.dob ? profile.dob : 'Not specified';
    document.getElementById('profileGovId').textContent = profile.governmentId ? profile.governmentId : 'Verified Document';
    document.getElementById('profileBranchName').textContent = profile.branchName || 'Apex Metropolitan Central Branch';
    document.getElementById('profileRouting').textContent = profile.routingNumber || 'APEX000101';
    document.getElementById('profileCreatedAt').textContent = new Date(profile.createdAt).toLocaleDateString('en-US', {
        year: 'numeric', month: 'long', day: 'numeric'
    });

    const lockStatusBadge = document.getElementById('profileLockStatus');
    if (profile.locked) {
        lockStatusBadge.className = 'badge bg-danger text-white';
        lockStatusBadge.textContent = `Security Hold: ${profile.lockReason || 'Administrative Lock'}`;
    } else {
        lockStatusBadge.className = 'badge bg-success text-white';
        lockStatusBadge.textContent = 'Account Active & In Good Standing';
    }

    // Pre-fill editable contact info form
    document.getElementById('editEmail').value = profile.email || '';
    document.getElementById('editPhone').value = profile.phone || '';
    document.getElementById('editAddress').value = profile.address || '';
}

function updateSummaryStats(accounts) {
    const totalBalance = accounts
        .filter(a => a.status === 'ACTIVE')
        .reduce((sum, a) => sum + a.balance, 0);

    document.getElementById('totalBalanceDisplay').textContent = `$${totalBalance.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
    document.getElementById('totalAccountsDisplay').textContent = accounts.length;
}

function renderAccountCards(accounts) {
    const container = document.getElementById('accountsContainer');
    container.innerHTML = '';

    if (accounts.length === 0) {
        container.innerHTML = `
            <div class="col-12 text-center py-5">
                <div class="bank-card p-5 mx-auto" style="max-width: 500px;">
                    <i class="bi bi-wallet2 text-primary fs-1 mb-3 d-block"></i>
                    <h5 class="fw-bold">No Active Portfolios Found</h5>
                    <p class="text-muted small mb-4">You have not opened any deposit or checking accounts yet. Open an account to start your financial journey with Apex National Bank.</p>
                    <button class="btn btn-primary-bank" data-bs-toggle="modal" data-bs-target="#openAccountModal">
                        <i class="bi bi-plus-circle me-1"></i> Open Primary Account
                    </button>
                </div>
            </div>
        `;
        return;
    }

    accounts.forEach(acc => {
        const typeClass = acc.accountType.toLowerCase().replace('_', '-');
        const badgeClass = acc.status === 'ACTIVE' ? 'badge-active' : (acc.status === 'LOCKED' ? 'badge-locked' : 'badge-closed');

        const card = document.createElement('div');
        card.className = 'col-md-6 col-lg-4 mb-4';
        card.innerHTML = `
            <div class="bank-card account-card ${typeClass} p-4 h-100 d-flex flex-column justify-content-between">
                <div>
                    <div class="d-flex justify-content-between align-items-center mb-2">
                        <span class="badge ${badgeClass} px-2 py-1">${acc.status}</span>
                        <button class="btn btn-sm btn-link p-0 text-decoration-none text-muted" onclick="copyToClipboard('${acc.accountId}', this)" title="Copy Account Number">
                            <span class="font-monospace small fw-bold">${acc.accountId}</span> <i class="bi bi-copy ms-1"></i>
                        </button>
                    </div>
                    <h5 class="fw-bold mb-1">${formatAccountType(acc.accountType)}</h5>
                    <div class="account-balance my-3">$${acc.balance.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</div>
                    <div class="small text-muted mb-3">
                        ${acc.accountType === 'SAVINGS' ? `<span class="d-block"><i class="bi bi-graph-up-arrow text-success me-1"></i> 4.0% APY Interest</span><span class="d-block text-secondary">Min Required Balance: $${acc.minimumBalance.toFixed(2)}</span>` : ''}
                        ${acc.accountType === 'CURRENT' ? `<span class="d-block"><i class="bi bi-shield-check text-primary me-1"></i> Overdraft Facility: $${acc.overdraftLimit.toFixed(2)}</span>` : ''}
                        ${acc.accountType === 'FIXED_DEPOSIT' ? `<span class="d-block"><i class="bi bi-calendar-check text-warning me-1"></i> Term: ${acc.termMonths} Months (7.0% APY)</span>` : ''}
                    </div>
                </div>
                <div class="btn-group w-100 mt-2">
                    <button class="btn btn-outline-primary btn-sm" onclick="openDepositModal('${acc.accountId}')" ${acc.status !== 'ACTIVE' || acc.accountType === 'FIXED_DEPOSIT' ? 'disabled' : ''}>
                        <i class="bi bi-plus me-1"></i>Deposit
                    </button>
                    <button class="btn btn-outline-danger btn-sm" onclick="openWithdrawModal('${acc.accountId}')" ${acc.status !== 'ACTIVE' ? 'disabled' : ''}>
                        <i class="bi bi-dash me-1"></i>Withdraw
                    </button>
                    <button class="btn btn-outline-secondary btn-sm" onclick="switchToStatementsTab('${acc.accountId}')">
                        <i class="bi bi-receipt me-1"></i>Ledger
                    </button>
                </div>
            </div>
        `;
        container.appendChild(card);
    });
}

function formatAccountType(type) {
    switch (type) {
        case 'SAVINGS': return 'Premier Savings Account';
        case 'CURRENT': return 'Commercial Current Account';
        case 'FIXED_DEPOSIT': return 'Fixed Term Deposit';
        default: return type;
    }
}

function populateAccountDropdowns(accounts) {
    const activeAccounts = accounts.filter(a => a.status === 'ACTIVE');
    const transferFromSelect = document.getElementById('transferFromAccount');
    if (transferFromSelect) {
        transferFromSelect.innerHTML = activeAccounts
            .map(a => `<option value="${a.accountId}">${a.accountId} (${formatAccountType(a.accountType)} - $${a.balance.toFixed(2)})</option>`)
            .join('');
    }
}

async function loadAccountTransactions(accountId) {
    selectedAccountId = accountId;
    document.getElementById('currentViewingAccountTitle').textContent = `Account Statement & Ledger - ${accountId}`;
    const tableBody = document.getElementById('transactionsTableBody');
    tableBody.innerHTML = '<tr><td colspan="6" class="text-center py-3"><div class="spinner-border spinner-border-sm text-primary"></div> Loading ledger transactions...</td></tr>';

    try {
        const txns = await api.transactions.getByAccount(accountId);
        currentAccountTransactions = txns;

        if (txns.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="6" class="text-center py-4 text-muted">No transactions recorded for this account.</td></tr>';
            return;
        }

        tableBody.innerHTML = txns.map(t => {
            const isCredit = t.transactionType === 'DEPOSIT' || (t.transactionType === 'TRANSFER' && t.description.includes('Received from'));
            const colorClass = isCredit ? 'text-success' : 'text-danger';
            const sign = isCredit ? '+' : '-';

            return `
                <tr>
                    <td class="fw-semibold small font-monospace">${t.transactionId}</td>
                    <td><span class="badge ${isCredit ? 'bg-success-subtle text-success' : 'bg-danger-subtle text-danger'}">${t.transactionType}</span></td>
                    <td class="fw-bold ${colorClass}">${sign}$${t.amount.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</td>
                    <td class="small text-muted">${new Date(t.timestamp).toLocaleString()}</td>
                    <td class="small">${t.description}</td>
                    <td><span class="badge bg-light text-dark border">${t.status}</span></td>
                </tr>
            `;
        }).join('');
    } catch (err) {
        tableBody.innerHTML = `<tr><td colspan="6" class="text-danger text-center py-3">Error loading transactions: ${err.message}</td></tr>`;
    }
}

function switchToStatementsTab(accountId) {
    const tabEl = document.getElementById('tab-statements');
    const tab = new bootstrap.Tab(tabEl);
    tab.show();
    loadAccountTransactions(accountId);
}

function copyToClipboard(text, btnElement) {
    navigator.clipboard.writeText(text).then(() => {
        const original = btnElement.innerHTML;
        btnElement.innerHTML = '<span class="text-success"><i class="bi bi-check2"></i> Copied</span>';
        setTimeout(() => { btnElement.innerHTML = original; }, 2000);
    }).catch(err => {
        auth.showNotification('Could not copy to clipboard', 'warning');
    });
}

function downloadStatementCSV() {
    if (!currentAccountTransactions || currentAccountTransactions.length === 0) {
        auth.showNotification('No transactions available to download for this account.', 'warning');
        return;
    }

    const headers = ["Transaction ID", "Account ID", "Type", "Amount", "Timestamp", "Description", "Status"];
    const rows = currentAccountTransactions.map(t => [
        t.transactionId,
        t.accountId,
        t.transactionType,
        t.amount.toFixed(2),
        new Date(t.timestamp).toISOString(),
        `"${t.description.replace(/"/g, '""')}"`,
        t.status
    ]);

    const csvContent = "data:text/csv;charset=utf-8," + [headers.join(","), ...rows.map(e => e.join(","))].join("\n");
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement("a");
    link.setAttribute("href", encodedUri);
    link.setAttribute("download", `Apex_Statement_${selectedAccountId || 'Account'}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}

function openDepositModal(accountId) {
    document.getElementById('depositAccountId').value = accountId;
    document.getElementById('depositAmount').value = '';
    document.getElementById('depositNote').value = '';
    const modal = new bootstrap.Modal(document.getElementById('depositModal'));
    modal.show();
}

function openWithdrawModal(accountId) {
    document.getElementById('withdrawAccountId').value = accountId;
    document.getElementById('withdrawAmount').value = '';
    document.getElementById('withdrawNote').value = '';
    const modal = new bootstrap.Modal(document.getElementById('withdrawModal'));
    modal.show();
}

function setupFormListeners() {
    // Open Account Form
    document.getElementById('openAccountForm')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const accountType = document.getElementById('newAccountType').value;
        const initialDeposit = parseFloat(document.getElementById('newAccountInitialDeposit').value);
        const overdraft = parseFloat(document.getElementById('newAccountOverdraft')?.value || '0');
        const term = parseInt(document.getElementById('newAccountTerm')?.value || '12');

        try {
            await api.accounts.create({
                customerId: currentUser.userId,
                accountType,
                initialDeposit,
                overdraftLimit: overdraft,
                termMonths: term
            });
            auth.showNotification('New account successfully opened and activated!', 'success');
            bootstrap.Modal.getInstance(document.getElementById('openAccountModal')).hide();
            await loadCustomerData();
        } catch (err) {
            auth.showNotification(err.message, 'danger');
        }
    });

    // Deposit Form
    document.getElementById('depositForm')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const accountId = document.getElementById('depositAccountId').value;
        const amount = parseFloat(document.getElementById('depositAmount').value);
        const description = document.getElementById('depositNote').value;

        try {
            await api.accounts.deposit(accountId, amount, description);
            auth.showNotification(`Successfully deposited $${amount.toFixed(2)} into ${accountId}`, 'success');
            bootstrap.Modal.getInstance(document.getElementById('depositModal')).hide();
            await loadCustomerData();
        } catch (err) {
            auth.showNotification(err.message, 'danger');
        }
    });

    // Withdraw Form
    document.getElementById('withdrawForm')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const accountId = document.getElementById('withdrawAccountId').value;
        const amount = parseFloat(document.getElementById('withdrawAmount').value);
        const description = document.getElementById('withdrawNote').value;

        try {
            await api.accounts.withdraw(accountId, amount, description);
            auth.showNotification(`Successfully withdrew $${amount.toFixed(2)} from ${accountId}`, 'success');
            bootstrap.Modal.getInstance(document.getElementById('withdrawModal')).hide();
            await loadCustomerData();
        } catch (err) {
            auth.showNotification(err.message, 'danger');
        }
    });

    // Transfer Form
    document.getElementById('transferForm')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const fromAccountId = document.getElementById('transferFromAccount').value;
        const toAccountId = document.getElementById('transferToAccount').value.trim();
        const amount = parseFloat(document.getElementById('transferAmount').value);
        const description = document.getElementById('transferNote').value;

        try {
            await api.transactions.transfer(fromAccountId, toAccountId, amount, description);
            auth.showNotification(`Atomic Transfer Complete: $${amount.toFixed(2)} transferred to ${toAccountId}`, 'success');
            bootstrap.Modal.getInstance(document.getElementById('transferModal')).hide();
            document.getElementById('transferToAccount').value = '';
            document.getElementById('transferAmount').value = '';
            document.getElementById('transferNote').value = '';
            await loadCustomerData();
        } catch (err) {
            auth.showNotification(err.message, 'danger');
        }
    });

    // Update Contact Details Form
    document.getElementById('updateProfileForm')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const email = document.getElementById('editEmail').value.trim();
        const phone = document.getElementById('editPhone').value.trim();
        const address = document.getElementById('editAddress').value.trim();

        try {
            const updated = await api.customers.update(currentUser.userId, {
                name: customerProfile.name,
                email,
                phone,
                address,
                pin: "0000" // Not changing PIN here
            });
            auth.showNotification('Contact details updated successfully!', 'success');
            await loadCustomerData();
        } catch (err) {
            auth.showNotification(err.message, 'danger');
        }
    });

    // Change PIN Form
    document.getElementById('changePinForm')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const oldPin = document.getElementById('oldPinInput').value;
        const newPin = document.getElementById('newPinInput').value;

        try {
            await api.customers.changePin(currentUser.userId, oldPin, newPin);
            auth.showNotification('Security PIN successfully updated!', 'success');
            document.getElementById('oldPinInput').value = '';
            document.getElementById('newPinInput').value = '';
        } catch (err) {
            auth.showNotification(err.message, 'danger');
        }
    });
}
