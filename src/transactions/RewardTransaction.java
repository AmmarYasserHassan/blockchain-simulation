package transactions;

import java.security.PublicKey;

import blocks.Block;

public class RewardTransaction extends Transaction {

	private UTXOReward output;

	public RewardTransaction(UTXOReward reward) {
		super();
		this.output = reward;
	}
	
	private RewardTransaction(String id, byte[] signature, java.sql.Timestamp createdAt,
			java.sql.Timestamp recievedAt, java.sql.Timestamp addedToBlockAt,UTXOReward output)
	{
		this.id = id;
		this.signature = signature;
		this.createdAt = createdAt;
		this.recievedAt = recievedAt;
		this.addedToBlockAt = addedToBlockAt;
		this.output = output;
	}

	public TransactionOutput getOutput(int index) {
		return index==0? this.output:null;
	}

	public String toString() {
//TODO
		return "********************************** Reward: " +this.id+ "**********************************" + "\n"
				+ this.output.toString() + "\n" + "Created at: " + this.getCreatedAt() + "\n"
				+ "*************************************************************" + "\n";
//		return "Transaction: " + this.id;
	}

	public RewardTransaction clone() {
		return new RewardTransaction(id, signature, createdAt, recievedAt, addedToBlockAt, output.clone());
	}


}
