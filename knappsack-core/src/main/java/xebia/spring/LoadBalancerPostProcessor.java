package xebia.spring;

import fr.xebia.spring.security.web.access.channel.InsecureChannelProcessor;
import fr.xebia.spring.security.web.access.channel.SecureChannelProcessor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.security.web.access.channel.ChannelDecisionManagerImpl;

import static com.google.common.collect.Lists.newArrayList;

public class LoadBalancerPostProcessor implements BeanPostProcessor {

    SecureChannelProcessor secureChannelProcessor;

    InsecureChannelProcessor insecureChannelProcessor;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof ChannelDecisionManagerImpl) {
            ((ChannelDecisionManagerImpl) bean).setChannelProcessors(newArrayList(
                    insecureChannelProcessor,
                    secureChannelProcessor
            ));
        }
        return bean;
    }

    public void setSecureChannelProcessor(SecureChannelProcessor secureChannelProcessor) {
        this.secureChannelProcessor = secureChannelProcessor;
    }

    public void setInsecureChannelProcessor(InsecureChannelProcessor insecureChannelProcessor) {
        this.insecureChannelProcessor = insecureChannelProcessor;
    }
}
