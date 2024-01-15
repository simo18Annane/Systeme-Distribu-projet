package projet_sd;

import java.io.Serializable;
import java.util.Arrays;
import java.util.function.Consumer;

import javax.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;

public class JMSManager {
    private ActiveMQConnectionFactory connectionFactory;
    private Connection connection;
    private Session session;
    private Queue srcQueue;
    private MessageProducer producer;
    private MessageConsumer consumer;
    private ActiveMQQueue queueCheck;
    private Consumer<Transaction> transactionConsumer;
    private Consumer<String> textMessagConsumer;
    private String localName;
    private boolean checkServer;

    public JMSManager(String localName) throws JMSException{
        this.localName = localName;
        this.queueCheck = new ActiveMQQueue();
        try {
            this.connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
            this.connectionFactory.setTrustAllPackages(false);
            this.connectionFactory.setTrustedPackages(Arrays.asList("projet_sd"));
            this.connection = connectionFactory.createConnection();
            this.connection.start();
            //etre notifié des problemes de connexion JMS
            connection.setExceptionListener(new ExceptionListener(){
                @Override
                public void onException(JMSException e){
                    System.err.println("Un problème de connexion a été détecté: " + e.getMessage());
                    checkServer = false;
                    reconnect();
                }
            });
            this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            this.srcQueue = session.createQueue(this.localName);
            this.consumer = session.createConsumer(srcQueue);
            this.checkServer = true;
            startListening();
        } catch (Exception e) {
            // TODO: handle exception
            System.out.println("Caught: " + e);
            e.printStackTrace();
        }
        
    }

    public void closeSessionConnection() throws JMSException{
        session.close();
        connection.close();
    }



    public void setTransactionConsumer(Consumer<Transaction> transactionConsumer){
        this.transactionConsumer = transactionConsumer;
    }

    public void setTextMessageConsumer(Consumer<String> textMessagConsumer){
        this.textMessagConsumer = textMessagConsumer;
    }


    public void sendTextMessage(String text, String destination) throws JMSException{
        String brokerJmxUrl = "service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi";
        if(queueCheck.checkQueueExists(brokerJmxUrl, destination)){
            try {
                Queue destQueue = session.createQueue(destination);
                producer = session.createProducer(destQueue);
                producer.setDeliveryMode(DeliveryMode.PERSISTENT);
                TextMessage message = session.createTextMessage(text);
                producer.send(message);
            } catch (Exception e) {
                // TODO: handle exception
            }
        } else {
            System.out.println("la destination n'existe pas");
        }
        
    }



    public void sendObjectMessage(Serializable object, String destination) throws JMSException{
        String brokerJmxUrl = "service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi";
        if(queueCheck.checkQueueExists(brokerJmxUrl, destination)){
            try {
                Queue destQueue = session.createQueue(destination);
                producer = session.createProducer(destQueue);
                producer.setDeliveryMode(DeliveryMode.PERSISTENT);
                ObjectMessage message = session.createObjectMessage(object);
                message.setObject(object);
                producer.send(message);
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        } else {
            System.out.println("La destination n'existe pas");
        }
    }

    public void startListening() throws JMSException{
        consumer.setMessageListener(message -> {
            try {
                Thread.sleep(10000);
                if(checkServer){
                    if(message instanceof TextMessage){
                        TextMessage textMessage = (TextMessage) message;
                        String text = textMessage.getText();
                        if(textMessagConsumer != null){
                            textMessagConsumer.accept(text);
                        }
                    } else if(message instanceof ObjectMessage){
                        ObjectMessage objectMessage = (ObjectMessage) message;
                        Serializable data = objectMessage.getObject();
                        if(data instanceof Transaction && transactionConsumer != null){
                            transactionConsumer.accept((Transaction) data);
                        }
                    }
                }
                } catch (Exception e) {
                    // TODO: handle exception
                    e.printStackTrace();
                }
            
        });
    }

    //tentative de reconnexion
    private void reconnect(){
        int max = 5;
        for(int i=0; i < max; i++){
            try{
                
                System.out.println("Tentative de reconnexion (" + (i+1) + "/" + max + ")");
                connection = connectionFactory.createConnection();
                connection.setExceptionListener(new ExceptionListener(){
                    @Override
                    public void onException(JMSException e){
                        System.err.println("Un problème de connexion a été détecté: " + e.getMessage());
                        checkServer = false;
                        reconnect();
                    }
                });
                connection.start();
                session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                srcQueue = session.createQueue(localName);
                consumer = session.createConsumer(srcQueue);
                startListening();
                System.out.println("Reconnexion réussie");
                checkServer = true;
                return;
            } catch(JMSException e){
                System.err.println("La reconnexion a échoué: " + e.getMessage());
                try {
                    connection.close();
                    Thread.sleep(5000);
                } catch (InterruptedException | JMSException e1) {
                    // TODO: handle exception
                }
            }
        }
        System.err.println("La reconnexion a échoué après " + max + " tentatives");
    }

}

