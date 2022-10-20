// package dev.speakeasyapi.micronaut;

// import io.micronaut.context.event.BeanCreatedEvent;
// import io.micronaut.context.event.BeanCreatedEventListener;
// import io.micronaut.http.netty.channel.ChannelPipelineCustomizer;
// import io.micronaut.http.server.netty.NettyServerCustomizer;
// import io.netty.channel.Channel;

// public class SpeakeasyNettyServerCustomizer implements
// BeanCreatedEventListener<NettyServerCustomizer.Registry> {
// @Override
// public NettyServerCustomizer.Registry
// onCreated(BeanCreatedEvent<NettyServerCustomizer.Registry> event) {
// NettyServerCustomizer.Registry registry = event.getBean();
// registry.register(new Customizer(null)); //
// return registry;
// }

// private class Customizer implements NettyServerCustomizer { //
// private final Channel channel;

// Customizer(Channel channel) {
// this.channel = channel;
// }

// @Override
// public NettyServerCustomizer specializeForChannel(Channel channel,
// ChannelRole role) {
// return new Customizer(channel); //
// }

// @Override
// public void onStreamPipelineBuilt() {
// channel.pipeline().addBefore(ChannelPipelineCustomizer.HANDLER_HTTP_STREAM,
// "speakeasy",
// new SpeakeasyChannelDuplexHandler());
// }
// }
// }
