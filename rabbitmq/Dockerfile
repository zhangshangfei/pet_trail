FROM rabbitmq:3-management-alpine

ARG PLUGIN_VERSION=3.13.0

RUN apk add --no-cache curl \
    && curl -L "https://github.com/rabbitmq/rabbitmq-delayed-message-exchange/releases/download/v${PLUGIN_VERSION}/rabbitmq_delayed_message_exchange-${PLUGIN_VERSION}.ez" \
       -o /opt/rabbitmq/plugins/rabbitmq_delayed_message_exchange-${PLUGIN_VERSION}.ez \
    && rabbitmq-plugins enable --offline rabbitmq_delayed_message_exchange \
    && apk del curl
