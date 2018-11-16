package transactions;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.UUID;
import blocks.Block;
import network.Node;

public abstract class Transaction {
	
	//TODO
	public String id;
	protected byte[] signature;    
    protected java.sql.Timestamp createdAt;
    protected java.sql.Timestamp recievedAt;
    protected java.sql.Timestamp addedToBlockAt;
    
    
    protected int numberOfConfirmations;
    
    //to avoid one node sending multiple confirmations
    ArrayList<PublicKey> nodesWhoConfirmedThisTransaction;
    
	public Transaction()
    {
    	this.id =UUID.randomUUID().toString();
    	this.createdAt = new java.sql.Timestamp(System.currentTimeMillis());
    	this.nodesWhoConfirmedThisTransaction = new ArrayList<PublicKey>();
    	
    }
    
    public int getNumberOfConfirmations() {
		return numberOfConfirmations;
	}

    public void addConfirmation(PublicKey confirmer) {
    		boolean wasConfirmedBefore = false;
    		for(PublicKey previouslyConfirmed: nodesWhoConfirmedThisTransaction)
    			if(previouslyConfirmed.equals(confirmer)) {
    				wasConfirmedBefore = true;
    				break;
    			}
    		
    		if(!wasConfirmedBefore)
		this.numberOfConfirmations++;
	}
    
	public String getId() {
		return this.id;
	}
	
	public byte[] getSignature() {
		return signature;
	}

	public void setSignature(byte[] signature) {
		this.signature = signature;
	}
	
	public boolean equals(Object o) {
		return this.id.equals(((Transaction) o).id);
	}
	
	public abstract TransactionOutput getOutput(int index);
		

	public java.sql.Timestamp getCreatedAt() {
		return createdAt;
	}
	

	public java.sql.Timestamp getRecievedAt() {
		return recievedAt;
	}

	public java.sql.Timestamp getAddedToBlockAt() {
		return addedToBlockAt;
	}

	
	protected void setRecievedAt(java.sql.Timestamp recievedAt) {
		this.recievedAt = recievedAt;
	}

	protected void setAddedToBlockAt(java.sql.Timestamp addedToBlockAt) {
		this.addedToBlockAt = addedToBlockAt;
	}

	public abstract Transaction clone();
	
	
	public static void main(String [] args) {
		
		
		Node ammar = new Node("ammar");
		Node leo = new Node("leo");
		
		UTXOReward reward = new UTXOReward(ammar.getPublicKey(), ammar.getDisplayName(),2.5);
		Transaction trans = new RewardTransaction(reward);
		
		ArrayList<Transaction> inputs = new ArrayList<Transaction>();
		inputs.add(trans);
		
	//	Transaction p2p = new PeerToPeerTransaction(ammar.getPublicKey(), leo.getPublicKey(), ammar.getDisplayName(), leo.getDisplayName(), 2.8, 0,inputs);
	//	Transaction p2p2 = new PeerToPeerTransaction(ammar.getPublicKey(), leo.getPublicKey(), ammar.getDisplayName(), leo.getDisplayName(), 2.4,0.05, inputs);

		//System.out.println(p2p);
	}
	

	
}
