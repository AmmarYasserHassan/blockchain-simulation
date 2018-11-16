package network;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import blocks.Block;
import blocks.ChainedHashMap;
import transactions.PeerToPeerTransaction;
import transactions.RewardTransaction;
import transactions.Transaction;
import transactions.TransactionBufferInputs;
import transactions.Trindex;
import transactions.UTXOReward;

public class Node {

	// For printing only
	String displayName;

	private PublicKey publicKey;
	private PrivateKey privateKey;

	public ArrayList<Node> connectedNeighbors;

	public ArrayList<Transaction> transactionsNotInABlock;

	public ArrayList<Trindex> UTXOSet;
	public ArrayList<ChainedHashMap> forks;
	public ChainedHashMap blockChain;
	
	public ArrayList<TransactionBufferInputs> myTransactionsBuffer;
	int pendingTransactionIndex;

	private PrintStream stream;
	
	public ArrayList<ChainedHashMap> allForks;

	public Node(String displayName) {
		this.displayName = displayName;
		OutputStream out;
		try {
			out = new FileOutputStream(this.displayName + ".txt");
			this.stream = new PrintStream(out);
			stream.println("Logging for Node: " + this.displayName);
			stream.println();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		intializeKeys();
		intializeStructures();

	}

	public void fancyPrint(String s) {
		System.out.println(s);
		stream.println(s);
	}
	
	public void fancyPrint(String s,boolean error) {
		System.err.println(s);
		stream.println(s);
	}

	public void fancyPrint() {
		System.out.println();
		stream.println();
	}

	private void intializeStructures() {
		this.UTXOSet = new ArrayList<Trindex>();
		this.connectedNeighbors = new ArrayList<Node>();
		this.transactionsNotInABlock = new ArrayList<Transaction>();
		this.forks = new ArrayList<ChainedHashMap>();
		this.blockChain = new ChainedHashMap();
		this.myTransactionsBuffer = new ArrayList<TransactionBufferInputs>();
		this.allForks = new ArrayList<ChainedHashMap>();
		pendingTransactionIndex = 0;
	}

	public void intializeKeys() {
		KeyPairGenerator keyGen;
		try {
			keyGen = KeyPairGenerator.getInstance("DSA");
			keyGen.initialize(1024, new SecureRandom());
			KeyPair pair = keyGen.generateKeyPair();
			privateKey = pair.getPrivate();
			publicKey = pair.getPublic();
		} catch (NoSuchAlgorithmException e) {
			System.err.println(e.getMessage());
		}

	}

	public ArrayList<Node> getConnectedNeighbors() {
		return connectedNeighbors;
	}

	public void addNeighbor(Node n) {

		if (!this.connectedNeighbors.contains(n))
			this.connectedNeighbors.add(n);
	}

	public int getNeighborsSize() {
		return this.connectedNeighbors.size();
	}

	public void setConnectedNeighbors(ArrayList<Node> connectedNeighbors) {
		this.connectedNeighbors = connectedNeighbors;
	}

	public String getDisplayName() {
		return this.displayName;
	}

	public PublicKey getPublicKey() {
		return publicKey;
	}

	public void bootstrap(ChainedHashMap blocks) {

		for (int i = 0; i < blocks.size(); i++) {
			this.blockChain.put(blocks.get(i).getThisBlockHashPointer(), blocks.get(i));
			for (Transaction trans : blocks.get(i).getTransactions()) {
				if (trans instanceof PeerToPeerTransaction) {
					for (Trindex trin : ((PeerToPeerTransaction) trans).getInputs()) {
						if (this.UTXOSet.contains(trin))
							UTXOSet.remove(trin);
					}
					UTXOSet.add(new Trindex(trans.clone(), 0));
					if (((PeerToPeerTransaction) trans).getChange().getAmount() != 0)
						UTXOSet.add(new Trindex(trans.clone(), 1));
				} else
					UTXOSet.add(new Trindex(trans.clone(), 0));
			}
		}

	}

	public void printBlockChainsAndForks() {

		fancyPrint("Node: " + this.displayName);
		fancyPrint("Blockchain's ");
		fancyPrint(this.blockChain.toString());
		fancyPrint("forks: ");
		fancyPrint(this.forks.toString());
//		fancyPrint("all forks: ");
//		fancyPrint(this.allForks.toString());
	}

	public void recieveTransaction(Transaction transaction, Node sender) {

		//TODO
		if (!this.transactionsNotInABlock.contains(transaction) && validateTransactionOrigin(transaction)
				&& validateTransactionIntegrity(transaction)
				) 
				{
			fancyPrint("--------------------------- Transactions Recieved ----------------");
			fancyPrint("Node: " + this.displayName);
			fancyPrint("Recieved Transaction: " + "\n" + transaction);
			fancyPrint("From: " + sender.displayName);
			fancyPrint("Timestamp: " + ((System.currentTimeMillis() - Network.startTime)));
			fancyPrint("-------------------------------------------------------------------");
			fancyPrint();
			this.addTransactionToTransactionsNotInABlock(transaction);
			this.gossip(transaction);
		} else {
			fancyPrint("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX Transactions Ignored ");
			fancyPrint("Node: " + this.displayName);
			fancyPrint("Ignored Transaction: " + transaction.getId());
			fancyPrint("From: " + sender.displayName);
			fancyPrint("Timestamp: " + ((System.currentTimeMillis() - Network.startTime)));
			fancyPrint("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
			fancyPrint();
		}
	}

	public void runMyTransactionBuffer() {
		if(pendingTransactionIndex<=myTransactionsBuffer.size() && myTransactionsBuffer.size()>0) {
			ArrayList<Transaction> transactionsInBlockChain = new ArrayList<Transaction>();
			for (int i = 0; i < blockChain.size(); i++) {
				Block currentBlock = blockChain.get(i);
				for (Transaction trans : currentBlock.getTransactions()) {
					transactionsInBlockChain.add(trans);
				}
			}
			if(pendingTransactionIndex == 0) {
				System.err.println("ruuuuuuuuuning traaaaaaaaans");
				runTransaction(myTransactionsBuffer.get(pendingTransactionIndex));
				pendingTransactionIndex++;
			}else {
				if(transactionsInBlockChain.contains(myTransactionsBuffer.get(pendingTransactionIndex - 1).createdTransaction)) {
					runTransaction(myTransactionsBuffer.get(pendingTransactionIndex));
					pendingTransactionIndex++;
				}
			}
		}
	}
	
	public void makeTransaction(PublicKey recieverNPublickKey, String recieverDisplayName, double amount, double fees) {
//		myTransactionsBuffer.add(new TransactionBufferInputs(recieverNPublickKey, recieverDisplayName, amount, fees));
//		runMyTransactionBuffer();
		
//TODO
		ArrayList<Trindex> inputs = this.findTransactionsToSatisfyAmount(amount + fees);
		fancyPrint("In node: " + this.displayName);
		fancyPrint("Making a transaction: ");
		fancyPrint("Inputs are: " + inputs);
		PeerToPeerTransaction trans = new PeerToPeerTransaction(this.publicKey, recieverNPublickKey, this.displayName,
				recieverDisplayName, amount, fees, inputs);
		if (trans.isCreated()) {
		//	trans.id = this.displayName + " " + amount + " " + recieverDisplayName;
			//fancyPrint(trans.toString());
			this.signTransaction(trans);
			this.addTransactionToTransactionsNotInABlock(trans);
			this.gossip(trans);
		}
	}
	
	public void makeTransaction(PublicKey recieverNPublickKey, String recieverDisplayName, double amount, double fees,ArrayList<Trindex> inputs) {
//		myTransactionsBuffer.add(new TransactionBufferInputs(recieverNPublickKey, recieverDisplayName, amount, fees));
//		runMyTransactionBuffer();
		//TODO
			fancyPrint("In node: " + this.displayName);
		fancyPrint("Making a transaction: ");
		fancyPrint("Inputs are: " + inputs);
		PeerToPeerTransaction trans = new PeerToPeerTransaction(this.publicKey, recieverNPublickKey, this.displayName,
				recieverDisplayName, amount, fees, inputs);
		if (trans.isCreated()) {
		//	trans.id = this.displayName + " " + amount + " " + recieverDisplayName;
			fancyPrint(trans.toString());
			this.signTransaction(trans);
			this.addTransactionToTransactionsNotInABlock(trans);
			this.gossip(trans);
		}
	}
	
	public void runTransaction(TransactionBufferInputs transactionBufferInputs) {
		PublicKey recieverNPublickKey = transactionBufferInputs.recieverNPublickKey;
		String recieverDisplayName = transactionBufferInputs.recieverDisplayName;
		double amount = transactionBufferInputs.amount;
		double fees = transactionBufferInputs.fees;
		ArrayList<Trindex> inputs = this.findTransactionsToSatisfyAmount(amount + fees);
		fancyPrint("In node: " + this.displayName);
		fancyPrint("Making a transaction: ");
		fancyPrint("Inputs are: " + inputs);
		PeerToPeerTransaction trans = new PeerToPeerTransaction(this.publicKey, recieverNPublickKey, this.displayName,
				recieverDisplayName, amount, fees, inputs);
		if (trans.isCreated()) {
		//	trans.id = this.displayName + " " + amount + " " + recieverDisplayName;
			fancyPrint(trans.toString());
			this.signTransaction(trans);
			transactionBufferInputs.createdTransaction = trans;
			this.addTransactionToTransactionsNotInABlock(trans);
			this.gossip(trans);
		}
	}


	public ArrayList<Trindex> findTransactionsToSatisfyAmount(double amount) {

		ArrayList<Trindex> myTransactions = new ArrayList<Trindex>();
		ArrayList<Trindex> result = new ArrayList<Trindex>();

		for (Trindex trin : this.UTXOSet) {
			Transaction trans = trin.getTransaction();
			int index = trin.getIndex();
			if (trans.getOutput(index).getRecieverPublicKey().equals(this.getPublicKey()))
				myTransactions.add(trin);

		}

		Collections.sort(myTransactions, new Comparator<Trindex>() {
			public int compare(Trindex o1, Trindex o2) {
				if (o1.getTransaction().getOutput(o1.getIndex()).getAmount() == o2.getTransaction()
						.getOutput(o2.getIndex()).getAmount())
					return 0;

				if (o1.getTransaction().getOutput(o1.getIndex()).getAmount() < o2.getTransaction()
						.getOutput(o2.getIndex()).getAmount())
					return -1;
				return 1;
			}
		});

		double totalAggregatedAmount = 0;

		for (Trindex trin : myTransactions) {
			totalAggregatedAmount += trin.getTransaction().getOutput(trin.getIndex()).getAmount();
			if (totalAggregatedAmount >= amount) {
				result.add(trin);
				break;
			}

		}

		return result;

	}

	public void addTransactionToTransactionsNotInABlock(Transaction transaction) {
		this.transactionsNotInABlock.add(transaction);
		if (this.transactionsNotInABlock.size() >= Network.numberOfTransactionsInABlock)
			mine();
	}

	public void mine() {

		fancyPrint("");
		fancyPrint("Node: " + this.displayName + " Started mining");
		fancyPrint();

		Transaction[] transactionsToBeAddedToBlock = new Transaction[Network.numberOfTransactionsInABlock + 1];
		for (int i = 1; i < transactionsToBeAddedToBlock.length; i++)
			transactionsToBeAddedToBlock[i] = transactionsNotInABlock.remove(0).clone();

		UTXOReward reward = new UTXOReward(this.publicKey, this.displayName, Network.baseReward);
		Transaction trans = new RewardTransaction(reward);
		transactionsToBeAddedToBlock[0] = trans;

		int indexOfLongestChain = getIndexOfLongestChain();
		String previousHashPointer = "";
		int number = 0;
		if (indexOfLongestChain == -1) {
			previousHashPointer = this.blockChain.last().getThisBlockHashPointer();
			indexOfLongestChain = 0;
			number = this.blockChain.last().getNumber() + 1;
		} else {
			previousHashPointer = this.forks.get(indexOfLongestChain).last().getThisBlockHashPointer();
			number = this.forks.get(indexOfLongestChain).last().getNumber() + 1;
		}

		String allTransactionsAndPreviousHash = this.findAllStringsToBeHashed(transactionsToBeAddedToBlock,
				previousHashPointer);
		Long nonce = findNonce(allTransactionsAndPreviousHash, Network.nonceTarget);
		String thisBlockHashPointer = Network.sha256(nonce + allTransactionsAndPreviousHash);
		java.sql.Timestamp createdAt = new java.sql.Timestamp(System.currentTimeMillis());
		Block newBlock = new Block(transactionsToBeAddedToBlock, previousHashPointer, thisBlockHashPointer, nonce,
				createdAt, this, number);
//TODO
		fancyPrint("---------------------------------------");
		fancyPrint("Node: " + this.displayName);
		fancyPrint("submitted block to network");
		fancyPrint(newBlock.toString());
		fancyPrint("---------------------------------------");
		fancyPrint();
		Network.addBlockToNetwork(this, newBlock);

	}

	public int getIndexOfLongestChain() {

		if (this.forks.size() != 0) {

			int sizeOfLongestChain = this.forks.get(0).size();
			int indexOfLongestChain = 0;

			for (int i = 1; i < this.forks.size(); i++) {
				if (this.forks.get(i).size() > sizeOfLongestChain) {
					sizeOfLongestChain = this.forks.get(i).size();
					indexOfLongestChain = i;
				}
			}
			return indexOfLongestChain;
		} else {
			return -1;
		}

	}

	public void gossip(Transaction transaction) {
		//TODO
		int random = (int) (this.connectedNeighbors.size() * Math.random());
		Collections.shuffle(connectedNeighbors);
		for (int i = 0; i < random; i++)
			this.connectedNeighbors.get(i).recieveTransaction(transaction.clone(), this);
	
//		for(Node node:this.connectedNeighbors)
//			node.recieveTransaction(transaction, this);
	}

	public void gossip(Block block) {

		
//		if (block.getMiner().equals(this)) {
//			addBlockToItsPlaceInTheFork(block);
//		}
		
		//TODO
		int random = (int) (this.connectedNeighbors.size() * Math.random());
		Collections.shuffle(connectedNeighbors);
		for (int i = 0; i < random; i++)
			this.connectedNeighbors.get(i).recieveBlock(block.clone(), this);
		
//		for(Node node:this.connectedNeighbors)
//			node.recieveBlock(block, this);
	}

	public void recieveBlock(Block block, Node sender) {

		if (!recievedThisBlockBefore(block)) {
			if (allTransactionsInThisBlockHaveValidInputs(block)) {
				if (noCommonInputsBetweenTransactionsInThisBlock(block)) {
					fancyPrint("--------------------------- Block Recieved -----------------------");
					fancyPrint("Node: " + this.displayName);
					fancyPrint("Recieved Block: " + block.getThisBlockHashPointer());
					fancyPrint("Recieved Block: " + "\n" + block);
					fancyPrint("From: " + sender.displayName);
					fancyPrint("Timestamp: " + ((System.currentTimeMillis() - Network.startTime)));
					fancyPrint("------------------------------------------------------------------");
					fancyPrint();
					if(addBlockToItsPlaceInTheFork(block))
					{
						checkConsensus();
						gossip(block);
					}
				} else {
					fancyPrint("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX Block Ignored",true);
					fancyPrint("Node: " + this.displayName,true);
					fancyPrint("Fraud block: " + block.getThisBlockHashPointer(),true);
					fancyPrint("From: " + sender.displayName,true);
					fancyPrint("Timestamp: " + ((System.currentTimeMillis() - Network.startTime)),true);
					fancyPrint("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
					fancyPrint();
				}
			} else {
				fancyPrint("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX Block Ignored",true);
				fancyPrint("Node: " + this.displayName,true);
				fancyPrint("Block contains faulty transactions: " + block.getThisBlockHashPointer(),true);
				fancyPrint("From: " + sender.displayName,true);
				fancyPrint("Timestamp: " + ((System.currentTimeMillis() - Network.startTime)),true);
				fancyPrint("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",true);
				fancyPrint();
			}
		} else {
			fancyPrint("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX Block Ignored");
			fancyPrint("Node: " + this.displayName);
			fancyPrint("Recieved block before: " + block.getThisBlockHashPointer());
			fancyPrint("From: " + sender.displayName);
			fancyPrint("Timestamp: " + ((System.currentTimeMillis() - Network.startTime)));
			fancyPrint("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
			fancyPrint();
		}
	}

	public boolean noCommonInputsBetweenTransactionsInThisBlock(Block block) {

		ArrayList<Trindex> inputs = new ArrayList<Trindex>();
		for (Transaction trans : block.getTransactions())
			if (trans instanceof PeerToPeerTransaction)
				for (Trindex trin : ((PeerToPeerTransaction) trans).getInputs())
					if (!inputs.contains(trin))
						inputs.add(trin);
					else
						return false;
		return true;

	}

	public boolean addBlockToItsPlaceInTheFork(Block block) {

		String previousBlockHashPointer = block.getPreviousBlockHashPointer();
		if (this.blockChain.last().getThisBlockHashPointer().equals(previousBlockHashPointer)) {
			ChainedHashMap newFork = new ChainedHashMap();
			newFork.put(block.getThisBlockHashPointer(), block.clone());
			this.forks.add(newFork);
		} else {
			for (ChainedHashMap fork : this.forks) {
				if (fork.last().getThisBlockHashPointer().equals(previousBlockHashPointer)) {
					if (checkCommonPreviousTransactionsBetweenBlockAndFrok(block, fork)) {
						fork.put(block.getThisBlockHashPointer(), block.clone());
					} else {
						fancyPrint("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX Block Ignored");
						fancyPrint("Node: " + this.displayName);
						fancyPrint(
								"Block contains transactions from previous blocks: " + block.getThisBlockHashPointer());
						fancyPrint("Timestamp: " + ((System.currentTimeMillis() - Network.startTime)));
						fancyPrint("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
						fancyPrint();
						return false;

					}
					break;
				}
			}

		}

		return true;
	}

	public boolean checkCommonPreviousTransactionsBetweenBlockAndFrok(Block block, ChainedHashMap fork) {

		ArrayList<Transaction> transactionsInFork = new ArrayList<Transaction>();

		for (int i = 0; i < fork.size(); i++) {
			Block currentBlock = fork.get(i);
			for (Transaction trans : currentBlock.getTransactions()) {
				transactionsInFork.add(trans);
			}

		}

		for (Transaction trans : block.getTransactions()) {
			if (transactionsInFork.contains(trans))
				return false;
		}

		return true;
	}

	public void checkConsensus() {

		int longest = 0;
		int indexOfLongestChain = 0;
		int secondLongest = 0;

		for (int i = 0; i < this.forks.size(); i++) {
			int newSize = forks.get(i).size();
			if (newSize > longest) {
				longest = newSize;
				indexOfLongestChain = i;
			}
		}

		for (int i = 0; i < this.forks.size(); i++) {
			int newSize = forks.get(i).size();
			if (newSize <= longest && i != indexOfLongestChain) {
				if (newSize > secondLongest) {
					secondLongest = newSize;
				}
			}
		}

		if (longest - secondLongest >= Network.numberOfBlocksToReachConsensus) {

			ChainedHashMap longestChain = this.forks.get(indexOfLongestChain);
			int startingIndex = this.blockChain.size();
			for (int i = 0; i < longestChain.size() - Network.numberOfBlocksToReachConsensus; i++) {
				this.blockChain.put(longestChain.get(i).getThisBlockHashPointer(), longestChain.get(i).clone());
			}

			int stopAt = longestChain.size() - Network.numberOfBlocksToReachConsensus;
			for (int i = 0; i < stopAt; i++) {
				longestChain.remove(0);
			}
		
			ArrayList<Transaction> transactionsInBlocksMinedByMeButDropped = new ArrayList<Transaction>();
			for (int i = 0; i < this.forks.size(); i++) {
				if (i != indexOfLongestChain) {
					ChainedHashMap currentChain = this.forks.get(i);
					for (int j = 0; j < currentChain.size(); j++) {
						if (currentChain.get(j).getMiner().equals(this)) {
							for (Transaction trans : currentChain.get(j).getTransactions())
								transactionsInBlocksMinedByMeButDropped.add(trans);
						}
					}
				}
			}

			this.forks.clear();
			
			if(longestChain.size()!=0)
			this.forks.add(longestChain);
			
			updateUTXOSet(startingIndex, transactionsInBlocksMinedByMeButDropped);

			//runMyTransactionBuffer();
		}

	}

	public void updateUTXOSet(int startingIndex, ArrayList<Transaction> transactionsInBlocksMinedByMeButDropped) {

		 System.out.println("UPDATING UTXO SET!");
		 System.out.println("transactions in a block to mined by me: " +
		 transactionsInBlocksMinedByMeButDropped);
		 System.out.println("starting index: " + startingIndex);
		
		 System.out.println("OLD UTXO");
		 System.out.println(this.UTXOSet);
		 System.out.println("Traversing the blockchain");
		for (int i = startingIndex; i < this.blockChain.size(); i++) {
			Block currentBlock = this.blockChain.get(i);
			// System.out.println("current block is: " + currentBlock);

			for (Transaction trans : currentBlock.getTransactions()) {

				// System.out.println("current trans is: " + trans);
				if (transactionsInBlocksMinedByMeButDropped.contains(trans))
					transactionsInBlocksMinedByMeButDropped.remove(trans);

				if (trans instanceof PeerToPeerTransaction) {
					for (Trindex trin : ((PeerToPeerTransaction) trans).getInputs()) {
						// System.out.println("current trindex is: " + trin);
						if (this.UTXOSet.contains(trin)) {
							// System.out.println("Removing trindex: " + trin);
							UTXOSet.remove(trin);
						}

					}
					UTXOSet.add(new Trindex(trans.clone(), 0));
					if (((PeerToPeerTransaction) trans).getChange().getAmount() != 0)
						UTXOSet.add(new Trindex(trans.clone(), 1));
				} else
					UTXOSet.add(new Trindex(trans.clone(), 0));
			}
		}

		for (Transaction trans : transactionsInBlocksMinedByMeButDropped)
			if (!this.transactionsNotInABlock.contains(trans))
				this.transactionsNotInABlock.add(trans);

	}

	public boolean allTransactionsInThisBlockHaveValidInputs(Block block) {
		//TODO
		for (Transaction trans : block.getTransactions())
			if (!(this.validateTransactionIntegrity(trans) && this.validateTransactionOrigin(trans)))
				return false;
		return true;
	}

	public boolean nonceOfBlockIsCorrect(Block block) {
		return Network.firstNCharsEqualZero(block.getNonce() + Network.findAllStringsToBeHashed(block),
				Network.nonceTarget);
	}

	public boolean recievedThisBlockBefore(Block block) {
		for (ChainedHashMap possibleBlockChain : this.forks)
			if (possibleBlockChain.get(block.getThisBlockHashPointer()) != null)
				return true;
		for (int i = 0; i < this.blockChain.size(); i++)
			if (blockChain.get(block.getThisBlockHashPointer()) != null)
				return true;
		return false;
	}

	public void signTransaction(Transaction transaction) {
		try {
			byte[] data = transaction.getId().getBytes();
			Signature dsa = Signature.getInstance("SHA/DSA");
			dsa.initSign(privateKey);
			dsa.update(data);
			transaction.setSignature(dsa.sign());
		} catch (InvalidKeyException e) {
			System.err.println(e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			System.err.println(e.getMessage());
		} catch (SignatureException e) {
			System.err.println(e.getMessage());
		}
	}

	public boolean validateTransactionOrigin(Transaction transaction) {
		// validating signature
		try {
			if (transaction instanceof PeerToPeerTransaction) {
				byte[] data = transaction.getId().getBytes();
				Signature dsa = Signature.getInstance("SHA/DSA");
				dsa.initVerify(((PeerToPeerTransaction) transaction).getSenderPublicKey());
				/* Update and verify the data */
				dsa.update(data);
				return dsa.verify(transaction.getSignature());
			}
		} catch (NoSuchAlgorithmException e) {
			System.err.println(e.getMessage());
		} catch (InvalidKeyException e) {
			System.err.println(e.getMessage());
		} catch (SignatureException e) {
			System.err.println(e.getMessage());
		}
		return true;
	}

	public boolean validateTransactionIntegrity(Transaction transaction) {

		if (transaction instanceof PeerToPeerTransaction) {
			ArrayList<Trindex> inputs = ((PeerToPeerTransaction) transaction).getInputs();
			for (Trindex trin : inputs) {
				if (!UTXOSet.contains(trin))
					return false;
			}
		}
		return true;

	}

	public String findAllStringsToBeHashed(Transaction[] transactionsToBeAddedToABlock,
			String previousBlockHashPointer) {
		String res = "";
		for (Transaction tran : transactionsToBeAddedToABlock)
			res += tran.getId();
		res += previousBlockHashPointer;

		return res;
	}

	public Long findNonce(String input, int numberOfLeadingZeors) {
		boolean found = false;
		String hash = "";
		Long nonce = 0L;

		while (!found) {
			hash = Network.sha256(nonce + input);
			if (Network.firstNCharsEqualZero(hash, numberOfLeadingZeors)) {
				found = true;
				break;
			}

			nonce++;
		}

		return nonce;
	}

	public boolean equals(Object o) {
		return this.publicKey.equals(((Node) o).publicKey);
	}

	
	@Override
	public String toString() {
		return this.displayName;
	}
	public static void main(String[] args) {
		// Node n = new Node("test");
		// fancyPrint(n.findNonce("first", 1));

		// fancyPrint();
		// fancyPrint(n.sha256("ammar"));
		// fancyPrint(n.sha256("amma"));
		// fancyPrint(n.sha256("ammr"));
		// fancyPrint(n.sha256("ramma"));
	}

}
