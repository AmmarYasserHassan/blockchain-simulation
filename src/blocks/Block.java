package blocks;

import java.sql.Timestamp;
import network.Node;
import transactions.Transaction;

public class Block {

	protected Transaction[] transactions;
	protected String previousBlockHashPointer;
	protected String thisBlockHashPointer;
	protected Long nonce;
	protected java.sql.Timestamp createdAt;
	protected Node miner;
	protected int number;

	public Block(Transaction[] transactions, String previousBlockHashPointer, String thisBlockHashPointer, Long nonce,
			Timestamp createdAt, Node minedBy, int number) {
		this.transactions = transactions;
		this.previousBlockHashPointer = previousBlockHashPointer;
		this.thisBlockHashPointer = thisBlockHashPointer;
		this.nonce = nonce;
		this.createdAt = createdAt;
		this.miner = minedBy;
		this.number = number;
	}
	
	public Block() {}


	public Transaction[] getTransactions() {
		return transactions;
	}

	public String getPreviousBlockHashPointer() {
		return previousBlockHashPointer;
	}

	public String getThisBlockHashPointer() {
		return thisBlockHashPointer;
	}

	public Long getNonce() {
		return nonce;
	}

	public java.sql.Timestamp getCreatedAt() {
		return createdAt;
	}

	public Node getMiner() {
		return this.miner;
	}

	public boolean containsTransaction(String id) {
		for (Transaction tran : transactions)
			if (tran.getId().equals(id))
				return true;
		return false;
	}

	public int getNumber() {
		return this.number;
	}

	public Block clone() {

		Transaction[] newTransactions = new Transaction[this.transactions.length];

		for (int i = 0; i < this.transactions.length; i++)
			newTransactions[i] = this.transactions[i].clone();

		return new Block(newTransactions, previousBlockHashPointer, thisBlockHashPointer, nonce, createdAt, miner,number);
	}

	public String toString() {
		
		String allTransactions="";
		String minedby = "no one";
		if(this.miner!=null)
			minedby = this.miner.getDisplayName();
		
		for(Transaction tran:this.transactions)
			allTransactions+=tran.toString()+"\n";
		
		
		//TODO
//		return "\n" + "------------------------------- Block " + number+ " ------------------------------------"+"\n"+
//				"Previous hash pointer: " + this.previousBlockHashPointer + "\n"+
//				"Hash pointer: " + this.thisBlockHashPointer+ "\n"+
//				"Nonce: " + this.nonce + "\n"+
//				"Mined by: " + minedby + "\n"+
//				allTransactions
//		+ "---------------------------------------------------------------------------------------------" + "\n";

		return "Block: " + this.number + " mined by: " + this.miner + " \n" +"This hash: " + this.getThisBlockHashPointer()+  "\n"+"Previous: " + this.getPreviousBlockHashPointer();
	}

}
