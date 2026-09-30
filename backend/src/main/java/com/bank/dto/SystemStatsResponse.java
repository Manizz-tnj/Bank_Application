package com.bank.dto;

public class SystemStatsResponse {

    private long totalCustomers;
    private long totalAccounts;
    private long activeAccounts;
    private long lockedAccounts;
    private long closedAccounts;
    private double totalBankBalance;
    private long totalTransactions;
    private double totalDepositVolume;
    private double totalWithdrawVolume;
    private double totalTransferVolume;
    private long totalAlerts;
    private long highAlerts;
    private long criticalAlerts;

    public SystemStatsResponse() {
    }

    public SystemStatsResponse(long totalCustomers, long totalAccounts, long activeAccounts,
                               long lockedAccounts, long closedAccounts, double totalBankBalance,
                               long totalTransactions, double totalDepositVolume, double totalWithdrawVolume,
                               double totalTransferVolume, long totalAlerts, long highAlerts, long criticalAlerts) {
        this.totalCustomers = totalCustomers;
        this.totalAccounts = totalAccounts;
        this.activeAccounts = activeAccounts;
        this.lockedAccounts = lockedAccounts;
        this.closedAccounts = closedAccounts;
        this.totalBankBalance = totalBankBalance;
        this.totalTransactions = totalTransactions;
        this.totalDepositVolume = totalDepositVolume;
        this.totalWithdrawVolume = totalWithdrawVolume;
        this.totalTransferVolume = totalTransferVolume;
        this.totalAlerts = totalAlerts;
        this.highAlerts = highAlerts;
        this.criticalAlerts = criticalAlerts;
    }

    public long getTotalCustomers() {
        return totalCustomers;
    }

    public void setTotalCustomers(long totalCustomers) {
        this.totalCustomers = totalCustomers;
    }

    public long getTotalAccounts() {
        return totalAccounts;
    }

    public void setTotalAccounts(long totalAccounts) {
        this.totalAccounts = totalAccounts;
    }

    public long getActiveAccounts() {
        return activeAccounts;
    }

    public void setActiveAccounts(long activeAccounts) {
        this.activeAccounts = activeAccounts;
    }

    public long getLockedAccounts() {
        return lockedAccounts;
    }

    public void setLockedAccounts(long lockedAccounts) {
        this.lockedAccounts = lockedAccounts;
    }

    public long getClosedAccounts() {
        return closedAccounts;
    }

    public void setClosedAccounts(long closedAccounts) {
        this.closedAccounts = closedAccounts;
    }

    public double getTotalBankBalance() {
        return totalBankBalance;
    }

    public void setTotalBankBalance(double totalBankBalance) {
        this.totalBankBalance = totalBankBalance;
    }

    public long getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(long totalTransactions) {
        this.totalTransactions = totalTransactions;
    }

    public double getTotalDepositVolume() {
        return totalDepositVolume;
    }

    public void setTotalDepositVolume(double totalDepositVolume) {
        this.totalDepositVolume = totalDepositVolume;
    }

    public double getTotalWithdrawVolume() {
        return totalWithdrawVolume;
    }

    public void setTotalWithdrawVolume(double totalWithdrawVolume) {
        this.totalWithdrawVolume = totalWithdrawVolume;
    }

    public double getTotalTransferVolume() {
        return totalTransferVolume;
    }

    public void setTotalTransferVolume(double totalTransferVolume) {
        this.totalTransferVolume = totalTransferVolume;
    }

    public long getTotalAlerts() {
        return totalAlerts;
    }

    public void setTotalAlerts(long totalAlerts) {
        this.totalAlerts = totalAlerts;
    }

    public long getHighAlerts() {
        return highAlerts;
    }

    public void setHighAlerts(long highAlerts) {
        this.highAlerts = highAlerts;
    }

    public long getCriticalAlerts() {
        return criticalAlerts;
    }

    public void setCriticalAlerts(long criticalAlerts) {
        this.criticalAlerts = criticalAlerts;
    }
}
