package network;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Set;

import blocks.Block;
import blocks.ChainedHashMap;
import transactions.PeerToPeerTransaction;
import transactions.RewardTransaction;
import transactions.Transaction;
import transactions.Trindex;
import transactions.UTXOReward;

public class Network {

	public static Long startTime;
	public static double baseReward;
	public static int nonceTarget;
	public static int numberOfBlocksToReachConsensus;
	public static int numberOfTransactionsInABlock;
	private static int numberOfBlocksMinedBeforeReducingBaseReward;
	private static int numberOfBlocksMinedBeforeIncreasingNonceTarget;
	private static int currentNumberOfBlocks;
	private static int numberOfSubmissionsBeforeChoosingAWinner;

	private static ArrayList<Node> nodes;
	private static ArrayList<Node> minersToAddNextBlock;
	private static ArrayList<Block> submittedBlocks;

	public static void initNetwork(double startingReward, int startingTarget, int numberOfBlocksMinedBeforeIncreasingNonceTarget,int numberOfTransactionsInABlock,
			int numberOfBlocksMinedBeforeReducingBaseReward,int numberOfSubmissionsBeforeChoosingAWinner, int numberOfBlocksToReachConsensus) {
		startTime = System.currentTimeMillis();
		baseReward = startingReward;
		nonceTarget = startingTarget;
		Network.numberOfBlocksToReachConsensus = numberOfBlocksToReachConsensus;
		Network.numberOfBlocksMinedBeforeIncreasingNonceTarget = numberOfBlocksMinedBeforeIncreasingNonceTarget;
		Network.numberOfBlocksMinedBeforeReducingBaseReward = numberOfBlocksMinedBeforeReducingBaseReward;
		Network.numberOfSubmissionsBeforeChoosingAWinner = numberOfSubmissionsBeforeChoosingAWinner;
		minersToAddNextBlock = new ArrayList<Node>();
		submittedBlocks = new ArrayList<Block>();
		Network.numberOfTransactionsInABlock = numberOfTransactionsInABlock;
	}

	public static void addBlockToNetwork(Node miner, Block minedBlock) {
		
		if (minersToAddNextBlock.size() < numberOfSubmissionsBeforeChoosingAWinner) {
			if (nonceOfBlockIsCorrect(minedBlock)) {
				minersToAddNextBlock.add(miner);
				submittedBlocks.add(minedBlock);
				if (minersToAddNextBlock.size() == numberOfSubmissionsBeforeChoosingAWinner) {
					randomizeGossiping();
				}
			}
		}
	}

	static void randomizeGossiping() {
	
		//Pre empty them for further security and concurency access control
		Network.newBlockWasAdded();
		int i = 1; 
		while(!Network.minersToAddNextBlock.isEmpty())
		{
			int randomIndex = (int) ((Math.random() * minersToAddNextBlock.size()));
			Node chosenNode = Network.minersToAddNextBlock.remove(randomIndex);
			Block submittedBlock = Network.submittedBlocks.remove(randomIndex);
			System.out.println("NETWORK--NODE GOSSIPING: " + chosenNode.displayName + " order: " + i);
			i++;
			//TODO
			chosenNode.gossip(submittedBlock);
			//chosenNode.recieveBlock(submittedBlock, chosenNode);
		}
		Network.minersToAddNextBlock.clear();
		Network.submittedBlocks.clear();
		
	}

	static boolean nonceOfBlockIsCorrect(Block minedBlock) {
		
		return (minedBlock == null) ? false
				: Network.firstNCharsEqualZero(Network.sha256(minedBlock.getNonce() + Network.findAllStringsToBeHashed(minedBlock)),
						Network.nonceTarget);

	}

	public static void newBlockWasAdded() {
		Network.currentNumberOfBlocks++;
		if (Network.currentNumberOfBlocks % Network.numberOfBlocksMinedBeforeReducingBaseReward == 0)
			Network.baseReward /= 2;
	//TODO
//			if (Network.currentNumberOfBlocks % Network.numberOfBlocksMinedBeforeIncreasingNonceTarget == 0)
//			Network.nonceTarget++;

	}

	static void generateNodes(int numberOfNodes) {
		ArrayList<Node> nodes = new ArrayList<Node>();
		for (int i = 0; i < numberOfNodes; i++) {
			nodes.add(new Node("node-" + i));
		}

		Network.nodes = nodes;
	}

	static void generateRandomConnections() {

		for (int i = 0; i < nodes.size(); i++) {

			Node currentNode = nodes.get(i);

			int numberOfNeighbhors = (int) ((Math.random() * nodes.size())+1);

			for (int j = 0; j < numberOfNeighbhors; j++) {
				int randomIndex = (int) ((Math.random() * nodes.size()));

				if (randomIndex != i)
					currentNode.addNeighbor(nodes.get(randomIndex));
				else
					j--;
			}
		}

	}

	static void runTestTransactions(int numberOfTransactions) {

		for (int i = 0; i < numberOfTransactions; i++) {
			int senderIndex = (int) ((Math.random() * nodes.size()));
			int recieverIndex = (int) ((Math.random() * nodes.size()));
			nodes.get(senderIndex).makeTransaction(nodes.get(recieverIndex).getPublicKey(),
					nodes.get(recieverIndex).getDisplayName(), Math.random() * 50, Math.random() * 5);
		}

	}
	
	static void runTestTransactionsSequence(int numberOfTransactions) {

		for (int i = 0; i < numberOfTransactions; i++) {
			nodes.get(i).makeTransaction(nodes.get((i+1)%nodes.size()).getPublicKey(),
					nodes.get((i+1)%nodes.size()).getDisplayName(), Math.random() * 50, Math.random() * 5);
		}

	}


	static void printNetworkToplogy() {

		for (Node node : nodes) {
			System.out.println(node.displayName);
			System.out.println("Connected to: ");
			for (int i = 0; i < node.getNeighborsSize(); i++) {
				System.out.print(node.getConnectedNeighbors().get(i).displayName + " , ");
			}
			System.out.println();
			System.out.println("-------------------------------");
			System.out.println();
		}

	}

	static void startSimulation(int numberOfNodes, int numberOfTransactions) {
		generateNodes(numberOfNodes);
		generateRandomConnections();
		bootstrapNetwork();
		printNetworkToplogy();
		runTestTransactions(numberOfTransactions);
	//	runTestTransactionsSequence(numberOfNodes);
		printBlockChainsAndForks();

	}
	
	static void bootstrapNetwork() {
		
		Transaction [] blockTrans = new Transaction[nodes.size()];
		
		for(int i=0; i<blockTrans.length;i++)
			blockTrans[i] =  new RewardTransaction(new UTXOReward(nodes.get(i).getPublicKey(), nodes.get(i).getDisplayName(),Network.baseReward));
		
		Block block1 = new Block(blockTrans, "first", "0a5a847ed6d392d8bd44746463d7cb8ef2280f347aa70dbb0495edcc29dae0d3", 6L,
				 new Timestamp(System.currentTimeMillis()), null,  1);
		
		ChainedHashMap blocks = new ChainedHashMap();
		
		blocks.put(block1.getThisBlockHashPointer(),block1);
		
		for(int i=0; i<nodes.size();i++)
			nodes.get(i).bootstrap(blocks);
	}

	static void addNodeToNetwork(int numberOfTransactions) {
		Node newNode = new Node("node-" + Network.nodes.size());
      
		int numberOfNeighbhors = (int) (Math.random() * nodes.size());
		for (int j = 0; j < numberOfNeighbhors; j++) {
			int randomIndex = (int) ((Math.random() * nodes.size()));
			if (randomIndex != Network.nodes.size())
				newNode.addNeighbor(nodes.get(randomIndex));
			else
				j--;
		}
		int randomNeighborIndex = (int) Math.random() *newNode.getNeighborsSize();
		
		newNode.bootstrap(newNode.connectedNeighbors.get(randomNeighborIndex).blockChain);
		Network.nodes.add(newNode);
		System.out.println();
		System.out.println("----------- Added new node to the network ------------------");
		System.out.println();
		printNetworkToplogy();
		runTestTransactions(numberOfTransactions);
	}
	

	public static void printBlockChainsAndForks() {
	
		for(Node node: nodes)
			node.printBlockChainsAndForks();
		
	}

	static String findAllStringsToBeHashed(Block block) {
		String res = "";
		for (Transaction tran : block.getTransactions())
			res += tran.getId();
		res += block.getPreviousBlockHashPointer();
		return res;
	}

	public static String sha256(String input) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(input.getBytes("UTF-8"));
			StringBuffer hexString = new StringBuffer();

			for (int i = 0; i < hash.length; i++) {
				String hex = Integer.toHexString(0xff & hash[i]);
				if (hex.length() == 1)
					hexString.append('0');
				hexString.append(hex);
			}
			return hexString.toString();
		} catch (Exception ex) {
			System.err.println("Can not hash: " + input);
			return "";
		}

	}

	public static boolean firstNCharsEqualZero(String str, int numberOfChars) {
		for (int i = 0; i < numberOfChars; i++)
			if (str.charAt(i) != '0')
				return false;

		return true;
	}
	
	public static void main(String[] args) throws FileNotFoundException {

		
		//TODO el ydedk mtdeloosh
//		Network.startTime = System.currentTimeMillis();
		 Network.initNetwork(50,2,5,2,10,4,3);
		 startSimulation(7,50);
		
		 Node ammar = new Node("Ammar");
		 Node leo = new Node("Leo");
		 Node kamal = new Node("Kamal");
		 Node salah = new Node("Salah");
		 
		 ammar.addNeighbor(kamal);
		 kamal.addNeighbor(ammar);
		 
		 UTXOReward reward1 = new UTXOReward(ammar.getPublicKey(), ammar.getDisplayName(),1000);
		 Transaction trans1 = new RewardTransaction(reward1);
		 trans1.id = "Trans 1 in B1";
		 UTXOReward reward2 = new UTXOReward(kamal.getPublicKey(), kamal.getDisplayName(),1000);
		 Transaction trans2 = new RewardTransaction(reward2);
		 trans2.id = "Trans 2 in B1";
		 
		 UTXOReward reward3 = new UTXOReward(kamal.getPublicKey(), kamal.getDisplayName(),1000);
		 Transaction trans3 = new RewardTransaction(reward3);
		 trans3.id = "Trans 3 in B2";
		 UTXOReward reward4 = new UTXOReward(leo.getPublicKey(), leo.getDisplayName(),1000);
		 Transaction trans4 = new RewardTransaction(reward4);
		 trans4.id = "Trans 4 in B2";
		 
		 UTXOReward reward5 = new UTXOReward(ammar.getPublicKey(), ammar.getDisplayName(),1000);
		 Transaction trans5 = new RewardTransaction(reward5);
		 trans5.id = "Trans 5 in B3x";
		 UTXOReward reward6 = new UTXOReward(salah.getPublicKey(), salah.getDisplayName(),1000);
		 Transaction trans6 = new RewardTransaction(reward6);
		 trans6.id = "Trans 6 in B3x";
		 
		 UTXOReward reward7 = new UTXOReward(kamal.getPublicKey(), kamal.getDisplayName(),1000);
		 Transaction trans7 = new RewardTransaction(reward7);
		 trans7.id = "Trans 7 in B3y";
		 UTXOReward reward8 = new UTXOReward(leo.getPublicKey(), leo.getDisplayName(),1000);
		 Transaction trans8 = new RewardTransaction(reward8);
		 trans8.id = "Trans 8 in B3y";
		 
		 UTXOReward reward9 = new UTXOReward(kamal.getPublicKey(), kamal.getDisplayName(),1000);
		 Transaction trans9 = new RewardTransaction(reward7);
		 trans9.id = "Trans 9 in B4x";
		 UTXOReward reward10 = new UTXOReward(leo.getPublicKey(), leo.getDisplayName(),1000);
		 Transaction trans10 = new RewardTransaction(reward8);
		 trans10.id = "Trans 10 in B4x";
		 
		 
		 ArrayList<Trindex> inputs1 = new ArrayList<Trindex>();
		 Trindex trin0 = new Trindex(trans1,0);
		 inputs1.add(trin0);
		 PeerToPeerTransaction ammar2kamal = new PeerToPeerTransaction(ammar.getPublicKey(),kamal.getPublicKey(),ammar.displayName,kamal.displayName,20,0,inputs1);
		 ammar2kamal.id = "Ammar to kamal 1";
		 ammar.signTransaction(ammar2kamal);
		 
		 ArrayList<Trindex> inputs2 = new ArrayList<Trindex>();
		 inputs2.add(new Trindex(trans2,0));
		 //inputs2.add(trin0.clone());
		 PeerToPeerTransaction kamal2ammar = new PeerToPeerTransaction(kamal.getPublicKey(),ammar.getPublicKey(),kamal.displayName,ammar.displayName,20,0.1,inputs2);
		 kamal2ammar.id = "kamal to ammar 1";
		 kamal.signTransaction(kamal2ammar);
		 
		 Trindex trin1 = new Trindex(ammar2kamal,1);
		 ArrayList<Trindex> inputs3 = new ArrayList<Trindex>();
		 inputs3.add(trin0);
		 PeerToPeerTransaction ammar2kamal2 = new PeerToPeerTransaction(ammar.getPublicKey(),kamal.getPublicKey(),ammar.displayName,kamal.displayName,20,0,inputs3);
		 ammar2kamal2.id = "Ammar to kamal 2";
		 ammar.signTransaction(ammar2kamal2);
		 
		 
		 Trindex trin2 = new Trindex(kamal2ammar,1);
		 ArrayList<Trindex> inputs4 = new ArrayList<Trindex>();
		 inputs4.add(trin2);
		 PeerToPeerTransaction kamal2ammar2 = new PeerToPeerTransaction(kamal.getPublicKey(),ammar.getPublicKey(),kamal.displayName,ammar.displayName,20,0.1,inputs4);
		 kamal2ammar2.id = "kamal to ammar 2";
		 kamal.signTransaction(kamal2ammar2);
		 
		 Transaction [] blockTrans1 = new Transaction[2];
		 blockTrans1[0] = trans1;
		 blockTrans1[1] = trans2;
		 
		 Transaction [] blockTrans2 = new Transaction[2];
		 blockTrans2[0] = trans3;
		 blockTrans2[1] = trans4;
		 
		 Transaction [] blockTrans3x = new Transaction[2];
		 blockTrans3x[0] = ammar2kamal;
		 blockTrans3x[1] = kamal2ammar;
		 
		 Transaction [] blockTrans3y = new Transaction[2];
		 blockTrans3y[0] = trans7;
		 blockTrans3y[1] = trans8;
		 

		 Transaction [] blockTrans4x = new Transaction[2];
		 blockTrans4x[0] = ammar2kamal;
		 blockTrans4x[1] = ammar2kamal2;

		Block block1 = new Block(blockTrans1, "first", "0a5a847ed6d392d8bd44746463d7cb8ef2280f347aa70dbb0495edcc29dae0d3", 6L,
				 new Timestamp(System.currentTimeMillis()), null,  1);
		
		Block block2 = new Block(blockTrans4x, "first", "0a5a847ed6d392d8bd44746463d7cb8ef2280f347aa70dbb0495edcc29dae0d3", 6L,
				 new Timestamp(System.currentTimeMillis()), null,  1);
		
		ChainedHashMap chm = new ChainedHashMap();
		chm.put(block1.getThisBlockHashPointer(), block1);
//		chm.put(block2.getThisBlockHashPointer(), block2);

		ammar.bootstrap(chm);
//		leo.bootstrap(chm);
		kamal.bootstrap(chm);
		salah.bootstrap(chm);


		
//		System.out.println(ammar2kamal);
//		System.out.println(ammar2kamal2);
//		System.out.println(kamal2ammar);
//		System.out.println(kamal2ammar2);
		
		
//		ammar.UTXOSet.add(trin1);
//		ammar.UTXOSet.add(trin2);
//		kamal.UTXOSet.add(trin1);
//		kamal.UTXOSet.add(trin2);
		
////		ammar.recieveTransaction(ammar2kamal, ammar);
//		ammar.recieveTransaction(ammar2kamal2, ammar);
//		kamal.recieveTransaction(ammar2kamal, ammar);
//		kamal.recieveTransaction(ammar2kamal2, ammar);
//		
//		ammar.recieveTransaction(kamal2ammar, kamal);
//		ammar.recieveTransaction(kamal2ammar2, kamal);
////		
//		ammar.fancyPrint("Block chain ammar");
//		ammar.fancyPrint(ammar.blockChain.toString());
//		ammar.fancyPrint("Forks ammar");
//		ammar.fancyPrint(ammar.forks.toString());
//		
//		kamal.fancyPrint("Block chain kamal");
//		kamal.fancyPrint(kamal.blockChain.toString());
//		kamal.fancyPrint("Forks kamal");
//		kamal.fancyPrint(kamal.forks.toString());
		
		
//		for(int i=0; i<ammar.blockChain.size();i++)
//		ammar.fancyPrint("Ammar's testing " + ammar.noCommonInputsBetweenTransactionsInThisBlock(ammar.blockChain.get(i)));
//		
//		for(int i=0; i<ammar.forks.size();i++)
//			for(int j=0; j<ammar.forks.get(i).size();j++)
//			ammar.fancyPrint("Ammar's testing " + ammar.noCommonInputsBetweenTransactionsInThisBlock(ammar.forks.get(i).get(j)));
//			
		
		
//		System.out.println(ammar.UTXOSet);
//		System.out.println(ammar.validateTransactionIntegrity(kamal2ammar2));

		
//		ammar.fancyPrint("UTXO 1");
//		System.out.println(ammar.UTXOSet);
//		
//		System.out.println("UTXO 2");
//		System.out.println(ammar.UTXOSet);
//		System.out.println("Tx to be mined");
//		System.out.println(ammar.transactionsNotInABlock);
//	
//
//		
//		System.out.println(ammar.blockChain);
//		System.out.println("forks");
//		System.out.println(ammar.forks);
		
		
		

		 
	}

}
