package transactions;

import java.security.PublicKey;

public abstract class TransactionOutput {
	
	// Fancy printing
	protected String recieverDisplayName;
	
	
	protected PublicKey recieverPublicKey;
	protected double amount;
	protected String id;

	public TransactionOutput(PublicKey recieverPublicKey,String recieverDisplayName, double amount) {
		this.recieverPublicKey = recieverPublicKey;
		this.recieverDisplayName = recieverDisplayName;
		this.amount = amount;
	}
	
	public TransactionOutput(PublicKey recieverPublicKey,String recieverDisplayName, double amount,String id) {
		this.recieverPublicKey = recieverPublicKey;
		this.recieverDisplayName = recieverDisplayName;
		this.amount = amount;
		this.id= id;
	}

	public PublicKey getRecieverPublicKey() {
		return recieverPublicKey;
	}

	public String getRecieverDisplayName() {
		return this.recieverDisplayName;
	}
	
	public double getAmount() {
		return amount;
	}
	
	public String getId() {
		return this.id;
	}
	


}
