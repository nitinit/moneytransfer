package com.usb.model;

public class AcccountBalance {
	
	private String accountId;
	private double balance;
		
	public AcccountBalance(String accountId, double balance) {
		super();
		this.accountId = accountId;
		this.balance = balance;
	}
	
	public String getAccountId() {
		return accountId;
	}
	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}
	public double getBalance() {
		return balance;
	}
	public void setBalance(double balance) {
		this.balance = balance;
	}
}
