package projet_sd.BibliothèqueJava;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public class ActiveMQQueue {

    public boolean checkQueueExists(String brokerJmxUrl, String queueName) {
        try {
            JMXServiceURL url = new JMXServiceURL(brokerJmxUrl);
            try (JMXConnector jmxc = JMXConnectorFactory.connect(url, null)) {
                MBeanServerConnection connection = jmxc.getMBeanServerConnection();

                ObjectName brokerName = new ObjectName("org.apache.activemq:type=Broker,brokerName=localhost");
                ObjectName[] queues = (ObjectName[]) connection.getAttribute(brokerName, "Queues");
                
                for(ObjectName queueObjectName: queues){
                    if(queueObjectName.getKeyProperty("destinationName").equals(queueName)){
                        return true;
                    }
                }

                return false;
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            return false;
        }
    }
    
}
