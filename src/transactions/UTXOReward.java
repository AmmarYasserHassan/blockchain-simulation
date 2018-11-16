package transactions;

import java.security.PublicKey;

public class UTXOReward extends TransactionOutput{
	
	public UTXOReward(PublicKey recieverPublicKey,String recieverDisplayName ,double amount) {
		super(recieverPublicKey,recieverDisplayName,amount);
	}
	
	public UTXOReward(PublicKey recieverPublicKey,String recieverDisplayName ,double amount,String id) {
		super(recieverPublicKey,recieverDisplayName,amount,id);
	}
	
	
	public String getRecieverDisplayName() {
		return this.recieverDisplayName;
	}

	public String toString() {
		return "Newly generated coins to miner [ " +recieverDisplayName + " ] " + " --> " + amount+"BTC";
	}
	
	public UTXOReward clone() {
		
		return new UTXOReward(recieverPublicKey, this.recieverDisplayName, amount);
	}


}
