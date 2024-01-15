package projet_sd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import javax.jms.JMSException;

import projet_sd.BibliothèqueJava.JMSManager;

public class Bank {
    private String name;
    private Map<Integer, BankAccount> accounts;
    private boolean operationState;
    Scanner scanner = new Scanner(System.in);
    private JMSManager jmsmanager;
    private List<Transaction> bankTransaction;


    public Bank(String name){
        this.name = name;
        this.accounts = new HashMap<>();
        this.operationState = true;
        this.bankTransaction = new ArrayList<>();
        generationOfAccounts();
        
        try {
            this.jmsmanager = new JMSManager(name);
            this.jmsmanager.setTransactionConsumer(this::processTransaction);
            this.jmsmanager.setTextMessageConsumer(this::processTextMessage);
        } catch (JMSException e) {
            // TODO Auto-generated catch block
            System.out.println("Caught: " + e);
            e.printStackTrace();
        }

    }


    public String getName(){
        return this.name;
    }

    public boolean getOperationState(){
        return this.operationState;
    }

    public void addAccount(BankAccount account){
        accounts.put(account.getId(), account);
    }

    public BankAccount getAccount(int id){
        return accounts.get(id);
    }

    public void generationOfAccounts(){
        for(int i=0; i<10; i++){
            BankAccount account = new BankAccount(i+1, 1000);
            addAccount(account);
        }
    }

    public void addBankTransaction(Transaction t){
        bankTransaction.add(t);
    }

    public int getBankTransactionSize(){
        return this.bankTransaction.size();
    }

    public Transaction searchTransaction(int id){
        Transaction transaction = null;
        for(Transaction t : bankTransaction){
            if(t.getId() == id){
                transaction = t;
            }
        }
        if(transaction != null){
            return transaction;
        } else {
            return null;
        }
    }


    public void bankOperations(){
        System.out.println("Bienvenue dans votre Banque '" + getName() + "', Voici une liste des operations que vous pouvez effectuer: ");
        System.out.println("1- Visualiser le solde d'un compte et son historique des opérations.");
        System.out.println("2- Effectuer une transaction vers un autre compte dans une autre banque");
        while(getOperationState()){
            System.out.println("Saisir le numero d'opération a effectuée: ");
            int numberOp = scanner.nextInt();
            scanner.nextLine();

            switch(numberOp){
                case 1:{
                    Set<Integer> accountsId = accounts.keySet();
                    System.out.println("Voici les ids des comptes ouverts dans votre banque: ");
                    for(int id : accountsId){
                        System.out.println(id);
                    }
                    System.out.println("saisir le numero du compte que vous souhaitez visualiser: ");
                    int number = scanner.nextInt();
                    getAccount(number).displayAccount();
                } break;
                case 2:{
                    boolean verifBalance = false;
                    System.out.println("Depuis le compte: ");
                    int srcAccount = scanner.nextInt();
                    System.out.println("vers le compte: ");
                    int destAccount = scanner.nextInt();
                    scanner.nextLine();
                    System.out.println("Banque de destinataire: ");
                    String destBank = scanner.nextLine();
                    double amount = 0.0;
                    while(!verifBalance){
                        System.out.println("montant en €: ");
                        amount = scanner.nextDouble();
                        if(amount <= getAccount(srcAccount).getBalance()){
                            verifBalance = !verifBalance;
                        } else {
                            System.out.println("---->Solde insuffisant!!");
                        }
                    }

                    Transaction transaction = new Transaction(getBankTransactionSize()+1, srcAccount, this.getName(), destAccount, destBank, amount);
                    addBankTransaction(transaction);
                    try {
                        //this.jmsmanager.sendTextMessage(src + "," + dest + "," + destBank + "," + amount, destBank);
                        this.jmsmanager.sendObjectMessage(transaction, destBank);
                    } catch (JMSException e) {
                        // TODO Auto-generated catch block
                        System.out.println("Caught: " + e);
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void processTransaction(Transaction transaction){
        System.out.println("Traitement de la transaction: " + transaction.getAmount());//a supprimer
        Set<Integer> accountsId = accounts.keySet();
        for(int id : accountsId){
            if(id == transaction.getDestinationAccount()){
                getAccount(id).deposit(transaction.getAmount());
                getAccount(id).addTransactionHistory(transaction.descDestination());
                try {
                    this.jmsmanager.sendTextMessage(transaction.getId() + " Transaction effectuée", transaction.getSourceBank());
                } catch (JMSException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    public void processTextMessage(String text){
        System.out.println("Message texte recu: " + text);// a supprimer
        int idTransaction = 0;
        String[] parts = text.split(" ", 2);
        if(parts.length > 1){
            idTransaction = Integer.parseInt(parts[0]);
        }
        Transaction transaction = searchTransaction(idTransaction);
        getAccount(transaction.getSourceAccount()).withdraw(transaction.getAmount());
        getAccount(transaction.getSourceAccount()).addTransactionHistory(transaction.descSource());
        
    }

    public void closeConnexionJMS() throws JMSException{
        this.jmsmanager.closeSessionConnection();
    }

}
