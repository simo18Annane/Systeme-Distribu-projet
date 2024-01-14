package projet_sd;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        if(args.length > 0){
            String bankName = args [0];
            Bank bank = new Bank(bankName);
            bank.bankOperations();
        } else {
            System.out.println("Veuillez fournir un nom de banque.");
        }
    }
}
