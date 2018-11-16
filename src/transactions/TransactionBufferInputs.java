package transactions;

import java.security.PublicKey;

public class TransactionBufferInputs {
	public PublicKey recieverNPublickKey;
	public String recieverDisplayName;
	public double amount;
	public double fees;
	public Transaction createdTransaction;
	
	public TransactionBufferInputs(PublicKey recieverNPublickKey, String recieverDisplayName, double amount,
			double fees) {
		this.recieverNPublickKey = recieverNPublickKey;
		this.recieverDisplayName = recieverDisplayName;
		this.amount = amount;
		this.fees = fees;
	}
}
