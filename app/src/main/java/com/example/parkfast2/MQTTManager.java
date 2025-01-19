package com.example.parkfast2;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.android.service.MqttAndroidClient;
import android.content.Context;

public class MQTTManager {

    private MqttAndroidClient mqttClient;

    public MQTTManager(Context context, String serverUri, String clientId) {
        mqttClient = new MqttAndroidClient(context, serverUri, clientId);
    }

    public void connect(IMqttActionListener callback) {
        try {
            MqttConnectOptions options = new MqttConnectOptions();
            mqttClient.connect(options, null, callback);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void subscribe(String topic, int qos, IMqttMessageListener messageListener) {
        try {
            mqttClient.subscribe(topic, qos, messageListener);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void publish(String topic, String message, int qos) {
        try {
            MqttMessage mqttMessage = new MqttMessage();
            mqttMessage.setPayload(message.getBytes());
            mqttMessage.setQos(qos);
            mqttClient.publish(topic, mqttMessage);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            mqttClient.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

}

