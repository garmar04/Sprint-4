package com.example.parkfast2;

import android.content.Context;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;

public class MQTTHandler {

    private final MQTTManager mqttManager;
    private final ParkingManager parkingManager;

    public MQTTHandler(Context context, ParkingManager parkingManager) {
        this.mqttManager = new MQTTManager(context, "tcp://172.20.10.10:1883", "AndroidClient");
        this.parkingManager = parkingManager;
    }

    public void start() {
        mqttManager.connect(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                // Suscribirse al topic
                mqttManager.subscribe("parking/idTagEntrada", 1, (topic, message) -> {
                    String idTarjeta = new String(message.getPayload());
                    // Verificar reserva y responder
                    parkingManager.verificarReserva(idTarjeta, resultado -> {
                        mqttManager.publish("parking/respuesta", resultado, 1);
                    });
                });
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                exception.printStackTrace();
            }
        });
    }

    public void stop() {
        // Desconectar MQTT cuando no sea necesario
        mqttManager.disconnect();
    }
}
