package chat.rocket.android.wallet;

import android.content.Context;

import org.web3j.crypto.Bip39Wallet;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import chat.rocket.android.util.OwnWalletUtils;
import chat.rocket.android.wallet.ui.TransactionViewModel;
import timber.log.Timber;


public class BlockchainInterface {

    private Web3j web3;

    /* Example Network
        EXPLORER_URL = "https://rinkeby.etherscan.io/";
        TX_ADDON = "tx/";
        RPC_URL = "https://rinkeby.infura.io/<your token>";
     */
    public static final String EXPLORER_URL = "https://etherscan.io/";
    public static final String TX_ADDON = "tx/";
    public static final String RPC_URL = "";

    public BlockchainInterface() {
        // Connect to the private ethereum network via the json rpc url
        HttpService httpService = new HttpService(RPC_URL);
        this.web3 = Web3jFactory.build(httpService);
    }

    /**
     * Get transaction information for a list of transaction hashes that are associated with
     *  the current user for the purpose of displaying the transaction history
     *
     * @param userWalletAddress the user's wallet address
     * @param transactionHashes List of Strings which are transaction hashes
     * @return List of TransactionViewModel containing data for displaying the transactions
     */
    public List<TransactionViewModel> getTransactions(String userWalletAddress, List<String> transactionHashes) {
        // Get web3 Transaction objects from the hashes
        List<Transaction> transactions = new ArrayList<>();
        try {
            for (String tx : transactionHashes) {
                transactions.add(web3.ethGetTransactionByHash(tx).send().getTransaction());
            }
        } catch (Exception ex) {
            Timber.e(ex);
            return new ArrayList<>();
        }

        // Map the Transaction objects to TransactionViewModels
        List<TransactionViewModel> txModels = new ArrayList<>();
        for (Transaction tx: transactions) {
            Boolean sentFromUser = tx.getFrom().substring(2).equals(userWalletAddress);
            txModels.add(new TransactionViewModel(tx.getHash(),
                    Convert.fromWei(tx.getValue().toString(), Convert.Unit.ETHER),
                    getTimeStamp(tx).longValue(),
                    sentFromUser));
        }
        Collections.reverse(txModels);
        return txModels;
    }

    /**
     * Check if a given wallet address has a corresponding wallet file stored on the device
     */
    public Boolean walletFileExists(Context c, String address) {
        return getFilesByAddress(c, address).length > 0;
    }

    /**
     * Get balance in Wei of a wallet
     * @param address the address/public key of the wallet
     * @return BigDecimal amount of Wei in the wallet or
     *          -1 if the call failed
     */
    public BigDecimal getBalance(String address) {
        try {
            EthGetBalance ethGetBalance = this.web3
                    .ethGetBalance(addAddressPrefix(address), DefaultBlockParameterName.LATEST)
                    .sendAsync()
                    .get();
            return Convert.fromWei(ethGetBalance.getBalance().toString(), Convert.Unit.ETHER);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return BigDecimal.valueOf(-1);
    }

    /**
     * Create a new account on the blockchain and save private key file to internal storage
     * @param password user's password to make the account/wallet
     * @param c the app's current Context/Activity
     * @return a String array of the wallet address and the mnemonic
     */
    public String[] createBip39Wallet(Context c, String password) {
        String address = "";
        String mnemonic = "";
        try {
            Bip39Wallet wallet = OwnWalletUtils.generateBip39Wallet(password, c.getFilesDir());
            address = getAddressFromFileName(wallet.getFilename());
            mnemonic = wallet.getMnemonic();
        } catch (Exception e) {
            Timber.d("Failed creating Bip39Wallet! Exception: %s", e.getMessage());
            // printStackTrace method
            // prints line numbers + call stack
            e.printStackTrace();

            // Prints what exception has been thrown
            System.out.println(e);
        } finally {

            return new String[]{address, mnemonic};
        }
    }

    /**
     * Initiate a transaction on the blockchain
     * @param password user/sender's wallet password
     * @param sender wallet address
     * @param recipient wallet address
     * @param amount Double amount of ether to send
     * @param c Context/Activity
     * @return String of the hash of the initiated transaction,
     *          return empty string if transaction initiation failed
     *
     * @throws Exception for errors such as wrong password, no private file found,
     *                      or other errors in the transaction sending process
     */
    public String sendTransaction(Context c, final String password, String sender, String recipient, Double amount)
            throws Exception {
        // Find the sender's private key file
        File[] files = getFilesByAddress(c, sender);
        if (files.length == 0)
            throw new FileNotFoundException("Private key file not found");

        // Load sender's credentials
        Credentials credentials = WalletUtils.loadCredentials(password, files[0]);

        // Get next nonce
        EthGetTransactionCount ethGetTransactionCount = web3.ethGetTransactionCount(
                addAddressPrefix(sender), DefaultBlockParameterName.LATEST).send();
        BigInteger nonce = ethGetTransactionCount.getTransactionCount();

        // Create raw transaction
        RawTransaction rawTransaction = RawTransaction.createEtherTransaction(
                nonce,
                web3.ethGasPrice().send().getGasPrice(),
                BigInteger.valueOf(4000000), // TODO find a better way to determine gas limit
                addAddressPrefix(recipient),
                Convert.toWei(BigDecimal.valueOf(amount), Convert.Unit.ETHER).toBigInteger());

        // Sign the transaction
        byte[] signedTx = TransactionEncoder.signMessage(rawTransaction, credentials);

        // Send transaction
        String hexValue = Numeric.toHexString(signedTx);
        String txHash = web3.ethSendRawTransaction(hexValue).send().getTransactionHash();
        // txHash is sometimes null, meaning the transaction was unable to be sent or is invalid
        if (txHash == null) {
            throw new Exception("Transaction Failed.");
        }
        return txHash;
    }

    /**
     * Check the formatting of an address
     *
     * @param address wallet address that will be checked
     * @return Boolean value if the address is valid or not
     */
    public Boolean isValidAddress(String address) {
        return (address.startsWith("0x") && address.length() == 42) ||
                (address.length() == 40);
    }

    /**
     * Get the time the transaction was mined by getting the block it is in
     * @param tx
     * @return timestamp as BigInteger value
     */
    private BigInteger getTimeStamp(Transaction tx) {
        try {
            return web3.ethGetBlockByHash(tx.getBlockHash(), false).sendAsync().get().getBlock().getTimestamp();
        } catch (Exception ex) {
            return BigInteger.ZERO;
        }
    }

    /**
     * Find all files in the app's internal storage directory that have the given
     *  wallet address in the file name
     * @param address Wallet address to search for in the file names
     * @param c Context of the app's current activity
     * @return array of Files
     */
    private File[] getFilesByAddress(Context c, String address) {
        final String addr = removeAddressPrefix(address);
        File[] fileList = c.getFilesDir().listFiles(new FileFilter(){
            @Override
            public boolean accept(File file) {
                return file.getName().contains(addr);
            }
        });

        return fileList;
    }

    /**
     * Parse the address that is in a private key file name
     * @param fileName the file name of the private key file
     * @return The wallet address (Not prepended with "0x")
     */
    private String getAddressFromFileName(String fileName) {
        // A file is in the format of "*--*--[address].json"
        return fileName.split("--",3)[2].split("\\.",2)[0];
    }

    private String addAddressPrefix(String address) {
        if (!address.startsWith("0x")) {
            address = "0x" + address;
        }
        return address;
    }

    private String removeAddressPrefix(String address) {
        if (address.startsWith("0x")) {
            address = address.substring(2);
        }
        return address;
    }
}
