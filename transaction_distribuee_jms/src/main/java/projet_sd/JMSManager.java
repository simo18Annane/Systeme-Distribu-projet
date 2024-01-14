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

    public JMSManager(String localName) throws JMSException{
        this.queueCheck = new ActiveMQQueue();
        try {
            this.connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
            this.connectionFactory.setTrustAllPackages(false);
            this.connectionFactory.setTrustedPackages(Arrays.asList("projet_sd"));
            this.connection = connectionFactory.createConnection();
            this.connection.start();
            this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            this.srcQueue = session.createQueue(localName);
            this.consumer = session.createConsumer(srcQueue);
        } catch (Exception e) {
            // TODO: handle exception
            System.out.println("Caught: " + e);
            e.printStackTrace();
        }
        startListening();
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
                producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
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
                producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
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

/* 
    public void startListening() throws JMSException{
        consumer.setMessageListener(new MessageListener(){
            @Override
            public void onMessage(Message message){
                if(message instanceof TextMessage){
                    TextMessage textMessage = (TextMessage) message;
                    try{
                        String text = textMessage.getText();
                        System.out.println("Message recu: " + text);
                    } catch (JMSException e){
                        e.printStackTrace();
                    }
                }
                if(message instanceof ObjectMessage){
                    ObjectMessage objectMessage = (ObjectMessage) message;
                    Serializable data = null;
                    try {
                        data = objectMessage.getObject();
                    } catch (JMSException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    /
                    if(data instanceof Transaction){
                        Transaction transaction = (Transaction) data;
                        System.out.println(transaction.getSource());
                    }

                    //System.out.println(data);
                }    
            }
        });
    }*/

    /* 
    public void startListening() throws JMSException{
        consumer.setMessageListener(message -> {
            if(message instanceof ObjectMessage){
                ObjectMessage objectMessage = (ObjectMessage) message;
                try {
                    Serializable data = objectMessage.getObject();
                    if(data instanceof Transaction){
                        if(transactionConsumer != null){
                            transactionConsumer.accept((Transaction) data);
                        }
                    }
                } catch (JMSException e){
                    e.printStackTrace();
                }
            }
        });
    }*/

    public void startListening() throws JMSException{
        consumer.setMessageListener(message -> {
            try {
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
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        });
    }
}

