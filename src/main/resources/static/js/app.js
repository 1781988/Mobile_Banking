const state = {
    token: sessionStorage.getItem('mobileBankToken'),
    user: JSON.parse(sessionStorage.getItem('mobileBankUser') || 'null'),
    accounts: [],
    transfers: []
};

const $ = (id) => document.getElementById(id);
const loginView = $('loginView');
const appView = $('appView');
const sessionActions = $('sessionActions');

function escapeText(value) {
    return value == null ? '' : String(value);
}

function formatMoney(value, currency = 'CNY') {
    return new Intl.NumberFormat('zh-CN', {
        style: 'currency', currency, minimumFractionDigits: 2
    }).format(Number(value || 0));
}

function formatTime(value) {
    if (!value) return '-';
    return new Intl.DateTimeFormat('zh-CN', {
        year: 'numeric', month: '2-digit', day: '2-digit',
        hour: '2-digit', minute: '2-digit', second: '2-digit'
    }).format(new Date(value));
}

function shortId(value) {
    if (!value) return '-';
    return value.length > 12 ? `${value.slice(0, 8)}…` : value;
}

function statusChip(status) {
    const span = document.createElement('span');
    span.className = `status-chip status-${String(status).toLowerCase().replaceAll('_', '-')}`;
    span.textContent = status;
    return span;
}

function setCell(row, value, className) {
    const cell = document.createElement('td');
    if (value instanceof Node) cell.appendChild(value);
    else cell.textContent = escapeText(value);
    if (className) cell.className = className;
    row.appendChild(cell);
    return cell;
}

async function api(path, options = {}) {
    const headers = new Headers(options.headers || {});
    headers.set('Accept', 'application/json');
    if (options.body && !headers.has('Content-Type')) headers.set('Content-Type', 'application/json');
    if (state.token) headers.set('Authorization', `Bearer ${state.token}`);

    const response = await fetch(path, {...options, headers});
    let payload = null;
    try { payload = await response.json(); } catch (_) { /* no response body */ }
    if (!response.ok) {
        if (response.status === 401 && path !== '/api/v1/auth/login') logout(false);
        const error = new Error(payload?.message || `请求失败（HTTP ${response.status}）`);
        error.code = payload?.code;
        error.requestId = payload?.requestId;
        throw error;
    }
    return payload?.data;
}

function showToast(message) {
    const toast = $('toast');
    toast.textContent = message;
    toast.classList.remove('hidden');
    window.clearTimeout(showToast.timer);
    showToast.timer = window.setTimeout(() => toast.classList.add('hidden'), 3500);
}

function switchToApp() {
    loginView.classList.add('hidden');
    appView.classList.remove('hidden');
    sessionActions.classList.remove('hidden');
    $('currentUser').textContent = `${state.user.displayName} · ${state.user.role}`;
    $('welcomeTitle').textContent = state.user.role === 'ADMIN' ? '运营与风险总览' : `你好，${state.user.displayName}`;
    $('customerDashboard').classList.toggle('hidden', state.user.role === 'ADMIN');
    $('adminDashboard').classList.toggle('hidden', state.user.role !== 'ADMIN');
}

function switchToLogin() {
    loginView.classList.remove('hidden');
    appView.classList.add('hidden');
    sessionActions.classList.add('hidden');
}

async function login(event) {
    event.preventDefault();
    $('loginError').textContent = '';
    const button = event.submitter;
    button.disabled = true;
    button.textContent = '正在验证…';
    try {
        const data = await api('/api/v1/auth/login', {
            method: 'POST',
            body: JSON.stringify({username: $('username').value, password: $('password').value})
        });
        state.token = data.accessToken;
        state.user = data.user;
        sessionStorage.setItem('mobileBankToken', state.token);
        sessionStorage.setItem('mobileBankUser', JSON.stringify(state.user));
        switchToApp();
        await loadCurrentView();
    } catch (error) {
        $('loginError').textContent = `${error.message}${error.requestId ? `（请求 ${error.requestId}）` : ''}`;
    } finally {
        button.disabled = false;
        button.textContent = '进入数字银行';
    }
}

async function logout(callServer = true) {
    if (callServer && state.token) {
        try { await api('/api/v1/auth/logout', {method: 'POST'}); } catch (_) { /* local logout still proceeds */ }
    }
    state.token = null;
    state.user = null;
    state.accounts = [];
    state.transfers = [];
    sessionStorage.removeItem('mobileBankToken');
    sessionStorage.removeItem('mobileBankUser');
    switchToLogin();
}

async function loadCurrentView() {
    if (state.user.role === 'ADMIN') await loadAdminDashboard();
    else await loadCustomerDashboard();
}

async function loadCustomerDashboard() {
    try {
        const [accounts, transfers] = await Promise.all([
            api('/api/v1/accounts'),
            api('/api/v1/transfers?size=20')
        ]);
        state.accounts = accounts;
        state.transfers = transfers.content;
        renderAccounts();
        renderTransfers();
        await loadStatement();
    } catch (error) {
        showToast(error.message);
    }
}

function renderAccounts() {
    $('accountCount').textContent = state.accounts.length;
    const total = state.accounts
        .filter(a => a.currency === 'CNY' && a.status === 'ACTIVE')
        .reduce((sum, account) => sum + Number(account.balance), 0);
    $('totalBalance').textContent = formatMoney(total);
    $('transferCount').textContent = state.transfers.length;

    const container = $('accountCards');
    container.replaceChildren();
    const payerSelect = $('payerAccount');
    const statementSelect = $('statementAccount');
    payerSelect.replaceChildren();
    statementSelect.replaceChildren();

    if (!state.accounts.length) {
        const empty = document.createElement('div');
        empty.className = 'empty-state';
        empty.textContent = '当前用户没有可用账户';
        container.appendChild(empty);
        return;
    }

    state.accounts.forEach(account => {
        const card = document.createElement('article');
        card.className = 'account-card';
        const top = document.createElement('div');
        top.className = 'account-top';
        const currency = document.createElement('span');
        currency.className = 'currency';
        currency.textContent = `${account.currency} · 储蓄账户`;
        top.append(currency, statusChip(account.status));
        const number = document.createElement('div');
        number.className = 'account-number';
        number.textContent = account.maskedAccountNumber;
        const balance = document.createElement('div');
        balance.className = 'balance';
        balance.textContent = formatMoney(account.balance, account.currency);
        card.append(top, number, balance);
        container.appendChild(card);

        const option = new Option(`${account.maskedAccountNumber} · ${formatMoney(account.balance)}`, account.accountNumber);
        option.disabled = account.status !== 'ACTIVE';
        payerSelect.add(option);
        statementSelect.add(new Option(account.maskedAccountNumber, account.accountNumber));
    });
}

function renderTransfers() {
    const body = $('transferTable');
    body.replaceChildren();
    if (!state.transfers.length) {
        appendEmptyRow(body, 5, '暂无交易订单');
        return;
    }
    state.transfers.forEach(transfer => {
        const row = document.createElement('tr');
        setCell(row, shortId(transfer.transferId));
        const own = state.accounts.some(a => transfer.payerAccountNumber.includes(a.accountNumber?.slice(-4)));
        setCell(row, own ? '转出' : '转入');
        setCell(row, formatMoney(transfer.amount, transfer.currency), own ? 'amount-debit' : 'amount-credit');
        setCell(row, statusChip(transfer.status));
        setCell(row, formatTime(transfer.createdAt));
        row.title = `订单号：${transfer.transferId}`;
        body.appendChild(row);
    });
}

async function loadStatement() {
    const accountNumber = $('statementAccount').value;
    const body = $('statementTable');
    if (!accountNumber) {
        body.replaceChildren();
        appendEmptyRow(body, 5, '请选择账户');
        return;
    }
    try {
        const page = await api(`/api/v1/accounts/${encodeURIComponent(accountNumber)}/statement?size=20`);
        body.replaceChildren();
        if (!page.content.length) {
            appendEmptyRow(body, 5, '暂无账户流水');
            return;
        }
        page.content.forEach(entry => {
            const row = document.createElement('tr');
            setCell(row, entry.direction === 'DEBIT' ? '支出' : '收入');
            setCell(row,
                `${entry.direction === 'DEBIT' ? '-' : '+'}${formatMoney(entry.amount)}`,
                entry.direction === 'DEBIT' ? 'amount-debit' : 'amount-credit');
            setCell(row, formatMoney(entry.balanceAfter));
            setCell(row, entry.counterpartyAccountNumber);
            setCell(row, formatTime(entry.createdAt));
            body.appendChild(row);
        });
    } catch (error) {
        showToast(error.message);
    }
}

async function submitTransfer(event) {
    event.preventDefault();
    const message = $('transferMessage');
    message.textContent = '';
    const button = event.submitter;
    button.disabled = true;
    button.textContent = '交易处理中…';
    try {
        const key = `web-${crypto.randomUUID()}`;
        const transfer = await api('/api/v1/transfers', {
            method: 'POST',
            headers: {'Idempotency-Key': key},
            body: JSON.stringify({
                payerAccountNumber: $('payerAccount').value,
                payeeAccountNumber: $('payeeAccount').value.trim(),
                amount: $('transferAmount').value,
                remark: $('transferRemark').value.trim() || null
            })
        });
        message.textContent = `转账成功，订单号 ${transfer.transferId}`;
        $('transferAmount').value = '';
        $('transferRemark').value = '';
        showToast('资金已完成双边记账');
        await loadCustomerDashboard();
    } catch (error) {
        message.textContent = `${error.message}${error.requestId ? `（请求 ${error.requestId}）` : ''}`;
        message.style.color = 'var(--danger)';
    } finally {
        button.disabled = false;
        button.textContent = '确认转账';
    }
}

async function loadAdminDashboard() {
    try {
        const results = await Promise.allSettled([
            api('/api/v1/admin/accounts?size=50'),
            api('/api/v1/admin/risk-events?size=30'),
            api('/api/v1/admin/audit-logs?size=30'),
            api('/api/v1/admin/reconciliations/latest')
        ]);
        renderAdminAccounts(results[0].status === 'fulfilled' ? results[0].value.content : []);
        renderRisks(results[1].status === 'fulfilled' ? results[1].value.content : []);
        renderAudits(results[2].status === 'fulfilled' ? results[2].value.content : []);
        renderReconciliation(results[3].status === 'fulfilled' ? results[3].value : null);
    } catch (error) {
        showToast(error.message);
    }
}

function renderAdminAccounts(accounts) {
    const body = $('adminAccountTable');
    body.replaceChildren();
    if (!accounts.length) return appendEmptyRow(body, 5, '暂无账户');
    accounts.forEach(account => {
        const row = document.createElement('tr');
        setCell(row, account.maskedAccountNumber);
        setCell(row, account.ownerName);
        setCell(row, formatMoney(account.balance, account.currency));
        setCell(row, statusChip(account.status));
        const actions = document.createElement('div');
        actions.className = 'inline-actions';
        const nextStatus = account.status === 'ACTIVE' ? 'FROZEN' : 'ACTIVE';
        const button = document.createElement('button');
        button.className = 'mini-button';
        button.type = 'button';
        button.textContent = nextStatus === 'FROZEN' ? '冻结' : '解冻';
        button.addEventListener('click', () => changeAccountStatus(account.accountNumber, nextStatus));
        actions.appendChild(button);
        setCell(row, actions);
        body.appendChild(row);
    });
}

async function changeAccountStatus(accountNumber, status) {
    const reason = window.prompt(`请输入${status === 'FROZEN' ? '冻结' : '解冻'}原因：`, '运营人工复核');
    if (!reason) return;
    try {
        await api(`/api/v1/admin/accounts/${encodeURIComponent(accountNumber)}/status`, {
            method: 'PUT', body: JSON.stringify({status, reason})
        });
        showToast(`账户状态已更新为 ${status}`);
        await loadAdminDashboard();
    } catch (error) {
        showToast(error.message);
    }
}

function renderRisks(events) {
    const body = $('riskTable');
    body.replaceChildren();
    if (!events.length) return appendEmptyRow(body, 5, '暂无风控事件');
    events.forEach(event => {
        const row = document.createElement('tr');
        setCell(row, statusChip(event.level));
        setCell(row, event.eventType);
        setCell(row, event.amount == null ? '-' : formatMoney(event.amount));
        setCell(row, event.reason);
        setCell(row, formatTime(event.createdAt));
        body.appendChild(row);
    });
}

function renderAudits(logs) {
    const body = $('auditTable');
    body.replaceChildren();
    if (!logs.length) return appendEmptyRow(body, 5, '暂无审计日志');
    logs.forEach(log => {
        const row = document.createElement('tr');
        setCell(row, log.action);
        setCell(row, statusChip(log.result));
        setCell(row, `${log.resourceType}:${shortId(log.resourceId)}`);
        setCell(row, shortId(log.requestId));
        setCell(row, formatTime(log.createdAt));
        body.appendChild(row);
    });
}

function renderReconciliation(batch) {
    const container = $('reconciliationSummary');
    container.replaceChildren();
    if (!batch) {
        container.className = 'empty-state';
        container.textContent = '尚未执行对账';
        return;
    }
    container.className = 'recon-card';
    const title = document.createElement('strong');
    title.textContent = `业务日期 ${batch.businessDate}`;
    const status = statusChip(batch.status);
    const grid = document.createElement('div');
    grid.className = 'recon-grid';
    [['核对账户', batch.totalAccounts], ['差异账户', batch.mismatchCount], ['批次号', shortId(batch.id)], ['完成时间', formatTime(batch.completedAt)]].forEach(([label, value]) => {
        const item = document.createElement('div');
        const span = document.createElement('span'); span.textContent = label;
        const bold = document.createElement('b'); bold.textContent = value;
        item.append(span, bold); grid.appendChild(item);
    });
    container.append(title, status, grid);
}

async function runReconciliation() {
    const button = $('runReconciliationBtn');
    button.disabled = true;
    button.textContent = '正在核对…';
    try {
        const batch = await api('/api/v1/admin/reconciliations', {method: 'POST'});
        renderReconciliation(batch);
        showToast(`对账完成，差异账户 ${batch.mismatchCount} 个`);
        await loadAdminDashboard();
    } catch (error) {
        showToast(error.message);
    } finally {
        button.disabled = false;
        button.textContent = '立即执行对账';
    }
}

function appendEmptyRow(body, columns, message) {
    const row = document.createElement('tr');
    const cell = document.createElement('td');
    cell.colSpan = columns;
    cell.textContent = message;
    cell.style.textAlign = 'center';
    cell.style.color = 'var(--muted)';
    cell.style.padding = '30px';
    row.appendChild(cell);
    body.appendChild(row);
}

$('loginForm').addEventListener('submit', login);
$('logoutBtn').addEventListener('click', () => logout(true));
$('transferForm').addEventListener('submit', submitTransfer);
$('statementAccount').addEventListener('change', loadStatement);
$('refreshAccountsBtn').addEventListener('click', loadCustomerDashboard);
$('refreshTransfersBtn').addEventListener('click', loadCustomerDashboard);
$('refreshAdminBtn').addEventListener('click', loadAdminDashboard);
$('runReconciliationBtn').addEventListener('click', runReconciliation);

if (state.token && state.user) {
    switchToApp();
    loadCurrentView();
} else {
    switchToLogin();
}
