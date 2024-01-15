package projet_sd;

import javax.jms.JMSException;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws JMSException
    {
        if(args.length > 0){
            String bankName = args [0];
            Bank bank = new Bank(bankName);
            bank.bankOperations();
            bank.closeConnexionJMS();
        } else {
            System.out.println("Veuillez fournir un nom de banque.");
        }
    }
}
