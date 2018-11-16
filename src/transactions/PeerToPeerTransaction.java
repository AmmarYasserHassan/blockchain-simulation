package transactions;

import java.security.PublicKey;
import java.util.ArrayList;
import blocks.Block;

public class PeerToPeerTransaction extends Transaction {

	// For fancy printing only
	private String senderDisplayName;

	private PublicKey senderPublicKey;
	private ArrayList<Trindex> inputs;
	private double totalInAmount;
	private UTXO payment;
	private UTXO change;
	private double addedFeeToMiner;

	// For validation and overcoming null pointers
	private boolean created;

	// For cloning
	private PeerToPeerTransaction(String id, byte[] signature, java.sql.Timestamp createdAt,
			java.sql.Timestamp recievedAt, java.sql.Timestamp addedToBlockAt,
			String senderDisplayName, PublicKey senderPublicKey, ArrayList<Trindex> inputs, double totalInAmount,
			UTXO payment, UTXO change, double addedFeeToMiner, boolean created) {
		this.id = id;
		this.signature = signature;
		this.createdAt = createdAt;
		this.recievedAt = recievedAt;
		this.addedToBlockAt = addedToBlockAt;
		this.senderDisplayName = senderDisplayName;
		this.senderPublicKey = senderPublicKey;
		this.inputs = inputs;
		this.totalInAmount = totalInAmount;
		this.payment = payment;
		this.change = change;
		this.addedFeeToMiner = addedFeeToMiner;
		this.created = created;
	}

	public PeerToPeerTransaction(PublicKey senderPublicKey, PublicKey reciever, String senderDisplayName,
			String recieverDisplayName, double amount, double fees, ArrayList<Trindex> inputs) {
		super();
		this.senderPublicKey = senderPublicKey;
		this.inputs = inputs;
		this.senderDisplayName = senderDisplayName;
		getTotalAmountFromInputs();
		if (this.totalInAmount >= (amount + fees))
			createPayment(amount, fees, reciever, recieverDisplayName);
		else
			System.err.println("Can not make transaction" + "\n" + "Total amount from inputs: " + this.totalInAmount
					+ "\n" + "Transaction amount: " + amount + "\n" + "Added fees: " + fees);

	}

	private void getTotalAmountFromInputs() {

		for (Trindex trin : inputs) {
			Transaction trans = trin.getTransaction();
			int index = trin.getIndex();			
			if (trans.getOutput(index).getRecieverPublicKey().equals(this.senderPublicKey)) {
				this.totalInAmount+= trans.getOutput(index).getAmount();
			}
		}

	}

	public TransactionOutput getChange() {
		return this.change;
	}

	public TransactionOutput getPayment() {
		return this.payment;
	}
	
	public TransactionOutput getOutput(int index) {
		if(index ==0)
			return this.getPayment();
		if(index ==1)
			return this.getChange();
		return null;
	}

	public double getFees() {
		return this.addedFeeToMiner;
	}

	public PublicKey getSenderPublicKey() {
		return this.senderPublicKey;
	}

	public ArrayList<Trindex> getInputs() {
		return this.inputs;
	}

	public String toString() {
		if (created) {
			if (this.change != null)
				return "\n" + "*************************** Transaction: " + this.id  +"**************************"
						+ "\n" + "Payment: " + this.payment+ " \n" + "Change: " + this.change.toString()
						+ " \n" + "Added fees: " + this.getFees() + "\n" + "Created at: " + this.getCreatedAt() + "\n"
						+ "*****************************************************" + "\n";
			else
				return "\n" + "*************************** Transaction: " + this.id  +"**************************"
						+ "\n" + "Payment: " + this.payment + " \n" + "Change: 0.0" + " \n" + "Added Fees: "
						+ this.getFees() + "\n" + "Created at: " + this.getCreatedAt() + "\n"
						+ "*****************************************************" + "\n";
			//return "Transaction: " + this.id;
		} else {
			return "";
		}
	}

	private void createPayment(double amount, double fees,PublicKey reciever, String recieverDisplayName) {
		double change = this.totalInAmount - (amount + fees);
		this.payment = new UTXO(this.senderPublicKey, reciever, this.senderDisplayName, recieverDisplayName, amount,this.getId()+"0");
		if (change != 0)
			this.change = new UTXO(this.senderPublicKey, this.senderPublicKey, this.senderDisplayName,this.senderDisplayName, change,this.id+"1");
		this.addedFeeToMiner = fees;
		this.created = true;
	}

	
	public boolean isCreated() {
		return this.created;
	}


	public PeerToPeerTransaction clone() {
		
		ArrayList<Trindex> clonedInputs = new ArrayList<Trindex>();
		for (Trindex trin : inputs)
			clonedInputs.add(trin.clone());
		return new PeerToPeerTransaction(id, signature, createdAt, recievedAt, addedToBlockAt,
				senderDisplayName, senderPublicKey, clonedInputs, totalInAmount, payment.clone(), change.clone(),
				addedFeeToMiner, created);
	}

}
