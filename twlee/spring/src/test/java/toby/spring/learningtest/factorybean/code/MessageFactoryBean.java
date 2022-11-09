package toby.spring.learningtest.factorybean.code;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Component;

@Component("message")
public class MessageFactoryBean implements FactoryBean<Message> {
    private final String text;

    public MessageFactoryBean() {
        this.text = "Factory Bean";
    }

    @Override
    public Message getObject() throws Exception {
        return Message.newMessage(text);
    }

    @Override
    public Class<? extends Message> getObjectType() {
        return Message.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}
