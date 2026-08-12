package com.mobilebanking.platform.common.util;

public final class AccountMasker {

    private AccountMasker() {
    }

    public static String mask(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 8) {
            return accountNumber;
        }
        return accountNumber.substring(0, 4)
                + " **** **** "
                + accountNumber.substring(accountNumber.length() - 4);
    }
}
