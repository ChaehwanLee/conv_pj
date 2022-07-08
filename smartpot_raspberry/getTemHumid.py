from threading import Thread
import board #데이터 송신용 board모듈
import adafruit_dht
import time

class getdht11(Thread):
    def __init__(self):
        Thread.__init__(self)
        dht22 = board.D13
        self.mydht22 = adafruit_dht.DHT22(dht22)
        self.humidity_data = 66
        self.temperature_data = 20
    
    def gethumid11(self):
        return self.humidity_data
    
    def gettemp11(self):
        return self.temperature_data
    
    def run(self):
        try:
            while True:
                # 온습도 값 가져오기
                try:
                    self.humidity_data = self.mydht22.humidity
                    self.temperature_data = self.mydht22.temperature
                except RuntimeError as error:
                    print(error.args[0])
                finally:
                    pass
                time.sleep(10)
        except KeyboardInterrupt:
            pass
        finally:
            pass
            