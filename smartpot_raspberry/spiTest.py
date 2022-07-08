
import mymqtt
try:
    mqttworker = mymqtt.MqttWorker()
    mqttworker.mymqtt_connect()
except KeyboardInterrupt:
    pass
finally:
    pass